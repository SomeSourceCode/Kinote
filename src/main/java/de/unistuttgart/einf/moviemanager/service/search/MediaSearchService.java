package de.unistuttgart.einf.moviemanager.service.search;

import de.unistuttgart.einf.moviemanager.model.TopLevelMedia;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class MediaSearchService {

	public record ScoredMedia(TopLevelMedia media, int score) {}

	public static Set<ScoredMedia> search(String query, Set<TopLevelMedia> media, int threshold) {
		final MediaScorer mediaScorer = new MediaScorer();

		final String normalizedQuery = TextNormalizer.normalize(query);
		if (normalizedQuery.isEmpty()) {
			return Collections.emptySet();
		}

		return media.stream()
				.map(m -> new ScoredMedia(m, mediaScorer.getScore(normalizedQuery, m)))
				.filter(sm -> sm.score() >= threshold)
				.collect(Collectors.toSet());
	}

}
