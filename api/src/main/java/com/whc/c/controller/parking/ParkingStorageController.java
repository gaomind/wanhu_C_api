package com.whc.c.controller.parking;

import com.wanhuchina.common.code.CommonCode;
import com.wanhuchina.common.code.TxResultResponse;
import com.wanhuchina.common.exception.CommonException;
import com.whc.c.model.order.OrderInceParam;
import com.whc.c.service.parking.ParkingStorageService;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author ssh
 *         Email: shenshanghua@wanhuchina.com
 *         Date:  2018/3/19
 *         Time:  11:50
 */
@RestController
@RequestMapping(value = "/parkingManage")
public class ParkingStorageController {
    public static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(parkingCenterController.class);
    @Resource
    private ParkingStorageService parkingStorageService;


    /**
     * 车场储物柜 获取门店地图分布
     * @return
     */
    @RequestMapping(value = "/distributionByMap",method = RequestMethod.POST)
    public TxResultResponse distributionByMap(String openId,
                                              String unionId,
                                              @RequestParam String keyword,
                                              @RequestParam String type,
                                              String accessTime){
        LOGGER.info("进入车场储物柜业务逻辑，distributionByMap");
        TxResultResponse resultResponse = new TxResultResponse(CommonCode.SUCCESS.getCode(),"操作成功");
        try{
            return parkingStorageService.distributionByMap(openId, unionId, keyword, type, accessTime);
        } catch (CommonException e) {
            return new TxResultResponse(e.getCode(),e.getMsg());
        } catch (Exception e) {
            return new TxResultResponse(CommonCode.SERVER_ERROR.getCode(),"服务器内部异常");
        }
    }

    /**
     * 车场储物柜 新增订单列表
     * @return
     */
    @RequestMapping(value = "/addImmediateCar",method = RequestMethod.POST)
    @ResponseBody
    public TxResultResponse addImmediateCar(@RequestBody OrderInceParam orderInceParam){
        LOGGER.info("进入车场储物柜订单支付业务逻辑，OrderInceParam={}，",orderInceParam.toString());
        TxResultResponse resultResponse = new TxResultResponse(CommonCode.SUCCESS.getCode(),"操作成功");
        try{
            return parkingStorageService.addImmediateCar(orderInceParam);
        } catch (CommonException e) {
            return new TxResultResponse(e.getCode(),e.getMsg());
        } catch (Exception e) {
            return new TxResultResponse(CommonCode.SERVER_ERROR.getCode(),"服务器内部异常");
        }
    }

    /**
     * 车场储物柜 修改订单状态
     * @param orderId
     * @param transactionId
     * @param tradeOrderId
     * @return
     */
    @RequestMapping(value = "/updateParkingOrderStatus",method = RequestMethod.POST)
    public TxResultResponse updateParkingOrderStatus(@RequestBody String orderId,
                                                     @RequestBody String transactionId,
                                                     @RequestBody String tradeOrderId){
        LOGGER.info("进入车场储物柜订单支付业务逻辑，orderId={}，transactionId={}，tradeOrderId={}，",orderId,transactionId,tradeOrderId);
        TxResultResponse resultResponse = new TxResultResponse(CommonCode.SUCCESS.getCode(),"操作成功");
        try{
            return parkingStorageService.updateParkingOrderStatus(orderId, transactionId, tradeOrderId);
        } catch (CommonException e) {
            return new TxResultResponse(e.getCode(),e.getMsg());
        } catch (Exception e) {
            return new TxResultResponse(CommonCode.SERVER_ERROR.getCode(),"服务器内部异常");
        }
    }



}
