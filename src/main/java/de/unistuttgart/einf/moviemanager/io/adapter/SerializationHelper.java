package de.unistuttgart.einf.moviemanager.io.adapter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import de.unistuttgart.einf.moviemanager.model.*;
import de.unistuttgart.einf.moviemanager.model.age.AgeRating;
import de.unistuttgart.einf.moviemanager.model.age.RatingSystem;

import static de.unistuttgart.einf.moviemanager.io.adapter.Converter.*;

public class SerializationHelper {

	public static class Keys {

		public static final String TITLE = "title";
		public static final String DESCRIPTION = "description";

		public static final String ID = "id";
		public static final String TYPE = "type";
		public static final String CATEGORY = "category";

		public static final String WATCHED = "watched";
		public static final String DURATION = "duration";
		public static final String RATING = "rating";
		public static final String AGE_RATINGS = "age_ratings";

		public static final String SEASONS = "seasons";
		public static final String EPISODES = "episodes";
		public static final String NUMBER = "number";

	}

	/**
	 * Sets the following attributes on the given JSON object from
	 * the provided media item:
	 * <ul>
	 *     <li>{@link Keys#TITLE}</li>
	 *     <li>{@link Keys#DESCRIPTION}</li>
	 * </ul>
	 *
	 * @param object the JSON object
	 * @param item the media item
	 * @param context the JSON serialization context
	 */
	public static void setMediaAttributesOnJson(JsonObject object, Media item, JsonSerializationContext context) {
		object.addProperty(Keys.TITLE, item.getTitle());
		object.addProperty(Keys.DESCRIPTION, item.getDescription());
	}

	/**
	 * Sets the following attributes on the given media item from
	 * the provided JSON object:
	 * <ul>
	 *     <li>{@link Keys#TITLE}</li>
	 *     <li>{@link Keys#DESCRIPTION}</li>
	 * </ul>
	 *
	 * @param item the media item
	 * @param object the JSON object
	 * @param context the JSON deserialization context
	 */
	public static void setMediaAttributesFromJson(Media item, JsonObject object, JsonDeserializationContext context) {
		item.setTitle(getAsStringOrNull(object, Keys.TITLE));
		item.setDescription(getAsStringOrNull(object, Keys.DESCRIPTION));
	}

	/**
	 * Sets the following attributes on the given JSON object from
	 * the provided media item:
	 * <ul>
	 *     <li>{@link Keys#ID}</li>
	 *     <li>{@link Keys#TYPE}</li>
	 *     <li>{@link Keys#CATEGORY}</li>
	 * </ul>
	 *
	 * @param object the JSON object
	 * @param item the media item
	 * @param context the JSON serialization context
	 */
	public static void setTopLevelMediaAttributesOnJson(JsonObject object, TopLevelMedia item, JsonSerializationContext context) {
		object.addProperty(Keys.ID, item.getId().toString());
		object.addProperty(Keys.TYPE, switch (item) {
			case Movie _ -> "movie";
			case Series _ -> "series";
		});
		if (item.getCategory() != null) {
			object.addProperty(Keys.CATEGORY, item.getCategory().name());
		}
	}

	/**
	 * Sets the following attributes on the given media item from
	 * the provided JSON object:
	 * <ul>
	 *     <li>{@link Keys#CATEGORY}</li>
	 * </ul>
	 *
	 * @param item the media item
	 * @param object the JSON object
	 * @param context the JSON deserialization context
	 */
	public static void setTopLevelMediaAttributesFromJson(TopLevelMedia item, JsonObject object, JsonDeserializationContext context) {
		item.setCategory(getAsCategoryOrNull(object, Keys.CATEGORY));
	}

	/**
	 * Sets the following attributes on the given JSON object from
	 * the provided media item:
	 * <ul>
	 *     <li>{@link Keys#WATCHED}</li>
	 *     <li>{@link Keys#DURATION}</li>
	 *     <li>{@link Keys#RATING}</li>
	 *     <li>{@link Keys#AGE_RATINGS}</li>
	 * </ul>
	 *
	 * @param object the JSON object
	 * @param item the media item
	 * @param context the JSON serialization context
	 */
	public static void setLeafMediaAttributesOnJson(JsonObject object, LeafMedia item, JsonSerializationContext context) {
		object.addProperty(Keys.WATCHED, item.getStatus() == Status.WATCHED);
		object.addProperty(Keys.DURATION, item.getDuration());
		object.addProperty(Keys.RATING, item.getRating());

		final JsonObject ratingsObject = new JsonObject();
		for (RatingSystem ratingSystem : RatingSystem.values()) {
			if (!item.hasAgeRating(ratingSystem)) {
				continue;
			}
			final AgeRating ageRating = item.getAgeRating(ratingSystem);
			if (!(ageRating instanceof Enum<?> ageRatingAsEnum)) {
				continue;
			}
			ratingsObject.addProperty(ratingSystem.name(), ageRatingAsEnum.name());
		}
		if (!ratingsObject.isEmpty()) {
			object.add(Keys.AGE_RATINGS, ratingsObject);
		}
	}

	/**
	 * Sets the following attributes on the given leaf media item from
	 * the provided JSON object:
	 * <ul>
	 *     <li>{@link Keys#WATCHED}</li>
	 *     <li>{@link Keys#DURATION}</li>
	 *     <li>{@link Keys#RATING}</li>
	 *     <li>{@link Keys#AGE_RATINGS}</li>
	 * </ul>
	 *
	 * @param item the leaf media item
	 * @param object the JSON object
	 * @param context the JSON deserialization context
	 */
	public static void setLeafMediaAttributesFromJson(LeafMedia item, JsonObject object, JsonDeserializationContext context) {
		item.setWatched(getAsBooleanOrFalse(object, Keys.WATCHED));
		item.setDuration(getAsIntOrElse(object, Keys.DURATION, 0));
		item.setRating(getAsIntOrElse(object, Keys.RATING, -1));

		final JsonObject ratingsObject = getAsJsonObjectOrNull(object, Keys.AGE_RATINGS);
		if (ratingsObject != null) {
			for (String ratingKey : ratingsObject.keySet()) {
				final RatingSystem ratingSystem = RatingSystem.fromName(ratingKey);
				if (ratingSystem == null) {
					continue;
				}
				final String ageRatingString = getAsStringOrNull(ratingsObject, ratingKey);
				final AgeRating ageRating = ratingSystem.getRating(ageRatingString);
				if (ageRating == null) {
					continue;
				}
				item.setAgeRating(ageRating);
			}
		}
	}

}
