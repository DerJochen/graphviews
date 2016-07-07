package de.jochor.rdf.graphview.modification;

import java.util.ArrayList;

import org.apache.jena.rdf.model.Resource;
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

	private ArrayList<RenamingPart> renamingPattern;

	public NodeRenaming(Resource resource) {
		super(resource);
		createRenamingPattern(resource);
	}

	@Override
	protected Matcher createMatcher(Resource matcherResource) {
		Resource subjectType = matcherResource.getPropertyResourceValue(ViewSchema.subjectType);
		Matcher matcher = new TypeMatcher(subjectType);
		return matcher;
	}

	private void createRenamingPattern(Resource resource) {
		Resource valuePattern = resource.getPropertyResourceValue(ViewSchema.valuePattern);
		renamingPattern = new ArrayList<>();
		addNextElement(valuePattern, renamingPattern);
	}

	private void addNextElement(Resource currentResource, ArrayList<RenamingPart> renamingPattern) {
		Resource currentElement = currentResource.getPropertyResourceValue(RDF.first);
		RenamingPart renamingPart;
		if (currentElement.isAnon()) {
			renamingPart = new PropertyPathRenamingPart(currentElement);
		} else if (currentElement.isLiteral()) {
			renamingPart = new LiteralRenamingPart(currentElement);
		} else {
			throw new IllegalArgumentException("Node is not supported: " + currentElement);
		}
		renamingPattern.add(renamingPart);

		Resource nextElement = currentResource.getPropertyResourceValue(RDF.rest);
		if (!RDF.nil.equals(nextElement)) {
			addNextElement(nextElement, renamingPattern);
		}
	}

	// vs:valuePattern[
	// a rdf:List;rdf:
	// first foaf:name;rdf:
	// rest rdf:nil].

	public String getNewName(Resource resource) {
		StringBuilder sb = new StringBuilder();
		for (RenamingPart renamingPart : renamingPattern) {
			sb.append(renamingPart.getNewNamePart(resource));
		}
		String newName = sb.toString();
		return newName;
	}

}
