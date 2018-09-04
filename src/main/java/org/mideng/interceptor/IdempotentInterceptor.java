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
import org.mideng.service.RedisLock;
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
	
	@Autowired
	private RedisLock redisLock;

	//在请求到方法前触发
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		String requestId = request.getHeader(Constants.REQ_IDEM_ID);
		DispatcherType dispatcherType = request.getDispatcherType();
		String method = request.getMethod().toUpperCase();
		logger.info("[preHandle] url:{}, requestId:{}, method:{}, dispatcherType:{}", request.getRequestURI(), requestId, method, dispatcherType);
		if (requestId == null)  return true;//不包含requestId的不进行幂等处理, 一般 ("GET".equals(method))是查询数据也不需要

		Idempotent idempotent = null;
		try {// 如果redis出现连接异常。所有的幂等操作全部取消
			idempotent = idempotentService.getCache(requestId);
		} catch (Exception e) {
			//redis异常无法提供幂等性操作，直接返回
			return false;
		}
		
		//判断请求是否重复提交，如果是且已经处理完毕，直接返回上次的请求结果
		if (null != idempotent.getKey() && Idempotent.STATUS_FINISIED.equals(idempotent.getStatus())) {
			doReponse(response, idempotent);
			return false;//中断后续处理
		}

		//用户首次请求直接执行或者重复请求处理结果未完成重新获取锁重新执行
		boolean isCreatedLock = redisLock.lock(requestId, System.currentTimeMillis() + 60000 + "");//获得60秒的锁
		if (!isCreatedLock) {//未获取到锁说明被其他服务器或者线程抢到在正在处理，直接返回前端等待处理
			inProcessResp(response);
			return false;
		}
		idempotent = new Idempotent(requestId, Idempotent.STATUS_START, null, null, null);
		idempotentService.setCache(requestId, idempotent);//获得锁保存为了不同服务器共享
		IdempotentHolder.setIdempotentVo(idempotent);

//		if (DispatcherType.ERROR.equals(dispatcherType)) {//本地保存为了本地forward跳转用
//			return true;
//		} 
//		
//		if (DispatcherType.FORWARD.equals(dispatcherType)) {
//			return true;
//		}
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
		if (idempotent == null) return;
		if (idempotent.getKey() == null) {
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
	
	
	//将当前结果返回给前端
	private void doReponse(HttpServletResponse response, Idempotent idempotent) {
		response.setStatus(idempotent.getStatusCode());
		try {
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
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
