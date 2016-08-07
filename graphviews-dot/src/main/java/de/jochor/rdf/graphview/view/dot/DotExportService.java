package de.jochor.rdf.graphview.view.dot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.jochor.rdf.graphview.model.Edge;
import de.jochor.rdf.graphview.model.Graph;
import de.jochor.rdf.graphview.model.Node;
import info.leadinglight.jdot.enums.Style;

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
		writeGraph(graph, new ArrayDeque<String>(), targetFolder);
	}

	private void writeGraph(Graph graph, Deque<String> stack, Path targetFolder) throws IOException {
		String graphName = graph.getName();
		stack.push(graphName);

		info.leadinglight.jdot.Graph dotGraph = new info.leadinglight.jdot.Graph();

		HashSet<Node> nodesToAdd = new HashSet<>(graph.getNodes());

		Set<Edge> edges = graph.getEdges();
		for (Edge edge : edges) {
			String label = edge.getLabel();
			Node node1 = edge.getNode1();
			Node node2 = edge.getNode2();

			info.leadinglight.jdot.Edge dotEdge = new info.leadinglight.jdot.Edge(node1.getName(), node2.getName());
			dotEdge.setLabel(label);

			Map<String, List<String>> attributes = edge.getAttributes();
			if (attributes != null) {
				addToolTips(dotEdge, attributes);
			}

			dotGraph.addEdge(dotEdge);

			nodesToAdd.add(node1);
			nodesToAdd.add(node2);
		}
		for (Node node : nodesToAdd) {
			String name = node.getName();
			String label = node.getLabel();

			info.leadinglight.jdot.Node dotNode = new info.leadinglight.jdot.Node(name);
			dotNode.setLabel(label);
			if (node.getColor() != null) {
				dotNode.setFillColor(node.getColor());
				dotNode.setStyle(Style.Node.filled);
			}

			Map<String, List<String>> attributes = node.getAttributes();
			if (attributes != null) {
				addToolTips(dotNode, attributes);
			}

			dotGraph.addNode(dotNode);
		}

		Path filePath = createFilePath(targetFolder, stack);
		String dotString = dotGraph.toDot();
		Files.write(filePath, dotString.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

		Set<Graph> graphs = graph.getGraphs();
		for (Graph subGraph : graphs) {
			writeGraph(subGraph, stack, targetFolder);
		}

		stack.pop();
	}

	private void addToolTips(info.leadinglight.jdot.Node dotNode, Map<String, List<String>> attributes) {
		String toolTip = createToolTips(attributes);
		dotNode.setToolTip(toolTip);
	}

	private void addToolTips(info.leadinglight.jdot.Edge dotEdge, Map<String, List<String>> attributes) {
		String toolTip = createToolTips(attributes);
		dotEdge.setToolTip(toolTip);
	}

	private String createToolTips(Map<String, List<String>> attributes) {
		StringBuilder sb = new StringBuilder();
		String nl = "";
		Iterator<Entry<String, List<String>>> iter2 = attributes.entrySet().iterator();
		while (iter2.hasNext()) {
			Entry<String, List<String>> entry2 = iter2.next();
			String attributeName = entry2.getKey();
			List<String> values = entry2.getValue();
			String value = String.join(", ", values);
			sb.append(nl).append(attributeName).append(": ").append(value);
			nl = System.lineSeparator();
		}
		String toolTip = sb.toString();
		return toolTip;
	}

	private Path createFilePath(Path targetFolder, Deque<String> stack) {
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
