import de.unistuttgart.einf.moviemanager.io.FileRepository;
import de.unistuttgart.einf.moviemanager.io.GsonDataSerializer;
import de.unistuttgart.einf.moviemanager.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class FileRepositoryJsonTest {

	@TempDir
	Path tempDir = Path.of("testRepository/");

	@Test
	void loadReturnsNullIfFileDoesNotExist() {
		Path file = tempDir.resolve("does-not-exist.json");
		FileRepository<Set<TopLevelMedia>> repo =
				new FileRepository<>(file, new GsonDataSerializer());

		assertNull(repo.load());
	}

	@Test
	void saveCreatesFileAndIsNotEmpty() throws Exception {
		Path file = tempDir.resolve("media.json");
		FileRepository<Set<TopLevelMedia>> repo =
				new FileRepository<>(file, new GsonDataSerializer());

		Set<TopLevelMedia> data = new HashSet<>();
		data.add(new Movie(null, "Inception", "Dreams", true));

		repo.save(data);

		assertTrue(Files.exists(file));
		assertTrue(Files.size(file) > 0);
	}

	@Test
	void roundtripMoviePreservesFields() {
		Path file = tempDir.resolve("media.json");
		FileRepository<Set<TopLevelMedia>> repo =
				new FileRepository<>(file, new GsonDataSerializer());

		Movie m = new Movie(null, "Interstellar", "Space", true);
		Set<TopLevelMedia> data = new HashSet<>();
		data.add(m);

		repo.save(data);
		Set<TopLevelMedia> loaded = repo.load();

		assertNotNull(loaded);
		assertEquals(1, loaded.size());

		TopLevelMedia only = loaded.iterator().next();
		assertInstanceOf(Movie.class, only);

		Movie lm = (Movie) only;
		assertEquals("Interstellar", lm.getTitle());
		assertEquals("Space", lm.getDescription());
		assertEquals(Status.WATCHED, lm.getStatus());
	}


	@Test
	void roundtripSeriesPreservesTreeAndRelinksParents() {
		Path file = tempDir.resolve("media.json");
		FileRepository<Set<TopLevelMedia>> repo =
				new FileRepository<>(file, new GsonDataSerializer());

		// build: Series -> Season -> Episodes
		Series s = new Series("Dark", "Time travel");
		Season season1 = new Season(1, "S1", "Start");
		Episode e1 = new Episode(1, "E1", "Intro", true);
		Episode e2 = new Episode(2, "E2", "More", false);

		season1.addChild(e1);
		season1.addChild(e2);
		s.addChild(season1);

		Set<TopLevelMedia> data = new HashSet<>();
		data.add(s);

		repo.save(data);
		Set<TopLevelMedia> loaded = repo.load();

		assertNotNull(loaded);
		assertEquals(1, loaded.size());

		TopLevelMedia only = loaded.iterator().next();
		assertInstanceOf(Series.class, only);

		Series ls = (Series) only;
		assertEquals("Dark", ls.getTitle());
		assertEquals("Time travel", ls.getDescription());

		assertEquals(s.getId(), ls.getId());
		assertEquals(1, ls.getChildren().size());
		Season lSeason1 = ls.getChild(1);
		assertNotNull(lSeason1);
		assertEquals("S1", lSeason1.getTitle());

		assertEquals(2, lSeason1.getChildren().size());
		Episode le1 = lSeason1.getChild(1);
		Episode le2 = lSeason1.getChild(2);
		assertNotNull(le1);
		assertNotNull(le2);

		assertEquals("E1", le1.getTitle());
		assertEquals(Status.WATCHED, le1.getStatus());
		assertEquals("E2", le2.getTitle());
		assertEquals(Status.UNWATCHED, le2.getStatus());


		assertSame(lSeason1, le1.getSeason());
		assertSame(lSeason1, le2.getSeason());
		assertSame(ls, lSeason1.getParent());
	}


	@Test
	void roundtripMixedMovieAndSeries() {
		Path file = tempDir.resolve("media.json");
		FileRepository<Set<TopLevelMedia>> repo =
				new FileRepository<>(file, new GsonDataSerializer());

		Movie m = new Movie(null, "Matrix", "Sci-fi", false);

		Series s = new Series("The Office", null);
		Season season2 = new Season(2, "Season 2");
		season2.addChild(new Episode(1, "Ep1"));
		s.addChild(season2);

		Movie m2 = new Movie(null, "Matrix2", "2222", false);
		Set<TopLevelMedia> data = new HashSet<>();
		data.add(m);
		data.add(s);
		data.add(m2);


		repo.save(data);
		Set<TopLevelMedia> loaded = repo.load();

		assertNotNull(loaded);
		assertEquals(3, loaded.size());

		boolean hasMovie = loaded.stream().anyMatch(x -> x instanceof Movie);
		boolean hasSeries = loaded.stream().anyMatch(x -> x instanceof Series);
		assertTrue(hasMovie);
		assertTrue(hasSeries);
	}

}
