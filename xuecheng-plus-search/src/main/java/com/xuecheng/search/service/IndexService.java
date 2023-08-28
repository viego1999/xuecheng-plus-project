package com.xuecheng.search.service;

/**
 * 课程索引service
 *
 * @author Wuxy
 * @version 1.0
 * @since 2022/9/24 22:40
 */
public interface IndexService {

    /**
     * 添加索引
     *
     * @param indexName 索引名称
     * @param id        主键
     * @param object    索引对象
     * @return Boolean true表示成功,false失败
     * @author Wuxy
     * @since 2022/9/24 22:57
     */
    Boolean addCourseIndex(String indexName, String id, Object object);


    /**
     * 更新索引
     *
     * @param indexName 索引名称
     * @param id        主键
     * @param object    索引对象
     * @return {@link Boolean} true表示成功,false失败
     * @author Wuxy
     * @since 2022/9/25 7:49
     */
    Boolean updateCourseIndex(String indexName, String id, Object object);

    /**
     * 删除索引
     *
     * @param indexName 索引名称
     * @param id        主键
     * @return {@link java.lang.Boolean}
     * @author Wuxy
     * @since 2022/9/25 9:27
     */
    Boolean deleteCourseIndex(String indexName, String id);

}
