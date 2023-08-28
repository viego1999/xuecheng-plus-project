package com.xuecheng.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.model.po.CourseCategory;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 课程分类 Mapper 接口
 * </p>
 *
 * @author itcast
 */
public interface CourseCategoryMapper extends BaseMapper<CourseCategory> {

    /**
     * 根据分类id查询所有子目录
     *
     * @param id 分类id
     * @return 子目录
     */
    List<CourseCategoryTreeDto> selectTreeNodes(String id);

    /**
     * 根据分类id查询分类名称
     *
     * @param id 分类id
     * @return 分类名称
     */
    @Select("SELECT name FROM course_category where id = #{id}")
    String selectNameById(@Param("id") String id);

}
