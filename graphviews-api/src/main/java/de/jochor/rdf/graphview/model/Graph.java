package de.jochor.rdf.graphview.model;

import java.util.Map;
import java.util.Set;

/**
 * Representation of a graph.
 *
 * <p>
 * <b>Started:</b> 2016-08-02
 * </p>
 *
 * @author Jochen Hormes
 *
 */
public interface Graph {

	String getName();

	Set<Graph> getGraphs();

	Set<Edge> getEdges();

	Set<Node> getNodes();

	Map<String, Node> getNameToNodeMap();

	Node useNode(String name);

}
