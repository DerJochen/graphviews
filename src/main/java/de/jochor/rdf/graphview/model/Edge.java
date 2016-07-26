package de.jochor.rdf.graphview.model;

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

	private Node node1;

	private Node node2;

	private String label;

}
