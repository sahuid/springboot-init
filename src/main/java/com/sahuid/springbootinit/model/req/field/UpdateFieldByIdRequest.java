package com.sahuid.springbootinit.model.req.field;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.sahuid.springbootinit.model.entity.Locational;
import lombok.Data;

import java.util.List;

/**
 * @Author: wxb
 * @Description: TODO
 * @DateTime: 2025/3/17 10:37
 **/
@Data
public class UpdateFieldByIdRequest {

    /**
     * 主键id
     */
    private Long id;

    /**
     * 地块编号
     */
    private String fieldId;

    /**
     * 灌溉单元编号
     */
    private String fieldUnitId;

    /**
     * 地理位置
     */
    private List<Locational> fieldRange;
}
