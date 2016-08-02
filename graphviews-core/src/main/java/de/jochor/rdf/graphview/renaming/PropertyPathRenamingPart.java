package de.jochor.rdf.graphview.renaming;

import java.util.ArrayList;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.impl.RDFListImpl;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDF;

public class PropertyPathRenamingPart implements RenamingPart {

	private final ArrayList<Property> propertyPath = new ArrayList<>();

	public PropertyPathRenamingPart(Resource propertyPathResource) {
		if (propertyPathResource.hasProperty(RDF.first)) {
			RDFList list = new RDFListImpl(propertyPathResource.asNode(), (EnhGraph) propertyPathResource.getModel());
			ExtendedIterator<RDFNode> iter = list.iterator();
			while (iter.hasNext()) {
				RDFNode currentElement = iter.next();
				if (currentElement.isURIResource()) {
					Property pathPart = currentElement.as(Property.class);
					propertyPath.add(pathPart);
				} else {
					throw new IllegalArgumentException("Node is not supported: " + currentElement);
				}
			}
		} else {
			if (propertyPathResource.isURIResource() && propertyPathResource.canAs(Property.class)) {
				Property pathPart = propertyPathResource.as(Property.class);
				propertyPath.add(pathPart);
			} else {
				throw new IllegalArgumentException("Node is not supported: " + propertyPathResource);
			}
		}
	}

	@Override
	public String getNewNamePart(Resource resource) {
		Resource currentResource = resource;
		for (int i = 0; i < propertyPath.size() - 1; i++) {
			Property pathPart = propertyPath.get(i);
			Statement pathStatement = currentResource.getProperty(pathPart);
			if (pathStatement == null) {
				return null;
			}
			currentResource = pathStatement.getResource();
		}
		Property lastPathPart = propertyPath.get(propertyPath.size() - 1);
		Statement literalStatement = currentResource.getProperty(lastPathPart);
		if (literalStatement == null) {
			return null;
		}
		String newNamePart = literalStatement.getString();
		return newNamePart;
	}

}
