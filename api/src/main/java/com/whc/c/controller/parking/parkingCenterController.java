package com.whc.c.controller.parking;

import com.wanhuchina.common.code.CommonCode;
import com.wanhuchina.common.code.TxResultResponse;
import com.wanhuchina.common.exception.CommonException;
import com.whc.c.service.parking.ParkingCenterService;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author ssh
 *         Email: shenshanghua@wanhuchina.com
 *         Date:  2018/3/19
 *         Time:  17:58
 */
@RestController
@RequestMapping(value = "/parkingCenter")
public class parkingCenterController {
    public static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(parkingCenterController.class);
    @Resource
    private ParkingCenterService parkingCenterService;

    /**
     * 车场储物柜 获取订单列表
     * @param openId
     * @param status
     * @return
     */
    @RequestMapping(value = "/getCarOrderList",method = RequestMethod.POST)
    public TxResultResponse getCarOrderList(@RequestParam String openId, @RequestParam String status){
        LOGGER.info("进入车场储物柜订单支付业务逻辑,getCarOrderList，openId={},status={}",openId,status);
        TxResultResponse resultResponse = new TxResultResponse(CommonCode.SUCCESS.getCode(),"操作成功");
        try{
            return parkingCenterService.getCarOrderList(openId, status);
        } catch (CommonException e) {
            return new TxResultResponse(e.getCode(),e.getMsg());
        } catch (Exception e) {
            return new TxResultResponse(CommonCode.SERVER_ERROR.getCode(),"服务器内部异常");
        }
    }

    /**
     * 车场储物柜 获取订单列表
     * @param orderId
     * @param startDate
     * @param endDate
     * @return
     */
    @RequestMapping(value = "/closeOrder",method = RequestMethod.POST)
    public TxResultResponse closeOrder(@RequestParam String orderId,
                                       @RequestParam String startDate,
                                       @RequestParam String endDate){
        LOGGER.info("进入车场储物柜订单业务逻辑,closeOrder，orderId={},startDate={},endDate={}",orderId,startDate,endDate);
        TxResultResponse resultResponse = new TxResultResponse(CommonCode.SUCCESS.getCode(),"操作成功");
        try{
            return parkingCenterService.finishParkingOrder(orderId, startDate, endDate);
        } catch (CommonException e) {
            return new TxResultResponse(e.getCode(),e.getMsg());
        } catch (Exception e) {
            return new TxResultResponse(CommonCode.SERVER_ERROR.getCode(),"服务器内部异常");
        }
    }


    /**
     * 车场储物柜 获取订单列表
     * @param orderId
     * @param wahoceId
     * @param pwd
     * @return
     */
    @RequestMapping(value = "/updStoragePwd",method = RequestMethod.POST)
    public TxResultResponse updStoragePwd(@RequestParam String wahoceId,
                                       @RequestParam String orderId,
                                       @RequestParam String pwd){
        LOGGER.info("进入车场储物柜订单业务逻辑,updStoragePwd，wahoceId={},orderId={},pwd={}",wahoceId,orderId,pwd);
        TxResultResponse resultResponse = new TxResultResponse(CommonCode.SUCCESS.getCode(),"操作成功");
        try{
            return parkingCenterService.updStoragePwd(wahoceId, orderId, pwd);
        } catch (CommonException e) {
            return new TxResultResponse(e.getCode(),e.getMsg());
        } catch (Exception e) {
            return new TxResultResponse(CommonCode.SERVER_ERROR.getCode(),"服务器内部异常");
        }
    }



    /**
     * 车场储物柜 获取订单列表
     * @param orderId
     * @return
     */
    @RequestMapping(value = "/orderDetail",method = RequestMethod.POST)
    public TxResultResponse orderDetail(@RequestParam String orderId){
        LOGGER.info("进入车场储物柜订单业务逻辑,updStoragePwd，orderId={}",orderId);
        TxResultResponse resultResponse = new TxResultResponse(CommonCode.SUCCESS.getCode(),"操作成功");
        try{
            return parkingCenterService.orderDetail(orderId);
        } catch (CommonException e) {
            return new TxResultResponse(e.getCode(),e.getMsg());
        } catch (Exception e) {
            return new TxResultResponse(CommonCode.SERVER_ERROR.getCode(),"服务器内部异常");
        }
    }

}
