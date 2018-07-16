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
import java.util.concurrent.ExecutionException;
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

	public static HashMap<String, List<CompletableFuture<String>>> resCounter = new HashMap<String, List<CompletableFuture<String>>>();

	public HashMap<String, String> fetch(String input) throws IOException {

		List<String> tokens = regexUtils.splitByDelimiter(input, " ");

		RestTemplate restTemplate = new RestTemplate();
		String endpoint = tokens.get(2);

		List<CompletableFuture<String>> responsePromises = new ArrayList<>();

		if (regexUtils.containsPattern(endpoint, "\\W\\w{1}\\.{1}\\w")) {
			List<String> endpointVariables = regexUtils.findVariables(endpoint);
			List<String> formattedEndpoints = jsonUtils.replaceEndpointVariables(endpoint, endpointVariables);

			for (String formattedEndpoint : formattedEndpoints) {
				System.out.println(formattedEndpoint);

				responsePromises.add(this.fetchPromise(formattedEndpoint));

			}
			resCounter.put(tokens.get(1), responsePromises);

		} else {

			responsePromises.add(this.fetchPromise(endpoint));
			resCounter.put(tokens.get(1), responsePromises);
			try {
				System.out.println(resCounter.get(tokens.get(1)).get(0).get());
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			RequestEntity<Object> request = new RequestEntity<>(HttpMethod.GET, URI.create(endpoint));
			ResponseEntity<String> jsonContent = restTemplate.exchange(request, String.class);

			resultCounter.put(tokens.get(1), jsonContent.getBody());
			return resultCounter;

		}

		return null;
	}

	public HashMap<String, String> foreach(String input) throws IOException {
		List<String> tokens = regexUtils.splitByDelimiter(input, " ");
		List<String> jsonNodes = regexUtils.splitByDelimiter(tokens.get(2), ".");
		List<String> nodesToParse = jsonNodes.stream().skip(1).collect(Collectors.toList());
		List<CompletableFuture<String>> partialResult = new ArrayList<CompletableFuture<String>>();

		int sizeOfPromises = resCounter.get(jsonNodes.get(0)).size();
		for (int i = 0; i < sizeOfPromises; i++) {
			CompletableFuture<String> forEachPromise = this.forEachPromise(input, i);
			partialResult.add(forEachPromise);
		}

		resCounter.put(tokens.get(1), partialResult);
		try {
			System.out.println(resCounter.get(tokens.get(1)).get(0).get());
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public CompletableFuture<String> fetchPromise(String endpoint) {
		return CompletableFuture.supplyAsync(() -> simpleGetRequest(endpoint));
	}

	public String simpleGetRequest(String endpoint) {

		RestTemplate restTemplate = new RestTemplate();
		RequestEntity<Object> request = new RequestEntity<>(HttpMethod.GET, URI.create(endpoint));
		ResponseEntity<String> jsonContent = restTemplate.exchange(request, String.class);
		System.out.println(jsonContent.toString());
		return jsonContent.getBody();
	}

	public String forEachLogic(String input, int index) {

		List<String> tokens = regexUtils.splitByDelimiter(input, " ");
		List<String> jsonNodes = regexUtils.splitByDelimiter(tokens.get(2), ".");
		List<String> nodesToParse = jsonNodes.stream().skip(1).collect(Collectors.toList());
		List<String> partialResult = new ArrayList<String>();
		String dataToBeParsed = null;
		try {
			dataToBeParsed = resCounter.get(jsonNodes.get(0)).get(index).get();
		} catch (InterruptedException | ExecutionException e) {

			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode rootNode = null;
		try {
			rootNode = objectMapper.readTree(dataToBeParsed);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (String path : nodesToParse) {

			rootNode = rootNode.path(path);
			System.out.println(rootNode.toString());
		}
		System.out.println(rootNode.toString());
		return rootNode.toString();
	}

	public CompletableFuture<String> forEachPromise(String input, int index) {
		return CompletableFuture.supplyAsync(() -> forEachLogic(input, index));

	}
}