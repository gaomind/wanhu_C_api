package com.whc.c.model.order;


import com.whc.c.model.base.BaseDO;

/**
 * 添加订单所需参数
 * @author ssh
 * Created by Administrator on 2018/2/5.
 */
public class OrderInceParam extends BaseDO {
    private String unit;
    private String number;
    private String totals;
    private String memberName;
    private String memberTel;
    private String openId;
    private String wahoceId;
    private String wahoId;
    private String wahoType;
    private String rentalTime;
    private String deposit;

    public String getDeposit() {
        return deposit;
    }

    public void setDeposit(String deposit) {
        this.deposit = deposit;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getTotals() {
        return totals;
    }

    public void setTotals(String totals) {
        this.totals = totals;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getMemberTel() {
        return memberTel;
    }

    public void setMemberTel(String memberTel) {
        this.memberTel = memberTel;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public String getWahoceId() {
        return wahoceId;
    }

    public void setWahoceId(String wahoceId) {
        this.wahoceId = wahoceId;
    }

    public String getWahoId() {
        return wahoId;
    }

    public void setWahoId(String wahoId) {
        this.wahoId = wahoId;
    }

    public String getWahoType() {
        return wahoType;
    }

    public void setWahoType(String wahoType) {
        this.wahoType = wahoType;
    }

    public String getRentalTime() {
        return rentalTime;
    }

    public void setRentalTime(String rentalTime) {
        this.rentalTime = rentalTime;
    }
}
