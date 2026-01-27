package de.unistuttgart.einf.moviemanager.command.argument;

import de.unistuttgart.einf.moviemanager.command.Token;

import java.util.List;

/**
 * A string argument.
 */
public class StringArgument extends Argument<String> {

	/**
	 * Constructs a new string argument from the given builder.
	 *
	 * @param builder the builder
	 */
	protected StringArgument(Builder builder) {
		super(builder);
	}

	@Override
	public boolean matches(Token token) {
		return true;
	}

	@Override
	public String parse(Token token) {
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

		/**
		 * Constructs a new string argument builder with the given name.
		 *
		 * @param name the name of the argument
		 */
		protected Builder(String name) {
			super(name);
		}

		@Override
		public StringArgument build() {
			return new StringArgument(this);
		}

	}

}
