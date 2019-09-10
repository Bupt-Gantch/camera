package com.edu.bupt.camera.controller;

import com.alibaba.fastjson.JSONObject;
import com.edu.bupt.camera.model.CameraUser;
import com.edu.bupt.camera.service.CameraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/camera")
public class CameraController {

    @Autowired
    private CameraService cameraService;



    /*
    *   测试完成
    * */
    @RequestMapping(value = "/getToken", method = RequestMethod.GET)
    @ResponseBody
    public JSONObject getToken(@RequestParam("customerId")Integer id) throws Exception{

        return cameraService.getAccessToken(id);
    }


    /**
     * 获取摄像头流地址列表   测试完成
     * @param serial
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/getLiveAddressbySerial", method = RequestMethod.GET)
    @ResponseBody
    public JSONObject getLiveAddressbySerial(@RequestParam("customerId")Integer id,
                                             @RequestParam("serial")String serial) throws Exception{
        return cameraService.getLiveAddrBydeviceSerial(id,serial,"1");
    }



    /**
     * 打开视频流    ceshiwancheng
     * @param serial
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/openLive", method = RequestMethod.GET)
    @ResponseBody
    public JSONObject openLiveByserial(@RequestParam("customerId")Integer id,
                                        @RequestParam("serial") String serial) throws Exception{

        return cameraService.openLiveBydeviceSerial(id,serial,"1");

    }

    /**
     * 关闭视频流 ceshiwancheng
     * @param serial
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/closeLive", method = RequestMethod.GET)
    @ResponseBody
    public JSONObject closeLiveByserial(@RequestParam("customerId")Integer id,
                                       @RequestParam(value = "serial")String serial) throws Exception{
        return cameraService.closeLiveBydeviceSerial(id,serial,"1");
    }


    /**
     * 获取设备能力 ceshiwancheng
     * @param serial
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/getCapacity", method = RequestMethod.GET)
    @ResponseBody
    public JSONObject getDevicecapacitybySerial(@RequestParam("customerId")Integer id,
                                                @RequestParam(value = "serial", required = false)String serial) throws Exception{

        return cameraService.getDevicecapacity(id,serial);
    }


    /**
     * 添加摄像头 测试完成
     * @param data
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/addDevice", method = RequestMethod.POST)
    @ResponseBody
    public JSONObject addDevice(@RequestBody JSONObject data) throws Exception{

        return cameraService.addDevice(data.getInteger("customerId"),data.getString("serial"),
                                        data.getString("validateCode"),data.getString("name"),
                                        data.getString("discription"));
    }


    /**
     * 删除摄像头 测试完成
     * @param serial
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/delDevice", method = RequestMethod.GET)
    @ResponseBody
    public JSONObject delDevice(@RequestParam(value = "customerId",required = true) Integer customerId,
                                @RequestParam(value = "serial",required = true) String serial) throws Exception{
        return cameraService.delDevice(customerId,serial);
    }




    /**
     * 获取摄像头列表并排序 ceshiwancheng
     * @param customerId
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/getDevices", method = RequestMethod.GET)
    public JSONObject getDevices(@RequestParam(value = "customerId",required = true) Integer customerId) throws Exception{

        return cameraService.dealGetDevices(customerId);
    }

    /**
     * 修改摄像头信息 测试完成
     * @param body
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/updateDeviceInfo", method = RequestMethod.POST)
    public JSONObject updateDeviceInfo(@RequestBody JSONObject body) throws Exception{

        return cameraService.updateDeviceInfo(body);
    }

    /**
     * 用户注册 测试完成
     * @param customerId
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public JSONObject register(@RequestParam("customerId")Integer customerId) {
        JSONObject data = new JSONObject();
        data.put("customerId",customerId);
        data.put("appKey","9a51a0bfb7234668a6cbfb5b7c23271d");
        data.put("appSecret","cda559f8fdfd5d82234b48c0db9d61c9");
        return cameraService.register(data);
    }

    /**
     * 使用序列号获取设备信息
     * @param serial
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/getDeviceBySerial",method =  RequestMethod.GET)
    public  JSONObject getDeviceBySerial(@RequestParam("customerId")Integer customerId,
                                     @RequestParam("serial")String serial){
        return cameraService.getDeviceBySerial(customerId,serial);
    }
}
