package com.example.demo.controller;


import java.util.*;
import java.util.concurrent.*;


import com.example.demo.common.response.ResponseVo;
import com.example.demo.service.SeqGenerator;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.redis.RedisService;
import org.thymeleaf.util.DateUtils;


/**
 * @author shixianqing
 */
@RestController
@RequestMapping("/redis")
@Slf4j
public class RedisController {

	@Autowired
	private RedisService redisService;

	@Autowired
    private SeqGenerator seqGenerator;
	
	@RequestMapping("/add/{id}/{name}")
	public ResponseVo add(@PathVariable String id,@PathVariable String name){
		redisService.set(id, name);
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("STATUS", "OK");
		jsonObject.put("DATE", DateUtils.format(new Date(), Locale.CHINA));
		return ResponseVo.success(jsonObject);
	}
	
	@RequestMapping("/get/{key}")
	public ResponseVo get(@PathVariable String key){
		String value = redisService.get(key);
		return ResponseVo.success(value);
	}
	
	
	@RequestMapping("/del/{key}")
	public ResponseVo del(@PathVariable String key){
        redisService.del(key);
		if(!redisService.exsit(key)){
            return ResponseVo.success(key+"：删除成功");
		}else {
            return ResponseVo.error(key+"：删除失败");
		}
	}
	
	@RequestMapping("/hset/{key}/{field}/{value}")
	public ResponseVo hset(@PathVariable Object key,@PathVariable Object field,
			@PathVariable Object value){
		redisService.hset(key, field, value);

		return ResponseVo.success("key："+key+"，field："+field+"，" +
                "value："+value+"，设置成功");
	}
	
	@RequestMapping("/hset/{key}/{field}")
	public ResponseVo hget(@PathVariable Object key,@PathVariable Object field){
		Object obj = redisService.hget(key, field);
		JSONObject jsonObject = new JSONObject(true);
		jsonObject.put("KEY", key);
		jsonObject.put("FIELD", field);
		jsonObject.put("VALUE", obj);
		jsonObject.put("STATUS", "OK");
		jsonObject.put("DATE", DateUtils.format(new Date(), Locale.CHINA));
		return ResponseVo.success(jsonObject);
	}
	
	@RequestMapping("/hset/{key}")
	public ResponseVo hGetAll(@PathVariable Object key){
		Map<Object, Object> map = redisService.hgetAll(key);
		map.put("KEY", key);
		map.put("STATUS", "OK");
		map.put("DATE", DateUtils.format(new Date(), Locale.CHINA));
		return ResponseVo.success(map);
	}
	
	
	
	@RequestMapping("/keys/{pattern}")
	public ResponseVo keys(@PathVariable String pattern){
		return ResponseVo.success(redisService.keys(pattern));
	}


    /**
     * 设置锁
     * @param lockKey
     * @param value
     * @return
     */
    @GetMapping("/setNX")
	public Boolean setNX(String lockKey,String value){

	    return redisService.tryGetDistributedLock(lockKey,value,60000L);
    }

    /**
     * 释放锁
     * @param lockKey
     * @param value
     * @return
     */
    @GetMapping("/releaseLock")
    public Boolean releaseLock(String lockKey,String value){
	    return redisService.releaseDistributedLock(lockKey,value);
    }


    /**
     * 实现分布式锁案例
     * @param name
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @GetMapping("/seq/{name}")
    public void generateSeq(@PathVariable String name) throws ExecutionException, InterruptedException {
        ThreadFactory nameFactory = new ThreadFactoryBuilder().setNameFormat("seq-pool-%d").build();
        ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(5, 10, 60000L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(1024),nameFactory);

        for (int i=0; i<50; i++){
            poolExecutor.execute(() -> {
                String seq = seqGenerator.genSeqCode(name);
                log.info("{}", seq);
            });

        }
        poolExecutor.shutdown();

    }
}
