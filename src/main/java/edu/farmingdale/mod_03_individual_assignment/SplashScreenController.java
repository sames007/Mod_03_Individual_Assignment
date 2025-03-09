package edu.farmingdale.mod_03_individual_assignment;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class SplashScreenController {

    @FXML
    private ImageView splashImage; // This is the image view for the splash screen

    @FXML
    public void initialize() {
        // Set the path for the splash screen image
        String splashPath = "/edu/farmingdale/mod_03_individual_assignment/splashscreenimage/splashscreen.png";
        // Load the image from the given path
        Image image = new Image(getClass().getResourceAsStream(splashPath));
        // Set the loaded image in the splashImage view
        splashImage.setImage(image);
    }
}

