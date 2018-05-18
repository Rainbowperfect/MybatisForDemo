package test.org.person.mybatis;

import org.apache.ibatis.session.SqlSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.person.mybatis.model.User;
import org.person.mybatis.utils.MybatisUtils;

import java.util.List;

/**
 * @author RainbowPerferct/zero
 * @version v1.0
 * @create 2018/5/7/14:48
 */

public class TestMybatis02 {

    private SqlSession sqlSession;
    @Before
    public void init(){
        sqlSession= MybatisUtils.getSqlSession();
    }

    @Test
    public void addUser() {
        User user=new User ();
        user.setUserage(28);
        user.setUsername("诸葛亮");

    sqlSession.insert("org.person.mybatis.model.User.addUser",user);
    }

    /**
     * 不传参数,list集合是有序，可以重复的
     */
    @Test
    public void testSelect(){
        List<User> list= sqlSession.selectList("org.person.mybatis.model.User.getAllUser");
        for (User user : list) {
            System.out.println(user);
        }
    }

    /**
     * 入参查询
     */
    @Test
    public void getUser() {
        User user = sqlSession.selectOne("org.person.mybatis.model.User.getUser", 13);
        System.out.println(user);
    }

    /**
     * 增加数据，获取主键
     */
    @Test
    public void add () {
        User user =new User();
        user.setUsername("赵云");
        user.setUserage(18);
        int i = sqlSession.insert("org.person.mybatis.model.User.add", user);
        System.out.println("i的增加的数量"+i+" "+ user);
    }

    /**
     * 获取自增主键--useGeneratedKeys
     */
    @Test
    public void addAtuoKey(){
        User user =new User();
        user.setUserage(25);
        user.setUsername("kebi");
        sqlSession.insert("org.person.mybatis.model.User.add",user);
        System.out.println(user);
    }

    /**
     * 获取UUID
     */
    @Test
    public void addUUId(){
        User user =new User();
        user.setUsername("ton");
        user.setUserage(12);
        sqlSession.insert("org.person.mybatis.model.User.addUUId",user);
        System.out.println(user);
    }
    @Test
    public void modify(){
        User user=new User();
        user.setUsername("黑客");
        user.setUserage(56);
        user.setUserid("587610f6-520b-11e8-89d5-b06ebfaba02a");
        sqlSession.update("org.person.mybatis.model.User.modify",user);
    }
    @Test
    public void deleteUser(){
        User user=new User();
        user.setUsername("黑客");
        user.setUserage(56);
        user.setUserid("587610f6-520b-11e8-89d5-b06ebfaba02a");
        sqlSession.delete("org.person.mybatis.model.User.deleteUser",user);

    }

    @After
    public void  destory(){
        sqlSession.commit();
        sqlSession.close();
    }
}

