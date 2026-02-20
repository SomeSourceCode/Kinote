package de.unistuttgart.einf.moviemanager.cli.api.util;

import de.unistuttgart.einf.moviemanager.model.*;
import de.unistuttgart.einf.moviemanager.model.age.RatingSystem;

import java.util.Arrays;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Utility class for formatting {@link Media} properties into human-readable strings.
 */
public class MediaFormatter {

	/**
	 * Returns a string representation of the media type.
	 *
	 * @param media the media
	 * @return the type
	 */
	public static String type(Media media) {
		return switch (media) {
			case Movie _ -> "Movie";
			case Series _ -> "Series";
			case Season _ -> "Season";
			case Episode _ -> "Episode";
			default -> "Unknown";
		};
	}

	/**
	 * Returns a string representation of the media status.
	 *
	 * @param media the media
	 * @return the status
	 */
	public static String status(Media media) {
		return switch (media.getStatus()) {
			case WATCHED -> "Watched";
			case WATCHING -> "In Progress";
			case UNWATCHED -> "Unwatched";
		};
	}

	/**
	 * Returns a string representation of the media runtime.
	 *
	 * @param media the media
	 * @return the runtime
	 */
	public static String runtime(Media media) {
		final int duration = media.getRuntime();
		if (!media.hasRuntime()) {
			return "No Runtime";
		}
		final int hours = duration / 60;
		final int minutes = duration % 60;
		return String.format("%dh %02dm", hours, minutes);
	}

	/**
	 * Returns a string representation of the media genre.
	 *
	 * @param genre the genre
	 * @return the genre
	 */
	public static String genre(Genre genre) {
		if (genre == null) {
			return "Unknown";
		}
		final String text = genre.name()
				.replace("_", " ")
				.toLowerCase();
		return Pattern.compile("\\b\\w").matcher(text)
				.replaceAll(match -> match.group().toUpperCase());
	}

	/**
	 * Returns a string representation of the media genres, separated by commas.
	 *
	 * @param media the media
	 * @return the genres
	 */
	public static String genres(Media media) {
		final Set<Genre> genres = media.getGenres();
		if (genres.isEmpty()) {
			return "No Genres";
		}
		return Arrays.stream(Genre.values())
				.filter(genres::contains)
				.map(MediaFormatter::genre)
				.collect(Collectors.joining(", "));
	}

	/**
	 * Returns a string representation of the media rating as a percentage.
	 *
	 * @param media the media
	 * @return the rating
	 */
	public static String rating(Media media) {
		if (!media.hasRating()) {
			return "-/10";
		}
		final int rating = media.getRating();
		if (rating % 10 == 0) {
			return String.format("%d/10", rating / 10);
		}
		return String.format("%.1f/10", rating / 10.0);
	}

	/**
	 * Returns a string representation of the media age rating for
	 * the given rating system, prefixed with a tilde to mark
	 * an estimation.
	 *
	 * @param media the media
	 * @param system the rating system
	 * @return the rating
	 * @throws IllegalArgumentException if system is null
	 */
	public static String ageRating(Media media, RatingSystem system) {
		if (system == null) {
			throw new IllegalArgumentException("system must be non-null");
		}
		if (!media.hasAgeRating()) {
			return "No Rating";
		}
		return (media.hasAgeRating(system) ? "" : "~") + media.getAgeRating(system).getLabel();
	}

}
