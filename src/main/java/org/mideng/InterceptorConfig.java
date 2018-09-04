package org.mideng;

import org.mideng.interceptor.IdempotentInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 *  拦截器配置
 * @author xiaoxuwu
 */
@Configuration
@ComponentScan(basePackages = { "org.mideng" })
public class InterceptorConfig extends WebMvcConfigurerAdapter {

	@Autowired
	private IdempotentInterceptor idempotentInterceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(idempotentInterceptor).addPathPatterns("/**");
		super.addInterceptors(registry);
	}
}

