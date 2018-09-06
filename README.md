
package com.org.support.metaservice.business;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.common.utils.Log;
import com.org.support.idgenerateservice.service.IDGenerateService;
import com.org.support.metaservice.business.cache.AsyncCleanTermRedisService;
import com.org.support.metaservice.business.cache.TermCache;
import com.org.support.metaservice.dao.TermDao;
import com.org.support.metaservice.errcode.TermErrCode;
import com.org.support.metaservice.jms.MetaJMSMessage;
import com.org.support.metaservice.jms.MetaMsgSenderService;
import com.org.support.metaservice.model.Term;
import com.org.support.metaservice.model.TermHierarchy;
import com.org.support.metaservice.model.TermModification;
import com.org.support.metaservice.model.Vocabulary;
import com.org.tools.commonlib.exception.ServiceException;

//import com.org.support.jms.json.JSONUtil;

/**
 * 
 * TermBusiness 实现类
 * 
 */
@Component
public class TermBusiness
{
    /**
     * logger
     */
    private static final Logger logger = LoggerFactory.getLogger(TermBusiness.class);
    
    /**
     * 根节点Id
     */
    String ROOT_TERM_ID = "Root";
    
    String KEY_ID_TYPE_TERM = "Term";
    
    String KEY_ID_TERM_MODIFICATION = "TERM_MD";
    
    /**
     * vocabularyBusiness
     */
    @Autowired
    private VocabularyBusiness vocabularyBusiness;
    
    /**
     * Cassandra 缓存读写类
     */
    @Autowired
    private TermCache cache;
    
    @Autowired
    private MetaMsgSenderService listenerService;
    
    @Autowired
    private AsyncCleanTermRedisService cleanTermRedisService;
    
    public void setListenerService(MetaMsgSenderService listenerService)
    {
        this.listenerService = listenerService;
    }
    
    /**
     * TermDao 数据库操作类
     */
    @Autowired
    private TermDao dao;
    
    public TermDao getDao()
    {
        return dao;
    }
    
    public void setDao(TermDao dao)
    {
        this.dao = dao;
    }
    
    public VocabularyBusiness getVocabularyBusiness()
    {
        return vocabularyBusiness;
    }
    
    public void setVocabularyBusiness(VocabularyBusiness vocabularyBusiness)
    {
        this.vocabularyBusiness = vocabularyBusiness;
    }
    
    public TermCache getCache()
    {
        return cache;
    }
    
    public void setCache(TermCache cache)
    {
        this.cache = cache;
    }
    
    public IDGenerateService getIdGenerateService()
    {
        return idGenerateService;
    }
    
    public void setIdGenerateService(IDGenerateService idGenerateService)
    {
        this.idGenerateService = idGenerateService;
    }
    
    public Comparator<Term> getTermComparator()
    {
        return termComparator;
    }
    
    public void setTermComparator(Comparator<Term> termComparator)
    {
        this.termComparator = termComparator;
    }
    
    /**
     * ID 生成器帮助类
     */
    @Resource(name = "idGenerateService")
    private IDGenerateService idGenerateService;
    
    /**
     * Term比较器
     */
    private Comparator<Term> termComparator = new Comparator<Term>()
    {
        @Override
        public int compare(Term o1, Term o2)
        {
            if (o1.getWeight() != o2.getWeight())
            {
                return o1.getWeight() - o2.getWeight();
            }
            else
            {
                return o1.getId().compareTo(o2.getId());
            }
        }
    };
    
    /**
     * <根据给定的Level和Vid，获取对应的Vocabulary的对应级别的Term Level
     * 从1开始， 第1级Term表示Vocabulary所有顶层Term，
     * 第2级Term表示Vocabulary所有顶层Term的所有子Term，依次类推。
     * Level小于1或者Level大于Vocabulary的最大级别时，返回null。
     */
    public List<Term> getTermsByLevel(String vid, int level)
    {
        if (StringUtils.isBlank(vid) || level < 1)
        {
            return null;
        }
        
        //获取level 1节点
        List<Term> topTerms = getTopTermsByVid(vid);
        
        //如果顶层节点为空直接返回，
        if (topTerms == null || topTerms.isEmpty())
        {
            return topTerms;
        }
        
        //如果是Level为1，直接返回顶层节点。
        if (level == 1)
        {
            Collections.sort(topTerms, termComparator);
            return topTerms;
        }
        
        List<Term> result = new ArrayList<Term>();
        List<Term> tempTopTerms = topTerms;
        List<Term> tempTerms = null;
        
        int clevel = 1;
        
        //循环找子节点
        while ((++clevel) <= level)
        {
            result.clear();
            //如果父节点已经为空，则表示Level已经超出了Vocabulary的最大层级，直接结束循环
            if (tempTopTerms != null && !tempTopTerms.isEmpty())
            {
                //将所有的子节点加到结果中去
                for (Term term : tempTopTerms)
                {
                    tempTerms = dao.getSubterms(term.getId());
                    if (tempTerms != null)
                    {
                        
                        if (!CollectionUtils.isEmpty(tempTerms))
                        {
                            // 去重添加（id 相同)，认为相等
                            for (Term terAdding : tempTerms)
                            {
                                boolean isDuplicate = false;
                                
                                for (Term temAdded : result)
                                {
                                    if (terAdding.getId().equals(temAdded.getId()))
                                    {
                                        isDuplicate = true;
                                        break;
                                    }
                                }
                                
                                // 没有重复
                                if (!isDuplicate)
                                {
                                    result.add(terAdding);
                                }
                            }
                        }
                        
                        // result.addAll(tempTerms);
                    }
                }
                //将当前级别的子节点赋给临时变量作为父节点，再次查找子节点
                tempTopTerms.clear();
                tempTopTerms.addAll(result);
            }
            else
            {
                break;
            }
        }
        
        //排序
        Collections.sort(result, termComparator);
        
        return result;
    }
    
    /**
     * 功能描述：获取Vocabualry Term的总数
     * 2017年3月24日
     * @param vid
     * @return
     */
    public int getVocabularyTermsCount(String vid)
    {
        return dao.getVocabularyTermsCount(vid);
    }
    
    /**
     * 功能描述：获取Term的子Term（下一级）
     * 2017年3月24日
     * @param termid
     * @return
     */
    public List<Term> getSubterms(String termid)
    {
        Term term = getTermById(termid);
        if (term == null || StringUtils.isEmpty(term.getVid()) || StringUtils.isEmpty(term.getId()))
        {
            return null;
        }
        
        //在数据库获取数据之前，先查询缓存中是否存有数据
        List<Term> subterms = cache.getSubTermListCache(term.getVid(), term.getId());
        if (CollectionUtils.isEmpty(subterms))
        {
            subterms = dao.getSubterms(term.getId());
            if (subterms != null)
            {
                cache.putSubTermListCache(term.getVid(), term.getId(), subterms);
            }
        }
        return subterms;
    }
    
    /**
     * 功能描述：获取Term的父Term（上一级）
     * 2017年3月24日
     * @param termid
     * @return
     */
    public List<Term> getSupterms(String termid)
    {
        Term term = getTermById(termid);
        if (term == null || StringUtils.isEmpty(term.getVid()) || StringUtils.isEmpty(term.getId()))
        {
            return null;
        }
        
        //在数据库获取数据之前，先查询缓存中是否存有数据
        List<Term> supterms = cache.getSupTermListCache(term.getVid(), term.getId());
        if (CollectionUtils.isEmpty(supterms))
        {
            supterms = dao.getSupterms(term.getId());
            if (supterms != null)
            {
                cache.putSupTermListCache(term.getVid(), term.getId(), supterms);
            }
        }
        return supterms;
    }
    
    /**
     * 根据Id获取Term， needPut2Cache表示Term是否需要保存到Cassandra中去。
     * 针对更新Term的操作，会先查找Term，更新成功后然后从缓存中删除Term
     * 因此如果Term不再缓存中无需先保存到缓存中去，因为马上又会被删除。
     */
    public Term getTermById(String termid)
    {
        logger.info("Enter >> getTermById");
        
        if (StringUtils.isBlank(termid))
        {
            return null;
        }
        //先从缓存中取
        Term term = cache.getCacheObject(termid);
        if (term == null)
        {
            term = dao.getTermById(termid);
            if (term != null)
            {
                cache.putCacheObject(term);
            }
        }
        logger.info("Leave << getTermById");
        return term;
    }
    
    /**
     * 功能描述：删除Term
     * 2017年3月24日
     * @param uid
     * @param termid
     * @return
     * @throws ServiceException
     */
    @Transactional(rollbackFor = ServiceException.class)
    public boolean deleteTerm(String uid, String termid)
        throws ServiceException
    {
        if (StringUtils.isEmpty(termid))
        {
            return false;
        }
        //先从数据库中取
        Term term = dao.getTermById(termid);
        if (term == null)
        {
            return false;
        }
        
        //存在子Term， 不允许删除
        if (dao.getSubtermsCount(termid) > 0)
        {
            throw TermErrCode.SC018.exception();
        }
        
        //Term被Node使用， 不允许删除
        //        if (dao.isTermUsedByNode(term.getVid(), termid))
        //        {
        //            throw TermErrCode.SC019.exception();
        //        }
        
        //获取父节点，删除Hierarchy关系，并进行缓存同步
        List<Term> supterms = dao.getSupterms(termid);
        //要么必须有父亲，要么父亲是metadb
        if (supterms != null && !supterms.isEmpty())
        {
            for (Term supterm : supterms)
            {
                int deleteTermHierarchy = dao.deleteTermHierarchy(supterm.getId(), termid, uid);
                if (deleteTermHierarchy<0)
                {
                    logger.error("要删除的子节点不存在");
                    return false;
                }
                // 父节点删除subterms 关系
                cache.deleteSubTermListCache(supterm.getVid(), supterm.getId());
                
                TermHierarchy termHierarchy = new TermHierarchy();
                termHierarchy.setSubId(termid);
                termHierarchy.setSupId(supterm.getId());
                
                listenerService.sendTermChangeMsg(term.getVid(), MetaJMSMessage.generateMsg4DelHierarchy(termHierarchy));
            }
        }
        else
        {
            // 如果是顶级节点，删除Root的subterms缓存
            cache.deleteSubTermListCache(term.getVid(), ROOT_TERM_ID);
        }
        
        // 删除与节点 Root 关系 （DTS2016062109131 问题单修改）
        dao.deleteTermHierarchy(ROOT_TERM_ID, termid, uid);
        TermHierarchy termHierarchyRoot = new TermHierarchy();
        termHierarchyRoot.setSubId(termid);
        termHierarchyRoot.setSupId(ROOT_TERM_ID);
        
        listenerService.sendTermChangeMsg(term.getVid(), MetaJMSMessage.generateMsg4DelHierarchy(termHierarchyRoot));
        
        //删除Term
        int delRows = dao.deleteTerm(termid);
        listenerService.sendTermChangeMsg(term.getVid(), MetaJMSMessage.generateMsg4DeleteTerm(term));
        
        // 删除后，通知缓存更新
        cache.delCacheObject(termid);
        
        return delRows > 0 ? true : false;
    }
    
    /**
     * 功能描述：查询子Term，当supId为blank时，则查询顶级节点的总数
     * 2017年3月24日
     * @param vid
     * @param supTid
     * @param offset
     * @param limit
     * @return
     */
    public List<Term> getTerms(String vid, String supTid, int offset, int limit)
    {
        if (StringUtils.isBlank(vid))
        {
            return null;
        }
        
        String fixedSupId = supTid;
        if (StringUtils.isBlank(fixedSupId))
        {
            fixedSupId = ROOT_TERM_ID;
        }
        return dao.getTerms(vid, fixedSupId, offset, limit);
    }
    
    /**
     * 功能描述：create Term
     * 2017年3月24日
     * @param term
     * @return
     * @throws ServiceException
     */
    @Transactional(rollbackFor = ServiceException.class)
    public String createTerm(Term term)
        throws ServiceException
    {
        //验证参数
        if (term == null || StringUtils.isBlank(term.getVid()))
        {
            throw TermErrCode.SC010.exception();
        }
        
        Vocabulary voc = vocabularyBusiness.getVocabularyById(term.getVid());
        
        //验证vid是否有效
        if (voc == null)
        {
            throw TermErrCode.SC011.exception(term.getVid());
        }
        
        //验证vocabulary.hierarchy=0,不能创建子term
        if (voc.getHierarchy() == null || voc.getHierarchy().equals("0"))
        {
            String sup_id = StringUtils.isBlank(term.getSupId()) ? ROOT_TERM_ID : term.getSupId();
            if (!ROOT_TERM_ID.equalsIgnoreCase(sup_id))
            {
                //vocabulary的hierarchy为0，term不能分层，不能创建子term。
                throw TermErrCode.SC023.exception(term.getVid());
            }
        }
        
        String termId = term.getId();
        //如果id为blank，则生成Id
        if (StringUtils.isBlank(termId))
        {
            try
            {
                termId = idGenerateService.generateId(KEY_ID_TYPE_TERM);
            }
            catch (Exception e)
            {
                throw TermErrCode.SC012.exception(termId);
            }
            term.setId(termId);
        }
        else
        {
            //验证Id是否被占用
            if (dao.getTermById(termId) != null)
            {
                throw TermErrCode.SC013.exception(termId);
            }
        }
        
        //设置父Term Id
        String sup_id = StringUtils.isBlank(term.getSupId()) ? ROOT_TERM_ID : term.getSupId();
        
        Term parentTerm = null;
        if (!ROOT_TERM_ID.equalsIgnoreCase(sup_id))
        {
            parentTerm = dao.getTermById(sup_id);
            if (parentTerm == null)
            {
                throw TermErrCode.SC013.exception(sup_id);
            }
        }
        
        //创建term
        dao.createTerm(term);
        //cache.putCacheObject(term);
        
        //设置Term的Hierarachy，并记录创建Hierachy缓存同步项
        TermHierarchy termHierarchy = new TermHierarchy();
        termHierarchy.setSupId(sup_id);
        termHierarchy.setSubId(term.getId());
        termHierarchy.setWeight(term.getWeight());
        termHierarchy.setLastUpdateBy(term.getLastUpdateBy());
        this.createTermHierarchy(termHierarchy);
        
        if (parentTerm != null)
        {
            // 新增了节点，在缓存中删除父节点subterms 关系
            cache.deleteSubTermListCache(parentTerm.getVid(), parentTerm.getId());
        }
        
        //Vocabulary Listener监听
        listenerService.sendTermChangeMsg(term.getVid(), MetaJMSMessage.generateMsg4AddTerm(term));
        listenerService.sendTermChangeMsg(term.getVid(), MetaJMSMessage.generateMsg4AddHierarchy(termHierarchy));
        
        return termId;
    }
    
    /**
     * 功能描述：更新Term
     * 2017年3月24日
     * @param term
     * @return
     * @throws ServiceException
     */
    @Transactional(rollbackFor = ServiceException.class)
    public int updateTerm(Term term)
        throws ServiceException
    {
        //验证参数
        if (term == null)
        {
            throw TermErrCode.SC010.exception();
        }
        //验证vid是否有效
        if (vocabularyBusiness.getVocabularyById(term.getVid()) == null)
        {
            throw TermErrCode.SC011.exception(term.getVid());
        }
        
        Term oldTerm = getTermById(term.getId());
        if (oldTerm == null)
        {
            throw TermErrCode.SC014.exception(term.getId());
        }
        
        if (!StringUtils.isBlank(term.getLastUpdateTime()) && !StringUtils.isBlank(oldTerm.getLastUpdateTime())
            && oldTerm.getLastUpdateTime().compareTo(term.getLastUpdateTime()) > 0)
        {
            //Term已经被修改了。
            throw TermErrCode.SC015.exception(term.getId(), term.getLastUpdateTime(), oldTerm.getLastUpdateTime());
        }
        //设置父Term Id
        String sup_id = StringUtils.isBlank(term.getSupId()) ? ROOT_TERM_ID : term.getSupId();
        
        Term parentTerm = null;
        if (!ROOT_TERM_ID.equalsIgnoreCase(sup_id))
        {
            parentTerm = dao.getTermById(sup_id);
            if (parentTerm == null)
            {
                throw TermErrCode.SC016.exception(sup_id);
            }
        }
        
        Term oldTermHierarchy = dao.getTermByIdAndSupid(term.getId(), sup_id);
        if (oldTermHierarchy == null)
        {
            throw TermErrCode.SC017.exception(term.getId(), sup_id);
        }
        
        //更新Term
        int updatedRow = dao.updateTerm(term);
        cache.delCacheObject(term.getId());
        
        logger.info("update Term[tid=%s] effect rows = %d", term.getId(), updatedRow);
        
        //更新TermHierarchy，并记录缓存同步项
        TermHierarchy termHierarchy = new TermHierarchy();
        termHierarchy.setSupId(sup_id);
        termHierarchy.setSubId(term.getId());
        termHierarchy.setWeight(term.getWeight());
        termHierarchy.setLastUpdateBy(term.getLastUpdateBy());
        dao.updateTermHierarchy(termHierarchy);
        
        //删除Term在Redis中的缓存
        List<Term> subTerms = getSubterms(term.getId());
        if (!CollectionUtils.isEmpty(subTerms))
        {
            cache.deleteSupTermListCache(subTerms);
        }
        
        if (parentTerm != null)
        {
            // weight 发生了变化，在缓存中删除父节点subterms 关系
            cache.deleteSubTermListCache(parentTerm.getVid(), parentTerm.getId());
        }
        else
        {
            // 如果是顶级节点，删除Root的subterms缓存
            cache.deleteSubTermListCache(term.getVid(), ROOT_TERM_ID);
        }
        
        //Vocabulary Listener监听
        listenerService.sendTermChangeMsg(term.getVid(), MetaJMSMessage.generateMsg4UpdateTerm(term, oldTerm));
        listenerService.sendTermChangeMsg(term.getVid(), MetaJMSMessage.generateMsg4UpdateHierarchy(termHierarchy));
        
        return updatedRow;
    }
    
    /**
     * 功能描述：set Term Attributes
     * 2017年3月24日
     * @param tid
     * @param attrs
     * @param uid
     * @return
     */
    public int setTermAttributes(String tid, Map<String, String> attrs, String uid)
    {
        Term term = this.getTermById(tid);
        if (term == null)
        {
            return 0;
        }
        return setTermAttrsWithVid(term.getVid(), tid, attrs, uid);
    }
    
    /**
     * 功能描述：add Term Attributes
     * 2017年3月24日
     * @param tid
     * @param attrs
     * @param uid
     * @return
     * @throws ServiceException
     */
    public int addTermAttributes(String tid, Map<String, String> attrs, String uid)
        throws ServiceException
    {
        if (StringUtils.isBlank(uid))
        {
            throw TermErrCode.SC021.exception();
        }
        
        if (attrs != null && !attrs.isEmpty())
        {
            Term term = this.getTermById(tid);
            if (term == null)
            {
                throw TermErrCode.SC022.exception(tid);
            }
            
            //重设Term属性
            Map<String, String> newAttrs = getTermAttributes(tid);
            newAttrs = newAttrs == null ? new HashMap<String, String>() : newAttrs;
            newAttrs.putAll(attrs);
            
            //保存新属性
            return setTermAttrsWithVid(term.getVid(), tid, newAttrs, uid);
        }
        return 0;
    }
    
    /**
     * 功能描述：delete Term Attributes
     * 2017年3月24日
     * @param tid
     * @param keys
     * @param uid
     * @return
     * @throws ServiceException
     */
    public int deleteTermAttributes(String tid, List<String> keys, String uid)
        throws ServiceException
    {
        if (StringUtils.isBlank(uid))
        {
            throw TermErrCode.SC021.exception();
        }
        
        if (!StringUtils.isBlank(tid) && keys != null && !keys.isEmpty())
        {
            Term term = getTermById(tid);
            if (term == null)
            {
                throw TermErrCode.SC022.exception(tid);
            }
            
            //如果Term没有属性，则直接返回
            Map<String, String> attrs = getTermAttributes(tid);
            if (attrs == null || attrs.isEmpty())
            {
                return 0;
            }
            
            //删除属性
            for (String key : keys)
            {
                attrs.remove(key);
            }
            
            //保存新属性
            return setTermAttrsWithVid(term.getVid(), tid, attrs, uid);
        }
        return 0;
    }
    
    /**
     * 功能描述：get Term Attributes
     * 2017年3月24日
     * @param tid
     * @return
     */
    public Map<String, String> getTermAttributes(String tid)
    {
        Map<String, String> map = new HashMap<String, String>();
        Term term = this.getTermById(tid);
        
        if (null == term)
        {
            return map;
        }
        
        return term.getAttributes();
    }
    
    /**
     * 功能描述：set Term Attributes
     * 2017年3月24日
     * @param vid
     * @param tid
     * @param attrs
     * @param uid
     * @return
     */
    private int setTermAttrsWithVid(String vid, String tid, Map<String, String> attrs, String uid)
    {
        
        logger.info(">>setTermAttrsWithVid");
        //设置Term的属性防
        int ret = dao.setTermAttributes(tid, attrs, uid);
        cache.delCacheObject(tid); // 从缓存删除term，在下次get 时重新写数据库
        
        List<Term> supTerms = dao.getSupterms(tid);
        cache.deleteSubTermListCache(supTerms);// 父亲节点，批量删除父节点的 subterms 缓存
        
        List<Term> subTerms = dao.getSubterms(tid);
        cache.deleteSupTermListCache(subTerms);// 子亲节点，批量删除子节点的 supterms 缓存
        
        //Vocabulary Listener监听
        Term term = new Term();
        term.setAttributes(attrs);
        term.setId(tid);
        listenerService.sendTermChangeMsg(vid, MetaJMSMessage.generateMsg4SetAttrs(term));
        
        logger.info("<<setTermAttrsWithVid");
        return ret;
    }
    
    /**
     * 解析全路径时的termId,获取最后一个‘/’后的termid
     * <功能详细描述>
     * @param tid
     * @return
     * @see [类、类#方法、类#成员]
     */
    private String parseTermIdValue(String tid)
    {
        if (null != tid && !"".equals(tid))
        {
            int n = tid.lastIndexOf("/");
            if (n > -1)
            {
                tid = tid.substring(n + 1, tid.length());
            }
        }
        return tid;
    }
    
    /**
     * 根据多个Term Id获取多个Term的详细信息
     */
    public List<Term> getTermsById(List<String> termIds)
    {
        if (CollectionUtils.isEmpty(termIds))
        {
            return null;
        }

        com.org.support.cbb.util.collections.CollectionUtils.removeDuplicate(termIds);

        List<Term> terms = new ArrayList<Term>();
        for (String tid : termIds)
        {
            //先从缓存找
            Term cterm = getTermById(parseTermIdValue(tid));
            if (cterm != null)
            {
                cterm.setId(tid);
                terms.add(cterm);
            }
        }
        
        return terms;
    }
    
    /**
     * 功能描述：返回最顶层的Term
     * 2017年3月24日
     * @param vid
     * @return
     */
    public List<Term> getTopTermsByVid(String vid)
    {
        if (StringUtils.isBlank(vid))
        {
            return null;
        }
        
        //在数据库获取数据之前，先查询缓存中是否存有数据
        List<Term> terms = cache.getSubTermListCache(vid, ROOT_TERM_ID);
        if (CollectionUtils.isEmpty(terms))
        {
            terms = dao.getTopTermsByVid(vid);
            
            if (!CollectionUtils.isEmpty(terms))
            {
                //                for (Term term : terms)
                //                {
                //                    cache.putCacheObject(term);
                //                }
                cache.putSubTermListCache(vid, ROOT_TERM_ID, terms);
            }
        }
        return terms;
    }
    
    /**
     * create Term Hierarchy
     * <功能详细描述>
     * @param toInsertValues
     * @return
     * @see [类、类#方法、类#成员]
     */
    public int createTermHierarchy(TermHierarchy termHierarchy)
        throws ServiceException
    {
        //验证参数
        if (termHierarchy == null || StringUtils.isEmpty(termHierarchy.getLastUpdateBy()))
        {
            throw TermErrCode.SC006.exception("Parameter: termHierarchy and termHierarchy.lastUpdateBy cannot be null.");
        }
        //父节点为空则设置为根节点
        if (StringUtils.isBlank(termHierarchy.getSupId()))
        {
            termHierarchy.setSupId(ROOT_TERM_ID);
        }
        
        //验证父子Term是否存在并返回子Term
        Term term = validateHierarchyAndGetSubTerm(termHierarchy.getSupId(), termHierarchy.getSubId(), false);
        
        //新建并同步缓存
        int retVal = dao.createTermHierarchy(termHierarchy);
        
        // 在缓存中删除sub terms 关系
        cache.deleteSubTermListCache(term.getVid(), termHierarchy.getSupId());
        
        // 在缓存中删除sup terms 关系
        cache.deleteSupTermListCache(term.getVid(), termHierarchy.getSubId());
        
        //Vocabulary Listener监听
        listenerService.sendTermChangeMsg(term.getVid(), MetaJMSMessage.generateMsg4AddHierarchy(termHierarchy));
        
        return retVal;
    }
    
    /**
     * <一句话功能简述>
     * <功能详细描述>
     * @param sup_Id
     * @param sub_id
     * @return
     * @throws DSDPException
     * @see [类、类#方法、类#成员]
     */
    private Term validateHierarchyAndGetSubTerm(String sup_Id, String sub_id, boolean existedFlag)
        throws ServiceException
    {
        if (!ROOT_TERM_ID.equals(sup_Id))
        {
            //验证父Term是否存在
            Term supTerm = dao.getTermById(sup_Id);
            if (supTerm == null)
            {
                throw TermErrCode.SC001.exception("HIERARCHY_TERM_NOT_EXIST, Parent term is not existed.");
            }
        }
        
        Term term = dao.getTermById(sub_id);
        //子Term不存在
        if (term == null)
        {
            throw TermErrCode.SC002.exception("HIERARCHY_TERM_NOT_EXIST, Parent term is not existed.");
            ///throw new DSDPException(TermErrorCode.HIERARCHY_TERM_NOT_EXIST, "Sub-term is not existed.");
        }
        
        Term hierarchyterm = dao.getTermByIdAndSupid(sub_id, sup_Id);
        //要求验证层级关系存在
        if (existedFlag)
        {
            if (hierarchyterm == null)
            {
                throw TermErrCode.SC003.exception("NOT_EXIST_HIERARCHY, Hierarchy dose not exist.");
            }
        }
        //验证层级关系不存在
        else
        {
            if (hierarchyterm != null)
            {
                throw TermErrCode.SC004.exception("EXIST_HIERARCHY, Hierarchy exists.");
            }
        }
        
        return term;
    }
    
    /**
     * 更新Term的Hierarchy关系,只可更新weight，不能修改父子关系，
     * 可以先通过删除Hierarchy再新建Hierarchy的方法修改Hierarchy关系
     */
    public int updateTermHierarchy(TermHierarchy termHierarchy)
        throws ServiceException
    {
        // 更新前，先检查关系是否存在
        if (dao.getTermByIdAndSupid(termHierarchy.getSubId(), termHierarchy.getSupId()) == null)
        {
            throw TermErrCode.SC024.exception(termHierarchy.getSubId(), termHierarchy.getSupId());
        }
        
        //验证子Term并获取子Term
        Term term = validateHierarchyAndGetSubTerm(termHierarchy.getSupId(), termHierarchy.getSubId(), true);
        
        int ret = dao.updateTermHierarchy(termHierarchy);
        
        // 在缓存中删除sub terms 关系
        cache.deleteSubTermListCache(term.getVid(), term.getId());
        
        // 在缓存中删除sup terms 关系
        cache.deleteSupTermListCache(term.getVid(), term.getId());
        
        listenerService.sendTermChangeMsg(term.getVid(), MetaJMSMessage.generateMsg4UpdateHierarchy(termHierarchy));
        
        return ret;
    }
    
    /**
     * 功能描述：删除Term的Hierarchy关系
     * 2017年3月24日
     * @param supid
     * @param subid
     * @param deleteBy
     * @return
     * @throws ServiceException
     */
    public int deleteTermHierarchy(String supid, String subid, String deleteBy)
        throws ServiceException
    {
        //验证参数
        if (StringUtils.isBlank(supid) || StringUtils.isBlank(deleteBy))
        {
            throw TermErrCode.SC005.exception("Parameter: supid and deleteBy cannot be null.");
        }
        
        if (StringUtils.isBlank(supid))
        {
            supid = ROOT_TERM_ID;
        }
        
        //验证父子Term是否存在并返回子Term
        Term term = validateHierarchyAndGetSubTerm(supid, subid, true);
        
        int delRow = dao.deleteTermHierarchy(supid, subid, deleteBy);
        
        // 在缓存中删除sub terms 关系
        cache.deleteSubTermListCache(term.getVid(), supid);
        
        // 在缓存中删除sup terms 关系
        cache.deleteSupTermListCache(term.getVid(), term.getId());
        
        //Vocabulary Listener监听
        TermHierarchy hierarchy = new TermHierarchy();
        hierarchy.setSubId(supid);
        hierarchy.setSupId(supid);
        listenerService.sendTermChangeMsg(term.getVid(), MetaJMSMessage.generateMsg4DelHierarchy(hierarchy));
        
        return delRow;
    }
    
    /**
     * 功能描述：getTermByIdAndSupid
     * 2017年3月24日
     * @param termId
     * @param supid
     * @return
     */
    public Term getTermByIdAndSupid(String termId, String supid)
    {
        if (StringUtils.isBlank(termId))
        {
            return null;
        }
        //设置父Term
        String parentTermId = supid;
        if (StringUtils.isBlank(parentTermId))
        {
            parentTermId = ROOT_TERM_ID;
        }
        //从缓存中去，如果从缓存中取到的Term不包含Weight和sup_tid，则重新从数据库取
        Term term = cache.getCacheObject(termId);
        
        if (term == null || term.getWeight() < 0 || !parentTermId.equals(term.getSupId()))
        {
            term = dao.getTermByIdAndSupid(termId, parentTermId);
            if (term != null)
            {
                cache.putCacheObject(term);
            }
        }
        return term;
    }
    
    /**
     * <一句话功能简述>
     * <功能详细描述>
     * @param termid
     * @return [参数说明]
     *
     * @return List<Term> [返回类型说明]
     * @exception throws [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    public List<Term> getSubtermsHierarchyByTid(String termid)
    {
        String parentTermId = termid;
        if (StringUtils.isBlank(parentTermId))
        {
            //parentTermId = ROOT_TERM_ID;
            //不建议全部查询出来，内存爆了
            return null;            
        }
        
        List<Term> lst = dao.getSubtermsHierarchyByTid(parentTermId);
        return lst;
    }
    
    /**
     * getNewTermID
     * <功能详细描述>
     * @return
     * @throws ServiceException [参数说明]
     *
     * @return String [返回类型说明]
     * @exception throws [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    public String getNewTermID()
        throws ServiceException
    {
        try
        {
            String termId = idGenerateService.generateId(KEY_ID_TYPE_TERM);
            return termId;
        }
        catch (Exception e)
        {
            return null;
        }
    }
    
    /**
     * 查询子Term的总数，当supId为blank时，则查询顶级节点的总数 <功能详细描述>
     * <功能详细描述>
     * @param vid
     * @param suptid
     * @return [参数说明]
     *
     * @return Integer [返回类型说明]
     * @exception throws [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    public Integer getTermsCount(String vid, String suptid)
    {
        if (StringUtils.isBlank(vid))
        {
            return 0;
        }
        String fixedSupId = suptid;
        if (StringUtils.isBlank(fixedSupId))
        {
            fixedSupId = ROOT_TERM_ID;
        }
        return dao.getTermsCount(vid, fixedSupId);
    }
    
    /**
     * moveTerm
     * <功能详细描述>
     * @param oldSupTid
     * @param targetSupTid
     * @param sysUserId
     * @param tids
     * @return
     * @throws ServiceException [参数说明]
     *
     * @return Integer [返回类型说明]
     * @exception throws [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    @Transactional(rollbackFor = ServiceException.class)
    public Integer moveTerm(String oldSupTid, String targetSupTid, String sysUserId, List<String> tids)
        throws ServiceException
    {
        if (CollectionUtils.isEmpty(tids) || StringUtils.isBlank(targetSupTid) || StringUtils.isBlank(sysUserId))
        {
            logger.error("moveTerm(), param null.");
            return 0;
        }
        com.org.support.cbb.util.collections.CollectionUtils.removeDuplicate(tids);
        
        for (String tid : tids)
        {
            TermHierarchy termHierarchy = new TermHierarchy();
            termHierarchy.setSupId(targetSupTid);
            termHierarchy.setSubId(tid);
            termHierarchy.setWeight(0);
            //termHierarchy.setLastUpdateTime(DateUtil.format(new Date(), DateUtil.DEFAULT_DATE_TIME_FORMAT));
            termHierarchy.setLastUpdateBy(sysUserId);
            
            deleteTermHierarchy(oldSupTid, tid, sysUserId);
            createTermHierarchy(termHierarchy);
        }
        
        return tids.size();
    }
    
    /**
     * 获取所有的父节点，包括父节点的父节点
     * <功能详细描述>
     * @param tm 
     * @param vid
     * @param termId
     * @return
     * @see [类、类#方法、类#成员]
     */
    private Term getSuptermsHierarchy(Term subTerm, Set<String> existIds)
    {
        if (subTerm == null || StringUtils.isEmpty(subTerm.getVid()) || StringUtils.isEmpty(subTerm.getId()))
        {
            return null;
        }
        
        // id 已处理过，就不要重复递归（避免递归死循环导致堆栈溢出）
        if (existIds.contains(subTerm.getId()))
        {
            logger.error("getSuptermsHierarchy->id={} 递归出现了环状,数据不合理，强制终止递归。",subTerm.getId());
            return null;
        }
        existIds.add(subTerm.getId());
        
        //在数据库获取数据之前，先查询缓存中是否存有数据
        List<Term> supterms = cache.getSupTermListCache(subTerm.getVid(), subTerm.getId());
        if (CollectionUtils.isEmpty(supterms))
        {
            supterms = dao.getSupterms(subTerm.getId());
            
            if (supterms != null)
            {
                cache.putSupTermListCache(subTerm.getVid(), subTerm.getId(), supterms);
            }
        }
        
        subTerm.setSupterms(supterms);
        
        if (subTerm != null && CollectionUtils.isNotEmpty(subTerm.getSupterms()))
        {
            for (Term sup : subTerm.getSupterms())
            {
                getSuptermsHierarchy(sup,existIds);
            }
        }
        
        return subTerm;
    }
    
    /**
     * 获取所有的父节点，包括父节点的父节点。
     * <功能详细描述>
     * @param vid
     * @param termId
     * @return
     * @see [类、类#方法、类#成员]
     */
    public Term getSuptermsHierarchy(String termId)
    {
        
        logger.info("Enter >> getSuptermsHierarchy");
        Term term = getTermById(parseTermIdValue(termId));
        if (term == null)
        {
            return null;
        }
        
        Set<String> existIds  = new HashSet<String>();
        getSuptermsHierarchy(term,existIds);
        
        logger.info("leave << getSuptermsHierarchy");
        return term;
    }
    
    public List<Term> getSuptermsHierarchys(List<String> tids)
    {
        List<Term> list = new ArrayList<Term>();
        if (CollectionUtils.isNotEmpty(tids))
        {
            com.org.support.cbb.util.collections.CollectionUtils.removeDuplicate(tids);
            
            for (int i = 0; i < tids.size(); i++)
            {
                //获取Term所有的父Term(包括父Term的父Term)
                Term term = getSuptermsHierarchy(tids.get(i));
                list.add(term);
            }
        }
        return list;
    }
    
    /**
     * 获取所有的子节点，包括子节点的子节点
     * <功能详细描述>
     * @param vid
     * @param termId
     * @return
     * @see [类、类#方法、类#成员]
     */
    private Term getSubtermsHierarchy(Term supTerm ,Set<String> existIds)
    {
        if (supTerm == null || StringUtils.isEmpty(supTerm.getVid()) || StringUtils.isEmpty(supTerm.getId()))
        {
            return null;
        }
        
        // id 已处理过，就不要重复递归（避免递归死循环导致堆栈溢出）
        if (existIds.contains(supTerm.getId()))
        {
            logger.error("getSubtermsHierarchy->id={} 递归出现了环状,数据不合理，强制终止递归。",supTerm.getId());
            return null;
        }
        existIds.add(supTerm.getId());
        
        //在数据库获取数据之前，先查询缓存中是否存有数据
        List<Term> subterms = cache.getSubTermListCache(supTerm.getVid(), supTerm.getId());
        if (CollectionUtils.isEmpty(subterms))
        {
            subterms = dao.getSubterms(supTerm.getId());
            
            if (subterms != null)
            {
                cache.putSubTermListCache(supTerm.getVid(), supTerm.getId(), subterms);
            }
        }
        
        supTerm.setSubterms(subterms);
        if (supTerm != null && CollectionUtils.isNotEmpty(supTerm.getSubterms()))
        {
            for (Term sub : supTerm.getSubterms())
            {
                getSubtermsHierarchy(sub,existIds);
            }
        }
        return supTerm;
    }
    
    /**
     * 功能描述：获取所有子节点
     * 2017年3月24日
     * @param termId
     * @return
     */
    public Term getSubtermsHierarchy(String termId)
    {
        logger.info("Enter >> getSubtermsHierarchy");
        Term term = getTermById(parseTermIdValue(termId));
        if (term == null)
        {
            return null;
        }
        Set<String> existIds  = new HashSet<String>();
        getSubtermsHierarchy(term,existIds);
        
        logger.info("leave << getSubtermsHierarchy");
        return term;
    }
    
    /**
     * 添加元数据日记记录
     * @param termModification
     * @return
     * @throws ServiceException
     */
    public String addTermMdLOG(TermModification termModification)
        throws ServiceException
    {
    	//数据为空返回
        if (termModification == null || StringUtils.isBlank(termModification.getTid()))
        {
            String str = "termModification is null termId is null";
            logger.error(str);
            throw TermErrCode.SC025.exception();
        }
        //编号为空，调用id生成器生产编号
        if (StringUtils.isBlank(termModification.getId()))
        {
            try
            {
                termModification.setId(idGenerateService.generateId(KEY_ID_TERM_MODIFICATION));
            }
            catch (Exception e)
            {
                String str = "get KEY_ID_TERM_MODIFICATION error";
                logger.error(str);
                throw TermErrCode.SC025.exception();
            }
        }
        dao.addTermMdLOG(termModification);
        return termModification.getId();
    }

    /**
     *  强制删除redis 缓存
     * @return
     */
    public String cleanRedis()
    {
        // 强制删除redis 缓存（数据库sql刷新数据后,执行清除缓存）
        String resultMsg = cleanTermRedisService.cleanRedis();
        logger.error(resultMsg);
        return resultMsg;
    }
    
}
