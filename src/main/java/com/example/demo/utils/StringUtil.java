package com.example.demo.utils;

import com.example.demo.table.Student;

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 字符串工具类
 * @author sxq
 * @time 2018年6月15日 下午5:24:19
 *
 */
public class StringUtil {

	
	public static boolean isEmpty(Object val){
		if(val instanceof String){
			return "".equals(val)||val==null||"null".equalsIgnoreCase((String) val);
		} else if(val instanceof Map){
			return val==null||((Map) val).isEmpty();
		} else if(val instanceof List){
			
			return val == null || ((List)val).size()==0;
		}else {
			return "".equals(val)||val==null;
		}
		
	}
	
	public static String getString(Object args){
		
		return String.valueOf(args);
	}
	
	public static String getString(Object args,String defaultVal){
		if(isEmpty(args)){
			return defaultVal;
		}else{
			return String.valueOf(args);
		}
		
	}
	
	public static String getString(Map map, Object key,String defaultVal){
		if(isEmpty(map)){ 
			return defaultVal;
		}else if(isEmpty(map.get(key))){
			return defaultVal;
		}else {
			return getString(map.get(key));
		}
		
	}
	
	public static int getInt(Map map,Object key,int defaultVal){
		if(isEmpty(map)){ 
			return defaultVal;
		}else if(isEmpty(map.get(key))){
			return defaultVal;
		}else {
			return getInt(map.get(key),defaultVal);
		}
	}
	public static int getInt(Object val,int defaultVal){
		if(isEmpty(val)){
			return defaultVal;
		}else{
			return Integer.valueOf(getString(val));
		}
	}
	
	/**
	 * 实体类转map
	 *	@return
	 *	Map
	 */
	public static <T> Map entityConvertMap(Object object){
		Map map = new HashMap();
		Class<?> clazz = object.getClass();
		Field[] fields = clazz.getDeclaredFields();
		try {
			for(Field field:fields){
				field.setAccessible(true);
				map.put(field.getName(), field.get(object));
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return map;
	}
	
	/**
	 * map转换实体类
	 *	@param map
	 *	@param clazz
	 *	@return
	 *	T
	 */
	public static <T> T mapConvertEntity(Map map,Class<T> clazz){
		T obj = null;
		try {
			obj = clazz.newInstance();
			Field[] fields = clazz.getDeclaredFields();
			for(Field field:fields){
				field.setAccessible(true);
				field.set(obj, map.get(field.getName()));
			}
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		return obj;
	}
	
	
	public static String formatString(String tpl,Object... args){
		return MessageFormat.format(tpl, args);
	}
	
	
	
	
//	public static void main(String[] args) {
//		Map<String, Object> map = new HashMap<String, Object>();
//		map.put("name", "张三");
//		map.put("age", 12);
//		map.put("job", "java开发工程师");
////		map.put("name", "占山");
//		System.out.println(mapConvertEntity(map, Student.class).toString());
//
//		Student student = new Student("李思思", 25, "主持人");
//		System.out.println(entityConvertMap(student));
//	}
}
