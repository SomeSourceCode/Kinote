package de.unistuttgart.einf.moviemanager.dbimport;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.unistuttgart.einf.moviemanager.model.Genre;
import de.unistuttgart.einf.moviemanager.model.Media;
import de.unistuttgart.einf.moviemanager.model.age.*;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Abstract class for importing media data from TMDb.
 */
public abstract class MediaImporter<T extends Media> {

	protected final TmdbClient client;
	protected Language language;

	protected final EnumSet<ImportableAttribute> included;
	protected final EnumSet<ImportableAttribute> overridden;
	protected final Map<ImportableAttribute, ImportStrategy<? super T>> strategies;

	public MediaImporter(TmdbClient client) {
		this.client = client;
		this.language = Language.ENGLISH;

		this.included = EnumSet.noneOf(ImportableAttribute.class);
		this.overridden = EnumSet.noneOf(ImportableAttribute.class);
		this.strategies = new EnumMap<>(ImportableAttribute.class);

		initializeStrategy();
	}

	/**
	 * Returns the language used for the import.
	 *
	 * @return the language
	 */
	public Language getLanguage() {
		return language;
	}

	/**
	 * Sets the language used for the import.
	 *
	 * @param language the language
	 */
	public void setLanguage(Language language) {
		this.language = language;
	}

	/**
	 * Includes the given attributes for import.
	 * These attributes will only be included, if the corresponding field is null.
	 *
	 * @param attributes the attributes to include
	 */
	public void include(ImportableAttribute... attributes) {
		included.addAll(List.of(attributes));
	}

	/**
	 * Excludes the given attributes from import.
	 *
	 * @param attributes the attributes to exclude
	 */
	public void exclude(ImportableAttribute... attributes) {
		List.of(attributes).forEach(attribute -> {
			included.remove(attribute);
			overridden.remove(attribute);
		});
	}

	/**
	 * Returns the included attributes for import.
	 * The collection is unmodifiable.
	 *
	 * @return the included attributes
	 */
	public Set<ImportableAttribute> getIncluded() {
		return Collections.unmodifiableSet(included);
	}

	/**
	 * Overrides the given attributes during import.
	 *
	 * @param attributes the attributes to override
	 */
	public void override(ImportableAttribute... attributes) {
		overridden.addAll(List.of(attributes));
		included.addAll(List.of(attributes));
	}

	/**
	 * Returns the attributes that will be overridden.
	 * The collection is unmodifiable.
	 *
	 * @return the overridden attributes
	 */
	public Set<ImportableAttribute> getOverridden() {
		return Collections.unmodifiableSet(overridden);
	}

	protected void applyAttributes(T media, JsonObject json, ImportContext context) {
		for (ImportableAttribute attribute : included) {
			final ImportStrategy<? super T> strategy = strategies.get(attribute);
			if (strategy == null) {
				continue;
			}
			strategy.apply(media, json, context, overridden.contains(attribute));
		}
	}

	protected String getStringOrNull(JsonObject json, String key) {
		if (!json.has(key) || json.get(key).isJsonNull()) {
			return null;
		}
		return json.get(key).getAsString();
	}

	protected Integer getIntOrNull(JsonObject json, String key) {
		if (!json.has(key) || json.get(key).isJsonNull()) {
			return null;
		}
		return json.get(key).getAsInt();
	}

	protected Set<AgeRating> parseSeriesAgeRatings(JsonObject json) {
		final Set<AgeRating> ageRatings = new HashSet<>();

		if (!json.has("results")) {
			return ageRatings;
		}

		final JsonArray results = json.getAsJsonArray("results");
		for (JsonElement countryElement : results) {
			final JsonObject countryObject = countryElement.getAsJsonObject();
			final String iso31661 = countryObject.get("iso_3166_1").getAsString();
			final String ratingString = countryObject.get("rating").getAsString();

			if (!ratingString.isBlank()) {
				final AgeRating ageRating = mapAgeRating(iso31661, ratingString);
				if (ageRating != null) {
					ageRatings.add(ageRating);
				}
			}
		}

		return ageRatings;
	}

	/**
	 * Fetches age ratings for the given series ID from TMDb.
	 *
	 * @param seriesId the TMDb series ID
	 * @return a set of age ratings
	 */
	protected Set<AgeRating> fetchSeriesAgeRatings(int seriesId) throws MediaImportException {
		final Set<AgeRating> ageRatings = new HashSet<>();
		final JsonObject jsonObject = client.get("tv/" + seriesId + "/content_ratings", language);
		return parseSeriesAgeRatings(jsonObject);
	}

	/**
	 * Maps the given country and age rating string to an AgeRating object.
	 *
	 * @param country the country code in ISO-3166-1 format
	 * @param ageRating the age rating string
	 * @return the corresponding rating or, or null if it cannot be mapped
	 */
	protected AgeRating mapAgeRating(String country, String ageRating) {
		return switch (country) {
			case "DE" -> switch (ageRating) {
				case "0" -> FskRating.FSK_0;
				case "6" -> FskRating.FSK_6;
				case "12" -> FskRating.FSK_12;
				case "16" -> FskRating.FSK_16;
				case "18" -> FskRating.FSK_18;
				default -> null;
			};
			case "US" -> switch (ageRating) {
				case "G", "TV-Y", "TV-G" -> MpaRating.G;
				case "PG", "TV-Y7", "TV-Y7-FV", "TV-PG" -> MpaRating.PG;
				case "PG-13", "TV-14" -> MpaRating.PG_13;
				case "R", "TV-MA" -> MpaRating.R;
				case "NC-17" -> MpaRating.NC_17;
				default -> null;
			};
			case "GB" -> switch (ageRating) {
				case "U" -> BbfcRating.BBFC_U;
				case "PG" -> BbfcRating.BBFC_PG;
				case "12", "12A" -> BbfcRating.BBFC_12;
				case "15" -> BbfcRating.BBFC_15;
				case "18" -> BbfcRating.BBFC_18;
				default -> null;
			};
			default -> null;
		};
	}

	/**
	 * Returns all ratings from the given set of age ratings in the specified system.
	 *
	 * @param ageRatings the set of age ratings
	 * @param system the rating system
	 * @return the list of age ratings
	 */
	protected List<AgeRating> getAgeRatingsBySystem(Set<AgeRating> ageRatings, RatingSystem system) {
		return ageRatings.stream().filter(rating -> rating.getSystem() == system).toList();
	}

	/**
	 * Returns all genres from the given set of TMDb gerne ids.
	 * The mappings were found <a href="https://www.themoviedb.org/talk/5daf6eb0ae36680011d7e6ee">here</a>.
	 *
	 * @param ids the set of TMDb genre ids
	 * @return the set of mapped genres
	 */
	protected Set<Genre> mapGenres(Set<Integer> ids) {
		EnumSet<Genre> genres = EnumSet.noneOf(Genre.class);
		for (int id : ids) {
			switch (id) {
				case 28 -> genres.add(Genre.ACTION);
				case 12 -> genres.add(Genre.ADVENTURE);
				case 16 -> genres.add(Genre.ANIMATION);
				case 35 -> genres.add(Genre.COMEDY);
				case 80 -> genres.add(Genre.CRIME);
				case 99 -> genres.add(Genre.DOCUMENTARY);
				case 18 -> genres.add(Genre.DRAMA);
				case 10751 -> genres.add(Genre.FAMILY);
				case 14 -> genres.add(Genre.FANTASY);
				case 36 -> genres.add(Genre.HISTORY);
				case 27 -> genres.add(Genre.HORROR);
				case 10762 -> genres.add(Genre.KIDS);
				case 10402 -> genres.add(Genre.MUSIC);
				case 9648 -> genres.add(Genre.MYSTERY);
				case 10763 -> genres.add(Genre.NEWS);
				case 10764 -> genres.add(Genre.REALITY);
				case 10749 -> genres.add(Genre.ROMANCE);
				case 878 -> genres.add(Genre.SCIENCE_FICTION);
				case 10766 -> genres.add(Genre.SOAP);
				case 10767 -> genres.add(Genre.TALK);
				case 10770 -> genres.add(Genre.TV_MOVIE);
				case 53 -> genres.add(Genre.THRILLER);
				case 10752 -> genres.add(Genre.WAR);
				case 37 -> genres.add(Genre.WESTERN);

				// Action & Adventure
				case 10759 -> {
					genres.add(Genre.ACTION);
					genres.add(Genre.ADVENTURE);
				}
				// Sci-Fi & Fantasy
				case 10765 -> {
					genres.add(Genre.SCIENCE_FICTION);
					genres.add(Genre.FANTASY);
				}
				// War & Politics
				case 10768 -> {
					genres.add(Genre.WAR);
					genres.add(Genre.POLITICS);
				}

				default -> {}
			}
		}
		return genres;
	}

	protected abstract void initializeStrategy();

	protected ImportStrategy<T> createStringStrategy(String key, Function<? super T, String> getter, BiConsumer<? super T, String> setter) {
		return createStringStrategy(key, getter, setter, null);
	}

	protected ImportStrategy<T> createStringStrategy(String key, Function<? super T, String> getter, BiConsumer<? super T, String> setter, Function<String, String> mapper) {
		return (media, json, context, override) -> {
			String tmdbValue = getStringOrNull(json, key);
			if (tmdbValue == null) {
				return;
			}

			if (mapper != null) {
				tmdbValue = mapper.apply(tmdbValue);
			}

			final String currentValue = getter.apply(media);
			if (tmdbValue.isBlank() || (!override && currentValue != null && !currentValue.isBlank())) {
				return;
			}
			setter.accept(media, tmdbValue);
		};
	}

	protected ImportStrategy<T> createRuntimeStrategy(String key, Predicate<? super T> hasRuntime, BiConsumer<? super T, Integer> setter) {
		return (media, json, context, override) -> {
			final Integer tmdbRuntime = getIntOrNull(json, key);
			if (tmdbRuntime == null || (!override && hasRuntime.test(media))) {
				return;
			}
			setter.accept(media, tmdbRuntime);
		};
	}

	protected ImportStrategy<T> createGenreStrategy(String key, BiConsumer<? super T, Genre> addGenre) {
		return (media, json, context, override) -> {
			if (!json.has(key)) {
				return;
			}
			final Set<Integer> genreIds = new HashSet<>();
			for (JsonElement element : json.getAsJsonArray(key)) {
				genreIds.add(element.getAsJsonObject().get("id").getAsInt());
			}
			final Set<Genre> genres = mapGenres(genreIds);
			for (Genre genre : genres) {
				addGenre.accept(media, genre);
			}
		};
	}

}
