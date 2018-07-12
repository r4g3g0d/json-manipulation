package com.example.jsonconsumer.repository;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class ForEachExpression implements Expression{
	
	private String expression;
	
	public ForEachExpression(String expression) {
		super();
		this.expression = expression;
	}

	@Override
	public HashMap<String,String> interpret(InterpreterEngine ie) throws IOException {
		return ie.foreach(expression); 
	}

}
