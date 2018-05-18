package org.person.mybatis.utils;


import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.Reader;


/**
 * @author RainbowPerferct/zero
 * @version v1.0
 * @create 2018/5/7/17:34
 */

public class MybatisUtils {
    private static  SqlSessionFactory sqlSessionFactory;
    static {
        try {
            //读取配置文件，获取session对象
            Reader reader = Resources.getResourceAsReader("mybaits.xml");
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /*
     *获取sqlSessionFactory对象
     */

    public static SqlSessionFactory getSqlSessionFactory(){
        return sqlSessionFactory;
    }

    /*
    *获取session回话
    */

    public static SqlSession getSqlSession(){
       return sqlSessionFactory.openSession();
    }
}
