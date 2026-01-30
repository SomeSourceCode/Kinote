# Kinote

The movie manager project for EInf.

# Project Structure

The project is structured into a few main packages:

- `model`
- `io`
- `service`
- `ui/cli` (to be added)

## Model

The model includes all classes, interfaces etc. used to model the underlying data, such as the most basic `Media` or the special `Movie`, `Series`, `Season` and `Episode`.

Their functionalities can be summarized like this:

### [`Media.java`](src/main/java/de/unistuttgart/einf/moviemanager/model/Media.java)

This is the interface every type of media implements.
It provides methods to set the most basic properties, such as title, description or similar.

### [`MediaBase.java`](src/main/java/de/unistuttgart/einf/moviemanager/model/MediaBase.java)

This class implements those methods of `Media` that are implemented the same in all (or most) subclasses.

### [`ChildMedia.java`](src/main/java/de/unistuttgart/einf/moviemanager/model/ChildMedia.java) and [`ParentMedia.java`](src/main/java/de/unistuttgart/einf/moviemanager/model/ParentMedia.java)

Since some media types can contain others, such as the `Series` which contains `Seasons`, there exists an interface that needs to be implemented called `ParentMedia`.
To be a child of a parent, a media type must extend the `ChildMedia` class.
Both of these classes help avoid code duplication.

Since java prohibits the extension of multiple classes, `ParentMedia` is an interface.
To prevent code duplication, parent methods are delegated to a [`MediaContainer`](src/main/java/de/unistuttgart/einf/moviemanager/model/TopLevelMedia.java) helper class.

### [`TopLevelMedia.java`](src/main/java/de/unistuttgart/einf/moviemanager/model/ChildMedia.java)

This is an interface that is mainly used for tagging the `Movie` and `Series` classes as being a "top level media type".
That means that only those are the ones managed by the `MediaService` or displayed in the "main" list of the application.

### [`Movie.java`](src/main/java/de/unistuttgart/einf/moviemanager/model/Movie.java), [`Series.java`](src/main/java/de/unistuttgart/einf/moviemanager/model/Series.java), [`Season.java`](src/main/java/de/unistuttgart/einf/moviemanager/model/Season.java), [`Episode.java`](src/main/java/de/unistuttgart/einf/moviemanager/model/Episode.java)

These are the concrete implementations for the actual media types. Here's a basic example usage

```java
import de.unistuttgart.einf.moviemanager.model.*;

void main() {
	// 1. A standalone Movie
	Movie movie = new Movie("The Lord of the Strings: Return of the CharArray");
	movie.setDescription("Frodo tries to cast a Ring into the Fire, but gets a ClassCastException.");

	// 2. A Series (TopLevelMedia)
	Series series = new Series("Breaking Bug");
	series.setDescription("A high school teacher starts writing untyped JavaScript to pay for his server costs.");

	// 3. A Season (Child of Series)
	Season season1 = new Season("Season 1: The Spaghetti Code");
	
	// Connect Season to Series
	series.addChild(season1);

	// 4. Episodes (Child of Season)
	Episode ep1 = new Episode("Ep 1: Public Static Void Pain");
	ep1.setDescription("Walter White forgets a semicolon and crashes the build.");

	Episode ep2 = new Episode("Ep 2: The Null Pointer");
	ep2.setDescription("Jesse tries to access a variable that doesn't exist.");

	// Connect Episodes to Season
	season1.addChild(ep1);
	season1.addChild(ep2);
	
	// Now 'series' contains the season, which contains the episodes.

	System.out.println("Binge-watching " + series.getTitle() + "...");

	for (Season s : series.getChildren()) {
		System.out.println("Loading " + s);

		for (Episode e : s.getChildren()) {
			System.out.println("Now playing: " + e);
			e.setWatched(true);
		}
	}
	// Note: you can directly iterate over the children via "for (Season s : series)"
}
```

## IO

The io classes are used to serialize (changing the format to something storable), deserialize, store and load the data, e.g. the added movies and series.

### [`DataSerializer.java`](src/main/java/de/unistuttgart/einf/moviemanager/io/DataSerializer.java)

This interface is responsible for the (de)serialization process.
A class implementing this interface must provide a way to convert the data (e.g. the movie/series list) into a format that is streamed to the output stream (e.g. json, plain text, binary, ...)
as well as reverse that process (deserialize).

### [`FileRepository.java`](src/main/java/de/unistuttgart/einf/moviemanager/io/FileRepository.java)

This class saves the serialized data to a given file and loads it again when requested.

## Service

The service package contains classes that can be used to interact with the model.

### [`MediaService.java`](src/main/java/de/unistuttgart/einf/moviemanager/service/MediaService.java)

This contains the methods to add, remove or get Media or for similar functionality (like retrieving a filtered list, etc.).
