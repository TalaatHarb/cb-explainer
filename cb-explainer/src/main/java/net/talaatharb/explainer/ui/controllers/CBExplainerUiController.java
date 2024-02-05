package net.talaatharb.explainer.ui.controllers;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.SwingUtilities;

import com.couchbase.client.core.deps.com.google.common.collect.Lists;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.talaatharb.explainer.config.HelperBeans;
import net.talaatharb.explainer.facade.CBExplainerFacade;

@Slf4j
@RequiredArgsConstructor
public class CBExplainerUiController implements Initializable {

	private static final String OPERATOR = "#operator";

	private static final int VERTICAL_STEP = 50;

	private static final double CHAR_TO_WIDTH_FACTOR = 6.1;

	private final CBExplainerFacade explainerFacade;

	private final ObjectMapper objectMapper;

	public CBExplainerUiController() {
		objectMapper = HelperBeans.buildObjectMapper();
		explainerFacade = HelperBeans.buildExplainerFacade(HelperBeans.buildConnectionService(),
				HelperBeans.buildExplainerService());
	}

	private final Map<String, Object> plans = new HashMap<>();

	@FXML
	@Setter(value = AccessLevel.PACKAGE)
	private TextArea queriesTextArea;

	@FXML
	@Setter(value = AccessLevel.PACKAGE)
	private TextArea planTextArea;

	@FXML
	@Setter(value = AccessLevel.PACKAGE)
	private Label connectionStatus;

	@FXML
	@Setter(value = AccessLevel.PACKAGE)
	private AnchorPane graphPane;

	@FXML
	@Setter(value = AccessLevel.PACKAGE)
	private ListView<String> resultListView;

	private SwingNode swingNode;

	@FXML
	void connect() {
		final boolean result = explainerFacade.connect();
		if (result) {
			log.info("Connection Successful...");
			connectionStatus.setStyle("-fx-background-color: #00ff00");
		} else {
			log.info("Connection Failed...");
			connectionStatus.setStyle("-fx-background-color: #ff0000");
		}
	}

	@FXML
	void explain() throws JsonProcessingException {
		final String[] queries = queriesTextArea.getText().split("\n\n");
		final var resultsList = resultListView.getItems();

		plans.clear();
		resultsList.clear();

		final StringBuilder builder = new StringBuilder("[\n");
		for (int i = 0; i < queries.length; i++) {
			final String query = queries[i];
			resultsList.add(query);

			final String result = explainerFacade.explain(query);

			if (result.startsWith("{")) {
				final JsonNode tree = objectMapper.readTree(result);
				builder.append(objectMapper.writeValueAsString(tree));

				plans.put(query, tree);
			} else {
				builder.append("\"");
				builder.append(result);
				builder.append("\"");

				plans.put(query, result);
			}

			if (i != queries.length - 1) {
				builder.append(",");
			}
		}

		builder.append("\n]");

		planTextArea.setText(builder.toString());
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		log.info("Initializing UI application Main window controller...");

		swingNode = new SwingNode();
		graphPane.getChildren().clear();
		graphPane.getChildren().add(swingNode);
		AnchorPane.setBottomAnchor(swingNode, 0.0);
		AnchorPane.setLeftAnchor(swingNode, 0.0);
		AnchorPane.setRightAnchor(swingNode, 0.0);
		AnchorPane.setTopAnchor(swingNode, 0.0);

		resultListView.getSelectionModel().selectedItemProperty()
		.addListener((observable, oldValue, newValue) -> selectResult(newValue));

		// Mandatory for JGraphX to work correctly
		System.setProperty("java.awt.headless", "false");
	}

	private void selectResult(final String selectedQuery) {
		log.info("Selected Query: " + selectedQuery);
		final Object plan = plans.get(selectedQuery);
		if (plan instanceof final JsonNode jsonNode) {
			SwingUtilities.invokeLater(() -> buildGraphPaneContents(jsonNode));
		} else {
			// clear swingnode
		}

	}

	private void buildGraphPaneContents(final JsonNode node) {
		final mxGraph graph = new mxGraph();
		final Object parent = graph.getDefaultParent();

		final int y = 20;
		final int x = 20;

		graph.getModel().beginUpdate();
		try {
			handleNode(graph, parent, node.get("plan"), x, y);
			// graph.insertEdge(parent, null, "Edge", v1, v2);
		} finally {
			graph.getModel().endUpdate();
		}

		final mxGraphComponent graphComponent = new mxGraphComponent(graph);
		graphComponent.setDragEnabled(false);

		swingNode.setContent(graphComponent);
		graphComponent.repaint();
		graphComponent.refresh();

		Platform.runLater(() -> {
			graphPane.requestFocus();
			swingNode.requestFocus();
			try {
				Thread.sleep(100);
			} catch (final InterruptedException e) {
				log.debug("Thread interrupted");
				Thread.currentThread().interrupt();
			}
			SwingUtilities.invokeLater(graphComponent::repaint);
			SwingUtilities.invokeLater(graphComponent::refresh);
		});
	}

	private int handleNode(final mxGraph graph, final Object parent, final JsonNode node, int x, int y) {
		final String operator = node.get(OPERATOR).asText();
		if ("Sequence".equals(operator)) {
			y = drawSequenceChildren(graph, parent, node, x, y);
		} else if ("Parallel".equals(operator)) {
			y = drawParallelChildren(graph, parent, node, x, y);
		} else if ("IntersectScan".equals(operator)) {
			drawIntersectScan(graph, parent, node, x, y);
			y += VERTICAL_STEP;
		} else if ("UnionScan".equals(operator)) {
			y = drawUnionScan(graph, parent, node, x, y);
		} else {
			drawSimpleNode(graph, parent, node, x, y);
			y += VERTICAL_STEP;
		}
		return y;
	}

	private int drawUnionScan(mxGraph graph, Object parent, JsonNode node, int x, int y) {
		final var scans = Lists.newArrayList(node.get("scans").elements());
		// draw union node
		graph.insertVertex(parent, null, "UnionScan", x, y, 9 * CHAR_TO_WIDTH_FACTOR, 40);

		y += VERTICAL_STEP;
		// draw scans
		for (final var scan : scans) {
			y = handleNode(graph, parent, scan, x, y);
		}
		return y;
	}

	private int drawIntersectScan(mxGraph graph, Object parent, JsonNode node, int x, int y) {
		final var scans = Lists.newArrayList(node.get("scans").elements());
		for (final var scan : scans) {
			x += drawSimpleNode(graph, parent, scan, x, y) + 10;
		}
		return VERTICAL_STEP;
	}

	private double drawSimpleNode(mxGraph graph, Object parent, JsonNode node, int x, int y) {
		String operator = node.get(OPERATOR).asText();
		final StringBuilder builder = new StringBuilder(operator);
		double width = operator.length() * CHAR_TO_WIDTH_FACTOR;

		if (operator.startsWith("DistinctScan")) {
			node = node.get("scan");
			builder.append(": ");
			operator = node.get(OPERATOR).asText();
			builder.append(operator);
			width += 2 + operator.length();
		}

		if (operator.startsWith("IndexScan")) {
			final String indexName = node.get("index").asText();

			builder.append("\n");
			builder.append(indexName);

			width = Math.max(width, indexName.length() * CHAR_TO_WIDTH_FACTOR);
		}

		if (operator.startsWith("KeyScan")) {
			final String keys = node.get("keys").asText();
			builder.append("\n");
			builder.append(keys);

			width = Math.max(width, keys.length() * CHAR_TO_WIDTH_FACTOR);
		}

		if (operator.startsWith("Filter")) {
			final String condition = node.get("condition").asText();
			builder.append("\n");
			builder.append(condition);

			width = Math.max(width, condition.length() * CHAR_TO_WIDTH_FACTOR);
		}

		graph.insertVertex(parent, null, builder.toString(), x, y, width, 40);

		return width;
	}

	private int drawParallelChildren(mxGraph graph, Object parent, JsonNode node, int x, int y) {
		return handleNode(graph, parent, node.get("~child"), x, y);
	}

	private int drawSequenceChildren(mxGraph graph, Object parent, JsonNode node, int x, int y) {
		final var children = Lists.newArrayList(node.get("~children").elements());

		for (final var child : children) {
			y = handleNode(graph, parent, child, x, y);
		}

		return y;
	}

}
