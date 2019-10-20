package com.example.demo.redis;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.stereotype.Component;

import com.example.demo.ns.Consts;
import com.example.demo.common.utils.SerializeUtil;

@Component
public class RedisService {

	@Autowired
	private RedisTemplate redisTemplate;
	
	@SuppressWarnings("unchecked")
	public void set(final byte[] key,final byte[] value,final long liveTime){
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection connection)
					throws DataAccessException {
				
				connection.set(key, value);
				if(liveTime!=0){
					connection.expire(key, liveTime);
				}
				return 1L;
			}
			
		});
	}
	
	public void set(String key,String value){
		set(key, value,Consts.TIME_OUT);
	}
	
	public void set(String key,String value,long liveTime){
		set(key.getBytes(), value.getBytes(),Consts.TIME_OUT);
	}
	
	public String get(String key){
		return get(key.getBytes());
	}
	
	@SuppressWarnings("unchecked")
	public String get(final byte[] key){
		return (String) redisTemplate.execute(new RedisCallback<String>() {

			@Override
			public String doInRedis(RedisConnection connection)
					throws DataAccessException {
				if(connection.exists(key)){
					try {
						return new String(connection.get(key),"utf-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				};
				return null;
			}
			
		});
	}

	@SuppressWarnings("unchecked")
	public List<Long> del(String... key) {
		List<Long> results = new ArrayList<>();
		redisTemplate.execute(new RedisCallback<Object>() {

			@Override
			public Object doInRedis(RedisConnection connection)
					throws DataAccessException {
				for(int i=0;i<key.length;i++){
					Long result = connection.del(key[i].getBytes());
					results.add(result);
				}
				return 1L;
			}
			
		});

		return results;
	}
	
	@SuppressWarnings("unchecked")
	public boolean exsit(String key){
		return (boolean) redisTemplate.execute(new RedisCallback<Object>() {

			@Override
			public Object doInRedis(RedisConnection connection)
					throws DataAccessException {
				return connection.exists(key.getBytes());
			}
			
		});
	}
	
	
	@SuppressWarnings("unchecked")
	public void hset(final byte[] key,final byte[] field,final byte[] value){
		redisTemplate.execute(new RedisCallback<Object>() {

			@Override
			public Object doInRedis(RedisConnection connection)
					throws DataAccessException {
				return connection.hSet(key, field, value);
			}
			
		});
	}
	
	/**
	 * 设置hash值
	 *	@param key
	 *	@param field
	 *	@param value
	 *	void
	 */
	public void hset(Object key,Object field,Object value){
		hset(SerializeUtil.serialize(key), SerializeUtil.serialize(field), SerializeUtil.serialize(value));
	}
	
	@SuppressWarnings("unchecked")
	public Object hget(Object key,Object field){
		return redisTemplate.execute(new RedisCallback<Object>() {

			@Override
			public Object doInRedis(RedisConnection connection)
					throws DataAccessException {
				byte[] values = connection.hGet(SerializeUtil.serialize(key), SerializeUtil.serialize(field));
				
				try {
					return SerializeUtil.unSerialize(values);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
			
		});
	}
	
	
	/**
	 * 根据key，获取以field为key，以value为值的集合
	 *	@param key
	 *	@return
	 *	Map<Object,Object>
	 */
	@SuppressWarnings("unchecked")
	public Map<Object,Object> hgetAll(Object key){
		return (Map<Object, Object>) redisTemplate.execute(new RedisCallback<Object>() {

			@Override
			public Object doInRedis(RedisConnection connection)
					throws DataAccessException {
				Map<Object, Object> result = new HashMap<Object, Object>();
				Map<byte[], byte[]>  map = connection.hGetAll(SerializeUtil.serialize(key));
				Set<byte[]> keySet = map.keySet();
				Iterator<byte[]> it =  keySet.iterator();
				while(it.hasNext()){
					byte[] key = it.next();
					try {
						result.put(SerializeUtil.unSerialize(key), SerializeUtil.unSerialize(map.get(key)));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				return result;
			}
			
		});
	}
	
	
	@SuppressWarnings("unchecked")
	public List<Object> keys(String pattern){
		return (List<Object>) redisTemplate.execute(new RedisCallback<Object>() {

			@Override
			public Object doInRedis(RedisConnection connection)
					throws DataAccessException {
				List<Object> list = new ArrayList<Object>();
				Set<byte[]> set = connection.keys(pattern.getBytes());
				Iterator<byte[]> it = set.iterator();
				while(it.hasNext()){
					byte[] by = it.next();
					try {
						//设置hash的key，读取时，需要反序列化
						Object key = SerializeUtil.unSerialize(by);
						list.add(key);
					} catch (Exception e) {
						try {
							list.add(new String(by,"utf-8"));
						} catch (UnsupportedEncodingException e1) {
							e1.printStackTrace();
						}
						e.printStackTrace();
					}
					
				}
				
				return list;
			}
			
		});
	}


	/**
	 *
	 * @param key
	 * @param value
	 * @param expireTime 毫秒
	 * @return
	 */
	public boolean tryGetDistributedLock(String key,String value,Long expireTime){

		return (boolean) redisTemplate.execute((RedisCallback<Boolean>) redisConnection -> {
			Boolean isSucess = redisConnection.set(key.getBytes(),value.getBytes(),
					Expiration.milliseconds(expireTime),
					RedisStringCommands.SetOption.SET_IF_ABSENT);
			return isSucess;
		});

	}


	/**
	 * 释放锁
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean releaseDistributedLock(String key,String value){
		String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return " +
				"0 end";
		Long result =
				(Long) redisTemplate.execute((RedisCallback<Long>) redisConnection ->redisConnection.eval(script.getBytes(),
				ReturnType.INTEGER,1,
				key.getBytes(),value.getBytes())
		);

		return 0 == result ? false : true;
	}
}

