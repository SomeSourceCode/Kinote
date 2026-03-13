package de.unistuttgart.einf.moviemanager.service.search;

import de.unistuttgart.einf.moviemanager.model.Episode;
import de.unistuttgart.einf.moviemanager.model.Season;
import de.unistuttgart.einf.moviemanager.model.Series;
import me.xdrop.fuzzywuzzy.FuzzySearch;

public class ChildMediaScorer {

	public int calculateBestChildScore(Series series, String query) {
		int best = 0;

		for (Season season : series.getChildren()) {
			best = Math.max(best, scoreTitle(query, season.getTitle()));
			best = Math.max(best, scoreDescription(query, season.getDescription()));

			for (Episode episode : season.getChildren()) {
				best = Math.max(best, scoreTitle(query, episode.getTitle()));
				best = Math.max(best, scoreDescription(query, episode.getDescription()));
			}
		}
		return best;
	}

	private int scoreTitle(String query, String title) {
		final String normalizedTitle = TextNormalizer.normalize(title);
		if (normalizedTitle.isEmpty()) {
			return 0;
		}
		return FuzzySearch.weightedRatio(query, normalizedTitle);
	}

	private int scoreDescription(String query, String description) {
		final String normalizedDesc = TextNormalizer.normalize(description);
		if (normalizedDesc.isEmpty()) {
			return 0;
		}
		return FuzzySearch.partialRatio(query, normalizedDesc);
	}

}
