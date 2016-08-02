package de.jochor.rdf.graphview.model;

import java.util.HashMap;
import java.util.List;

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
public class EdgeImpl implements Edge {

	private final HashMap<String, List<String>> attributes = new HashMap<>();

	private final String label;

	private final NodeImpl node1;

	private final NodeImpl node2;

}
