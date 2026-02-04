package de.unistuttgart.einf.moviemanager.model;

import de.unistuttgart.einf.moviemanager.model.age.AgeRating;
import de.unistuttgart.einf.moviemanager.model.age.RatingSystem;

import java.util.*;

/**
 * A series.
 *
 * @see TopLevelMedia
 */
public final class Series extends MediaBase implements ParentMedia<Season>, TopLevelMedia {

	private final MediaContainer<Series, Season> seasons = new MediaContainer<>(this);
	private final Set<Genre> genres = EnumSet.noneOf(Genre.class);

	private final UUID id;

	/**
	 * Constructs a new Series with the given id.
	 *
	 * @param id the uuid
	 */
	public Series(UUID id) {
		this.id = id != null ? id : UUID.randomUUID();
	}

	/**
	 * Constructs a new Series with the given id, title and description.
	 *
	 * @param title the title
	 * @param description the description
	 */
	public Series(UUID id, String title, String description) {
		setTitle(title);
		setDescription(description);

		if (id == null)
			this.id = UUID.randomUUID();
		else
			this.id = id;
	}

	/**
	 * Constructs a new Series with the given title and description.
	 *
	 * @param title the title
	 * @param description the description
	 */
	public Series(String title, String description) {
		this(null, title, description);
	}

	/**
	 * Constructs a new Series with the given title.
	 *
	 * @param title the title
	 */
	public Series(String title) {
		this(null, title, null);
	}

	/**
	 * Constructs a new Series with no title or description.
	 */
	public Series() {
		this(null, null, null);
	}

	@Override
	public UUID getId() {
		return this.id;
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
	public Set<Genre> getGenres() {
		return EnumSet.copyOf(genres);
	}

	@Override
	public boolean hasGenre(Genre genre) {
		return genres.contains(genre);
	}

	@Override
	public void addGenre(Genre genre) {
		genres.add(genre);
	}

	@Override
	public void removeGenre(Genre genre) {
		genres.remove(genre);
	}

	@Override
	public AgeRating getAgeRating(RatingSystem system) {
		return AgeRating.max(getChildren().stream()
				.map(season -> season.getAgeRating(system))
				.toList());
	}

	@Override
	public boolean hasAgeRating() {
		return getChildren().stream().anyMatch(Media::hasAgeRating);
	}

	@Override
	public boolean hasAgeRating(RatingSystem system) {
		return getChildren().stream().anyMatch(season -> season.hasAgeRating(system));
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
