package com.neymotx.twitterspringboot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.neymotx.twitterspringboot.model.FilterRules;
import com.neymotx.twitterspringboot.model.Tweets;
import com.neymotx.twitterspringboot.service.TwitterConnecttionService;

@Controller
public class TwitterSpringBootController {

	@Autowired
	private TwitterConnecttionService tcs;

	@MessageMapping("/filterrules")
	@SendTo("/twitterspringboot/tweets")
	public Tweets getFilterRules(FilterRules filterrules) throws Exception {	
		
		tcs.sendFilter(filterrules);
		return new Tweets("SUCCESS");

	}

	@GetMapping("/connect")
	public @ResponseBody String startConnection() {
		tcs.init();
		return "Successfully";

	}

}
