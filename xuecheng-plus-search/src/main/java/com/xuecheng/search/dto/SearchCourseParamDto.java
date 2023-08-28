package com.xuecheng.search.dto;

import lombok.Data;
import lombok.ToString;

/**
 * 搜索课程参数dto
 *
 * @author Wuxy
 * @version 1.0
 * @since 2022/9/24 22:36
 */
@Data
@ToString
public class SearchCourseParamDto {

    // 关键字
    private String keywords;

    // 大分类
    private String mt;

    // 小分类
    private String st;
    // 难度等级
    private String grade;

}
