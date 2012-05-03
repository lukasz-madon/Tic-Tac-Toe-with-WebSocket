package com.tictactoe.server.message;

import com.google.gson.Gson;

/**
 * Base class of message beans
 * @author lukasz madon
 */
public class MessageBean {

	public String toJson() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}	
	
}
