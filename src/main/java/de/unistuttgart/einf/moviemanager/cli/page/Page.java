package de.unistuttgart.einf.moviemanager.cli.page;

import de.unistuttgart.einf.moviemanager.cli.Cli;
import de.unistuttgart.einf.moviemanager.cli.api.Component;
import de.unistuttgart.einf.moviemanager.cli.api.Interactable;
import de.unistuttgart.einf.moviemanager.cli.api.Parent;
import de.unistuttgart.einf.moviemanager.model.Media;

/**
 * The base class for pages in the cli.
 */
public abstract class Page extends Parent {

	private final Cli cli;
	private final Component mainComponent;

	private Interactable focusCache;

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
	 * Returns the interactable that last had focus on this page, or that
	 * is supposed to receive focus when the user returns to or opens this page.
	 *
	 * @return the interactable
	 */
	public Interactable getFocusCache() {
		return focusCache;
	}

	/**
	 * Sets the interactable that last had focus on this page, or that
	 * is supposed to receive focus when the user returns to or opens this page.
	 *
	 * @param focusCache the interactable
	 */
	public void setFocusCache(Interactable focusCache) {
		this.focusCache = focusCache;
	}

	/**
	 * Returns the effective focus for this page. This is identical to
	 * the current focus of the scene, if that focus is a child of this page,
	 * the cache otherwise.
	 *
	 * @return the effective cache.
	 */
	public Interactable getEffectiveFocus() {
		final Interactable focus = getCli().getFocusManager().getCurrentFocus();
		if (isChild(focus)) {
			return focus;
		}
		return focusCache;
	}

	/**
	 * Returns the currently active media item, or null if no media item is active.
	 *
	 * @return the active media item, e.g. a highlighted movie or an opened episode
	 */
	public Media getActiveMedia() {
		return null;
	}

	public abstract void refresh();

}
