package de.unistuttgart.einf.moviemanager.model;

import de.unistuttgart.einf.moviemanager.dbimport.Language;
import de.unistuttgart.einf.moviemanager.model.age.RatingSystem;

import java.util.Objects;

/**
 * This class represents the settings of the application. It contains the rating system, the import language and the TMDB API key.
 */
public class Settings {

	private RatingSystem ratingSystem = RatingSystem.FSK;
	private Language importLanguage = Language.GERMAN;
	private String tmdbApiKey;

	/**
	 * Returns the current default rating system
	 *
	 * @return the rating system
	 */
	public RatingSystem getRatingSystem() {
		return ratingSystem;
	}

	/**
	 * Sets the default rating system. If the given rating system is null, the current rating system will not be changed.
	 *
	 * @param ratingSystem the rating system
	 */
	public void setRatingSystem(RatingSystem ratingSystem) {
		if (ratingSystem == null) {
			return;
		}
		this.ratingSystem = ratingSystem;
	}

	/**
	 * Returns the current default import language
	 *
	 * @return the import language
	 */
	public Language getImportLanguage() {
		return importLanguage;
	}

	/**
	 * Sets the default import language. If the given import language is null, the current import language will not be changed.
	 *
	 * @param importLanguage the import language
	 */
	public void setImportLanguage(Language importLanguage) {
		if (importLanguage == null) {
			return;
		}
		this.importLanguage = importLanguage;
	}

	/**
	 * Returns the TMDb API key.
	 *
	 * @return the TMDb API key
	 */
	public String getTmdbApiKey() {
		return tmdbApiKey;
	}

	/**
	 * Sets the TMDb API key.
	 *
	 * @param tmdbApiKey the TMDb API key
	 */
	public void setTmdbApiKey(String tmdbApiKey) {
		this.tmdbApiKey = tmdbApiKey;
	}

	@Override
	public int hashCode() {
		return Objects.hash(importLanguage, ratingSystem, tmdbApiKey);
	}

	@Override
	public boolean equals(Object other) {
		if (this.getClass() != other.getClass()) {
			return false;
		}
		Settings otherSettings = (Settings) other;
		return Objects.equals(this.importLanguage, otherSettings.importLanguage)
				&& Objects.equals(this.ratingSystem, otherSettings.ratingSystem)
				&& Objects.equals(this.tmdbApiKey, otherSettings.tmdbApiKey);
	}

	@Override
	public String toString() {
		return "Settings[importLanguage = " + importLanguage + ", ratingSystem = " + ratingSystem + ", tmdbApiKey = " + tmdbApiKey + "]";
	}

}
