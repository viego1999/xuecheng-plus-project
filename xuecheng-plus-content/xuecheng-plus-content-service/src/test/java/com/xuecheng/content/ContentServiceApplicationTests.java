package com.xuecheng.content;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.service.CourseBaseInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ContentServiceApplicationTests {

	@Autowired
	private CourseBaseMapper courseBaseMapper;

	@Autowired
	private CourseBaseInfoService courseBaseInfoService;

	@Test
	void testCourseBaseMapper() {
		System.out.println(courseBaseMapper.selectById(22));
	}

	@Test
	void testCourseBaseInfoService() {
		System.out.println(courseBaseInfoService.queryCourseBaseList(new PageParams(), new QueryCourseParamsDto()));
	}

}
