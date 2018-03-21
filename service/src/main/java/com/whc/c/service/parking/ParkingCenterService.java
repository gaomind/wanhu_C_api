package com.whc.c.service.parking;

import com.wanhuchina.common.code.TxResultResponse;
import com.wanhuchina.common.exception.CommonException;

/**
 *    车场储物柜个人中心
 *         @author ssh
 *         Email: shenshanghua@wanhuchina.com
 *         Date:  2018/3/19
 *         Time:  12:28
 */
public interface ParkingCenterService {


    /**
     * 订单中心  车场储物柜
     *
     * @param openId
     * @param status
     * @throws CommonException
     * @return
     */
    TxResultResponse getCarOrderList(String openId, String status) throws CommonException;

    /**
     * 车场储物柜退单
     *
     * @param orderId
     * @param startDate
     * @param endDate
     * @return
     * @throws CommonException
     */
    TxResultResponse finishParkingOrder(String orderId, String startDate, String endDate) throws CommonException;


    /**
     * 更新密码状态
     * @param wahoceId
     * @param orderId
     * @param pwd
     * @throws CommonException
     * @return
     */
    TxResultResponse updStoragePwd(String wahoceId ,String orderId , String pwd) throws CommonException;

    /**
     * 获取订单详情
     *
     * @param orderId
     * @return
     * @throws CommonException
     */
    TxResultResponse orderDetail(String orderId) throws CommonException;

}
