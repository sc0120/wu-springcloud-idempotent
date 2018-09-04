package org.mideng.interceptor;

import java.util.Map;

import org.mideng.bean.Idempotent;
import org.mideng.service.IdempotentHolder;
import org.mideng.service.IdempotentService;
import org.mideng.util.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;


/**
 * 请求返回值增强，用于修改接口的返回值
 * @author xiaoxuwu
 *ResponseBodyAdvice 只支持 @ResponseBody 注解的 controller 方法
 *RequestBodyAdvice 只支持带有 @RequestBody 注解的 controller 方法参数的方法，同时上报的数据必须是 json or xml
 */
@ControllerAdvice
public class IdempotentResponseAdvice implements ResponseBodyAdvice<Object> {

	private static final Logger logger = LoggerFactory.getLogger(IdempotentResponseAdvice.class);
	
	@Autowired
	private IdempotentService idempotentService;
	
	
	/**
	 * 判断是否是支持的类型
	 */
	@Override
	public boolean supports(MethodParameter arg0, Class<? extends HttpMessageConverter<?>> arg1) {
		logger.debug("[supports] {}, IdempotentHolder.getIdempotentVo:{}", arg0.getMember().getName(), IdempotentHolder.getIdempotentVo());
		if (null == IdempotentHolder.getIdempotentVo() || null == IdempotentHolder.getIdempotentVo().getKey()) {
			return false;
		} else {
			return true;
		}
	}
	
	
	/**
	 * 对返回结果进行处理
	 */
	@Override
	public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
			Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
		logger.debug("[beforeBodyWrite] begin {}", body);

		String idempotentKey = IdempotentHolder.getIdempotentVo().getKey();
		Idempotent idempotent = IdempotentHolder.getIdempotentVo();

		if (body instanceof Map) {
			JsonMapper jsonMapper = new JsonMapper();
			idempotent.setResult(jsonMapper.toJson(body));
		} else {
			idempotent.setResult(body.toString());
		}
		idempotent.setStatus(Idempotent.STATUS_FINISIED);
		idempotentService.setCache(idempotentKey, idempotent);
		IdempotentHolder.setIdempotentVo(idempotent);
		logger.debug("[beforeBodyWrite] end");
		return body;
	}

}
