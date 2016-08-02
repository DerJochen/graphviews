package de.jochor.rdf.graphview.modification;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;

import de.jochor.rdf.graphview.matcher.Matcher;
import de.jochor.rdf.graphview.matcher.StatementMatcher;
import de.jochor.rdf.graphview.matcher.TypeMatcher;
import de.jochor.rdf.graphview.vocabulary.ViewSchema;
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
		// TODO add support for subject and object types
		Resource subjectResource = matcherResource.getPropertyResourceValue(RDF.subject);
		Resource predicateResource = matcherResource.getPropertyResourceValue(RDF.predicate);
		Statement objectStatement = matcherResource.getProperty(RDF.object);
		if (subjectResource != null || predicateResource != null || objectStatement != null) {
			return new StatementMatcher(subjectResource, predicateResource, objectStatement, matcherResource.getModel());
		}

		Resource subjectTypeResource = matcherResource.getPropertyResourceValue(ViewSchema.subjectType);
		if (subjectTypeResource != null) {
			return new TypeMatcher(subjectTypeResource, true, false);
		}

		Resource objectTypeResource = matcherResource.getPropertyResourceValue(ViewSchema.objectType);
		if (objectTypeResource != null) {
			return new TypeMatcher(objectTypeResource, false, true);
		}

		throw new UnsupportedOperationException();
	}

}
