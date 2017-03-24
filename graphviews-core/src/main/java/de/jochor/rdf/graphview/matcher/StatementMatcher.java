package de.jochor.rdf.graphview.matcher;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.impl.ModelCom;
import org.apache.jena.rdf.model.impl.StatementImpl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

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
@RequiredArgsConstructor
@Getter
public class StatementMatcher implements Matcher {

	private final Statement statementPattern;

	public StatementMatcher(Resource subject, Resource predicate, Statement objectStatement, Model model) {
		Node subjectNode = subject != null ? subject.asNode() : Node.ANY;
		Node predicateNode = predicate != null ? predicate.asNode() : Node.ANY;
		Node objectNode = objectStatement != null ? objectStatement.getObject().asNode() : Node.ANY;

		Triple triple = new Triple(subjectNode, predicateNode, objectNode);
		ModelCom modelCom = (ModelCom) model;
		statementPattern = StatementImpl.toStatement(triple, modelCom);
	}

	@Override
	public boolean matches(Statement statement) {
		boolean matches = true;

		Resource patternSubject = statementPattern.getSubject();
		matches &= patternSubject.asNode().equals(Node.ANY) || patternSubject.equals(statement.getSubject());
		Property patternPredicate = statementPattern.getPredicate();
		matches &= patternPredicate.asNode().equals(Node.ANY) || patternPredicate.equals(statement.getPredicate());
		RDFNode patternObject = statementPattern.getObject();
		matches &= patternObject.asNode().equals(Node.ANY) || patternObject.equals(statement.getObject());

		return matches;
	}

}
