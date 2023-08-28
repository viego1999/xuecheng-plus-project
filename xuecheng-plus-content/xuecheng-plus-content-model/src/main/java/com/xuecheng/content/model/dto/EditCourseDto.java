package com.xuecheng.content.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 修改课程 dto
 *
 * @author Wuxy
 * @version 1.0
 * @ClassName EditCourseDto
 * @since 2023/1/19 20:34
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class EditCourseDto extends AddCourseDto {

    /**
     * 课程 id
     */
    private Long id;

}
