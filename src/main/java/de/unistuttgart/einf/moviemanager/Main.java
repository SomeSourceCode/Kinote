package de.unistuttgart.einf.moviemanager;

import de.unistuttgart.einf.moviemanager.cli.Cli;
import de.unistuttgart.einf.moviemanager.io.FileRepository;
import de.unistuttgart.einf.moviemanager.io.TopLevelMediaSetSerializer;
import de.unistuttgart.einf.moviemanager.service.MediaService;

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

		final MediaService mediaService = new MediaService(
				new FileRepository<>(mediaFile, new TopLevelMediaSetSerializer())
		);
		Runtime.getRuntime().addShutdownHook(new Thread(mediaService::save));

		new Cli(mediaService).run();
	}

}
