package de.jochor.rdf.graphview;

import java.io.IOException;
import java.nio.file.Path;

import de.jochor.rdf.graphview.model.Graph;

public interface GraphViews {

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
	Graph createView(Path dataFile) throws IOException;

	/**
	 * Creates a graph view of the data with literal nodes mapped to attributes of the graph nodes.
	 *
	 * @param dataFile
	 *            Data to visualize
	 * @param collapseAttributes
	 *            TODO
	 * @param targetFolder
	 *            Target folder for the result file(s)
	 * @throws IOException
	 *             In case of problems with the dataFile or the target Folder
	 */
	Graph createView(Path dataFile, boolean collapseAttributes) throws IOException;

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
	Graph createView(Path dataFile, Path... schemaFiles) throws IOException;

}