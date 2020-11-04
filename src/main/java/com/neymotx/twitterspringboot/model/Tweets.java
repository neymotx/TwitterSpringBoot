package com.neymotx.twitterspringboot.model;

import org.springframework.stereotype.Component;

public class Tweets {

	private String messege;

	public Tweets() {

	}

	public Tweets(String messege) {
		super();
		this.messege = messege;
	}

	public String getMessege() {
		return messege;
	}

	public void setMessege(String messege) {
		this.messege = messege;
	}

}
