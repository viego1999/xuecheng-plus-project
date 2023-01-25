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
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.UploadObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.*;
import java.nio.file.Files;
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

    @Resource
    private MediaProcessMapper mediaProcessMapper;

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
            MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(companyId, fileId, params, bucketFiles, objectName);
            UploadFileResultDto resultDto = new UploadFileResultDto();
            BeanUtils.copyProperties(mediaFiles, resultDto);
            return resultDto;
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new XueChengPlusException("上传过程中出错");
        }
    }

    @Transactional
    @Override
    public MediaFiles addMediaFilesToDb(Long companyId, String fileId, UploadFileParamsDto params, String bucket, String objectName) {
        // 根据文件名称取出媒体类型
        // 扩展名
        String extension = null;
        if (objectName.contains(".")) {
            extension = objectName.substring(objectName.lastIndexOf("."));
        }
        // 获取扩展名对应的媒体类型
        String contentType = getMimeTypeByExtension(extension);
        // 从数据库查询文件
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
        if (mediaFiles == null) {
            mediaFiles = new MediaFiles();
            // 拷贝基本信息
            BeanUtils.copyProperties(params, mediaFiles);
            mediaFiles.setId(fileId);
            mediaFiles.setFileId(fileId);
            mediaFiles.setCompanyId(companyId);
            mediaFiles.setFilePath(objectName);
            // 图片、mp4视频可以直接设置url
            if (contentType.contains("image") || contentType.contains("mp4")) {
                mediaFiles.setUrl("/" + bucket + "/" + objectName);
            }
            mediaFiles.setBucket(bucket);
            mediaFiles.setCreateDate(LocalDateTime.now());
            mediaFiles.setAuditStatus("002003");
            mediaFiles.setStatus("1"); // 初始状态正常显示
            // 上传到数据库
            int insert = mediaFilesMapper.insert(mediaFiles);
            if (insert <= 0) {
                XueChengPlusException.cast("文件信息保存失败");
            }
            // 对 avi 视频添加到待处理任务表
            if (contentType.equals("video/x-msvideo")) {
                // 创建一个视频待处理任务
                MediaProcess mediaProcess = new MediaProcess();
                BeanUtils.copyProperties(mediaFiles, mediaProcess);
                // 设置一个状态
                mediaProcess.setStatus("1"); // 设置处理状态为未处理
                int count = mediaProcessMapper.insert(mediaProcess);
                if (count <= 0) {
                    XueChengPlusException.cast("视频待处理任务添加失败");
                }
            }
        }
        return mediaFiles;
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
    public RestResponse<Boolean> mergeChunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {
        String filename = uploadFileParamsDto.getFilename();
        // 下载所有分块文件
        File[] chunkFiles = checkChunkStatus(fileMd5, chunkTotal);
        // 文件扩展名
        String extension = filename.substring(filename.lastIndexOf("."));
        // 创建临时文件作为合并文件
        File mergeFile;
        try {
            mergeFile = File.createTempFile(fileMd5, extension);
        } catch (Exception e) {
            throw new XueChengPlusException("合并文件过程中创建临时文件出错");
        }
        // 开始合并
        try {
            byte[] buffer = new byte[1024];
            try (RandomAccessFile rafWriter = new RandomAccessFile(mergeFile, "rw")) {
                for (File chunkFile : chunkFiles) {
                    try (FileInputStream fis = new FileInputStream(chunkFile)) {
                        int len;
                        while ((len = fis.read(buffer)) != -1) {
                            // 向合并后的文件写入
                            rafWriter.write(buffer, 0, len);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                XueChengPlusException.cast("合并文件过程中出错");
            }
            log.debug("合并文件完成{}", mergeFile.getAbsolutePath());
            uploadFileParamsDto.setFileSize(mergeFile.length());
            // 校验文件内容，通过 md5 对比
            try (FileInputStream mergeFis = new FileInputStream(mergeFile)) {
                String md5Hex = DigestUtils.md5Hex(mergeFis);
                if (!fileMd5.equalsIgnoreCase(md5Hex)) {
                    // 校验失败
                    XueChengPlusException.cast("合并文件校验失败");
                }
                log.debug("合并文件校验通过 {}", mergeFile.getAbsolutePath());
            } catch (Exception e) { // 校验失败
                e.printStackTrace();
                XueChengPlusException.cast("合并文件校验异常");
            }

            // 将临时文件上传到 minio
            String mergeFilepath = getFilePathByMd5(fileMd5, extension);
            // 上传到 minio
            try {
                addMediaFilesToMinio(mergeFile.getAbsolutePath(), bucketVideoFiles, mergeFilepath);
                log.debug("合并文件上传 MinIO 完成 {}", mergeFile.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
                XueChengPlusException.cast("合并文件时上传文件出错");
            }
            // 上传到数据库
            MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucketVideoFiles, mergeFilepath);
            if (mediaFiles == null) {
                XueChengPlusException.cast("媒资文件入库出错");
            }
            return RestResponse.success();
        } finally {
            // 删除临时文件
            for (File chunkFile : chunkFiles) {
                try {
                    chunkFile.delete();
                } catch (Exception ignored) {
                }
            }
            try {
                mergeFile.delete();
            } catch (Exception ignored) {
            }
            log.debug("临时文件清理完毕。");
        }
    }

    @Override
    public MediaFiles getFileById(String id) {
        MediaFiles mediaFiles = mediaFilesMapper.selectById(id);
        if (mediaFiles == null) {
            XueChengPlusException.cast("文件不存在");
        }
        String url = mediaFiles.getUrl();
        if (StringUtils.isEmpty(url)) {
            XueChengPlusException.cast("文件还没有转码处理，请稍后预览");
        }
        return mediaFiles;
    }

    /**
     * 根据文件md5值获取文件绝对路径
     *
     * @param fileMd5 文件md5值
     * @param fileExt 文件扩展名
     * @return 文件绝对路径
     */
    private String getFilePathByMd5(String fileMd5, String fileExt) {
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" + fileMd5 + fileExt;
    }

    @Override
    public void addMediaFilesToMinio(String filepath, String bucket, String objectName) {
        // 扩展名
        String extension = null;
        if (filepath.contains(".")) {
            extension = filepath.substring(filepath.lastIndexOf("."));
        }
        // 获取扩展名对应的媒体类型
        String contentType = getMimeTypeByExtension(extension);
        try {
            minioClient.uploadObject(UploadObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .filename(filepath)
                    .contentType(contentType)
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
            XueChengPlusException.cast("上传文件到文件系统出错");
        }
    }

    /**
     * 根据扩展名得到对应的媒体类型
     *
     * @param extension 文件扩展名
     * @return 对应的媒体类型
     */
    public String getMimeTypeByExtension(String extension) {
        String contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        if (StringUtils.isNotEmpty(extension)) {
            ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
            if (extensionMatch != null) {
                contentType = extensionMatch.getMimeType();
            }
        }
        return contentType;
    }

    /**
     * 检查所有分块是否上传完毕并返回所有的分块文件
     *
     * @param fileMd5    文件 md5
     * @param chunkTotal 总共的分块数
     * @return 分块文件数组
     */
    private File[] checkChunkStatus(String fileMd5, int chunkTotal) {
        // 得到分块文件的目录路径
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        File[] files = new File[chunkTotal];
        // 检查分块文件是否上传完毕
        for (int i = 0; i < chunkTotal; i++) {
            String chunkFilePath = chunkFileFolderPath + i;
            // 下载文件
            File chunkFile;
            try {
                chunkFile = File.createTempFile("chunk" + i, null);
            } catch (IOException e) {
                throw new XueChengPlusException("下载分块时创建临时文件出错");
            }
            // 从minio下载文件
            files[i] = downloadFileFromMinio(chunkFile, bucketVideoFiles, chunkFilePath);
        }
        return files;
    }

    @Override
    public File downloadFileFromMinio(File file, String bucket, String objectName) {
        try (InputStream inputStream = minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucket)
                .object(objectName)
                .build())) {
            try (OutputStream outputStream = Files.newOutputStream(file.toPath())) {
                IOUtils.copy(inputStream, outputStream);
            } catch (Exception e) {
                XueChengPlusException.cast("下载文件" + objectName + "出错");
            }
        } catch (Exception e) {
            e.printStackTrace();
            XueChengPlusException.cast("文件不存在 " + objectName);
        }
        return file;
    }

    /**
     * 根据文件md5值得到分块文件的目录
     *
     * @param fileMd5 文件md5值
     * @return 分块路径
     */
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

    /**
     * 根据日期拼接目录
     *
     * @param date  日期
     * @param year  年
     * @param month 月
     * @param day   日
     * @return 返回日期拼接目录
     */
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
