package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CourseCategoryTreeDto;

import java.util.List;

/**
 * 课程分类 Service
 *
 * @author Wuxy
 * @version 1.0
 * @ClassName CourseCategoryService
 * @since 2023/1/19 14:14
 */
public interface CourseCategoryService {

    /**
     * 课程分类查询
     *
     * @param id 根节点 id
     * @return 根节点下的所有子节点
     */
    List<CourseCategoryTreeDto> queryTreeNodes(String id);

}
