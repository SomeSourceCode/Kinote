package de.unistuttgart.einf.moviemanager.command.argument;

import de.unistuttgart.einf.moviemanager.command.CommandParseException;
import de.unistuttgart.einf.moviemanager.command.Token;
import de.unistuttgart.einf.moviemanager.model.Movie;
import de.unistuttgart.einf.moviemanager.model.TopLevelMedia;
import de.unistuttgart.einf.moviemanager.service.MediaService;

import java.util.List;

/**
 * A movie argument.
 *
 * @see Movie
 */
public class MovieArgument extends Argument<Movie> {

	private final MediaService mediaService;

	/**
	 * Constructs a new movie argument from the given builder.
	 *
	 * @param builder the builder
	 */
	protected MovieArgument(Builder builder) {
		super(builder);
		this.mediaService = builder.mediaService;
	}

	@Override
	public boolean matches(Token token) {
		return mediaService.getAllMedia().stream()
				.filter(media -> media instanceof Movie)
				.map(TopLevelMedia::getTitle)
				.anyMatch(title -> title.equals(token.value()));
	}

	@Override
	public Movie parse(Token token) {
		return mediaService.getAllMedia().stream()
				.filter(media -> media instanceof Movie)
				.filter(media -> media.getTitle().equals(token.value()))
				.map(Movie.class::cast)
				.findFirst()
				.orElseThrow(() -> new CommandParseException("Invalid movie title"));
	}

	@Override
	public List<String> getDefaultSuggestions() {
		return mediaService.getAllMedia().stream()
				.filter(media -> media instanceof Movie)
				.map(TopLevelMedia::getTitle)
				.map(title -> title.contains(" ") ? "\"" + title + "\"" : title)
				.toList();
	}

	/**
	 * Creates a new movie argument builder with the given name.
	 *
	 * @param name the name of the argument
	 * @param mediaService the media service
	 * @return the argument builder
	 */
	public static Builder create(String name, MediaService mediaService) {
		return new Builder(name, mediaService);
	}

	/**
	 * The builder for movie arguments
	 */
	public static class Builder extends Argument.Builder<MovieArgument> {

		private final MediaService mediaService;

		/**
		 * Constructs a new top level media argument builder with the given name
		 * and media service.
		 *
		 * @param name the name of the argument
		 * @param mediaService the media service
		 */
		protected Builder(String name, MediaService mediaService) {
			super(name);
			if (mediaService == null) {
				throw new IllegalArgumentException("mediaService must be non-null");
			}
			this.mediaService = mediaService;
		}

		@Override
		public MovieArgument build() {
			return new MovieArgument(this);
		}

	}

}
