package de.unistuttgart.einf.moviemanager.service.search;

import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TextNormalizer {

	private TextNormalizer() {}

	public static String normalize(String text) {
		if (text == null) {
			return "";
		}
		return text.toLowerCase(Locale.ROOT)
				.replaceAll("[^\\p{L}\\p{Nd}\\s]+", " ")
				.replaceAll("\\s+", " ")
				.trim();
	}

	private static final Pattern INTENT_PATTERN = Pattern.compile(
			"\\b(" + SearchConstants.TOP_LEVEL_MEDIA_WORDS.stream()
					.map(Pattern::quote)
					.collect(Collectors.joining("|")) + ")\\b",
			Pattern.CASE_INSENSITIVE
	);

	public static String stripIntentWords(String query) {
		return INTENT_PATTERN.matcher(query)
				.replaceAll(" ")
				.replaceAll("\\s+", " ")
				.trim();
	}

}
