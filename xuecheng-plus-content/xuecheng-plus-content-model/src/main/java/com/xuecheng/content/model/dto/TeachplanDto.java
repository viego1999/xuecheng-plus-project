package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author Wuxy
 * @version 1.0
 * @ClassName TeachplanDto
 * @since 2023/1/19 23:33
 */
@EqualsAndHashCode(callSuper = true)
@Slf4j
@Data
public class TeachplanDto extends Teachplan {

    /**
     * 关联的媒资信息
     */
    TeachplanMedia teachplanMedia;

    /**
     * 子目录
     */
    List<TeachplanDto> teachPlanTreeNodes;

}
