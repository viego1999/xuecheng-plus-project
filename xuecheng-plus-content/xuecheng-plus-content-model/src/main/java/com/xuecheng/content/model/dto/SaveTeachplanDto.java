package com.xuecheng.content.model.dto;

import lombok.Data;
import lombok.ToString;

/**
 * 保存课程计划dto，包括新增、修改
 *
 * @author Wuxy
 * @version 1.0
 * @ClassName SaveTeachplanDto
 * @since 2023/1/20 12:01
 */
@Data
@ToString
public class SaveTeachplanDto {

    /***
     * 教学计划id
     */
    private Long id;

    /**
     * 课程计划名称
     */
    private String pname;

    /**
     * 课程计划父级Id
     */
    private Long parentid;

    /**
     * 层级，分为1、2、3级
     */
    private Integer grade;

    /**
     * 课程类型:1视频、2文档
     */
    private String mediaType;

    /**
     * 课程标识
     */
    private Long courseId;

    /**
     * 课程发布标识
     */
    private Long coursePubId;

    /**
     * 是否支持试学或预览（试看）
     */
    private String isPreview;

}
