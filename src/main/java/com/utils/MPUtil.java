package com.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import cn.hutool.core.bean.BeanUtil;

import com.baomidou.mybatisplus.mapper.Wrapper;

/**
 * Mybatis-Plus工具类
 */
public class MPUtil {
	public static final char UNDERLINE = '_';

	
	//mybatis plus allEQ 表达式转换
	//将bean转换为Map，并将Map中的key从驼峰命名法转换为下划线命名法,并加上前缀pre。
		public static Map allEQMapPre(Object bean,String pre) {
		   Map<String, Object> map =BeanUtil.beanToMap(bean);
		  return camelToUnderlineMap(map,pre);
	   }

		//mybatis plus allEQ 表达式转换
		//将bean转换为Map，并将Map中的key从驼峰命名法转换为下划线命名法
		public static Map allEQMap(Object bean) {
		   Map<String, Object> map =BeanUtil.beanToMap(bean);
		   return camelToUnderlineMap(map,"");
	   }

		//将bean转换为Map，key转换为下划线命名法并加前缀，然后生成like条件
		public static Wrapper allLikePre(Wrapper wrapper,Object bean,String pre) {
			   Map<String, Object> map =BeanUtil.beanToMap(bean);
			   Map result = camelToUnderlineMap(map,pre);
			 
			return genLike(wrapper,result);
		}

	//将bean转换为Map，生成like条件。
		public static Wrapper allLike(Wrapper wrapper,Object bean) {
			  Map result = BeanUtil.beanToMap(bean, true, true);			 
			return genLike(wrapper,result);
		}


	//遍历Map，将每个key-value对生成like条件，并将其添加到wrapper中。
		public static Wrapper genLike( Wrapper wrapper,Map param) {
			Iterator<Map.Entry<String, Object>> it = param.entrySet().iterator();
			int i=0;
			while (it.hasNext()) {
				if(i>0) wrapper.and();
				Map.Entry<String, Object> entry = it.next();
				String key = entry.getKey();
				String value = (String) entry.getValue();
				wrapper.like(key, value);
				i++;
			}
			return wrapper;
		}

	//将bean转换为Map，生成like或eq条件。
		public static Wrapper likeOrEq(Wrapper wrapper,Object bean) {
			  Map result = BeanUtil.beanToMap(bean, true, true);			 
			return genLikeOrEq(wrapper,result);
		}

	//genLikeOrEq：遍历Map，如果值包含%，生成like条件，否则生成eq条件。
		public static Wrapper genLikeOrEq( Wrapper wrapper,Map param) {
			Iterator<Map.Entry<String, Object>> it = param.entrySet().iterator();
			int i=0;
			while (it.hasNext()) {
				if(i>0) wrapper.and();
				Map.Entry<String, Object> entry = it.next();
				String key = entry.getKey();
				if(entry.getValue().toString().contains("%")) {
					wrapper.like(key, entry.getValue().toString().replace("%", ""));
				} else {
					wrapper.eq(key, entry.getValue());
				}
				i++;
			}
			return wrapper;
		}

		//allEq：将bean转换为Map，生成eq条件。
		public static Wrapper allEq(Wrapper wrapper,Object bean) {
			  Map result = BeanUtil.beanToMap(bean, true, true);			 
			return genEq(wrapper,result);
		}
	
			//genEq：遍历Map，为每个key-value对生成eq条件。
		public static Wrapper genEq( Wrapper wrapper,Map param) {
			Iterator<Map.Entry<String, Object>> it = param.entrySet().iterator();
			int i=0;
			while (it.hasNext()) {
				if(i>0) wrapper.and();
				Map.Entry<String, Object> entry = it.next();
				String key = entry.getKey();
				wrapper.eq(key, entry.getValue());
				i++;
			}
			return wrapper;
		}

	//处理区间查询条件。如果key以_start结尾，生成ge条件；如果key以_end结尾，生成le条件。
		public static Wrapper between(Wrapper wrapper,Map<String, Object> params) {
			for(String key : params.keySet()) {
				String columnName = "";
				if(key.endsWith("_start")) {
					columnName = key.substring(0, key.indexOf("_start"));
					if(StringUtils.isNotBlank(params.get(key).toString())) {
						wrapper.ge(columnName, params.get(key));
					}
				}
				if(key.endsWith("_end")) {
					columnName = key.substring(0, key.indexOf("_end"));
					if(StringUtils.isNotBlank(params.get(key).toString())) {
						wrapper.le(columnName, params.get(key));
					}
				}
			}
			return wrapper;
		}

	//处理排序条件，根据params中的order和sort字段设置排序顺序。
		public static Wrapper sort(Wrapper wrapper,Map<String, Object> params) {
			String order = "";
			if(params.get("order") != null && StringUtils.isNotBlank(params.get("order").toString())) {
				order = params.get("order").toString();
			}
			if(params.get("sort") != null && StringUtils.isNotBlank(params.get("sort").toString())) {
				if(order.equalsIgnoreCase("desc")) {
					wrapper.orderDesc(Arrays.asList(params.get("sort")));
				} else {
					wrapper.orderAsc(Arrays.asList(params.get("sort")));
				}
			}
			return wrapper;
		}
	
	
	/**
	 * 驼峰格式字符串转换为下划线格式字符串
	 * 
	 * @param param
	 * @return
	 */
	public static String camelToUnderline(String param) {
		if (param == null || "".equals(param.trim())) {
			return "";
		}
		int len = param.length();
		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++) {
			char c = param.charAt(i);
			if (Character.isUpperCase(c)) {
				sb.append(UNDERLINE);
				sb.append(Character.toLowerCase(c));
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	public static void main(String[] ages) {
		System.out.println(camelToUnderline("ABCddfANM"));
	}
	
	public static Map camelToUnderlineMap(Map param, String pre) {

		Map<String, Object> newMap = new HashMap<String, Object>();
		Iterator<Map.Entry<String, Object>> it = param.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Object> entry = it.next();
			String key = entry.getKey();
			String newKey = camelToUnderline(key);
			if (pre.endsWith(".")) {
				newMap.put(pre + newKey, entry.getValue());
			} else if (StringUtils.isEmpty(pre)) {
				newMap.put(newKey, entry.getValue());
			} else {

				newMap.put(pre + "." + newKey, entry.getValue());
			}
		}
		return newMap;
	}
}
