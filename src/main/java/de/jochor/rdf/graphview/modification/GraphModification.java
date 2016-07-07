package de.jochor.rdf.graphview.modification;

import org.apache.jena.rdf.model.Statement;

/**
 * Interface for all graph modifying rules.
 *
 * <p>
 * <b>Started:</b> 2016-07-06
 * </p>
 *
 * @author Jochen Hormes
 *
 */
public interface GraphModification {

	/**
	 * Checks if this modification applies to the given {@link Statement}
	 *
	 * @param statement
	 *            {@link Statement} to check against
	 * @return True if this {@link GraphModification} handles this {@link Statement}, false otherwise
	 */
	boolean handles(Statement statement);

}
