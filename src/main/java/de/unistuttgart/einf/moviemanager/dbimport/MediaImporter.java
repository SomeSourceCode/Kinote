package de.unistuttgart.einf.moviemanager.dbimport;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.unistuttgart.einf.moviemanager.model.age.*;
import info.movito.themoviedbapi.TmdbApi;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

/**
 * Abstract class for importing media data from TMDb.
 */
public abstract class MediaImporter {

	protected final String apiKey;
	protected final TmdbApi tmdbApi;
	protected Language language;

	protected final EnumSet<ImportableAttribute> included;
	protected final EnumSet<ImportableAttribute> overridden;

	/**
	 * Creates a new MediaImporter with the given TMDb API key.
	 *
	 * @param apiKey the TMDb API key
	 */
	public MediaImporter(String apiKey) {
		this.apiKey = apiKey;
		tmdbApi = new TmdbApi(apiKey);
		this.language = Language.ENGLISH;
		included = EnumSet.noneOf(ImportableAttribute.class);
		overridden = EnumSet.noneOf(ImportableAttribute.class);
	}

	/**
	 * Returns the language used for the import.
	 *
	 * @return the language
	 */
	public Language getLanguage() {
		return language;
	}

	/**
	 * Sets the language used for the import.
	 *
	 * @param language the language
	 */
	public void setLanguage(Language language) {
		this.language = language;
	}

	/**
	 * Includes the given attributes for import.
	 * These attributes will only be included, if the corresponding field is null.
	 *
	 * @param attributes the attributes to include
	 */
	public void include(ImportableAttribute... attributes) {
		included.addAll(List.of(attributes));
	}

	/**
	 * Excludes the given attributes from import.
	 *
	 * @param attributes the attributes to exclude
	 */
	public void exclude(ImportableAttribute... attributes) {
		List.of(attributes).forEach(attribute -> {
			included.remove(attribute);
			overridden.remove(attribute);
		});
	}

	/**
	 * Returns the included attributes for import.
	 * The collection is unmodifiable.
	 *
	 * @return the included attributes
	 */
	public Set<ImportableAttribute> getIncluded() {
		return Collections.unmodifiableSet(included);
	}

	/**
	 * Overrides the given attributes during import.
	 *
	 * @param attributes the attributes to override
	 */
	public void override(ImportableAttribute... attributes) {
		overridden.addAll(List.of(attributes));
		included.addAll(List.of(attributes));
	}

	/**
	 * Returns the attributes that will be overridden.
	 * The collection is unmodifiable.
	 *
	 * @return the overridden attributes
	 */
	public Set<ImportableAttribute> getOverridden() {
		return Collections.unmodifiableSet(overridden);
	}

	/**
	 * Maps the given country and age rating string to an AgeRating object.
	 *
	 * @param country the country code in ISO-3166-1 format
	 * @param ageRating the age rating string
	 * @return the corresponding rating or, or null if it cannot be mapped
	 */
	protected AgeRating mapAgeRating(String country, String ageRating) {
		return switch (country) {
			case "DE" -> switch (ageRating) {
				case "0" -> FskRating.FSK_0;
				case "6" -> FskRating.FSK_6;
				case "12" -> FskRating.FSK_12;
				case "16" -> FskRating.FSK_16;
				case "18" -> FskRating.FSK_18;
				default -> null;
			};
			case "US" -> switch (ageRating) {
				case "G", "TV-Y", "TV-G" -> MpaRating.G;
				case "PG", "TV-Y7", "TV-Y7-FV", "TV-PG" -> MpaRating.PG;
				case "PG-13", "TV-14" -> MpaRating.PG_13;
				case "R", "TV-MA" -> MpaRating.R;
				case "NC-17" -> MpaRating.NC_17;
				default -> null;
			};
			case "GB" -> switch (ageRating) {
				case "U" -> BbfcRating.BBFC_U;
				case "PG" -> BbfcRating.BBFC_PG;
				case "12", "12A" -> BbfcRating.BBFC_12;
				case "15" -> BbfcRating.BBFC_15;
				case "18" -> BbfcRating.BBFC_18;
				default -> null;
			};
			default -> null;
		};
	}

	/**
	 * Returns all ratings from the given set of age ratings in the specified system.
	 *
	 * @param ageRatings the set of age ratings
	 * @param system the rating system
	 * @return the list of age ratings
	 */
	protected List<AgeRating> getAgeRatingsBySystem(Set<AgeRating> ageRatings, RatingSystem system) {
		return ageRatings.stream().filter(rating -> rating.getSystem() == system).toList();
	}

	/**
	 * Fetches age ratings for the given series ID from TMDb.
	 *
	 * @param seriesId the TMDb series ID
	 * @return a set of age ratings
	 */
	protected Set<AgeRating> fetchAgeRatingsFromSeries(int seriesId) {
		Set<AgeRating> ageRatings = new HashSet<>();

		try (HttpClient client = HttpClient.newHttpClient()) {
			HttpRequest request = HttpRequest.newBuilder()
					.GET()
					.uri(new URI("https://api.themoviedb.org/3/tv/" + seriesId + "/content_ratings"))
					.setHeader("accept", "application/json")
					.setHeader("Authorization", "Bearer " + apiKey)
					.build();
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());


			JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();
			JsonArray results = jsonObject.getAsJsonArray("results");

			for (JsonElement country : results) {
				JsonObject object = country.getAsJsonObject();

				String iso31661 = object.get("iso_3166_1").getAsString();
				String rating = object.get("rating").getAsString();

				if (rating.isBlank()) {
					continue;
				}

				AgeRating ageRating = mapAgeRating(iso31661, rating);
				if (ageRating != null) {
					ageRatings.add(ageRating);
				}
			}

		} catch (IOException | InterruptedException | URISyntaxException e) {
			throw new RuntimeException(e);
		}

		return ageRatings;
	}

}
