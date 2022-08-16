package com.mp.demo.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * <b>功能描述:</b><br>
 * @author qaz
 * @version 1.0.0
 * @Note <b>创建时间:</b> 2022-08-15
 * @since JDK 1.8
 */
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public enum StatusEnum {
    /** 删除*/
    DEL(1,"删除"),
    USE(0,"不删除");
    private Integer Code;
    private String name;

    public void setCode(Integer code) {
        Code = code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static StatusEnum geTarget(String code) {
        for (StatusEnum item : StatusEnum.values()) {
            Integer  flowCode = item.getCode();
            if (code.equals(flowCode)) {
                return item;
            }
        }
        return null;
    }
}