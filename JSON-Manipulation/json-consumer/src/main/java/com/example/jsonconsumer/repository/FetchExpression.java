package com.example.jsonconsumer.repository;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.script.ScriptException;

import org.springframework.web.client.RestClientException;

public class FetchExpression implements Expression {
	
	private String expression;
		
	public FetchExpression(String expression) {
		super();
		this.expression = expression;
	}

	@Override
	public HashMap<String,String> interpret(InterpreterEngine ie) throws IOException {
		return ie.fetch(expression);
	}

}