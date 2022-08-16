package com.mp.demo.serivce.indecent;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * <b>功能描述:不雅词汇</b><br>
 * @author qaz
 * @version 1.0.0
 * @Note <b>创建时间:</b> 2022-08-13
 * @since JDK 1.8
 */
public interface IndecentService {

    Set<String> getSensitiveWordsByDFA(String content);

    HashMap<String, Object> getSensitiveWordsByDFA(String content, boolean enableIndex, int minMatchTYpe);
    /**
     *
     * 重新reload
     * @param bloomFilterName
     * @throws IOException
     */
    void reLoadSensitiveWordsFilter(String bloomFilterName) throws IOException;

    /**
     * 通过布隆过滤器来实现不雅词汇的过滤
     * @author qaz
     * @createTime 2022/8/31
     * @param  segments 部门信息实体类
     * @return List<String>
     */
    List<String> segmentSensitiveFilterPassed(String[] segments);
}
