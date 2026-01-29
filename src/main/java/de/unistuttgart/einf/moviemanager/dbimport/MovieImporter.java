package de.unistuttgart.einf.moviemanager.dbimport;

import de.unistuttgart.einf.moviemanager.model.Movie;
import de.unistuttgart.einf.moviemanager.model.age.*;
import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.model.movies.MovieDb;
import info.movito.themoviedbapi.model.movies.ReleaseDate;
import info.movito.themoviedbapi.model.movies.ReleaseInfo;
import info.movito.themoviedbapi.model.movies.ReleaseType;
import info.movito.themoviedbapi.tools.TmdbException;

import java.util.*;

public class MovieImporter extends MediaImporter{

	/**
	 * Creates a new MovieImporter with the given TMDb API key.
	 *
	 * @param apiKey the TMDb API key
	 */
	public MovieImporter(String apiKey) {
		super(apiKey);
	}

	/**
	 * Runs the import with the current configuration for the given movie and TMDB ID.
	 *
	 * @param movie the to import data into
	 * @param tmdbId the TMDB ID of the movie
	 */
	public void importData(Movie movie, int tmdbId) {
		final TmdbMovies tmdbMovies = tmdbApi.getMovies();

		try {
			MovieDb tmdbMovie = tmdbMovies.getDetails(tmdbId, language.getCode());

			// calculate age ratings
			Set<AgeRating> ageRatings = new HashSet<>();

			if (included.contains(ImportableAttribute.AGE_RATING)) {
				List<ReleaseInfo> countries = tmdbMovies.getReleaseDates(tmdbId).getResults();
				List<ReleaseInfo> filteredCountries = countries.stream()
						.filter(releaseInfo -> releaseInfo.getIso31661().equals("DE") || releaseInfo.getIso31661().equals("US") || releaseInfo.getIso31661().equals("GB"))
						.toList();

				for (ReleaseInfo country : filteredCountries) {
					List<ReleaseDate> releaseDates = country.getReleaseDates();
					releaseDates.stream()
							.filter(releaseDate -> releaseDate.getType() == ReleaseType.THEATRICAL)
							.forEach(releaseDate -> {
								if (!releaseDate.getCertification().isBlank()) {
									ageRatings.add(mapAgeRating(country.getIso31661(), releaseDate.getCertification()));
								}
							});
				}
			}

			included.forEach(attribute -> {
				switch (attribute) {
					case TITLE -> {
						String tmdbTitle = tmdbMovie.getTitle();
						String title = movie.getTitle();

						if (tmdbTitle != null && (overridden.contains(attribute) ||title == null || title.isBlank())) {
							movie.setTitle(tmdbTitle);
						}
					}
					case DESCRIPTION -> {
						String tmdbDescription = tmdbMovie.getOverview();
						String description = movie.getDescription();

						if (tmdbDescription != null && (overridden.contains(attribute) || description == null || description.isBlank())) {
							movie.setDescription(tmdbDescription);
						}
					}
					case RUNTIME -> {
						Integer tmdbRuntime = tmdbMovie.getRuntime();
						int runtime = movie.getDuration();

						if (tmdbRuntime != null && (overridden.contains(attribute) || runtime == -1)) {
							movie.setDuration(tmdbRuntime);
						}
					}
					case AGE_RATING -> {
						for (RatingSystem ratingSystem : RatingSystem.values()) {
							AgeRating rating = AgeRating.max(getAgeRatingBySystem(ageRatings, ratingSystem));
							if (overridden.contains(attribute) || movie.getAgeRating(ratingSystem) == null) {
								movie.setAgeRating(rating);
							}
						}
					}
					// todo: case CATEGORY ->
				}
			});

		} catch (TmdbException e) {
			throw new RuntimeException(e);
		}
	}

}
