package org.mideng.action;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/test")
public class TestController {
	Logger logger = LoggerFactory.getLogger(TestController.class);

	@RequestMapping(method = {RequestMethod.POST, RequestMethod.GET}, value = "/exec/{id}")
	@ResponseBody
	public String exec(HttpServletResponse response, @PathVariable(value="id")  String id) throws Exception {
		logger.info("[exec] {}", id);
		return id + "|#|" + UUID.randomUUID();
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/exception/{id}")
	@ResponseBody
	public String exception(HttpServletResponse response, @PathVariable(value="id")  String id) throws Exception {
		logger.info("[exec] {}", id);
//		throw new Exception ("just a exception");
		Thread.sleep(5000);
		return "11";
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/redirect/{id}")
	private String redirect(HttpServletRequest request, HttpServletResponse response, @PathVariable(value="id")  String id) throws Exception {
		logger.info("[redirect] {}, {}, {}", id, request.getHeader("X-Mock-Referer"), request.getHeader("X-Real-IP"));
		return "redirect:/test/exec/" + id;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/forward/{id}")
	public String forward(HttpServletResponse response, @PathVariable(value="id")  String id) throws Exception {
		logger.info("[forward] {}", id);
		return "forward:/test/exec/" + id;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/timeout/{id}")
	@ResponseBody
//	@Loggable
	public String timeout(HttpServletResponse response, @PathVariable(value="id")  String id) throws Exception {
		logger.info("[timeout] {}", id);
		
		Thread.sleep(10000L);
		System.out.println("DONE exec4");
		return id + "||" + UUID.randomUUID();
	}
	
}
