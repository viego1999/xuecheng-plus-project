package com.xuecheng.content.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;

/**
 * 课程管理 Service
 *
 * @author Wuxy
 * @version 1.0
 * @ClassName CourseBaseInfoService
 * @since 2023/1/19 10:15
 */
public interface CourseBaseInfoService {

    /**
     * 课程查询
     *
     * @param params            分页参数
     * @param queryCourseParams 查询条件
     * @return 分页结果
     */
    PageResult<CourseBase> queryCourseBaseList(PageParams params, QueryCourseParamsDto queryCourseParams);

}
