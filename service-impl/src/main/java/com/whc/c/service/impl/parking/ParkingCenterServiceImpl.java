package com.whc.c.service.impl.parking;

import com.google.common.base.Strings;
import com.wanhuchina.common.code.CommonCode;
import com.wanhuchina.common.code.TxResultResponse;
import com.wanhuchina.common.exception.CommonException;
import com.whc.aip.member.model.member.Member;
import com.whc.aip.member.model.order.CarOrdesByMemCenter;
import com.whc.aip.member.model.order.Order;
import com.whc.aip.member.model.order.OrderDetail;
import com.whc.aip.member.service.member.MemCenterService;
import com.whc.aip.member.service.member.MemberService;
import com.whc.aip.member.service.order.OrderService;
import com.whc.aip.storage.model.warehouse.WarehouseCell;
import com.whc.aip.storage.service.dispenser.DispensertWarehouseCellService;
import com.whc.aip.storage.service.warehouse.WarehouseCellService;
import com.whc.c.service.parking.ParkingCenterService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ssh
 *         Email: shenshanghua@wanhuchina.com
 *         Date:  2018/3/19
 *         Time:  12:37
 */
@Service("parkingCenterService")
public class ParkingCenterServiceImpl implements ParkingCenterService {

    public static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ParkingCenterService.class);

    @Resource
    private OrderService orderService;
    @Resource
    private MemberService memberService;
    @Resource
    private MemCenterService memCenterService;
    @Resource
    private WarehouseCellService warehouseCellService;

    @Resource
    private DispensertWarehouseCellService dispensertWarehouseCellService;


    /**
     *车场储物柜2.0 订单列表
     * @param openId
     * @param status
     * @return
     * @throws CommonException
     */
    @Override
    public TxResultResponse getCarOrderList(String openId, String status) throws CommonException {
        TxResultResponse tx = new TxResultResponse(CommonCode.SUCCESS.getCode(), "操作成功");
        HashMap<String, Object> data = new HashMap<String, Object>(16);
        try {
            //如果opId为空的话 就去查询memberId
            Member member = null;
            if (!Strings.isNullOrEmpty(openId)) {
                member = memberService.selByOpenId(openId);
            }
            List<CarOrdesByMemCenter> memCenterCarYardList = memCenterService.getMemCenterCarYardList(member.getId(), status);
            data.put("member",member);
            data.put("carOrderList",memCenterCarYardList);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            data.put("nowDate", sdf.format(new Date()));
            tx.setData(data);
            return tx;
        } catch (Exception e) {
            LOGGER.warn("服务器内部异常,code={},error={}", e);
            throw new CommonException(CommonCode.SERVER_ERROR.getCode(), CommonCode.SERVER_ERROR.getMsg());
        }
    }

    /**
     * 结束订单
     * @param orderId
     * @param startDate
     * @param endDate
     * @return
     * @throws CommonException
     */
    @Override
    public TxResultResponse finishParkingOrder(String orderId, String startDate, String endDate) throws CommonException {
        TxResultResponse resultResponse = new TxResultResponse(CommonCode.SUCCESS.getCode(), "操作成功");
        LOGGER.info("车场储物柜退单流程，finishParkingOrder ，参数 orderId={},startDate={}，endDate={}", orderId);
        try {
            //根据订单id获取订单信息
            Order order = orderService.selByOrderId(orderId);
            if (order == null) {
                return new TxResultResponse(CommonCode.PARAM_ERROR.getCode(), "参数orderId为空");
            }
            if (order.getId() != null && !"".equals(order.getId())) {
                order.setStatus("2");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                order.setReturnCellTime(sdf.format(new Date()));
                order.setOverDate(sdf.format(new Date()));
                //更新订单状态
                int isSuccess = orderService.updOrderById(order);
                if (isSuccess == 1) {
                    //获取仓位信息
                    Map<String, Object> warehouseCellMap = dispensertWarehouseCellService.getDispensertWarehouseCell(order.getWahoceId());
                    WarehouseCell warehouseCell = (WarehouseCell) warehouseCellMap.get("warehouseCell");
                    if (StringUtils.isBlank(warehouseCell.getId())) {
                        throw new CommonException(CommonCode.DATA_NOT_FOUND.getCode(), CommonCode.DATA_NOT_FOUND.getMsg() + ",warehouseCell未找到");
                    }
                    //更新仓位状态：释放柜体
                    Map<String, Object> warehouseCellStatusMap = dispensertWarehouseCellService.updateWarehouseCellStatus(warehouseCell.getId(), "0");
                    warehouseCellStatusMap.get("result");
                    if (!"1".equals(warehouseCellStatusMap.get("result").toString())) {
                        throw new CommonException(CommonCode.SQL_UPD_ERROR.getCode(), "updateWarehouseCellStatus-->仓位状态修改失败");
                    }
                    return resultResponse;
                } else {
                    LOGGER.info("车场储物柜退单流程，finishParkingOrder ，参数 orderId={},updOrderById-->订单状态修改失败", orderId);
                    throw new CommonException(CommonCode.SQL_UPD_ERROR.getCode(), "updOrderById-->订单状态修改失败");
                }
            } else {
                throw new CommonException(CommonCode.DATA_NOT_FOUND.getCode(), CommonCode.DATA_NOT_FOUND.getMsg() + ",订单未找到");
            }
        } catch (CommonException e) {
            return new TxResultResponse(e.getCode(), e.getMsg());
        } catch (Exception e) {
            e.printStackTrace();
            throw new CommonException(CommonCode.SERVER_ERROR.getCode(), CommonCode.SERVER_ERROR.getMsg());
        }
    }

    /**
     * 修改仓柜密码
     * @param wahoceId
     * @param orderId
     * @param pwd
     * @return
     * @throws CommonException
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = CommonException.class)
    public TxResultResponse updStoragePwd(String wahoceId, String orderId, String pwd) throws CommonException {
        LOGGER.info("车场储物柜修改仓柜密码流程，updStoragePwd ，参数 wahoceId={},orderId={},pwd={},updOrderById-->订单状态修改失败", wahoceId,orderId,pwd);
        TxResultResponse tx = new TxResultResponse(CommonCode.SUCCESS.getCode(),"操作成功");
        try {
            if(Strings.isNullOrEmpty(pwd)){
                throw new CommonException(CommonCode.PARAM_ERROR.getCode(),"参数密码为空");
            }
            WarehouseCell warehouseCell = warehouseCellService.getWarehouseCell(wahoceId);
            warehouseCell.setParkingPassword(pwd);
            int i = warehouseCellService.updWahoCell(warehouseCell);
            if(i<1){
                throw new CommonException(CommonCode.SQL_UPD_ERROR.getCode(),"设置门柜密码失败");
            }
            Order order = orderService.selByOrderId(orderId);
            order.setORDER_PASSWORD(pwd);
            int index = orderService.updOrderById(order);
            if(index<1){
                throw new CommonException(CommonCode.SQL_UPD_ERROR.getCode(),"修改订单密码（车场储物柜）失败");
            }
            return tx;
        } catch (CommonException e) {
            LOGGER.info("车场储物柜修改仓柜密码流程报错，updStoragePwd，e={}", e);
            return new TxResultResponse(e.getCode(), e.getMsg());
        } catch (Exception e) {
            LOGGER.info("车场储物柜修改仓柜密码流程报错，updStoragePwd，e={}", e);
            throw new CommonException(CommonCode.SERVER_ERROR.getCode(), CommonCode.SERVER_ERROR.getMsg());
        }
    }

    /**
     * 订单详情
     * @param orderId
     * @return
     * @throws CommonException
     */
    @Override
    public TxResultResponse orderDetail(String orderId) throws CommonException {
        TxResultResponse resultResponse = new TxResultResponse(CommonCode.SUCCESS.getCode(), "操作成功");
        LOGGER.info("订单详情接口，orderDetail ，参数 orderId={}", orderId);
        try {
            List<OrderDetail> orderDetails = orderService.selDetailById(orderId);

            resultResponse.setData(orderDetails);
            return resultResponse;
        } catch (CommonException e) {
            return new TxResultResponse(e.getCode(), e.getMsg());
        } catch (Exception e) {
            e.printStackTrace();
            throw new CommonException(CommonCode.SERVER_ERROR.getCode(), CommonCode.SERVER_ERROR.getMsg());
        }
    }
}
