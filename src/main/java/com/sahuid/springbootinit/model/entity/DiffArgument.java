package com.sahuid.springbootinit.model.entity;

import lombok.Data;

/**
 * @Author: wxb
 * @Description: TODO
 * @DateTime: 2025/4/22 11:41
 **/
@Data
public class DiffArgument {
    //需要钾肥量
    private double kNum;
    // 需要氮肥量
    private double nNum;
    // 需要磷肥量
    private double pNum;
    // 单位纯水补偿量
    private double waterCompensationNum;
    // 氮肥扣除时间
    private double nDeductionTime;
    // 钾肥扣除时间
    private double kDeductionTime;
    // 磷肥扣除时间
    private double pDeductionTime;
    // 纯水补偿的扣除时间
    private double waterCompensationDeductionTime;
    // 单位面积氮肥补偿量
    private double nFerCompensation;
    // 单位面积钾肥补偿量
    private double kFerCompensation;
    // 单位面积磷肥补偿量
    private double pFerCompensation;


}
