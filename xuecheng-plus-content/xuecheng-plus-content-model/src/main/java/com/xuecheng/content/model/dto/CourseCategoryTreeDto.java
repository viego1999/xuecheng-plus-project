package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.CourseCategory;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 课程分类树型结点 dto
 *
 * @author Wuxy
 * @version 1.0
 * @ClassName CourseCategoryTreeDto
 * @since 2023/1/19 13:36
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CourseCategoryTreeDto extends CourseCategory {

    /**
     * 树形子节点
     */
    List<Object> childrenTreeNodes;

}
