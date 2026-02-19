package de.unistuttgart.einf.moviemanager.service;

import de.unistuttgart.einf.moviemanager.model.*;
import de.unistuttgart.einf.moviemanager.model.age.AgeRating;
import de.unistuttgart.einf.moviemanager.model.age.RatingSystem;

import java.util.function.Predicate;

public class MediaFilter {

	private enum Type {

		OR(1),
		AND(2),
		NOT(3),
		LEAF(4);

		final int precedence;

		Type(int precedence) {
			this.precedence = precedence;
		}

	}

	private final Predicate<TopLevelMedia> predicate;
	private final Type type;
	private final String leafDescription;
	private final MediaFilter left;
	private final MediaFilter right;

	private MediaFilter(Predicate<TopLevelMedia> predicate, Type type, MediaFilter left, MediaFilter right) {
		this.predicate = predicate;
		this.type = type;
		this.leafDescription = null;
		this.left = left;
		this.right = right;
	}

	private MediaFilter(Predicate<TopLevelMedia> predicate, String description) {
		this.predicate = predicate;
		this.type = Type.LEAF;
		this.leafDescription = description;
		this.left = null;
		this.right = null;
	}

	public boolean test(TopLevelMedia media) {
		return predicate.test(media);
	}

	public MediaFilter negate() {
		if (this.type == Type.NOT) {
			return this.left;
		}
		return new MediaFilter(this.predicate.negate(), Type.NOT, this, null);
	}

	public MediaFilter and(MediaFilter other) {
		if (other == null) {
			return this;
		}
		return new MediaFilter(this.predicate.and(other.predicate), Type.AND, this, other);
	}

	public MediaFilter or(MediaFilter other) {
		if (other == null) {
			return this;
		}
		return new MediaFilter(this.predicate.or(other.predicate), Type.OR, this, other);
	}

	@Override
	public String toString() {
		return toString(0);
	}

	private String toString(int parentPrecedence) {
		String text;
		switch (type) {
			case LEAF -> text = leafDescription;
			case NOT -> text = "NOT " + left.toString(type.precedence);
			case AND -> text = left.toString(type.precedence) + " AND " + right.toString(type.precedence);
			case OR -> text = left.toString(type.precedence) + " OR " + right.toString(type.precedence);
			default -> text = "";
		}
		if (type.precedence < parentPrecedence) {
			return "(" + text + ")";
		}
		return text;
	}

	public static MediaFilter isMovie() {
		Predicate<TopLevelMedia> predicate =  media -> media instanceof Movie;
		String description = "isMovie";
		return new MediaFilter(predicate, description);
	}

	public static MediaFilter isSeries() {
		Predicate<TopLevelMedia> predicate =  media -> media instanceof Series;
		String description = "isSeries";
		return new MediaFilter(predicate, description);
	}

	public static MediaFilter containsGenre(Genre genre) {
		Predicate<TopLevelMedia> predicate = media -> media.getGenres().contains(genre);
		String description = "Genre = " + genre.name();
		return new MediaFilter(predicate, description);
	}

	public static MediaFilter hasStatus(Status status) {
		Predicate<TopLevelMedia> predicate = media -> media.getStatus() == status;
		String description = "Status = " + status.name();
		return new MediaFilter(predicate, description);
	}

	public static <E extends Enum<E> & AgeRating> MediaFilter isAgeRatingAtLeast(E rating) {
		Predicate<TopLevelMedia> predicate = media -> {
			if (!media.hasAgeRating())
				return true;

			RatingSystem ratingSystem = rating.getSystem();
			AgeRating mediaAgeRating = media.getAgeRating(ratingSystem);

			return AgeRating.compare(mediaAgeRating, rating) >= 0;

		};
		String description = "AgeRating >= " + rating.getLabel();
		return new MediaFilter(predicate, description);
	}

	public static <E extends Enum<E> & AgeRating> MediaFilter isAgeRatingAtMost(E rating) {
		Predicate<TopLevelMedia> predicate = media -> {
			if (!media.hasAgeRating())
				return true;

			RatingSystem ratingSystem = rating.getSystem();
			AgeRating mediaAgeRating = media.getAgeRating(ratingSystem);

			return AgeRating.compare(mediaAgeRating, rating) <= 0;

		};
		String description = "AgeRating <= " + rating.getLabel();
		return new MediaFilter(predicate, description);
	}

	public static MediaFilter isAgeRatingAtLeast(int rating, SettingsService settingsService) {
		Predicate<TopLevelMedia> predicate = media -> {
			final RatingSystem system = settingsService.getSettings().getRatingSystem();
			if (!media.hasAgeRating()) {
				return true;
			}
			return media.getAgeRating(system).getMinimumAge() >= rating;
		};
		String description = "AgeRating >= " + rating;
		return new MediaFilter(predicate, description);
	}

	public static MediaFilter isAgeRatingAtMost(int rating, SettingsService settingsService) {
		Predicate<TopLevelMedia> predicate = media -> {
			final RatingSystem system = settingsService.getSettings().getRatingSystem();
			if (!media.hasAgeRating()) {
				return true;
			}
			return media.getAgeRating(system).getMinimumAge() <= rating;
		};
		String description = "AgeRating <= " + rating;
		return new MediaFilter(predicate, description);
	}

	public static MediaFilter minRating(int rating) {
		Predicate<TopLevelMedia> predicate = media -> media.getRating() >= rating;
		String description = "Rating >= " + rating;
		return new MediaFilter(predicate, description);
	}

	public static MediaFilter maxRating(int rating) {
		Predicate<TopLevelMedia> predicate = media -> media.getRating() <= rating;
		String description = "Rating <= " + rating;
		return new MediaFilter(predicate, description);
	}

}
