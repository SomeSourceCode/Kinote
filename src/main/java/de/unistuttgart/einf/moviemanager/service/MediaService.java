package de.unistuttgart.einf.moviemanager.service;

import de.unistuttgart.einf.moviemanager.io.FileRepository;
import de.unistuttgart.einf.moviemanager.model.TopLevelMedia;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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
			throw new IllegalArgumentException("Repository must not be null");
		}
		this.repository = repository;
		reload();
	}

	/**
	 * Reloads the data from the repository.
	 */
	public void reload() {
		mediaList.clear();
		final Set<TopLevelMedia> loadedMedia = repository.load();
		if (loadedMedia != null) {
			mediaList.addAll(loadedMedia);
		}
	}

	/**
	 * Saves the data to the repository.
	 */
	public void save() {
		repository.save(mediaList);
	}

	/**
	 * Adds a new media item.
	 *
	 * @param media the media item
	 */
	public void addMedia(TopLevelMedia media) {
		mediaList.add(media);
	}

	/**
	 * Removes a media item.
	 *
	 * @param media the media item
	 */
	public void removeMedia(TopLevelMedia media) {
		mediaList.remove(media);
	}

	/**
	 * Returns an immutable set of all managed media.
	 *
	 * @return the set of media.
	 */
	public Set<TopLevelMedia> getAllMedia() {
		return Set.copyOf(mediaList);
	}

	/**
	 * Returns the media item with the given id, or null if no such item exists.
	 *
	 * @param id the id of the media item
	 * @return the media item
	 */
	public TopLevelMedia getMediaById(UUID id) {
		if (id == null) {
			return null;
		}

		for (TopLevelMedia media : mediaList) {
			if (id.equals(media.getId())) {
				return media;
			}
		}

		return null;
	}

}
