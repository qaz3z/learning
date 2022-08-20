package com.mp.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * <b>功能描述:</b><br>
 * @author qaz
 * @version 1.0.0
 * @Note <b>创建时间:</b> 2022-08-17
 * @since JDK 1.8
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class TestEntity {
    private String username;
    private String password;
}