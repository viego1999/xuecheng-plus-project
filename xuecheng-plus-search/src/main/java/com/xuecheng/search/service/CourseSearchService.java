package com.xuecheng.search.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.search.dto.SearchCourseParamDto;
import com.xuecheng.search.dto.SearchPageResultDto;
import com.xuecheng.search.po.CourseIndex;

/**
 * 课程搜索service
 *
 * @author Wuxy
 * @version 1.0
 * @since 2022/9/24 22:40
 */
public interface CourseSearchService {


    /**
     * 搜索课程列表
     *
     * @param pageParams           分页参数
     * @param searchCourseParamDto 搜索条件
     * @return {@link com.xuecheng.base.model.PageResult}<{@link com.xuecheng.search.po.CourseIndex}> 课程列表
     * @author Wuxy
     * @since 2022/9/24 22:45
     */
    SearchPageResultDto<CourseIndex> queryCoursePubIndex(PageParams pageParams, SearchCourseParamDto searchCourseParamDto);

}
