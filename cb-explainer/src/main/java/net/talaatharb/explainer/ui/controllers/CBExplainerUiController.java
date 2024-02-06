package net.talaatharb.explainer.ui.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.SwingUtilities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.talaatharb.explainer.config.HelperBeans;
import net.talaatharb.explainer.facade.CBExplainerFacade;
import net.talaatharb.explainer.service.GraphBuilderService;

@Slf4j
@RequiredArgsConstructor
public class CBExplainerUiController implements Initializable {

	private static final String EDIT_WINDOW_FXML = "../EditWindow.fxml";

	private final CBExplainerFacade explainerFacade;

	private final ObjectMapper objectMapper;

	private final GraphBuilderService graphBuilderService;

	public CBExplainerUiController() {
		objectMapper = HelperBeans.buildObjectMapper();
		explainerFacade = HelperBeans.buildExplainerFacade(HelperBeans.buildConnectionService(),
				HelperBeans.buildExplainerService());
		graphBuilderService = HelperBeans.buildGraphBuilderService();
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

	@FXML
	@Setter(value = AccessLevel.PACKAGE)
	private Button connectButton;

	@FXML
	@Setter(value = AccessLevel.PACKAGE)
	private Button explainButton;

	@FXML
	@Setter(value = AccessLevel.PACKAGE)
	private Button editButton;

	private SwingNode swingNode;

	@FXML
	void connect() {
		final boolean result = explainerFacade.connect();
		if (result) {
			log.info("Connection Successful...");

			connectionStatus.setStyle("-fx-background-color: #00ff00");

			explainButton.setDisable(false);
			connectButton.setDisable(true);
			editButton.setDisable(true);
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
			final String query = queries[i].trim();
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

	@FXML
	void editConnection() {
		log.info("Edit connection");
		final FXMLLoader loader = new FXMLLoader(getClass().getResource(EDIT_WINDOW_FXML));
		Scene newScene;
		try {
			final Parent root = loader.load();
			newScene = new Scene(root);
		} catch (final IOException ex) {
			log.error(ex.getMessage());
			return;
		}

		final Stage editConnectionWindow = new Stage();
		editConnectionWindow.initOwner(null);
		editConnectionWindow.setScene(newScene);
		editConnectionWindow.showAndWait();
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
		final mxGraph graph = graphBuilderService.buildGraphFromJsonPlan(node);

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
}
