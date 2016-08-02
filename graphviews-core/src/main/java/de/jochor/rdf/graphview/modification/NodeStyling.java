package de.jochor.rdf.graphview.modification;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;

import de.jochor.rdf.graphview.matcher.Matcher;
import de.jochor.rdf.graphview.matcher.StatementMatcher;
import de.jochor.rdf.graphview.vocabulary.ViewSchema;
import lombok.Getter;

/**
 * Node styling rule.
 *
 * <p>
 * <b>Started:</b> 2016-07-26
 * </p>
 *
 * @author Jochen Hormes
 *
 */
@Getter
public class NodeStyling extends GraphModificationBase implements GraphModification {

	private String color;

	public NodeStyling(Resource resource) {
		super(resource);

		createStyling(resource);
	}

	@Override
	protected Matcher createMatcher(Resource matcherResource) {
		Resource predicate = matcherResource.getPropertyResourceValue(RDF.predicate);
		Statement objectStatement = matcherResource.getProperty(RDF.object);
		Matcher matcher = new StatementMatcher(null, predicate, objectStatement, matcherResource.getModel());
		return matcher;
	}

	private void createStyling(Resource resource) {
		Statement colorStatement = resource.getProperty(ViewSchema.color);
		if (colorStatement != null && colorStatement.getObject().isLiteral()) {
			color = colorStatement.getString();
		}
	}

}
