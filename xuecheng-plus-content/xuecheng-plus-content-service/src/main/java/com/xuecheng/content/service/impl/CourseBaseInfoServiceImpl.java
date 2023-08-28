package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CourseMarketService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Wuxy
 * @version 1.0
 * @ClassName CourseBaseInfoServiceImpl
 * @since 2023/1/19 10:18
 */
@Service
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {

    @Resource
    private CourseBaseMapper courseBaseMapper;
    @Resource
    private CourseMarketMapper courseMarketMapper;
    @Resource
    private CourseCategoryMapper courseCategoryMapper;
    @Resource
    private CourseMarketService courseMarketService;


    @Override
    public PageResult<CourseBase> queryCourseBaseList(Long companyId, PageParams params, QueryCourseParamsDto queryCourseParams) {
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        // 拼接查询条件
        // 根据机构id进行查询
        queryWrapper.eq(CourseBase::getCompanyId, companyId);
        // 根据课程名称模糊查询 name like '%name%'
        queryWrapper.like(StringUtils.isNotEmpty(queryCourseParams.getCourseName()), CourseBase::getName, queryCourseParams.getCourseName());
        // 根据课程审核状态
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParams.getAuditStatus()), CourseBase::getAuditStatus, queryCourseParams.getAuditStatus());
        // 根据课程发布状态
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParams.getPublishStatus()), CourseBase::getStatus, queryCourseParams.getPublishStatus());
        // 分页参数
        Page<CourseBase> page = new Page<>(params.getPageNo(), params.getPageSize());
        // 分页查询 Page 分页参数
        Page<CourseBase> pageResult = courseBaseMapper.selectPage(page, queryWrapper);

        // 数据
        List<CourseBase> items = pageResult.getRecords();
        // 总记录数
        long total = pageResult.getTotal();

        return new PageResult<>(items, total, params.getPageNo(), params.getPageSize());
    }

    @Transactional
    @Override
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto courseDto) {
        // 对参数进行合法性校验
        // 合法性校验
        if (StringUtils.isBlank(courseDto.getName())) {
            XueChengPlusException.cast("课程名称为空");
        }
        if (StringUtils.isBlank(courseDto.getMt())) {
            XueChengPlusException.cast("课程分类为空");
        }
        if (StringUtils.isBlank(courseDto.getSt())) {
            XueChengPlusException.cast("课程分类为空");
        }
        if (StringUtils.isBlank(courseDto.getGrade())) {
            XueChengPlusException.cast("课程等级为空");
        }
        if (StringUtils.isBlank(courseDto.getTeachmode())) {
            XueChengPlusException.cast("教育模式为空");
        }
        if (StringUtils.isBlank(courseDto.getUsers())) {
            XueChengPlusException.cast("适应人群为空");
        }
        if (StringUtils.isBlank(courseDto.getCharge())) {
            XueChengPlusException.cast("收费规则为空");
        }

        // 对数据进行封装，调用 mapper 进行数据持久化
        CourseBase courseBase = new CourseBase();
        // 拷贝基本信息
        BeanUtils.copyProperties(courseDto, courseBase);
        // 设置机构 id
        courseBase.setCompanyId(companyId);
        // 创建时间
        courseBase.setCreateDate(LocalDateTime.now());
        // 审核状态设置为未提交
        courseBase.setAuditStatus("202002");
        // 发布状态默认为未发布
        courseBase.setStatus("203001");
        // 向课程基本表插入一条数据
        int insert1 = courseBaseMapper.insert(courseBase);
        // 获取课程id
        Long courseId = courseBase.getId();
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(courseDto, courseMarket);
        courseMarket.setId(courseId);

        int insert2 = this.saveCourseMarket(courseMarket);

        if (insert1 < 1 || insert2 < 1) {
            XueChengPlusException.cast("添加课程失败");
        }

        // 组装要返回的结果
        return getCourseBaseInfo(courseId);
    }

    @Override
    public CourseBaseInfoDto queryCourseBaseById(Long courseId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        LambdaQueryWrapper<CourseMarket> queryWrapper = new LambdaQueryWrapper<>();
        CourseMarket courseMarket = courseMarketMapper.selectOne(queryWrapper.eq(CourseMarket::getId, courseId));
        if (courseBase == null || courseMarket == null) {
            return null;
        }
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase, courseBaseInfoDto);
        BeanUtils.copyProperties(courseMarket, courseBaseInfoDto);
        // 填充 mtName 和 stName
        courseBaseInfoDto.setMtName(courseCategoryMapper.selectNameById(courseBase.getMt()));
        courseBaseInfoDto.setStName(courseCategoryMapper.selectNameById(courseBase.getSt()));
        return courseBaseInfoDto;
    }

    @Transactional
    @Override
    public CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto dto) {
        // 校验
        // 课程 id
        Long courseId = dto.getId();
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase == null) {
            XueChengPlusException.cast("课程不存在");
            ;
        }
        // 校验本机构只能修改本机构的课程
        if (!courseBase.getCompanyId().equals(companyId)) {
            XueChengPlusException.cast("本机构只能修改本机构的课程");
        }
        // 封装基本信息的数据
        BeanUtils.copyProperties(dto, courseBase);

        // 更新课程基本信息
        courseBase.setChangeDate(LocalDateTime.now());
        courseBaseMapper.updateById(courseBase);

        // 封装营销信息的数据
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        if (courseMarket == null) {
            courseMarket = new CourseMarket();
        }
        BeanUtils.copyProperties(dto, courseMarket);
        int update = this.saveCourseMarket(courseMarket);
        System.out.println("更新营销表影响条数：" + update);
        // 查询课程信息并返回
        return getCourseBaseInfo(courseId);
    }

    @Override
    public RestResponse<Boolean> deleteCourseBase(Long courseId) {
        int delete = courseBaseMapper.deleteById(courseId);
        if (delete > 0) {
            return RestResponse.success(true);
        }
        return RestResponse.success(false);
    }

    /**
     * 根据课程id查询课程的基本和营销信息
     *
     * @param courseId 课程 id
     * @return 课程的信息
     */
    public CourseBaseInfoDto getCourseBaseInfo(Long courseId) {
        // 基本信息
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        // 营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);

        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase, courseBaseInfoDto);

        if (courseMarket != null) {
            BeanUtils.copyProperties(courseMarket, courseBaseInfoDto);
        }

        // 根据课程分类的编号查询分类的名称
        String mt = courseBase.getMt();
        String st = courseBase.getSt();

        CourseCategory mtCourseCategory = courseCategoryMapper.selectById(mt);
        CourseCategory stCourseCategory = courseCategoryMapper.selectById(st);

        if (mtCourseCategory != null) {
            // 大分类名称
            String mtName = mtCourseCategory.getName();
            courseBaseInfoDto.setMtName(mtName);
        }
        if (stCourseCategory != null) {
            // 小分类名称
            String stName = stCourseCategory.getName();
            courseBaseInfoDto.setStName(stName);
        }
        return courseBaseInfoDto;
    }

    /**
     * 抽取营销信息的保存
     *
     * @param courseMarket 课程营销对象
     * @return 返回更新条数
     */
    private int saveCourseMarket(CourseMarket courseMarket) {
        String charge = courseMarket.getCharge();
        if (StringUtils.isBlank(charge)) {
            XueChengPlusException.cast("收费规则没有选择");
        }
        // 如果是收费课程，价格必须输入
        if (charge.equals("201001")) { // 收费
            Float price = courseMarket.getPrice();
            if (price == null || price <= 0) {
                XueChengPlusException.cast("课程设置了收费价格不能为空且必须大于0");
            }
        }
        // 保存或更新
        return courseMarketService.saveOrUpdate(courseMarket) ? 1 : 0;
    }
}
