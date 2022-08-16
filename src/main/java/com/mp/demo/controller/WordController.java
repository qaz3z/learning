package com.mp.demo.controller;

import cn.hutool.core.map.MapUtil;
import com.mp.demo.serivce.indecent.IndecentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * 自动填充和逻辑删除示例
 * @Author qaz
 * @CreateTime 2022/8/15
 */
@RestController
@RequestMapping("/word")
public class WordController {

    @Autowired
    private IndecentService indecentService;

    /**
     * 通过布隆过滤器来实现不雅词汇的过滤
     * @Author qaz
     * @CreateTime 2022/8/31
     * @Param  map 部门信息实体类
     * @Return void
     */
    @PostMapping("/getWordsSensitive")
    public List<String> getWordsSensitive(@RequestBody HashMap<String,String > map){
        String word = map.get("word");
        String[] arr = {word};
        StopWatch sw = new StopWatch("从文件里面加载数据敏感次");
        sw.start("从文件里面加载数据");
        List<String> strings = indecentService.segmentSensitiveFilterPassed(arr);
        // 停止计时
        sw.stop();
        System.out.printf("加载到耗时：%d%s.\n", sw.getLastTaskTimeMillis(), "ms");
        return strings;
    }
    /**
     * 通过构建DFA算法模型实现不雅词汇的过滤
     * @Author qaz
     * @CreateTime 2022/8/31
     * @Param  map
     * @Return void
     */
    @PostMapping("/getSensitiveWordsByDFA")
    public Set<String> getWordsSensitiveByDFA1(@RequestBody HashMap<String,String > map){
        String word = map.get("word");
        return indecentService.getSensitiveWordsByDFA(word);
    }
    /**
     * 通过构建DFA算法模型实现不雅词汇的过滤
     * @Author qaz
     * @CreateTime 2022/8/31
     * @Param  map
     * @Return void
     */
    @PostMapping("/getSensitiveWordsByDFA1")
    public HashMap<String, Object> getWordsSensitiveByDFA(@RequestBody HashMap<String,Object> map){
        int minMatchTYpe = MapUtil.getInt(map, "minMatchTYpe");
        String content = MapUtil.getStr(map, "content");
        boolean enableIndex = MapUtil.getBool(map, "enableIndex");
        return indecentService.getSensitiveWordsByDFA(content, enableIndex, minMatchTYpe);
    }
    /**
     * 通过构建DFA算法模型实现不雅词汇的过滤
     * @Author qaz
     * @CreateTime 2022/8/31
     * @Param  map
     * @Return void
     */
    @DeleteMapping("/reLoadSensitiveWordsFilter")
    public String reLoadSensitiveWordsFilter(@RequestBody HashMap<String,Object> map){
        try {
            indecentService.reLoadSensitiveWordsFilter("SensitiveWords");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "1";
    }
}