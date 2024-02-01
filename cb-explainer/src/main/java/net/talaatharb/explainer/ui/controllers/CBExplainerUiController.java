package net.talaatharb.explainer.ui.controllers;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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

	private final CBExplainerFacade explainerFacade;

	private final ObjectMapper objectMapper;

	public CBExplainerUiController() {
		objectMapper = HelperBeans.buildObjectMapper();
		explainerFacade = HelperBeans.buildExplainerFacade();
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
	private void connect() {
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
	private void explain() throws JsonProcessingException {
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

		resultListView.getSelectionModel().selectedItemProperty()
		.addListener((observable, oldValue, newValue) -> selectResult(newValue));
	}

	private void selectResult(String selectedQuery) {
		log.info("Selected Query: " + selectedQuery);
	}

}
