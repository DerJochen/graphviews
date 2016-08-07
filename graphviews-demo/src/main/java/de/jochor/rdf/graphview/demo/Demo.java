package de.jochor.rdf.graphview.demo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.regex.Pattern;

import de.jochor.rdf.graphview.GraphViews;
import de.jochor.rdf.graphview.GraphViewsImpl;
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

	private static final Path rdfSyntaxIgnoreViewSchema = Paths.get(SCHEMA_BASE + "rdf-syntax-ignore.ttl").toAbsolutePath();

	private static final Path foafViewSchema = Paths.get(SCHEMA_BASE + "foaf-view-schema.ttl").toAbsolutePath();

	private static final Path exampleViewSchema1 = Paths.get(SCHEMA_BASE + "example-view-schema.ttl").toAbsolutePath();

	private static final Path exampleViewSchema2 = Paths.get(SCHEMA_BASE + "example-view-schema2.ttl").toAbsolutePath();

	private static final Pattern mapPattern = Pattern.compile("<map>.*?</map>");

	private GraphViews graphView;

	private DotExportService dotExportService;

	private Demo() {
		graphView = new GraphViewsImpl();
		dotExportService = new DotExportService();
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		Demo demo = new Demo();

		demo.createPlainView(dataFile, plainTarget);
		demo.createView(dataFile, defaultTarget);
		demo.createModifiedView(dataFile, schemaTarget, exampleViewSchema1, foafViewSchema, rdfSyntaxIgnoreViewSchema);
		demo.createModifiedView(dataFile, schemaTarget2, exampleViewSchema2, foafViewSchema, rdfSyntaxIgnoreViewSchema);
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
	 * @throws InterruptedException
	 */
	public void createPlainView(Path dataFile, Path targetFolder) throws IOException, InterruptedException {
		Graph plainView = graphView.createView(dataFile);
		dotExportService.export(plainView, targetFolder);
		createHtml(targetFolder, plainView.getName());
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
	 * @throws InterruptedException
	 */
	public void createView(Path dataFile, Path targetFolder) throws IOException, InterruptedException {
		Graph view = graphView.createView(dataFile, true);
		dotExportService.export(view, targetFolder);
		createHtml(targetFolder, view.getName());
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
	 * @throws InterruptedException
	 */
	public void createModifiedView(Path dataFile, Path targetFolder, Path... schemaFiles) throws IOException, InterruptedException {
		Graph modifiedView = graphView.createView(dataFile, schemaFiles);
		dotExportService.export(modifiedView, targetFolder);
		createHtml(targetFolder, modifiedView.getName());
	}

	private void createHtml(Path targetFolder, String name) throws IOException, InterruptedException {
		Path pngTarget = targetFolder.resolve(name + ".png");
		Path mapTarget = targetFolder.resolve(name + ".map");
		Path dotSource = targetFolder.resolve(name + ".dot");
		ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "D:\\Programme\\graphviz\\bin\\dot.exe", "-T", "png", "-o", pngTarget.toString(), "-T",
				"cmapx", "-o", mapTarget.toString(), dotSource.toString());
		Process p = builder.start();
		p.waitFor();

		byte[] allBytes = Files.readAllBytes(mapTarget);
		String mapContent = new String(allBytes, StandardCharsets.UTF_8);

		Path htmlFile = targetFolder.resolve(name + ".html");
		allBytes = Files.readAllBytes(htmlFile);
		String htmlContent = new String(allBytes, StandardCharsets.UTF_8);
		String newHtmlContent = mapPattern.matcher(htmlContent).replaceAll(mapContent);

		Files.write(htmlFile, newHtmlContent.getBytes(StandardCharsets.UTF_8), StandardOpenOption.TRUNCATE_EXISTING);
	}

}
