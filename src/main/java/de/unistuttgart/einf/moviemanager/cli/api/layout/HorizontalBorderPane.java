package de.unistuttgart.einf.moviemanager.cli.api.layout;

import com.googlecode.lanterna.TextColor;
import de.unistuttgart.einf.moviemanager.cli.api.Component;
import de.unistuttgart.einf.moviemanager.cli.api.Painter;
import de.unistuttgart.einf.moviemanager.cli.api.Parent;

import java.util.Objects;

/**
 * Arranges the component horizontally, placing the left and right
 * components first, then resizing the center to fit the remaining space.
 */
public class HorizontalBorderPane extends Parent {

	private Component leftComponent;
	private Component centerComponent;
	private Component rightComponent;

	private boolean showSeparators = false;

	/**
	 * Returns the left component.
	 *
	 * @return the left component
	 */
	public Component getLeft() {
		return leftComponent;
	}

	/**
	 * Sets the left component.
	 *
	 * @param leftComponent the left component
	 */
	public void setLeft(Component leftComponent) {
		if (Objects.equals(this.leftComponent, leftComponent)) {
			return;
		}
		removeChild(this.leftComponent);
		if (leftComponent == null) {
			this.leftComponent = null;
			return;
		}
		this.leftComponent = leftComponent;
		addChild(leftComponent);
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
	 * Returns the right component.
	 *
	 * @return the right component
	 */
	public Component getRight() {
		return rightComponent;
	}

	/**
	 * Sets the right component.
	 *
	 * @param rightComponent the right component
	 */
	public void setRight(Component rightComponent) {
		if (Objects.equals(this.rightComponent, rightComponent)) {
			return;
		}
		removeChild(this.rightComponent);
		if (rightComponent == null) {
			this.rightComponent = null;
			return;
		}
		this.rightComponent = rightComponent;
		addChild(rightComponent);
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

		final boolean renderLeft = leftComponent != null && !leftComponent.isHidden();
		final boolean renderRight = rightComponent != null && !rightComponent.isHidden();
		final boolean renderCenter = centerComponent != null && !centerComponent.isHidden();

		final boolean renderLeftSeparator = renderLeft && (renderCenter || renderRight);
		final boolean renderRightSeparator = renderRight && (renderCenter || renderLeft);

		if (renderLeftSeparator) {
			final int x = leftComponent.getX() + leftComponent.getWidth();
			painter.drawSmartVerticalLine(toGlobalX(x), toGlobalY(getPadding().getTop()), getInnerHeight(), TextColor.ANSI.DEFAULT, null);
		}
		if (renderRightSeparator) {
			final int x = rightComponent.getX() - 1;
			painter.drawSmartVerticalLine(toGlobalX(x), toGlobalY(getPadding().getTop()), getInnerHeight(), TextColor.ANSI.DEFAULT, null);
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

		final boolean renderLeft = leftComponent != null && !leftComponent.isHidden();
		final boolean renderRight = rightComponent != null && !rightComponent.isHidden();
		final boolean renderCenter = centerComponent != null && !centerComponent.isHidden();

		final boolean renderLeftSeparator = isShowSeparators() && renderLeft && (renderCenter || renderRight);
		final boolean renderRightSeparator = isShowSeparators() && renderRight && (renderCenter || renderLeft);

		int leftComponentWidth = 0;
		if (renderLeft) {
			leftComponentWidth = leftComponent.getWidth();
			leftComponent.setX(paddingLeft);
			leftComponent.setY(paddingTop);
			leftComponent.setHeight(height);
			leftComponentWidth += renderLeftSeparator ? 1 : 0;
		}

		int rightComponentWidth = 0;
		if (renderRight) {
			rightComponentWidth = rightComponent.getWidth();
			rightComponent.setX(Math.max(paddingLeft + leftComponentWidth + (renderRightSeparator ? 1 : 0), paddingLeft + width - rightComponentWidth));
			rightComponent.setY(paddingTop);
			rightComponent.setHeight(height);
			rightComponentWidth += renderRightSeparator ? 1 : 0;
		}

		if (renderCenter) {
			centerComponent.setX(paddingLeft + leftComponentWidth);
			centerComponent.setY(paddingTop);
			centerComponent.setWidth(Math.max(0, width - leftComponentWidth - rightComponentWidth));
			centerComponent.setHeight(height);
		}
	}

}
