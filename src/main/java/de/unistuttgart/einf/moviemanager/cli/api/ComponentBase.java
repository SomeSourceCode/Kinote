package de.unistuttgart.einf.moviemanager.cli.api;

import com.googlecode.lanterna.TextColor;
import de.unistuttgart.einf.moviemanager.cli.api.popover.Popover;

import java.util.Objects;

/**
 * The base implementation of the Component interface.
 */
public abstract class ComponentBase implements Component {

	private Parent parent;

	private int x;
	private int y;

	private int width;
	private int height;

	private boolean showBorders = false;
	private boolean hidden = false;

	private Insets padding;

	private Scene scene;

	@Override
	public void setScene(Scene scene) {
		if (Objects.equals(this.scene, scene)) {
			return;
		}

		if (this.parent != null) {
			if (!Objects.equals(scene, parent.getScene())) {
				throw new IllegalStateException("Cannot change scene of a child component, it's inherited from the parent");
			}
		} else {
			if (scene != null) {
				if (this instanceof Popover asPopover) {
					if (!scene.getPopovers().contains(asPopover)) {
						throw new IllegalStateException("Cannot set scene directly. Use scene.showPopover(popover) instead");
					}
				} else if (scene.getRoot() != this) {
					throw new IllegalStateException("Cannot set scene directly. Use scene.setRoot(component) instead");
				}
			} else {
				if (this.scene.getRoot() == this) {
					throw new IllegalStateException("Cannot remove scene directly. Use scene.setRoot(component) instead");
				}
			}
		}

		this.scene = scene;
		if (this instanceof Parent asParent) {
			for (Component child : asParent.getChildren()) {
				child.setScene(scene);
			}
		}
	}

	@Override
	public Scene getScene() {
		return scene;
	}

	@Override
	public Parent getParent() {
		return parent;
	}

	@Override
	public void setParent(Parent parent) {
		if (parent == null) {
			if (this.parent != null && this.parent.getChildren().contains(this)) {
				throw new IllegalStateException("Cannot remove parent directly. Use parent.removeChild(component) instead");
			}
			this.parent = null;
			setScene(null);
			return;
		}
		if (this.parent != null && this.parent != parent) {
			throw new IllegalStateException("Component is already child of another parent. Remove it first");
		}
		if (!parent.getChildren().contains(this)) {
			throw new IllegalStateException("Cannot set parent directly. Use parent.removeChild(component) instead");
		}

		this.parent = parent;
		setScene(parent.getScene());
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public void setX(int x) {
		this.x = x;
	}

	@Override
	public int getY() {
		return y;
	}

	@Override
	public void setY(int y) {
		this.y = y;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public void setWidth(int width) {
		this.width = width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public void setHeight(int height) {
		this.height = height;
	}

	@Override
	public int getInnerWidth() {
		return width - (showBorders ? 2 : 0);
	}

	@Override
	public int getInnerHeight() {
		return height - (showBorders ? 2 : 0);
	}

	@Override
	public boolean isShowBorders() {
		return showBorders;
	}

	@Override
	public void setShowBorders(boolean showBorders) {
		this.showBorders = showBorders;
	}

	@Override
	public boolean isHidden() {
		return hidden;
	}

	@Override
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	@Override
	public Insets getPadding() {
		return padding == null ? Insets.NONE : padding;
	}

	@Override
	public void setPadding(Insets padding) {
		this.padding = padding;
	}

	@Override
	public void draw(Painter painter) {
		if (hidden) {
			return;
		}
		if (showBorders) {
			final int drawX = getX();
			final int drawY = getY();

			if (parent != null) {
				painter.drawSmartHorizontalLine(parent.toGlobalX(drawX), parent.toGlobalY(drawY), getWidth(), TextColor.ANSI.DEFAULT, null);
				painter.drawSmartHorizontalLine(parent.toGlobalX(drawX), parent.toGlobalY(drawY + getHeight() - 1), getWidth(), TextColor.ANSI.DEFAULT, null);

				painter.drawSmartVerticalLine(parent.toGlobalX(drawX), parent.toGlobalY(drawY), getHeight(), TextColor.ANSI.DEFAULT, null);
				painter.drawSmartVerticalLine(parent.toGlobalX(drawX + getWidth() - 1), parent.toGlobalY(drawY), getHeight(), TextColor.ANSI.DEFAULT, null);
			} else {
				painter.drawSmartHorizontalLine(drawX, drawY, getWidth(), TextColor.ANSI.DEFAULT, null);
				painter.drawSmartHorizontalLine(drawX, drawY + getHeight() - 1, getWidth(), TextColor.ANSI.DEFAULT, null);

				painter.drawSmartVerticalLine(drawX, drawY, getHeight(), TextColor.ANSI.DEFAULT, null);
				painter.drawSmartVerticalLine(drawX + getWidth() - 1, drawY, getHeight(), TextColor.ANSI.DEFAULT, null);
			}
		}
		drawContent(painter);
	}

	protected abstract void drawContent(Painter painter);

	@Override
	public void layout() {

	}

}
