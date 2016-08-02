package de.jochor.rdf.graphview.model;

import java.util.List;
import java.util.Map;

/**
 * Representation of an edge.
 *
 * <p>
 * <b>Started:</b> 2016-08-02
 * </p>
 *
 * @author Jochen Hormes
 *
 */
public interface Edge {

	String getLabel();

	Node getNode1();

	Node getNode2();

	Map<String, List<String>> getAttributes();

}
