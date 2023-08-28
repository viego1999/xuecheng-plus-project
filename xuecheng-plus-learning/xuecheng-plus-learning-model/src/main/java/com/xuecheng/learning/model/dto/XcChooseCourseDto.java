package com.xuecheng.learning.model.dto;

import com.xuecheng.learning.model.po.XcChooseCourse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 选课 dto 类
 *
 * @author Wuxy
 * @version 1.0
 * @since 2022/10/2 16:10
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ToString
public class XcChooseCourseDto extends XcChooseCourse {

    /**
     * 学习资格，<p>
     * [{"code":"702001","desc":"正常学习"},{"code":"702002","desc":"没有选课或选课后没有支付"},{"code":"702003","desc":"已过期需要申请续期或重新支付"}]
     */
    public String learnStatus;

}
