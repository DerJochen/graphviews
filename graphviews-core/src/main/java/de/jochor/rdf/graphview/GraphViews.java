package de.jochor.rdf.graphview;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

import de.jochor.rdf.graphview.model.EdgeImpl;
import de.jochor.rdf.graphview.model.GraphImpl;
import de.jochor.rdf.graphview.model.NodeImpl;
import de.jochor.rdf.graphview.modification.GraphModification;
import de.jochor.rdf.graphview.modification.NodeRenaming;
import de.jochor.rdf.graphview.modification.NodeStyling;
import de.jochor.rdf.graphview.modification.PredicateRenaming;
import de.jochor.rdf.graphview.modification.StatementRemoval;
import de.jochor.rdf.graphview.vocabulary.ViewSchema;

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

	// TODO Make rules more into modules
	// TODO Add rule to not only create the label from multiple nodes and remove one or more, but explicitly combine
	// them, so removal is automatic and the attributes are included into the oder node (under a namespace).

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
	public GraphImpl createPlainView(Path dataFile) throws IOException {
		GraphImpl graph = createView(dataFile, EMPTY_ARRAY_LIST, true);
		return graph;
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
	public GraphImpl createView(Path dataFile) throws IOException {
		GraphImpl graph = createView(dataFile, EMPTY_ARRAY_LIST, false);
		return graph;
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
	public GraphImpl createModifiedView(Path dataFile, Path... schemaFiles) throws IOException {
		Model schemaModel = ModelFactory.createDefaultModel();
		ArrayList<GraphModification> graphModifications = new ArrayList<>();

		for (Path schemaFile : schemaFiles) {
			readGraphModifications(schemaFile, graphModifications, schemaModel);
		}

		findGraphModifications(ViewSchema.StatementRemoval, StatementRemoval.class, schemaModel, graphModifications);
		findGraphModifications(ViewSchema.NodeRenaming, NodeRenaming.class, schemaModel, graphModifications);
		findGraphModifications(ViewSchema.NodeStyling, NodeStyling.class, schemaModel, graphModifications);
		findGraphModifications(ViewSchema.PredicateRenaming, PredicateRenaming.class, schemaModel, graphModifications);

		GraphImpl graph = createView(dataFile, graphModifications, false);
		return graph;
	}

	private void readGraphModifications(Path schemaFile, ArrayList<GraphModification> graphModifications, Model schemaModel) throws IOException {
		if (!Files.isReadable(schemaFile)) {
			throw new IllegalArgumentException("File " + schemaFile + " is not readable");
		}

		schemaModel.read(Files.newInputStream(schemaFile), "", "TTL");

		StmtIterator stmtIterator = schemaModel.listStatements(null, ViewSchema.replaces, (RDFNode) null);
		while (stmtIterator.hasNext()) {
			Statement replaceStatement = stmtIterator.next();
			Resource replacedModification = replaceStatement.getObject().asResource();
			StmtIterator stmtIterator2 = schemaModel.listStatements(replacedModification, RDF.type, (RDFNode) null);
			schemaModel.remove(stmtIterator2);
		}
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

	private GraphImpl createView(Path dataFile, ArrayList<GraphModification> graphModifications, boolean plain) throws IOException {
		if (!Files.isReadable(dataFile)) {
			throw new IllegalArgumentException("File " + dataFile + " is not readable");
		}

		Model data = ModelFactory.createDefaultModel();
		data.read(Files.newInputStream(dataFile), "", "TTL");

		GraphImpl graph = new GraphImpl("main");

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

			Resource subject = statement.getSubject();
			Property predicate = statement.getPredicate();
			RDFNode object = statement.getObject();

			ArrayList<GraphModification> nodeStylings = relevantGraphModifications.get(NodeStyling.class);
			if (nodeStylings != null) {
				String subjectColor = null;

				for (int i = 0; i < nodeStylings.size(); i++) {
					NodeStyling nodeStyling = (NodeStyling) nodeStylings.get(i);

					String newSubjectColor = nodeStyling.getColor();
					if (subjectColor != null && newSubjectColor != null && !subjectColor.equals(newSubjectColor)) {
						throw new IllegalStateException("Duplicate coloring of " + subject);
					}
					subjectColor = newSubjectColor;
				}

				if (subjectColor != null) {
					NodeImpl subjectNode = graph.useNode(subject.getURI());
					subjectNode.setColor(subjectColor);
					graph.getNodes().add(subjectNode);
				}
			}

			ArrayList<GraphModification> statementRemovals = relevantGraphModifications.get(StatementRemoval.class);
			if (statementRemovals != null) {
				continue;
			}

			if (object.isURIResource()) {
				handleResource(relevantGraphModifications, graph, subject, predicate, object);
			} else if (object.isLiteral()) {
				handleLiteral(relevantGraphModifications, graph, subject, predicate, object, plain);
			} else {
				// TODO Implement support for anon nodes
				throw new UnsupportedOperationException("Anon nodes are not yet supported");
			}
		}

		return graph;
	}

	private void handleResource(HashMap<Class<? extends GraphModification>, ArrayList<GraphModification>> relevantGraphModifications, GraphImpl graph,
			Resource subject, Property predicate, RDFNode object) {
		Resource objectResource = object.asResource();

		String subjectName = subject.getURI();
		String predicateName = predicate.getURI();
		String objectName = objectResource.getURI();

		NodeImpl subjectNode = graph.useNode(subjectName);
		NodeImpl objectNode = graph.useNode(objectName);

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

		String predicateLabel = predicate.getLocalName();
		ArrayList<GraphModification> predicaateRenamings = relevantGraphModifications.get(PredicateRenaming.class);
		if (predicaateRenamings != null) {
			if (predicaateRenamings.size() > 1) {
				throw new IllegalStateException("Overlapping " + GraphModification.class.getSimpleName() + "s");
			}
			PredicateRenaming predicateRenaming = (PredicateRenaming) predicaateRenamings.get(0);
			predicateLabel = predicateRenaming.getValue();
		}

		EdgeImpl edge = new EdgeImpl(predicateLabel, subjectNode, objectNode);

		HashMap<String, List<String>> edgeAttributes = edge.getAttributes();
		List<String> attributeData = edgeAttributes.get("_Term_");
		if (attributeData == null) {
			attributeData = new ArrayList<>();
			edgeAttributes.put("_Term_", attributeData);
		}
		attributeData.add(predicateName);

		graph.getEdges().add(edge);
	}

	private void handleLiteral(HashMap<Class<? extends GraphModification>, ArrayList<GraphModification>> relevantGraphModifications, GraphImpl graph,
			Resource subject, Property predicate, RDFNode object, boolean plain) {
		Literal literal = object.asLiteral();

		String subjectName = subject.getURI();
		String predicateName = predicate.getURI();
		String literalValue = literal.getLexicalForm();

		NodeImpl subjectNode = graph.useNode(subjectName);

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

		if (plain) {
			String objectName = UUID.randomUUID().toString();
			NodeImpl objectNode = graph.useNode(objectName);
			objectNode.setLabel(literalValue);

			EdgeImpl edge = new EdgeImpl(predicateLabel, subjectNode, objectNode);
			graph.getEdges().add(edge);
		} else {
			HashMap<String, List<String>> subjectAttributes = subjectNode.getAttributes();
			List<String> attributeData = subjectAttributes.get(predicateLabel);
			if (attributeData == null) {
				attributeData = new ArrayList<>();
				subjectAttributes.put(predicateLabel, attributeData);
			}
			attributeData.add(literalValue);

			graph.getNodes().add(subjectNode);
		}
	}

}
