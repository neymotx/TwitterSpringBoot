package com.neymotx.twitterspringboot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TwitterSpringBootConfig {
	
	@Value("${twitter.bearerToken}")
	private String bearerToken;

	public String getBearerToken() {
		return bearerToken;
	}
	
	
	

}
