package de.unistuttgart.einf.moviemanager.command.argument;

import de.unistuttgart.einf.moviemanager.command.CommandParseException;
import de.unistuttgart.einf.moviemanager.command.Token;
import de.unistuttgart.einf.moviemanager.model.TopLevelMedia;
import de.unistuttgart.einf.moviemanager.service.MediaService;

import java.util.List;

/**
 * A top level media argument.
 *
 * @see TopLevelMedia
 */
public class TopLevelMediaArgument extends Argument<TopLevelMedia> {

	private final MediaService mediaService;

	/**
	 * Constructs a new top level media argument from the given builder.
	 *
	 * @param builder the builder
	 */
	protected TopLevelMediaArgument(Builder builder) {
		super(builder);
		this.mediaService = builder.mediaService;
	}

	@Override
	public boolean matches(Token token) {
		return mediaService.getAllMedia().stream()
				.map(TopLevelMedia::getTitle)
				.anyMatch(title -> title.equals(token.value()));
	}

	@Override
	public TopLevelMedia parse(Token token) {
		return mediaService.getAllMedia().stream()
				.filter(media -> media.getTitle().equals(token.value()))
				.findFirst()
				.orElseThrow(() -> new CommandParseException("Invalid media title"));
	}

	@Override
	public List<String> getDefaultSuggestions() {
		return mediaService.getAllMedia().stream()
				.map(TopLevelMedia::getTitle)
				.map(title -> title.contains(" ") ? "\"" + title + "\"" : title)
				.toList();
	}

	/**
	 * Creates a new top level media argument builder with the given name.
	 * It will fetch available media from the given media service.
	 *
	 * @param name the name of the argument
	 * @param mediaService the media service
	 * @return the argument builder
	 */
	public static Builder create(String name, MediaService mediaService) {
		return new Builder(name, mediaService);
	}

	/**
	 * The builder for top level media arguments
	 */
	public static class Builder extends Argument.Builder<TopLevelMediaArgument> {

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
				throw new IllegalArgumentException("MediaService cannot be null");
			}
			this.mediaService = mediaService;
		}

		@Override
		public TopLevelMediaArgument build() {
			return new TopLevelMediaArgument(this);
		}

	}

}
