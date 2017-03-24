package de.jochor.rdf.graphviews.databind;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.vocabulary.FOAF;

public class ModelNotify {

	public static void main(String[] args) {
		Model model = ModelFactory.createDefaultModel();
		ViewModelChangeListener listener = new ViewModelChangeListener(model);
		model.register(listener);

		Resource personResource = model.createResource();
		model.add(personResource, FOAF.givenname, "John");
		model.add(personResource, FOAF.surname, "Doe");

		Person person = new Person(personResource, listener);
		person.init();

		listener.update(model, personResource, FOAF.givenname, "Jane");

		model.remove(personResource, FOAF.surname, model.createLiteral("Doe"));
		model.add(personResource, FOAF.surname, "Austern");

		listener.change(personResource, FOAF.surname, "Auster");
		listener.change(personResource, FOAF.surname, "Auste");
		listener.change(personResource, FOAF.surname, "Austen");
		listener.save();
	}

}
