package de.jochor.rdf.graphview.rule;

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

	Matcher getMatcher();

}
