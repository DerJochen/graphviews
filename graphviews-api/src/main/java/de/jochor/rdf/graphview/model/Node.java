package de.jochor.rdf.graphview.model;

import java.util.List;
import java.util.Map;

/**
 * Representation of a node.
 *
 * <p>
 * <b>Started:</b> 2016-08-02
 * </p>
 *
 * @author Jochen Hormes
 *
 */
public interface Node {

	String getName();

	String getLabel();

	String getColor();

	Map<String, List<String>> getAttributes();

}
