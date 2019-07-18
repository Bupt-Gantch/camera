package com.edu.bupt.camera.dao;

import com.edu.bupt.camera.model.CameraUser;

public interface CameraUserMapper {
    int deleteByPrimaryKey(Integer customerId);

    int insert(CameraUser record);

    int insertSelective(CameraUser record);

    CameraUser selectByPrimaryKey(Integer customerId);

    int updateByPrimaryKeySelective(CameraUser record);

    int updateByPrimaryKey(CameraUser record);
}