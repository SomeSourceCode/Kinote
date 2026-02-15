package de.unistuttgart.einf.moviemanager.cli.api.util;

import de.unistuttgart.einf.moviemanager.cli.api.TextAlignment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A utility class to manipulate and format text.
 */
public class TextUtils {

	private TextUtils() {}

	/**
	 * Splits the text into lines of at most the given length, preserving
	 * line breaks and collapsing multiple spaces into one. No wrapped line will
	 * start or end with a space.
	 *
	 * @param text the text
	 * @param maxWidth the maximum width
	 * @return the wrapped lines
	 */
	public static List<String> wrapText(String text, int maxWidth) {
		if (text == null || text.isEmpty() || maxWidth < 1) {
			return Collections.emptyList();
		}

		final List<String> lines = new ArrayList<>();
		final String[] paragraphs = text.split("\n", -1);

		for (String paragraph : paragraphs) {
			if (paragraph.isEmpty()) {
				lines.add("");
				continue;
			}

			final StringBuilder builder = new StringBuilder();
			final String[] words = paragraph.split("\\s+");

			for (String word : words) {
				if (word.isEmpty()) {
					continue;
				}

				if (builder.length() + (!builder.isEmpty() ? 1 : 0) + word.length() <= maxWidth) {
					if (!builder.isEmpty()) {
						builder.append(" ");
					}
					builder.append(word);
					continue;
				}

				if (word.length() <= maxWidth) {
					lines.add(builder.toString());
					builder.setLength(0);
					builder.append(word);
					continue;
				}

				// giant word
				if (maxWidth > builder.length()) {
					if (!builder.isEmpty()) {
						builder.append(" ");
					}
					final int spaceLeft = maxWidth - builder.length();
					builder.append(word, 0, spaceLeft);
					lines.add(builder.toString());
					builder.setLength(0);

					word = word.substring(spaceLeft);
				} else if (!builder.isEmpty()) {
					lines.add(builder.toString());
					builder.setLength(0);
				}

				while (word.length() > maxWidth) {
					lines.add(word.substring(0, maxWidth));
					word = word.substring(maxWidth);
				}

				builder.append(word);
			}
			if (!builder.isEmpty()) {
				lines.add(builder.toString());
			}
		}
		return lines;
	}

	/**
	 * Abbreviates the text to the specified maximum length, adding the given ellipsis
	 * if the text exceeds the maximum length. If the maximum length is less than or equal
	 * to the length of the ellipsis, the text will just be truncated.
	 * <p>
	 * Note that this method first tries to abbreviate the text by removing trailing spaces.
	 *
	 * @param text the text
	 * @param maxLength the maximum length
	 * @param ellipsis the ellipsis to append if the text is abbreviated
	 * @return the abbreviated text
	 */
	public static String abbreviate(String text, int maxLength, String ellipsis) {
		if (text == null) {
			return null;
		}
		if (maxLength < 0) {
			return "";
		}
		text = text.stripTrailing();
		if (text.length() <= maxLength) {
			return text;
		}
		final int ellipsisLength = ellipsis.length();
		if (maxLength <= ellipsisLength) {
			return text.substring(0, maxLength);
		}
		return text.substring(0, maxLength - ellipsisLength).stripTrailing() + ellipsis;
	}

	/**
	 * Abbreviates the text using "..." as the ellipsis. For more details see {@link #abbreviate(String, int, String)}.
	 *
	 * @param text the text
	 * @param maxLength the maximum length
	 * @return the abbreviated text
	 */
	public static String abbreviate(String text, int maxLength) {
		return abbreviate(text, maxLength, "...");
	}

	/**
	 * Abbreviates the start of the text to the specified maximum length, prepending the given ellipsis
	 * if the text exceeds the maximum length. If the maximum length is less than or equal
	 * to the length of the ellipsis, the text will just be truncated from the start.
	 * <p>
	 * Note that this method first tries to abbreviate the text by removing leading spaces.
	 *
	 * @param text the text
	 * @param maxLength the maximum length
	 * @param ellipsis the ellipsis to prepend if the text is abbreviated
	 * @return the abbreviated text
	 */
	public static String abbreviateStart(String text, int maxLength, String ellipsis) {
		if (text == null) {
			return null;
		}
		if (maxLength < 0) {
			return "";
		}
		text = text.stripLeading();
		if (text.length() <= maxLength) {
			return text;
		}
		final int ellipsisLength = ellipsis.length();
		if (maxLength <= ellipsisLength) {
			return text.substring(text.length() - maxLength);
		}
		return ellipsis + text.substring(text.length() - (maxLength - ellipsisLength)).stripLeading();
	}

	/**
	 * Abbreviates the start of the text using "..." as the ellipsis.
	 * For more details see {@link #abbreviateStart(String, int, String)}.
	 *
	 * @param text the text
	 * @param maxLength the maximum length
	 * @return the abbreviated text
	 */
	public static String abbreviateStart(String text, int maxLength) {
		return abbreviateStart(text, maxLength, "...");
	}

	/**
	 * Pads the string on the left with the given character until it reaches the specified length.
	 *
	 * @param text the text to pad
	 * @param length the target length
	 * @param padChar the character to pad with
	 * @return the padded string
	 */
	public static String padLeft(String text, int length, char padChar) {
		if (text == null) {
			return null;
		}
		if (text.length() >= length) {
			return text;
		}
		return String.valueOf(padChar).repeat(length - text.length()) + text;
	}

	/**
	 * Pads the string on the left with spaces until it reaches the specified length.
	 *
	 * @param text the text to pad
	 * @param length the target length
	 * @return the padded string
	 */
	public static String padLeft(String text, int length) {
		return padLeft(text, length, ' ');
	}

	/**
	 * Pads the string on the right with the given character until it reaches the specified length.
	 *
	 * @param text the text to pad
	 * @param length the target length
	 * @param padChar the character to pad with
	 * @return the padded string
	 */
	public static String padRight(String text, int length, char padChar) {
		if (text == null) {
			return null;
		}
		if (text.length() >= length) {
			return text;
		}
		return text + String.valueOf(padChar).repeat(length - text.length());
	}

	/**
	 * Pads the string on the right with spaces until it reaches the specified length.
	 *
	 * @param text the text to pad
	 * @param length the target length
	 * @return the padded string
	 */
	public static String padRight(String text, int length) {
		return padRight(text, length, ' ');
	}

	/**
	 * Pads the string on both sides with the given character until it reaches the specified length.
	 * If the padding cannot be distributed equally, the right side will have one more character.
	 *
	 * @param text the text to pad
	 * @param length the target length
	 * @param padChar the character to pad with
	 * @return the padded string
	 */
	public static String padCenter(String text, int length, char padChar) {
		if (text == null) {
			return null;
		}
		if (text.length() >= length) {
			return text;
		}
		final int padding = length - text.length();
		final int leftPadding = padding / 2;
		final int rightPadding = padding - leftPadding;

		return String.valueOf(padChar).repeat(leftPadding) + text + String.valueOf(padChar).repeat(rightPadding);
	}

	/**
	 * Pads the string on both sides with spaces until it reaches the specified length.
	 *
	 * @param text the text to pad
	 * @param length the target length
	 * @return the padded string
	 */
	public static String padCenter(String text, int length) {
		return padCenter(text, length, ' ');
	}

	/**
	 * Pads the string with the given character until it reaches the specified length,
	 * according to the specified alignment.
	 *
	 * @param text the text to pad
	 * @param length the target length
	 * @param padChar the character to pad with
	 * @param alignment the alignment to use for padding
	 * @return the padded string
	 */
	public static String pad(String text, int length, char padChar, TextAlignment alignment) {
		return switch (alignment) {
			case LEFT -> padRight(text, length, padChar);
			case RIGHT -> padLeft(text, length, padChar);
			case CENTER -> padCenter(text, length, padChar);
		};
	}

	/**
	 * Pads the string with spaces until it reaches the specified length,
	 * according to the specified alignment.
	 *
	 * @param text the text to pad
	 * @param length the target length
	 * @param alignment the alignment to use for padding
	 * @return the padded string
	 */
	public static String pad(String text, int length, TextAlignment alignment) {
		return pad(text, length, ' ', alignment);
	}

}
