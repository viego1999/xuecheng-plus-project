package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CoursePreviewDto;

import java.io.File;

/**
 * 课程预览、发布接口
 *
 * @author Wuxy
 * @version 1.0
 * @ClassName CoursePublishService
 * @since 2023/1/30 15:44
 */
public interface CoursePublishService {

    /**
     * 获取课程预览信息
     *
     * @param courseId 课程 id
     * @return {@link com.xuecheng.content.model.dto.CoursePreviewDto}
     * @author Wuxy
     * @since 2022/9/16 15:36
     */
    CoursePreviewDto getCoursePreviewInfo(Long courseId);

    /**
     * 提交审核
     *
     * @param courseId 课程 id
     * @author Wuxy
     * @since 2022/9/18 10:31
     */
    void commitAudit(Long companyId, Long courseId);

    /**
     * 课程发布接口
     *
     * @param companyId 机构 id
     * @param courseId  课程 id
     * @author Wuxy
     * @since 2022/9/20 16:23
     */
    void publish(Long companyId, Long courseId);

    /**
     * 课程静态化
     *
     * @param courseId 课程 id
     * @return {@link File} 静态化文件
     * @author Wuxy
     * @since 2022/9/23 16:59
     */
    File generateCourseHtml(Long courseId);

    /**
     * 上传课程静态化页面
     *
     * @param courseId 课程 id
     * @param file     静态化文件
     * @author Wuxy
     * @since 2022/9/23 16:59
     */
    void uploadCourseHtml(Long courseId, File file);

    /**
     * 新增课程索引
     *
     * @param courseId 课程id
     * @return 新增成功返回 true，否则 false
     */
    Boolean saveCourseIndex(Long courseId);

}
