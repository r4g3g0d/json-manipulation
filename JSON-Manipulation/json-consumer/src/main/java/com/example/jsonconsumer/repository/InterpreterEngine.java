package com.example.jsonconsumer.repository;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.example.jsonconsumer.utils.JsonManipulationUtils;
import com.example.jsonconsumer.utils.StringRegexUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class InterpreterEngine {

	@Value("${client-id}")
	public String clientId;

	@Value("${client-secret}")
	public String clientSecret;
	@Autowired
	StringRegexUtils regexUtils;
	@Autowired
	JsonManipulationUtils jsonUtils;

	public static HashMap<String, String> resultCounter = new HashMap<String, String>();

	public HashMap<String, String> fetch(String input) throws IOException {

		List<String> tokens = regexUtils.splitByDelimiter(input, " ");

		RestTemplate restTemplate = new RestTemplate();
		String endpoint = tokens.get(2);
		if (regexUtils.containsPattern(endpoint, "\\W\\w{1}\\.{1}\\w")) {
			List<String> endpointVariables = regexUtils.findVariables(endpoint);
			List<String> formattedEndpoints = jsonUtils.replaceEndpointVariables(endpoint, endpointVariables);

			for (String formattedEndpoint : formattedEndpoints) {
				System.out.println(formattedEndpoint);

				RequestEntity<Object> request = new RequestEntity<>(HttpMethod.GET, URI.create(formattedEndpoint));

				ResponseEntity<String> jsonContent = restTemplate.exchange(request, String.class);
				
			}

		} else {

			RequestEntity<Object> request = new RequestEntity<>(HttpMethod.GET, URI.create(endpoint));
			ResponseEntity<String> jsonContent = restTemplate.exchange(request, String.class);

			resultCounter.put(tokens.get(1), jsonContent.getBody());
			return resultCounter;
		}
		// change this
		return null;
	}

	public HashMap<String, String> foreach(String input) throws IOException {
		List<String> tokens = regexUtils.splitByDelimiter(input, " ");
		List<String> jsonNodes = regexUtils.splitByDelimiter(tokens.get(2), ".");
		List<String> nodesToParse = jsonNodes.stream().skip(1).collect(Collectors.toList());
		String dataToBeParsed = resultCounter.get(jsonNodes.get(0));

		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode rootNode = objectMapper.readTree(dataToBeParsed);
		for (String path : nodesToParse) {
			rootNode = rootNode.path(path);
		}

		resultCounter.put(tokens.get(1), rootNode.toString());
		System.out.println(rootNode.size());
		return resultCounter;
	}
	
	
	/*public CompletableFuture<String> fetchPromise(String endpoints) {
		
	}*/

}
