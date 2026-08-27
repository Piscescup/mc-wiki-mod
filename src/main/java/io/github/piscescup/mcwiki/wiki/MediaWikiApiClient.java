package io.github.piscescup.mcwiki.wiki;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.piscescup.mcwiki.config.WikiLanguageConfig;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class MediaWikiApiClient {
	private static final int RESULT_LIMIT = 10;
	private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
		.connectTimeout(Duration.ofSeconds(10))
		.followRedirects(HttpClient.Redirect.NORMAL)
		.build();

	private MediaWikiApiClient() {
	}

	public static CompletableFuture<Optional<SearchResult>> search(
		WikiLanguageConfig language,
		WikiCategory category,
		String query
	) {
		return search(language, category, query, query)
			.thenCompose(result -> result.isPresent()
				? CompletableFuture.completedFuture(result)
				: search(language, category, category.searchTerm(language) + " " + query, query));
	}

	private static CompletableFuture<Optional<SearchResult>> search(
		WikiLanguageConfig language,
		WikiCategory category,
		String searchText,
		String query
	) {
		String apiUrl = "https://" + language.host() + "/api.php"
			+ "?action=query&generator=search&gsrnamespace=0&gsrlimit=" + RESULT_LIMIT
			+ "&prop=info&inprop=url&format=json&formatversion=2&utf8=1&gsrsearch="
			+ URLEncoder.encode(searchText, StandardCharsets.UTF_8);
		HttpRequest request = HttpRequest.newBuilder(URI.create(apiUrl))
			.timeout(Duration.ofSeconds(15))
			.header("Accept", "application/json")
			.header("User-Agent", "Minecraft-Wiki-Mod/1.0 (Fabric; MediaWiki search client)")
			.GET()
			.build();

		return HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
			.thenApply(response -> {
				if (response.statusCode() < 200 || response.statusCode() >= 300) {
					throw new IllegalStateException("MediaWiki API returned HTTP " + response.statusCode());
				}
				return selectBestResult(language, category, query, response.body());
			});
	}

	private static Optional<SearchResult> selectBestResult(
		WikiLanguageConfig language,
		WikiCategory category,
		String query,
		String responseBody
	) {
		JsonObject root = JsonParser.parseString(responseBody).getAsJsonObject();
		JsonObject queryObject = root.getAsJsonObject("query");
		if (queryObject == null) {
			return Optional.empty();
		}

		JsonArray results = queryObject.getAsJsonArray("pages");
		if (results == null || results.isEmpty()) {
			return Optional.empty();
		}

		JsonObject bestResult = null;
		int bestScore = Integer.MIN_VALUE;
		for (JsonElement resultElement : results) {
			JsonObject result = resultElement.getAsJsonObject();
			if (!result.has("title") || !result.has("fullurl")) {
				continue;
			}

			String title = result.get("title").getAsString();
			int score = score(title, query, category.searchTerm(language));
			if (score > bestScore) {
				bestResult = result;
				bestScore = score;
			}
		}

		if (bestResult == null) {
			return Optional.empty();
		}

		String title = bestResult.get("title").getAsString();
		String fullUrl = bestResult.get("fullurl").getAsString();
		return Optional.of(new SearchResult(title, fullUrl));
	}

	private static int score(String title, String query, String categoryTerm) {
		String normalizedTitle = normalize(title);
		String normalizedQuery = normalize(query);
		String normalizedCategory = normalize(categoryTerm);

		int score = 0;
		if (normalizedTitle.equals(normalizedQuery)) {
			score += 1_000;
		} else if (normalizedTitle.startsWith(normalizedQuery)) {
			score += 700;
		} else if (normalizedTitle.contains(normalizedQuery)) {
			score += 500;
		}
		if (!normalizedCategory.isEmpty() && normalizedTitle.contains(normalizedCategory)) {
			score += 50;
		}
		return score;
	}

	private static String normalize(String value) {
		return value.toLowerCase(Locale.ROOT).replace("_", "").replace(" ", "");
	}

	public record SearchResult(String title, String url) {
	}
}
