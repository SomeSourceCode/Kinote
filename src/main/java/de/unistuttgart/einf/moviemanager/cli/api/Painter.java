package de.unistuttgart.einf.moviemanager.cli.api;

import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.screen.Screen;

import java.util.HashMap;
import java.util.Map;

/**
 * A helper to draw on a terminal screen
 */
public class Painter {

	private static final char HORIZONTAL_LINE = '─';
	private static final char VERTICAL_LINE = '│';

	private static final char TOP_LEFT_CORNER = '┌';
	private static final char TOP_RIGHT_CORNER = '┐';
	private static final char BOTTOM_LEFT_CORNER = '└';
	private static final char BOTTOM_RIGHT_CORNER = '┘';

	private static final char T_UP = '┴';
	private static final char T_DOWN = '┬';
	private static final char T_LEFT = '┤';
	private static final char T_RIGHT = '├';

	private static final char CROSS = '┼';

	// bit masks
	private static final int UP = 1; // 0001
	private static final int DOWN = 2; // 0010
	private static final int LEFT = 4; // 0100
	private static final int RIGHT = 8; // 1000

	private static final char[] MASK_TO_CHAR = new char[16];
	private static final Map<Character, Integer> CHAR_TO_MASK = new HashMap<>();

	static {
		// Point
		MASK_TO_CHAR[0] = ' ';

		// Lines
		MASK_TO_CHAR[UP] = VERTICAL_LINE;
		MASK_TO_CHAR[DOWN] = VERTICAL_LINE;
		MASK_TO_CHAR[LEFT] = HORIZONTAL_LINE;
		MASK_TO_CHAR[RIGHT] = HORIZONTAL_LINE;

		MASK_TO_CHAR[UP | DOWN] = VERTICAL_LINE;
		MASK_TO_CHAR[LEFT | RIGHT] = HORIZONTAL_LINE;

		// Corners
		MASK_TO_CHAR[RIGHT | DOWN] = TOP_LEFT_CORNER;
		MASK_TO_CHAR[LEFT | DOWN] = TOP_RIGHT_CORNER;
		MASK_TO_CHAR[RIGHT | UP] = BOTTOM_LEFT_CORNER;
		MASK_TO_CHAR[LEFT | UP] = BOTTOM_RIGHT_CORNER;

		// T-Junctions
		MASK_TO_CHAR[LEFT | RIGHT | UP] = T_UP;
		MASK_TO_CHAR[LEFT | RIGHT | DOWN] = T_DOWN;
		MASK_TO_CHAR[UP | DOWN | LEFT] = T_LEFT;
		MASK_TO_CHAR[UP | DOWN | RIGHT] = T_RIGHT;

		// Cross
		MASK_TO_CHAR[UP | DOWN | LEFT | RIGHT] = CROSS;

		CHAR_TO_MASK.put(HORIZONTAL_LINE, LEFT | RIGHT);
		CHAR_TO_MASK.put(VERTICAL_LINE, UP | DOWN);

		CHAR_TO_MASK.put(TOP_LEFT_CORNER, RIGHT | DOWN);
		CHAR_TO_MASK.put(TOP_RIGHT_CORNER, LEFT | DOWN);
		CHAR_TO_MASK.put(BOTTOM_LEFT_CORNER, RIGHT | UP);
		CHAR_TO_MASK.put(BOTTOM_RIGHT_CORNER, LEFT | UP);

		CHAR_TO_MASK.put(T_UP, LEFT | RIGHT | UP);
		CHAR_TO_MASK.put(T_DOWN, LEFT | RIGHT | DOWN);
		CHAR_TO_MASK.put(T_LEFT, UP | DOWN | LEFT);
		CHAR_TO_MASK.put(T_RIGHT, UP | DOWN | RIGHT);

		CHAR_TO_MASK.put(CROSS, UP | DOWN | LEFT | RIGHT);
	}

	private final Screen screen;

	/**
	 * Constructs a screen for the given screen.
	 *
	 * @param screen the screen
	 */
	public Painter(Screen screen) {
		if (screen == null) {
			throw new IllegalArgumentException("screen must be non-null");
		}
		this.screen = screen;
	}

	/**
	 * Draws the character at the specified position on the screen.
	 *
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param character the character
	 * @param textColor the text color
	 * @param backgroundColor the background color
	 */
	public void drawChar(int x, int y, char character, TextColor textColor, TextColor backgroundColor) {
		screen.setCharacter(x, y, TextCharacter.fromCharacter(character, textColor, backgroundColor)[0]);
	}

	/**
	 * Draws the character at the specified position on the screen.
	 *
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param character the character
	 * @param textColor the text color
	 */
	public void drawChar(int x, int y, char character, TextColor textColor) {
		drawChar(x, y, character, textColor, null);
	}

	/**
	 * Draws the character at the specified position on the screen.
	 *
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param character the character
	 */
	public void drawChar(int x, int y, char character) {
		drawChar(x, y, character, TextColor.ANSI.DEFAULT, null);
	}

	/**
	 * Draws the string at the specified position on the screen, stopping
	 * after the provided maximum length.
	 *
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param length the length cutoff
	 * @param text the text
	 * @param textColor the text color
	 * @param backgroundColor the background color
	 */
	public void drawString(int x, int y, int length, String text, TextColor textColor, TextColor backgroundColor) {
		if (text == null) {
			throw new IllegalArgumentException("text must be non-null");
		}
		for (int i = 0; i < Math.min(text.length(), length); i++) {
			drawChar(x + i, y, text.charAt(i), textColor, backgroundColor);
		}
	}

	/**
	 * Draws the string at the specified position on the screen, stopping
	 * after the provided maximum length.
	 *
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param length the length cutoff
	 * @param text the text
	 * @param textColor the text color
	 */
	public void drawString(int x, int y, int length, String text, TextColor textColor) {
		drawString(x, y, length, text, textColor, null);
	}

	/**
	 * Draws the string at the specified position on the screen, stopping
	 * after the provided maximum length.
	 *
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param length the length cutoff
	 * @param text the text
	 */
	public void drawString(int x, int y, int length, String text) {
		drawString(x, y, length, text, TextColor.ANSI.DEFAULT, null);
	}

	/**
	 * Draws the string at the specified position on the screen, stopping
	 * after the provided maximum length.
	 *
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param text the text
	 * @param textColor the text color
	 * @param backgroundColor the background color
	 */
	public void drawString(int x, int y, String text, TextColor textColor, TextColor backgroundColor) {
		drawString(x, y, text == null ? 0 : text.length(), text, textColor, backgroundColor);
	}

	/**
	 * Draws the string at the specified position on the screen, stopping
	 * after the provided maximum length.
	 *
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param text the text
	 * @param textColor the text color
	 */
	public void drawString(int x, int y, String text, TextColor textColor) {
		drawString(x, y, text == null ? 0 : text.length(), text, textColor, null);
	}

	/**
	 * Draws the string at the specified position on the screen, stopping
	 * after the provided maximum length.
	 *
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param text the text
	 */
	public void drawString(int x, int y, String text) {
		drawString(x, y, text == null ? 0 : text.length(), text, TextColor.ANSI.DEFAULT, null);
	}

	/* *************************************************************** *
	 *                           Smart lines                           *
	 * *************************************************************** */

	/**
	 * Draws a horizontal line with the given length at the specified coordinates. If it
	 * comes across another line, it automatically connects with it visually.
	 *
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param length the length
	 * @param lineColor the line color
	 * @param backgroundColor the background color
	 */
	public void drawSmartHorizontalLine(int x, int y, int length, TextColor lineColor, TextColor backgroundColor) {
		if (length <= 0) {
			return;
		}

		connect(x - 1, y, RIGHT);

		for (int i = 0; i < length; i++) {
			final int characterX = x + i;

			final boolean connectLeft = (i > 0) || connects(characterX - 1, y, RIGHT);
			final boolean connectRight = (i < length - 1) || connects(characterX + 1, y, LEFT);
			final boolean connectUp = connects(characterX, y - 1, DOWN);
			final boolean connectDown = connects(characterX, y + 1, UP);

			drawMaskedChar(characterX, y, connectUp, connectDown, connectLeft, connectRight, lineColor, backgroundColor);
		}

		connect(x + length, y, LEFT);
	}

	/**
	 * Draws a vertical line with the given length at the specified coordinates. If it
	 * comes across another line, it automatically connects with it visually.
	 *
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param length the length
	 * @param lineColor the line color
	 * @param backgroundColor the background color
	 */
	public void drawSmartVerticalLine(int x, int y, int length, TextColor lineColor, TextColor backgroundColor) {
		if (length <= 0) {
			return;
		}

		connect(x, y - 1, DOWN);

		for (int i = 0; i < length; i++) {
			final int characterY = y + i;

			final boolean connectUp = (i > 0) || connects(x, characterY - 1, DOWN);
			final boolean connectDown = (i < length - 1) || connects(x, characterY + 1, UP);
			final boolean connectLeft = connects(x - 1, characterY, RIGHT);
			final boolean connectRight = connects(x + 1, characterY, LEFT);

			drawMaskedChar(x, characterY, connectUp, connectDown, connectLeft, connectRight, lineColor, backgroundColor);
		}

		connect(x, y + length, UP);
	}

	private void drawMaskedChar(int x, int y, boolean up, boolean down, boolean left, boolean right, TextColor textColor, TextColor backgroundColor) {
		int mask = 0;
		if (up) {
			mask |= UP;
		}
		if (down) {
			mask |= DOWN;
		}
		if (left) {
			mask |= LEFT;
		}
		if (right) {
			mask |= RIGHT;
		}
		drawChar(x, y, MASK_TO_CHAR[mask], textColor, backgroundColor);
	}

	private boolean connects(int x, int y, int directionToLookFor) {
		if (x < 0 || y < 0 || x >= screen.getTerminalSize().getColumns() || y >= screen.getTerminalSize().getRows()) {
			return false;
		}

		final TextCharacter backChar = screen.getBackCharacter(x, y);
		if (backChar == null) {
			return false;
		}

		final char character = backChar.getCharacterString().charAt(0);
		return (CHAR_TO_MASK.getOrDefault(character, 0) & directionToLookFor) != 0;
	}

	private void connect(int x, int y, int direction) {
		final TextCharacter backChar = screen.getBackCharacter(x, y);
		if (backChar == null) {
			return;
		}

		final char character = backChar.getCharacterString().charAt(0);
		final Integer mask = CHAR_TO_MASK.get(character);
		if (mask == null) {
			return;
		}

		final TextColor lineColor = backChar.getForegroundColor();
		final TextColor backgroundColor = backChar.getBackgroundColor();

		drawChar(x, y, MASK_TO_CHAR[mask | direction], lineColor, backgroundColor);
	}

}
