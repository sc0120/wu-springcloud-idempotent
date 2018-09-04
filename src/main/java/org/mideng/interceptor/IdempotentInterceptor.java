package org.mideng.interceptor;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mideng.bean.Idempotent;
import org.mideng.service.IdempotentHolder;
import org.mideng.service.IdempotentService;
import org.mideng.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

@Component
public class IdempotentInterceptor extends HandlerInterceptorAdapter {

	private static final Logger logger = LoggerFactory.getLogger(IdempotentInterceptor.class);

	private static final Integer HTTP_CODE_IDEMPOTENT_FAIL = 499;
	
	@Autowired
	private IdempotentService idempotentService;

	//在请求到方法前出发
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		String requestId = request.getHeader(Constants.REQ_IDEM_ID);
		DispatcherType dispatcherType = request.getDispatcherType();
		String method = request.getMethod().toUpperCase();
		logger.info("[preHandle] url:{}, requestId:{}, method:{}, dispatcherType:{}", request.getRequestURI(), requestId, method, dispatcherType);

		if (requestId == null || ("GET".equals(method))) {
			return true;
		}

		Idempotent idempotent = null;
		// 如果redis出现连接异常。所有的幂等操作全部取消
		try {
			idempotent = idempotentService.getCache(requestId);
		} catch (Exception e) {
			return true;
		}

		if (idempotent.getKey() == null) {
			idempotent = new Idempotent();
			idempotent.setKey(requestId);
			idempotent.setStatus(Idempotent.STATUS_START);

			boolean isCreatedLock = idempotentService.lock(requestId, idempotent);
			
			if (!isCreatedLock) {
				inProcessResp(response);
				return false;
			}
			
			IdempotentHolder.setIdempotentVo(idempotent);
			return true;
		}
		if (DispatcherType.REQUEST.equals(dispatcherType)) {
			if (Idempotent.STATUS_FINISIED.equals(idempotent.getStatus())) {
				response.setStatus(idempotent.getStatusCode());
				if (null != idempotent.getResult()) {
					response.getOutputStream().write(idempotent.getResult().getBytes());
				}
				if (null != idempotent.getHeaders()) {
					Map<String, String> headers = idempotent.getHeaders();
					for (String name : headers.keySet()) {
						response.setHeader(name, headers.get(name));
					}
				}

				response.getOutputStream().flush();
				return false;
			} else if (Idempotent.STATUS_REDIRECT.equals(idempotent.getStatus())) {
				// 除get请求外。其它的请求默认不支持redirect。暂时保留这块。预防以后需要支持get的操作
				logger.debug("[preHandle] reffer to a redirect request");
				IdempotentHolder.setIdempotentVo(idempotent);
				return true;
			} else {// 如果不为FINISHED状态。说明正在进展中
				inProcessResp(response);
				return false;
			}

		} else if (DispatcherType.ERROR.equals(dispatcherType)) {
			IdempotentHolder.setIdempotentVo(idempotent);
			return true;
		} else if (DispatcherType.FORWARD.equals(dispatcherType)) {
			IdempotentHolder.setIdempotentVo(idempotent);
			return true;
		}
		return true;
	}

	/**
	 * 在业务处理器处理请求执行完成后,生成视图之前执行的动作 可在modelAndView中加入数据，比如当前时间
	 */
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		String requestUri = request.getRequestURI();
		logger.info("[postHandle] {}, {}", requestUri, response.getStatus());

		// System.out.println("[postHandle]:" + request.getRequestURI() + "##" +
		// request.getHeader("X-SN-REQUEST-ID"));
	}

	/**
	 * 在视图解析完毕后被调用,可用于清理资源等
	 * 当有拦截器抛出异常时,会从当前拦截器往回执行所有的拦截器的afterCompletion()
	 */
	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		String requestUri = request.getRequestURI();
		int respStatus = response.getStatus();
		logger.info("[afterCompletion] {}, {}，{}", requestUri, respStatus, ex == null);

		Idempotent idempotent = IdempotentHolder.getIdempotentVo();

		if (idempotent == null || idempotent.getKey() == null) {
			IdempotentHolder.clear();
			return;
		}

		String idempotentKey = idempotent.getKey();
		// 重定向
		if (respStatus >= 300 && respStatus < 400) {
			logger.info("[afterCompletion] a redirect , httpStatusCode:{}", respStatus);
			idempotent.setStatus(Idempotent.STATUS_REDIRECT);
			idempotentService.setCache(idempotentKey, idempotent);
		}

		if (Idempotent.STATUS_FINISIED.equals(idempotent.getStatus())) {
			idempotent.setStatusCode(response.getStatus());
			Collection<String> headerNames = response.getHeaderNames();
			if (headerNames != null && headerNames.size() != 0) {
				Map<String, String> headers = new HashMap<>();
				for (String name : headerNames) {
					if (name.equals("Date") || name.equals("Connection") || name.equals("Transfer-Encoding")
							|| name.equals("X-Application-Context")) {
						continue;
					}
					headers.put(name, response.getHeader(name));
				}
				idempotent.setHeaders(headers);
			}
			idempotentService.setCache(idempotentKey, idempotent);
		}
		IdempotentHolder.clear();
	}
	
	
	private void inProcessResp(HttpServletResponse response) throws IOException {
		response.setStatus(HTTP_CODE_IDEMPOTENT_FAIL);
		response.getOutputStream().flush();
	}
}
