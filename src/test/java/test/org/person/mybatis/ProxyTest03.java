package test.org.person.mybatis;

import org.apache.ibatis.session.SqlSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.person.mybatis.dao.UserDao;
import org.person.mybatis.model.IdCard;
import org.person.mybatis.model.User;
import org.person.mybatis.utils.MybatisUtils;

import java.util.HashMap;
import java.util.List;


/**
 * @author RainbowPerferct/zero
 * @version v1.0
 * @create 2018/5/8/12:16
 */

public class ProxyTest03 {
    private SqlSession sqlSession;
    private UserDao userDao;
    @Before
    public void init(){
        sqlSession= MybatisUtils.getSqlSession();
        //获得接口的代理实现类
        userDao = sqlSession.getMapper(UserDao.class);
    }



    @Test
    public  void  TestByUserId() {
        IdCard idCard=new IdCard();
        idCard.setCartId("2564665");

       User user=userDao.getUserByUserCarId(idCard);
        System.out.println(user);

    }

    @Test
    public  void  TestByUserMap() {
        HashMap<String, Object> dataMap = new HashMap<>();
        dataMap.put("uid","13");

        User user=userDao.getUserMap(dataMap);
        System.out.println(user);

    }
    @Test
    public  void  TestByUserMapCount() {
        HashMap<String, Object> dataMap = new HashMap<>();
        dataMap.put("uid","13");

       int users=userDao.getUserCount(dataMap);
        System.out.println(users);
    }
    @Test
    public void TestGetUserByMap(){
        HashMap<String, Object> dataMap = new HashMap<>();
        dataMap.put("uid",13);

        dataMap.put("start",18);
        dataMap.put("end",100);
       List<User> users= userDao.getUserByMap(dataMap);
        for (User user : users) {
            System.out.println(user);
        }
    }

    @After
    public void destory() {
        sqlSession.commit();
        sqlSession.close();
    }
}
