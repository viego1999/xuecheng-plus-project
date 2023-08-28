package com.xuecheng.learning.model.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 选课表
 * </p>
 *
 * @author itcast
 */
@Data
@TableName("xc_choose_course")
public class XcChooseCourse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 课程id
     */
    private Long courseId;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 机构id
     */
    private Long companyId;

    /**
     * 选课类型 <p>
     * [{"code":"700001","desc":"免费课程"},{"code":"700002","desc":"收费课程"}]
     */
    private String orderType;

    /**
     * 添加时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createDate;

    /**
     * 课程有效期(天)
     */
    private Integer validDays;

    /**
     * 课程价格
     */
    private Float coursePrice;

    /**
     * 选课状态 <p>
     * [{"code":"701001","desc":"选课成功"},{"code":"701002","desc":"待支付"}]
     */
    private String status;

    /**
     * 开始服务时间
     */
    private LocalDateTime validtimeStart;

    /**
     * 结束服务时间
     */
    private LocalDateTime validtimeEnd;

    /**
     * 备注
     */
    private String remarks;

}
