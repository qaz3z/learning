package com.mp.demo.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * <b>功能描述:</b><br>
 * @author qaz
 * @version 1.0.0
 * @Note <b>创建时间:</b> 2022-08-20
 * @since JDK 1.8
 */
@Data
@ToString
public class MsgVO extends MsgDTO{
    /***
     * 校验的字段逗号隔开
     */
    private String checkField;
    /***
     * 是否进行不要词汇的过滤
     */
    private Boolean checkWord;
}