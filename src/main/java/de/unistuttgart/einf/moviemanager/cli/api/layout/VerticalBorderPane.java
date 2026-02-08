package de.unistuttgart.einf.moviemanager.cli.api.layout;

import com.googlecode.lanterna.TextColor;
import de.unistuttgart.einf.moviemanager.cli.api.Component;
import de.unistuttgart.einf.moviemanager.cli.api.Painter;
import de.unistuttgart.einf.moviemanager.cli.api.Parent;

import java.util.Objects;

/**
 * Arranges the component vertically, placing the top and bottom
 * components first, then resizing the center to fit the remaining space.
 */
public class VerticalBorderPane extends Parent {

	private Component topComponent;
	private Component centerComponent;
	private Component bottomComponent;

	private boolean showSeparators = false;

	/**
	 * Returns the top component.
	 *
	 * @return the top component
	 */
	public Component getTop() {
		return topComponent;
	}

	/**
	 * Sets the top component.
	 *
	 * @param topComponent the top component
	 */
	public void setTop(Component topComponent) {
		if (Objects.equals(this.topComponent, topComponent)) {
			return;
		}
		removeChild(this.topComponent);
		if (topComponent == null) {
			this.topComponent = null;
			return;
		}
		this.topComponent = topComponent;
		addChild(topComponent);
	}

	/**
	 * Returns the center component.
	 *
	 * @return the center component
	 */
	public Component getCenter() {
		return centerComponent;
	}

	/**
	 * Sets the center component.
	 *
	 * @param centerComponent the center component
	 */
	public void setCenter(Component centerComponent) {
		if (Objects.equals(this.centerComponent, centerComponent)) {
			return;
		}
		removeChild(this.centerComponent);
		if (centerComponent == null) {
			this.centerComponent = null;
			return;
		}
		this.centerComponent = centerComponent;
		addChild(centerComponent);
	}

	/**
	 * Returns the bottom component.
	 *
	 * @return the bottom component
	 */
	public Component getBottom() {
		return bottomComponent;
	}

	/**
	 * Sets the bottom component.
	 *
	 * @param bottomComponent the bottom component
	 */
	public void setBottom(Component bottomComponent) {
		if (Objects.equals(this.bottomComponent, bottomComponent)) {
			return;
		}
		removeChild(this.bottomComponent);
		if (bottomComponent == null) {
			this.bottomComponent = null;
			return;
		}
		this.bottomComponent = bottomComponent;
		addChild(bottomComponent);
	}

	/**
	 * Returns whether this pane renders separators between the sections.
	 *
	 * @return whether this pane renders separators between the sections
	 */
	public boolean isShowSeparators() {
		return showSeparators;
	}

	/**
	 * Sets whether this pane should render separators between the sections.
	 *
	 * @param showSeparators whether this pane should render separators between
	 * the sections
	 */
	public void setShowSeparators(boolean showSeparators) {
		this.showSeparators = showSeparators;
	}

	@Override
	protected void drawContent(Painter painter) {
		super.drawContent(painter);
		if (!isShowSeparators()) {
			return;
		}

		final boolean renderTopComponent = topComponent != null && !topComponent.isHidden();
		final boolean renderBottomComponent = bottomComponent != null && !bottomComponent.isHidden();
		final boolean renderCenterComponent = centerComponent != null && !centerComponent.isHidden();

		final boolean renderTopSeparator = renderTopComponent && (renderCenterComponent || renderBottomComponent);
		final boolean renderBottomSeparator = renderBottomComponent && renderTopComponent;

		if (renderTopSeparator) {
			final int y = topComponent.getY() + topComponent.getHeight();
			painter.drawSmartHorizontalLine(toGlobalX(getPadding().getLeft()), toGlobalY(y), getInnerWidth(), TextColor.ANSI.DEFAULT, null);
		}
		if (renderBottomSeparator) {
			final int y = bottomComponent.getY() - 1;
			painter.drawSmartHorizontalLine(toGlobalX(getPadding().getLeft()), toGlobalY(y), getInnerWidth(), TextColor.ANSI.DEFAULT, null);
		}
	}

	@Override
	public void layoutChildren() {
		final int paddingTop = getPadding().getTop();
		final int paddingBottom = getPadding().getBottom();
		final int paddingLeft = getPadding().getLeft();
		final int paddingRight = getPadding().getRight();

		final int width = getInnerWidth() - paddingLeft - paddingRight;
		final int height = getInnerHeight() - paddingTop - paddingBottom;

		final boolean renderTopComponent = topComponent != null && !topComponent.isHidden();
		final boolean renderBottomComponent = bottomComponent != null && !bottomComponent.isHidden();
		final boolean renderCenterComponent = centerComponent != null && !centerComponent.isHidden();

		final boolean renderTopSeparator = isShowSeparators() && renderTopComponent && (renderCenterComponent || renderBottomComponent);
		final boolean renderBottomSeparator = isShowSeparators() && renderBottomComponent && renderTopComponent;

		int topComponentHeight = 0;
		if (renderTopComponent) {
			topComponentHeight = topComponent.getHeight();
			topComponent.setX(paddingLeft);
			topComponent.setY(paddingTop);
			topComponent.setWidth(width);
			topComponentHeight += renderTopSeparator ? 1 : 0;
		}

		int bottomComponentHeight = 0;
		if (renderBottomComponent) {
			bottomComponentHeight = bottomComponent.getHeight();
			bottomComponent.setX(paddingLeft);
			bottomComponent.setY(Math.max(paddingTop + topComponentHeight + (renderBottomSeparator ? 1 : 0), paddingTop + height - bottomComponentHeight));
			bottomComponent.setWidth(width);
			bottomComponentHeight += renderBottomSeparator ? 1 : 0;
		}

		if (renderCenterComponent) {
			centerComponent.setX(paddingLeft);
			centerComponent.setY(paddingTop + topComponentHeight);
			centerComponent.setWidth(width);
			centerComponent.setHeight(Math.max(0, height - topComponentHeight - bottomComponentHeight));
		}
	}

}
