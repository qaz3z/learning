package com.mp.demo.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mp.demo.entity.SensitiveWordEntity;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 部门信息Dao
 * @Author qaz
 * @CreateTime 2022/8/29
 */
public interface SensitiveWordDao extends BaseMapper<SensitiveWordEntity> {
    @Select("select distinct word from sensitive_word where deleted = 0 order by word desc")
    List<String> getWord();
}