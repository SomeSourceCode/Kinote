package de.unistuttgart.einf.moviemanager.service.search;

import de.unistuttgart.einf.moviemanager.model.*;

import de.unistuttgart.einf.moviemanager.model.TopLevelMedia;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class MediaSearchServiceTest {

	private Set<TopLevelMedia> catalog;

	@BeforeEach
	void setUp() {
		// --- Movies ---
		Movie interstellar = new Movie(
				UUID.randomUUID(),
				"Interstellar",
				"A team travels through a wormhole in space to save humanity.",
				false
		);
		interstellar.addGenre(Genre.SCIENCE_FICTION);

		Movie conjuring = new Movie(
				UUID.randomUUID(),
				"The Conjuring",
				"Paranormal investigators help a family terrorized by a dark presence.",
				false
		);
		conjuring.addGenre(Genre.HORROR);

		Movie godfather = new Movie(
				UUID.randomUUID(),
				"The Godfather",
				"A crime family dynasty and the rise of Michael Corleone.",
				false
		);
		godfather.addGenre(Genre.CRIME);

		// --- Series + children ---
		Series breakingBad = new Series(
				UUID.randomUUID(),
				"Breaking Bad",
				"A chemistry teacher turns to cooking meth to secure his family's future."
		);
		breakingBad.addGenre(Genre.CRIME);

		Season bbS1 = new Season(1, "Season 1", "Walter's first steps into the drug world.");
		bbS1.addChild(new Episode(1, "Pilot", "A teacher starts cooking meth.", false));
		bbS1.addChild(new Episode(2, "Cat's in the Bag...", "They deal with the aftermath.", false));
		breakingBad.addChild(bbS1);

		Season bbS2 = new Season(2, "Season 2", "The business grows and consequences escalate.");
		bbS2.addChild(new Episode(1, "Seven Thirty-Seven", "A deal, a threat, and rising tension.", false));
		breakingBad.addChild(bbS2);

		Series office = new Series(
				UUID.randomUUID(),
				"The Office",
				"A mockumentary sitcom about office workers and awkward humor."
		);
		office.addGenre(Genre.COMEDY);

		Season offS1 = new Season(1, "Season 1", "New manager, new problems.");
		offS1.addChild(new Episode(1, "Pilot", "Welcome to the office.", false));
		office.addChild(offS1);

		catalog = new HashSet<>(List.of(interstellar, conjuring, godfather, breakingBad, office));
	}

	// --- helpers ---

	private Set<MediaSearchService.ScoredMedia> rawSearch(String query, int threshold) {
		return MediaSearchService.search(query, catalog, threshold);
	}

	private List<MediaSearchService.ScoredMedia> sortedResults(String query, int threshold) {
		return rawSearch(query, threshold).stream()
				.sorted(Comparator.comparingInt(MediaSearchService.ScoredMedia::score).reversed()
						.thenComparing(result -> titleOf(result.media())))
				.toList();
	}

	private MediaSearchService.ScoredMedia topResult(String query, int threshold) {
		return sortedResults(query, threshold).stream().findFirst().orElse(null);
	}

	private static String titleOf(TopLevelMedia m) {
		// Passe das an: getTitle() oder title()
		// return m.title();
		return m.getTitle();
	}

	// --- tests ---

	@Test
	void titleMatch_shouldReturnCorrectTopResult() {
		var top = topResult("interstellar", 70);
		System.out.print(top.score() + " scr");
		assertNotNull(top);
		assertEquals("Interstellar", titleOf(top.media()));
		assertTrue(top.score() >= 80, "Title match should score high");
	}

	@Test
	void descriptionMatch_shouldFindMovieEvenIfTitleNotMentioned() {
		var top = topResult("wormhole space save humanity", 30);

		assertNotNull(top);
		assertEquals("Interstellar", titleOf(top.media()));
	}

	@Test
	void categoryKeywordBonus_shouldBoostCorrectCategory() {
		// Query enthält "horror" -> Horror Movie sollte oben landen
		var top = topResult("horror spooky", 0);

		System.out.print(top.score() + " scoreee");
		assertNotNull(top);
		assertEquals("The Conjuring", titleOf(top.media()));
	}

	@Test
	void seriesIntent_shouldPreferSeriesWhenQueryMentionsSeries() {
		var top = topResult("crime series", 50);

		assertNotNull(top);
		assertEquals("Breaking Bad", titleOf(top.media()));
	}

	@Test//HERE
	void childEpisodeTitleMatch_shouldReturnSeriesAsTopLevelResult() {
		// Episode Title existiert nur unter Breaking Bad
		var top = topResult("seven thirty seven", 0);

		assertNotNull(top);
		assertEquals("Breaking Bad", titleOf(top.media()));
	}

	@Test
	void childEpisodeDescriptionMatch_shouldReturnSeriesAsTopLevelResult() {
		// Phrase steckt nur in der Episode-Description
		var top = topResult("a deal a threat rising tension", 55);

		assertNotNull(top);
		assertEquals("Breaking Bad", titleOf(top.media()));
	}

	@Test
	void comedyKeywords_shouldPreferComedySeries() {
		var top = topResult("funny sitcom office", 50);

		assertNotNull(top);
		assertEquals("The Office", titleOf(top.media()));
	}

	@Test
	void threshold_shouldFilterLowQualityMatches() {
		// random query -> bei hohem threshold nix
		var results = rawSearch("qwertyuiop asdfghjkl", 90);

		assertTrue(results.isEmpty());
	}


	@Test
	void resultsContainUniqueTopLevelMedia() {
		var results = rawSearch("crime", 0);

		// Set<ScoredMedia> könnte theoretisch doppelte media enthalten wenn equals/hashCode nicht sauber sind.
		// Wir prüfen daher, ob media innerhalb der Resultmenge eindeutig sind.
		var mediaIds = results.stream()
				.map(r -> r.media())
				.collect(Collectors.toSet());

		assertEquals(mediaIds.size(), results.size(), "Each media should appear at most once in the results");
	}

	@Test
	void scoresShouldRespectThreshold() {
		int threshold = 70;
		var results = rawSearch("crime", threshold);

		assertTrue(results.stream().allMatch(r -> r.score() >= threshold),
				"All returned results must have score >= threshold");
	}
}
