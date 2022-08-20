package com.mp.demo.config;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.IdUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.micrometer.core.instrument.Meter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
/**
 * <b>功能描述:唯一id</b><br>
 * @author qaz
 * @version 1.0.0
 * @Note <b>创建时间:</b> 2022-08-20
 * @since JDK 1.8
 */
@Component
@Slf4j
public class SnowflakeConfig {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    /**为终端ID**/
    private long workerId = 0;
    /**数据中心ID**/
    private long datacenterId = 1;
    private Snowflake snowflake = IdUtil.getSnowflake(workerId,datacenterId);
    @PostConstruct
    public void init(){
        workerId = NetUtil.ipv4ToLong(NetUtil.getLocalhostStr());
        log.info("当前机器的workId:{}",workerId);
    }
    public synchronized long snowflakeId(){
        return snowflake.nextId();
    }
    public synchronized long snowflakeId(long workerId,long datacenterId){
        Snowflake snowflake = IdUtil.getSnowflake(workerId, datacenterId);
        return snowflake.nextId();
    }
}

