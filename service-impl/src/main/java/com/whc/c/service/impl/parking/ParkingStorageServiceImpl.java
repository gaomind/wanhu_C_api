package com.whc.c.service.impl.parking;

import com.alibaba.fastjson.JSONObject;
import com.wanhuchina.common.code.CommonCode;
import com.wanhuchina.common.code.TxResultResponse;
import com.wanhuchina.common.exception.CommonException;
import com.wanhuchina.common.util.zk.ZkPropertyUtil;
import com.whc.aip.member.model.member.Member;
import com.whc.aip.member.model.order.Order;
import com.whc.aip.member.model.order.OrderRenew;
import com.whc.aip.member.service.member.MemberService;
import com.whc.aip.member.service.order.OrderService;
import com.whc.aip.storage.model.warehouse.Warehouse;
import com.whc.aip.storage.model.warehouse.WarehouseCell;
import com.whc.aip.storage.service.dispenser.DispensertWarehouseCellService;
import com.whc.aip.storage.service.dispenser.DispensertWarehouseService;
import com.whc.aip.storage.service.warehouse.WarehouseService;
import com.whc.c.model.order.OrderInceParam;
import com.whc.c.service.parking.ParkingStorageService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author ssh
 *         Email: shenshanghua@wanhuchina.com
 *         Date:  2018/3/19
 *         Time:  11:55
 */
@Service("parkingStorageService")
public class ParkingStorageServiceImpl implements ParkingStorageService {

    public static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ParkingStorageService.class);

    @Resource
    private OrderService orderService;
    @Resource
    private MemberService memberService;
    @Resource
    private WarehouseService warehouseService;
    @Resource
    private DispensertWarehouseCellService dispensertWarehouseCellService;
    @Resource
    private DispensertWarehouseService dispensertWarehouseService;

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
    @Override
    public TxResultResponse distributionByMap(String openId, String unionId, String keyword, String type, String accessTime) throws CommonException {

        TxResultResponse tx = new TxResultResponse(CommonCode.SUCCESS.getCode(), "操作成功");
        try {
            return warehouseService.getParkingWarehouseList(openId, unionId, keyword, type, accessTime);
        } catch (Exception e) {
            LOGGER.warn("服务器内部异常,code={},error={}", e);
            throw new CommonException(CommonCode.SERVER_ERROR.getCode(), CommonCode.SERVER_ERROR.getMsg());
        }


    }


    /**
     * 添加订单
     * @param orderInceParam
     * @return
     * @throws CommonException
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = CommonException.class)
    public TxResultResponse addImmediateCar(OrderInceParam orderInceParam) throws CommonException {
        LOGGER.info("进入车场储物柜业务逻辑，类addImmediateCar ，参数 orderInceParam={}", orderInceParam);
        TxResultResponse tx = new TxResultResponse(CommonCode.SUCCESS.getCode(), "订单新增成功");
        JSONObject object = new JSONObject();
        JSONObject insOrderObject = null;
        List<Map> returnList = new ArrayList<Map>();
        String message = "";
        try {
            //获取member信息
            Member member = memberService.selByOpenId(orderInceParam.getOpenId());
            //获取WarehouseCell信息
            Map<String, Object> dispensertWarehouseCell = dispensertWarehouseCellService.getDispensertWarehouseCell(orderInceParam.getWahoceId());
            //warehouseCell实体类
            WarehouseCell warehouseCell = (WarehouseCell) dispensertWarehouseCell.get("warehouseCell");
            //获取Warehouse信息
            Map<String, Object> dispensertWarehouseInfo = dispensertWarehouseService.getDispensertWarehouseInfo(orderInceParam.getWahoId());
            Warehouse warehouse = (Warehouse) dispensertWarehouseInfo.get("Warehouse");
            if ("1".equals(warehouse.getStatus())) {
                //返回门店已停用
                LOGGER.warn("接口异常,code={},error={}", CommonCode.ERROR.getCode(), "门店已经停用");
                throw new CommonException(CommonCode.ERROR.getCode(), "门店已经停用");
            } else {
                if ("0".equals(warehouseCell.getStatus())) {
                    //查询该柜体下还有无有效订单  1 :存在有效订单  0：无有效订单
                    int index = orderService.selOrderByWahoce(orderInceParam.getWahoceId());
                    if (index == 1) {
                        //存在有效订单 则占用该柜体  状态：0：空闲 1：已使用 2停用，3:可预定，4：预约
                        Map<String, Object> stringObjectMap = dispensertWarehouseCellService.updateWarehouseCellStatus(orderInceParam.getWahoceId(), "1");
                        int result = (int) stringObjectMap.get("result");
                        if (result > 0) {
                            LOGGER.info("下单流程失败，因柜体下有有效订单，故占用柜体成功,result={}", result);
                            throw new CommonException(CommonCode.DATA_NOT_FOUND.getCode(), "下单流程失败，因柜体下有有效订单，故占用柜体成功");
                        } else {
                            LOGGER.info("下单流程失败，因柜体下有有效订单，故占用柜体成功,result={}", result);
                            throw new CommonException(CommonCode.DATA_NOT_FOUND.getCode(), "修改门柜状态 ， 占用失败");
                        }
                    } else if (index == 0) {

                        //无有效订单，流程继续 修改订单状态为待支付  状态：0：空闲 1：已使用 2停用，3:可预定，4：预约
                        Map<String, Object> stringObjectMap = dispensertWarehouseCellService.updateWarehouseCellStatus(orderInceParam.getWahoceId(), "4");
                        int i = (int) stringObjectMap.get("result");
                        if (i < 1) {
                            throw new CommonException(CommonCode.DATA_NOT_FOUND.getCode(), "修改门柜状态 修改门柜状态");
                        }
                        LOGGER.info("下单流程继续。。。。新增订单");
                        Order order = new Order();
                        order.setMemberId(member.getId());
                        //增加已发生租金
                        order.setAmount("0");

                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Calendar calendar = Calendar.getInstance();
                        long time = System.currentTimeMillis();
                        calendar.setTimeInMillis(time / 1000 / 60 * 60 * 1000);
                        //添加订单开始时间
                        order.setStartDate(String.valueOf(dateFormat.format(calendar.getTime())));
                        calendar.setTimeInMillis(System.currentTimeMillis() / 1000 / 60 * 60 * 1000 + Long.valueOf(orderInceParam.getRentalTime()) * 3600 * 1000);
                        //添加订单结束时间
                        order.setEndDate(String.valueOf(String.valueOf(dateFormat.format(calendar.getTime()))));
                        order.setExeAccount(orderInceParam.getTotals());
                        order.setTotalAmount(orderInceParam.getTotals());
                        order.setWahoceId(orderInceParam.getWahoceId());
                        order.setCustName(orderInceParam.getMemberName());
                        order.setCustTel(orderInceParam.getMemberTel());
                        //历史订单删除项（是否显示） ssh 0310
                        order.setIsDisplay("0");
                        //订单状态：0 待付款(待支付) 1 已生效（已支付） 2 已过期 3 作废 4退单（已支付后退单的）5待审核
                        order.setStatus("0");
                        //订单类型：1续仓，0新增
                        order.setType("0");
                        SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                        String createDate = dateFormat1.format(new Date());
                        order.setCreateDate(createDate);
                        //订单的支付方式：1现金;2 汇款 3在线支付
                        order.setPayType("3");
                        //订单来源,0:微信端,1:pc端,2APP
                        order.setSource("0");
                        //订单租金支付方式：0:月付 1:季付 2:年付 3:一次性付清
                        order.setAmountType("3");
                        //订单按租金支付方式的每次应付的金额
                        order.setTypeMoeny(orderInceParam.getTotals());
                        //押金状态：0：未退还，1：已退还
                        order.setDepositStatus("0");
                        //续仓的订单的id 初始值:-1 表示此订单没有续仓
                        order.setNextOrder("-1");
                        //租用时长
                        order.setNumber(orderInceParam.getNumber());
                        //租用单位
                        order.setUnit(orderInceParam.getUnit());
                        order.setDeposit1(orderInceParam.getDeposit());
                        //添加密码 车场储物柜独有
                        order.setORDER_PASSWORD(warehouseCell.getParkingPassword());
                        //add by fjc 20160926 begin reason:去掉押金
                        DecimalFormat df = new DecimalFormat("######0.00");
                        String totalAmountStr = df.format(Double.valueOf(orderInceParam.getTotals()));
                        order.setTotalAmount(totalAmountStr);
                        int incr = orderService.insertSelective(order);
                        if (incr > 0) {
                            HashMap<String, String> dataP = new HashMap<>(16);
                            dataP.put("orderId", order.getId());
                            dataP.put("wahoId", warehouse.getId());
                            dataP.put("wahoType", warehouse.getType());
                            dataP.put("appId", ZkPropertyUtil.get("corpId"));
                            tx.setData(dataP);
                            LOGGER.info("插库成功，下单成功。。。。新增订单，order={},memberId={},warehouseCell={}", order.getId(), member.getId(), warehouseCell.getId());
                            return tx;
                        } else {
                            LOGGER.info("插库失败，下单失败。。。memberId={},warehouseCell={}", member.getId(), warehouseCell.getId());
                            throw new CommonException(CommonCode.SQL_UPD_ERROR.getCode(), "修改门柜状态 ， 出现错误");
                        }
                    } else {
                        LOGGER.info("修改门柜状态 ， 出现错误,warehouseCell={}", warehouseCell.getId());
                        throw new CommonException(CommonCode.DATA_NOT_FOUND.getCode(), "修改门柜状态 ， 出现错误");
                    }

                } else {
                    LOGGER.info("修改门柜状态 ， 出现错误,warehouseCell={}", warehouseCell.getId());
                    throw new CommonException(CommonCode.DATA_NOT_FOUND.getCode(), "修改门柜状态 ， 出现错误");
                }
            }

        } catch (CommonException e) {
            LOGGER.warn("CommonException异常,code={},error={}", e.getCode(), e.getMessage());
            return new TxResultResponse(e.getCode(), e.getMsg());
        } catch (Exception e) {
            LOGGER.warn("服务器内部异常,code={},error={}", insOrderObject.getJSONObject("data").getString("id"), e.getMessage());
            return new TxResultResponse(CommonCode.SERVER_ERROR.getCode(), CommonCode.SERVER_ERROR.getMsg());
        }
    }

    /**
     * 车场储物柜2.0 支付后更新订单状态接口
     * @param orderId
     * @param transactionId
     * @param tradeOrderId
     * @return
     * @throws CommonException
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = CommonException.class)
    public TxResultResponse updateParkingOrderStatus(String orderId, String transactionId, String tradeOrderId) throws CommonException {
        TxResultResponse resultResponse = new TxResultResponse(CommonCode.SUCCESS.getCode(), "操作成功");
        try {
            //查询是否有缴费记录
            boolean flag = orderService.selRenewByAccNum(transactionId);
            if (!flag) {
                //根据orderId查询订单
                Order order = orderService.selByOrderId(orderId);
                //根据门柜id查询柜子信息
                Map<String, Object> warehouseCellMap = dispensertWarehouseCellService.getDispensertWarehouseCell(order.getWahoceId());
                //根据memberId查询用户信息
                Member member = memberService.selByMemberId(order.getMemberId());
                if (!warehouseCellMap.isEmpty()) {
                    WarehouseCell warehouseCell = (WarehouseCell) warehouseCellMap.get("warehouseCell");
                    //根据wahoId获取门店信息
                    Map<String, Object> warehouseInfoMap = dispensertWarehouseService.getDispensertWarehouseInfo(warehouseCell.getWahoId());
                    Warehouse warehouse = (Warehouse) warehouseInfoMap.get("Warehouse");
                    //更新柜子状态
                    dispensertWarehouseCellService.updateWarehouseCellStatus(warehouseCell.getId(), "1");
                    //如果订单id不为空
                    if (StringUtils.isNotBlank(order.getId())) {
                        //这是续租
                        if (StringUtils.isNotBlank(order.getNextEndDate()) && StringUtils.isNotBlank(order.getNextAmount())) {
                            LOGGER.info("----车场储物柜--再次缴费----");
                            //柜子收费时间 小时 天 月
                            String unit = order.getUnit();
                            String startDate = order.getEndDate().substring(0, 16);
                            String endDate = order.getNextEndDate().substring(0, 16);
                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                            String totalTimes = "";
                            if ("1".equals(unit)) {
                                Date beginTime = format.parse(order.getStartDate().substring(0, 16));
                                Date endTime = format.parse(endDate);
                                long diff = endTime.getTime() - beginTime.getTime();
                                long hours = diff / 3600000L;
                                totalTimes = hours + "小时";
                            }
                            if ("2".equals(unit)) {
                                Date beginTime = format.parse(order.getStartDate().substring(0, 16));
                                Date endTime = format.parse(endDate);
                                long diff = endTime.getTime() - beginTime.getTime();
                                long days = diff / 86400000L;
                                totalTimes = days + "天";
                            }
                            if ("3".equals(unit)) {
                                String start = order.getStartDate().substring(0, 16);
                                Calendar beginMonth = Calendar.getInstance();
                                Calendar endMonth = Calendar.getInstance();
                                beginMonth.setTime(format.parse(start));
                                endMonth.setTime(format.parse(endDate));
                                int result = endMonth.get(2) - beginMonth.get(2);
                                int month = (endMonth.get(1) - beginMonth.get(1)) * 12;
                                totalTimes = Math.abs(month + result) + "个月";
                            }
                            OrderRenew orderRenew = new OrderRenew();
                            orderRenew.setAccountNumber(transactionId);
                            orderRenew.setEndDate(order.getNextEndDate());
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            orderRenew.setStartDate(sdf.format(new Date()));
                            orderRenew.setDate(sdf.format(new Date()));
                            orderRenew.setMoney(order.getNextAmount());
                            orderRenew.setStatus("1");
                            orderRenew.setOrderId(orderId);
                            orderRenew.setPaymentType("2");
                            orderRenew.setTradeOrderId(tradeOrderId);
                            //插入缴费记录
                            int isSuccess = orderService.insOrderRenew(orderRenew);
                            if (isSuccess == 1) {
                                Order updateOrder = new Order();
                                updateOrder.setId(orderId);
                                updateOrder.setEndDate(order.getNextEndDate());
                                updateOrder.setNextEndDate("");
                                Double newTotal = Double.valueOf(order.getTotalAmount()) + Double.valueOf(order.getNextAmount());
                                DecimalFormat df = new DecimalFormat("######0.00");
                                updateOrder.setTotalAmount(df.format(newTotal));
                                updateOrder.setNumber(df.format(newTotal));
                                updateOrder.setNextAmount("");
                                //更新订单信息
                                int updOrderSuccess = orderService.updOrderById(updateOrder);
                                if (updOrderSuccess == 1) {
                                    Map<String, String> returnMap = new HashMap<>(16);
                                    returnMap.put("msgFlag", "renew");
                                    returnMap.put("openId", member.getOpenId());
                                    returnMap.put("wahoName", warehouse.getName());
                                    returnMap.put("wahoceNum", warehouseCell.getNumber());
                                    returnMap.put("orderStartTime", startDate + "到" + endDate);
                                    returnMap.put("orderTime", totalTimes);
                                    returnMap.put("address", warehouse.getAddr());
                                    resultResponse.setData(returnMap);
                                    return resultResponse;
                                } else {
                                    LOGGER.error(orderId + "----->更新订单信息失败--再次缴费");
                                }
                            } else {
                                LOGGER.error(orderId + "----->插入缴费记录失败--再次缴费");
                            }
                        } else {
                            LOGGER.info("----车场储物柜--首次缴费----");
                            Order updateOrder = new Order();
                            updateOrder.setId(orderId);
                            updateOrder.setStatus("1");
                            updateOrder.setAmount(order.getTotalAmount());
                            //更新订单信息
                            int updOrderSuccess = orderService.updOrderById(updateOrder);
                            if (updOrderSuccess == 1) {
                                //插入缴费记录
                                OrderRenew orderRenew = new OrderRenew();
                                orderRenew.setAccountNumber(transactionId);
                                orderRenew.setOrderId(orderId);
                                orderRenew.setStartDate(order.getStartDate());
                                orderRenew.setEndDate(order.getEndDate());
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                                orderRenew.setDate(sdf.format(new Date()));
                                orderRenew.setMoney(order.getTotalAmount());
                                orderRenew.setStatus("1");
                                orderRenew.setPaymentType("2");
                                orderRenew.setTradeOrderId(tradeOrderId);
                                //插入缴费记录
                                int insOrderRenewSuccess = orderService.insOrderRenew(orderRenew);
                                if (insOrderRenewSuccess == 1) {
                                    String totalTimes = "";
                                    switch (order.getUnit()) {
                                        case "1":
                                            totalTimes = order.getNumber() + "小时";
                                            break;
                                        case "2":
                                            totalTimes = order.getNumber() + "天";
                                            break;
                                        case "3":
                                            totalTimes = order.getNumber() + "个月";
                                            break;
                                        default:
                                            totalTimes = order.getNumber() + "";
                                            break;
                                    }
                                    Map<String, String> returnMap = new HashMap<>(16);
                                    returnMap.put("msgFlag", "buy");
                                    returnMap.put("openId", member.getOpenId());
                                    returnMap.put("wahoName", warehouse.getName());
                                    returnMap.put("wahoceNum", warehouseCell.getNumber());
                                    returnMap.put("orderStartTime", order.getStartDate().substring(0, 16) + "到" + order.getEndDate().substring(0, 16));
                                    returnMap.put("orderTime", totalTimes);
                                    resultResponse.setData(returnMap);
                                    return resultResponse;
                                } else {
                                    LOGGER.error(orderId + "----->插入缴费记录失败--首次缴费");
                                }
                            } else {
                                LOGGER.error(orderId + "----->更新订单信息失败--首次缴费");
                            }
                        }
                    } else {
                        LOGGER.error(orderId + "----->orderId为空");
                        throw new CommonException(CommonCode.PARAM_ERROR.getCode(), orderId + "----->orderId为空");
                    }
                } else {
                    LOGGER.error(order.getWahoceId() + "---->warehouseCell查询错误");
                    throw new CommonException(CommonCode.SQL_SELECT_ERROR.getCode(), orderId + "----->orderId为空");
                }
            } else {
                LOGGER.error("--------updateOrderStatus----------查询orderRenew--有订单-----------------");
            }
        } catch (CommonException e) {
            return new TxResultResponse(e.getCode(), e.getMsg());
        } catch (Exception e) {
            e.printStackTrace();
            throw new CommonException(CommonCode.SERVER_ERROR.getCode(), CommonCode.SERVER_ERROR.getMsg());
        }
        return null;
    }
}
