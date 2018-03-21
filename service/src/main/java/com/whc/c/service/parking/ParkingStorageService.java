package com.whc.c.service.parking;

import com.wanhuchina.common.code.TxResultResponse;
import com.wanhuchina.common.exception.CommonException;
import com.whc.c.model.order.OrderInceParam;

/**
 *         @author ssh
 *         Email: shenshanghua@wanhuchina.com
 *         Date:  2018/3/19
 *         Time:  11:57
 */
public interface ParkingStorageService {


    /**
     * 车场储物柜 车场分布情况
     * @param openId
     * @param unionId
     * @param keyword
     * @param type
     * @param accessTime
     * @return
     * @throws CommonException
     */
    TxResultResponse distributionByMap(String openId, String unionId, String keyword, String type, String accessTime) throws CommonException;

    /**
     * 新增订单
     *
     * @param orderInceParam
     * @return
     * @throws CommonException
     */
    TxResultResponse addImmediateCar(OrderInceParam orderInceParam) throws CommonException;



    /**
     * 更新订单状态 首次缴费和再次缴费
     *
     * @param orderId
     * @param transactionId
     * @param tradeOrderId
     * @return
     * @throws CommonException
     */
    TxResultResponse updateParkingOrderStatus(String orderId, String transactionId, String tradeOrderId) throws CommonException;





}
