package com.xuecheng.learning;

import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.learning.feignclient.ContentServiceClient;
import com.xuecheng.learning.model.dto.MyCourseTableItemDto;
import com.xuecheng.learning.model.dto.MyCourseTableParams;
import com.xuecheng.learning.service.MyCourseTablesService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/10/2 10:32
 */
@SpringBootTest
public class LearningApplicationTest {

    @Autowired
    ContentServiceClient contentServiceClient;

    @Autowired
    MyCourseTablesService myCourseTablesService;

    @Test
    public void test() {
        CoursePublish coursepublish = contentServiceClient.getCoursePublish(2L);
        System.out.println(coursepublish);
    }

    @Test
    public void test2() {
        MyCourseTableParams myCourseTableParams = new MyCourseTableParams();
        myCourseTableParams.setUserId("52");
        PageResult<MyCourseTableItemDto> myCourseTables = myCourseTablesService.myCourseTables(myCourseTableParams);
        System.out.println(myCourseTables);
    }

}
