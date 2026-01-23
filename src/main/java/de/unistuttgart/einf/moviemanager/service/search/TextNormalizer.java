package de.unistuttgart.einf.moviemanager.service.search;

import java.util.Locale;

public class TextNormalizer {

	private TextNormalizer() {}

	public static String normalize(String s) {
		if (s == null)
			return "";
		return s.toLowerCase(Locale.ROOT)
				.replaceAll("[^\\p{L}\\p{Nd}\\s]+", " ")
				.replaceAll("\\s+", " ")
				.trim();
	}
}
