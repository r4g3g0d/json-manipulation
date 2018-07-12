package com.example.jsonconsumer.repository;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public interface Expression {
	public HashMap<String, String> interpret(InterpreterEngine ie) throws IOException;
}
