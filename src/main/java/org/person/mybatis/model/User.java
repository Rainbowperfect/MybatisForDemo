package org.person.mybatis.model;

import java.io.Serializable;

/**
 * @author RainbowPerferct/zero
 * @version v1.0
 * @create 2018/5/7/14:42
 */

public class User implements Serializable {
    private String userid;

    private String username;

    private Integer userage;

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getUserage() {
        return userage;
    }

    public void setUserage(Integer userage) {
        this.userage = userage;
    }

    @Override
    public String toString() {
        return "User{" + "userid='" + userid + '\'' + ", username='" + username + '\'' + ", userage=" + userage + '}';
    }
}
