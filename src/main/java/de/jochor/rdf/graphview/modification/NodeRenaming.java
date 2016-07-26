package de.jochor.rdf.graphview.modification;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.RDFListImpl;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDF;

import de.jochor.rdf.graphview.matcher.Matcher;
import de.jochor.rdf.graphview.matcher.TypeMatcher;
import de.jochor.rdf.graphview.renaming.LiteralRenamingPart;
import de.jochor.rdf.graphview.renaming.PropertyPathRenamingPart;
import de.jochor.rdf.graphview.renaming.RenamingPart;
import de.jochor.rdf.graphview.vocabulary.ViewSchema;

/**
 * Node renaming rule.
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
		Matcher matcher = new TypeMatcher(subjectType, true, true);
		return matcher;
	}

	private void createRenamingPattern(Resource resource) {
		Resource valuePattern = resource.getPropertyResourceValue(ViewSchema.valuePattern);
		if (valuePattern.hasProperty(RDF.first)) {
			RDFList list = new RDFListImpl(valuePattern.asNode(), (EnhGraph) valuePattern.getModel());
			ExtendedIterator<RDFNode> iter = list.iterator();
			while (iter.hasNext()) {
				RDFNode currentElement = iter.next();
				RenamingPart renamingPart = createRenamingPart(currentElement);
				renamingPattern.add(renamingPart);
			}
		} else {
			RenamingPart renamingPart = createRenamingPart(valuePattern);
			renamingPattern.add(renamingPart);
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
		if (!handlesResource(resource)) {
			// return null; TODO
		}

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

	// TODO
	private boolean handlesResource(Resource resource) {
		// Statement tempStatement = new StatementImpl(resource, null, null);
		// boolean matches = matcher.matches(tempStatement);
		// return matches;
		return true;
	}

	// TODO Add canHandle(Resource) that checks all propertyPaths

}
