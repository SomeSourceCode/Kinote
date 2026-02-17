package de.unistuttgart.einf.moviemanager.command.argument;

import de.unistuttgart.einf.moviemanager.command.CommandParseException;
import de.unistuttgart.einf.moviemanager.command.Token;

import java.util.List;
import java.util.regex.Pattern;

/**
 * A string argument.
 */
public class StringArgument extends Argument<String> {

	private final Pattern pattern;

	/**
	 * Constructs a new string argument from the given builder.
	 *
	 * @param builder the builder
	 */
	protected StringArgument(Builder builder) {
		super(builder);
		this.pattern = builder.pattern;
	}

	/**
	 * Returns the pattern that the argument value must match.
	 *
	 * @return the pattern, or null if no pattern is set
	 */
	public Pattern getPattern() {
		return pattern;
	}

	@Override
	public boolean matches(Token token) {
		return true;
	}

	@Override
	public boolean isValid(Token token) {
		if (pattern == null) {
			return true;
		}
		return pattern.matcher(token.value()).matches();
	}

	@Override
	public String parse(Token token) {
		if (!pattern.matcher(token.value()).matches()) {
			throw new CommandParseException("Invalid string format");
		}
		return token.value();
	}

	@Override
	public List<String> getSuggestions() {
		final List<String> suggestions = super.getSuggestions().stream()
				.map(suggestion -> suggestion.contains(" ") ? "\"" + suggestion + "\"" : suggestion)
				.toList();
		return suggestions.isEmpty() ? List.of("\"") : suggestions;
	}

	/**
	 * Creates a new string argument builder with the given name.
	 *
	 * @param name the name of the argument
	 * @return the argument builder
	 */
	public static Builder create(String name) {
		return new Builder(name);
	}

	/**
	 * The builder for string arguments.
	 */
	public static class Builder extends Argument.Builder<StringArgument> {

		private Pattern pattern;

		/**
		 * Constructs a new string argument builder with the given name.
		 *
		 * @param name the name of the argument
		 */
		protected Builder(String name) {
			super(name);
		}

		/**
		 * Sets the pattern that the argument value must match.
		 *
		 * @param pattern the pattern
		 * @return this builder for chaining
		 */
		public Builder withPattern(Pattern pattern) {
			this.pattern = pattern;
			return this;
		}

		@Override
		public StringArgument build() {
			return new StringArgument(this);
		}

	}

}
