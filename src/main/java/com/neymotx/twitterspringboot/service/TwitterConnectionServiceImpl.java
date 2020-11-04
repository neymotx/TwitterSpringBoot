package com.neymotx.twitterspringboot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.neymotx.twitterspringboot.model.FilterRules;
import com.neymotx.twitterspringboot.model.Tweets;

import reactor.core.publisher.Flux;

@Service
public class TwitterConnectionServiceImpl implements TwitterConnecttionService {

	@Autowired
	private Environment env;
	@Autowired
	private SimpMessagingTemplate simpMessagingTemplate;
	private WebClient webClient;

	@Override
	public void init() {
			if(webClient==null) {
			
			webClient = WebClient.builder().baseUrl(env.getProperty("twitter.stream.url"))
					.defaultHeader(HttpHeaders.ACCEPT, MediaType.ALL_VALUE)
					.defaultHeader("Authorization", "Bearer " + env.getProperty("twitter.bearerToken")).build();
			beginStream();
			}
	}

	private void beginStream() {
		try {
			Flux<String> tweetStream = webClient.get().retrieve().bodyToFlux(String.class); 	

			tweetStream.subscribe(tweet -> {
				try {
					String previousText = "";
					ObjectMapper objectMapper = new ObjectMapper();

					JsonNode rootNode = objectMapper.readTree(tweet);
					JsonNode text = rootNode.path("data").path("text");
					
					if (!text.asText().equals("") && !previousText.equals(text.asText())) {
						simpMessagingTemplate.convertAndSend(env.getProperty("client.endpoint"),
								new Tweets(text.asText()));
						previousText = text.asText();
					}

				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
			});
		} catch (Exception e) {
			System.out.println("-----Twitter connection failure-----:" + e.getMessage());
		}
	}

	@Override
	public void sendFilter(FilterRules filterRules) throws Exception {

		if (filterRules != null) {
			retreiveFilter();
			if (filterRules.getFollowers().length != 0 || filterRules.getKeywords().length != 0) {

				StringBuffer usernameBuffer = new StringBuffer();
				for (String followers : filterRules.getFollowers()) {
					usernameBuffer.append("@" + followers);
					if (followers != filterRules.getFollowers()[filterRules.getFollowers().length - 1])
						usernameBuffer.append(" OR ");
				}

				StringBuffer keywordBuffer = new StringBuffer();
				for (String keyword : filterRules.getKeywords()) {
					keywordBuffer.append(keyword);
					if (keyword != filterRules.getKeywords()[filterRules.getKeywords().length - 1])
						keywordBuffer.append(" OR ");
				}

				ObjectMapper objectMapper = new ObjectMapper();

				ObjectNode rootRulesObjectNode = objectMapper.createObjectNode();
				ArrayNode addArrayNode = objectMapper.createArrayNode();

				ObjectNode usernameRulesObjectNode = objectMapper.createObjectNode();
				ObjectNode keywordRulesObjectNode = objectMapper.createObjectNode();

				usernameRulesObjectNode.put("value", usernameBuffer.toString());
				usernameRulesObjectNode.put("tag", "usernames");

				keywordRulesObjectNode.put("value", keywordBuffer.toString());
				keywordRulesObjectNode.put("tag", "keywords");

				if (usernameBuffer.length() != 0)
					addArrayNode.add(usernameRulesObjectNode);
				if (keywordBuffer.length() != 0)
					addArrayNode.add(keywordRulesObjectNode);

				rootRulesObjectNode.putPOJO("add", addArrayNode);

				System.out.println("\n\n-----RULES-----");
				System.out
						.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootRulesObjectNode));

				try {
					String responseConfiguredRules = webClient.post().uri("/rules")
							.contentType(MediaType.APPLICATION_JSON).bodyValue(rootRulesObjectNode).retrieve()
							.bodyToMono(String.class).block();

					System.out.println("\n\n-----CONFIGURED RULES-----");
					System.out.println(responseConfiguredRules);
				} catch (Exception e) {
					System.out.println("\n\n-----Twitter connection failure-----:" + e.getMessage());
				}
			} else
				System.out.println("\n\n-----No Rules-----");
		}
	}

	private void retreiveFilter() {
		// TODO Auto-generated method stub
		try {
			String responseActiveRules = webClient.get().uri("/rules").retrieve().bodyToMono(String.class).block();

			if (responseActiveRules != null && !responseActiveRules.isEmpty()) {
				
				System.out.println("\n\n-----ACTIVE RULES-----");
				System.out.println(responseActiveRules);
				deleteFilter(responseActiveRules);
			}
		} catch (Exception e) {
			System.out.println("\\n\\n-----Twitter connection failure-----:" + e.getMessage());
		}

	}

	private void deleteFilter(String activeFilter) throws Exception {

		ObjectMapper objectMapper = new ObjectMapper();

		JsonNode rootRulesJsonNode = objectMapper.readTree(activeFilter);
		JsonNode dataJsonNode = rootRulesJsonNode.path("data");

		ObjectNode rootIdsObjectNode = objectMapper.createObjectNode();
		ObjectNode deleteObjectNode = objectMapper.createObjectNode();
		ArrayNode idsArrayNode = objectMapper.createArrayNode();

		if (!dataJsonNode.isEmpty()) {

			for (JsonNode rule : dataJsonNode) {
				idsArrayNode.add(rule.path("id"));
			}

			deleteObjectNode.putPOJO("ids", idsArrayNode);
			rootIdsObjectNode.putPOJO("delete", deleteObjectNode);

			System.out.println("\n\n-----DELETE IDs****");
			System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootIdsObjectNode));

			try {
				String responseDeletedRules = webClient.post().uri("/rules").contentType(MediaType.APPLICATION_JSON)
						.bodyValue(rootIdsObjectNode).retrieve().bodyToMono(String.class).block();

				System.out.println("\n****DELETED RULES****");
				System.out.println(responseDeletedRules);
			} catch (Exception e) {
				System.out.println("\\n\\n-----Twitter connection failure-----:" + e.getMessage());
			}
		} else
			System.out.println("\n****NO ACTIVE RULES TO DELETE****");

	}

}
