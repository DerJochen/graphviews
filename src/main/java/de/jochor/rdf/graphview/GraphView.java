package de.jochor.rdf.graphview;

import java.io.IOException;
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
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import de.jochor.rdf.graphview.rule.GraphModification;
import de.jochor.rdf.graphview.rule.StatementRemoval;
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
public class GraphView {

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
		createView(dataFile, null, targetFolder, true);
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
		createView(dataFile, null, targetFolder, false);
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

		// TODO
	}

	private void createView(Path dataFile, ArrayList<GraphModification> graphModifications, Path targetFolder, boolean plain) throws IOException {
		if (!Files.isReadable(dataFile)) {
			throw new IllegalArgumentException("File " + dataFile + " is not readable");
		}

		Model data = ModelFactory.createDefaultModel();
		data.read(Files.newInputStream(dataFile), "", "TTL");

		for (GraphModification graphModification : graphModifications) {
			if (graphModification instanceof StatementRemoval) {
				((StatementRemoval) graphModification).modifyGraph(data);
			}
		}

		Graph dotGraph = new Graph();
		HashMap<String, HashMap<String, ArrayList<String>>> attributes = plain ? null : new HashMap<>();

		StmtIterator stmtIterator = data.listStatements();
		while (stmtIterator.hasNext()) {
			Statement statement = stmtIterator.next();

			Resource subject = statement.getSubject();
			dotGraph.getNode(subject.getURI(), true);
			Property predicate = statement.getPredicate();
			RDFNode object = statement.getObject();
			if (object.isURIResource()) {
				handleResource(graphModifications, dotGraph, subject, predicate, object);
			} else if (object.isLiteral()) {
				handleLiteral(graphModifications, dotGraph, subject, predicate, object, attributes);
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
		Files.write(targetFile, dotString.getBytes(), StandardOpenOption.CREATE);
	}

	private void handleResource(ArrayList<GraphModification> graphModifications, Graph dotGraph, Resource subject, Property predicate, RDFNode object) {
		Resource objectResource = object.asResource();

		String subjectName = subject.getURI();
		String predicateName = predicate.getURI();
		String objectName = objectResource.getURI();

		dotGraph.getNode(objectName, true);

		Edge edge = new Edge(subjectName, objectName);
		edge.setLabel(predicateName);
		dotGraph.addEdge(edge);
	}

	private void handleLiteral(ArrayList<GraphModification> graphModifications, Graph dotGraph, Resource subject, Property predicate, RDFNode object,
			HashMap<String, HashMap<String, ArrayList<String>>> attributes) {
		Literal literal = object.asLiteral();

		String subjectName = subject.getURI();
		String predicateName = predicate.getURI();
		String literalValue = literal.getValue().toString();

		if (attributes == null) {
			String objectName = UUID.randomUUID().toString();
			Node objectNode = dotGraph.getNode(objectName, true);
			objectNode.setLabel(literalValue);

			Edge edge = new Edge(subjectName, objectName);
			edge.setLabel(predicateName);
			dotGraph.addEdge(edge);
		} else {
			HashMap<String, ArrayList<String>> subjectAttributes = attributes.get(subjectName);
			if (subjectAttributes == null) {
				subjectAttributes = new HashMap<>();
				attributes.put(subjectName, subjectAttributes);
			}
			ArrayList<String> attributeData = subjectAttributes.get(predicateName);
			if (attributeData == null) {
				attributeData = new ArrayList<>();
				subjectAttributes.put(predicateName, attributeData);
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
			Iterator<Entry<String, ArrayList<String>>> iter2 = subjectAttributes.entrySet().iterator();
			while (iter2.hasNext()) {
				Entry<String, ArrayList<String>> entry2 = iter2.next();
				String attributeName = entry2.getKey();
				ArrayList<String> values = entry2.getValue();
				String value = String.join(", ", values);
				sb.append(attributeName).append(": ").append(value).append(System.lineSeparator());
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
