package com.example.jsonconsumer.utils;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.jsonconsumer.repository.InterpreterEngine;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class JsonManipulationUtils {
	@Value("${client-id}")
	public String clientId;

	@Value("${client-secret}")
	public String clientSecret;

	@Value("${some_local_path}")
	public String photoPath;

	@Autowired
	StringRegexUtils regexUtils;

	public HashMap<String, List<String>> obtainDownloadFullPath(String rawPath, List<String> rawPathVariables)
			throws IOException {
		HashMap<String, List<String>> finalResult = new HashMap<>();
		for (String variable : rawPathVariables) {
			List<String> eachVariableResult = new ArrayList<String>();
			List<String> splitVariables = regexUtils.splitByDelimiter(variable, ".");
			int sizeOfDataSetToParse = InterpreterEngine.resCounter.get(splitVariables.get(0)).size();

			for (int i = 0; i < sizeOfDataSetToParse; i++) {
				String promiseResult = null;
				try {
					promiseResult = InterpreterEngine.resCounter.get(splitVariables.get(0)).get(i).get();
				} catch (InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (promiseResult != null) {
					ObjectMapper objectMapper = new ObjectMapper();
					System.out.println(promiseResult);
					JsonNode rootNode = objectMapper.readTree(promiseResult);
					if (rootNode.size() > 0) {
						Iterator<JsonNode> iter = rootNode.iterator();
						while (iter.hasNext()) {
							JsonNode next = iter.next();
							for (String v : splitVariables.stream().skip(1).collect(Collectors.toList())) {
								if (regexUtils.containsPattern(variable, "\\[.*\\]")) {
									// if split variable contains List Indices (ex : address[0])
									if (regexUtils.containsPattern(v, "\\[.*\\]")) {

										rootNode = next.path(v.replaceAll("\\[.*\\]", ""));
										rootNode = rootNode
												.get(Integer.parseInt(v.substring(v.indexOf("[") + 1, v.indexOf("]"))));
									} else {
										rootNode = rootNode.path(v);
									}
								} else {
									rootNode = next.path(v);
								}
								eachVariableResult.add(rootNode.toString());
							}
						}

					}
				}
			}
			finalResult.put(variable, eachVariableResult);

		}
		return finalResult;
	}

	public HashMap<String, List<String>> obtainDownloadEndpoint(String endpoint, List<String> endpointVariables)
			throws IOException {
		HashMap<String, List<String>> finalResult = new HashMap<>();
		// List<String> eachVariableResult = new ArrayList<String>();
		for (String variable : endpointVariables) {
			List<String> eachVariableResult = new ArrayList<String>();
			List<String> splitVariables = regexUtils.splitByDelimiter(variable, ".");
			int sizeOfDataSetToParse = InterpreterEngine.resCounter.get(splitVariables.get(0)).size();
			for (int i = 0; i < sizeOfDataSetToParse; i++) {
				String promiseResult = null;
				try {
					promiseResult = InterpreterEngine.resCounter.get(splitVariables.get(0)).get(i).get();
				} catch (InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (promiseResult != null) {
					ObjectMapper objectMapper = new ObjectMapper();
					System.out.println("e empty aici oare ? " + promiseResult);
					JsonNode rootNode = objectMapper.readTree(promiseResult);
					if (rootNode.size() > 0) {
						Iterator<JsonNode> iter = rootNode.iterator();
						while (iter.hasNext()) {
							JsonNode next = iter.next();
							for (String v : splitVariables.stream().skip(1).collect(Collectors.toList())) {
								if (regexUtils.containsPattern(variable, "\\[.*\\]")) {
									// if split variable contains List Indices (ex : address[0])
									if (regexUtils.containsPattern(v, "\\[.*\\]")) {

										rootNode = next.path(v.replaceAll("\\[.*\\]", ""));
										rootNode = rootNode
												.get(Integer.parseInt(v.substring(v.indexOf("[") + 1, v.indexOf("]"))));
									} else {
										rootNode = rootNode.path(v);
									}
								} else {
									rootNode = next.path(v);
								}
								eachVariableResult.add(rootNode.toString());
							}
						}

					}
				}

			}
			System.out.println(eachVariableResult);
			finalResult.put(variable, eachVariableResult);
		}

		return finalResult;

	}

	public List<String> replaceEndpointVariables(String endpoint, List<String> endpointVariables) throws IOException {
		Map<String, List<JsonNode>> finalResult = new HashMap<String, List<JsonNode>>();

		List<String> listOfEndpoints = new ArrayList<>();

		for (String variable : endpointVariables) {
			List<JsonNode> eachVariableResult = new ArrayList();

			List<String> splitVariables = regexUtils.splitByDelimiter(variable, ".");
			String dataSetToParse = null;

			String firstOfDataSet = null;
			try {
				firstOfDataSet = InterpreterEngine.resCounter.get(splitVariables.get(0)).get(0).get();

			} catch (InterruptedException | ExecutionException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			if (firstOfDataSet.matches("\\{\".*")) {
				eachVariableResult = this.replaceVariablesFromDict(variable, endpoint);
				finalResult.put(variable, eachVariableResult);
			} else {

				try {
					dataSetToParse = InterpreterEngine.resCounter.get(splitVariables.get(0)).get(0).get();
				} catch (InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ObjectMapper objectMapper = new ObjectMapper();
				JsonNode rootNode = objectMapper.readTree(dataSetToParse);
				Iterator<JsonNode> iter = rootNode.iterator();

				while (iter.hasNext()) {
					JsonNode next = iter.next();
					for (String v : splitVariables.stream().skip(1).collect(Collectors.toList())) {
						if (regexUtils.containsPattern(variable, "\\[.*\\]")) {
							// if split variable contains List Indices (ex : address[0])
							if (regexUtils.containsPattern(v, "\\[.*\\]")) {

								rootNode = next.path(v.replaceAll("\\[.*\\]", ""));
								rootNode = rootNode
										.get(Integer.parseInt(v.substring(v.indexOf("[") + 1, v.indexOf("]"))));
							} else {
								rootNode = rootNode.path(v);
							}
						} else {
							rootNode = next.path(v);
						}

					}
					if (rootNode.toString() != "") {
						eachVariableResult.add(rootNode);
					}
					

				}

				finalResult.put(variable, eachVariableResult);

			}
		}
		// obtain length of data
		int size = finalResult.get(endpointVariables.get(0)).size();
		for (int i = 0; i < size; i++) {
			String partialEndpoint = endpoint;
			// String partialEndpoint;
			for (int j = 0; j < endpointVariables.size(); j++) {
				String valueToReplaceWith = finalResult.get(endpointVariables.get(j)).get(i).toString();
				// if (valueToReplaceWith.contains(" ")) {
				valueToReplaceWith = URLEncoder.encode(valueToReplaceWith.replace("\"", ""), "UTF-8");
				// }
				partialEndpoint = partialEndpoint.replace(endpointVariables.get(j), valueToReplaceWith);

			}

			listOfEndpoints.add(partialEndpoint.replaceAll("\\b(\\w*CLIENT_ID\\w*)\\b", clientId)
					.replaceAll("\\b(\\w*CLIENT_SECRET\\w*)\\b", clientSecret).replaceAll("\\}|\\{|\\\"", "")
					.replaceAll(" ", "+"));
		}
		return listOfEndpoints;
	}

	public List<JsonNode> replaceVariablesFromDict(String variable, String endpoint) throws IOException {
		List<String> splitVariables = regexUtils.splitByDelimiter(variable, ".");
		int sizeOfPromises = InterpreterEngine.resCounter.get(splitVariables.get(0)).size();
		String dataSetToParse = null;

		List<JsonNode> eachVariableResult = new ArrayList<JsonNode>();
		for (int i = 0; i < sizeOfPromises; i++) {
			try {
				dataSetToParse = InterpreterEngine.resCounter.get(splitVariables.get(0)).get(i).get();
				System.out.println(dataSetToParse);
			} catch (InterruptedException | ExecutionException e) { // TODO Auto-generated catch block
				e.printStackTrace();
			}
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode rootNode = objectMapper.readTree(dataSetToParse);
			for (String v : splitVariables.stream().skip(1).collect(Collectors.toList())) {
				if (regexUtils.containsPattern(variable, "\\[.*\\]")) {
					if (regexUtils.containsPattern(v, "\\[.*\\]")) {

						rootNode = rootNode.path(v.replaceAll("\\[.*\\]", ""));
						if (rootNode.size() > 0) {
							rootNode = rootNode.get(Integer.parseInt(v.substring(v.indexOf("[") + 1, v.indexOf("]"))));
						} else {
							break;
						}
					} else {

						rootNode = rootNode.path(v);
					}

				}

			}
			if (regexUtils.checkValidity(rootNode.toString())) {
				eachVariableResult.add(rootNode);
			}

		}
		return eachVariableResult;
	}

}
