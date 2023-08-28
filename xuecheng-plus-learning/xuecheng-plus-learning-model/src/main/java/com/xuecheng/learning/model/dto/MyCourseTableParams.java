package com.xuecheng.learning.model.dto;

import lombok.Data;
import lombok.ToString;

/**
 * 我的课程查询条件
 *
 * @author Wuxy
 * @version 1.0
 * @since 2022/10/6 9:42
 */
@Data
@ToString
public class MyCourseTableParams {

    /**
     * 用户id
     */
    private String userId;

    /**
     * 课程类型  [{"code":"700001","desc":"免费课程"},{"code":"700002","desc":"收费课程"}]
     */
    private String courseType;

    /**
     * 排序 1按学习时间进行排序 2按加入时间进行排序
     */
    private String sortType;

    /**
     * 1 即将过期、2 已经过期
     */
    private String expiresType;

    /**
     * 页码
     */
    int page = 1;

    /**
     * 开始索引
     */
    int startIndex;

    /**
     * 每页大小
     */
    int size = 4;

}
