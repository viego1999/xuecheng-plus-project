package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.TeachplanMedia;

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

    /**
     * 教学计划绑定媒资
     *
     * @param bindTeachplanMediaDto 教学计划-媒资管理绑定数据
     * @return {@link com.xuecheng.content.model.po.TeachplanMedia}
     * @author Mr.M
     * @since 2022/9/14 22:20
     */
    TeachplanMedia associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto);

    /**
     * 删除指定教学计划-媒资绑定信息
     *
     * @param teachplanId 教学计划id
     * @param mediaId     媒资id
     */
    void deleteTeachplanMedia(Long teachplanId, String mediaId);

}
