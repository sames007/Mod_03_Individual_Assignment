package edu.farmingdale.mod_03_individual_assignment;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.IOException;
import java.net.URL;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Load and show the splash screen first
        Scene splashScene = loadFXMLScene("/edu/farmingdale/mod_03_individual_assignment/splashscreen.fxml");
        // Attach the CSS file so that styles are applied
        splashScene.getStylesheets().add(getClass().getResource("/edu/farmingdale/mod_03_individual_assignment/style.css").toExternalForm());
        stage.setScene(splashScene);       // Set the scene to splash screen
        stage.setTitle("Card Game - 24");    // Set the window title
        stage.show();                        // Show the window
        // Keep the splash screen for 2 seconds before switching
        PauseTransition delay = new PauseTransition(Duration.seconds(2));
        delay.setOnFinished(event -> switchToGameScene(stage));
        delay.play();  // Start the delay timer
    }

    /**
     * Loads an FXML file and returns the created Scene.
     * @param fxmlFile The path to the FXML file.
     * @return A new Scene built from the FXML, or null if there was an error.
     */
    private Scene loadFXMLScene(String fxmlFile) {
        try {
            URL fxmlUrl = getClass().getResource(fxmlFile);
            // Load the FXML file and create a Scene
            return new Scene(FXMLLoader.load(fxmlUrl));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Switches the current scene to the game scene.
     * Also attaches the CSS stylesheet for proper styling.
     * @param stage The main application window.
     */
    private void switchToGameScene(Stage stage) {
        try {
            // Load the game scene from the FXML file
            Scene gameScene = loadFXMLScene("/edu/farmingdale/mod_03_individual_assignment/card.fxml");
            // Attach the CSS file to the game scene
            String cssFile = "/edu/farmingdale/mod_03_individual_assignment/style.css";
            URL cssUrl = getClass().getResource(cssFile);
            gameScene.getStylesheets().add(cssUrl.toExternalForm());
            stage.setScene(gameScene); // Set the scene to the game scene
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Main method to launch the application
    public static void main(String[] args) {
        launch();
    }
}
