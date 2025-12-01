package de.unistuttgart.einf.moviemanager.service;

import de.unistuttgart.einf.moviemanager.io.FileRepository;
import de.unistuttgart.einf.moviemanager.model.TopLevelMedia;

import java.util.HashSet;
import java.util.Set;

/**
 * The service over which the frontend interacts with the model.
 */
public class MediaService {

	private final Set<TopLevelMedia> mediaList = new HashSet<>();
	private final FileRepository<Set<TopLevelMedia>> repository;

	/**
	 * Constructs a new MediaService with the given repository.
	 *
	 * @param repository the repository
	 * @throws IllegalArgumentException if repository is null
	 */
	public MediaService(FileRepository<Set<TopLevelMedia>> repository) {
		if (repository == null) {
			throw new IllegalStateException("Repository must not be null");
		}
		this.repository = repository;
		reload();
	}

	/**
	 * Reloads the data from the repository
	 */
	public void reload() {
		mediaList.clear();
		final Set<TopLevelMedia> loadedMedia = repository.load();
		if (loadedMedia != null) {
			mediaList.addAll(loadedMedia);
		}
	}

	/**
	 * Saves the data to the repository
	 */
	public void save() {
		repository.save(mediaList);
	}

	/**
	 * Adds a new media item
	 *
	 * @param media the media item
	 */
	public void addMedia(TopLevelMedia media) {
		mediaList.add(media);
	}

	/**
	 * Returns an immutable set of all managed media.
	 *
	 * @return the set of media.
	 */
	public Set<TopLevelMedia> getAllMedia() {
		return Set.copyOf(mediaList);
	}

}
