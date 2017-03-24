package de.jochor.rdf.graphviews.databind;

import java.util.Observable;
import java.util.Observer;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.vocabulary.FOAF;

public class Person implements Observer {

	private final Resource identifier;

	private final ViewModelChangeListener eventService;

	public Person(Resource identifier, ViewModelChangeListener eventService) {
		this.identifier = identifier;
		this.eventService = eventService;
	}

	public void init() {
		eventService.register(this, new SubjectsPredicate(identifier, FOAF.givenname));
		eventService.register(this, new SubjectsPredicate(identifier, FOAF.surname));
		System.out.println(toString());
	}

	@Override
	public void update(Observable o, Object arg) {
		System.out.println(toString());
	}

	@Override
	public String toString() {
		RdfValue givenNameValue = eventService.get(identifier, FOAF.givenname);
		String givenname = givenNameValue != null ? givenNameValue.getValue() : "?";
		RdfValue surnameValue = eventService.get(identifier, FOAF.surname);
		String surname = surnameValue != null ? surnameValue.getValue() : "?";
		return "My name is " + givenname + " " + surname;
	}

}
