package net.talaatharb.explainer.ui.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import org.springframework.stereotype.Component;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.talaatharb.explainer.facade.CBExplainerFacade;
import net.talaatharb.explainer.facade.CBExplainerFacadeImpl;
import net.talaatharb.explainer.service.CBConnectionServiceImpl;
import net.talaatharb.explainer.service.CBExplainServiceImpl;

@Slf4j
@Component
@RequiredArgsConstructor
public class CBExplainerUiController implements Initializable {

	private final CBExplainerFacade explainerFacade;

	public CBExplainerUiController() {
		explainerFacade = new CBExplainerFacadeImpl(new CBConnectionServiceImpl(), new CBExplainServiceImpl());
	}

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
	private void connect() {
		final boolean result = explainerFacade.connect();
		if (result) {
			log.info("Connection Successful...");
			connectionStatus.setStyle("-fx-background-color: #00ff00");
		}
	}

	@FXML
	private void explain() {
		final String[] queries = queriesTextArea.getText().split("\n\n");

		final StringBuilder builder = new StringBuilder("[");
		for (int i = 0; i < queries.length; i++) {
			final String result = explainerFacade.explain(queries[i]);

			if (result.startsWith("{")) {
				builder.append(result);
			} else {
				builder.append("\"");
				builder.append(result);
				builder.append("\"");
			}

			if (i != queries.length - 1) {
				builder.append(",");
			}
		}

		builder.append("]");

		planTextArea.setText(builder.toString());
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		log.info("Initializing UI application Main window controller...");

	}

}