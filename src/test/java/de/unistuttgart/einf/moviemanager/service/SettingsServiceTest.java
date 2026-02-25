package de.unistuttgart.einf.moviemanager.service;

import de.unistuttgart.einf.moviemanager.dbimport.Language;
import de.unistuttgart.einf.moviemanager.io.DefaultGsonSerializer;
import de.unistuttgart.einf.moviemanager.io.FileRepository;
import de.unistuttgart.einf.moviemanager.model.Settings;
import de.unistuttgart.einf.moviemanager.model.age.RatingSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SettingsServiceTest {

	@TempDir
	Path tempDir = Path.of("temp");
	Path file;

	FileRepository<Settings> fileRepository;
	SettingsService settingsService;
	Settings settings;

	@BeforeEach
	void setup() {
		file = tempDir.resolve("settings.json");
		fileRepository= new FileRepository<>(file, DefaultGsonSerializer.createFor(Settings.class));
		settingsService = new SettingsService(fileRepository);

		settings = new Settings();
		settings.setImportLanguage(Language.ENGLISH);
		settings.setRatingSystem(RatingSystem.MPA);
		settings.setTmdbApiKey("apiKey");

		settingsService.getSettings().setImportLanguage(Language.ENGLISH);
		settingsService.getSettings().setRatingSystem(RatingSystem.MPA);
		settingsService.getSettings().setTmdbApiKey("apiKey");
	}

	@Test
	void testSave() throws IOException {
		assertEquals(settings, settingsService.getSettings());

		settingsService.save();

		assertTrue(Files.exists(file));
		assertFalse(Files.readString(file).isBlank());
	}

	@Test
	void testReload() {
		assertEquals(settings, settingsService.getSettings());

		settingsService.save();
		settingsService.reload();

		assertEquals(settings, settingsService.getSettings());
	}

}
