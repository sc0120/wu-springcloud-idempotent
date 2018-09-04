package org.mideng;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * 
 * @author lichengzhou
 *
 */
@Configuration
@ComponentScan(basePackages = { "org.mideng" })
public class Configurations extends WebMvcConfigurerAdapter {

	@Autowired
	private IdempotentInterceptor idempotentInterceptor;
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(idempotentInterceptor).addPathPatterns("/**");
		super.addInterceptors(registry);
	}
	
}
