package com.example.jsonconsumer.repository;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;


@Service
@Configuration
public class CommandInterpreter {

	@Value("${filepath}")
	public String fileName;

	@Autowired
	InterpreterEngine ie;
	
	@Autowired

	public void interpretCommands() throws IOException {


		Expression exp = null;
		List<String> commandsList = this.retrieveCommands(fileName);

		for (String commandRow : commandsList) {
			if (commandRow.trim().startsWith("fetch")) {	
				exp = new FetchExpression(commandRow.trim());
			} else if (commandRow.trim().startsWith("foreach")) {
				exp = new ForEachExpression(commandRow.trim());
			} else if (commandRow.trim().startsWith("download")) {
				exp = new DownloadExpression(commandRow.trim());
			}

			HashMap<String, List<CompletableFuture<String>>> result = exp.interpret(ie);

		}
	}

	public List<String> retrieveCommands(String fileName) throws IOException {

		File file = ResourceUtils.getFile(fileName);
		List<String> jsonContent = FileUtils.readLines(file, "UTF-8");
		System.out.println(jsonContent);
		return jsonContent;
	}

}
