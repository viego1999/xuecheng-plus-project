package com.xuecheng.content.model.dto;

import lombok.Data;
import lombok.ToString;

/**
 * 课程查询参数Dto
 *
 * @author Wuxy
 * @version 1.0
 * @ClassName QueryCourseParamsDto
 * @since 2023/1/18 18:54
 */
@Data
@ToString
public class QueryCourseParamsDto {
    //审核状态
    private String auditStatus;
    //课程名称
    private String courseName;
    //发布状态
    private String publishStatus;
}
