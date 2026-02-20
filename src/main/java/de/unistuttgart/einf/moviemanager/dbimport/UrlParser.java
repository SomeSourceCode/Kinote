package de.unistuttgart.einf.moviemanager.dbimport;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for parsing TMDb URLs and extracting media information such as media type, TMDb IDs, season and episode numbers, and language.
 */
public class UrlParser {

	private static final String BASE_URL = "^(?:https://)?(?:www\\.)?themoviedb\\.org/";

	private static final Pattern VALID_URL = Pattern.compile(BASE_URL + "((movie)|(tv))/\\d+-.*");
	private static final Pattern MOVIE_PATTERN = Pattern.compile(BASE_URL + "movie/(\\d+)-.*");
	private static final Pattern SERIES_PATTERN = Pattern.compile(BASE_URL + "tv/(\\d+)-.*");
	private static final Pattern SEASON_PATTERN = Pattern.compile(BASE_URL + "tv/\\d+-.*/season/(\\d+).*");
	private static final Pattern EPISODE_PATTERN = Pattern.compile(BASE_URL + "tv/\\d+-.*/season/\\d+/episode/(\\d+).*");
	private static final Pattern LANGUAGE_PATTERN = Pattern.compile(BASE_URL + ".*\\?language=([a-z]{2}).*");

	/**
	 * Checks if the given URL is a valid TMDb URL for a movie, TV series, season, or episode.
	 *
	 * @param url the URL to validate
	 * @return true if the URL is valid, false otherwise
	 */
	public static boolean isValid(String url) {
		return VALID_URL.matcher(url).matches();
	}

	/**
	 * Determines the media type (movie, series, season, or episode) based on the structure of the given URL.
	 * Returns null if the type cannot be determined.
	 *
	 * @param url the URL
	 * @return the media type
	 */
	public static MediaType getMediaType(String url) {
		if (MOVIE_PATTERN.matcher(url).matches()) {
			return MediaType.MOVIE;
		} else if (EPISODE_PATTERN.matcher(url).matches()) {
			return MediaType.EPISODE;
		} else if (SEASON_PATTERN.matcher(url).matches()) {
			return MediaType.SEASON;
		} else if (SERIES_PATTERN.matcher(url).matches()) {
			return MediaType.SERIES;
		}
		return null;
	}

	/**
	 * Extracts the media language from the given URL based on the "language" query parameter.
	 * Returns null if the language cannot be determined or is not supported.
	 *
	 * @param url the URL
	 * @return the media language
	 */
	public static Language getMediaLanguage(String url) {
		Matcher matcher = LANGUAGE_PATTERN.matcher(url);
		if (!matcher.matches()) {
			return null;
		}
		return Language.getLanguageByCode(matcher.group(1));
	}

	/**
	 * Extracts the TMDb ID for a movie from the given URL.
	 * Returns -1 if the ID cannot be determined.
	 *
	 * @param url the URL
	 * @return the TMDb ID
	 */
	public static int getMovieTmdbId(String url) {
		Matcher matcher = MOVIE_PATTERN.matcher(url);
		if (matcher.find()) {
			try {
				return Integer.parseInt(matcher.group(1));
			} catch (NumberFormatException _) {
				return -1;
			}
		}
		return -1;
	}

	/**
	 * Extracts the TMDb ID for a TV series from the given URL.
	 * Returns -1 if the ID cannot be determined.
	 *
	 * @param url the URL
	 * @return the TMDb ID
	 */
	public static int getSeriesTmdbId(String url) {
		Matcher matcher = SERIES_PATTERN.matcher(url);
		if (matcher.find()) {
			try {
				return Integer.parseInt(matcher.group(1));
			} catch (NumberFormatException _) {
				return -1;
			}
		}
		return -1;
	}

	/**
	 * Extracts the season number from the given URL.
	 * Returns -1 if the season number cannot be determined.
	 *
	 * @param url the URL
	 * @return the season number
	 */
	public static int getSeasonNumber(String url) {
		Matcher matcher = SEASON_PATTERN.matcher(url);
		if (matcher.find()) {
			try {
				return Integer.parseInt(matcher.group(1));
			} catch (NumberFormatException _) {
				return -1;
			}
		}
		return -1;
	}

	/**
	 * Extracts the episode number from the given URL.
	 * Returns -1 if the episode number cannot be determined.
	 *
	 * @param url the URL
	 * @return the episode number
	 */
	public static int getEpisodeNumber(String url) {
		Matcher matcher = EPISODE_PATTERN.matcher(url);
		if (matcher.find()) {
			try {
				return Integer.parseInt(matcher.group(1));
			} catch (NumberFormatException _) {
				return -1;
			}
		}
		return -1;
	}
}
