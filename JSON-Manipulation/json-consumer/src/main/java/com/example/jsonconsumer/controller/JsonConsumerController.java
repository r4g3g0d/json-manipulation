package com.example.jsonconsumer.controller;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.example.jsonconsumer.repository.CommandInterpreter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
public class JsonConsumerController {

	@Autowired
	CommandInterpreter commandInterpreter;

	@RequestMapping(value = "commands", method = RequestMethod.GET)
	private ResponseEntity<List<String>> interpretCommands() throws IOException {
		commandInterpreter.interpretCommands();
		return null;
	}

	@RequestMapping(value = "fetch-json", method = RequestMethod.GET)
	private ResponseEntity<String> fetchJson() {

		RestTemplate restTemplate = new RestTemplate();
		String endpoint = "http://localhost:8080/get-json";
		ResponseEntity<String> jsonContent;
		try {
			RequestEntity<Object> request = new RequestEntity<>(HttpMethod.GET, URI.create(endpoint));
			jsonContent = restTemplate.exchange(request, String.class);

		} catch (HttpClientErrorException e) {
			throw new RuntimeException("it was not possible to retrieve user profile");
		}
		return jsonContent;
	}

}
