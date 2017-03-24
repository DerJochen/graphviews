package de.jochor.rdf.graphviews.databind;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Observer;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.jena.rdf.listeners.StatementListener;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.impl.StatementImpl;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ViewModelChangeListener extends StatementListener {

	private final HashMap<SubjectsPredicate, HashSet<Observer>> spToObserverMap = new HashMap<>();

	private final HashMap<SubjectsPredicate, RdfValue> spToValueMap = new HashMap<>();

	private final ConcurrentHashMap<SubjectsPredicate, SubjectsPredicate> updating = new ConcurrentHashMap<>();

	private final ConcurrentHashMap<SubjectsPredicate, RdfValue> spToEditedValues = new ConcurrentHashMap<>();

	private final Model model;

	@Override
	public void addedStatement(Statement statement) {
		System.out.println("Added: " + statement);

		SubjectsPredicate sp = new SubjectsPredicate(statement.getSubject(), statement.getPredicate());
		if (updating.containsKey(sp)) {
			return;
		}

		RdfValue rdfValue;
		RDFNode object = statement.getObject();
		if (object.isLiteral()) {
			Literal literal = object.asLiteral();

			rdfValue = new RdfValue(literal);
		} else {
			throw new UnsupportedOperationException("Not yet implemented");
		}
		spToValueMap.put(sp, rdfValue);

		doNotify(statement);
	}

	@Override
	public void removedStatement(Statement statement) {
		System.out.println("Removed: " + statement);

		SubjectsPredicate sp = new SubjectsPredicate(statement.getSubject(), statement.getPredicate());
		if (updating.containsKey(sp)) {
			return;
		}

		spToValueMap.remove(sp);
		doNotify(statement);
	}

	public void register(Observer observer, SubjectsPredicate sp) {
		HashSet<Observer> observers = spToObserverMap.get(sp);
		if (observers == null) {
			observers = new HashSet<>();
			spToObserverMap.put(sp, observers);
		}
		observers.add(observer);
	}

	public void change(Resource subject, Property predicate, String newValue) {
		SubjectsPredicate sp = new SubjectsPredicate(subject, predicate);
		RdfValue rdfValue = spToEditedValues.get(sp);
		if (rdfValue == null) {
			rdfValue = spToValueMap.get(sp);
			spToEditedValues.put(sp, rdfValue);
		}
		rdfValue.setValue(newValue);

		Literal object = rdfValue.toRDFNode(model);
		Statement statement = new StatementImpl(subject, predicate, object);
		doNotify(statement);
	}

	public void save() {
		Set<Entry<SubjectsPredicate, RdfValue>> entrySet = spToEditedValues.entrySet();
		Iterator<Entry<SubjectsPredicate, RdfValue>> iter = entrySet.iterator();
		while (iter.hasNext()) {
			Entry<SubjectsPredicate, RdfValue> entry = iter.next();
			SubjectsPredicate sp = entry.getKey();
			RdfValue value = entry.getValue();

			update(model, sp, value);
		}
		spToEditedValues.clear();
	}

	public void update(Model model, Resource subject, Property predicate, String newValue) {
		SubjectsPredicate sp = new SubjectsPredicate(subject, predicate);
		RdfValue rdfValue = spToValueMap.get(sp);
		if (rdfValue == null) {
			rdfValue = new RdfValue();
			spToValueMap.put(sp, rdfValue);
		}
		rdfValue.setValue(newValue);
		update(model, sp, rdfValue);
	}

	public void update(Model model, SubjectsPredicate sp, RdfValue newValue) {
		RDFNode object = newValue.toRDFNode(model);
		Statement statement = model.createStatement(sp.getSubject(), sp.getPredicate(), object);

		updating.put(sp, sp);
		try {
			StmtIterator stmtIterator = model.listStatements(sp.getSubject(), sp.getPredicate(), (RDFNode) null);
			model.remove(stmtIterator);

			model.add(statement);
		} finally {
			updating.remove(sp);
			spToValueMap.put(sp, newValue);
			doNotify(statement);
		}
	}

	private void doNotify(Statement statement) {
		SubjectsPredicate sp = new SubjectsPredicate(statement.getSubject(), statement.getPredicate());
		HashSet<Observer> observers = spToObserverMap.get(sp);
		if (observers != null) {
			for (Observer observer : observers) {
				observer.update(sp, statement);
			}
		}
	}

	public RdfValue get(Resource subject, Property predicate) {
		SubjectsPredicate sp = new SubjectsPredicate(subject, predicate);
		RdfValue rdfValue = spToValueMap.get(sp);
		if (rdfValue == null) {
			Statement statement = model.getProperty(subject, predicate);
			if (statement == null) {
				return null;
			}
			RDFNode object = statement.getObject();
			if (object.isLiteral()) {
				rdfValue = new RdfValue(object.asLiteral());
			} else {
				throw new UnsupportedOperationException("Not yet implemented");
			}
			spToValueMap.put(sp, rdfValue);
		}
		return rdfValue;
	}

}
