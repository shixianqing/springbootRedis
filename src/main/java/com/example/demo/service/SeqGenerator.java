package com.example.demo.service;

import com.example.demo.dao.SeqNumMapper;
import com.example.demo.model.BatchNoSeq;
import com.example.demo.redis.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.thymeleaf.util.DateUtils;

import java.util.Date;
import java.util.Locale;
import java.util.UUID;

/**
 * @Author:shixianqing
 * @Date:2019/1/1420:52
 * @Description: 流水号生成器
 **/
@Component
public class SeqGenerator {
    private final static Logger logger = LoggerFactory.getLogger(SeqGenerator.class);
    @Autowired
    private SeqNumMapper seqNumMapper;

    private static final Long MIN_VALUE = 1L;

    private static final Long MAX_VALUE = 9999999999L;

    @Autowired
    private RedisService redisService;


    //项目名称
    private final static String LOCK_KEY = "claimconf";

    /**
     * 使用分布式锁获取全局自增数据
     * @param code
     * @return
     */
    public String genSeqCode(String code){
        String temp = "";
        String uid = UUID.randomUUID().toString();
        Boolean getlock = redisService.tryGetDistributedLock(LOCK_KEY,uid,60000L);
        if(getlock){
            logger.info("线程：{}，获取锁成功了",Thread.currentThread().getName());
            temp = generateSeq(code);
        }else{
            logger.error("线程：{}，获取锁失败！，继续获取",Thread.currentThread().getName());
            while (true){
                Boolean getLockSuccess = redisService.tryGetDistributedLock(LOCK_KEY,uid,60000L);
                if (getLockSuccess){
                    logger.info("线程：{}，重新获取锁成功了",Thread.currentThread().getName());
                    temp = generateSeq(code);
                    break;
                }
            }
        }
        redisService.releaseDistributedLock(LOCK_KEY,uid);
        return temp;
    }
    /**
     * 生成序列号
     * @param code 业务代码
     * @return
     */
    public String generateSeq(String code){
        Long currentVal;
        Long nextVal;
        String currDateStr = DateUtils.format(new Date(), "yyyy-MM-dd", Locale.CHINA);
        BatchNoSeq batchNoSeq = seqNumMapper.getNextValue(code);

        if (ObjectUtils.isEmpty(batchNoSeq)){
            currentVal = MIN_VALUE;
            nextVal = currentVal + 1;
            batchNoSeq = new BatchNoSeq();
            batchNoSeq.setCurrentDate(currDateStr);
            batchNoSeq.setCompanyCode(code);
            batchNoSeq.setCurrentValue(nextVal);
            seqNumMapper.insert(batchNoSeq);
        } else {
            Long currVal = batchNoSeq.getCurrentValue();
            if (currVal > MAX_VALUE){
                throw new RuntimeException("当天生成的十位流水号已超过最大值："+MAX_VALUE);
            }

            currentVal = currVal;
            nextVal = currVal + 1;
            batchNoSeq.setCurrentValue(nextVal);
            batchNoSeq.setCurrentDate(currDateStr);
            batchNoSeq.setCompanyCode(code);
            seqNumMapper.update(batchNoSeq);
        }
        return formatString(currentVal,code);
    }

    private String formatString(Long currVal,String code){
        String seqNo = String.format("%010d",currVal);
        return String.format("%s%s",code,seqNo);
    }




}
