package de.jochor.rdf.graphview.rule;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;

import lombok.Getter;

/**
 * {@link Statement} removal rule.
 *
 * <p>
 * <b>Started:</b> 2016-07-06
 * </p>
 *
 * @author Jochen Hormes
 *
 */
@Getter
public class StatementRemoval implements GraphModification {

	private Matcher matcher;

	public void modifyGraph(Model data) {
		Statement statement = ((StatementMatcher) matcher).getMatcher();
		data.remove(statement);
	}

}
