package de.unistuttgart.einf.moviemanager.service;

import de.unistuttgart.einf.moviemanager.io.FileRepository;
import de.unistuttgart.einf.moviemanager.model.Settings;

/**
 * This class is responsible for managing the settings of the application.
 * It provides methods to load and save the settings from/to a file repository.
 * The settings are stored in memory and can be accessed and modified through the getSettings() method.
 */
public class SettingsService {

	private Settings settings;
	private final FileRepository<Settings> repository;

	/**
	 * Creates a new SettingsService with the given file repository. The settings will be loaded from the repository.
	 *
	 * @param repository the file repository to load and save the settings
	 */
	public SettingsService(FileRepository<Settings> repository) {
		if (repository == null) {
			throw new IllegalArgumentException("repository must not be null");
		}
		this.repository = repository;
		reload();
	}

	/**
	 * Reloads the settings from the repository.
	 * If the repository does not contain any settings, a new Settings object will be created.
	 */
	public void reload() {
		settings = repository.load();
		if (settings == null) {
			settings = new Settings();
		}
	}

	/**
	 * Saves the current settings to the repository.
	 */
	public void save() {
		repository.save(settings);
	}

	/**
	 * Returns the current settings. Modifying the returned settings will modify the settings in memory, but will not save them to the repository.
	 * To save the modified settings, call the save() method.
	 *
	 * @return the current settings
	 */
	public Settings getSettings() {
		return settings;
	}

}
