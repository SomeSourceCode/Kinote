package de.unistuttgart.einf.moviemanager.service.search;

import de.unistuttgart.einf.moviemanager.model.Genre;
import de.unistuttgart.einf.moviemanager.model.Movie;
import de.unistuttgart.einf.moviemanager.model.Series;
import de.unistuttgart.einf.moviemanager.model.TopLevelMedia;

import java.util.List;

import static de.unistuttgart.einf.moviemanager.service.search.SearchConstants.*;

public class BonusScorer {

	private static final int BONUS = 6;

	public double topLevelMediaTypeBonus(String query, TopLevelMedia media) {
		if (media instanceof Movie)
			if (containsAny(query, MOVIE_WORDS))
				return BONUS;

		if (media instanceof Series)
			if (containsAny(query, SERIES_WORDS))
				return BONUS;

		return 0;
	}

	public double genreBonus(String query, Genre genre) {
		if (genre == null)
			return 0;

		for (String key : genre.getKeywords()) {
			String normalizedKey = TextNormalizer.normalize(key);

			if (normalizedKey.isEmpty())
				continue;

			if (query.contains(normalizedKey))
				return BONUS;
		}
		return 0;
	}

	private boolean containsAny(String query, List<String> words) {
		for (String w : words)
			if (query.contains(w))
				return true;

		return false;
	}

}
