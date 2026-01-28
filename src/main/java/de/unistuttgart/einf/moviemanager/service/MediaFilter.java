package de.unistuttgart.einf.moviemanager.service;

import de.unistuttgart.einf.moviemanager.model.*;
import de.unistuttgart.einf.moviemanager.model.age.AgeRating;
import de.unistuttgart.einf.moviemanager.model.age.AgeRatings;
import de.unistuttgart.einf.moviemanager.model.age.RatingSystem;

import java.util.function.Predicate;

public class MediaFilter {

	private final Predicate<TopLevelMedia> predicate;
	private final String description;

	private MediaFilter(Predicate<TopLevelMedia> predicate, String description) {
		this.predicate = predicate;
		this.description = description;
	}

	@Override
	public String toString() {
		return description;
	}

	public boolean test(TopLevelMedia media) {
		return predicate.test(media);
	}

	public MediaFilter and(MediaFilter other) {
		Predicate<TopLevelMedia> andPredicate = this.predicate.and(other.predicate);
		String andDescription = "(" + this.description + " AND " + other.description + ")";
		return new MediaFilter(andPredicate, andDescription);
	}

	public MediaFilter or(MediaFilter other) {
		Predicate<TopLevelMedia> orPredicate = this.predicate.or(other.predicate);
		String orDescription = "(" + this.description + " OR " + other.description + ")";
		return new MediaFilter(orPredicate, orDescription);
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

	public static MediaFilter isCategory(Category category) {
		Predicate<TopLevelMedia> predicate = media -> media.getCategory() == category;
		String description = "isCategory(" + category.name() + ")";
		return new MediaFilter(predicate, description);
	}

	public static MediaFilter hasStatus(Status status) {
		Predicate<TopLevelMedia> predicate = media -> media.getStatus() == status;
		String description = "hasStatus(" + status.name() + ")";
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
		String description = "isAgeRatingAtLeast(" + rating.getLabel() + ")";
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
		String description = "isAgeRatingAtMost(" + rating.getLabel() + ")";
		return new MediaFilter(predicate, description);
	}

	public static MediaFilter minRating(int rating) {
		Predicate<TopLevelMedia> predicate = media -> media.getRating() >= rating;
		String description = "minRating(" + rating + ")";
		return new MediaFilter(predicate, description);
	}

	public static MediaFilter maxRating(int rating) {
		Predicate<TopLevelMedia> predicate = media -> media.getRating() <= rating;
		String description = "maxRating(" + rating + ")";
		return new MediaFilter(predicate, description);
	}

}
