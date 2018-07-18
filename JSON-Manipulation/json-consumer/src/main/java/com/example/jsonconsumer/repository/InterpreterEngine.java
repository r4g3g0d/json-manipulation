package com.example.jsonconsumer.repository;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
	private String clientId;
	@Value("${client-secret}")
	private String clientSecret;

	@Autowired
	StringRegexUtils regexUtils;
	@Autowired
	JsonManipulationUtils jsonUtils;

	public static HashMap<String, List<CompletableFuture<String>>> resultsMap = new HashMap<String, List<CompletableFuture<String>>>();

	public HashMap<String, List<CompletableFuture<String>>> download(String input) throws IOException {

		List<String> tokens = regexUtils.splitByDelimiter(input, " ");
		String endpoint = tokens.get(1);
		List<String> endpointVariables = regexUtils.findVariables(endpoint);
		String rawPath = tokens.get(2);
		List<String> rawPathVariables = regexUtils.findVariables(rawPath);

		HashMap<String, List<String>> formattedEndpoints = jsonUtils.obtainDownloadEndpoint(endpoint,
				endpointVariables);
		HashMap<String, List<String>> fullPaths = jsonUtils.obtainDownloadFullPath(rawPath, rawPathVariables);

		List<String> listOfDownloadEndpoints = new ArrayList<String>();
		List<String> formattedPaths = new ArrayList<String>();
		Set<String> keys = formattedEndpoints.keySet();
		String anyKey = keys.iterator().next();

		for (int i = 0; i < formattedEndpoints.get(anyKey).size(); i++) {

			String partialResult = endpoint;
			for (String key : keys) {
				partialResult = partialResult.replace(key, formattedEndpoints.get(key).get(i))
						.replaceAll("\\}|\\{|\\\"", "");
			}
			listOfDownloadEndpoints.add(partialResult);
		}

		int counter = 0;
		for (String downloadLink : listOfDownloadEndpoints) {
			counter++;
			try (InputStream in = new URL(downloadLink).openStream()) {
				Files.copy(in, Paths.get("D:/" + Integer.toString(counter) + ".jpg"));
			}
		}
		return null;

	}

	public HashMap<String, List<CompletableFuture<String>>> fetch(String input) throws IOException {

		List<String> tokens = regexUtils.splitByDelimiter(input, " ");

		String endpoint = tokens.get(2);

		List<CompletableFuture<String>> responsePromises = new ArrayList<>();
		// if endpoint contains variables
		if (regexUtils.containsPattern(endpoint, "\\W\\w{1}\\.{1}\\w")) {
			List<String> endpointVariables = regexUtils.findVariables(endpoint);

			List<String> formattedEndpoints = jsonUtils.replaceEndpointVariables(endpoint, endpointVariables);
			System.out.println(formattedEndpoints);
			for (String formattedEndpoint : formattedEndpoints) {
				//For debugging purpose
				/*System.out.println(formattedEndpoint);
				String s = this.simpleGetRequest(formattedEndpoint);*/
				responsePromises.add(this.fetchPromise(formattedEndpoint));

			}
			resultsMap.put(tokens.get(1), responsePromises);

		} else {

			responsePromises.add(this.fetchPromise(endpoint));
			resultsMap.put(tokens.get(1), responsePromises);

		}
		return resultsMap;

	}

	public HashMap<String, List<CompletableFuture<String>>> foreach(String input) throws IOException {
		List<String> tokens = regexUtils.splitByDelimiter(input, " ");
		List<String> jsonNodes = regexUtils.splitByDelimiter(tokens.get(2), ".");
		List<String> nodesToParse = jsonNodes.stream().skip(1).collect(Collectors.toList());
		List<CompletableFuture<String>> partialResult = new ArrayList<CompletableFuture<String>>();

		int sizeOfPromises = resultsMap.get(jsonNodes.get(0)).size();
		for (int i = 0; i < sizeOfPromises; i++) {
			CompletableFuture<String> forEachPromise = this.forEachPromise(input, i);
			try {
				String s = forEachPromise.get();
				System.out.println(s);
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			partialResult.add(forEachPromise);
		}

		resultsMap.put(tokens.get(1), partialResult);
		try {
			String s = resultsMap.get("c").get(0).get();
			System.out.println("TTT" + s);
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return resultsMap;
	}

	public CompletableFuture<String> fetchPromise(String endpoint) {
		return CompletableFuture.supplyAsync(() -> simpleGetRequest(endpoint));
	}

	public String simpleGetRequest(String endpoint) {
		RestTemplate restTemplate = new RestTemplate();
		RequestEntity<Object> request = new RequestEntity<>(HttpMethod.GET, URI.create(endpoint));
		ResponseEntity<String> jsonContent = restTemplate.exchange(request, String.class);
		return jsonContent.getBody();
	}

	public String forEachLogic(String input, int index) {
		List<String> tokens = regexUtils.splitByDelimiter(input, " ");
		List<String> jsonNodes = regexUtils.splitByDelimiter(tokens.get(2), ".");
		List<String> nodesToParse = jsonNodes.stream().skip(1).collect(Collectors.toList());
		List<String> partialResult = new ArrayList<String>();
		String dataToBeParsed = null;
		try {
			dataToBeParsed = resultsMap.get(jsonNodes.get(0)).get(index).get();
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

		}

		return rootNode.toString();
	}

	public CompletableFuture<String> forEachPromise(String input, int index) {
		return CompletableFuture.supplyAsync(() -> forEachLogic(input, index));

	}

}