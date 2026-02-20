package de.unistuttgart.einf.moviemanager.command;

import java.util.List;

/**
 * Provider of suggestions for command inputs.
 */
@FunctionalInterface
public interface SuggestionProvider {

	/**
	 * Returns a list of suggestions.
	 *
	 * @return the suggestions
	 */
	List<String> getSuggestions();

}
