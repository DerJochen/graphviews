package de.jochor.rdf.graphview.model;

import java.util.ArrayList;
import java.util.HashMap;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Representation of an edge.
 *
 * <p>
 * <b>Started:</b> 2016-07-26
 * </p>
 *
 * @author Jochen Hormes
 *
 */
@RequiredArgsConstructor
@Getter
@Setter
public class Edge {

	private final HashMap<String, ArrayList<String>> attributes = new HashMap<>();

	private final String label;

	private final Node node1;

	private final Node node2;

}
