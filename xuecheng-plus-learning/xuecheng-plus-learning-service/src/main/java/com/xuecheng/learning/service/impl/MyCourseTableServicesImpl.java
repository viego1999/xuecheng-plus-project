package com.xuecheng.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.CommonError;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.learning.feignclient.ContentServiceClient;
import com.xuecheng.learning.mapper.XcChooseCourseMapper;
import com.xuecheng.learning.mapper.XcCourseTablesMapper;
import com.xuecheng.learning.model.dto.MyCourseTableItemDto;
import com.xuecheng.learning.model.dto.MyCourseTableParams;
import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.model.po.XcChooseCourse;
import com.xuecheng.learning.model.po.XcCourseTables;
import com.xuecheng.learning.service.MyCourseTablesService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Wuxy
 * @version 1.0
 * @ClassName MyCourseTableServiceImpl
 * @since 2023/2/2 11:54
 */
@Service
public class MyCourseTableServicesImpl implements MyCourseTablesService {

    @Resource
    private XcChooseCourseMapper chooseCourseMapper;

    @Resource
    private XcCourseTablesMapper courseTablesMapper;

    @Autowired
    private ContentServiceClient contentServiceClient;

    @Autowired
    private MyCourseTablesService currentPoxy;


    @Override
    public XcChooseCourseDto addChooseCourse(String userId, Long courseId) {
        // 查询课程信息
        CoursePublish coursePublish = contentServiceClient.getCoursePublish(courseId);
        if (coursePublish == null) {
            XueChengPlusException.cast("课程信息不存在");
        }
        Long id = coursePublish.getId();
        if (id == null) {
            XueChengPlusException.cast(CommonError.UNKNOWN_ERROR);
        }
        // 课程收费标准
        String charge = coursePublish.getCharge();
        XcChooseCourse chooseCourse;
        if ("201000".equals(charge)) {
            // 添加免费课程到选课记录表 + 添加到我的课程表
            chooseCourse = currentPoxy.addFreeCourse(userId, coursePublish);
        } else {
            // 添加收费课程，只能添加到选课记录表
            chooseCourse = currentPoxy.addChargeCourse(userId, coursePublish);
        }

        XcChooseCourseDto chooseCourseDto = new XcChooseCourseDto();
        BeanUtils.copyProperties(chooseCourse, chooseCourseDto);
        // 获取用户对该课程的学习资格
        XcCourseTablesDto courseTablesDto = getLearningStatus(userId, courseId);
        chooseCourseDto.setLearnStatus(courseTablesDto.getLearnStatus());
        return chooseCourseDto;
    }

    @Transactional
    @Override
    public XcChooseCourse addFreeCourse(String userId, CoursePublish coursePublish) {
        // 查询选课记录表是否已经存在免费的且选课成功的订单
        LambdaQueryWrapper<XcChooseCourse> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(XcChooseCourse::getUserId, userId)
                .eq(XcChooseCourse::getCourseId, coursePublish.getId())
                .eq(XcChooseCourse::getOrderType, "700001") // 免费课程
                .eq(XcChooseCourse::getStatus, "701001"); // 选课成功
        List<XcChooseCourse> chooseCourses = chooseCourseMapper.selectList(queryWrapper);
        // 已经存在免费的且选课成功的订单直接返回
        if (chooseCourses != null && chooseCourses.size() > 0) {
            return chooseCourses.get(0);
        }
        // 添加选课记录信息
        XcChooseCourse chooseCourse = new XcChooseCourse();
        chooseCourse.setCourseId(coursePublish.getId());
        chooseCourse.setCourseName(coursePublish.getName());
        chooseCourse.setCoursePrice(0f); // 免费课程价格为0
        chooseCourse.setUserId(userId);
        chooseCourse.setCompanyId(coursePublish.getCompanyId());
        chooseCourse.setOrderType("700001"); // 免费课程
        chooseCourse.setCreateDate(LocalDateTime.now());
        chooseCourse.setStatus("701001"); // 选课成功
        chooseCourse.setValidDays(365); // 免费课程默认为 365
        chooseCourse.setValidtimeStart(LocalDateTime.now());
        chooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(365));

        // 向选课记录表中添加记录
        chooseCourseMapper.insert(chooseCourse);
        // 添加到我的课程表
        XcCourseTables courseTables = addCourseTables(chooseCourse);

        return chooseCourse;
    }

    @Transactional
    @Override
    public XcChooseCourse addChargeCourse(String userId, CoursePublish coursePublish) {
        // 如果存在待支付交易记录直接返回
        List<XcChooseCourse> chooseCourses = chooseCourseMapper.selectList(new LambdaQueryWrapper<XcChooseCourse>()
                .eq(XcChooseCourse::getUserId, userId)
                .eq(XcChooseCourse::getCourseId, coursePublish.getId())
                .eq(XcChooseCourse::getOrderType, "700002") // 收费订单
                .eq(XcChooseCourse::getStatus, "701002") // 待支付
        );
        if (chooseCourses != null && chooseCourses.size() > 0) {
            return chooseCourses.get(0);
        }
        // 创建选课记录
        XcChooseCourse chooseCourse = new XcChooseCourse();
        chooseCourse.setCourseId(coursePublish.getId());
        chooseCourse.setCourseName(coursePublish.getName());
        chooseCourse.setCoursePrice(coursePublish.getPrice());
        chooseCourse.setUserId(userId);
        chooseCourse.setCompanyId(coursePublish.getCompanyId());
        chooseCourse.setOrderType("700002"); // 收费课程
        chooseCourse.setCreateDate(LocalDateTime.now());
        chooseCourse.setStatus("701002"); // 待支付
        chooseCourse.setValidDays(coursePublish.getValidDays());
        chooseCourse.setValidtimeStart(LocalDateTime.now());
        chooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(coursePublish.getValidDays()));

        // 插入一条选课记录到数据库中
        chooseCourseMapper.insert(chooseCourse);

        return chooseCourse;
    }

    @Transactional
    @Override
    public XcCourseTables addCourseTables(XcChooseCourse chooseCourse) {
        // 选课记录完成且尚未过期可以添加到课程表
        String status = chooseCourse.getStatus();
        if (!"701001".equals(status)) {
            XueChengPlusException.cast("选课未成功，无法添加到课程表");
        }
        // 查询我的课程表
        XcCourseTables courseTables = getXcCourseTables(chooseCourse.getUserId(), chooseCourse.getCourseId());
        if (courseTables != null) {
            return courseTables;
        }
        // 新增课程表
        courseTables = new XcCourseTables();
        courseTables.setChooseCourseId(chooseCourse.getId());
        courseTables.setUserId(chooseCourse.getUserId());
        courseTables.setCourseId(chooseCourse.getCourseId());
        courseTables.setCompanyId(chooseCourse.getCompanyId());
        courseTables.setCourseName(chooseCourse.getCourseName());
        courseTables.setCreateDate(LocalDateTime.now());
        courseTables.setValidtimeStart(chooseCourse.getValidtimeStart());
        courseTables.setValidtimeEnd(chooseCourse.getValidtimeEnd());
        courseTables.setCourseType(chooseCourse.getOrderType());
        // 添加到数据库
        courseTablesMapper.insert(courseTables);

        return courseTables;
    }

    @Override
    public XcCourseTables getXcCourseTables(String userId, Long courseId) {
        return courseTablesMapper.selectOne(new LambdaQueryWrapper<XcCourseTables>()
                .eq(XcCourseTables::getUserId, userId)
                .eq(XcCourseTables::getCourseId, courseId));
    }

    @Override
    public XcCourseTablesDto getLearningStatus(String userId, Long courseId) {
        // 查询我的课程表
        XcCourseTables courseTables = getXcCourseTables(userId, courseId);
        if (courseTables == null) {
            XcCourseTablesDto courseTablesDto = new XcCourseTablesDto();
            // 没有选课或选课后没有支付
            courseTablesDto.setLearnStatus("702002");
            return courseTablesDto;
        }
        XcCourseTablesDto courseTablesDto = new XcCourseTablesDto();
        BeanUtils.copyProperties(courseTables, courseTablesDto);
        // 是否过期
        boolean isExpires = courseTables.getValidtimeEnd().isBefore(LocalDateTime.now());
        if (!isExpires) {
            // 未过期，正常学习
            courseTablesDto.setLearnStatus("702001");
        } else {
            // 已过期
            courseTablesDto.setLearnStatus("702003");
        }
        return courseTablesDto;
    }

    @Override
    public boolean saveChooseCourseStatus(String chooseCourseId) {
        return false;
    }

    @Override
    public PageResult<MyCourseTableItemDto> myCourseTables(MyCourseTableParams params) {
        // 页码
        int page = params.getPage();
        // 每页记录数，固定为 4
        int size = params.getSize();
        // 开始索引
        int startIndex = (page - 1) * size;

        params.setStartIndex(startIndex);

        List<MyCourseTableItemDto> myCourseTableItemDtos = courseTablesMapper.myCourseTables(params);

        int total = courseTablesMapper.myCourseTablesCount(params);

        PageResult<MyCourseTableItemDto> pageResult = new PageResult<>();
        pageResult.setItems(myCourseTableItemDtos);
        pageResult.setCounts(total);
        pageResult.setPage(page);
        pageResult.setPageSize(size);

        return pageResult;
    }
}
