package com.example.jsonconsumer.repository;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import com.example.jsonconsumer.mapper.ResultCounter;

@Service
@Configuration
public class CommandInterpreter {

	@Value("${filepath}")
	public String fileName;

	@Autowired
	InterpreterEngine ie;

	public void interpretCommands() throws IOException {

		ResultCounter resultCounter = new ResultCounter();

		Expression exp = null;
		List<String> commandsList = this.retrieveCommands(fileName);

		for (String commandRow : commandsList) {
			if (commandRow.trim().startsWith("fetch")) {
				exp = new FetchExpression(commandRow.trim());
			} else if (commandRow.trim().startsWith("foreach")) {
				exp = new ForEachExpression(commandRow.trim());
			}

			HashMap<String, String> result = exp.interpret(ie);

			System.out.println(result);
		}
	}

	public List<String> retrieveCommands(String fileName) throws IOException {

		File file = ResourceUtils.getFile(fileName);
		List<String> jsonContent = FileUtils.readLines(file, "UTF-8");
		System.out.println(jsonContent);
		return jsonContent;
	}

}
