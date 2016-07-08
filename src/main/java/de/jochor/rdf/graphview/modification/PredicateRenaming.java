package de.jochor.rdf.graphview.modification;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;

import de.jochor.rdf.graphview.matcher.Matcher;
import de.jochor.rdf.graphview.matcher.StatementMatcher;
import de.jochor.rdf.graphview.vocabulary.ViewSchema;
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
public class PredicateRenaming extends GraphModificationBase {

	private int priority;

	private String value;

	public PredicateRenaming(Resource resource) {
		super(resource);
		priority = resource.getRequiredProperty(ViewSchema.priority).getInt();
		// TODO may be also property from original ontology (e.g. prefLabel)
		value = resource.getRequiredProperty(ViewSchema.value).getString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Matcher createMatcher(Resource matcherResource) {
		Statement predicateStatement = matcherResource.getProperty(RDF.predicate);
		Property predicateResource = predicateStatement.getObject().as(Property.class);
		Matcher matcher = new StatementMatcher(null, predicateResource, null, matcherResource.getModel());
		return matcher;
	}

}
