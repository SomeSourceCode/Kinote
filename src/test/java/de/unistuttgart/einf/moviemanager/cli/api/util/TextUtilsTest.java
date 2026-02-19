package de.unistuttgart.einf.moviemanager.cli.api.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TextUtilsTest {

	@Nested
	class WrapTextTest {

		private void assertWrapping(String input, int width, List<String> expected) {
			assertWrapping(input, width, expected, "Failed to wrap text");
		}

		private void assertWrapping(String input, int width, List<String> expected, String message) {
			final List<String> actual = TextUtils.wrapText(input, width);

			assertEquals(expected, actual, () -> String.format(
					"%s: \"%s\" (width: %d)", message, input, width
			));
		}

		@Test
		@DisplayName("Should wrap text")
		void testWrap() {
			assertWrapping("Hello World", 5, List.of("Hello", "World"));
			assertWrapping("Hello World", 10, List.of("Hello", "World"));
			assertWrapping("Hello World", 11, List.of("Hello World"));
		}

		@Test
		@DisplayName("Should return empty list for empty string or null")
		void testWrapEmpty() {
			assertWrapping(null, 10, Collections.emptyList(), "Should return empty list for null");
			assertWrapping("", 10, Collections.emptyList(), "Should return empty list empty string");
			assertWrapping("   ", 10, Collections.emptyList(), "Should return empty list string for only spaces");
		}

		@Test
		@DisplayName("Should collapse multiple spaces")
		void testWrapMultipleSpaces() {
			assertWrapping("A   B", 10, List.of("A B"), "Should collapse multiple spaces into one");
			assertWrapping("  Lead Trail  ", 10, List.of("Lead Trail"), "Should remove leading and trailing spaces");
		}

		@Test
		@DisplayName("Should split giant words")
		void testWrapGiantWords() {
			assertWrapping("ABCDE", 3, List.of("ABC", "DE"), "Should split words longer than max width");
			assertWrapping("ABCDE", 2, List.of("AB", "CD", "E"), "Should split words longer than max width over multiple lines");
			assertWrapping("Giant WORD0123456789", 10, List.of("Giant WORD", "0123456789"), "Should start giant word on the same line");
		}

		@Test
		@DisplayName("Should preserve explicit newlines")
		void testWrapPreservesLineBreaks() {
			assertWrapping("A\nB", 10, List.of("A", "B"), "Should preserve explicit line breaks");
			assertWrapping("A\n\nB", 10, List.of("A", "", "B"), "Should preserve explicit new lines between paragraphs");
		}

	}

	@Nested
	class AbbreviateTest {

		@Test
		@DisplayName("Should abbreviate text")
		void testAbbreviate() {
			assertEquals("Hello W...", TextUtils.abbreviate("Hello World", 10));
			assertEquals("Hello World", TextUtils.abbreviate("Hello World", 11));
			assertEquals("Hello World", TextUtils.abbreviate("Hello World", 20));
		}

		@Test
		@DisplayName("Should not use ellipsis if max width is too small")
		void testAbbreviateSmallWidth() {
			assertEquals("H", TextUtils.abbreviate("Hello World", 1), "Should not use ellipsis if max width is too small");
			assertEquals("He", TextUtils.abbreviate("Hello World", 2), "Should not use ellipsis if max width is too small");
			assertEquals("Hel", TextUtils.abbreviate("Hello World", 3), "Should not use ellipsis if max width is too small");
		}

		@Test
		@DisplayName("Should return empty string for empty string or null")
		void testAbbreviateEmpty() {
			assertEquals("", TextUtils.abbreviate("", 10), "Should return empty string for empty string");
			assertEquals("", TextUtils.abbreviate("   ", 10), "Should return empty string for string with only spaces");
			assertEquals("", TextUtils.abbreviate( "               ", 10), "Should return empty string for string with only spaces");
		}

		@Test
		@DisplayName("Should try to remove trailing spaces before using ellipsis")
		void testAbbreviateTrailingSpaces() {
			assertEquals("Hello", TextUtils.abbreviate("Hello     ", 10), "Should remove trailing spaces");
			assertEquals("Hello", TextUtils.abbreviate("Hello     ", 5), "Should remove trailing spaces instead of using ellipsis");
			assertEquals("Hello...", TextUtils.abbreviate("Hello World    ", 8), "Should remove trailing spaces before applying ellipsis");
		}

	}

}
