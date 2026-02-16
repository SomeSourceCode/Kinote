package de.unistuttgart.einf.moviemanager.cli.commands;

import de.unistuttgart.einf.moviemanager.cli.Cli;
import de.unistuttgart.einf.moviemanager.command.Command;
import de.unistuttgart.einf.moviemanager.command.CommandDispatcher;
import de.unistuttgart.einf.moviemanager.command.argument.EnumArgument;
import de.unistuttgart.einf.moviemanager.command.argument.LiteralArgument;
import de.unistuttgart.einf.moviemanager.command.argument.StringArgument;
import de.unistuttgart.einf.moviemanager.dbimport.Language;
import de.unistuttgart.einf.moviemanager.model.age.RatingSystem;

/**
 * The command to view and modify application settings.
 */
public class SettingsCommand {

	/**
	 * Registers the "settings" command to the given dispatcher.
	 *
	 * @param dispatcher the dispatcher
	 * @param cli the cli.
	 */
	public static void register(CommandDispatcher dispatcher, Cli cli) {
		dispatcher.register(Command.create("settings")
				.then(LiteralArgument.create("rating-system")
						.executes(_ -> {
							cli.showInfoDialog("Current rating system: " + cli.getSettingsService().getSettings().getRatingSystem().name());
						})
						.then(EnumArgument.create("system", RatingSystem.class)
								.executes(context -> {
									final RatingSystem system = context.get("system", RatingSystem.class);
									cli.getSettingsService().getSettings().setRatingSystem(system);
								})))
				.then(LiteralArgument.create("tmdb-api-key")
						.executes(context -> {
							final String apiKey = cli.getSettingsService().getSettings().getTmdbApiKey();
							if (apiKey == null) {
								cli.showInfoDialog("No TMDb API key set. You can find your key at https://www.themoviedb.org/settings/api");
								return;
							}
							cli.showInfoDialog("Current TMDb API key: " + apiKey);
						})
						.then(StringArgument.create("key")
								.executes(context -> {
									final String apiKey = context.get("key", String.class);
									cli.getSettingsService().getSettings().setTmdbApiKey(apiKey.isBlank() ? null : apiKey.trim());
								}))
						.then(LiteralArgument.create("unset")
								.executes(_ -> {
									cli.getSettingsService().getSettings().setTmdbApiKey(null);
								})))
				.then(LiteralArgument.create("default-import-lang")
						.executes(_ -> {
							final Language language = cli.getSettingsService().getSettings().getImportLanguage();
							cli.showInfoDialog("Current default import language: " + language.getEnglishName());
						})
						.then(EnumArgument.create("language", Language.class)
								.executes(context -> {
									final Language language = context.get("language", Language.class);
									cli.getSettingsService().getSettings().setImportLanguage(language);
								}))));
	}

}
