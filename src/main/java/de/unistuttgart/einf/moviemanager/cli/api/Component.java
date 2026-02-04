package de.unistuttgart.einf.moviemanager.cli.api;

/**
 * The base interface for all components in the cli
 * scene graph.
 */
public interface Component {

	/**
	 * Returns the scene this component is part of.
	 *
	 * @return the scene
	 */
	Scene getScene();

	/**
	 * Sets the scene the component is part of. This method is intended for
	 * internal use only. Attempting to change the scene manually will result
	 * in an IllegalStateException.
	 *
	 * @param scene the scene
	 * @throws IllegalStateException if the scene is modified externally
	 * @see Scene#setRoot(Parent) 
	 */
	void setScene(Scene scene);

	/**
	 * Returns the parent.
	 */
	Parent getParent();

	/**
	 * Sets the parent. This method is intended for internal use only.
	 * Attempting to change the parent manually will result in an
	 * IllegalStateException.
	 *
	 * @throws IllegalStateException if the parent is modified externally
	 * @see Parent#addChild(Component)
	 */
	void setParent(Parent parent);

	/**
	 * Returns the x coordinate relative to the parent's bounds.
	 * The origin is located in the top left corner. If the parent has
	 * borders, the origin lies within those.
	 *
	 * @return the x coordinate
	 */
	int getX();

	/**
	 * Sets the x coordinate relative to the parent's bounds.
	 * The origin is located in the top left corner. If the parent has
	 * borders, the origin lies within those.
	 *
	 * @param x the x coordinate
	 */
	void setX(int x);

	/**
	 * Returns the y coordinate relative to the parent's bounds.
	 * The origin is located in the top left corner. If the parent has
	 * borders, the origin lies within those.
	 *
	 * @return the y coordinate
	 */
	int getY();

	/**
	 * Sets the y coordinate relative to the parent's bounds.
	 * The origin is located in the top left corner. If the parent has
	 * borders, the origin lies within those.
	 *
	 * @param y the y coordinate
	 */
	void setY(int y);

	/**
	 * Returns the given x coordinate relative to the scene's coordinate
	 * system.
	 *
	 * @param x the x coordinate relative to this nodes inner bounds
	 * @return the global x coordinate
	 */
	default int toGlobalX(int x) {
		final Parent parent = getParent();
		final int localeX = getX() + x + (isShowBorders() ? 1 : 0);
		return parent == null ? localeX : parent.toGlobalX(localeX);
	}

	/**
	 * Returns the given y coordinate relative to the scene's coordinate
	 * system.
	 *
	 * @param y the y coordinate relative to this nodes inner bounds
	 * @return the global y coordinate
	 */
	default int toGlobalY(int y) {
		final Parent parent = getParent();
		final int localeY = getY() + y + (isShowBorders() ? 1 : 0);
		return parent == null ? localeY : parent.toGlobalY(localeY);
	}

	/**
	 * Returns the width. If borders are shown, their width is included.
	 *
	 * @return the width
	 * @see #getInnerWidth()
	 */
	int getWidth();

	/**
	 * Returns the width. If borders are shown, their width is included.
	 *
	 * @param width the width
	 */
	void setWidth(int width);

	/**
	 * Returns the height. If borders are shown, their height is included.
	 *
	 * @return the height
	 * @see #getInnerHeight()
	 */
	int getHeight();

	/**
	 * Returns the height. If borders are shown, their height is included.
	 *
	 * @param height the height
	 */
	void setHeight(int height);

	/**
	 * Returns the inner width accounting for the border's thickness.
	 *
	 * @return the inner width
	 */
	default int getInnerWidth() {
		return getWidth() - (isShowBorders() ? 2 : 0);
	}

	/**
	 * Returns the inner height accounting for the border's thickness.
	 *
	 * @return the inner height
	 */
	default int getInnerHeight() {
		return getHeight() - (isShowBorders() ? 2 : 0);
	}

	/**
	 * Returns whether this component renders a border.
	 *
	 * @return whether this component renders a border
	 */
	boolean isShowBorders();

	/**
	 * Sets whether this component should render a border.
	 *
	 * @param showBorders whether this component should render a border
	 */
	void setShowBorders(boolean showBorders);

	/**
	 * Returns whether this component is hidden. Hidden components
	 * will not be drawn on the screen.
	 *
	 * @return whether this component is hidden
	 */
	boolean isHidden();

	/**
	 * Returns whether this component is hidden. Hidden components
	 * will not be drawn on the screen.
	 *
	 * @param hidden whether this component is hidden
	 */
	void setHidden(boolean hidden);

	/**
	 * Returns the padding.
	 *
	 * @return the padding
	 */
	Insets getPadding();

	/**
	 * Sets the padding.
	 *
	 * @param padding the padding
	 */
	void setPadding(Insets padding);

	/**
	 * Draws the component to the screen via the given painter.
	 *
	 * @param painter the painter
	 */
	void draw(Painter painter);

	/**
	 * Lays out the component. If this is a {@link Parent}, this includes
	 * layout out the children.
	 */
	void layout();

}
