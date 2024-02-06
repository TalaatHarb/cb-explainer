package net.talaatharb.explainer.ui.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.talaatharb.explainer.config.HelperBeans;
import net.talaatharb.explainer.service.CBConnectionService;

@Slf4j
@RequiredArgsConstructor
public class EditConnectionUiController implements Initializable {

	private final CBConnectionService connectionService;

	public EditConnectionUiController() {
		connectionService = HelperBeans.buildConnectionService();
	}

	@FXML
	@Setter(value = AccessLevel.PACKAGE)
	private TextField connectionText;

	@FXML
	@Setter(value = AccessLevel.PACKAGE)
	private TextField userText;

	@FXML
	@Setter(value = AccessLevel.PACKAGE)
	private PasswordField passwordText;

	@FXML
	@Setter(value = AccessLevel.PACKAGE)
	private TextField bucketText;

	@FXML
	@Setter(value = AccessLevel.PACKAGE)
	private TextField scopeText;

	@FXML
	void save() {
		log.info("Save Edit");
		final Properties properties = new Properties();

		properties.setProperty(CBConnectionService.CONNECTION, connectionText.getText());
		properties.setProperty(CBConnectionService.USER, userText.getText());
		properties.setProperty(CBConnectionService.PASS, passwordText.getText());
		properties.setProperty(CBConnectionService.BUCKET, bucketText.getText());
		properties.setProperty(CBConnectionService.SCOPE, scopeText.getText());

		connectionService.editConnectionDetails(properties);

		getStage().close();
	}

	@FXML
	void cancel() {
		log.info("Cancel Edit");
		getStage().close();
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		log.info("Editing Connection Details...");

		try {
			final Properties properties = connectionService.loadConnectionDetails();

			connectionText.setText(properties.getProperty(CBConnectionService.CONNECTION));
			userText.setText(properties.getProperty(CBConnectionService.USER));
			passwordText.setText(properties.getProperty(CBConnectionService.PASS));
			bucketText.setText(properties.getProperty(CBConnectionService.BUCKET));
			scopeText.setText(properties.getProperty(CBConnectionService.SCOPE));

		} catch (final IOException e) {
			log.error(e.getMessage());
		}

	}

	Stage getStage() {
		return (Stage) connectionText.getScene().getWindow();
	}

}
