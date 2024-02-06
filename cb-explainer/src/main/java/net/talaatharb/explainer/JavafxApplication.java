package net.talaatharb.explainer;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import net.talaatharb.explainer.ui.events.StageReadyEvent;

public class JavafxApplication extends Application {

	private static final String CSS_FILE = "ui/theme.css";
	private static final int HEIGHT = 600;
	private static final String MAIN_FXML = "ui/MainWindow.fxml";
	private static final String ICON_FILE = "ui/logo.jpg";
	private static final String TITLE = "Couchbase Explainer";
	private static final int WIDTH = 800;

	private ConfigurableApplicationContext context;

	@Override
	public void init() throws Exception {
		final ApplicationContextInitializer<GenericApplicationContext> initializer = genericApplicationContext -> {
			genericApplicationContext.registerBean(Application.class, () -> JavafxApplication.this);
			genericApplicationContext.registerBean(Parameters.class, this::getParameters);
			genericApplicationContext.registerBean(HostServices.class, this::getHostServices);
		};

		context = new SpringApplicationBuilder().sources(CBExplainerApplication.class).initializers(initializer)
				.build().run(getParameters().getRaw().toArray(new String[0]));
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		context.publishEvent(new StageReadyEvent(primaryStage));

		final FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(MAIN_FXML));
		final Parent root = fxmlLoader.load();

		final Image icon = new Image(getClass().getResourceAsStream(ICON_FILE));
		final Scene scene = new Scene(root, WIDTH, HEIGHT);
		scene.getStylesheets().add(getClass().getResource(CSS_FILE).toExternalForm());

		primaryStage.setScene(scene);
		primaryStage.setTitle(TITLE);
		primaryStage.getIcons().add(icon);
		primaryStage.show();
		primaryStage.setMaximized(true);
	}

	@Override
	public void stop() throws Exception {
		context.close();
		Platform.exit();
	}

}
