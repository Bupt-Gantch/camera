package com.edu.bupt.camera.dao;

import com.edu.bupt.camera.model.Camera;

public interface CameraMapper {
    int deleteByPrimaryKey(String id);

    int insert(Camera record);

    int insertSelective(Camera record);

    Camera selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(Camera record);

    int updateByPrimaryKeyWithBLOBs(Camera record);

    int updateByPrimaryKey(Camera record);
}