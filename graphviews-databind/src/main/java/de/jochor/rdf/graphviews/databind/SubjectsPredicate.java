package de.jochor.rdf.graphviews.databind;

import java.util.Observable;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode(of = { "subject", "predicate" }, callSuper = false)
public class SubjectsPredicate extends Observable {

	private final Resource subject;

	private final Property predicate;

}
