package com.xuecheng.media.model.dto;

import com.xuecheng.media.model.po.MediaFiles;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 上传普通文件成功响应结果
 *
 * @author Wuxy
 * @version 1.0
 * @ClassName UploadFileResultDto
 * @since 2023/1/21 20:17
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UploadFileResultDto extends MediaFiles {
}
