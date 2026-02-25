package de.unistuttgart.einf.moviemanager;

import de.unistuttgart.einf.moviemanager.cli.Cli;
import de.unistuttgart.einf.moviemanager.io.*;
import de.unistuttgart.einf.moviemanager.model.Settings;
import de.unistuttgart.einf.moviemanager.service.CommandHistoryService;
import de.unistuttgart.einf.moviemanager.service.MediaService;
import de.unistuttgart.einf.moviemanager.service.SettingsService;

import java.nio.file.Path;

/**
 * The main class of the Kinote application, containing the main method.
 */
public class Main {

	/**
	 * The main entry point of the application. Initializes services and starts the cli.
	 */
	static void main() {
		final String userHome = System.getProperty("user.home");
		final Path appDataDir = Path.of(userHome, ".kinote");

		final Path mediaFile = appDataDir.resolve("media.json");
		final Path settingsFile = appDataDir.resolve("settings.json");
		final Path commandHistoryFile = appDataDir.resolve("command-history.json");

		final MediaService mediaService = new MediaService(
				new FileRepository<>(mediaFile, new TopLevelMediaSetSerializer())
		);
		Runtime.getRuntime().addShutdownHook(new Thread(mediaService::save));

		final SettingsService settingsService = new SettingsService(
				new FileRepository<>(settingsFile, DefaultGsonSerializer.createFor(Settings.class))
		);
		Runtime.getRuntime().addShutdownHook(new Thread(settingsService::save));

		final CommandHistoryService commandHistoryService = new CommandHistoryService(
				new FileRepository<>(commandHistoryFile, DefaultGsonSerializer.createForList(String.class))
		);
		Runtime.getRuntime().addShutdownHook(new Thread(commandHistoryService::save));

		new Cli(mediaService, settingsService, commandHistoryService).run();
	}

}
