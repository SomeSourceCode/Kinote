package de.unistuttgart.einf.moviemanager.dbimport;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UrlParserTest {

	private final String movieUrl = "https://www.themoviedb.org/movie/1317288-marty-supreme?language=de-DE";
	private final String seriesUrl = "https://www.themoviedb.org/tv/224372-a-knight-of-the-seven-kingdoms?language=da";
	private final String seasonUrl = "www.themoviedb.org/tv/224372-a-knight-of-the-seven-kingdoms/season/1?language=en-US";
	private final String episodeUrl = "themoviedb.org/tv/224372-a-knight-of-the-seven-kingdoms/season/1/episode/19?language=fr";

	private final String invalidUrl = "abc.google.tv/manu?language=b";

	@Test
	void testUrlValidation() {
		assertTrue(UrlParser.isValid(movieUrl));
		assertTrue(UrlParser.isValid(seriesUrl));
		assertTrue(UrlParser.isValid(seasonUrl));
		assertTrue(UrlParser.isValid(episodeUrl));

		assertFalse(UrlParser.isValid(invalidUrl));
		assertFalse(UrlParser.isValid("www.themoviedb.org/film/1234-ein-film/"));
		assertFalse(UrlParser.isValid("www.themoviedb.org/movie/ein-film/"));
		assertFalse(UrlParser.isValid("www.themoviedb.org/movie/1234ein-film/"));
	}

	@Test
	void testParseMediaType() {
		assertEquals(MediaType.MOVIE, UrlParser.getMediaType(movieUrl));
		assertEquals(MediaType.SERIES, UrlParser.getMediaType(seriesUrl));
		assertEquals(MediaType.SEASON, UrlParser.getMediaType(seasonUrl));
		assertEquals(MediaType.EPISODE, UrlParser.getMediaType(episodeUrl));

		assertNull(UrlParser.getMediaType(invalidUrl));
	}

	@Test
	void testParseMediaLanguage() {
		assertEquals(Language.GERMAN, UrlParser.getMediaLanguage(movieUrl));
		assertEquals(Language.DANISH, UrlParser.getMediaLanguage(seriesUrl));
		assertEquals(Language.ENGLISH, UrlParser.getMediaLanguage(seasonUrl));
		assertEquals(Language.FRENCH, UrlParser.getMediaLanguage(episodeUrl));

		assertNull(UrlParser.getMediaLanguage(invalidUrl));
	}

	@Test
	void testParseMovieId() {
		assertEquals(1317288, UrlParser.getMovieTmdbId(movieUrl));

		assertEquals(-1, UrlParser.getMovieTmdbId(seriesUrl));
		assertEquals(-1, UrlParser.getMovieTmdbId(invalidUrl));
	}

	@Test
	void testParseSeriesId() {
		assertEquals(224372, UrlParser.getSeriesTmdbId(seriesUrl));
		assertEquals(224372, UrlParser.getSeriesTmdbId(seasonUrl));
		assertEquals(224372, UrlParser.getSeriesTmdbId(episodeUrl));

		assertEquals(-1, UrlParser.getSeriesTmdbId(movieUrl));
		assertEquals(-1, UrlParser.getSeriesTmdbId(invalidUrl));
	}

	@Test
	void testParseSeasonNumber() {
		assertEquals(1, UrlParser.getSeasonNumber(seasonUrl));
		assertEquals(1, UrlParser.getSeasonNumber(episodeUrl));

		assertEquals(-1, UrlParser.getSeasonNumber(movieUrl));
		assertEquals(-1, UrlParser.getSeasonNumber(seriesUrl));
		assertEquals(-1, UrlParser.getSeasonNumber(invalidUrl));
	}

	@Test
	void testParseEpisodeNumber() {
		assertEquals(19, UrlParser.getEpisodeNumber(episodeUrl));

		assertEquals(-1, UrlParser.getEpisodeNumber(movieUrl));
		assertEquals(-1, UrlParser.getEpisodeNumber(seriesUrl));
		assertEquals(-1, UrlParser.getEpisodeNumber(seasonUrl));
		assertEquals(-1, UrlParser.getEpisodeNumber(invalidUrl));
	}

}
