package de.unistuttgart.einf.moviemanager.cli.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ComponentHierarchyTest {

	static class TestComponent extends ComponentBase {
		@Override
		protected void drawContent(Painter painter) {

		}
	}

	static class TestParent extends Parent {
		@Override
		public void layoutChildren() {

		}
	}

	private Scene scene;
	private Parent rootParent;
	private Component child;

	@BeforeEach
	void setup() {
		scene = new Scene();
		rootParent = new TestParent();
		child = new TestComponent();
	}

	@Nested
	class ParentChildRelation {

		@Test
		@DisplayName("addChild should establish bidirectional link")
		void testAddChildSuccess() {
			rootParent.addChild(child);

			assertEquals(rootParent, child.getParent(), "Child should know its parent");
			assertTrue(rootParent.getChildren().contains(child), "Parent should know its child");
		}

		@Test
		@DisplayName("removeChild should clear bidirectional link")
		void testRemoveChildSuccess() {
			rootParent.addChild(child);
			rootParent.removeChild(child);

			assertNull(child.getParent(), "Child should be orphaned");
			assertFalse(rootParent.getChildren().contains(child), "Parent should release child");
		}

		@Test
		@DisplayName("Setting parent directly should throw IllegalStateException")
		void testDirectSetParentThrows() {
			assertThrows(IllegalStateException.class, () -> {
				child.setParent(rootParent);
			});
		}

		@Test
		@DisplayName("Detaching parent directly should throw IllegalStateException")
		void testDirectSetParentNullThrows() {
			rootParent.addChild(child);

			assertThrows(IllegalStateException.class, () -> {
				child.setParent(null);
			});
		}

		@Test
		@DisplayName("Stealing a child from another parent should throw IllegalStateException")
		void testStealChildThrows() {
			final TestParent otherParent = new TestParent();
			otherParent.addChild(child);

			assertThrows(IllegalStateException.class, () -> {
				rootParent.addChild(child);
			});
		}

	}

	@Nested
	class ScenePropagation {

		@Test
		@DisplayName("Scene should propagate down to existing children")
		void testScenePropagatesDown() {
			final TestParent subParent = new TestParent();
			rootParent.addChild(subParent);
			subParent.addChild(child);

			scene.setRoot(rootParent);

			assertEquals(scene, rootParent.getScene());
			assertEquals(scene, subParent.getScene());
			assertEquals(scene, child.getScene());
		}

		@Test
		@DisplayName("Child should pick up scene when added to attached parent")
		void testScenePickupOnAdd() {
			scene.setRoot(rootParent);

			rootParent.addChild(child);

			assertEquals(scene, child.getScene(), "Child should inherit scene");
		}

		@Test
		@DisplayName("Directly changing scene should throw IllegalStateException")
		void testChildCannotDivergeScene() {
			scene.setRoot(rootParent);
			rootParent.addChild(child);
			final Scene otherScene = new Scene();

			assertThrows(IllegalStateException.class, () -> {
				child.setScene(otherScene);
			});
		}

		@Test
		@DisplayName("Changing scene root should update both old and new")
		void testSceneRootSwap() {
			scene.setRoot(rootParent);
			final TestParent newRoot = new TestParent();

			scene.setRoot(newRoot);

			assertNull(rootParent.getScene(), "Old root should be detached");
			assertEquals(scene, newRoot.getScene(), "New root should be attached");
			assertEquals(newRoot, scene.getRoot());
		}

	}

}
