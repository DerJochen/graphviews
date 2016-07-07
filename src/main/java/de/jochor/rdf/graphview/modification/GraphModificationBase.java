package de.jochor.rdf.graphview.modification;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import de.jochor.rdf.graphview.matcher.Matcher;
import de.jochor.rdf.graphview.vocabulary.ViewSchema;

/**
 * Base class for all {@link GraphModification}s.
 *
 * <p>
 * <b>Started:</b> 2016-07-07
 * </p>
 *
 * @author Jochen Hormes
 *
 */
public abstract class GraphModificationBase implements GraphModification {

	private final Matcher matcher;

	public GraphModificationBase(Resource resource) {
		Resource matcherResource = resource.getPropertyResourceValue(ViewSchema.matcher);
		matcher = createMatcher(matcherResource);
	}

	/**
	 * Creates the matcher appropriate for this kind of modification.
	 *
	 * @param matcherResource
	 *            {@link Matcher} {@link Resource} of this {@link GraphModification}
	 * @return {@link Matcher} for this {@link GraphModification}
	 */
	protected abstract Matcher createMatcher(Resource matcherResource);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean handles(Statement statement) {
		boolean matches = matcher.matches(statement);
		return matches;
	}

}