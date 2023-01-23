package com.xuecheng.media.model.dto;

import lombok.Data;
import lombok.ToString;

/**
 * 上传普通文件请求参数
 *
 * @author Mr.M
 * @version 1.0
 * @since 2022/9/12 18:49
 */
@Data
@ToString
public class UploadFileParamsDto {

    /**
     * 文件名称
     */
    private String filename;

    /**
     * 文件content-type
     */
    private String contentType;

    /**
     * 文件类型（文档，音频，视频）
     */
    private String fileType;
    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 标签
     */
    private String tags;

    /**
     * 上传人
     */
    private String username;

    /**
     * 备注
     */
    private String remark;

}
