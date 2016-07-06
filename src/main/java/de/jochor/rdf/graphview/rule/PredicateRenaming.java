package de.jochor.rdf.graphview.rule;

import lombok.Getter;

/**
 * Predicate renaming rule.
 *
 * <p>
 * <b>Started:</b> 2016-07-06
 * </p>
 *
 * @author Jochen Hormes
 *
 */
@Getter
public class PredicateRenaming implements GraphModification {

	private Matcher matcher;

	private String namePattern;

}
