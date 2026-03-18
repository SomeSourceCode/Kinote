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

	public List<SearchResult> searchMovies(String query, Language language) throws MediaImportException {
		final JsonObject json = client.get("search/movie", language, Map.of("query", query));

		if (!json.has("results") || !json.get("results").isJsonArray()) {
			throw new MediaImportException("Invalid response from TMDb API: missing 'results' array");
		}

		final List<SearchResult> results = new ArrayList<>();

		final JsonArray resultsArray = json.getAsJsonArray("results");
		for (JsonElement movieElement : resultsArray) {
			if (!movieElement.isJsonObject()) {
				continue;
			}
			final JsonObject movieObject = movieElement.getAsJsonObject();
			if (!movieObject.has("id") || !movieObject.has("title")) {
				continue;
			}
			final int id = movieObject.get("id").getAsInt();
			final String title = movieObject.get("title").getAsString();
			results.add(new SearchResult(id, title, MediaType.MOVIE));
		}

		return results;
	}

	public List<SearchResult> searchSeries(String query, Language language) throws MediaImportException {
		final JsonObject json = client.get("search/series", language, Map.of("query", query));

		if (!json.has("results") || !json.get("results").isJsonArray()) {
			throw new MediaImportException("Invalid response from TMDb API: missing 'results' array");
		}

		final List<SearchResult> results = new ArrayList<>();

		final JsonArray resultsArray = json.getAsJsonArray("results");
		for (JsonElement seriesElement : resultsArray) {
			if (!seriesElement.isJsonObject()) {
				continue;
			}
			final JsonObject seriesObject = seriesElement.getAsJsonObject();
			if (!seriesObject.has("id") || !seriesObject.has("title")) {
				continue;
			}
			final int id = seriesObject.get("id").getAsInt();
			final String title = seriesObject.get("title").getAsString();
			results.add(new SearchResult(id, title, MediaType.SERIES));
		}

		return results;
	}

	public List<SearchResult> searchMedia(String query, Language language) throws MediaImportException {
		final JsonObject json = client.get("search/multi", language, Map.of("query", query));

		if (!json.has("results") || !json.get("results").isJsonArray()) {
			throw new MediaImportException("Invalid response from TMDb API: missing 'results' array");
		}

		final List<SearchResult> results = new ArrayList<>();

		final JsonArray resultsArray = json.getAsJsonArray("results");
		for (JsonElement mediaElement : resultsArray) {
			if (!mediaElement.isJsonObject()) {
				continue;
			}
			final JsonObject mediaObject = mediaElement.getAsJsonObject();
			if (!mediaObject.has("id") || !mediaObject.has("title") || !mediaObject.has("media_type")) {
				continue;
			}
			final String mediaTypeString = mediaObject.get("media_type").getAsString();
			final MediaType mediaType = switch (mediaTypeString) {
				case "movie" -> MediaType.MOVIE;
				case "tv" -> MediaType.SERIES;
				default -> null;
			};
			if (mediaType == null) {
				continue;
			}
			final int id = mediaObject.get("id").getAsInt();
			final String title = mediaObject.get("title").getAsString();
			results.add(new SearchResult(id, title, mediaType));
		}

		return results;
	}

}
