package com.mp.demo.serivce.impl.indecent;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hankcs.hanlp.seg.Dijkstra.DijkstraSegment;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;
import com.mp.demo.dao.SensitiveWordDao;
import com.mp.demo.entity.SensitiveWordEntity;
import com.mp.demo.serivce.indecent.IndecentService;
import com.mp.demo.util.RedisUtil;
import com.mp.demo.util.SensitiveWordUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <b>功能描述:</b><br>
 * @author qaz
 * @version 1.0.0
 * @Note <b>创建时间:</b> 2022-08-13
 * @since JDK 1.8
 */
@Slf4j
@Service
@Transactional
public class IndecentServiceImpl  extends ServiceImpl<SensitiveWordDao, SensitiveWordEntity> implements IndecentService {
    @Autowired
    private RedisUtil redisUtil;


    /**
     *
     * 读取带敏感词的布隆过滤器通过布隆过滤器来实现
     *
     * @return
     * @throws IOException
     */
    @Override
    public Set<String> getSensitiveWordsByDFA(String content)  {
        StopWatch sw = new StopWatch("从Mysql里面加载数据");
        sw.start("从Mysql里面加载数据");
        List<String> sensitiveWordList = this.baseMapper.getWord();
        Set<String> strings = SensitiveWordUtil.sensitiveHelper(sensitiveWordList, content);
        sw.stop();
        log.info("加载到耗时:[{}ms]",sw.getLastTaskTimeMillis());
        return strings;
    }

    /**
     *
     * 读取带敏感词的布隆过滤器通过布隆过滤器来实现
     *
     * @return
     * @throws IOException
     */
    @Override
    public HashMap<String, Object> getSensitiveWordsByDFA(String content, boolean enableIndex, int minMatchTYpe)  {
        StopWatch sw = new StopWatch("从Mysql里面加载数据");
        sw.start("从Mysql里面加载数据");
        List<String> sensitiveWordList = this.baseMapper.getWord();
        HashMap<String, Object> objectHashMap = SensitiveWordUtil.sensitiveHelper(sensitiveWordList, content, enableIndex, minMatchTYpe);
        sw.stop();
        log.info("加载到耗时:[{}ms]",sw.getLastTaskTimeMillis());
        return objectHashMap;
    }
    /**
     *
     * 读取带敏感词的布隆过滤器通过布隆过滤器来实现
     *
     * @return
     * @throws IOException
     */
    public List<String> getWhiteFilter() throws IOException {
        InputStreamReader read;
        BufferedReader bufferedReader;
        read = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("word/white.txt"), StandardCharsets.UTF_8);
        bufferedReader = new BufferedReader(read);
        StopWatch sw = new StopWatch("从文件里面加载数据敏感次");
        sw.start("从文件里面加载数据");
        ArrayList<String> objects = new ArrayList<>(8);
        for (String txt; (txt = bufferedReader.readLine()) != null; ) {
            objects.add(txt);
        }
        // 停止计时
        sw.stop();
        log.info("加载到耗时:[{}ms]",sw.getLastTaskTimeMillis());
        return objects;
    }
    /**
     *
     * 读取带敏感词的布隆过滤器通过布隆过滤器来实现
     * @param  bloomFilterName
     * @return
     * @throws IOException
     */
    public void getSensitiveWordsFilter(String bloomFilterName) throws IOException {
        InputStreamReader read;
        BufferedReader bufferedReader;
        read = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("word/keywords.txt"), StandardCharsets.UTF_8);
        bufferedReader = new BufferedReader(read);
        RBloomFilter<String> bloomFilterByName = redisUtil.getBloomFilterByName(bloomFilterName);
        if (bloomFilterByName.isExists()){
            return;
        }
        RBloomFilter<String> sensitiveWordsBloom = redisUtil.bloomFilterInit(bloomFilterName);

        StopWatch sw = new StopWatch("从文件里面加载数据敏感次");
        sw.start("从文件里面加载数据");
        for (String txt; (txt = bufferedReader.readLine()) != null; ) {
            sensitiveWordsBloom.add(txt);
        }
        // 停止计时
        sw.stop();
        System.out.printf("加载到耗时：%d%s.\n", sw.getLastTaskTimeMillis(), "ms");
    }
    /**
     *
     * 重新reload
     * @param bloomFilterName
     * @throws IOException
     */
    @Override
    public void reLoadSensitiveWordsFilter(String bloomFilterName) throws IOException {
        boolean sensitiveWords = redisUtil.delBloomFilter(bloomFilterName);
        log.info("重新加载敏感词布隆过滤起：[]",sensitiveWords);
        getSensitiveWordsFilter(bloomFilterName);
    }
    /**
     * 判断一段文字中，是否包含敏感词
     *
     * @param segments
     * @return
     */
    @Override
    public List<String> segmentSensitiveFilterPassed(String[] segments) {
        ArrayList<String> words = new ArrayList<>();
        RBloomFilter<String> sensitiveWords  = redisUtil.getBloomFilterByName("SensitiveWords");
        if( sensitiveWords.sizeInMemory() < 1){
            try {
                getSensitiveWordsFilter("SensitiveWords");
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        Segment shortestSegment = new DijkstraSegment()
                .enableCustomDictionary(false)
                .enablePlaceRecognize(true)
                .enableOrganizationRecognize(true)
                .enableNumberQuantifierRecognize(true);

        for(String segment :segments){
            List<Term> termList = shortestSegment.seg(segment);
            for (Term term :termList){
                // 如果布隆过滤器中找到了对应的词，则认为敏感检测不通过
                if(sensitiveWords.contains(term.word)){
                    log.info("检测到敏感词：[{}]",term.word);
                    words.add(term.word);
                }
            }
        }

        // 过滤白名单
        try {
            if (!CollectionUtils.isEmpty(words)){
                List<String> whiteFilter = getWhiteFilter();
                List<String> result = words.stream().filter(word -> !whiteFilter.contains(word)).collect(Collectors.toList());
                return result;
            }

        } catch (IOException e) {
            log.info("d读取白名单数据失败：[{}]",e);
        }
        return words;
    }
}