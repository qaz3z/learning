package com.mp.demo.serivce.impl.word;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mp.demo.dao.SensitiveWordDao;
import com.mp.demo.entity.SensitiveWordEntity;
import com.mp.demo.serivce.word.SensitiveWordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 部门业务实现
 * @Author qaz
 * @CreateTime 2022/8/29
 */
@Service
@Transactional
public class SensitiveWordServiceImpl extends ServiceImpl<SensitiveWordDao, SensitiveWordEntity> implements SensitiveWordService {

}