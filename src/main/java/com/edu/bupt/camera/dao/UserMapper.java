package com.edu.bupt.new_account.dao;

import com.edu.bupt.new_account.model.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    User selectByOpenid(String openid);

    void updateByUser(User user);

    List<User> searchAllUser();

    User selectByPhone(String phone);

    User selectByemail(String email);

    void deleteById(Integer id);
}