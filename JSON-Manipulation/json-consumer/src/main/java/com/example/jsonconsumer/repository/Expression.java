package com.example.jsonconsumer.repository;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface Expression {
	public HashMap<String, List<CompletableFuture<String>>> interpret(InterpreterEngine ie) throws IOException;
}
