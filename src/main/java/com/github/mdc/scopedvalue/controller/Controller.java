package com.github.mdc.scopedvalue.controller;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.mdc.scopevalue.support.ScopedValueMdc;

import jakarta.servlet.http.HttpServletRequest;

@org.springframework.stereotype.Controller
public class Controller {
	
	public static class Response {
		
		private String user;
		private Integer order;

		public void setUser(String user) {
			this.user = user;
		}

		public void setOrder(Integer order) {
			this.order = order;
		}

		public String getUser() {
			return user;
		}

		public Integer getOrder() {
			return order;
		}
		
	}

	private static final String THREAD_NAME_ATTRIBUTE_NAME = "threadName";
	private static final String REQUEST_ID_ATTRIBUTE_NAME = "requestId";
	private static final Logger logger = LoggerFactory.getLogger(Controller.class);
	
	@RequestMapping("/handle")
	@ResponseBody
	public Response handle(HttpServletRequest request) throws InterruptedException, ExecutionException {

		Response response = new Response();
		MDC.put(THREAD_NAME_ATTRIBUTE_NAME, Thread.currentThread().getName());// set MDC 
		
		ScopedValueMdc.runWhere( () -> {
    		try (var scope = new StructuredTaskScope<>()) {
    			
     			MDC.put(REQUEST_ID_ATTRIBUTE_NAME, UUID.randomUUID().toString());// set MDC 
    			
    			Supplier<String> user = scope.fork(() -> {
    				logger.info("1");
    				return "OK";
    			});
    			Supplier<Integer> order = scope.fork(() -> {
       				logger.info("2");
       			 	return 777;
    			});

    			scope.join();

    			logger.info("3");

    			response.setUser(user.get());
    			response.setOrder(order.get());
    		} catch (InterruptedException e) {
			}
        });
		logger.info("4");
        return response;
	}

}
