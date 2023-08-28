package com.xuecheng.learning.service;

import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.learning.model.dto.MyCourseTableItemDto;
import com.xuecheng.learning.model.dto.MyCourseTableParams;
import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.model.po.XcChooseCourse;
import com.xuecheng.learning.model.po.XcCourseTables;

/**
 * 我的课程表service接口
 *
 * @author Wuxy
 * @version 1.0
 * @since 2022/10/2 16:07
 */
public interface MyCourseTablesService {

    /**
     * 添加选课
     *
     * @param userId   用户 id
     * @param courseId 课程 id
     * @return {@link com.xuecheng.learning.model.dto.XcChooseCourseDto}
     * @author Wuxy
     * @since 2022/10/24 17:33
     */
    XcChooseCourseDto addChooseCourse(String userId, Long courseId);

    /**
     * 添加免费课程
     *
     * @param userId        用户 id
     * @param coursePublish 课程发布信息
     * @return 选课信息
     */
    XcChooseCourse addFreeCourse(String userId, CoursePublish coursePublish);

    /**
     * 添加收费课程
     *
     * @param userId        用户 id
     * @param coursePublish 课程发布信息
     * @return 选课信息
     */
    XcChooseCourse addChargeCourse(String userId, CoursePublish coursePublish);

    /**
     * 添加到我的课程表
     *
     * @param chooseCourse 选课记录
     * @return {@link com.xuecheng.learning.model.po.XcCourseTables}
     * @author Wuxy
     * @since 2022/10/3 11:24
     */
    XcCourseTables addCourseTables(XcChooseCourse chooseCourse);

    /**
     * 根据课程和用户查询我的课程表中某一门课程
     *
     * @param userId   用户 id
     * @param courseId 课程 id
     * @return {@link com.xuecheng.learning.model.po.XcCourseTables}
     * @author Wuxy
     * @since 2022/10/2 17:07
     */
    XcCourseTables getXcCourseTables(String userId, Long courseId);

    /**
     * 判断学习资格
     * <pre>
     * 学习资格状态 [{"code":"702001","desc":"正常学习"},
     *            {"code":"702002","desc":"没有选课或选课后没有支付"},
     *            {"code":"702003","desc":"已过期需要申请续期或重新支付"}]
     * </pre>
     *
     * @param userId   用户 id
     * @param courseId 课程 id
     * @return {@link XcCourseTablesDto}
     * @author Wuxy
     * @since 2022/10/3 7:37
     */
    XcCourseTablesDto getLearningStatus(String userId, Long courseId);

    boolean saveChooseCourseStatus(String chooseCourseId);

    PageResult<MyCourseTableItemDto> myCourseTables(MyCourseTableParams params);

}
