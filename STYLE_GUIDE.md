# Style Guide

## 1. Introduction

This style guide outlines the conventions and best practices for writing clean, maintainable, and consistent code.
Following these guidelines will help ensure that our codebase remains easy to read and collaborate on.

## 2. Source File Basics

### 2.1. File Encoding

All source files must use **UTF-8** encoding.

### 2.2. File Naming

The source file must exactly match the public class name (case-sensitive) contained within it and end with the `.java` extension (e.g. `Main.java`).

### 2.3. Line Endings

All source files must use **Unix-style** line endings (`\n`).
To ensure consistency, configure your git client to use `core.autocrlf` set to `input` on Unix/Linux/MacOS systems and `true` on Windows systems.

To set this configuration, run the following command:

```bash
$ git config --global core.autocrlf input # for Unix/Linux/MacOS
$ git config --global core.autocrlf true # for Windows
```

### 2.4. Indentation

You **must** use tabs for indentation. A single tab character is used for every level of indentation.

In IntelliJ, you can find the indentation settings under: `Settings` > `Editor` > `Code Style` > `Java`.
Select the `Tabs and Indentations` tab and ensure that the `Use tab character` option is checked.
You can set your tab size via the `Tab size` field (commonly set to 4).

The same applies for indentation in `xml` (e.g. `pom.xml`) files.

### 2.5. Line Length

There is no hard limit on line length, but lines should be kept *reasonably* short for readability.
If lines must be wrapped, do so according to the guidelines in section TODO.

## 3. Naming Conventions

### 3.1. Packages

The projects top-level package is `de.unistuttgart.einf.moviemanager`.

All packages must be named in all lowercase and avoid underscores, hyphens, or other special characters.

### 3.2. Classes and Interfaces

Class, interface, enum and record names must be written in **PascalCase** (UpperCamelCase).

- **Classes:** Nouns describing the object (e.g. `Movie`, `MovieManager`)
- **Interfaces:** Nouns (e.g. `Storage`) or adjectives (e.g. `Watchable`)
- **Enums:** Nouns describing a set of related constants (e.g. `Genre`, `Rating`)
- **Records:** Nouns describing the data they hold (e.g. `MovieData`, `UserInfo`)

### 3.3. Methods

Method names must be written in **camelCase** and should generally be verbs or verb phrases describing the action (e.g. `openWindow`, `calculateRating`).

- **Getters:** Must start with `get` (e.g. `getTitle()`, `getReleaseDate()`).
  - **Booleans:** Must start with `is`, `has` or `can` (e.g. `isAvailable()`, `hasSubtitles()`, `canPlay()`).
- **Setters:** Must start with `set` (e.g. `setTitle(String title)`).

### 3.4. Variables

Variables names must be written in **camelCase** and be meaningful.
Avoid single-letter names unless used as loop counters (e.g. `i`, `j`, `k`).

### 3.5. Constants

Constant names (fields that are `static final`) must be written in **UPPER_SNAKE_CASE** (all uppercase letters with words separated by underscores) (e.g. `MAX_RETRIES`, `DEFAULT_TIMEOUT`).

### 3.6. Type Parameters (Generics)

Type parameter names must be single uppercase letters (e.g. `<T>` for Type, `<E>` for Element, `<K, V>` for Key/Value).

## 4. Formatting

### 4.1. Braces (`{}`)

We use the **K&R style** for braces: the opening brace `{` is placed at the end of the current line, and the closing brace `}` is placed on a new line aligned with the beginning of the line containing the corresponding opening brace.

### 4.2. Block Indentation

A single tab is used for every level of indentation (after opening braces `{`).
Empty lines should not contain any whitespace characters (spaces or tabs).

In IntelliJ, you can enforce this under `Settings` > `Editor` > `Code Style` > `Java`. Select the `Tabs and Indents` tab and make sure the option `Keep indents on empty lines` is **unchecked**.

### 4.3. Empty Blocks

If a block (e.g. method, class, control structure) is empty, opening and closing braces **may** be placed on the same line.

### 4.4. Whitespace

- **Binary Operators:** Always surround binary operators (e.g. `=`, `+`, `-`, `*`, `/`, `==`, `!=`, `<`, `>`, `<=`, `>=`, `&&`, `||`, etc.) with a single space (e.g. `a + b`, `x == y`).
- **Keywords:** Always follow keywords (e.g. `if`, `for`, `while`, `switch`, `return`, `catch`, etc.) with a single space (e.g. `if (condition)`, `for (int i = 0; i < n; i++)`).
- **Method/Constructor calls:** Do not put a space between the method/constructor name and the opening parenthesis `(` (e.g. `myMethod(arg1, arg2)`).
- **Commas:** Always follow a comma `,` with a single space (e.g. `arg1, arg2, arg3`).
- **Semicolons:** Do not put a space before a semicolon `;`.

### 4.5. Imports

Layout static imports and regular imports separately.

### 4.6. End of File

Ensure that all source files end with a single newline character.

In IntelliJ, you can do this by selecting the `Ensure every saved file ends with a line break` option under `Settings` > `Editor` > `General`.

### 4.7. Line Wrapping

When a line exceeds a reasonable length, break it at logical points such as after commas, before operators, or after opening parentheses.

### 4.8. Example

This is a simple non-exhaustive example demonstrating the above formatting rules:

```java
public class FooBar {

	public static final int MAX_COUNT = 100;

	private int count;

	public FooBar() {
		this.count = 0;
	}

	public FooBar(int initialCount) {
		this.count = initialCount;
	}

	public void incrementCount() {
		if (count < MAX_COUNT) {
			count++;
		} else {
			System.out.println("Maximum count reached.");
		}
	}

	public int getCount() {
		return count;
	}

	public boolean canIncrement() {
		return count < MAX_COUNT;
    }

	public boolean isMaxedOut() {
		return count >= MAX_COUNT;
	}

}
```

## 5. Documentation

### 5.1. Javadoc

All public, protected and package-private members, classes, interfaces, enums and records must be documented using Javadoc comments.
Use tags such as `@param`, `@return`, and `@throws` where appropriate and in this following order:

- `@param`
- `@return`
- `@throws`
- `@see`

### 5.2. Implementation Comments

Generally, implementation comments should be avoided in favor of clear and self-explanatory code.
However, *if* a complex or non-obvious piece of code requires explanation, comments should be used judiciously to clarify intent.

Before adding a comment, consider whether the code can be refactored to be more understandable, e.g. by extracting methods with descriptive names or using well-named variables.

Use `//` for brief and `/* ... */` for longer comments.
If not possible otherwise, use `// TODO:` to indicate tasks that need to be completed later.

## 6. Programming Practices

### 6.1. Imports

Avoid wildcard imports (e.g. `import java.util.*;`), unless the number of imported classes from the same package is reasonably high (e.g. more than 5).
This can be automatically configured in IntelliJ under the `Settings` > `Editor` > `Code Style` > `Java` > `Imports` tab by setting the `Class count to use import with '*'` value to `5`.

### 6.2. Early Exit

Prefer early exits in methods to reduce nesting and improve readability, e.g.

```java
if (!isValid(input)) {
	return;
}
// proceed with valid input
```

in favor of

```java
if (isValid(input)) {
	// proceed with valid input
}
```

### 6.3. Exception Handling

- Catch specific exceptions instead of generic ones (e.g. `IOException` instead of `Exception`).
- Throw exceptions that are meaningful and provide context about the error (e.g. `throw new IllegalArgumentException("number must be non-negative")`).

### 6.4. Generics

Use the diamond operator `<>` to infer generic types where possible (e.g. `List<String> list = new ArrayList<>();`).

### 6.5. Interface preference

Prefer interfaces (e.g. `List`, `Map`, `Set`) over concrete implementations (e.g. `ArrayList`, `HashMap`, `HashSet`) when declaring variables, return types, and parameters.
