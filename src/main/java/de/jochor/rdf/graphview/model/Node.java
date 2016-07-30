package de.jochor.rdf.graphview.model;

import java.util.ArrayList;
import java.util.HashMap;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Representation of a node.
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
public class Node {

	private final HashMap<String, ArrayList<String>> attributes = new HashMap<>();

	private final String name;

	private String label;

	private String color;

}
