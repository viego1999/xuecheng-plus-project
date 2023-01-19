package com.xuecheng.base.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 分页参数类
 *
 * @author Wuxy
 * @version 1.0
 * @ClassName PageParams
 * @since 2023/1/18 18:47
 */
@Data
@ToString
@ApiModel("分页参数类")
@NoArgsConstructor
@AllArgsConstructor
public class PageParams {

    // 当前页码默认值
    @ApiModelProperty("当前页码默认值")
    public static final long DEFAULT_PAGE_CURRENT = 1L;
    // 每页记录数默认值
    @ApiModelProperty("每页记录数默认值")
    public static final long DEFAULT_PAGE_SIZE = 10L;
    // 当前页码
    @ApiModelProperty("当前页码")
    private Long pageNo = DEFAULT_PAGE_CURRENT;
    // 每页记录数默认值
    @ApiModelProperty("每页记录数默认值")
    private Long pageSize = DEFAULT_PAGE_SIZE;

}
