package com.example.jsonconsumer.repository;

import java.io.IOException;
import java.util.HashMap;

public class DownloadExpression implements Expression{
	private String expression;
	
	public DownloadExpression(String expression) {
		super();
		this.expression = expression;
	}

	@Override
	public HashMap<String,String> interpret(InterpreterEngine ie) throws IOException {
		return ie.download(expression);
	}
}
