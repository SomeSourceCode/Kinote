package de.unistuttgart.einf.moviemanager.dbimport;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.unistuttgart.einf.moviemanager.model.Genre;
import de.unistuttgart.einf.moviemanager.model.Movie;
import de.unistuttgart.einf.moviemanager.model.age.AgeRating;
import de.unistuttgart.einf.moviemanager.model.age.RatingSystem;

import java.util.HashSet;
import java.util.Set;

/**
 * Used to import movie data from TMDb.
 */
public class MovieImporter extends MediaImporter<Movie> {

	/**
	 * Constructs a new movie importer with the given TMDb client.
	 *
	 * @param client the TMDb client to use for API requests
	 */
	public MovieImporter(TmdbClient client) {
		super(client);
	}

	@Override
	protected void initializeStrategy() {
		strategies.put(ImportableAttribute.TITLE, createStringStrategy("title", Movie::getTitle, Movie::setTitle));
		strategies.put(ImportableAttribute.DESCRIPTION, createStringStrategy("overview", Movie::getDescription, Movie::setDescription));
		strategies.put(ImportableAttribute.RUNTIME, createRuntimeStrategy("runtime", Movie::hasRuntime, Movie::setRuntime));
		strategies.put(ImportableAttribute.GENRE, createGenreStrategy("genres", Movie::addGenre));

		strategies.put(ImportableAttribute.AGE_RATING, (movie, json, context, override) -> {
			if (!json.has("release_dates")) {
				return;
			}
			final Set<AgeRating> ageRatings = new HashSet<>();
			final JsonArray countries = json.getAsJsonObject("release_dates").getAsJsonArray("results");

			for (JsonElement countryElement : countries) {
				final JsonObject countryObject = countryElement.getAsJsonObject();
				final String iso31661 = countryObject.get("iso_3166_1").getAsString();
				final JsonArray releaseDates = countryObject.getAsJsonArray("release_dates");

				for (JsonElement dateElement : releaseDates) {
					final JsonObject dateObject = dateElement.getAsJsonObject();
					final int releaseType = dateObject.get("type").getAsInt();
					if (releaseType != 3) {
						continue;
					}

					final String certification = dateObject.get("certification").getAsString();
					final AgeRating rating = mapAgeRating(iso31661, certification);
					if (rating == null) {
						continue;
					}
					ageRatings.add(rating);
				}
			}

			if (ageRatings.isEmpty()) {
				return;
			}

			for (RatingSystem ratingSystem : RatingSystem.values()) {
				if (!override && movie.hasAgeRating(ratingSystem)) {
					continue;
				}
				final AgeRating rating = AgeRating.max(getAgeRatingsBySystem(ageRatings, ratingSystem));
				if (rating == null) {
					continue;
				}
				movie.setAgeRating(rating);
			}
		});
	}

	/**
	 * Fills the given movie with data from TMDb for the given TMDb ID.
	 *
	 * @param movie the movie to fill
	 * @param tmdbId the TMDb ID of the movie to import
	 * @throws MediaImportException if an error occurs during the import process
	 */
	public void fill(Movie movie, int tmdbId) throws MediaImportException {
		final JsonObject json = client.get("movie/" + tmdbId, language, "release_dates");
		applyAttributes(movie, json, null);
	}

	/**
	 * Creates a new movie and fills it with data from TMDb for the given TMDb ID.
	 *
	 * @param tmdbId the TMDb ID of the movie to import
	 * @return the newly created movie
	 * @throws MediaImportException if an error occurs during the import process
	 */
	public Movie create(int tmdbId) throws MediaImportException {
		final Movie movie = new Movie();
		fill(movie, tmdbId);
		return movie;
	}

}
