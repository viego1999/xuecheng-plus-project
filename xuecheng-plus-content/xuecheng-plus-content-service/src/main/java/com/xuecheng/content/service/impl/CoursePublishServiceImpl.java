package com.xuecheng.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignclient.MediaServiceClient;
import com.xuecheng.content.feignclient.SearchServiceClient;
import com.xuecheng.content.feignclient.po.CourseIndex;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.mapper.CoursePublishPreMapper;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.model.po.CoursePublishPre;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.service.TeachplanService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Wuxy
 * @version 1.0
 * @ClassName CoursePublishServiceImpl
 * @since 2023/1/30 15:45
 */
@Slf4j
@Service
public class CoursePublishServiceImpl implements CoursePublishService {
    @Resource
    private CourseBaseMapper courseBaseMapper;

    @Resource
    private CourseMarketMapper courseMarketMapper;

    @Resource
    private CoursePublishMapper coursePublishMapper;

    @Resource
    private CoursePublishPreMapper coursePublishPreMapper;

    @Autowired
    private CourseBaseInfoService courseBaseInfoService;

    @Autowired
    private TeachplanService teachplanService;

    @Autowired
    private MqMessageService mqMessageService;

    @Autowired
    private MediaServiceClient mediaServiceClient;

    @Autowired
    private SearchServiceClient searchServiceClient;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedissonClient redissonClient;


    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {
        // 查询课程发布信息
        CoursePublish coursePublish = getCoursePublishCache(courseId);

        // 课程基本信息
        CourseBaseInfoDto courseBaseInfo = new CourseBaseInfoDto();
        BeanUtils.copyProperties(coursePublish, courseBaseInfo);

        // 课程计划信息
        List<TeachplanDto> teachplans = JSON.parseArray(coursePublish.getTeachplan(), TeachplanDto.class);

        // 创建课程预览信息
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        coursePreviewDto.setCourseBase(courseBaseInfo);
        coursePreviewDto.setTeachplans(teachplans);

        return coursePreviewDto;
    }

    @Override
    public CoursePreviewDto getOpenCoursePreviewInfo(Long courseId) {
        // 课程基本信息
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.queryCourseBaseById(courseId);
        // 课程计划信息
        List<TeachplanDto> teachplans = teachplanService.findTeachplanTree(courseId);

        // 创建课程预览信息
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        coursePreviewDto.setCourseBase(courseBaseInfo);
        coursePreviewDto.setTeachplans(teachplans);

        return coursePreviewDto;
    }

    @Override
    public void commitAudit(Long companyId, Long courseId) {
        // 课程基本信息
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        // 课程审核状态
        String auditStatus = courseBase.getAuditStatus();
        if ("202003".equals(auditStatus)) {
            XueChengPlusException.cast("当前为等待审核状态，审核完成可以再次提交。");
        }
        // 本机构只允许提交本机构的课程
        if (!companyId.equals(courseBase.getCompanyId())) {
            XueChengPlusException.cast("不允许提交其它机构的课程。");
        }
        // 课程图片是否填写
        if (StringUtils.isEmpty(courseBase.getPic())) {
            XueChengPlusException.cast("提交失败，请上传课程图片");
        }
        // 添加课程预发布记录
        CoursePublishPre coursePublishPre = new CoursePublishPre();
        // 课程基本信息和营销信息
        CourseBaseInfoDto courseBaseInfoDto = courseBaseInfoService.queryCourseBaseById(courseId);
        BeanUtils.copyProperties(courseBaseInfoDto, coursePublishPre);
        // 课程营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        // 转为 JSON
        String courseMarketJson = JSON.toJSONString(courseMarket);
        // 将课程营销信息放入课程预发布表
        coursePublishPre.setMarket(courseMarketJson);

        // 查询课程计划信息
        List<TeachplanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);
        if (teachplanTree == null || teachplanTree.isEmpty()) {
            XueChengPlusException.cast("提交失败，还没有添加课程计划");
        }
        // 转 json
        String teachplanJson = JSON.toJSONString(teachplanTree);
        coursePublishPre.setTeachplan(teachplanJson);

        // 设置预发布记录状态
        coursePublishPre.setStatus("202003");
        // 教学机构id
        coursePublishPre.setCompanyId(companyId);
        // 提交时间
        coursePublishPre.setCreateDate(LocalDateTime.now());

        CoursePublishPre coursePublishPreUpdate = coursePublishPreMapper.selectById(companyId);
        if (coursePublishPreUpdate == null) {
            // 添加课程预发布记录
            coursePublishPreMapper.insert(coursePublishPre);
        } else {
            // 更新课程预发布记录
            coursePublishPreMapper.updateById(coursePublishPre);
        }

        // 更新课程基本表的审核状态
        courseBase.setAuditStatus("202003");
        courseBaseMapper.updateById(courseBase);
    }

    @Transactional
    @Override
    public void publish(Long companyId, Long courseId) {
        // 查询课程预发布表
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPre == null) {
            XueChengPlusException.cast("请先提交课程审核，审核通过才可以发布");
        }
        // 本机构只允许提交本机构的课程
        if (!companyId.equals(coursePublishPre.getCompanyId())) {
            XueChengPlusException.cast("不允许提交其它机构的课程。");
        }

        // 获得课程审核状态
        String status = coursePublishPre.getStatus();
        // 审核通过才可以进行发布
        if (!"202004".equals(status)) {
            XueChengPlusException.cast("操作失败，课程审核通过方可发布。");
        }

        // 保存课程发布信息
        saveCoursePublish(courseId);

        // 保存消息表
        saveCoursePublishMessage(courseId);

        // 删除课程预发布表记录
        coursePublishPreMapper.deleteById(courseId);
    }

    @Override
    public File generateCourseHtml(Long courseId) {
        // 静态化文件
        File file = null;
        try {
            // 配置 freemarker
            Configuration configuration = new Configuration(Configuration.getVersion());
            // 加载模板，选指定模板路径，classpath 下 templates 下
            // 得到 classpath
            String classpath = this.getClass().getResource("/").getPath();
            configuration.setDirectoryForTemplateLoading(new File(classpath + "/templates/"));
            // 设置字符编码
            configuration.setDefaultEncoding("utf-8");
            // 指定模板文件名称
            Template template = configuration.getTemplate("course_template.ftl");
            // 准备数据
            CoursePreviewDto coursePreviewInfo = this.getCoursePreviewInfo(courseId);

            Map<String, Object> map = new HashMap<>();
            map.put("model", coursePreviewInfo);

            // 静态化
            String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
            // 将静态化内容输出到文件中
            InputStream inputStream = IOUtils.toInputStream(content, StandardCharsets.UTF_8);
            // 创建静态化文件
            file = File.createTempFile("course", "html");
            log.debug("课程静态化，生成静态化文件：{}", file.getAbsolutePath());
            // 输出流
            FileOutputStream outputStream = new FileOutputStream(file);
            IOUtils.copy(inputStream, outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    @Override
    public void uploadCourseHtml(Long courseId, File file) {
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);
        String course = mediaServiceClient.uploadFile(multipartFile, "course", courseId + ".html");
        if (course == null) {
            XueChengPlusException.cast("远程调用媒资服务上传文件失败");
        }
    }

    @Override
    public Boolean saveCourseIndex(Long courseId) {
        // 1.取出课程发布信息
        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
        // 2.拷贝至课程索引对象
        CourseIndex courseIndex = new CourseIndex();
        BeanUtils.copyProperties(coursePublish, courseIndex);
        // 3.远程调用搜索服务 api 添加课程信息到索引
        Boolean success = searchServiceClient.add(courseIndex);
        if (!success) {
            XueChengPlusException.cast("添加课程索引失败");
        }
        return true;
    }

    @Override
    public CoursePublish getCoursePublish(Long courseId) {
        return coursePublishMapper.selectById(courseId);
    }

    @Override
    public CoursePublish getCoursePublishCache(Long courseId) {
        // 查询缓存
        String key = "course_" + courseId;
        String jsonStr = stringRedisTemplate.opsForValue().get(key);
        // 缓存命中
        if (StringUtils.isNotEmpty(jsonStr)) {
            if (jsonStr.equals("null")) { // 如果为null字符串，表明数据库中不存在此课程，直接返回（解决缓存穿透）
                return null;
            }
            return JSON.parseObject(jsonStr, CoursePublish.class);
        }
        // 缓存未命中，查询数据库并添加到缓存中
        // 每一门课程设置一个锁
        RLock lock = redissonClient.getLock("coursequerylock:" + courseId);
        // 阻塞等待获取锁
        lock.lock();
        try {
            // 检查是否已经存在（双从检查）
            jsonStr = stringRedisTemplate.opsForValue().get(key);
            if (StringUtils.isNotEmpty(jsonStr)) {
                return JSON.parseObject(jsonStr, CoursePublish.class);
            }
            // 查询数据库
            CoursePublish coursePublish = getCoursePublish(courseId);
            // 当 coursePublish 为空时，缓存 null 字符串
            stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(coursePublish), 1, TimeUnit.DAYS);
            return coursePublish;
        } catch (Exception e) {
            log.error("查询课程发布信息失败：", e);
            throw new XueChengPlusException("课程发布信息查询异常：" + e.getMessage());
        } finally {
            // 释放锁
            lock.unlock();
        }
    }

    /**
     * 保存课程发布信息
     *
     * @param courseId 课程 id
     * @author Wuxy
     * @since 2022/9/20 16:32
     */
    private void saveCoursePublish(Long courseId) {
        // 查询课程预发布信息
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPre == null) {
            XueChengPlusException.cast("课程预发布数据为空");
        }
        CoursePublish coursePublish = new CoursePublish();
        // 属性拷贝
        BeanUtils.copyProperties(coursePublishPre, coursePublish);
        coursePublish.setStatus("203002");
        CoursePublish coursePublishUpdate = coursePublishMapper.selectById(courseId);
        if (coursePublishUpdate == null) {
            // 插入新的课程发布记录
            coursePublishMapper.insert(coursePublish);
        } else {
            // 更新课程发布记录
            coursePublishMapper.updateById(coursePublish);
        }
        // 更新课程基本表的发布状态
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setStatus("203002");
        courseBaseMapper.updateById(courseBase);
    }

    /**
     * 保存消息表记录，稍后实现
     *
     * @param courseId 课程 id
     * @author Mr.W
     * @since 2022/9/20 16:32
     */
    private void saveCoursePublishMessage(Long courseId) {
        MqMessage mqMessage = mqMessageService.addMessage("course_publish", String.valueOf(courseId), null, null);
        if (mqMessage == null) {
            XueChengPlusException.cast("添加消息记录失败");
        }
    }
}
