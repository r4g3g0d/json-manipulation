package com.example.jsonconsumer.mapper;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

 public class ResultCounter {
	

	    public Map<String, String> map = new HashMap<String, String>();
	 
	    public ResultCounter() {}
	    
	    public ResultCounter with(String key, String value) {
	        map.put(key, value);
	        return this;
	    }
	    
	    public ResultCounter done() {
	        try {
	            new ObjectMapper().writeValueAsString(map);
	            return this;
	        } catch (Exception e) {
	        	return new ResultCounter().with("Error", "Well, this is embarrassing, we are having trouble generating the response for you !");
	        }
	    }

	    public Map<String, String> getMap() {
	        return map;
	    }

	}


