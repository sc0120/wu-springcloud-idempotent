package org.mideng.test;

import org.junit.FixMethodOrder;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mideng.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringApplicationConfiguration(classes = IdempotentInterceptorTests.Application.class)
//@WebAppConfiguration
//@IntegrationTest({"server.port=0"})
//@DirtiesContext
@ContextConfiguration
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes=Application.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)

//启动IdempotentTestApplication的配置
@Configuration
@EnableAutoConfiguration
public class TimeoutCallTest {
	
	private static final Logger logger = LoggerFactory.getLogger(TimeoutCallTest.class);

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	Environment environment;
	
//	@Test
//	public void timeoutCall() {
//		String port = environment.getProperty("local.server.port");
//		environment.getActiveProfiles();
//		String requestId = "execDiffrentCall_:"+UUID.randomUUID();
////		TestRestTemplate restTemplate = customRestTemplate();
//		MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
//		headers.add(Constants.REQ_IDEM_ID, requestId);
//		headers.add("Content-Type", "application/json");
//		HttpEntity requests = new HttpEntity(headers);
//		
//		
//		
//		HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
//        httpRequestFactory.setConnectionRequestTimeout(3000);
//        httpRequestFactory.setConnectTimeout(3000);
//        httpRequestFactory.setReadTimeout(3000);
//
//		RestTemplate restTemplate2 = new RestTemplate(httpRequestFactory);
//		ResponseEntity<String> response = null;
//		
//		try {
//			response = restTemplate2.exchange("http://localhost:"+port+"/test/timeout/123", HttpMethod.POST, requests,
//					String.class);
//			System.out.println(response.getStatusCode());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		
//		try {
//			response = restTemplate2.exchange("http://localhost:"+port+"/test/timeout/123", HttpMethod.POST, requests,
//					String.class);
//			System.out.println(response.getStatusCode());
//		} catch (Exception e) {
//			Assert.assertTrue("The call is in process. http response code must be 499", e.getMessage().matches(".*?499.*?"));
//		}
//		
//
//		try {
//			Thread.sleep(11000L);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		response = restTemplate2.exchange("http://localhost:"+port+"/test/timeout/123", HttpMethod.POST, requests,
//				String.class);
//		Assert.assertNotEquals("The call is in ended. http response code must be 200", 200, response.getStatusCode());
//	}
}
