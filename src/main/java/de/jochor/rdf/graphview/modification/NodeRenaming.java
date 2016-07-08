package de.jochor.rdf.graphview.modification;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;

import de.jochor.rdf.graphview.matcher.Matcher;
import de.jochor.rdf.graphview.matcher.TypeMatcher;
import de.jochor.rdf.graphview.renaming.LiteralRenamingPart;
import de.jochor.rdf.graphview.renaming.PropertyPathRenamingPart;
import de.jochor.rdf.graphview.renaming.RenamingPart;
import de.jochor.rdf.graphview.vocabulary.ViewSchema;

/**
 * Predicate renaming rule.
 *
 * <p>
 * <b>Started:</b> 2016-07-07
 * </p>
 *
 * @author Jochen Hormes
 *
 */
public class NodeRenaming extends GraphModificationBase {

	private final ArrayList<RenamingPart> renamingPattern = new ArrayList<>();

	private final HashMap<Resource, String> alreadyResolvedNames = new HashMap<>();

	public NodeRenaming(Resource resource) {
		super(resource);

		createRenamingPattern(resource);
	}

	@Override
	protected Matcher createMatcher(Resource matcherResource) {
		Resource subjectType = matcherResource.getPropertyResourceValue(ViewSchema.subjectType);
		Matcher matcher = new TypeMatcher(subjectType, true);
		return matcher;
	}

	private void createRenamingPattern(Resource resource) {
		Resource valuePattern = resource.getPropertyResourceValue(ViewSchema.valuePattern);
		if (valuePattern.hasProperty(RDF.type, RDF.List)) {
			addNextElement(valuePattern, renamingPattern);
		} else {
			RenamingPart renamingPart = createRenamingPart(valuePattern);
			renamingPattern.add(renamingPart);
		}
	}

	private void addNextElement(Resource currentResource, ArrayList<RenamingPart> renamingPattern) {
		Statement elementStatement = currentResource.getProperty(RDF.first);
		RDFNode currentElement = elementStatement.getObject();
		RenamingPart renamingPart = createRenamingPart(currentElement);
		renamingPattern.add(renamingPart);

		Resource nextElement = currentResource.getPropertyResourceValue(RDF.rest);
		if (!RDF.nil.equals(nextElement)) {
			addNextElement(nextElement, renamingPattern);
		}
	}

	private RenamingPart createRenamingPart(RDFNode currentElement) {
		RenamingPart renamingPart;
		if (currentElement.isURIResource() || currentElement.isAnon()) {
			renamingPart = new PropertyPathRenamingPart(currentElement.asResource());
		} else if (currentElement.isLiteral()) {
			renamingPart = new LiteralRenamingPart(currentElement);
		} else {
			throw new IllegalArgumentException("Node is not supported: " + currentElement);
		}
		return renamingPart;
	}

	public String getNewName(Resource resource) {
		String newName = alreadyResolvedNames.get(resource);
		if (newName == null) {
			StringBuilder sb = new StringBuilder();
			for (RenamingPart renamingPart : renamingPattern) {
				String newNamePart = renamingPart.getNewNamePart(resource);
				if (newNamePart == null) {
					return null;
				}
				sb.append(newNamePart);
			}
			newName = sb.toString();
		}
		return newName;
	}

	// TODO Add canHandle(Resource) that checks all propertyPaths

}
