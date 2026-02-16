package de.unistuttgart.einf.moviemanager.cli.page;

import de.unistuttgart.einf.moviemanager.cli.Cli;
import de.unistuttgart.einf.moviemanager.cli.api.Component;
import de.unistuttgart.einf.moviemanager.cli.api.Parent;
import de.unistuttgart.einf.moviemanager.model.Media;

/**
 * The base class for pages in the cli.
 */
public abstract class Page extends Parent {

	private final Cli cli;
	private final Component mainComponent;

	/**
	 * Constructs a new page with the given cli and
	 * main component.
	 *
	 * @param cli the cli
	 * @param mainComponent the main component
	 * @throws IllegalArgumentException if mainComponent is null
	 */
	public Page(Cli cli, Component mainComponent) {
		if (mainComponent == null) {
			throw new IllegalArgumentException("mainComponent must be non-null");
		}
		this.cli = cli;
		this.mainComponent = mainComponent;
		addChild(mainComponent);
	}

	/**
	 * Returns the cli this page belongs to.
	 *
	 * @return the cli
	 */
	public Cli getCli() {
		return cli;
	}

	/**
	 * Returns the main component.
	 *
	 * @return the main component
	 */
	protected Component getMainComponent() {
		return mainComponent;
	}

	@Override
	public void layoutChildren() {
		final int topPadding = getPadding().getTop();
		final int bottomPadding = getPadding().getBottom();
		final int leftPadding = getPadding().getLeft();
		final int rightPadding = getPadding().getRight();

		final int width = getInnerWidth() - leftPadding - rightPadding;
		final int height = getInnerHeight() - topPadding - bottomPadding;

		mainComponent.setX(leftPadding);
		mainComponent.setY(topPadding);
		mainComponent.setWidth(width);
		mainComponent.setHeight(height);
	}

	/**
	 * Returns the currently active media item, or null if no media item is active.
	 *
	 * @return the active media item, e.g. a highlighted movie or an opened episode
	 */
	public Media getActiveMedia() {
		return null;
	}

}
