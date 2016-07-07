package de.jochor.rdf.graphview.renaming;

import org.apache.jena.rdf.model.Resource;

/**
 * Part of a renaming pattern.
 *
 * <p>
 * <b>Started:</b> 2016-07-07
 * </p>
 *
 * @author Jochen Hormes
 *
 */
public interface RenamingPart {

	/**
	 * Creates and returns a part of the new name.
	 *
	 * @param resource
	 *            Main {@link Resource} to create the name for
	 * @return Part of the new name
	 */
	String getNewNamePart(Resource resource);

}
