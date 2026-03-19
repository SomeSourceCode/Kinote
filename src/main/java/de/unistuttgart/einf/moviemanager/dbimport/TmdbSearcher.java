package de.unistuttgart.einf.moviemanager.dbimport;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TmdbSearcher {

	private final TmdbClient client;

	public TmdbSearcher(TmdbClient client) {
		this.client = client;
	}

	public record SearchResult(int id, String title, MediaType type) {}

	private List<SearchResult> executeSearchRequest(String endpoint, String query, Language language, java.util.function.Function<JsonObject, MediaType> mediaTypeExtractor) throws MediaImportException {
		final JsonObject json = client.get(endpoint, language, Map.of("query", query));

		if (!json.has("results") || !json.get("results").isJsonArray()) {
			throw new MediaImportException("Invalid response from TMDb API: missing 'results' array");
		}

		final List<SearchResult> results = new ArrayList<>();
		final JsonArray resultsArray = json.getAsJsonArray("results");

		for (JsonElement searchElement : resultsArray) {
			if (!searchElement.isJsonObject()) {
				continue;
			}

			final JsonObject searchResultObject = searchElement.getAsJsonObject();
			if (!searchResultObject.has("id") || !searchResultObject.has("title")) {
				continue;
			}

			final MediaType mediaType = mediaTypeExtractor.apply(searchResultObject);
			if (mediaType == null) {
				continue;
			}

			final int identifier = searchResultObject.get("id").getAsInt();
			final String title = searchResultObject.get("title").getAsString();

			results.add(new SearchResult(identifier, title, mediaType));
		}

		return results;
	}

	public List<SearchResult> searchMovies(String query, Language language) throws MediaImportException {
		return executeSearchRequest("search/movie", query, language, _ -> MediaType.MOVIE);
	}

	public List<SearchResult> searchSeries(String query, Language language) throws MediaImportException {
		return executeSearchRequest("search/series", query, language, _ -> MediaType.SERIES);
	}

	public List<SearchResult> searchMedia(String query, Language language) throws MediaImportException {
		return executeSearchRequest("search/multi", query, language, searchResultObject -> {
			if (!searchResultObject.has("media_type")) {
				return null;
			}

			final String mediaTypeString = searchResultObject.get("media_type").getAsString();
			return switch (mediaTypeString) {
				case "movie" -> MediaType.MOVIE;
				case "tv" -> MediaType.SERIES;
				default -> null;
			};
		});
	}

}
