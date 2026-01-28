package de.unistuttgart.einf.moviemanager.service.search;

import de.unistuttgart.einf.moviemanager.model.Movie;
import de.unistuttgart.einf.moviemanager.model.Series;
import de.unistuttgart.einf.moviemanager.model.TopLevelMedia;

import static de.unistuttgart.einf.moviemanager.service.search.SearchConstants.*;

import me.xdrop.fuzzywuzzy.FuzzySearch;

public class MediaScorer {

	private MediaScorer() {}

	public static int score(String query, TopLevelMedia media) {
		int titleScore = FuzzySearch.weightedRatio(query, TextNormalizer.normalize(media.getTitle()));
		int descScore = scoreTopLevelMediaDescription(query, media.getDescription());

		double bonus = 1.0;

		bonus += Bonus.topLevelMediaTypeBonus(query, media);
		bonus += media.getGenres().stream()
				.mapToDouble(genre -> Bonus.genreBonus(query, genre))
				.average()
				.orElse(0);

		return switch (media) {
			case Movie _ -> scoreMovie(titleScore, descScore, bonus);
			case Series series -> scoreSeries(query, series, titleScore, descScore, bonus);
		};
	}

	private static int scoreMovie(int titleScore, int descScore, double bonus) {
		double score = MOVIE_WEIGHTS[0] * titleScore + MOVIE_WEIGHTS[1] * descScore;
		int roundedScore = (int) Math.round(score * bonus);
		return clamp(roundedScore);
	}

	private static int scoreSeries(String query, Series series, int titleScore, int descScore, double bonus) {
		int bestChildScore = ChildMediaScorer.calculateBestChildScore(series, query);

		double score = SERIES_WEIGHTS[0] * titleScore + SERIES_WEIGHTS[1] * descScore + SERIES_WEIGHTS[2] * bestChildScore;
		int roundedScore = (int) Math.round(score * bonus);

		return clamp(roundedScore);
	}

	private static int scoreTopLevelMediaDescription(String query, String description) {
		String normalizedDesc = TextNormalizer.normalize(description);
		if (normalizedDesc.isEmpty())
			return 0;

		int base = FuzzySearch.partialRatio(query, normalizedDesc);

		//short queries should not be driven by description too much
		int tokenCount = query.split("\\s").length;
		if (tokenCount < 3)
			return (int) Math.round(base * 0.3);

		return base;
	}

	private static int clamp(int value) {
		final int MIN_VAL = 0, MAX_VAL = 100;
		return Math.max(MIN_VAL, Math.min(value, MAX_VAL));
	}

}
