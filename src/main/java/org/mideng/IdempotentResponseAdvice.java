package org.mideng;

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


//@ControllerAdvice(basePackages = "com.example.intecepter")
@ControllerAdvice
public class IdempotentResponseAdvice implements ResponseBodyAdvice<Object> {

	private static final Logger logger = LoggerFactory.getLogger(IdempotentResponseAdvice.class);
	
	@Autowired
	private IdempotentService idempotentService;
	
	@Override
	public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
			Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
			ServerHttpResponse response) {
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

	
	@Override
	public boolean supports(MethodParameter arg0, Class<? extends HttpMessageConverter<?>> arg1) {
		logger.debug("[supports] {}, IdempotentHolder.getIdempotentVo:{}", arg0.getMember().getName(),
				IdempotentHolder.getIdempotentVo());
		if (null == IdempotentHolder.getIdempotentVo() || null == IdempotentHolder.getIdempotentVo().getKey()) {
			return false;
		} else {
			return true;
		}
		
//		if (IdempotentHolder.STATUS_BEGIN.equals(IdempotentHolder.getStatus()) 
//				|| IdempotentHolder.STATUS_REDIRECT.equals(IdempotentHolder.getStatus())) {
//			return true;
//		} else {
//			return false;
//		}
	}

}
