package de.unistuttgart.einf.moviemanager.service.search;

import de.unistuttgart.einf.moviemanager.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class MediaSearchServiceBasicTest {

	private Set<TopLevelMedia> catalog;

	@BeforeEach
	void setUp() {
		Movie interstellar = new Movie(UUID.randomUUID(), "Interstellar",
				"A team travels through a wormhole in space.", false);

		Movie conjuring = new Movie(UUID.randomUUID(), "The Conjuring",
				"Paranormal investigators and a dark presence.", false);

		Series breakingBad = new Series(UUID.randomUUID(), "Breaking Bad",
				"A chemistry teacher turns to cooking meth.");

		// children
		Season s2 = new Season(2, "Season 2", "The business grows and consequences escalate.");
		s2.addChild(new Episode(1, "Seven Thirty-Seven", "A deal, a threat, and rising tension.", false));
		breakingBad.addChild(s2);

		catalog = new HashSet<>(List.of(interstellar, conjuring, breakingBad));
	}

	private Set<MediaSearchService.ScoredMedia> search(String q, int threshold) {
		return MediaSearchService.search(q, catalog, threshold);
	}

	private List<MediaSearchService.ScoredMedia> sorted(String q, int threshold) {
		return search(q, threshold).stream()
				.sorted(Comparator.comparingInt(MediaSearchService.ScoredMedia::score).reversed())
				.toList();
	}

	private static String titleOf(TopLevelMedia m) {
		return m.getTitle();
	}

	@Test
	void titleMatch_shouldReturnResult() {
		var results = sorted("Interstellar", 0);
		assertFalse(results.isEmpty(), "Searching by exact title should return something");
		assertEquals("Interstellar", titleOf(results.get(0).media()));
	}

	@Test
	void threshold_shouldFilterOutEverythingWhenVeryHigh() {
		var results = search("Interstellar", 100);
		assertTrue(results.isEmpty(), "Threshold 100 should typically filter everything (unless you allow 100 exact only)");
	}

	@Test
	void scoresReturnedMustMeetThreshold() {
		int threshold = 60;
		var results = search("Interstellar", threshold);
		assertTrue(results.stream().allMatch(r -> r.score() >= threshold),
				"All results must have score >= threshold");
	}

	@Test
	void setShouldNotContainSameMediaTwice() {
		var results = search("the", 0);

		var uniqueMedia = new HashSet<TopLevelMedia>();
		for (var r : results) {
			assertTrue(uniqueMedia.add(r.media()), "Same TopLevelMedia appears twice in the result set");
		}
	}
}
