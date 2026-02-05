package de.unistuttgart.einf.moviemanager.dbimport;

import de.unistuttgart.einf.moviemanager.model.Genre;
import de.unistuttgart.einf.moviemanager.model.Movie;
import de.unistuttgart.einf.moviemanager.model.age.AgeRating;
import de.unistuttgart.einf.moviemanager.model.age.RatingSystem;
import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.model.core.IdElement;
import info.movito.themoviedbapi.model.movies.MovieDb;
import info.movito.themoviedbapi.model.movies.ReleaseType;
import info.movito.themoviedbapi.tools.TmdbException;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
	 * @throws MediaImportException if the import fails due to a TMDb API error
	 */
	public void importData(Movie movie, int tmdbId) throws MediaImportException {
		final TmdbMovies tmdbMovies = tmdbApi.getMovies();

		try {
			MovieDb tmdbMovie = tmdbMovies.getDetails(tmdbId, language.getCode());

			included.forEach(attribute -> {
				switch (attribute) {
					case TITLE -> {
						String tmdbTitle = tmdbMovie.getTitle();
						String title = movie.getTitle();

						if (tmdbTitle != null && (overridden.contains(attribute) || title == null || title.isBlank())) {
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
						Set<AgeRating> ageRatings = null;
						try {
							ageRatings = tmdbMovies.getReleaseDates(tmdbId).getResults().stream()
								.flatMap(country -> country.getReleaseDates().stream()
										.filter(releaseDate -> releaseDate.getType() == ReleaseType.THEATRICAL)
										.map(releaseDate -> mapAgeRating(country.getIso31661(), releaseDate.getCertification())))
								.filter(Objects::nonNull)
								.collect(Collectors.toSet());
						} catch (TmdbException e) {
							throw new RuntimeException(e);
						}

						if (ageRatings.isEmpty()) {
							return;
						}

						for (RatingSystem ratingSystem : RatingSystem.values()) {
							if (!overridden.contains(attribute) && movie.getAgeRating(ratingSystem) != null) {
								continue;
							}
							AgeRating rating = AgeRating.max(getAgeRatingsBySystem(ageRatings, ratingSystem));
							movie.setAgeRating(rating);
						}
					}
					case GENRE -> {
						Set<Genre> genres = mapGenres(tmdbMovie.getGenres().stream()
								.map(IdElement::getId)
								.collect(Collectors.toSet()));

						for (Genre genre : genres) {
							if (!overridden.contains(attribute) && movie.hasGenre(genre)) {
								continue;
							}
							movie.addGenre(genre);
						}
					}
				}
			});

		} catch (TmdbException e) {
			throw new MediaImportException(e.getMessage(), e.getCause());
		}
	}

}
