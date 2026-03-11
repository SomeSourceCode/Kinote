package de.unistuttgart.einf.moviemanager.dbimport;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;

import java.io.IOException;
import java.util.function.Supplier;

/**
 * The client to access the TMDb API.
 */
public class TmdbClient {

	private static final String SCHEME = "https";
	private static final String HOST = "api.themoviedb.org";
	private static final String VERSION_SEGMENT = "3";

	private final OkHttpClient client;
	private final Supplier<String> apiKeySupplier;

	/**
	 * Constructs a new tmdb client with the given api key supplier.
	 *
	 * @param apiKeySupplier the api key supplier
	 * @throws IllegalArgumentException if apiKeySupplier is null
	 */
	public TmdbClient(Supplier<String> apiKeySupplier) {
		if (apiKeySupplier == null) {
			throw new IllegalArgumentException("apiKeySupplier must be non-null");
		}
		this.apiKeySupplier = apiKeySupplier;
		this.client = new OkHttpClient();
	}

	/**
	 * Performs a GET request to the TMDb API with the given endpoint, language and appendToResponse parameters.
	 *
	 * @param endpoint the API endpoint to call, e.g. "movie/550" or "tv/1399"
	 * @param language the language for the response (default: English)
	 * @param appendToResponse a comma-separated list of additional data to include in the response, e.g. "images,videos" (optional)
	 * @return the JSON response from the TMDb API as a JsonObject
	 * @throws MediaImportException if the API key is missing or invalid, if the endpoint is null, if the HTTP request fails, or if a network error occurs
	 */
	public JsonObject get(String endpoint, Language language, String appendToResponse) throws MediaImportException {
		if (endpoint == null) {
			throw new IllegalArgumentException("Endpoint cannot be null");
		}

		final String apiKey = apiKeySupplier.get();
		if (apiKey == null || apiKey.isBlank()) {
			throw new MediaImportException("API key is missing or invalid");
		}

		language = language == null ? Language.ENGLISH : language;

		final HttpUrl.Builder urlBuilder = new HttpUrl.Builder()
				.scheme(SCHEME)
				.host(HOST)
				.addPathSegment(VERSION_SEGMENT)
				.addPathSegments(endpoint)
				.addQueryParameter("language", language.getCode());

		if (appendToResponse != null && !appendToResponse.isBlank()) {
			urlBuilder.addQueryParameter("append_to_response", appendToResponse);
		}

		final Request request = new Request.Builder()
				.get()
				.url(urlBuilder.build())
				.addHeader("accept", "application/json")
				.addHeader("Authorization", "Bearer " + apiKey)
				.build();

		try (Response response = client.newCall(request).execute()) {
			if (!response.isSuccessful()) {
				throw new MediaImportException("HTTP request failed with status code: " + response.code());
			}
			return JsonParser.parseString(response.body().string()).getAsJsonObject();
		} catch (IOException exception) {
			throw new MediaImportException("Network error occurred during TMDb API call", exception);
		}
	}

}
