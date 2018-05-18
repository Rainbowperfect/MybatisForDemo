package test.org.person.mybatis;

import org.junit.Before;
import org.junit.Test;
import org.person.mybatis.dao.UserDao;
import org.person.mybatis.model.User;

/**
 * @author RainbowPerferct/zero
 * @version v1.0
 * @create 2018/5/8/12:16
 */

public class ProxyTest {
    private UserDao userDao;
    @Before
    public void init(){

    }

    @Test
    public  void  TestAdduser() {
        User user =new User();
        user.setUserage(38);
        user.setUsername("死基佬");

       int count= userDao.addUser(user);
        System.out.println(count);
    }
}
