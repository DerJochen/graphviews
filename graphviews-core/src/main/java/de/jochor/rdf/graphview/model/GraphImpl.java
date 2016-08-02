package de.jochor.rdf.graphview.model;

import java.util.HashMap;
import java.util.HashSet;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Representation of a graph.
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
public class GraphImpl implements Graph {

	private final HashSet<Graph> graphs = new HashSet<>();

	private final HashMap<String, Node> nameToNodeMap = new HashMap<>();

	private final HashSet<Edge> edges = new HashSet<>();

	private final String name;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NodeImpl useNode(String name) {
		NodeImpl node = (NodeImpl) nameToNodeMap.get(name);
		if (node == null) {
			node = new NodeImpl(name);
			nameToNodeMap.put(name, node);
		}
		return node;
	}

}
