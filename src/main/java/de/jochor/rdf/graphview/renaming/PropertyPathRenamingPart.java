package de.jochor.rdf.graphview.renaming;

import java.util.ArrayList;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;

public class PropertyPathRenamingPart implements RenamingPart {

	private final ArrayList<Property> propertyPath = new ArrayList<>();

	public PropertyPathRenamingPart(Resource propertyPathResource) {
		if (propertyPathResource.hasProperty(RDF.first)) {
			addNextElement(propertyPathResource);
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

	private void addNextElement(Resource currentElement) {
		Resource currentEntry = currentElement.getPropertyResourceValue(RDF.first);
		if (currentEntry.isURIResource()) {
			Property pathPart = currentEntry.as(Property.class);
			propertyPath.add(pathPart);
		} else {
			throw new IllegalArgumentException("Node is not supported: " + currentEntry);
		}

		Resource nextElement = currentElement.getPropertyResourceValue(RDF.rest);
		if (!RDF.nil.equals(nextElement)) {
			addNextElement(nextElement);
		}
	}

}
