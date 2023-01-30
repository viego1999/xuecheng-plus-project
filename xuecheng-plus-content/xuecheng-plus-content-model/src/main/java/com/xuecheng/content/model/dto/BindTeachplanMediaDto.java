package com.xuecheng.content.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 教学计划-媒资绑定提交数据
 *
 * @author Wuxy
 * @version 1.0
 * @ClassName BindTeachplanMediaDto
 * @since 2023/1/27 23:02
 */
@Data
@ApiModel(value = "BindTeachplanMediaDto", description = "教学计划-媒资绑定提交数据")
public class BindTeachplanMediaDto {

    /**
     * 媒资文件 id
     */
    @ApiModelProperty(value = "媒资文件 id", required = true)
    private String mediaId;

    /**
     * 媒资文件名称
     */
    @ApiModelProperty(value = "媒资文件名称", required = true)
    private String fileName;

    /**
     * 课程计划 id
     */
    @ApiModelProperty(value = "课程计划标识", required = true)
    private Long teachplanId;

}
