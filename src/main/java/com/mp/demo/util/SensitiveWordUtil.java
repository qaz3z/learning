package com.mp.demo.util;


import cn.hutool.core.util.ObjectUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <b>功能描述:敏感词处理工具 - DFA算法实现</b><br>
 * @author qaz
 */
public class SensitiveWordUtil {

    /**
     * 最小匹配规则，如：敏感词库["中国","中国人"]，语句："我是中国人"，匹配结果：我是[中国]人
     */
    public static final int MinMatchTYpe = 1;
    /**
     * 最大匹配规则，如：敏感词库["中国","中国人"]，语句："我是中国人"，匹配结果：我是[中国人]
     */
    public static final int MaxMatchType = 2;

    /**
     * 敏感词集合
     */
    public static ConcurrentHashMap<String, String> sensitiveWordMap;

    /**
     * 初始化敏感词库，构建DFA算法模型
     * @param sensitiveWordSet 敏感词库
     */
    public static synchronized void init(List<String> sensitiveWordSet) {
        initSensitiveWordMap(sensitiveWordSet);
    }

    /**
     * 初始化敏感词库，构建DFA算法模型
     * @param sensitiveWordSet 敏感词库
     */
    private static void initSensitiveWordMap(List<String> sensitiveWordSet) {
        if (!CollectionUtils.isEmpty(sensitiveWordSet)) {
            sensitiveWordMap = new ConcurrentHashMap(sensitiveWordSet.size());
            Map nowMap;
            Map<String, String> newWorMap;
            for (String word : sensitiveWordSet) {
                nowMap = sensitiveWordMap;
                int wordLength = word.length();
                for (int i = 0; i < wordLength; i++) {
                    // 转换成char型
                    char keyChar = word.charAt(i);
                    // 库中获取关键字
                    Object wordMap = nowMap.get(keyChar);
                    //如果存在该key，直接赋值，用于下一个循环获取
                    if (ObjectUtil.isNotEmpty(wordMap)) {
                        nowMap = (Map) wordMap;
                    } else {
                        // 不存在则，则构建一个map，同时将isEnd设置为0，因为他不是最后一个
                        newWorMap = new HashMap<>(2);
                        // 不是最后一个
                        newWorMap.put("isEnd", "0");
                        nowMap.put(keyChar, newWorMap);
                        nowMap = newWorMap;
                    }
                    if (i == wordLength - 1) {
                        // 最后一个
                        nowMap.put("isEnd", "1");
                    }

                }
            }
        }

    }

    /**
     * 判断文字是否包含敏感字符
     * @param txt       文字
     * @param matchType 匹配规则 1：最小匹配规则，2：最大匹配规则
     * @return 若包含返回true，否则返回false
     */
    public static boolean contains(String txt, int matchType) {
        if (StringUtils.isNotBlank(txt)) {
            for (int i = 0; i < txt.length(); i++) {
                int matchFlag = checkSensitiveWord(txt, i, matchType);
                // ture中止循环
                return matchFlag > 0 ? true : false;
            }
        }
        return false;
    }

    /**
     * 判断文字是否包含敏感字符
     * @param txt 文字
     * @return 若包含返回true，否则返回false
     */
    public static boolean contains(String txt) {
        return contains(txt, MaxMatchType);
    }

    /**
     * 获取文字中的敏感词
     * @param txt       文字
     * @param matchType 匹配规则 1：最小匹配规则，2：最大匹配规则
     * @return
     */
    public static Set<String> getSensitiveWord(String txt, int matchType) {
        Set<String> sensitiveWordList = new HashSet<>();
        for (int i = 0; i < txt.length(); i++) {
            int length = checkSensitiveWord(txt, i, matchType);
            //存在,加入list中
            if (length > 0) {
                sensitiveWordList.add(txt.substring(i, i + length));
                //减1的原因，是因为for会自增
                i = i + length - 1;
            }
        }
        return sensitiveWordList;
    }

    /**
     * 获取文字中的敏感词
     * @param txt       文字
     * @param matchType 匹配规则 1：最小匹配规则，2：最大匹配规则
     * @return HashMap<String, Object> 具体的内容和位置
     */
    public static HashMap<String, Object> getSensitiveWordIndex(String txt, int matchType) {
        Set<String> sensitiveWordSet = new HashSet<>();
        List<String> sensitiveIndexList = new ArrayList<>();
        HashMap<String, Object> result = new HashMap<>(2);
        for (int i = 0; i < txt.length(); i++) {
            int length = checkSensitiveWord(txt, i, matchType);
            //存在,加入list中
            if (length > 0) {
                sensitiveWordSet.add(txt.substring(i, i + length));
                StringBuffer indexBuffer = new StringBuffer(i+"").append(",").append(i + length);
                sensitiveIndexList.add(indexBuffer.toString());
                //减1的原因，是因为for会自增
                i = i + length - 1;
            }
        }
        result.put("sensitiveWordSet", sensitiveWordSet);
        result.put("sensitiveIndexList", sensitiveIndexList);
        return result;
    }

    /**
     * 获取文字中的敏感词
     * @param txt 文字
     * @return
     */
    public static Set<String> getSensitiveWord(String txt) {
        return getSensitiveWord(txt, MaxMatchType);
    }

    /**
     * 获取文字中的敏感词
     * @param txt 文字
     * @return
     */
    public static HashMap<String, Object> getSensitiveWordAndIndex(String txt, int MaxMatchType) {
        return getSensitiveWordIndex(txt, MaxMatchType);
    }

    /**
     * 检查文字中是否包含敏感字符，检查规则如下：
     * @param txt
     * @param beginIndex
     * @param matchType
     * @return 如果存在，则返回敏感词字符的长度，不存在返回0
     */
    private static int checkSensitiveWord(String txt, int beginIndex, int matchType) {
        //敏感词结束标识位：用于敏感词只有1位的情况
        boolean flag = false;
        //匹配标识数默认为0
        int matchFlag = 0;
        char word;
        Map nowMap = sensitiveWordMap;
        for (int i = beginIndex; i < txt.length(); i++) {
            word = txt.charAt(i);
            // 获取指定key
            nowMap = (Map) nowMap.get(word);
            // 存在，则判断是否为最后一个
            if (ObjectUtil.isNotEmpty(nowMap)) {
                // 找到相应key，匹配标识+1
                matchFlag++;
                // 如果为最后一个匹配规则,结束循环，返回匹配标识数
                if ("1".equals(nowMap.get("isEnd"))) {
                    //结束标志位为true
                    flag = true;
                    //最小规则，直接返回,最大规则还需继续查找
                    if (MinMatchTYpe == matchType) {
                        break;
                    }
                }
            } else {
                //不存在，直接返回
                break;
            }
        }
        //长度必须大于等于1，为词
        if (matchFlag < 2 || !flag) {
            matchFlag = 0;
        }
        return matchFlag;
    }


    /**
     * 敏感词替换工具方法（对外方法）
     * @param sensitiveWordList
     * @param contents
     * @return
     * @throws IOException 读写文件异常
     */
    public static Set<String> sensitiveHelper(List<String> sensitiveWordList, String contents) {
        // 初始化敏感词库
        SensitiveWordUtil.init(sensitiveWordList);
        // 判断是否包含敏感词库
        if (contains(contents)) {
            return SensitiveWordUtil.getSensitiveWord(contents);
        }
        // 不包含返回原本字符
        return new HashSet<>();
    }

    /**
     * 敏感词替换工具方法（对外方法）
     * @param sensitiveWordList
     * @param contents
     * @return
     * @throws IOException 读写文件异常
     */
    public static HashMap<String, Object> sensitiveHelper(List<String> sensitiveWordList, String contents, boolean enableIndex, int minMatchTYpe) {
        // 初始化敏感词库
        SensitiveWordUtil.init(sensitiveWordList);
        HashMap<String, Object> result = new HashMap<>(2);
        // 判断是否包含敏感词库
        if (contains(contents)) {
            if (enableIndex) {
                result = SensitiveWordUtil.getSensitiveWordAndIndex(contents, minMatchTYpe);
            } else {
                Set<String> sensitiveWord = SensitiveWordUtil.getSensitiveWord(contents);
                result.put("sensitiveWordSet", sensitiveWord);
            }
        }
        // 不包含返回原本字符
        return result;
    }
}