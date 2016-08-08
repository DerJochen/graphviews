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
public class GraphViewsImpl implements GraphViews {

	// TODO Make rules more into modules
	// TODO Add rule to not only create the label from multiple nodes and remove one or more, but explicitly combine
	// them, so removal is automatic and the attributes are included into the oder node (under a namespace).

	private static final String FORMAT_TTL = "TTL";
	private static final String EMPTY_STRING = "";
	private static final String MSG_OVERLAPPING_MODIFICATIONS = "Overlapping " + GraphModification.class.getSimpleName() + "s";
	private static final ArrayList<GraphModification> EMPTY_ARRAY_LIST = new ArrayList<>();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GraphImpl createView(Path dataFile) throws IOException {
		GraphImpl graph = createView(dataFile, EMPTY_ARRAY_LIST, false);
		return graph;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GraphImpl createView(Path dataFile, boolean collapseAttributes) throws IOException {
		GraphImpl graph = createView(dataFile, EMPTY_ARRAY_LIST, collapseAttributes);
		return graph;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GraphImpl createView(Path dataFile, Path... schemaFiles) throws IOException {
		Model schemaModel = ModelFactory.createDefaultModel();
		ArrayList<GraphModification> graphModifications = new ArrayList<>();

		for (Path schemaFile : schemaFiles) {
			readGraphModifications(schemaFile, schemaModel);
		}

		findGraphModifications(ViewSchema.StatementRemoval, StatementRemoval.class, schemaModel, graphModifications);
		findGraphModifications(ViewSchema.NodeRenaming, NodeRenaming.class, schemaModel, graphModifications);
		findGraphModifications(ViewSchema.NodeStyling, NodeStyling.class, schemaModel, graphModifications);
		findGraphModifications(ViewSchema.PredicateRenaming, PredicateRenaming.class, schemaModel, graphModifications);

		GraphImpl graph = createView(dataFile, graphModifications, true);
		return graph;
	}

	private void readGraphModifications(Path schemaFile, Model schemaModel) throws IOException {
		if (!Files.isReadable(schemaFile)) {
			throw new IllegalArgumentException("File " + schemaFile + " is not readable");
		}

		schemaModel.read(Files.newInputStream(schemaFile), EMPTY_STRING, FORMAT_TTL);

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

	private GraphImpl createView(Path dataFile, ArrayList<GraphModification> graphModifications, boolean collapseAttributes) throws IOException {
		if (!Files.isReadable(dataFile)) {
			throw new IllegalArgumentException("File " + dataFile + " is not readable");
		}

		Model data = ModelFactory.createDefaultModel();
		data.read(Files.newInputStream(dataFile), EMPTY_STRING, FORMAT_TTL);

		GraphImpl graph = new GraphImpl("main");

		// TODO Statement state with applicable modifications and a collision check.
		StmtIterator stmtIterator = data.listStatements();
		while (stmtIterator.hasNext()) {
			Statement statement = stmtIterator.next();

			HashMap<Class<? extends GraphModification>, ArrayList<GraphModification>> relevantGraphModifications = findRelevantGraphModifications(
					graphModifications, statement);

			Resource subject = statement.getSubject();
			Property predicate = statement.getPredicate();
			RDFNode object = statement.getObject();

			handleNodeStylings(subject, graph, relevantGraphModifications);

			ArrayList<GraphModification> statementRemovals = relevantGraphModifications.get(StatementRemoval.class);
			if (statementRemovals != null) {
				continue;
			}

			if (object.isURIResource()) {
				handleResource(relevantGraphModifications, graph, subject, predicate, object);
			} else if (object.isLiteral()) {
				handleLiteral(relevantGraphModifications, graph, subject, predicate, object, collapseAttributes);
			} else {
				// TODO Implement support for anon nodes
				throw new UnsupportedOperationException("Anon nodes are not yet supported");
			}
		}

		return graph;
	}

	private HashMap<Class<? extends GraphModification>, ArrayList<GraphModification>> findRelevantGraphModifications(
			ArrayList<GraphModification> graphModifications, Statement statement) {
		HashMap<Class<? extends GraphModification>, ArrayList<GraphModification>> relevantGraphModifications = new HashMap<>();
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
		return relevantGraphModifications;
	}

	private void handleNodeStylings(Resource subject, GraphImpl graph,
			HashMap<Class<? extends GraphModification>, ArrayList<GraphModification>> relevantGraphModifications) {
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
	}

	private void handleResource(HashMap<Class<? extends GraphModification>, ArrayList<GraphModification>> relevantGraphModifications, GraphImpl graph,
			Resource subject, Property predicate, RDFNode object) {
		Resource objectResource = object.asResource();

		String subjectName = subject.getURI();
		String predicateName = predicate.getURI();
		String objectName = objectResource.getURI();

		NodeImpl subjectNode = graph.useNode(subjectName);
		NodeImpl objectNode = graph.useNode(objectName);

		handleNodeRenamings(subject, subjectNode, object, objectNode, relevantGraphModifications);

		String predicateLabel = handlePredicateRenamings(predicate, relevantGraphModifications);

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
			Resource subject, Property predicate, RDFNode object, boolean collapseAttributes) {
		Literal literal = object.asLiteral();

		String subjectName = subject.getURI();
		String predicateName = predicate.getURI();
		String literalValue = literal.getLexicalForm();

		NodeImpl subjectNode = graph.useNode(subjectName);

		String predicateLabel = predicateName;

		ArrayList<GraphModification> nodeRenamings = relevantGraphModifications.get(NodeRenaming.class);
		if (nodeRenamings != null) {
			if (nodeRenamings.size() > 1) {
				throw new IllegalStateException(MSG_OVERLAPPING_MODIFICATIONS);
			}
			NodeRenaming nodeRenaming = (NodeRenaming) nodeRenamings.get(0);
			String subjectLabel = nodeRenaming.getNewName(subject);
			subjectNode.setLabel(subjectLabel);
		}

		ArrayList<GraphModification> predicaateRenamings = relevantGraphModifications.get(PredicateRenaming.class);
		if (predicaateRenamings != null) {
			if (predicaateRenamings.size() > 1) {
				throw new IllegalStateException(MSG_OVERLAPPING_MODIFICATIONS);
			}
			PredicateRenaming predicateRenaming = (PredicateRenaming) predicaateRenamings.get(0);
			predicateLabel = predicateRenaming.getValue();
		}

		if (collapseAttributes) {
			HashMap<String, List<String>> subjectAttributes = subjectNode.getAttributes();
			List<String> attributeData = subjectAttributes.get(predicateLabel);
			if (attributeData == null) {
				attributeData = new ArrayList<>();
				subjectAttributes.put(predicateLabel, attributeData);
			}
			attributeData.add(literalValue);

			graph.getNodes().add(subjectNode);
		} else {
			String objectName = UUID.randomUUID().toString();
			NodeImpl objectNode = graph.useNode(objectName);
			objectNode.setLabel(literalValue);

			EdgeImpl edge = new EdgeImpl(predicateLabel, subjectNode, objectNode);
			graph.getEdges().add(edge);
		}
	}

	private void handleNodeRenamings(Resource subject, NodeImpl subjectNode, RDFNode object, NodeImpl objectNode,
			HashMap<Class<? extends GraphModification>, ArrayList<GraphModification>> relevantGraphModifications) {
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
	}

	private String handlePredicateRenamings(Property predicate,
			HashMap<Class<? extends GraphModification>, ArrayList<GraphModification>> relevantGraphModifications) {
		String predicateLabel = predicate.getLocalName();
		ArrayList<GraphModification> predicaateRenamings = relevantGraphModifications.get(PredicateRenaming.class);
		if (predicaateRenamings != null) {
			if (predicaateRenamings.size() > 1) {
				throw new IllegalStateException(MSG_OVERLAPPING_MODIFICATIONS);
			}
			PredicateRenaming predicateRenaming = (PredicateRenaming) predicaateRenamings.get(0);
			predicateLabel = predicateRenaming.getValue();
		}
		return predicateLabel;
	}

}
