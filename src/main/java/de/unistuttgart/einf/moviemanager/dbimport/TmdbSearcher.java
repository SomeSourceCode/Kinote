package de.unistuttgart.einf.moviemanager.dbimport;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A helper class to search for media on TMDb.
 */
public class TmdbSearcher {

	private final TmdbClient client;

	/**
	 * Constructs a new searcher with the given TMDb client.
	 *
	 * @param client the client
	 */
	public TmdbSearcher(TmdbClient client) {
		this.client = client;
	}

	/**
	 * Represents a search result from TMDb.
	 *
	 * @param id the TMDb ID of the media
	 * @param title the title
	 * @param type the media type (movie or series)
	 * @param releaseDate the release date (for movies) or first air date (for series), can be null if not available
	 */
	public record SearchResult(int id, String title, MediaType type, LocalDate releaseDate) {}

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

			final LocalDate releaseDate;
			if (searchResultObject.has("release_date") && !searchResultObject.get("release_date").getAsString().isEmpty()) {
				releaseDate = LocalDate.parse(searchResultObject.get("release_date").getAsString());
			} else if (searchResultObject.has("first_air_date") && !searchResultObject.get("first_air_date").getAsString().isEmpty()) {
				releaseDate = LocalDate.parse(searchResultObject.get("first_air_date").getAsString());
			} else {
				releaseDate = null;
			}

			results.add(new SearchResult(identifier, title, mediaType, releaseDate));
		}

		return results;
	}

	/**
	 * Searches for movies on TMDb matching the given query and language.
	 *
	 * @param query the search query
	 * @param language the language
	 * @return a list of search results matching the query
	 * @throws MediaImportException if the search request fails or the response is invalid
	 */
	public List<SearchResult> searchMovies(String query, Language language) throws MediaImportException {
		return executeSearchRequest("search/movie", query, language, _ -> MediaType.MOVIE);
	}

	/**
	 * Searches for series on TMDb matching the given query and language.
	 *
	 * @param query the search query
	 * @param language the language
	 * @return a list of search results matching the query
	 * @throws MediaImportException if the search request fails or the response is invalid
	 */
	public List<SearchResult> searchSeries(String query, Language language) throws MediaImportException {
		return executeSearchRequest("search/series", query, language, _ -> MediaType.SERIES);
	}

	/**
	 * Searches for movies and series on TMDb matching the given query and language.
	 *
	 * @param query the search query
	 * @param language the language
	 * @return a list of search results matching the query
	 * @throws MediaImportException if the search request fails or the response is invalid
	 */
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
