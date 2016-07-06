package de.jochor.rdf.graphview.rule;

import org.apache.jena.rdf.model.Statement;

/**
 * Interface for all {@link Statement} matcher classes.
 *
 * <p>
 * <b>Started:</b> 2016-07-06
 * </p>
 *
 * @author Jochen Hormes
 *
 */
public interface Matcher {

	boolean matches(Statement statement);

}
