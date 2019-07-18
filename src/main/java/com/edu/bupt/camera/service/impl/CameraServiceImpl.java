package com.edu.bupt.camera.service.impl;

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
//        this.appInfo.clear();
//        this.appInfo.put("appKey","2202b037f424462888e3918831dd9680");
//        this.appInfo.put("appSecret","4e45ac4dbaf66fddb8afb4da7e313cef");
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

    private boolean isSubAccount(Integer customerId,String appKey){
        return false;
    }


    //chewangle
    public JSONObject register(JSONObject userJson){
        JSONObject resp = new JSONObject();
        int insert = 0;
        CameraUser user = new CameraUser();

        user.setCustomerId(userJson.getInteger("customerId"));
        user.setAppkey(userJson.getString("appkey"));
        user.setAppsecret(userJson.getString("appSecret"));
        if(true == isAppkeyRegisted(user.getCustomerId(),user.getAppkey())){
            resp.put("status","403");
            resp.put("msg","该AppKey被人注册！");
            return resp;
        }
        String accessToken = sendForaccessToken(userJson.getInteger("customerId"),userJson.getString("appkey"),
                                                userJson.getString("appSecret"));

        if (accessToken.equals("404") || accessToken.equals("500")) {
            resp.put("status","400");
            resp.put("msg","注册错误，请检查AppKey和appSecret！");
        } else {
            user.setAccesstoken(accessToken);
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

    public JSONObject updateUserInfo(JSONObject userInfo){
        JSONObject ret = new JSONObject();
        String reslt =new String();
        CameraUser user = new CameraUser();
        user.setCustomerId(userInfo.getInteger("customerId"));
        user.setAppkey(userInfo.getString("appKey"));
        user.setAppsecret(userInfo.getString("appSecret"));
        reslt = sendForaccessToken(userInfo.getInteger("customerId"),
                                    userInfo.getString("appKey"),
                                    userInfo.getString("appSecret"));

        if(reslt.equals("404")||reslt.equals("500")){
            ret.put("status","400");
            ret.put("msg","请检查appKey和appSecret并且检查是否注册");
            return ret;
        }
        user.setAccesstoken(reslt);

        int update = cameraUserMapper.updateByPrimaryKeySelective(user);
        if (1 == update){
            ret.put("status","200");
            ret.put("msg","修改信息成功");
        }else{
            ret.put("status","400");
            ret.put("msg","修改信息失败,请检查appKey和appSecret");
        }
        return ret;
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
                if (true == resultJson.getString("code").equals("200")) {
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

    public JSONObject getAccessToken(Integer customerId){
        JSONObject ret = new JSONObject();
        CameraUser user = null;
        user = cameraUserMapper.selectByPrimaryKey(customerId);
        if(null == user){
            ret.put("status","404");
            ret.put("msg","未注册");
            return ret;
        }
        if(!validAccessToken(user)){
           String accessToken = sendForaccessToken(customerId);
           if(accessToken.length()>10){ //错误信息的长度不会大于10
               ret.put("msg",accessToken);
           }
        }else{
            ret.put("msg",user.getAccesstoken());
        }
        ret.put("status","200");
        return  ret;
    }

    public boolean validAccessToken(CameraUser user) {

        return false;

//        String timestamp = user.getStore();
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
//        ParsePosition pos = new ParsePosition(0);
//        Date last = format.parse(timestamp,pos);
//        Date now = new Date();
//        long yet = now.getTime() - last.getTime()/ 1000L; // 距离上一次更新过了多少秒
//        return yet < 1563856469974L;
    }


    public String sendForaccessToken(Integer customerId,String appKey,String appSecret) {
        String postUrl = "https://open.ys7.com/api/lapp/token/get";
        String result = new String();
        if(null == customerId){
            result = "404";
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
                    result = response.body().string();
                    JSONObject resultJson = JSONObject.parseObject(result);
                    if (true == resultJson.getString("code").equals("200")) {
                        result = resultJson.getJSONObject("data").getString("accessToken");
                    } else {
                        result = "500";
                    }
                } else {
                    result = "500";
                }
            } catch (IOException e) {
                e.printStackTrace();
                result = "500";
            }
        }
        return result;
    }

    public String sendForaccessToken(Integer customerId) {
        String postUrl = "https://open.ys7.com/api/lapp/token/get";
        String result = new String();
        CameraUser user = cameraUserMapper.selectByPrimaryKey(customerId);
        if(null == user){
            result = "404";
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
                    result = response.body().string();
                    JSONObject resultJson = JSONObject.parseObject(result);
                    if (true == resultJson.getString("code").equals("200")) {
                        result = resultJson.getJSONObject("data").getString("accessToken");
                        user.setAccesstoken(result);
                        cameraUserMapper.updateByPrimaryKeySelective(user);
                    } else {
                        result = "500";
                    }
                } else {
                    result = "500";
                }
            } catch (IOException e) {
                e.printStackTrace();
                result = "500";
            }
        }
        return result;
    }

    public JSONObject getLiveAddressList(Integer customerId){

        String postUrl = "https://open.ys7.com/api/lapp/live/video/list";
        JSONObject ret = new JSONObject();
        JSONObject res = getAccessToken(customerId);
        if(!res.getString("status").equals("200")){
            return res;
        }
        String accessToken = res.getString("msg");

        okhttp3.RequestBody body = new FormBody.Builder()
                .add("accessToken", accessToken).build();
        Request request = new Request.Builder()
                .url(postUrl)
                .post(body)
                .build();
        String result  = this.POST(request);
        if(null != result){
            JSONObject resultJson = JSONObject.parseObject(result);
            ret.put("data",resultJson.getJSONArray("data"));
            ret.put("status","200");
        }else{
            ret.put("status","500");
            ret.put("data","内部错误");
        }
        return ret;
    }

    public JSONObject getLiveAddrBydeviceSerial(Integer customerId, String deviceSerial,String Cam){

        String postUrl = "https://open.ys7.com/api/lapp/live/address/get";
        JSONObject ret = new JSONObject();

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
            ret.put("data",JSONObject.parseObject(response).getJSONArray("data"));
        }else{
            ret.put("status","500");
            ret.put("data","内部错误");
        }
        return ret;
    }

    public JSONObject closeLiveBydeviceSerial(Integer customerId,String deviceSerial,String Cam){
        String postUrl = "https://open.ys7.com/api/lapp/live/video/close";

        JSONObject ret = new JSONObject();
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
            ret.put("data",JSONObject.parseObject(response).getJSONArray("data"));
        }else{
            ret.put("status","500");
            ret.put("data","内部错误");
        }
        return ret;
    }

    public JSONObject getDevicecapacity(Integer customerId, String deviceSerial){
        String postUrl = "https://open.ys7.com/api/lapp/device/capacity";

        JSONObject ret = new JSONObject();
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
            ret.put("data",JSONObject.parseObject(response).getJSONObject("data"));
            ret.put("status","200");
        }else{
            ret.put("status","500");
            ret.put("data","内部错误");
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
            if (null == camera.getName()){ // 取id最后四位作为默认名
                camera.setName("camera_"+ id.substring(id.length()-4, id.length()));
            }
            // 设置云平台的设备信息
            setDeviceName(customerId,camera.getSerial(),camera.getName());
            // 存设备信息到数据库
            for (int i =0; i<3; i++) {
                int add = dealAddDevice(customerId, camera);
                if (add != 0) {
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
        String postUrl = "https://open.ys7.com/api/lapp/device/delete";
        JSONObject ret = new JSONObject();

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
        System.out.println(response);
        if(null != response){

            ret.put("status","200");
            for (int i = 0; i<3; i++) {
                int delete = dealDeleteDevice(customerId,deviceSerial);
                if (delete != 0) {
                    ret.put("msg",JSONObject.parseObject(response).getString("msg"));
                    ret.put("status","200");
                    break;
                } else {
                    ret.put("status","500");
                    ret.put("msg","sql fail");
                    continue;
                }
            }
        }else{
            ret.put("status","404");
            ret.put("msg","该设备不存在");
        }
        return ret;
    }

    @Override
    public JSONObject dealGetDevices(Integer customerId){
        String postUrl = "https://open.ys7.com/api/lapp/camera/list";
        JSONObject ret = new JSONObject();

        JSONObject res = getAccessToken(customerId);
        if(!res.getString("status").equals("200")){
            return res;
        }
        String accessToken = res.getString("msg");

        okhttp3.RequestBody body = new FormBody.Builder()
                .add("accessToken", accessToken).build();
        Request request = new Request.Builder()
                .url(postUrl)
                .post(body)
                .build();

        String response = this.POST(request);
        if(null != response){
            ret.put("data",JSONObject.parseObject(response).getJSONArray("data"));
            ret.put("status","200");
        }else{
            ret.put("status","500");
            ret.put("data","内部错误");
        }
        return ret;
    }


    public JSONObject setDeviceName(Integer customerId,String deviceSerial,String deviceName){
        String postUrl = "https://open.ys7.com/api/lapp/device/name/update";

        JSONObject ret = new JSONObject();

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

        JSONObject ret = new JSONObject();

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

    public JSONObject createSubAccount(Integer customerId,Integer subCustomerId){
        return null;
    }

    public JSONObject shareDevices(Integer customerId,String phone){
        String postUrl = "https://open.ys7.com/api/lapp/ram/account/create";
        JSONObject ret =new JSONObject();

        JSONObject res = getAccessToken(customerId);
        if(!res.getString("status").equals("200")){
            return res;
        }
        String accessToken = res.getString("msg");

        User user = userMapper.selectByPhone(phone);
        if(null == user){
            ret.put("status","403");
            ret.put("msg","对方尚未对小程序授权,请授权后重试");
            return ret;
        }
        String appKey = cameraUserMapper.selectByPrimaryKey(customerId).getAppkey();

        String accountName = customerId.toString()+"_"+user.getId().toString(); //作为子账户的一台设备的开头几位
        String password = user.getPhone();   //作为子账户的的一台设备开头的中间几位
        String passwordMD5 = appKey+"#"+password;
        passwordMD5 = DigestUtils.md5DigestAsHex(passwordMD5.getBytes()).toLowerCase();

        okhttp3.RequestBody body = new FormBody.Builder()
                .add("accessToken", accessToken)
                .add("accountName",accountName)
                .add("password",passwordMD5).build();
        Request request = new Request.Builder()
                .url(postUrl)
                .post(body)
                .build();
        String result = POST(request);
        if(null != result){
            JSONObject resultJson = JSONObject.parseObject(result);
            String accountId = resultJson.getJSONObject("data").getString("accountId");   //做为子账户一台设备号的最后部分
            String camera_id = accountName+"_"+password+"_"+accountId;  //mainId_subId_password_accountId
            CameraUserRelation cameraUserRelation = new CameraUserRelation();
            cameraUserRelation.setCameraId(camera_id);
            cameraUserRelation.setCustomerId(user.getId());
            int sqlUpdate = relationMapper.insertSelective(cameraUserRelation);
            if(1 == sqlUpdate){
                System.out.println("cameraId:"+camera_id);
                ret.put("status","200");
                ret.put("msg",resultJson.getJSONObject("data"));
            }
        }else{
            ret.put("status","500");
            ret.put("msg","网络访问错误");
        }
        return ret;
    }

    public JSONObject getSharedList(Integer customerId){
        String postUrl = "https://open.ys7.com/api/lapp/ram/account/list";
        JSONObject ret =new JSONObject();

        JSONObject res = getAccessToken(customerId);
        if(!res.getString("status").equals("200")){
            return res;
        }
        String accessToken = res.getString("msg");
        okhttp3.RequestBody body = new FormBody.Builder()
                .add("accessToken", accessToken).build();
        Request request = new Request.Builder()
                .url(postUrl)
                .post(body)
                .build();
        String result = POST(request);
        if(null != result){
            ret.put("serial","200");
            ret.put("msg",JSONObject.parseObject(result).getJSONArray("data"));
        }else{
            ret.put("status","500");
            ret.put("msg","网络访问错误");
        }
        return ret;
    }
    public JSONObject delSubAccount(Integer customerId,String accountId){
        String postUrl = "https://open.ys7.com/api/lapp/ram/account/delete";

        JSONObject ret = new JSONObject();

        JSONObject res = getAccessToken(customerId);
        if(!res.getString("status").equals("200")){
            return res;
        }
        String accessToken = res.getString("msg");

        okhttp3.RequestBody body = new FormBody.Builder()
                .add("accessToken", accessToken)
                .add("accountId",accountId).build();
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

    @Override
    public JSONObject  updateDeviceInfo(JSONObject cameraJson) {

        return setDeviceName(cameraJson.getInteger("customerId"),cameraJson.getString("serial"),
                      cameraJson.getString("name"));
    }

    @Override
    public int updateAlarmSettings(Integer customerId, String serial) {
        return 0;
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
