package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachplanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程计划编辑接口
 *
 * @author Wuxy
 * @version 1.0
 * @ClassName TeachplanController
 * @since 2023/1/19 23:35
 */
@Api(value = "课程计划编辑接口", tags = "课程计划编辑接口")
@Slf4j
@RestController
@RequestMapping("/teachplan")
public class TeachplanController {

    @Autowired
    private TeachplanService teachplanService;

    @ApiOperation("查询课程计划树形结构")
    @ApiImplicitParam(value = "courseId", name = "课程id", required = true, dataType = "Long", paramType = "path")
    @GetMapping("/{courseId}/tree-nodes")
    public List<TeachplanDto> getTreeNodes(@PathVariable("courseId") Long courseId) {
        return teachplanService.findTeachplanTree(courseId);
    }

    @ApiOperation("课程计划创建或修改")
    @PostMapping("")
    public void saveTeachplan(@RequestBody SaveTeachplanDto teachplan) {
        teachplanService.saveTeachplan(teachplan);
    }

    @ApiOperation("课程计划和媒资信息绑定")
    @PostMapping("/association/media")
    public TeachplanMedia associationMedia(@RequestBody BindTeachplanMediaDto bindTeachplanMediaDto) {
        return teachplanService.associationMedia(bindTeachplanMediaDto);
    }

    @ApiOperation("解绑课程计划和媒资信息")
    @DeleteMapping("/association/media/{teachplanId}/{mediaId}")
    public void dissociationMedia(@PathVariable("teachplanId") Long teachplanId, @PathVariable("mediaId") String mediaId) {
        teachplanService.deleteTeachplanMedia(teachplanId, mediaId);
    }
}
