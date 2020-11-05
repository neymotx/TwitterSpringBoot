package com.neymotx.twitterspringboot;



import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import com.neymotx.twitterspringboot.model.FilterRules;
import com.neymotx.twitterspringboot.model.Tweets;
import com.neymotx.twitterspringboot.service.TwitterConnecttionService;

@TestMethodOrder(OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestWebSocket {
	
	@LocalServerPort
	private int port;
	WebSocketStompClient stompClient;
	private SockJsClient sockJsClient;
	private final WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
	
	@Autowired
	TwitterConnecttionService tcs;
	
	@Autowired
	private TestRestTemplate restTemplate;
	
	@BeforeEach
	public void setup()throws Exception {
		System.out.println("Inside before");
		List<Transport> transports = new ArrayList<>();
		transports.add(new WebSocketTransport(new StandardWebSocketClient()));
		sockJsClient = new SockJsClient(transports);   
		stompClient = new WebSocketStompClient(sockJsClient);
		stompClient.setMessageConverter(new MappingJackson2MessageConverter());
		//tcs.init();
	}
	
	@Test
	@Order(1)
	public void testHttp() throws IOException {
		System.out.println("inside HTTP");
		assertThat(this.restTemplate.getForObject("http://localhost:" + port + "/connect",
				String.class)).contains("Successfully");
		
	}
	
	@Test
	@Order(2)
	public void testStomp() throws InterruptedException, Throwable {
		System.out.println("inside test stomp");
		
		
	    final AtomicReference<Throwable> failure = new AtomicReference<>();
	    final CountDownLatch latch = new CountDownLatch(1);
	    StompSessionHandler handler = new StompSessionHandlerAdapter() {

	        @Override
	        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
	        	System.out.println("after connected");
	            session.subscribe("/twitterspringboot/tweets", new StompFrameHandler() {

	                @Override
	                public void handleFrame(StompHeaders headers, Object payload) {
	                    Tweets tweets=(Tweets)payload;
	                    System.out.println("-----WebSocket Response-----:" + tweets.getMessege());
	                    
	                    latch.countDown();
	                }

	                @Override
	                public Type getPayloadType(StompHeaders headers) {
	                    return Tweets.class;
	                }
	            });
	            
	            session.send("/app/filterrules", new FilterRules(new String[] {"narendramodi", "BarackObama","katperry"}, new String[] {"BiharElection2020","COVID19","EMAILMARKETING"}));
	        }

	        @Override
	        public void handleFrame(StompHeaders headers, Object payload) {
	            latch.countDown();
	        }

	        @Override
	        public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
	            failure.set(exception);
	            latch.countDown();
	        }

	        @Override
	        public void handleTransportError(StompSession session, Throwable exception) {
	            failure.set(exception);
	            latch.countDown();
	        }
	        
	        
	    
	    };
	   
	    this.stompClient.connect("ws://localhost:{port}/twitterSpringBoot",headers, handler, port);
	    if (!latch.await(30000, TimeUnit.SECONDS)) {
	        if (failure.get() != null) {
	            throw failure.get();
	        }
	        fail("Response was not received within 30 seconds");
	    }
	}
}
	   
		
	
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		



