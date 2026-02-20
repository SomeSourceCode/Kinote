package de.unistuttgart.einf.moviemanager.command.argument;

import de.unistuttgart.einf.moviemanager.command.CommandNode;
import de.unistuttgart.einf.moviemanager.command.SuggestionProvider;
import de.unistuttgart.einf.moviemanager.command.Token;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The base class for all arguments.
 *
 * @param <T> the type of the argument value
 */
public abstract class Argument<T> extends CommandNode {

	private final List<String> suggestions;
	private final SuggestionProvider suggestionProvider;
	private final boolean replaceSuggestions;

	/**
	 * Constructs a new argument from the given builder
	 *
	 * @param builder the builder
	 */
	protected Argument(Builder<? extends Argument<T>> builder) {
		super(builder);
		this.suggestions = new ArrayList<>(builder.suggestions);
		this.suggestionProvider = builder.suggestionProvider;
		this.replaceSuggestions = builder.replaceSuggestions;
	}

	@Override
	public abstract T parse(Token token);

	@Override
	public List<String> getSuggestions() {
		final List<String> result = replaceSuggestions
				? new ArrayList<>() : new ArrayList<>(getDefaultSuggestions());
		result.addAll(suggestions);
		if (suggestionProvider != null) {
			List<String> providedSuggestions = suggestionProvider.getSuggestions();
			if (providedSuggestions != null) {
				result.addAll(providedSuggestions);
			}
		}
		return result;
	}

	/**
	 * Returns the default suggestions for this argument.
	 *
	 * @return the default suggestions
	 */
	public List<String> getDefaultSuggestions() {
		return Collections.emptyList();
	}

	/**
	 * The builder for arguments.
	 *
	 * @param <T> the type of argument
	 */
	public static abstract class Builder<T extends Argument<?>> extends CommandNode.Builder<T> {

		private final List<String> suggestions = new ArrayList<>();
		private SuggestionProvider suggestionProvider;
		private boolean replaceSuggestions = false;

		/**
		 * Constructs a new argument builder with the given name.
		 *
		 * @param name the name of the argument
		 */
		protected Builder(String name) {
			super(name);
		}

		/**
		 * Adds the given suggestion to the list of suggestions.
		 *
		 * @param suggestion the suggestion
		 * @return this builder for chaining
		 * @throws IllegalArgumentException if suggestion is null
		 */
		public Builder<T> withSuggestion(String suggestion) {
			if (suggestion == null) {
				throw new IllegalArgumentException("suggestion must be non-null");
			}
			this.suggestions.add(suggestion);
			return this;
		}

		/**
		 * Adds the given suggestions to the list of suggestions.
		 *
		 * @param suggestions the suggestions
		 * @return this builder for chaining
		 * @throws IllegalArgumentException if suggestions is null or contains null
		 */
		public Builder<T> withSuggestions(List<String> suggestions) {
			if (suggestions == null) {
				throw new IllegalArgumentException("suggestions must be non-null");
			}
			if (suggestions.stream().anyMatch(s -> s == null)) {
				throw new IllegalArgumentException("suggestions must not contain null elements");
			}
			this.suggestions.addAll(suggestions);
			return this;
		}

		/**
		 * Adds the given suggestions to the list of suggestions.
		 *
		 * @param suggestions the suggestions
		 * @return this builder for chaining
		 * @throws IllegalArgumentException if suggestions is null or contains null
		 */
		public Builder<T> withSuggestions(String... suggestions) {
			return withSuggestions(List.of(suggestions));
		}

		/**
		 * Sets the suggestion provider for this argument.
		 *
		 * @param suggestionProvider the suggestion provider
		 * @return this builder for chaining
		 * @throws IllegalArgumentException if suggestionProvider is null
		 */
		public Builder<T> withSuggestions(SuggestionProvider suggestionProvider) {
			if (suggestionProvider == null) {
				throw new IllegalArgumentException("suggestionProvider must be non-null");
			}
			this.suggestionProvider = suggestionProvider;
			return this;
		}

		/**
		 * Sets whether to replace the default suggestions with the provided ones.
		 *
		 * @param replace whether to replace the default suggestions
		 * @return this builder for chaining
		 */
		public Builder<T> replaceSuggestions(boolean replace) {
			this.replaceSuggestions = replace;
			return this;
		}

		/**
		 * A shorthand for {@link #replaceSuggestions(boolean) replaceSuggestions(true)}.
		 *
		 * @return this builder for chaining
		 */
		public Builder<T> replaceSuggestions() {
			return replaceSuggestions(true);
		}

	}

}
