package com.xuecheng.search.dto;

import com.xuecheng.base.model.PageResult;
import lombok.ToString;

import java.util.List;

/**
 * @author Wuxy
 * @version 1.0
 * @since 2022/9/25 17:51
 */
@ToString
public class SearchPageResultDto<T> extends PageResult<T> {

    /**
     * 大分类列表
     */
    List<String> mtList;

    /**
     * 小分类列表
     */
    List<String> stList;


    public SearchPageResultDto(List<T> items, long counts, long page, long pageSize) {
        super(items, counts, page, pageSize);
    }

    public List<String> getMtList() {
        return mtList;
    }

    public void setMtList(List<String> mtList) {
        this.mtList = mtList;
    }

    public List<String> getStList() {
        return stList;
    }

    public void setStList(List<String> stList) {
        this.stList = stList;
    }
}
