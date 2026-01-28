package de.unistuttgart.einf.moviemanager.service.search;

import de.unistuttgart.einf.moviemanager.model.Movie;
import de.unistuttgart.einf.moviemanager.model.Series;
import de.unistuttgart.einf.moviemanager.model.TopLevelMedia;

import static de.unistuttgart.einf.moviemanager.service.search.SearchConstants.*;
import static java.lang.Math.max;
import static java.lang.Math.min;

import me.xdrop.fuzzywuzzy.FuzzySearch;

public class MediaScorer {

	private ChildMediaScorer childMediaScorer;
	private BonusScorer bonusScorer;

	public MediaScorer() {
		this.childMediaScorer = new ChildMediaScorer();
		this.bonusScorer = new BonusScorer();
	}

	public int getScore(String query, TopLevelMedia media) {
		int titleScore = FuzzySearch.weightedRatio(query, TextNormalizer.normalize(media.getTitle()));
		int descScore = scoreTopLevelMediaDescription(query, media.getDescription());

		double bonus = 1.0;

		bonus += bonusScorer.topLevelMediaTypeBonus(query, media);
		bonus += media.getGenres().stream()
				.mapToDouble(genre -> bonusScorer.genreBonus(query, genre))
				.average()
				.orElse(0);

		return switch (media) {
			case Movie _ -> scoreMovie(titleScore, descScore, bonus);
			case Series series -> scoreSeries(query, series, titleScore, descScore, bonus);
		};
	}

	private int scoreMovie(int titleScore, int descScore, double bonus) {
		double score = MIXING_COEFFICIENT * (MOVIE_WEIGHTS[0] * titleScore + MOVIE_WEIGHTS[1] * descScore)
				+ (1 - MIXING_COEFFICIENT) * max(titleScore, descScore);
		int roundedScore = (int) Math.round(score * bonus);
		return clamp(roundedScore);
	}

	private int scoreSeries(String query, Series series, int titleScore, int descScore, double bonus) {
		int bestChildScore = childMediaScorer.calculateBestChildScore(series, query);

		double score = MIXING_COEFFICIENT * (SERIES_WEIGHTS[0] * titleScore + SERIES_WEIGHTS[1] * descScore + SERIES_WEIGHTS[2] * bestChildScore)
				+ (1-MIXING_COEFFICIENT) * max(titleScore, max(descScore, bestChildScore));

		int roundedScore = (int) Math.round(score * bonus);
		return clamp(roundedScore);
	}

	private int scoreTopLevelMediaDescription(String query, String description) {
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

	private int clamp(int value) {
		final int MIN_VAL = 0, MAX_VAL = 100;
		return max(MIN_VAL, min(value, MAX_VAL));
	}

}
