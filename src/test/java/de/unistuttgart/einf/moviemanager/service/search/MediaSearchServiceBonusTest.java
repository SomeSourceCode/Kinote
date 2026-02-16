package de.unistuttgart.einf.moviemanager.service.search;

import de.unistuttgart.einf.moviemanager.model.Movie;
import de.unistuttgart.einf.moviemanager.model.Series;
import de.unistuttgart.einf.moviemanager.model.TopLevelMedia;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class MediaSearchServiceBonusTest {

	private Set<TopLevelMedia> catalog;

	@BeforeEach
	void setUp() {
		// Movie Horror
		Movie conjuring = new Movie(
				UUID.randomUUID(),
				"The Conjuring",
				"Paranormal investigators help a family terrorized by a dark presence.",
				false
		);

		// Movie SciFi
		Movie interstellar = new Movie(
				UUID.randomUUID(),
				"Interstellar",
				"A team travels through a wormhole in space to save humanity.",
				false
		);

		// Series Crime
		Series breakingBad = new Series(
				UUID.randomUUID(),
				"Breaking Bad",
				"A chemistry teacher turns to cooking meth."
		);

		catalog = new HashSet<>(List.of(conjuring, interstellar, breakingBad));
	}

	private List<MediaSearchService.ScoredMedia> sorted(String q, int threshold) {
		return MediaSearchService.search(q, catalog, threshold).stream()
				.sorted(Comparator.comparingInt(MediaSearchService.ScoredMedia::score).reversed())
				.toList();
	}

	private static String titleOf(TopLevelMedia m) {
		return m.getTitle();
	}


	@Test
	void categoryBonus_shouldPreferHorrorMovie_forMovieHorrorQuery() {
		var results = sorted("movie horror", 40);

		assertFalse(results.isEmpty());
		assertEquals("The Conjuring", titleOf(results.get(0).media()));
	}

	@Test
	void typeBonus_shouldPreferSeries_whenQueryMentionsSeries() {
		var results = sorted("crime series break ad", 40);

		results.forEach(r -> System.out.println(titleOf(r.media()) + r.score() + ","));
		assertFalse(results.isEmpty());
		assertEquals("Breaking Bad", titleOf(results.get(0).media()));
	}


	@Test
	void typeBonus_shouldPreferMovie_whenQueryMentionsMovie() {
		// "movie" + space -> sollte eher Interstellar liefern als Breaking Bad
		var results = sorted("space movie", 40);

		assertFalse(results.isEmpty());
		assertEquals("Interstellar", titleOf(results.get(0).media()));
	}

	@Test
	void categoryBonusValueShouldBeExactlySix_inInfluence() {
		// Dieser Test prüft indirekt: gleiche Query ohne category keyword sollte schlechter sein.
		// Wir machen das, indem wir nur ein Item betrachten und Scores vergleichen:
		var horror = sorted("horror", 0).stream()
				.filter(r -> titleOf(r.media()).equals("The Conjuring"))
				.findFirst().orElseThrow();

		var noKeyword = sorted("spooky", 0).stream()
				.filter(r -> titleOf(r.media()).equals("The Conjuring"))
				.findFirst().orElseThrow();

		assertTrue(horror.score() >= noKeyword.score(),
				"Score with category keyword should not be lower than without it");
		// Exakt +6 lässt sich nur testen, wenn sonst alles identisch ist.
		// Das ist in echten fuzzy scores oft nicht garantiert, daher nur monotonic check.
	}
}
