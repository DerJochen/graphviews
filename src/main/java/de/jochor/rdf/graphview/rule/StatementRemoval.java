package de.jochor.rdf.graphview.rule;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.impl.ModelCom;
import org.apache.jena.rdf.model.impl.StatementImpl;
import org.apache.jena.vocabulary.RDF;

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
public class StatementRemoval implements GraphModification {

	private Matcher matcher;

	Node subjectNode;
	Node predicateNode;
	Node objectNode;

	public StatementRemoval(Resource resource, ModelCom model) {
		Resource matcherBlankNode = resource.getPropertyResourceValue(ViewSchema.matcher);
		Resource subjectResource = matcherBlankNode.getPropertyResourceValue(RDF.subject);
		Resource predicateResource = matcherBlankNode.getPropertyResourceValue(RDF.predicate);
		Statement objectStatement = matcherBlankNode.getProperty(RDF.subject);

		subjectNode = subjectResource != null ? subjectResource.asNode() : Node.ANY;
		predicateNode = predicateResource != null ? predicateResource.asNode() : Node.ANY;
		objectNode = objectStatement != null ? objectStatement.getObject().asNode() : Node.ANY;

		Triple triple = new Triple(subjectNode, predicateNode, objectNode);
		Statement statement = StatementImpl.toStatement(triple, model);
		matcher = new StatementMatcher(statement);
	}

	public void modifyGraph(Model data) {
		// Statement statement = ((StatementMatcher) matcher).getMatcher();
		// data.remove(statement);
		data.getGraph().remove(subjectNode, predicateNode, objectNode);
	}

}
