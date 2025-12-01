package de.unistuttgart.einf.moviemanager.model;

/**
 * A top-level media item.
 *
 * @see Movie
 * @see Series
 */
public sealed interface TopLevelMedia extends Media permits Movie, Series {

}
