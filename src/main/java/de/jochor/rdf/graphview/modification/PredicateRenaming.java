package de.jochor.rdf.graphview.modification;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import de.jochor.rdf.graphview.matcher.Matcher;
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
		// TODO Auto-generated method stub
		return new Matcher() {

			@Override
			public boolean matches(Statement statement) {
				return false;
			}
		};
	}

}

// :AddressStreet a vs:PredicateRenaming ;
// vs:matcher [
// vs:subjectType ex:Address
// rdf:predicate ex:street
// ] ;
// vs:priority 1
// vs:value "Street" .
