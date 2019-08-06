package com.edu.bupt.camera.dao;

import com.edu.bupt.camera.model.CameraUserRelation;

import java.util.List;

public interface CameraUserRelationMapper {
    int deleteByPrimaryKey(Integer id);

    int deleteByCustomerIdAndCameraId(String cameraId);

    int insert(CameraUserRelation record);

    int insertSelective(CameraUserRelation record);

    CameraUserRelation selectByCameraId(String cameraId);

    CameraUserRelation selectByPrimaryKey(Integer id);

    List<String> selectCameraIdByCustomerId(Integer id);

    int updateByPrimaryKeySelective(CameraUserRelation record);

    int updateByPrimaryKey(CameraUserRelation record);
}