package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.service.TeachplanService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 课程计划service接口实现类
 *
 * @author Wuxy
 * @version 1.0
 * @ClassName TeachplaneServiceImpl
 * @since 2023/1/20 10:28
 */
@Service
public class TeachplaneServiceImpl implements TeachplanService {

    @Resource
    private TeachplanMapper teachplanMapper;

    @Override
    public List<TeachplanDto> findTeachplanTree(Long courseId) {
        return teachplanMapper.selectTreeNodes(courseId);
    }

    @Transactional
    @Override
    public void saveTeachplan(SaveTeachplanDto dto) {
        // 获得教学计划id
        Long id = dto.getId();
        // 修改课程计划
        if (id != null) {
            Teachplan teachplan = teachplanMapper.selectById(id);
            if (teachplan != null) {
                BeanUtils.copyProperties(dto, teachplan);
                teachplanMapper.updateById(teachplan);
            }
        } else { // 新增
            Teachplan teachplan = new Teachplan();
            BeanUtils.copyProperties(dto, teachplan);
            // 取出同父同级别的课程数量
            int count = getTeachplanCount(dto.getCourseId(), dto.getParentid());
            // 计算下默认顺序（新的课程计划的orderby）
            teachplan.setOrderby(count + 1);

            teachplanMapper.insert(teachplan);
        }
    }


    /**
     * 找到同级课程计划的数量 <pre>{@code select count(*) from teachplan where course_id=id and parentid=pid;}</pre>
     *
     * @param courseId 课程id
     * @param parentId 父级目录id
     * @return 同级课程数量
     */
    private int getTeachplanCount(Long courseId, Long parentId) {
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId, courseId).eq(Teachplan::getParentid, parentId);
        return teachplanMapper.selectCount(queryWrapper);
    }
}
