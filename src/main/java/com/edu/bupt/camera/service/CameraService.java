package com.edu.bupt.camera.service;

import com.alibaba.fastjson.JSONObject;
import com.edu.bupt.camera.model.Camera;
import com.edu.bupt.camera.model.CameraUser;

public interface CameraService {

    boolean validAccessToken(CameraUser user);
    JSONObject getAccessToken(Integer customerId);
    JSONObject sendForaccessToken(Integer customerId);
    JSONObject sendForaccessToken(Integer customerId,String appKey,String appSecret);
    JSONObject register(JSONObject userJson);
    JSONObject getLiveAddrBydeviceSerial(Integer customer_id,String deviceSerial,String cam);
    JSONObject openLiveBydeviceSerial(Integer customer_id,String deviceSerial,String Cam);
    JSONObject closeLiveBydeviceSerial(Integer customer_id,String deviceSerial,String Cam);
    JSONObject getDevicecapacity(Integer customer_id, String deviceSerial);
    JSONObject addDevice(Integer customer_id, String serial,String validateCode, String name,String discription);
    JSONObject delDevice(Integer customer_id,String serial);
    JSONObject dealGetDevices(Integer customerId);
    JSONObject updateDeviceInfo(JSONObject cameraJson);
    JSONObject getDeviceBySerial(Integer customerId,String serial);
    int dealAddDevice(Integer customerId, Camera camera);
    int dealDeleteDevice(Integer customerId, String camera_id);
    JSONObject setDeviceName(Integer customerId,String deviceSerial,String deviceName);


}
