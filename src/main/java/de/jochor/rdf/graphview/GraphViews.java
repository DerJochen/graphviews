package de.jochor.rdf.graphview;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;

import de.jochor.rdf.graphview.modification.GraphModification;
import de.jochor.rdf.graphview.modification.NodeRenaming;
import de.jochor.rdf.graphview.modification.PredicateRenaming;
import de.jochor.rdf.graphview.modification.StatementRemoval;
import de.jochor.rdf.graphview.vocabulary.ViewSchema;
import info.leadinglight.jdot.Edge;
import info.leadinglight.jdot.Graph;
import info.leadinglight.jdot.Node;

/**
 * Primary class of the GraphView project.
 *
 * <p>
 * <b>Started:</b> 2016-07-05
 * </p>
 *
 * @author Jochen Hormes
 *
 */
public class GraphViews {

	private static final ArrayList<GraphModification> EMPTY_ARRAY_LIST = new ArrayList<>();

	/**
	 * Creates a plain view of the data without any graph modifications.
	 *
	 * @param dataFile
	 *            Data to visualize
	 * @param targetFolder
	 *            Target folder for the result file(s)
	 * @throws IOException
	 *             In case of problems with the dataFile or the target Folder
	 */
	public void createPlainView(Path dataFile, Path targetFolder) throws IOException {
		createView(dataFile, EMPTY_ARRAY_LIST, targetFolder, true);
	}

	/**
	 * Creates a graph view of the data with literal nodes mapped to attributes of the graph nodes.
	 *
	 * @param dataFile
	 *            Data to visualize
	 * @param targetFolder
	 *            Target folder for the result file(s)
	 * @throws IOException
	 *             In case of problems with the dataFile or the target Folder
	 */
	public void createView(Path dataFile, Path targetFolder) throws IOException {
		createView(dataFile, EMPTY_ARRAY_LIST, targetFolder, false);
	}

	/**
	 * Creates a graph view of the data with literal nodes mapped to attributes of the graph nodes and view schema files
	 * applied.
	 *
	 * @param dataFile
	 *            Data to visualize
	 * @param targetFolder
	 *            Target folder for the result file(s)
	 * @param schemaFiles
	 *            Schema files to apply to the data
	 * @throws IOException
	 *             In case of problems with the dataFile or the target Folder
	 */
	public void createModifiedView(Path dataFile, Path targetFolder, Path... schemaFiles) throws IOException {
		ArrayList<GraphModification> graphModifications = new ArrayList<>();

		for (Path schemaFile : schemaFiles) {
			readGraphModifications(schemaFile, graphModifications);
		}

		createView(dataFile, graphModifications, targetFolder, false);
	}

	private void readGraphModifications(Path schemaFile, ArrayList<GraphModification> graphModifications) throws IOException {
		if (!Files.isReadable(schemaFile)) {
			throw new IllegalArgumentException("File " + schemaFile + " is not readable");
		}

		Model schemaModel = ModelFactory.createDefaultModel();
		schemaModel.read(Files.newInputStream(schemaFile), "", "TTL");

		findGraphModifications(ViewSchema.StatementRemoval, StatementRemoval.class, schemaModel, graphModifications);
		findGraphModifications(ViewSchema.NodeRenaming, NodeRenaming.class, schemaModel, graphModifications);
		findGraphModifications(ViewSchema.PredicateRenaming, PredicateRenaming.class, schemaModel, graphModifications);
	}

	@SuppressWarnings("unchecked")
	private void findGraphModifications(Resource rdfType, Class<?> javaType, Model schemaModel, ArrayList<GraphModification> graphModifications) {
		ResIterator statementRemovalIter = schemaModel.listSubjectsWithProperty(RDF.type, rdfType);
		while (statementRemovalIter.hasNext()) {
			Resource resource = statementRemovalIter.next();
			try {
				Constructor<? extends GraphModification> constructor;
				constructor = (Constructor<? extends GraphModification>) javaType.getConstructor(Resource.class);
				GraphModification graphModification = constructor.newInstance(resource);
				graphModifications.add(graphModification);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	private void createView(Path dataFile, ArrayList<GraphModification> graphModifications, Path targetFolder, boolean plain) throws IOException {
		if (!Files.isReadable(dataFile)) {
			throw new IllegalArgumentException("File " + dataFile + " is not readable");
		}

		Model data = ModelFactory.createDefaultModel();
		data.read(Files.newInputStream(dataFile), "", "TTL");

		Graph dotGraph = new Graph();
		HashMap<String, HashMap<String, ArrayList<String>>> attributes = plain ? null : new HashMap<>();

		// TODO Statement state with applicable modifications and a collision check.
		HashMap<Class<? extends GraphModification>, ArrayList<GraphModification>> relevantGraphModifications = new HashMap<>();
		StmtIterator stmtIterator = data.listStatements();
		while (stmtIterator.hasNext()) {
			Statement statement = stmtIterator.next();

			relevantGraphModifications.clear();
			for (GraphModification graphModification : graphModifications) {
				if (graphModification.handles(statement)) {
					Class<? extends GraphModification> type = graphModification.getClass();
					ArrayList<GraphModification> modifications = relevantGraphModifications.get(type);
					if (modifications == null) {
						modifications = new ArrayList<>();
						relevantGraphModifications.put(type, modifications);
					}
					modifications.add(graphModification);
				}
			}

			ArrayList<GraphModification> statementRemovals = relevantGraphModifications.get(StatementRemoval.class);
			if (statementRemovals != null) {
				continue;
			}

			Resource subject = statement.getSubject();
			dotGraph.getNode(subject.getURI(), true);
			Property predicate = statement.getPredicate();
			RDFNode object = statement.getObject();
			if (object.isURIResource()) {
				handleResource(relevantGraphModifications, dotGraph, subject, predicate, object);
			} else if (object.isLiteral()) {
				handleLiteral(relevantGraphModifications, dotGraph, subject, predicate, object, attributes);
			} else {
				// TODO Implement support for anon nodes
				throw new UnsupportedOperationException("Anon nodes are not yet supported");
			}
		}

		if (attributes != null) {
			createTooltips(dotGraph, attributes);
		}

		Path targetFile = Files.createDirectories(targetFolder).resolve("graph.dot");
		String dotString = dotGraph.toDot();
		Files.write(targetFile, dotString.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
	}

	private void handleResource(HashMap<Class<? extends GraphModification>, ArrayList<GraphModification>> relevantGraphModifications, Graph dotGraph,
			Resource subject, Property predicate, RDFNode object) {
		Resource objectResource = object.asResource();

		String subjectName = subject.getURI();
		String predicateName = predicate.getURI();
		String objectName = objectResource.getURI();

		Node subjectNode = dotGraph.getNode(subjectName, true);
		Node objectNode = dotGraph.getNode(objectName, true);

		String predicateLabel = predicateName;

		ArrayList<GraphModification> nodeRenamings = relevantGraphModifications.get(NodeRenaming.class);
		if (nodeRenamings != null) {
			String subjectLabel = null;
			String objectLabel = null;

			for (int i = 0; i < nodeRenamings.size(); i++) {
				NodeRenaming nodeRenaming = (NodeRenaming) nodeRenamings.get(i);

				String newSubjectLabel = nodeRenaming.getNewName(subject);
				if (subjectLabel != null && newSubjectLabel != null && !subjectLabel.equals(newSubjectLabel)) {
					throw new IllegalStateException("Duplicate renaming of " + subject);
				}
				subjectLabel = newSubjectLabel;

				String newObjectLabel = nodeRenaming.getNewName(object.asResource());
				if (objectLabel != null && newObjectLabel != null && !objectLabel.equals(newObjectLabel)) {
					throw new IllegalStateException("Duplicate renaming of " + object);
				}
				objectLabel = newObjectLabel;
			}

			if (subjectLabel != null) {
				subjectNode.setLabel(subjectLabel);
			}
			if (objectLabel != null) {
				objectNode.setLabel(objectLabel);
			}
		}

		ArrayList<GraphModification> predicaateRenamings = relevantGraphModifications.get(PredicateRenaming.class);
		if (predicaateRenamings != null) {
			if (predicaateRenamings.size() > 1) {
				throw new IllegalStateException("Overlapping " + GraphModification.class.getSimpleName() + "s");
			}
			PredicateRenaming predicateRenaming = (PredicateRenaming) predicaateRenamings.get(0);
			predicateLabel = predicateRenaming.getValue();
		}

		dotGraph.getNode(objectName, true);

		Edge edge = new Edge(subjectName, objectName);
		edge.setLabel(predicateLabel);
		dotGraph.addEdge(edge);
	}

	private void handleLiteral(HashMap<Class<? extends GraphModification>, ArrayList<GraphModification>> relevantGraphModifications, Graph dotGraph,
			Resource subject, Property predicate, RDFNode object, HashMap<String, HashMap<String, ArrayList<String>>> attributes) {
		Literal literal = object.asLiteral();

		String subjectName = subject.getURI();
		String predicateName = predicate.getURI();
		String literalValue = literal.getValue().toString();

		Node subjectNode = dotGraph.getNode(subjectName, true);

		String predicateLabel = predicateName;

		ArrayList<GraphModification> nodeRenamings = relevantGraphModifications.get(NodeRenaming.class);
		if (nodeRenamings != null) {
			if (nodeRenamings.size() > 1) {
				throw new IllegalStateException("Overlapping " + GraphModification.class.getSimpleName() + "s");
			}
			NodeRenaming nodeRenaming = (NodeRenaming) nodeRenamings.get(0);
			String subjectLabel = nodeRenaming.getNewName(subject);
			subjectNode.setLabel(subjectLabel);
		}

		ArrayList<GraphModification> predicaateRenamings = relevantGraphModifications.get(PredicateRenaming.class);
		if (predicaateRenamings != null) {
			if (predicaateRenamings.size() > 1) {
				throw new IllegalStateException("Overlapping " + GraphModification.class.getSimpleName() + "s");
			}
			PredicateRenaming predicateRenaming = (PredicateRenaming) predicaateRenamings.get(0);
			predicateLabel = predicateRenaming.getValue();
		}

		if (attributes == null) {
			String objectName = UUID.randomUUID().toString();
			Node objectNode = dotGraph.getNode(objectName, true);
			objectNode.setLabel(literalValue);

			Edge edge = new Edge(subjectName, objectName);
			edge.setLabel(predicateLabel);
			dotGraph.addEdge(edge);
		} else {
			HashMap<String, ArrayList<String>> subjectAttributes = attributes.get(subjectName);
			if (subjectAttributes == null) {
				subjectAttributes = new HashMap<>();
				attributes.put(subjectName, subjectAttributes);
			}
			ArrayList<String> attributeData = subjectAttributes.get(predicateLabel);
			if (attributeData == null) {
				attributeData = new ArrayList<>();
				subjectAttributes.put(predicateLabel, attributeData);
			}
			attributeData.add(literalValue);
		}
	}

	private void createTooltips(Graph dotGraph, HashMap<String, HashMap<String, ArrayList<String>>> attributes) {
		Iterator<Entry<String, HashMap<String, ArrayList<String>>>> iter = attributes.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, HashMap<String, ArrayList<String>>> entry = iter.next();

			HashMap<String, ArrayList<String>> subjectAttributes = entry.getValue();
			StringBuilder sb = new StringBuilder();
			String nl = "";
			Iterator<Entry<String, ArrayList<String>>> iter2 = subjectAttributes.entrySet().iterator();
			while (iter2.hasNext()) {
				Entry<String, ArrayList<String>> entry2 = iter2.next();
				String attributeName = entry2.getKey();
				ArrayList<String> values = entry2.getValue();
				String value = String.join(", ", values);
				sb.append(nl).append(attributeName).append(": ").append(value);
				nl = System.lineSeparator();
			}

			String subjectName = entry.getKey();
			Node subjectNode = dotGraph.getNode(subjectName);
			subjectNode.setToolTip(sb.toString());
		}
	}

	// digraph {
	// "http://www.jochor.de/demo/person/1" [label="Jochen Hormes"]
	// "http://www.jochor.de/demo/person/1" ->
	// "http://www.jochor.de/demo/address/1" [label="lives at"]
	// "http://www.jochor.de/demo/address/1"
	// [label="42 Demo Street, 12345 Demo"]
	// }

}
