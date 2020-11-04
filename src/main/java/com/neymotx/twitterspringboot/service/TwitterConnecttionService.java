package com.neymotx.twitterspringboot.service;

import com.neymotx.twitterspringboot.model.FilterRules;

public interface TwitterConnecttionService {

	void init();
	
	void sendFilter(FilterRules filterrules)throws Exception;
	
	
}
