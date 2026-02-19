package de.unistuttgart.einf.moviemanager.cli.commands;

import de.unistuttgart.einf.moviemanager.command.argument.StringArgument;

import java.util.regex.Pattern;

/**
 * A factory for common argument types.
 */
public class Arguments {

	/**
	 * Returns a string argument builder that validates against valid TMDb URLs.
	 *
	 * @param name the name
	 * @return the string argument builder
	 */
	public static StringArgument.Builder tmdbUrl(String name) {
		return StringArgument.create(name)
				.withPattern(Pattern.compile("(https?://)?(www\\.)?themoviedb\\.org/((movie)|(tv))/\\d+-.*"));
	}

}

