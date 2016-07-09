package de.jochor.rdf.graphview.vocabulary;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class ViewSchema {

	/**
	 * The namespace of the vocabulary as a string
	 */
	public static final String uri = "http://www.jochor.de/view-schema/0.1/";

	/**
	 * returns the URI for this schema
	 *
	 * @return the URI for this schema
	 */
	public static String getURI() {
		return uri;
	}

	protected static final Resource resource(String local) {
		return ResourceFactory.createResource(uri + local);
	}

	protected static final Property property(String local) {
		return ResourceFactory.createProperty(uri, local);
	}

	public static final Resource GraphModification = resource("GraphModification");
	public static final Resource NodeRenaming = resource("NodeRenaming");
	public static final Resource PredicateRenaming = resource("PredicateRenaming");
	public static final Resource StatementRemoval = resource("StatementRemoval");

	public static final Property matcher = property("matcher");
	public static final Property objectType = property("objectType");
	public static final Property priority = property("priority");
	public static final Property subjectType = property("subjectType");
	public static final Property value = property("value");
	public static final Property valuePattern = property("valuePattern");

}
