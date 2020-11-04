package com.neymotx.twitterspringboot.model;

public class FilterRules {

	private String followers[];
	private String keywords[];
	
	public FilterRules(String[] followers, String[] keywords) {
		this.followers = followers;
		this.keywords = keywords;
	}
	
	public String[] getFollowers() {
		return followers;
	}
	
	public void setFollowers(String[] followers) {
		this.followers = followers;
	}
	
	public String[] getKeywords() {
		return keywords;
	}

	public void setKeywords(String[] keywords) {
		this.keywords = keywords;
	}


}
