package com.edu.bupt.camera.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.edu.bupt.camera.dao.CameraMapper;
import com.edu.bupt.camera.dao.CameraUserMapper;
import com.edu.bupt.camera.dao.CameraUserRelationMapper;
import com.edu.bupt.camera.dao.UserMapper;
import com.edu.bupt.camera.model.Camera;
import com.edu.bupt.camera.model.CameraUser;
import com.edu.bupt.camera.model.CameraUserRelation;
import com.edu.bupt.camera.service.CameraService;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.edu.bupt.camera.model.User;
import org.springframework.util.DigestUtils;

import java.io.IOException;
import java.util.Date;
import java.util.List;


@Service
public class CameraServiceImpl implements CameraService {

    private JSONObject appInfo = new JSONObject();
    private static OkHttpClient client = new OkHttpClient();

    @Autowired
    private CameraMapper cameraMapper;

    @Autowired
    private CameraUserMapper cameraUserMapper;

    @Autowired
    private CameraUserRelationMapper relationMapper;

    @Autowired
    private UserMapper userMapper;


    private JSONObject  getAppInfoByuserInfo(Integer customerId){
        CameraUser user = cameraUserMapper.selectByPrimaryKey(customerId);
        return JSONObject.parseObject(user.toString());
    }

    private boolean isAppkeyRegisted(Integer customerId,String appKey){
        CameraUser user = cameraUserMapper.selectByAppKey(appKey);
        if(user != null && user.getAppkey().equals(appKey)){
            return true;
        }
        return false;
    }
    //chewangle
    public JSONObject register(JSONObject userJson){
        JSONObject resp = new JSONObject();
        int insert = 0;
        CameraUser user = new CameraUser();

        user.setCustomerId(userJson.getInteger("customerId"));
        user.setAppkey(userJson.getString("appKey"));
        user.setAppsecret(userJson.getString("appSecret"));
        JSONObject accessTokenJson = sendForaccessToken(userJson.getInteger("customerId"),
                                                        userJson.getString("appKey"),
                                                        userJson.getString("appSecret"));
        if (accessTokenJson.getString("status").equals("404") || accessTokenJson.getString("status").equals("500")) {
            resp.put("status","400");
            resp.put("msg","注册错误，请检查AppKey和appSecret！");
        } else {
            user.setAccesstoken(accessTokenJson.getString("msg"));
            try {
                insert = cameraUserMapper.insertSelective(user);
            }catch (Exception e){
                resp.put("status","500");
                resp.put("msg","你已经注册过了");
            }
            if(insert == 1){
                resp.put("status","200");
                resp.put("msg","注册成功！");
           }
        }
        return resp;
    }

    private String POST(Request request){
        String ret = null;
        String result = new String();
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                result = response.body().string();
                JSONObject resultJson = JSONObject.parseObject(result);
                System.out.println("POST Result:"+result);
                if (true == resultJson.getString("code").equals("200") ||
                        true == resultJson.getString("code").equals("20017")) {
                    ret = result;
                }else{
                    //("post code error:"+resultJson.getString("code"));
                    ret = null;
                }
            }else{
                //("respose failed");
                ret = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            ret = null;
        }
        return  ret;
    }

    public JSONObject getAccessToken(Integer customerId) {
        JSONObject ret = new JSONObject();
        CameraUser user = null;
        user = cameraUserMapper.selectByPrimaryKey(customerId);
        if (null == user) {
            ret.put("status", "404");
            ret.put("msg", "未注册");
            return ret;
        }
        if (!validAccessToken(user)){
            JSONObject accessTokenJson = sendForaccessToken(customerId);
            if (accessTokenJson.getString("status").equals("200")) { //错误信息的长度不会大于10
                String accessToken = accessTokenJson.getString("msg");
                Date now = new Date();

                user.setAccesstoken(accessToken);
                user.setTimestamp(now);
                cameraUserMapper.updateByPrimaryKeySelective(user);
                System.out.println("token 失效");
                ret.put("msg", accessToken);
                ret.put("status", "200");
            }else{
                ret.put("msg","获取Token错误");
                ret.put("status","500");
            }
        }else{
            System.out.println("token 有效");
            ret.put("msg", user.getAccesstoken());
            ret.put("status", "200");
        }
        return ret;
    }

    public boolean validAccessToken(CameraUser user) {
        return false;
    }


    public JSONObject sendForaccessToken(Integer customerId,String appKey,String appSecret) {
        String postUrl = "https://open.ys7.com/api/lapp/token/get";
        JSONObject result = new JSONObject();
        if(null == customerId){
            result.put("status","404");
        }else {
            okhttp3.RequestBody body = new FormBody.Builder()
                    .add("appKey", appKey)
                    .add("appSecret", appSecret).build();
            Request request = new Request.Builder()
                    .url(postUrl)
                    .post(body)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    JSONObject resultJson = JSONObject.parseObject(response.body().string());
                    if (true == resultJson.getString("code").equals("200")) {
                        result.put("msg",resultJson.getJSONObject("data").getString("accessToken"));
                        result.put("status","200");
                    } else {
                        result.put("status","500");
                    }
                }else{
                    result.put("status","500");
                }
            } catch (IOException e) {
                e.printStackTrace();
                result.put("status","500");
            }
        }
        return result;
    }

    public JSONObject sendForaccessToken(Integer customerId) {
        String postUrl = "https://open.ys7.com/api/lapp/token/get";
        JSONObject result = new JSONObject();
        CameraUser user = cameraUserMapper.selectByPrimaryKey(customerId);
        if(null == user){
            result.put("status","404");
        }else {
            okhttp3.RequestBody body = new FormBody.Builder()
                    .add("appKey", user.getAppkey())
                    .add("appSecret", user.getAppsecret()).build();
            Request request = new Request.Builder()
                    .url(postUrl)
                    .post(body)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    JSONObject resultJson = JSONObject.parseObject(response.body().string());

                    if (true == resultJson.getString("code").equals("200")) {
                        result.put("msg",resultJson.getJSONObject("data").getString("accessToken"));
                        result.put("status","200");
                    } else {
                        result.put("status","500");
                    }
                } else {
                    result.put("status","500");
                }
            } catch (IOException e) {
                e.printStackTrace();
                result.put("status","500");
            }
        }
        return result;
    }
        public JSONObject getLiveAddrBydeviceSerial(Integer customerId, String deviceSerial,String Cam){

        String postUrl = "https://open.ys7.com/api/lapp/live/address/get";
        JSONObject ret = new JSONObject();
        Camera camera = cameraMapper.selectByPrimaryKey(deviceSerial);
        System.out.println(camera.toString());
        if(null != camera) {
            CameraUserRelation relation = relationMapper.selectByCameraId(deviceSerial);
            if (null == relation){
                ret.put("status", "404");
                ret.put("msg", "该设备不属于当前用户！");
                return ret;
            }
        }else{
            ret.put("status", "404");
            ret.put("msg", "该设备不存在！");
            return ret;
        }
        JSONObject res = getAccessToken(customerId);
        if(!res.getString("status").equals("200")){
            return res;
        }
        String accessToken = res.getString("msg");
        okhttp3.RequestBody body = new FormBody.Builder()
                .add("accessToken", accessToken)
                .add("source",deviceSerial+":"+Cam).build();
        Request request = new Request.Builder()
                .url(postUrl)
                .post(body)
                .build();
        String response = this.POST(request);
        if(null != response){
            ret.put("msg",JSONObject.parseObject(response).getJSONArray("data"));
            ret.put("status","200");
        }else{
            ret.put("msg","请检查序列号和校验码！");
            ret.put("status","400");
        }
        return ret;
    }

    public JSONObject openLiveBydeviceSerial(Integer customerId,String deviceSerial,String Cam){
        String postUrl = "https://open.ys7.com/api/lapp/live/video/open";
        JSONObject ret = new JSONObject();
        Camera camera = cameraMapper.selectByPrimaryKey(deviceSerial);
        System.out.println(camera.toString());
        if(null != camera) {
            CameraUserRelation relation = relationMapper.selectByCameraId(deviceSerial);
            if (null == relation){
                ret.put("status", "404");
                ret.put("msg", "该设备不属于当前用户！");
                return ret;
            }
        }else{
            ret.put("status", "404");
            ret.put("msg", "该设备不存在！");
            return ret;
        }
        JSONObject res = getAccessToken(customerId);
        if(!res.getString("status").equals("200")){
            return res;
        }
        String accessToken = res.getString("msg");

        okhttp3.RequestBody body = new FormBody.Builder()
                .add("accessToken", accessToken)
                .add("source",deviceSerial+":"+Cam).build();
        Request request = new Request.Builder()
                .url(postUrl)
                .post(body)
                .build();

        String response = this.POST(request);
        if(null != response){
            ret.put("status","200");
            ret.put("msg",JSONObject.parseObject(response).getJSONArray("data"));
        }else{
            ret.put("status","500");
            ret.put("msg","内部错误");
        }
        return ret;
    }

    public JSONObject closeLiveBydeviceSerial(Integer customerId,String deviceSerial,String Cam){
        String postUrl = "https://open.ys7.com/api/lapp/live/video/close";
        JSONObject ret = new JSONObject();
        Camera camera = cameraMapper.selectByPrimaryKey(deviceSerial);
        System.out.println(camera.toString());
        if(null != camera) {
            CameraUserRelation relation = relationMapper.selectByCameraId(deviceSerial);
            if (null == relation){
                ret.put("status", "404");
                ret.put("msg", "该设备不属于当前用户！");
                return ret;
            }
        }else{
            ret.put("status", "404");
            ret.put("msg", "该设备不存在！");
            return ret;
        }
        JSONObject res = getAccessToken(customerId);
        if(!res.getString("status").equals("200")){
            return res;
        }
        String accessToken = res.getString("msg");

        okhttp3.RequestBody body = new FormBody.Builder()
                .add("accessToken", accessToken)
                .add("source",deviceSerial+":"+Cam).build();
        Request request = new Request.Builder()
                .url(postUrl)
                .post(body)
                .build();

        String response = this.POST(request);
        if(null != response){
            ret.put("status","200");
            ret.put("msg",JSONObject.parseObject(response).getJSONArray("data"));
        }else{
            ret.put("status","500");
            ret.put("msg","内部错误");
        }
        return ret;
    }

    public JSONObject getDevicecapacity(Integer customerId, String deviceSerial){
        String postUrl = "https://open.ys7.com/api/lapp/device/capacity";
        JSONObject ret = new JSONObject();
        Camera camera = cameraMapper.selectByPrimaryKey(deviceSerial);
        System.out.println(camera.toString());
        if(null != camera) {
            CameraUserRelation relation = relationMapper.selectByCameraId(deviceSerial);
            if (null == relation){
                ret.put("status", "404");
                ret.put("msg", "该设备不属于当前用户！");
                return ret;
            }
        }else{
            ret.put("status", "404");
            ret.put("msg", "该设备不存在！");
            return ret;
        }
        JSONObject res = getAccessToken(customerId);
        if(!res.getString("status").equals("200")){
            return res;
        }
        String accessToken = res.getString("msg");

        okhttp3.RequestBody body = new FormBody.Builder()
                .add("accessToken", accessToken)
                .add("deviceSerial",deviceSerial).build();
        Request request = new Request.Builder()
                .url(postUrl)
                .post(body)
                .build();

        String response = this.POST(request);
        if(null != response){
            ret.put("msg",JSONObject.parseObject(response).getJSONObject("data"));
            ret.put("status","200");
        }else{
            ret.put("status","500");
            ret.put("msg","内部错误");
        }
        return ret;
    }

    public JSONObject addDevice(Integer customerId, String serial, String validateCode, String name,String discription){
        String postUrl = "https://open.ys7.com/api/lapp/device/add";
        Camera camera = new Camera();
        JSONObject ret = new JSONObject();
        JSONObject res = getAccessToken(customerId);
        if(!res.getString("status").equals("200")){
            return res;
        }
        String accessToken = res.getString("msg");
        okhttp3.RequestBody body = new FormBody.Builder()
                .add("accessToken", accessToken)
                .add("deviceSerial",serial)
                .add("validateCode",validateCode).build();
        Request request = new Request.Builder()
                .url(postUrl)
                .post(body)
                .build();
        String response = this.POST(request);
        if(null != response){
            // add device into database
            String id = serial;
            camera.setId(id);
            camera.setSerial(serial);
            camera.setName(name);
            // 存设备信息到数据库
            for (int i =0; i<3; i++) {
                int add = dealAddDevice(customerId, camera);
                if (add != 0) {
                    // 设置云平台的设备信息
                    setDeviceName(customerId,camera.getSerial(),camera.getName());
                    ret.put("status","200");
                    JSONObject cameraJson = new JSONObject();
                    cameraJson.put("cameraId",camera.getId());
                    cameraJson.put("deviceSerial",camera.getSerial());
                    ret.put("msg",getDeviceBySerial(customerId,serial).getJSONObject("msg"));
                    break;
                } else {
                    ret.put("status","500");
                    ret.put("msg", "fail");
                    continue;
                }
            }
        }else{
            ret.put("status","500");
            ret.put("msg","设备添加错误，请检查序列号和校验码！");
        }
        return ret;
    }

    public JSONObject delDevice(Integer customerId,String deviceSerial){
        JSONObject ret = new JSONObject();
        Camera camera = cameraMapper.selectByPrimaryKey(deviceSerial);
        System.out.println(camera.toString());
        if(null != camera) {
            CameraUserRelation relation = relationMapper.selectByCameraId(deviceSerial);
            if (null != relation) {
                int delete = dealDeleteDevice(customerId, deviceSerial);
                if (delete != 0) {
                    ret.put("msg", "删除成功");
                    ret.put("status", "200");
                } else {
                    ret.put("status", "500");
                    ret.put("msg", "sql fail");
                }
            } else {
                ret.put("status", "404");
                ret.put("msg", "该设备不属于当前用户！");
            }
        }else {
            ret.put("status", "404");
            ret.put("msg", "该设备不存在！");
        }
        return ret;
    }

    @Override
    public JSONObject dealGetDevices(Integer customerId){
        JSONObject ret = new JSONObject();
        JSONArray cameraInfo = new JSONArray();
        List<String> cameraList = relationMapper.selectCameraIdByCustomerId(customerId);
        if (null != cameraList && !cameraList.isEmpty()) {
            JSONObject deviceInfo = new JSONObject();
            for (int i = 0; i < cameraList.size(); i++) {
                deviceInfo = getDeviceBySerial(customerId,cameraList.get(i));
                if(deviceInfo.getString("status").equals("200")) {
                    cameraInfo.add(i,deviceInfo.getJSONObject("msg"));
                }
                deviceInfo.clear();
            }
            ret.put("msg",cameraInfo);
            ret.put("status","200");
        }else{
            ret.put("msg","没有找到该用户的设备！");
            ret.put("status","404");
        }
        return ret;
    }


    public JSONObject setDeviceName(Integer customerId,String deviceSerial,String deviceName){
        String postUrl = "https://open.ys7.com/api/lapp/device/name/update";
        JSONObject ret = new JSONObject();
        Camera camera = cameraMapper.selectByPrimaryKey(deviceSerial);
        System.out.println(camera.toString());
        if(null != camera) {
            CameraUserRelation relation = relationMapper.selectByCameraId(deviceSerial);
            if (null == relation){
                ret.put("status", "404");
                ret.put("msg", "该设备不属于当前用户！");
                return ret;
            }
        }else{
            ret.put("status", "404");
            ret.put("msg", "该设备不存在！");
            return ret;
        }
        JSONObject res = getAccessToken(customerId);
        if(!res.getString("status").equals("200")){
            return res;
        }
        String accessToken = res.getString("msg");

        okhttp3.RequestBody body = new FormBody.Builder()
                .add("accessToken", accessToken)
                .add("deviceSerial",deviceSerial)
                .add("deviceName",deviceName).build();
        Request request = new Request.Builder()
                .url(postUrl)
                .post(body)
                .build();

        String response = this.POST(request);
        if(null != response){
            ret.put("msg",JSONObject.parseObject(response).getString("msg"));
            ret.put("status","200");
        }else{
            ret.put("status","500");
            ret.put("msg","内部错误");
        }
        return ret;
    }


    public JSONObject getDeviceBySerial(Integer customerId,String serial){
        String postUrl = "https://open.ys7.com/api/lapp/device/info";
        Camera camera = cameraMapper.selectByPrimaryKey(serial);
        JSONObject ret = new JSONObject();
        if(null != camera) {
            CameraUserRelation relation = relationMapper.selectByCameraId(serial);
            if (null == relation){
                ret.put("status", "404");
                ret.put("msg", "该设备不属于当前用户！");
                return ret;
            }
        }else{
            ret.put("status", "404");
            ret.put("msg", "该设备不存在！");
            return ret;
        }
        JSONObject res = getAccessToken(customerId);
        if(!res.getString("status").equals("200")){
            return res;
        }
        String accessToken = res.getString("msg");

        okhttp3.RequestBody body = new FormBody.Builder()
                .add("accessToken", accessToken)
                .add("deviceSerial",serial).build();
        Request request = new Request.Builder()
                .url(postUrl)
                .post(body)
                .build();

        String response = this.POST(request);
        if(null != response){
            ret.put("msg",JSONObject.parseObject(response).getJSONObject("data"));
            ret.put("status","200");
        }else{
            ret.put("status","500");
            ret.put("msg","内部错误");
        }
        return ret;
    }
    @Override
    public JSONObject  updateDeviceInfo(JSONObject cameraJson) {

        return setDeviceName(cameraJson.getInteger("customerId"),cameraJson.getString("serial"),
                      cameraJson.getString("name"));
    }

    @Override
    public int dealAddDevice(Integer customerId, Camera camera) {
        int insert = cameraMapper.insertSelective(camera);
        if(insert != 0) {
            CameraUserRelation r = new CameraUserRelation(camera.getId(), customerId);
            return relationMapper.insertSelective(r);
        }
        return 0;
    }

    @Override
    public int dealDeleteDevice(Integer customerId, String serial) {
        // 删除 camera_account_relation 中的关联关系
        Integer delete = 0;
        if( 1 == cameraMapper.deleteByPrimaryKey(serial)){
            delete = relationMapper.deleteByCustomerIdAndCameraId(serial);
        }
        return delete;
    }

}
