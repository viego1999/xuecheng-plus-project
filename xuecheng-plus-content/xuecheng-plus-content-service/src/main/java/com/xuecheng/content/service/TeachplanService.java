package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;

import java.util.List;

/**
 * 课程计划业务接口
 *
 * @author Wuxy
 * @version 1.0
 * @ClassName TeachplanService
 * @since 2023/1/20 10:26
 */
public interface TeachplanService {

    /**
     * 查询课程计划树形结构
     *
     * @param courseId 课程id
     * @return 课程计划树形结构
     */
    List<TeachplanDto> findTeachplanTree(Long courseId);

    /**
     * 保存课程计划（新增/修改）
     *
     * @param teachplan 课程计划
     */
    void saveTeachplan(SaveTeachplanDto teachplan);

}
