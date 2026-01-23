package de.unistuttgart.einf.moviemanager.service.search;

import de.unistuttgart.einf.moviemanager.model.Genre;
import de.unistuttgart.einf.moviemanager.model.Movie;
import de.unistuttgart.einf.moviemanager.model.Series;
import de.unistuttgart.einf.moviemanager.model.TopLevelMedia;

public class Bonus {

	private static final int BONUS_VALUE = 6;

	public static int topLevelMediaTypeBonus(String query, TopLevelMedia media) {
		if (media instanceof Movie)
			if (containsAny(query, "movie", "film"))
				return BONUS_VALUE;

		if (media instanceof Series)
			if (containsAny(query, "series", "show", "tv"))
				return BONUS_VALUE;

		return 0;
	}

	public static int genreBonus(String query, Genre genre) {
		if (genre == null)
			return 0;

		for (String key : genre.getKeywords()) {
			String normalizedKey = TextNormalizer.normalize(key);

			if (normalizedKey.isEmpty())
				continue;

			if(query.contains(normalizedKey))
				return BONUS_VALUE;
		}
		return 0;
	}

	private static boolean containsAny(String query, String... words) {
		for (String w : words)
			if (query.contains(w))
				return true;

		return false;
	}

}
