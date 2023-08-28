package com.xuecheng;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CourseCategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class ContentServiceApplicationTests {

    @Resource
    private CourseBaseMapper courseBaseMapper;
    @Autowired
    private CourseBaseInfoService courseBaseInfoService;
    @Autowired
    private CourseCategoryService courseCategoryService;

    @Test
    void testCourseBaseMapper() {
        System.out.println(courseBaseMapper.selectById(22));
    }

    @Test
    void testCourseBaseInfoService() {
        Long companyId = 1232141425L;
        System.out.println(courseBaseInfoService.queryCourseBaseList(companyId, new PageParams(), new QueryCourseParamsDto()));
    }

    @Test
    void testCourseCategoryService() {
        System.out.println(courseCategoryService.queryTreeNodes("1"));
    }

}
