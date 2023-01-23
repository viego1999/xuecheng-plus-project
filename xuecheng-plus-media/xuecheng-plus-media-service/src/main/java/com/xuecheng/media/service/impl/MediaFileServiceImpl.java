package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * 媒体文件服务类
 *
 * @author Mr.M
 * @version 1.0
 * @since 2022/9/10 8:58
 */
@Slf4j
@Service
public class MediaFileServiceImpl implements MediaFileService {

    @Resource
    private MediaFilesMapper mediaFilesMapper;

    @Autowired
    private MinioClient minioClient;

    /**
     * 普通文件存储的桶
     */
    @Value("${minio.bucket.files}")
    private String bucketFiles;

    /**
     * 视频文件存储的桶
     */
    @Value("${minio.bucket.videofiles}")
    private String bucketVideoFiles;

    @Autowired
    private MediaFileService currentProxy;

    @Override
    public PageResult<MediaFiles> queryMediaFiles(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {
        // 构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();
        // 查询条件
        if (queryMediaParamsDto.getFileType() != null) {
            queryWrapper.eq(MediaFiles::getFileType, queryMediaParamsDto.getFileType());
        }
        if (queryMediaParamsDto.getFilename() != null) {
            queryWrapper.like(MediaFiles::getFilename, "%" + queryMediaParamsDto.getFilename() + "%");
        }
        if (queryMediaParamsDto.getAuditStatus() != null) {
            queryWrapper.eq(MediaFiles::getAuditStatus, queryMediaParamsDto.getAuditStatus());
        }
        // 分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        return new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
    }

    @Override
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto params, byte[] bytes, String folder, String objectName) {
        if (StringUtils.isEmpty(folder)) {
            // 自动生成目录的路径：按年月日生成
            folder = getFileFolder(new Date(), true, true, true);
        } else if (!folder.contains("/")) {
            folder += "/";
        }
        // 生成文件 id，文件的 md5 值
        String fileId = DigestUtils.md5Hex(bytes);
        // 得到文件名
        String filename = params.getFilename();
        // 构造 objectName
        if (StringUtils.isEmpty(objectName)) {
            objectName = fileId + filename.substring(filename.lastIndexOf("."));
        }
        // 对象名称
        objectName = folder + objectName;
        try {
            addMediaFilesToMinio(bytes, bucketFiles, objectName);
            return currentProxy.addMediaFilesToDb(companyId, fileId, params, bucketFiles, objectName);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new XueChengPlusException("上传过程中出错");
        }
    }

    @Transactional
    public UploadFileResultDto addMediaFilesToDb(Long companyId, String fileId, UploadFileParamsDto params, String bucket, String objectName) {
        // 从数据库查询文件
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
        UploadFileResultDto resultDto = null;
        if (mediaFiles == null) {
            mediaFiles = new MediaFiles();
            // 拷贝基本信息
            BeanUtils.copyProperties(params, mediaFiles);
            mediaFiles.setId(fileId);
            mediaFiles.setFileId(fileId);
            mediaFiles.setCompanyId(companyId);
            mediaFiles.setUrl("/" + bucket + "/" + objectName);
            mediaFiles.setBucket(bucket);
            mediaFiles.setCreateDate(LocalDateTime.now());
            mediaFiles.setAuditStatus("002003");
            mediaFiles.setStatus("1");
            // 上传到数据库
            int insert = mediaFilesMapper.insert(mediaFiles);
            if (insert <= 0) {
                XueChengPlusException.cast("文件信息保存失败");
            }
            resultDto = new UploadFileResultDto();
            BeanUtils.copyProperties(mediaFiles, resultDto);
        }
        return resultDto;
    }

    @Override
    public RestResponse<Boolean> checkFile(String fileMd5) {
        // 在文件表中存在，并且在文件系统中存在，此文件才存在
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles == null) {
            return RestResponse.success(false);
        }
        // 查询文件系统中是否存在
        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket(mediaFiles.getBucket())
                .object(mediaFiles.getFilePath())
                .build();
        try {
            InputStream inputStream = minioClient.getObject(getObjectArgs);
            if (inputStream == null) {
                // 文件不存在
                return RestResponse.success(false);
            }
        } catch (Exception e) {
            // 文件不存在
            return RestResponse.success(false);
        }
        // 文件已存在
        return RestResponse.success(true);
    }

    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {
        // 分块文件所在目录
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        // 分块文件的路径
        String chunkFilePath = chunkFileFolderPath + chunkIndex;
        // 查询文件系统分块文件是否存在
        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket(bucketVideoFiles)
                .object(chunkFilePath)
                .build();
        try {
            InputStream inputStream = minioClient.getObject(getObjectArgs);
            if (inputStream == null) {
                // 文件不存在
                return RestResponse.success(false);
            }
        } catch (Exception e) {
            // 文件不存在
            return RestResponse.success(false);
        }
        // 文件存在
        return RestResponse.success(true);
    }

    @Override
    public RestResponse<Boolean> uploadChunk(String fileMd5, int chunk, byte[] bytes) {
        // 得到分块文件的目录路径
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        // 得到分块文件的路径
        String chunkFilePath = chunkFileFolderPath + chunk;
        try {
            // 将文件存储至 minIO
            addMediaFilesToMinio(bytes, bucketVideoFiles, chunkFilePath);
            return RestResponse.success(true);
        } catch (Exception e) {
            log.error("上传分块文件:{},失 败。", chunkFilePath, e);
        }
        return RestResponse.validfail(false, "上传分块失败");
    }

    @Override
    public RestResponse<Boolean> mergechunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {
        return null;
    }

    private File[] checkChunkStatus(String fileMd5, int chunkTotal) {
        // 得到分块文件的目录路径
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);

        return null;
    }

    // 得到分块文件的目录
    private String getChunkFileFolderPath(String fileMd5) {
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" + "chunk" + "/";
    }

    /**
     * 将文件上传到分布式文件系统
     *
     * @param bytes      数据字节流
     * @param bucket     桶
     * @param objectName 对象名称（文件名）
     */
    private void addMediaFilesToMinio(byte[] bytes, String bucket, String objectName) {
        try {
            // 获取 contentType
            String contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE; // 未知的二进制流
            if (objectName.contains(".")) {
                // 取 objectName 中的扩展名
                String extension = objectName.substring(objectName.lastIndexOf("."));
                ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
                if (extensionMatch != null) {
                    contentType = extensionMatch.getMimeType();
                }
            }
            ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
            // 上传到 minio
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .stream(inputStream, inputStream.available(), -1) // -1 表示文件分片按 5M(不小于 5M,不大于 5T),分片数量最大 10000
                    .contentType(contentType)
                    .build();
            minioClient.putObject(putObjectArgs);
        } catch (Exception e) {
            log.error("上传文件到文件系统出错。", e);
            throw new XueChengPlusException("上传文件到文件系统出错");
        }
    }

    // 根据日期拼接目录
    @SuppressWarnings("all")
    private String getFileFolder(Date date, boolean year, boolean month, boolean day) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        // 获取当前日期字符串
        String dateString = sdf.format(date);
        // 取出年、月、日
        String[] dateStringArray = dateString.split("-");
        StringBuilder folderString = new StringBuilder();
        if (year) {
            folderString.append(dateStringArray[0]).append("/");
        }
        if (month) {
            folderString.append(dateStringArray[1]).append("/");
        }
        if (day) {
            folderString.append(dateStringArray[2]).append("/");
        }
        return folderString.toString();
    }

}
