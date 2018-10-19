package com.example.demo.controller;


import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.redis.RedisService;
import com.example.demo.table.Student;
import com.study.util.DateUtil;
import com.study.util.LoggerUtil;


@Controller
@RequestMapping("/redis")
public class RedisController {

	private LoggerUtil logger = LoggerUtil.getInstance(getClass());
	
	@Autowired
	private RedisService redisService;
	
	@RequestMapping("/add/{id}/{name}")
	@ResponseBody
	public JSONObject add(@PathVariable String id,@PathVariable String name){
		redisService.set(id, name);
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("STATUS", "OK");
		jsonObject.put("DATE", DateUtil.fmtDateToStr());
		return jsonObject;
	}
	
	@RequestMapping("/get/{key}")
	@ResponseBody
	public JSONObject get(@PathVariable String key){
		String value = redisService.get(key);
		JSONObject jsonObject = new JSONObject(true);
		jsonObject.put(key, value);
		jsonObject.put("STATUS", "OK");
		jsonObject.put("DATE", DateUtil.fmtDateToStr());
		return jsonObject;
	}
	
	
	@RequestMapping("/del/{key}")
	@ResponseBody
	public JSONObject del(@PathVariable String key){
		JSONObject jsonObject = new JSONObject(true);
		redisService.del(key);
		if(!redisService.exsit(key)){
			logger.debug("【key】{0}被成功删除！",key);
			jsonObject.put("STATUS", "OK");
		}else {
			jsonObject.put("STATUS", "FAIL");
		}
		jsonObject.put("KEY", key);
		jsonObject.put("DATE", DateUtil.fmtDateToStr());
		return jsonObject;
	}
	
	@RequestMapping("/hset/{key}/{field}/{value}")
	@ResponseBody
	public JSONObject hset(@PathVariable Object key,@PathVariable Object field,
			@PathVariable Object value){
		redisService.hset(key, field, value);
		JSONObject jsonObject = new JSONObject(true);
		jsonObject.put("STATUS", "OK");
		jsonObject.put("DATE", DateUtil.fmtDateToStr());
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
		jsonObject.put("DATE", DateUtil.fmtDateToStr());
		return jsonObject;
	}
	
	@RequestMapping("/hset/{key}")
	@ResponseBody
	public Map<Object, Object> hGetAll(@PathVariable Object key){
		Map<Object, Object> map = redisService.hgetAll(key);
		map.put("KEY", key);
		map.put("STATUS", "OK");
		map.put("DATE", DateUtil.fmtDateToStr());
		return map;
	}
	
	
	
	@RequestMapping("/keys/{pattern}")
	@ResponseBody
	public List<Object>  keys(@PathVariable String pattern){
		return redisService.keys(pattern);
	}
	
	
}
