package de.unistuttgart.einf.moviemanager.service.search;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class SearchConstants {

	public static final List<String> MOVIE_WORDS = Arrays.asList("movie", "film");
	public static final List<String> SERIES_WORDS = Arrays.asList("series", "show", "tv");

	public static final List<String> TOP_LEVEL_MEDIA_WORDS =
			Stream.concat(MOVIE_WORDS.stream(), SERIES_WORDS.stream()).distinct().toList();

	public static double BONUS = 0.4;

	//title, description, (bestChildScore)
	public static double[] MOVIE_WEIGHTS = new double[]{0.5, 0.5};
	public static double[] SERIES_WEIGHTS = new double[]{0.30, 0.30, 0.40};

}
