package de.jochor.rdf.graphview;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;

public class GraphViewTest {

	private static final String SCHEMA_BASE = "src/test/resources/";

	private static final Path dataFile = Paths.get("src/test/resources/demo.ttl");

	private static final Path defaultTarget = Paths.get("target/default");

	private static final Path plainTarget = Paths.get("target/plain");

	private static final Path schemaTarget = Paths.get("target/withSchema");

	private static final Path schemaTarget2 = Paths.get("target/withSchema2");

	private GraphViews graphView;

	@Before
	public void setUp() throws IOException {
		graphView = new GraphViews();
	}

	@Test
	public void testPlainView() throws IOException {
		graphView.createPlainView(dataFile);

		// TODO check Result
	}

	@Test
	public void testDefaultView() throws IOException {
		graphView.createView(dataFile);

		// TODO check Result
	}

	@Test
	public void testModifiedView() throws IOException {
		graphView.createModifiedView(dataFile, Paths.get(SCHEMA_BASE + "example-view-schema.ttl"), Paths.get(SCHEMA_BASE + "foaf-view-schema.ttl"),
				Paths.get(SCHEMA_BASE + "rdf-syntax-ignore.ttl"));

		// TODO check Result
	}

	@Test
	public void testModifiedView2() throws IOException {
		graphView.createModifiedView(dataFile, Paths.get(SCHEMA_BASE + "example-view-schema2.ttl"), Paths.get(SCHEMA_BASE + "foaf-view-schema.ttl"),
				Paths.get(SCHEMA_BASE + "rdf-syntax-ignore.ttl"));

		// TODO check Result
	}

}
