package de.unistuttgart.einf.moviemanager.model;

import java.util.Iterator;
import java.util.List;

/**
 * A series.
 *
 * @see TopLevelMedia
 */
public final class Series extends MediaBase implements ParentMedia<Season>, TopLevelMedia {

	private final MediaContainer<Series, Season> seasons = new MediaContainer<>(this);
	private Category category;

	/**
	 * Constructs a new Series with the given title and description.
	 *
	 * @param title the title
	 * @param description the description
	 * @param category the category
	 */
	public Series(String title, String description, Category category) {
		setTitle(title);
		setDescription(description);
		setCategory(category);
	}

	/**
	 * Constructs a new Series with the given title.
	 *
	 * @param title the title
	 */
	public Series(String title) {
		this(title, null, null);
	}

	/**
	 * Constructs a new Series with no title or description.
	 */
	public Series() {
		this(null, null, null);
	}

	@Override
	public List<Season> getChildren() {
		return seasons.getChildren();
	}

	@Override
	public Season getChild(int number) {
		return seasons.getChild(number);
	}

	@Override
	public void addChild(Season child) {
		seasons.addChild(child);
	}

	@Override
	public void removeChild(int number) {
		seasons.removeChild(number);
	}

	@Override
	public Iterator<Season> iterator() {
		return seasons.iterator();
	}

	@Override
	public Status getStatus() {
		if (getChildren().isEmpty()) {
			return Status.UNWATCHED;
		}

		int watched = 0;
		int unwatched = 0;
		for (Season season : this) {
			final Status seasonStatus = season.getStatus();
			if (seasonStatus == Status.WATCHED) {
				watched++;
			} else if (seasonStatus == Status.UNWATCHED) {
				unwatched++;
			}
		}

		final int seasons = getChildren().size();
		if (unwatched == seasons) {
			return Status.UNWATCHED;
		}
		if (watched == seasons) {
			return Status.WATCHED;
		}
		return Status.WATCHING;
	}

	@Override
	public int getRating() {
		int rating = 0;
		int counter = 0;
		for (Season season : this) {
			for (Episode episode : season) {
				if (episode.getRating() == -1) {
					continue;
				}
				rating += episode.getRating();
				counter++;
			}
		}
		if (counter == 0) {
			return -1;
		}
		return rating / counter;
	}

	@Override
	public boolean hasRating() {
		for (Season season : this) {
			if (!season.hasRating()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int getDuration() {
		return getChildren().stream()
				.mapToInt(Media::getDuration)
				.sum();
	}

	@Override
	public Category getCategory() {
		return category;
	}

	@Override
	public void setCategory(Category category) {
		this.category = category;
	}

	@Override
	public String toString() {
		final String title = getTitle();
		if (title != null && !title.isBlank()) {
			return "Series: " + title;
		}
		return "Series";
	}

}
