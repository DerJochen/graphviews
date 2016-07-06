package de.jochor.rdf.graphview.rule;

import org.apache.jena.rdf.model.Statement;

import lombok.Getter;

/**
 * {@link Statement} based {@link Matcher} implementation.
 *
 * <p>
 * <b>Started:</b> 2016-07-06
 * </p>
 *
 * @author Jochen Hormes
 *
 */
@Getter
public class StatementMatcher implements Matcher {

	private Statement matcher;

	@Override
	public boolean matches(Statement statement) {
		boolean matches = matcher.equals(statement);
		return matches;
	}

}
