package de.jochor.rdf.graphview.modification;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.impl.ModelCom;
import org.apache.jena.rdf.model.impl.StatementImpl;
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
		Statement objectStatement = matcherResource.getProperty(RDF.subject);

		Node subjectNode = subjectResource != null ? subjectResource.asNode() : Node.ANY;
		Node predicateNode = predicateResource != null ? predicateResource.asNode() : Node.ANY;
		Node objectNode = objectStatement != null ? objectStatement.getObject().asNode() : Node.ANY;

		Triple triple = new Triple(subjectNode, predicateNode, objectNode);
		ModelCom model = (ModelCom) matcherResource.getModel();
		Statement statement = StatementImpl.toStatement(triple, model);
		return new StatementMatcher(statement);
	}

}
