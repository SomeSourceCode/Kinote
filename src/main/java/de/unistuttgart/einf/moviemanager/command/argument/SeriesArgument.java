package de.unistuttgart.einf.moviemanager.command.argument;

import de.unistuttgart.einf.moviemanager.command.CommandParseException;
import de.unistuttgart.einf.moviemanager.command.Token;
import de.unistuttgart.einf.moviemanager.model.Series;
import de.unistuttgart.einf.moviemanager.model.TopLevelMedia;
import de.unistuttgart.einf.moviemanager.service.MediaService;

import java.util.List;

/**
 * A series argument.
 *
 * @see Series
 */
public class SeriesArgument extends Argument<Series> {

	private final MediaService mediaService;

	/**
	 * Constructs a new series argument from the given builder.
	 *
	 * @param builder the builder
	 */
	protected SeriesArgument(Builder builder) {
		super(builder);
		this.mediaService = builder.mediaService;
	}

	@Override
	public boolean matches(Token token) {
		return mediaService.getAllMedia().stream()
				.filter(media -> media instanceof Series)
				.map(TopLevelMedia::getTitle)
				.anyMatch(title -> title.equals(token.value()));
	}

	@Override
	public Series parse(Token token) {
		return mediaService.getAllMedia().stream()
				.filter(media -> media instanceof Series)
				.filter(media -> media.getTitle().equals(token.value()))
				.map(Series.class::cast)
				.findFirst()
				.orElseThrow(() -> new CommandParseException("Invalid series title"));
	}

	@Override
	public List<String> getDefaultSuggestions() {
		return mediaService.getAllMedia().stream()
				.filter(media -> media instanceof Series)
				.map(TopLevelMedia::getTitle)
				.map(title -> title.contains(" ") ? "\"" + title + "\"" : title)
				.toList();
	}

	/**
	 * Creates a new series argument builder with the given name.
	 *
	 * @param name the name of the argument
	 * @param mediaService the media service
	 * @return the argument builder
	 */
	public static Builder create(String name, MediaService mediaService) {
		return new Builder(name, mediaService);
	}

	/**
	 * The builder for series arguments.
	 */
	public static class Builder extends Argument.Builder<SeriesArgument> {

		private final MediaService mediaService;

		/**
		 * Constructs a new series argument builder with the given name
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
		public SeriesArgument build() {
			return new SeriesArgument(this);
		}

	}

}
