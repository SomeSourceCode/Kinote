package de.unistuttgart.einf.moviemanager.model;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * A season of a series.
 */
public class Season extends ChildMedia<Series, Season> implements ParentMedia<Episode> {

	private final MediaContainer<Season, Episode> episodes = new MediaContainer<>(this);

	/**
	 * Constructs a new Season with the given season number, title and description.
	 *
	 * @param seasonNumber the season number (must be at least 1)
	 * @param title the title
	 * @param description the description
	 * @throws IllegalArgumentException if seasonNumber is less than 1
	 */
	public Season(int seasonNumber, String title, String description) {
		if (seasonNumber < 1) {
			throw new IllegalArgumentException("Season number must be at least 1");
		}
		setIndex(seasonNumber);
		setTitle(title);
		setDescription(description);
	}

	/**
	 * Constructs a new Season with the given season number and title.
	 *
	 * @param seasonNumber the season number (must be at least 1)
	 * @param title the title
	 * @throws IllegalArgumentException if seasonNumber is less than 1
	 */
	public Season(int seasonNumber, String title) {
		this(seasonNumber, title, null);
	}

	/**
	 * Constructs a new Season with the given season number.
	 *
	 * @param seasonNumber the season number (must be at least 1)
	 * @throws IllegalArgumentException if seasonNumber is less than 1
	 */
	public Season(int seasonNumber) {
		this(seasonNumber, null, null);
	}

	/**
	 * Returns the series this season belongs to.
	 *
	 * @return the series
	 */
	public Series getSeries() {
		return getParent();
	}

	/**
	 * Returns the season number.
	 *
	 * @return the season number
	 */
	public int getSeasonNumber() {
		return getIndex();
	}

	/**
	 * Sets the season number.
	 * This also updates the parent's season mapping accordingly.
	 *
	 * @param seasonNumber the season number
	 */
	public void setSeasonNumber(int seasonNumber) {
		setIndex(seasonNumber);
	}

	/**
	 * Returns an immutable list of all episodes in this season.
	 *
	 * @return the list of episodes
	 */
	public List<Episode> getEpisodes() {
		return getChildren();
	}

	/**
	 * Adds an episode to this season.
	 *
	 * @param episode the episode to add
	 */
	public void addEpisode(Episode episode) {
		addChild(episode);
	}

	/**
	 * Removes the episode with the given episode number from this season.
	 *
	 * @param episodeNumber the episode number
	 */
	public void removeEpisode(int episodeNumber) {
		removeChild(episodeNumber);
	}

	/**
	 * Returns the episode with the given episode number.
	 *
	 * @param episodeNumber the episode number
	 * @return the episode, or null if no episode with the given episode number exists
	 */
	public Episode getEpisode(int episodeNumber) {
		return getChild(episodeNumber);
	}

	/**
	 * Returns whether an episode with the given episode number exists in this season.
	 *
	 * @param episodeNumber the episode number
	 * @return true if an episode with the given episode number exists, false otherwise
	 */
	public boolean hasEpisode(int episodeNumber) {
		return hasChild(episodeNumber);
	}

	@Override
	public List<Episode> getChildren() {
		return episodes.getChildren();
	}

	@Override
	public Episode getChild(int index) {
		return episodes.getChild(index);
	}

	@Override
	public void addChild(Episode child) {
		episodes.addChild(child);
	}

	@Override
	public void removeChild(int index) {
		episodes.removeChild(index);
	}

	@Override
	public Iterator<Episode> iterator() {
		return episodes.iterator();
	}

	@Override
	public void setWatched(boolean watched) {
		for (final Episode episode : episodes.getChildren()) {
			episode.setWatched(watched);
		}
	}

	@Override
	public boolean isWatched() {
		return getStatus() == Status.WATCHED;
	}

	@Override
	public Status getStatus() {
		final int watchedEpisodeCount = (int) episodes.getChildren().stream().filter(Episode::isWatched).count();
		if (watchedEpisodeCount == 0) {
			return Status.UNWATCHED;
		}
		if (watchedEpisodeCount == episodes.getChildren().size()) {
			return Status.WATCHED;
		}
		return Status.WATCHING;
	}

	@Override
	public String toString() {
		final String title = getTitle();
		if (title != null && !title.isBlank()) {
			return "Season " + getSeasonNumber() + ": " + title;
		}
		return "Season " + getSeasonNumber();
	}

}
