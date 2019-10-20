package com.example.demo.common.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * 对象序列化与反序列化工具类
 * @author sxq
 * @time 2018年7月4日 下午2:07:41
 *
 */
public class SerializeUtil {

	/**
	 * 序列化
	 *	@param obj
	 *	@return
	 *	byte[]
	 */
	public static byte[] serialize(Object obj){
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(obj);
			return bos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * 反序列化
	 *	@param bytes
	 *	@return
	 *	Object
	 * @throws Exception 
	 */
	public static Object unSerialize(byte[] bytes) throws Exception{
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		try {
			ObjectInputStream inputStream = new ObjectInputStream(bis);
			return inputStream.readObject();
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
	}
}
