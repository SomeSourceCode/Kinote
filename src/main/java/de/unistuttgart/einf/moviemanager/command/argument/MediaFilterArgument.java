package de.unistuttgart.einf.moviemanager.command.argument;

import de.unistuttgart.einf.moviemanager.command.CommandParseException;
import de.unistuttgart.einf.moviemanager.command.Token;
import de.unistuttgart.einf.moviemanager.model.Genre;
import de.unistuttgart.einf.moviemanager.model.Status;
import de.unistuttgart.einf.moviemanager.model.age.RatingSystem;
import de.unistuttgart.einf.moviemanager.service.MediaFilter;
import de.unistuttgart.einf.moviemanager.service.SettingsService;

import java.util.Arrays;
import java.util.List;

/**
 * A media filter argument.
 */
public class MediaFilterArgument extends Argument<MediaFilter> {

	private final SettingsService settingsService;

	/**
	 * Constructs a new media filter argument from the given builder.
	 *
	 * @param builder the builder
	 */
	protected MediaFilterArgument(Builder builder) {
		super(builder);
		this.settingsService = builder.settingsService;
	}

	@Override
	public boolean matches(Token token) {
		return true;
	}

	@Override
	public boolean isValid(Token token) {
		try {
			new FilterParser(token.value(), settingsService).parse();
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	@Override
	public MediaFilter parse(Token token) {
		try {
			return new FilterParser(token.value(), settingsService).parse();
		} catch (IllegalArgumentException exception) {
			throw new CommandParseException("Invalid media filter: " + exception.getMessage());
		}
	}

	/**
	 * Creates a new media filter argument builder with the given name
	 * and settings service.
	 *
	 * @param name the name of the argument
	 * @return the argument builder
	 */
	public static Builder create(String name, SettingsService settingsService) {
		return new Builder(name, settingsService);
	}

	/**
	 * The builder for media filter arguments.
	 */
	public static class Builder extends Argument.Builder<MediaFilterArgument> {

		private final SettingsService settingsService;

		/**
		 * Constructs a bew media filter argument with the given name.
		 *
		 * @param name the name of the argument
		 */
		protected Builder(String name, SettingsService settingsService) {
			super(name);
			this.settingsService = settingsService;
		}

		@Override
		public MediaFilterArgument build() {
			return new MediaFilterArgument(this);
		}

	}

	private static class FilterParser {

		private final String input;
		private final SettingsService settingsService;
		private int pos = 0;

		public FilterParser(String input, SettingsService settingsService) {
			this.input = input.trim();
			this.settingsService = settingsService;
		}

		public MediaFilter parse() {
			final MediaFilter result = parseOr();
			skipWhitespace();
			if (pos < input.length()) {
				throw new IllegalArgumentException("Unexpected character at end of filter: " + input.substring(pos));
			}
			return result;
		}

		private MediaFilter parseOr() {
			MediaFilter left = parseAnd();
			skipWhitespace();
			while (match("||")) {
				MediaFilter right = parseAnd();
				left = left.or(right);
				skipWhitespace();
			}
			return left;
		}

		private MediaFilter parseAnd() {
			MediaFilter left = parseNot();
			skipWhitespace();
			while (match("&&")) {
				MediaFilter right = parseNot();
				left = left.and(right);
				skipWhitespace();
			}
			return left;
		}

		private MediaFilter parseNot() {
			skipWhitespace();
			if (match("!")) {
				return parseNot().negate();
			}
			return parsePrimary();
		}

		private MediaFilter parsePrimary() {
			skipWhitespace();
			if (match("(")) {
				MediaFilter expression = parseOr();
				skipWhitespace();
				if (!match(")")) {
					throw new IllegalArgumentException("Missing closing parenthesis");
				}
				return expression;
			}
			return parseLeaf();
		}

		private MediaFilter parseLeaf() {
			skipWhitespace();
			String token = readToken().trim();
			if (token.isEmpty()) {
				throw new IllegalArgumentException("Unexpected end of input");
			}

			switch (token.toLowerCase()) {
				case "watched" -> {
					return MediaFilter.hasStatus(Status.WATCHED);
				}
				case "unwatched" -> {
					return MediaFilter.hasStatus(Status.UNWATCHED);
				}
				case "in-progress", "in_progress", "inprogress", "watching" -> {
					return MediaFilter.hasStatus(Status.WATCHING);
				}
				case "movie" -> {
					return MediaFilter.isMovie();
				}
				case "series" -> {
					return MediaFilter.isSeries();
				}
			}

			if (token.startsWith("[") && token.endsWith("]")) {
				String genreName = token.substring(1, token.length() - 1);
				return Arrays.stream(Genre.values())
						.filter(g -> g.name().equalsIgnoreCase(genreName) || g.name().replaceAll("_", " ").equalsIgnoreCase(genreName))
						.findFirst()
						.map(MediaFilter::containsGenre)
						.orElseThrow(() -> new IllegalArgumentException("Unknown genre: " + genreName));
			}

			if (token.equalsIgnoreCase("age") || token.equalsIgnoreCase("rating")) {
				String operator = readOperator();
				if (operator.isEmpty()) {
					throw new IllegalArgumentException("Missing operator after " + token);
				}

				final int number = readInt();

				final boolean isAge = token.equalsIgnoreCase("age");
				final boolean isMin = operator.equals(">=") || operator.equals("=>") || operator.equals(">");

				if (isAge) {
					RatingSystem system = settingsService.getSettings().getRatingSystem();
					return isMin ? MediaFilter.isAgeRatingAtLeast(number, settingsService)
							: MediaFilter.isAgeRatingAtMost(number, settingsService);
				} else {
					return isMin ? MediaFilter.minRating(number) : MediaFilter.maxRating(number);
				}
			}

			throw new IllegalArgumentException("Unknown filter criterion: " + token);
		}

		private boolean match(String s) {
			if (input.startsWith(s, pos)) {
				pos += s.length();
				return true;
			}
			return false;
		}

		private void skipWhitespace() {
			while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) {
				pos++;
			}
		}

		private String readToken() {
			int start = pos;
			while (pos < input.length() && (Character.isLetterOrDigit(input.charAt(pos))
					|| List.of(' ', '-', '_', '[', ']').contains(input.charAt(pos)))) {
				pos++;
			}
			return input.substring(start, pos);
		}

		private String readOperator() {
			skipWhitespace();
			if (match(">=")) {
				return ">=";
			}
			if (match("<=")) {
				return "<=";
			}
			if (match("=>")) {
				return "=>";
			}
			if (match("=<")) {
				return "=<";
			}
			return "";
		}

		private int readInt() {
			skipWhitespace();
			final int start = pos;
			while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
				pos++;
			}
			if (start == pos) {
				throw new IllegalArgumentException("Expected number at pos " + pos);
			}
			return Integer.parseInt(input.substring(start, pos));
		}

	}

}
