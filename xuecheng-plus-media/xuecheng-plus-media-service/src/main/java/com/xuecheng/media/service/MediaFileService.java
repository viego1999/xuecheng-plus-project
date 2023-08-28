package com.xuecheng.media.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;

import java.io.File;

/**
 * 媒资文件管理业务类
 *
 * @author Mr.M
 * @version 1.0
 * @since 2022/9/10 8:55
 */
public interface MediaFileService {

    /**
     * 媒资文件查询方法
     *
     * @param pageParams          分页参数
     * @param queryMediaParamsDto 查询条件
     * @return {@link com.xuecheng.base.model.PageResult}<{@link com.xuecheng.media.model.po.MediaFiles}>
     * @author Mr.M
     * @since 2022/9/10 8:57
     */
    PageResult<MediaFiles> queryMediaFiles(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);

    /**
     * 上传文件
     *
     * @param companyId  机构id
     * @param params     上传文件信息
     * @param bytes      数据字节流
     * @param folder     文件夹，如果不传则默认年、月、日
     * @param objectName 文件名
     * @return 上传结果 {@link UploadFileResultDto}
     */
    UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto params, byte[] bytes, String folder, String objectName);

    /**
     * 将文件信息存入数据库中
     *
     * @param companyId  机构 id
     * @param fileId     文件id（md5）
     * @param params     文件参数
     * @param bucket     桶
     * @param objectName 对象名称
     * @return 上传结果
     */
    MediaFiles addMediaFilesToDb(Long companyId, String fileId, UploadFileParamsDto params, String bucket, String objectName);

    /**
     * 根据绝对路径将文件上传到 minio
     *
     * @param filepath   文件绝对路径
     * @param bucket     桶
     * @param objectName 对象名
     */
    void addMediaFilesToMinio(String filepath, String bucket, String objectName);

    /**
     * 检查文件是否存在
     *
     * @param fileMd5 文件的 md5
     * @return {@link com.xuecheng.base.model.RestResponse}<{@link java.lang.Boolean}> false 不存在，true 存在
     * @author Mr.M
     * @since 2022/9/13 15:38
     */
    RestResponse<Boolean> checkFile(String fileMd5);

    /**
     * 检查分块是否存在
     *
     * @param fileMd5    文件的 md5
     * @param chunkIndex 分块序号
     * @return {@link com.xuecheng.base.model.RestResponse}<{@link java.lang.Boolean}> false 不存在，true 存在
     * @author Mr.M
     * @since 2022/9/13 15:39
     */
    RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex);

    /**
     * 上传分块
     *
     * @param fileMd5 文件 md5
     * @param chunk   分块序号
     * @param bytes   文件字节
     * @return {@link com.xuecheng.base.model.RestResponse}
     * @author Mr.M
     * @since 2022/9/13 15:50
     */
    RestResponse<Boolean> uploadChunk(String fileMd5, int chunk, byte[] bytes);

    /**
     * 合并分块
     *
     * @param companyId           机构 id
     * @param fileMd5             文件 md5
     * @param chunkTotal          分块总和
     * @param uploadFileParamsDto 文件信息
     * @return {@link com.xuecheng.base.model.RestResponse}
     * @author Mr.M
     * @since 2022/9/13 15:56
     */
    RestResponse<Boolean> mergeChunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto);

    /**
     * 根据桶和文件路径从 minio 下载文件
     *
     * @param file       文件
     * @param bucket     桶
     * @param objectName 对象名
     * @return 文件
     */
    File downloadFileFromMinio(File file, String bucket, String objectName);

    /**
     * 根据 id 查询文件信息
     *
     * @param id 文件 id
     * @return {@link com.xuecheng.media.model.po.MediaFiles} 文件信息
     * @author Mr.M
     * @since 2022/9/13 17:47
     */
    MediaFiles getFileById(String id);

}
