package org.person.mybatis.dao;

import org.person.mybatis.model.IdCard;
import org.person.mybatis.model.User;

import java.util.HashMap;
import java.util.List;

/**
 * @author RainbowPerferct/zero
 * @version v1.0
 * @create 2018/5/8/12:16
 */

public interface UserDao {

    int addUser(User user);

    void modify(User user);

    User getUserByUserCarId(IdCard idCard);

    User getUserMap(HashMap<String, Object> dataMap);

    int getUserCount(HashMap<String, Object> dataMap);

    List<User> getUserByMap(HashMap<String, Object> dataMap);
}
