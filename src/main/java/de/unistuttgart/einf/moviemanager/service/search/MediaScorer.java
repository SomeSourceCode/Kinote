package de.unistuttgart.einf.moviemanager.service.search;

import de.unistuttgart.einf.moviemanager.model.Movie;
import de.unistuttgart.einf.moviemanager.model.Series;
import de.unistuttgart.einf.moviemanager.model.TopLevelMedia;
import me.xdrop.fuzzywuzzy.FuzzySearch;

public class MediaScorer {

	private MediaScorer() {

	}

	public static int score(String query, TopLevelMedia media) {
		int titleScore = FuzzySearch.weightedRatio(query, TextNormalizer.normalize(media.getTitle()));
		int descScore  = scoreTopLevelMediaDescription(query, media.getDescription());

		int bonus = 0;
		bonus += Bonus.topLevelMediaTypeBonus(query, media);
		bonus += (int) media.getGenres().stream()
				.mapToInt(genre -> Bonus.genreBonus(query, genre))
				.average()
				.orElse(0);

		return switch (media) {
			case Movie _ -> scoreMovie(titleScore, descScore, bonus);
			case Series series -> scoreSeries(query, series, titleScore, descScore, bonus);
		};
	}

	private static int scoreMovie(int titleScore, int descScore, int bonus) {
		int roundedScore = (int) Math.round(0.75 * titleScore + 0.25 * descScore) + bonus;
		return clamp(roundedScore);
	}

	private static int scoreSeries(String query, Series series, int titleScore, int descScore, int bonus) {
		int bestChildScore = ChildMediaScorer.calculateBestChildScore(series, query);
		int roundedScore = (int) Math.round(0.4 * titleScore + 0.2 * descScore + 0.4 * bestChildScore) + bonus;
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
