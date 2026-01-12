package de.unistuttgart.einf.moviemanager.model;

/**
 * A base implementation of the Media interface.
 */
public abstract class MediaBase implements Media {

	private String title;
	private String description;
	private int duration; // duration in minutes
	private Category category;

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

	@Override
	public void setDuration(int duration) {
		this.duration = duration;
	}

	@Override
	public int getDuration() {
		return duration;
	}

	@Override
    public void setCategory(Category category) {
        this.category = category;
    }

    @Override
    public Category getCategory() {
        return category;
    }
}
