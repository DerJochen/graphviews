package de.jochor.rdf.graphview.demo;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import de.jochor.rdf.graphview.GraphViews;
import de.jochor.rdf.graphview.model.Graph;
import de.jochor.rdf.graphview.view.dot.DotExportService;

/**
 * Demo class for the dot export.
 *
 * <p>
 * <b>Started:</b> 2016-08-02
 * </p>
 *
 * @author Jochen Hormes
 *
 */
public class Demo {

	private static final String SCHEMA_BASE = "src/main/resources/";

	private static final Path dataFile = Paths.get("src/main/resources/demo.ttl");

	private static final Path defaultTarget = Paths.get("target/default");

	private static final Path plainTarget = Paths.get("target/plain");

	private static final Path schemaTarget = Paths.get("target/withSchema");

	private static final Path schemaTarget2 = Paths.get("target/withSchema2");

	private GraphViews graphView;

	private DotExportService dotExportService;

	public static void main(String[] args) throws IOException {
		Demo demo = new Demo();

		demo.createPlainView(dataFile, plainTarget);
		demo.createView(dataFile, defaultTarget);
		demo.createModifiedView(dataFile, schemaTarget, Paths.get(SCHEMA_BASE + "example-view-schema.ttl"), Paths.get(SCHEMA_BASE + "foaf-view-schema.ttl"),
				Paths.get(SCHEMA_BASE + "rdf-syntax-ignore.ttl"));
		demo.createModifiedView(dataFile, schemaTarget2, Paths.get(SCHEMA_BASE + "example-view-schema2.ttl"), Paths.get(SCHEMA_BASE + "foaf-view-schema.ttl"),
				Paths.get(SCHEMA_BASE + "rdf-syntax-ignore.ttl"));
	}

	private Demo() {
		graphView = new GraphViews();
		dotExportService = new DotExportService();
	}

	/**
	 * Creates a plain view of the data without any graph modifications.
	 *
	 * @param dataFile
	 *            Data to visualize
	 * @param targetFolder
	 *            Target folder for the result file(s)
	 * @throws IOException
	 *             In case of problems with the dataFile or the target Folder
	 */
	public void createPlainView(Path dataFile, Path targetFolder) throws IOException {
		Graph plainView = graphView.createPlainView(dataFile);
		dotExportService.export(plainView, targetFolder);
	}

	/**
	 * Creates a graph view of the data with literal nodes mapped to attributes of the graph nodes.
	 *
	 * @param dataFile
	 *            Data to visualize
	 * @param targetFolder
	 *            Target folder for the result file(s)
	 * @throws IOException
	 *             In case of problems with the dataFile or the target Folder
	 */
	public void createView(Path dataFile, Path targetFolder) throws IOException {
		Graph view = graphView.createView(dataFile);
		dotExportService.export(view, targetFolder);
	}

	/**
	 * Creates a graph view of the data with literal nodes mapped to attributes of the graph nodes and view schema files
	 * applied.
	 *
	 * @param dataFile
	 *            Data to visualize
	 * @param targetFolder
	 *            Target folder for the result file(s)
	 * @param schemaFiles
	 *            Schema files to apply to the data
	 * @throws IOException
	 *             In case of problems with the dataFile or the target Folder
	 */
	public void createModifiedView(Path dataFile, Path targetFolder, Path... schemaFiles) throws IOException {
		Graph modifiedView = graphView.createModifiedView(dataFile);
		dotExportService.export(modifiedView, targetFolder);
	}

}
