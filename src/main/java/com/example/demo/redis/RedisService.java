package com.example.demo.redis;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

import com.example.demo.ns.Consts;
import com.example.demo.common.utils.SerializeUtil;

@Component
@Slf4j
public class RedisService {

	@Autowired
	private RedisTemplate redisTemplate;
	
	@SuppressWarnings("unchecked")
	public void set(final byte[] key,final byte[] value,final long liveTime){
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection connection)
					throws DataAccessException {
				
				connection.set(key,value,Expiration.seconds(liveTime),
						RedisStringCommands.SetOption.SET_IF_PRESENT);
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
					Long result = connection.del(redisTemplate.getKeySerializer().serialize(key[i]));
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
     * 多变量设置
     * @param key
     * @param map
     */
	public Boolean hmset(final Object key,Map<Object,Object> map){

	    redisTemplate.opsForHash().putAll(key,map);
	    return true;
    }


    /**
     * 设置key自增长id
     * @param key
     * @return
     */
    public Long incrby(Object key){

        Long increment = redisTemplate.opsForValue().increment(key);
        return increment;
    }

    public Boolean sadd(Object key,Object... member){
        Long add = redisTemplate.opsForSet().add(key, member);
        return add > 0 ? true : false;
    }

    /**
     * 获取指定key下的所有成员
     * @param key
     * @return
     */
    public Set<Integer> smembers(Object key){

        Set<Integer> members = redisTemplate.opsForSet().members(key);
        return members;
    }


	/**
	 * 设置hash值
	 *	@param key
	 *	@param field
	 *	@param value
	 *	void
	 */
	public void hset(Object key,Object field,Object value){
		redisTemplate.opsForHash().put(key,field,value);
	}


	
	@SuppressWarnings("unchecked")
	public Object hget(Object key,Object field){
		return redisTemplate.opsForHash().get(key,field);
	}

	
	/**
	 * 根据key，获取以field为key，以value为值的集合
	 *	@param key
	 *	@return
	 *	Map<Object,Object>
	 */
	@SuppressWarnings("unchecked")
	public Map<Object,Object> hgetAll(Object key){
        Map entries = redisTemplate.opsForHash().entries(key);
        return entries;
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


	public List eval(String script,int numkeys,List<Object> keysAndArgs){
	    byte[][] bytes = new byte[keysAndArgs.size()][keysAndArgs.size()];
        for (int i = 0; i < keysAndArgs.size(); i++){
            bytes[i] = redisTemplate.getKeySerializer().serialize(keysAndArgs.get(i));


        }

       return (List) redisTemplate.execute((RedisCallback<List>) connection -> {
           List<Object> results = connection.eval(script.getBytes(), ReturnType.MULTI, numkeys, bytes);
           return results;
       });
    }

    public Boolean zadd(Object key,double score,Object member){


	    return redisTemplate.opsForZSet().add(key,member,score);
    }

    /**
     * 获取分数
     * @param key
     * @param member
     * @return
     */
    public Double zscore(Object key,Object member){
        return redisTemplate.opsForZSet().score(key,member);
    }

    public Long hincrby(Object key,Object field,Long data){
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        Long increment = redisTemplate.opsForHash().increment(key, field, data);
//        return (Double) redisTemplate.execute((RedisCallback<Double>) connection -> {
//            Double result = connection.hIncrBy(SerializeUtil.serialize(key),
//                    SerializeUtil.serialize(field), data);
//            return result;
//        });

        redisTemplate.setKeySerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        return increment;

    }


    /**
     * 降序 获取指定key对应的成员
     * @param key
     * @param <T>
     * @return
     */
    public <T> Set<T> zrevrange(Object key){
		return redisTemplate.opsForZSet().reverseRange(key, 0, -1);
    }

	/**
	 * 正序获取成员
	 * @param key
	 * @param <T>
	 * @return
	 */
	public <T> Set<T> zrange (Object key){
        return redisTemplate.opsForZSet().range(key,0,-1);
    }


    public Double zincrby(Object key,Object member,double incriment){
        return redisTemplate.opsForZSet().incrementScore(key, member, incriment);
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



	public Long lpush(Object key,Collection values){

        Long aLong = redisTemplate.opsForList().leftPushAll(key, values);

        return aLong;
    }
}

