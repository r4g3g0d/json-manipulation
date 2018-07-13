package com.example.jsonconsumer.repository;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.script.ScriptException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class InterpreterEngine {

	@Value("${client-id}")
	public String clientId;

	@Value("${client-secret}")
	public String clientSecret;

	private static HashMap<String, String> resultCounter = new HashMap<String, String>();

	public HashMap<String, String> fetch(String input) throws IOException {

		List<String> tokens = interpret(input, " ");

		RestTemplate restTemplate = new RestTemplate();
		String endpoint = tokens.get(2);
		if (containsVariables(endpoint)) {
			List<String> endpointVariables = this.findVariables(endpoint);
			List<String> formattedEndpoints = this.replaceEndpointVariables(endpoint, endpointVariables);
			System.out.println(endpointVariables);
			for (String formattedEndpoint : formattedEndpoints) {

				RequestEntity<Object> request = new RequestEntity<>(HttpMethod.GET, URI.create(formattedEndpoint));
				ResponseEntity<String> jsonContent = restTemplate.exchange(request, String.class);
				System.out.println(jsonContent);

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

	private List<String> replaceEndpointVariables(String endpoint, List<String> endpointVariables) throws IOException {
		Map<String, List<JsonNode>> finalResult = new HashMap<String, List<JsonNode>>();

		List<String> listOfEndpoints = new ArrayList<>();

		for (String variable : endpointVariables) {
			List<JsonNode> eachVariableResult = new ArrayList();

			List<String> splitVariables = this.interpret(variable, ".");
			String dataSetToParse = resultCounter.get(splitVariables.get(0));
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode rootNode = objectMapper.readTree(dataSetToParse);
			Iterator<JsonNode> iter = rootNode.iterator();

			while (iter.hasNext()) {
				JsonNode next = iter.next();
				for (String v : splitVariables.stream().skip(1).collect(Collectors.toList())) {
					if (this.containsPattern(variable, "\\[.*\\]")) {
						// if split variable contains List indices
						if (this.containsPattern(v, "\\[.*\\]")) {
							System.out.println(v.replaceAll("\\[.*\\]", ""));
							rootNode = next.path(v.replaceAll("\\[.*\\]", ""));
							rootNode = rootNode.get(Integer.parseInt(v.substring(v.indexOf("[") + 1, v.indexOf("]"))));
						} else {
							rootNode = rootNode.path(v);
						}
					} else {
						rootNode = next.path(v);
					}

				}
				eachVariableResult.add(rootNode);

			}

			finalResult.put(variable, eachVariableResult);

		}
		// obtain length of data
		int size = finalResult.get(endpointVariables.get(0)).size();
		// finalResult.get()
		for (int i = 0; i < size; i++) {
			String partialEndpoint = endpoint;
			// String partialEndpoint;
			for (int j = 0; j < endpointVariables.size(); j++) {
				partialEndpoint = partialEndpoint.replace(endpointVariables.get(j),
						finalResult.get(endpointVariables.get(j)).get(i).toString());
			}
			listOfEndpoints.add(partialEndpoint.replaceAll("\\b(\\w*CLIENT_ID\\w*)\\b", clientId)
					.replaceAll("\\b(\\w*CLIENT_SECRET\\w*)\\b", clientSecret));
		}
		return listOfEndpoints;
	}

	public HashMap<String, String> foreach(String input) throws IOException {
		List<String> tokens = interpret(input, " ");
		List<String> jsonNodes = interpret(tokens.get(2), ".");
		List<String> nodesToParse = jsonNodes.stream().skip(1).collect(Collectors.toList());
		String dataToBeParsed = resultCounter.get(jsonNodes.get(0));

		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode rootNode = objectMapper.readTree(dataToBeParsed);
		for (String path : nodesToParse) {
			rootNode = rootNode.path(path);
		}

		resultCounter.put(tokens.get(1), rootNode.toString());
		return resultCounter;
	}

	// MAKE ANOTHER HELPER CLASS

	private List<String> interpret(String input, String delimiter) {

		// TODO : must return map,another name
		return Collections.list(new StringTokenizer(input.trim(), delimiter)).stream().map(token -> (String) token)
				.collect(Collectors.toList());
	}

	private boolean containsVariables(String endpoint) {

		Pattern pattern = Pattern.compile("\\W\\w{1}\\.{1}\\w");
		Matcher matcher = pattern.matcher(endpoint);

		// TODO : SHOULD CHECK IF THE VARIABLE(S) IS ALSO IN THE MAP

		return matcher.find();

	}

	private boolean containsPattern(String endpoint, String patternToMatch) {
		Pattern pattern = Pattern.compile(patternToMatch);
		Matcher matcher = pattern.matcher(endpoint);
		return matcher.find();
	}

	private List<String> findVariables(String endpoint) {

		Pattern pattern = Pattern.compile("\\W\\w{1}\\.[aA-zZ0-9.]+");

		Matcher matcher = pattern.matcher(endpoint);

		List<String> resultList = new ArrayList<String>();
		while (matcher.find()) {
			resultList.add(matcher.group(0).replaceAll(("\\{|\\|\\}|\\,"), ""));
		}
		return resultList;

	}

}
