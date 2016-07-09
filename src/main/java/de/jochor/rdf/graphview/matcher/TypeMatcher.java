package de.jochor.rdf.graphview.matcher;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * {@link Statement} based {@link Matcher} implementation.
 *
 * <p>
 * <b>Started:</b> 2016-07-07
 * </p>
 *
 * @author Jochen Hormes
 *
 */
@RequiredArgsConstructor
@Getter
public class TypeMatcher implements Matcher {

	private final Resource type;

	private final boolean checkSubject;

	private final boolean checkObject;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean matches(Statement statement) {
		boolean matches = false;
		if (checkSubject) {
			matches |= statement.getSubject().hasProperty(RDF.type, type);
		}
		RDFNode object = statement.getObject();
		if (checkObject && object.isURIResource()) {
			matches |= object.asResource().hasProperty(RDF.type, type);
		}
		return matches;
	}

}
