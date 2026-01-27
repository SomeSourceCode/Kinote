package de.unistuttgart.einf.moviemanager.service.search;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class SearchConstants {

	public static final List<String> MOVIE_WORDS = Arrays.asList("movie", "film");
	public static final List<String> SERIES_WORDS = Arrays.asList("series", "show", "tv");

	public static final List<String> TOPLEVELMEDIA_WORDS =
			Stream.concat(MOVIE_WORDS.stream(), SERIES_WORDS.stream()).distinct().toList();


	public static int BONUS = 12;

	//title, description, (bestChildScore)
	public static double[] MOVIE_WEIGHTS = new double[]{0.75, 0.25};
	public static double[] SERIES_WEIGHTS = new double[]{0.70, 0.20, 0.10};


}
