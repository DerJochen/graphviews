package de.jochor.rdf.graphview.renaming;

import org.apache.jena.rdf.model.Resource;

import lombok.Getter;

/**
 * Literal {@link RenamingPart}. This means, it is a static string.
 *
 * <p>
 * <b>Started:</b> 2016-07-08
 * </p>
 *
 * @author Jochen Hormes
 *
 */
@Getter
public class LiteralRenamingPart implements RenamingPart {

	private final String newNamePart;

	public LiteralRenamingPart(Resource currentElement) {
		newNamePart = currentElement.asLiteral().toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getNewNamePart(Resource resource) {
		return newNamePart;
	}

}
