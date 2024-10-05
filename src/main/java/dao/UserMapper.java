package dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import util.User;

public interface UserMapper {
    @Select("SELECT * FROM users WHERE account = #{account} AND password = #{password}")
    User authenticate(String account, String password);

    @Insert("INSERT INTO users (account, password) VALUES (#{account}, #{password})")
    void insertUser(User user);
}
