package de.jochor.rdf.graphview.view.dot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Stack;

import de.jochor.rdf.graphview.model.Edge;
import de.jochor.rdf.graphview.model.Graph;
import de.jochor.rdf.graphview.model.Node;

/**
 * Service that exports a graph to a dot file.
 *
 * <p>
 * <b>Started:</b> 2016-07-26
 * </p>
 *
 * @author Jochen Hormes
 *
 */
public class DotExportService {

	/**
	 * Exports a graph to a dot file.
	 *
	 * @param graph
	 *            {@link Graph} to export
	 * @param targetFolder
	 *            Target folder
	 * @throws IOException
	 */
	public void export(Graph graph, Path targetFolder) throws IOException {
		Files.createDirectories(targetFolder);
		writeGraph(graph, new Stack<String>(), targetFolder);
	}

	private void writeGraph(Graph graph, Stack<String> stack, Path targetFolder) throws IOException {
		String name = graph.getName();
		stack.push(name);

		info.leadinglight.jdot.Graph dotGraph = new info.leadinglight.jdot.Graph();

		HashSet<Node> nodesToAdd = new HashSet<>();

		HashSet<Edge> edges = graph.getEdges();
		for (Edge edge : edges) {
			;
			// TODO add graph elements to dot graph
		}

		// TODO add graph elements to dot graph

		Path filePath = createFilePath(targetFolder, stack);
		String dotString = dotGraph.toDot();
		Files.write(filePath, dotString.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

		HashSet<Graph> graphs = graph.getGraphs();
		for (Graph subGraph : graphs) {
			writeGraph(subGraph, stack, targetFolder);
		}

		stack.pop();
	}

	private Path createFilePath(Path targetFolder, Stack<String> stack) {
		StringBuilder sb = new StringBuilder();
		for (String name : stack) {
			sb.append(name).append(" - ");
		}
		sb.setLength(sb.length() - 3);
		sb.append(".dot");
		String fileName = sb.toString();

		Path filePath = targetFolder.resolve(fileName);

		return filePath;
	}

}
