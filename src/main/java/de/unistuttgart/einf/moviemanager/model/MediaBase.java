package de.unistuttgart.einf.moviemanager.model;

/**
 * A base implementation of the Media interface.
 */
public abstract class MediaBase implements Media {

	private String title;
	private String description;

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

}
