package org.mideng.test;

import java.util.UUID;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mideng.Application;
import org.mideng.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;


@ContextConfiguration
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes=Application.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)

@Configuration
@EnableAutoConfiguration
public class ExceptionCallTest {

	private static final String REQ_URL = "/test/exception/123";
	
	@Autowired
	private TestRestTemplate restTemplate;
	
	@Test
	public void execSameCall() {
		String requestId = "execSameCall:"+UUID.randomUUID();
		
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
		headers.add(Constants.REQ_IDEM_ID, requestId);
		headers.add("Content-Type", "application/json");
		HttpEntity requests = new HttpEntity(headers);
		
		ResponseEntity<String> response = restTemplate.exchange(REQ_URL, HttpMethod.POST, requests, String.class);
		String reponse1 = response.getBody();
		
 		HttpEntity requests2 = new HttpEntity(headers);
		
		ResponseEntity<String> response2 = restTemplate.exchange(REQ_URL, HttpMethod.POST, requests2, String.class);
		String reponse2 = response2.getBody();
		Assert.assertEquals("The same result", reponse1, reponse2);
	}
	
	@Test
	public void execDifferentCall() {
		String requestId1 = "execDiffrentCall_:"+UUID.randomUUID();
		String requestId2 = "execDiffrentCall_:"+UUID.randomUUID();

		MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
		headers.add(Constants.REQ_IDEM_ID, requestId1);
		headers.add("Content-Type", "application/json");
		HttpEntity requests = new HttpEntity(headers);
		
		ResponseEntity<String> response = restTemplate.exchange(REQ_URL, HttpMethod.POST, requests,
				String.class);
		String reponse1 = response.getBody();
		
		MultiValueMap<String, String> headers2 = new LinkedMultiValueMap<String, String>();
		headers.add(Constants.REQ_IDEM_ID, requestId2);
		headers.add("Content-Type", "application/json");
		HttpEntity requests2 = new HttpEntity(headers2);
		
		ResponseEntity<String> response2 = restTemplate.exchange(REQ_URL, HttpMethod.POST, requests2, String.class);
		String reponse2 = response2.getBody();
		System.out.println(reponse1 + "\n" + reponse2);
		Assert.assertNotEquals("The different result", reponse1, reponse2);
	}

}
