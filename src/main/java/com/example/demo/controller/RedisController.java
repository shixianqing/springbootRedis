package com.example.demo.controller;


import java.util.*;
import java.util.concurrent.*;


import com.example.demo.service.SeqGenerator;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.redis.RedisService;
import org.thymeleaf.util.DateUtils;


@Controller
@RequestMapping("/redis")
@Slf4j
public class RedisController {

	@Autowired
	private RedisService redisService;

	@Autowired
    private SeqGenerator seqGenerator;
	
	@RequestMapping("/add/{id}/{name}")
	@ResponseBody
	public JSONObject add(@PathVariable String id,@PathVariable String name){
		redisService.set(id, name);
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("STATUS", "OK");
		jsonObject.put("DATE", DateUtils.format(new Date(), Locale.CHINA));
		return jsonObject;
	}
	
	@RequestMapping("/get/{key}")
	@ResponseBody
	public JSONObject get(@PathVariable String key){
		String value = redisService.get(key);
		JSONObject jsonObject = new JSONObject(true);
		jsonObject.put(key, value);
		jsonObject.put("STATUS", "OK");
		jsonObject.put("DATE", DateUtils.format(new Date(), Locale.CHINA));
		return jsonObject;
	}
	
	
	@RequestMapping("/del/{key}")
	@ResponseBody
	public JSONObject del(@PathVariable String key){
		JSONObject jsonObject = new JSONObject(true);
		redisService.del(key);
		if(!redisService.exsit(key)){
			log.debug("【key】{0}被成功删除！",key);
			jsonObject.put("STATUS", "OK");
		}else {
			jsonObject.put("STATUS", "FAIL");
		}
		jsonObject.put("KEY", key);
		jsonObject.put("DATE", DateUtils.format(new Date(), Locale.CHINA));
		return jsonObject;
	}
	
	@RequestMapping("/hset/{key}/{field}/{value}")
	@ResponseBody
	public JSONObject hset(@PathVariable Object key,@PathVariable Object field,
			@PathVariable Object value){
		redisService.hset(key, field, value);
		JSONObject jsonObject = new JSONObject(true);
		jsonObject.put("STATUS", "OK");
		jsonObject.put("DATE", DateUtils.format(new Date(), Locale.CHINA));
		return jsonObject;
	}
	
	@RequestMapping("/hset/{key}/{field}")
	@ResponseBody
	public JSONObject hget(@PathVariable Object key,@PathVariable Object field){
		Object obj = redisService.hget(key, field);
		JSONObject jsonObject = new JSONObject(true);
		jsonObject.put("KEY", key);
		jsonObject.put("FIELD", field);
		jsonObject.put("VALUE", obj);
		jsonObject.put("STATUS", "OK");
		jsonObject.put("DATE", DateUtils.format(new Date(), Locale.CHINA));
		return jsonObject;
	}
	
	@RequestMapping("/hset/{key}")
	@ResponseBody
	public Map<Object, Object> hGetAll(@PathVariable Object key){
		Map<Object, Object> map = redisService.hgetAll(key);
		map.put("KEY", key);
		map.put("STATUS", "OK");
		map.put("DATE", DateUtils.format(new Date(), Locale.CHINA));
		return map;
	}
	
	
	
	@RequestMapping("/keys/{pattern}")
	@ResponseBody
	public List<Object>  keys(@PathVariable String pattern){
		return redisService.keys(pattern);
	}


    @ResponseBody
    @GetMapping("/setNX")
	public Boolean setNX(String lockKey,String value){

	    return redisService.tryGetDistributedLock(lockKey,value,60000L);
    }

    @ResponseBody
    @GetMapping("/releaseLock")
    public Boolean releaseLock(String lockKey,String value){
	    return redisService.releaseDistributedLock(lockKey,value);
    }


    @GetMapping("/seq/{name}")
    @ResponseBody
    public void generateSeq(@PathVariable String name) throws ExecutionException, InterruptedException {
        ThreadFactory nameFactory = new ThreadFactoryBuilder().setNameFormat("seq-pool-%d").build();
        ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(5, 10, 60000L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(1024),nameFactory);
        List<String> seqList = new ArrayList<>();

        for (int i=0; i<50; i++){
            poolExecutor.execute(() -> {
                String seq = seqGenerator.genSeqCode(name);
                log.info("{}", seq);
                seqList.add(seq);
            });

        }
        poolExecutor.shutdown();

        log.info("seqList：{}",seqList);
    }
}
