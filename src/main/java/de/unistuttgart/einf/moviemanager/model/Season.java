package de.unistuttgart.einf.moviemanager.model;

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
		setNumber(seasonNumber);
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

	@Override
	public List<Episode> getChildren() {
		return episodes.getChildren();
	}

	@Override
	public Episode getChild(int number) {
		return episodes.getChild(number);
	}

	@Override
	public void addChild(Episode child) {
		episodes.addChild(child);
	}

	@Override
	public void removeChild(int number) {
		episodes.removeChild(number);
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
	public int getRating() {
		int rating = 0;
		int counter = 0;
		for (Episode episode : this) {
			if (episode.getRating() == -1) {
				continue;
			}
			rating += episode.getRating();
			counter++;
		}
		if (counter == 0) {
			return -1;
		}
		return rating / counter;
	}

	@Override
	public boolean hasRating() {
		for (Episode episode : this) {
			if (!episode.hasRating()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		final String title = getTitle();
		if (title != null && !title.isBlank()) {
			return "Season " + getNumber() + ": " + title;
		}
		return "Season " + getNumber();
	}

}
