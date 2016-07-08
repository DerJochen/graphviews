package de.jochor.rdf.graphview.modification;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;

import de.jochor.rdf.graphview.matcher.Matcher;
import de.jochor.rdf.graphview.matcher.StatementMatcher;
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
public class StatementRemoval extends GraphModificationBase {

	public StatementRemoval(Resource resource) {
		super(resource);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Matcher createMatcher(Resource matcherResource) {
		Resource subjectResource = matcherResource.getPropertyResourceValue(RDF.subject);
		Resource predicateResource = matcherResource.getPropertyResourceValue(RDF.predicate);
		Statement objectStatement = matcherResource.getProperty(RDF.object);
		return new StatementMatcher(subjectResource, predicateResource, objectStatement, matcherResource.getModel());
	}

}
