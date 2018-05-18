package test.org.person.mybatis;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Test;
import org.person.mybatis.model.User;

import java.io.IOException;
import java.io.Reader;

/**
 * @author RainbowPerferct/zero
 * @version v1.0
 * @create 2018/5/7/14:48
 */

public class TestMybatis {

    @Test
    public void addUser() {
        User user=new User ();
        user.setUserage(26);
        user.setUsername("小明");

        //创建回话sqlSession的对象
        try {
            Reader reader = Resources.getResourceAsReader("mybaits.xml");
            SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
            SqlSession session = sqlSessionFactory.openSession();

            session.insert("org.person.mybatis.model.User.addUser", user);

            session.commit();
            session.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

