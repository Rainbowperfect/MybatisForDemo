package org.person.mybatis.model;

/**
 * @author RainbowPerferct/zero
 * @version v1.0
 * @create 2018/5/8/17:07
 */

public class IdCard {
    private String cartId;

    private String city;

    private String province;

    private User user;

    public String getCartId() {
        return cartId;
    }

    public void setCartId(String cartId) {
        this.cartId = cartId;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }



    @Override
    public String toString() {
        return "IdCard{" + "cartId='" + cartId + '\'' + ", city='" + city + '\'' + ", province='" + province + '\'' + ", user=" + user + '}';
    }
}
