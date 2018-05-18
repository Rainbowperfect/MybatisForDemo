package test.org.person.mybatis;

import org.apache.ibatis.session.SqlSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.person.mybatis.dao.UserDao;
import org.person.mybatis.model.User;
import org.person.mybatis.utils.MybatisUtils;

/**
 * @author RainbowPerferct/zero
 * @version v1.0
 * @create 2018/5/8/12:16
 */

public class ProxyTest02 {
    private SqlSession sqlSession;
    private UserDao userDao;
    @Before
    public void init(){
    sqlSession= MybatisUtils.getSqlSession();
    //获得接口的代理实现类
     userDao = sqlSession.getMapper(UserDao.class);
    }


    @Test
    public  void  TestModifyuser() {
        User user =new User();
        user.setUsername("烦躁03");
        user.setUserage(23);
        user.setUserid("26");

        userDao.modify(user);
        System.out.println(user);
    }

    @After
    public void destory() {
        sqlSession.commit();
        sqlSession.close();
    }
}
