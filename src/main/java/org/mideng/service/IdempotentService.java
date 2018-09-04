package org.mideng.service;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.mideng.bean.Idempotent;
import org.mideng.util.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IdempotentService{
	
	@Autowired
	private RedisService redisService;
	
	public void setCache(String key, Idempotent idempotent) {
		Map<String, Object> map = null;
		map = CommonUtils.transBean2Map(idempotent);
		redisService.putAll(key, map);
	}

	
	public Idempotent getCache(String key) {
		Idempotent idempotent = new Idempotent();
		Map<String, Object> map = redisService.entries(key);
		try {
			BeanUtils.copyProperties(idempotent, map);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return idempotent;
	}

	
	public boolean lock(String key, Idempotent idempotent) {
		boolean isCreatedLock =  redisService.lock(key, "idempotentKey", idempotent.getKey());
		setCache(key, idempotent);
		return isCreatedLock;
	}

}
