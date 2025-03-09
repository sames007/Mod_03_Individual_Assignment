package edu.farmingdale.mod_03_individual_assignment;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CardGameController {

    // UI elements for showing cards and getting user input.
    @FXML private ImageView cardImage1; // First card image
    @FXML private ImageView cardImage2; // Second card image
    @FXML private ImageView cardImage3; // Third card image
    @FXML private ImageView cardImage4; // Fourth card image
    @FXML private TextField expressionField; // Input field for arithmetic expression
    @FXML private Button refreshButton;      // Button to refresh cards
    @FXML private Button verifyButton;       // Button to verify expression

    // Store current card values (each between 1 and 52).
    private final int[] currentCards = new int[4];

    // Timer to detect user inactivity (10 seconds).
    private PauseTransition inactivityTimer;
    // Tracks how many hints have been shown (max allowed is 3).
    private int hintIndex = 0;

    /**
     * Called automatically when the FXML file is loaded.
     * Sets up the game: shows new cards, starts the inactivity timer,
     * and assigns click handlers for the card images.
     */
    @FXML
    public void initialize() {
        handleRefresh(null);         // Refresh game to show new cards
        setupInactivityTimer();      // Start the inactivity timer for hints
        setupCardClickHandlers();    // Set up click actions for cards

        // Add styling to the card image views and set a hand cursor to indicate interactivity.
        cardImage1.getStyleClass().add("card-container");
        cardImage2.getStyleClass().add("card-container");
        cardImage3.getStyleClass().add("card-container");
        cardImage4.getStyleClass().add("card-container");

        cardImage1.setCursor(Cursor.HAND);
        cardImage2.setCursor(Cursor.HAND);
        cardImage3.setCursor(Cursor.HAND);
        cardImage4.setCursor(Cursor.HAND);
    }

    /**
     * Called when the Refresh button is clicked.
     * Resets the hint counter and generates a new set of random cards.
     */
    @FXML
    void handleRefresh(ActionEvent event) {
        hintIndex = 0; // Reset hint count (allows 3 hints again for the new deal)
        Random rand = new Random();
        for (int i = 0; i < 4; i++) {
            currentCards[i] = rand.nextInt(52) + 1; // Random card number between 1 and 52
        }
        updateCardImages();    // Refresh the card images displayed on screen
        resetInactivityTimer(); // Restart the inactivity timer for hints
    }

    /**
     * Called when the Verify button is clicked.
     * Checks if the user's arithmetic expression uses the correct card values
     * and whether it evaluates to 24.
     */
    @FXML
    void handleVerify(ActionEvent event) {
        hintIndex = 0; // Reset hint count on user action
        resetInactivityTimer();
        String expression = expressionField.getText().trim();
        if (expression.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Verification Error", "Please enter an expression first.");
            return;
        }

        // Convert card numbers (1-52) to card ranks (1-13)
        List<Integer> cardRanks = getCardRanks(currentCards);
        cardRanks.sort(Integer::compareTo);
        // Extract all number tokens from the expression
        List<Integer> expressionNums = parseNumbers(expression);
        expressionNums.sort(Integer::compareTo);

        // If the numbers don't match, alert the user.
        if (!cardRanks.equals(expressionNums)) {
            showAlert(Alert.AlertType.ERROR, "Verification Error",
                    "Your expression does not use the four card values exactly once.\n" +
                            "Cards are: " + cardRanks + "\n" +
                            "You used: " + expressionNums);
            return;
        }
        double result;
        try {
            result = evaluateExpression(expression);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Verification Error", "Invalid expression.\n" + e.getMessage());
            return;
        }
        // If the expression equals 24, congratulate the user.
        if (Math.abs(result - 24.0) < 1e-6) {
            showAlert(Alert.AlertType.INFORMATION, "Success",
                    "Congratulations! Your expression evaluates to 24. Great job!");
            handleRefresh(null); // Refresh game for a new deal
        } else {
            showAlert(Alert.AlertType.INFORMATION, "Result",
                    "Your expression evaluates to " + result + ", not 24.");
        }
    }

    /**
     * Converts an array of card numbers (1-52) to their respective ranks (1-13).
     */
    private List<Integer> getCardRanks(int[] cards) {
        List<Integer> ranks = new ArrayList<>();
        for (int card : cards) {
            int rank = (card - 1) % 13 + 1;
            ranks.add(rank);
        }
        return ranks;
    }

    /**
     * Extracts all numeric tokens from the given expression string.
     */
    private List<Integer> parseNumbers(String expression) {
        List<Integer> nums = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(expression);
        while (matcher.find()) {
            nums.add(Integer.parseInt(matcher.group()));
        }
        return nums;
    }

    /**
     * Evaluates the arithmetic expression using a simple recursive-descent parser.
     */
    private double evaluateExpression(String expression) throws Exception {
        return new ExpressionParser(expression).parse();
    }

    /**
     * Updates the card images on screen based on the current card numbers.
     */
    private void updateCardImages() {
        for (int i = 0; i < 4; i++) {
            // Build the image path for each card.
            String imagePath = "/edu/farmingdale/mod_03_individual_assignment/cards/card" + currentCards[i] + ".png";
            Image img = new Image(getClass().getResourceAsStream(imagePath));
            // Set the image to the corresponding ImageView.
            switch (i) {
                case 0 -> cardImage1.setImage(img);
                case 1 -> cardImage2.setImage(img);
                case 2 -> cardImage3.setImage(img);
                case 3 -> cardImage4.setImage(img);
            }
        }
    }

    /**
     * Creates and shows a styled alert window.
     * If the message is long or multiline, it uses a TextArea so text can be easily copied.
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = createStyledAlert(type, title, content);
        alert.showAndWait();
        // If there are still hints remaining, restart the inactivity timer.
        if (hintIndex < 3) {
            resetInactivityTimer();
        }
    }

    /**
     * Helper method to create an alert with our CSS style.
     * Uses a TextArea for long messages and makes it editable for copy purposes.
     */
    private Alert createStyledAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        applyCssToDialog(alert);
        // If content is long or has newlines, show it in a TextArea.
        if (content.contains("\n") || content.length() > 100) {
            TextArea textArea = new TextArea(content);
            textArea.setEditable(true); // Allow copying text
            textArea.setWrapText(true);
            textArea.setPrefWidth(480);
            textArea.setPrefHeight(320);
            alert.getDialogPane().setContent(textArea);
        } else {
            alert.setContentText(content);
        }
        return alert;
    }

    /**
     * Applies the CSS file to the alert dialog so it follows the game’s color scheme.
     * Also ensures the dialog pane is tall enough for full text display.
     */
    private void applyCssToDialog(Alert alert) {
        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/edu/farmingdale/mod_03_individual_assignment/style.css").toExternalForm()
        );
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.getDialogPane().getStyleClass().add("hint-alert");
    }

    /**
     * Sets up the inactivity timer.
     * The timer restarts on key press or when the expression field gains focus.
     * If no user action occurs within 10 seconds, a hint is requested.
     */
    private void setupInactivityTimer() {
        inactivityTimer = new PauseTransition(Duration.seconds(10));
        inactivityTimer.setOnFinished(event -> getHintFromAPI());
        expressionField.setOnKeyTyped(e -> resetInactivityTimer());
        expressionField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) resetInactivityTimer();
        });
        inactivityTimer.playFromStart();
    }

    /**
     * Restarts the inactivity timer.
     */
    private void resetInactivityTimer() {
        if (inactivityTimer != null) {
            inactivityTimer.playFromStart();
        }
    }

    /**
     * Requests a hint from the user.
     * Only three hints are allowed per card deal.
     * After three hints, further requests will notify the user that no more hints are available.
     * When the game is refreshed, the hint counter resets.
     */
    private void getHintFromAPI() {
        if (hintIndex >= 3) {
            Platform.runLater(() -> showAlert(Alert.AlertType.INFORMATION, "Hint", "No more hints available for this card configuration."));
            return;
        }
        System.out.println("getHintFromAPI() triggered, hintIndex: " + hintIndex);
        // First show an advertisement before providing a hint.
        showAdvertisement(() -> {
            if (hintIndex == 2) {
                // For the third hint, use our built-in solver.
                String solution = getSolution();
                Platform.runLater(() -> showAlert(Alert.AlertType.INFORMATION, "Hint", "Solution: " + solution));
                hintIndex++;
                if (hintIndex < 3) {
                    inactivityTimer.playFromStart();
                }
                return;
            }
            // For hints 0 and 1, call the external Gemini API.
            Properties config = Helper.loadProperties();
            String apiKey = config.getProperty("API_KEY");
            if (apiKey == null || apiKey.isEmpty()) {
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Config Error", "API key not found in config.properties."));
                return;
            }
            String cardData = getCardRanks(currentCards).stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
            String promptMessage = (hintIndex == 0)
                    ? "You are a helpful assistant for the Card 24 game. For the cards: " + cardData + ", please give a small hint to help solve the game."
                    : "You are a helpful assistant for the Card 24 game. For the cards: " + cardData + ", please give another hint without giving the answer.";

            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey;
            String jsonRequest = "{" +
                    "\"contents\": [{" +
                    "    \"parts\": [{" +
                    "        \"text\": \"" + promptMessage + "\"" +
                    "    }]" +
                    "}]" +
                    "}";
            System.out.println("Request JSON: " + jsonRequest);
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                    .build();
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        System.out.println("HTTP Status: " + response.statusCode());
                        System.out.println("Response Body: " + response.body());
                        String hint = extractHintFromResponse(response.body());
                        Platform.runLater(() -> showAlert(Alert.AlertType.INFORMATION, "Hint", hint));
                    })
                    .exceptionally(ex -> {
                        ex.printStackTrace();
                        Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "API Error", "Failed to retrieve hint."));
                        return null;
                    });
            hintIndex++; // Increase the hint counter
            if (hintIndex < 3) {
                inactivityTimer.playFromStart();
            }
        });
    }

    /**
     * Displays a random advertisement image in a new window.
     * The ad window is undecorated and automatically closes after a few seconds.
     */
    private void showAdvertisement(Runnable callback) {
        List<String> ads = Arrays.asList(
                "/edu/farmingdale/mod_03_individual_assignment/Ads/Image1.png",
                "/edu/farmingdale/mod_03_individual_assignment/Ads/Image2.png",
                "/edu/farmingdale/mod_03_individual_assignment/Ads/Image.gif"
        );
        Random random = new Random();
        String selectedAd = ads.get(random.nextInt(ads.size()));
        Platform.runLater(() -> {
            Stage adStage = new Stage();
            adStage.initStyle(StageStyle.UNDECORATED); // No title bar
            ImageView adImageView = new ImageView();
            Image adImage = new Image(getClass().getResourceAsStream(selectedAd));
            adImageView.setImage(adImage);
            adImageView.setPreserveRatio(true);
            adImageView.setFitWidth(500);
            adImageView.setFitHeight(400);
            StackPane adPane = new StackPane(adImageView);
            Scene adScene = new Scene(adPane);
            // Apply the CSS so that the ad window matches the game’s color scheme.
            adScene.getStylesheets().add(getClass().getResource("/edu/farmingdale/mod_03_individual_assignment/style.css").toExternalForm());
            adStage.setScene(adScene);
            adStage.show();
            int delaySeconds = selectedAd.endsWith(".gif") ? 7 : 5;
            PauseTransition adDelay = new PauseTransition(Duration.seconds(delaySeconds));
            adDelay.setOnFinished(e -> {
                adStage.close();
                callback.run();
            });
            adDelay.play();
        });
    }

    /**
     * Extracts and concatenates hint text from the Gemini API JSON response.
     */
    private String extractHintFromResponse(String responseBody) {
        Pattern pattern = Pattern.compile("\"text\"\\s*:\\s*\"(.*?)\"", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(responseBody);
        StringBuilder hintBuilder = new StringBuilder();
        while (matcher.find()) {
            String part = matcher.group(1).replace("\\n", "\n").replace("\\\"", "\"");
            hintBuilder.append(part).append("\n");
        }
        return hintBuilder.length() > 0 ? hintBuilder.toString().trim() : "No hint available.";
    }

    /**
     * Uses a recursive solver to find an expression that evaluates to 24.
     * Returns the expression if found, or a message indicating no solution.
     */
    private String getSolution() {
        List<Double> nums = new ArrayList<>();
        List<String> exprs = new ArrayList<>();
        for (int n : getCardRanks(currentCards)) {
            nums.add((double) n);
            exprs.add(String.valueOf(n));
        }
        String sol = solve24(nums, exprs);
        return sol != null ? sol : "No solution available.";
    }

    /**
     * Recursively tries different operations to solve the 24 game.
     */
    private String solve24(List<Double> nums, List<String> exprs) {
        if (nums.size() == 1) {
            return Math.abs(nums.get(0) - 24) < 1e-6 ? exprs.get(0) : null;
        }
        for (int i = 0; i < nums.size(); i++) {
            for (int j = i + 1; j < nums.size(); j++) {
                double a = nums.get(i), b = nums.get(j);
                String exprA = exprs.get(i), exprB = exprs.get(j);
                List<Double> candidateVals = new ArrayList<>();
                List<String> candidateExprs = new ArrayList<>();
                // Try addition
                candidateVals.add(a + b);
                candidateExprs.add("(" + exprA + "+" + exprB + ")");
                // Try subtraction (both orders)
                candidateVals.add(a - b);
                candidateExprs.add("(" + exprA + "-" + exprB + ")");
                candidateVals.add(b - a);
                candidateExprs.add("(" + exprB + "-" + exprA + ")");
                // Try multiplication
                candidateVals.add(a * b);
                candidateExprs.add("(" + exprA + "*" + exprB + ")");
                // Try division (avoiding division by zero)
                if (Math.abs(b) > 1e-6) {
                    candidateVals.add(a / b);
                    candidateExprs.add("(" + exprA + "/" + exprB + ")");
                }
                if (Math.abs(a) > 1e-6) {
                    candidateVals.add(b / a);
                    candidateExprs.add("(" + exprB + "/" + exprA + ")");
                }
                // Try each candidate operation
                for (int k = 0; k < candidateVals.size(); k++) {
                    List<Double> nextNums = new ArrayList<>();
                    List<String> nextExprs = new ArrayList<>();
                    nextNums.add(candidateVals.get(k));
                    nextExprs.add(candidateExprs.get(k));
                    for (int m = 0; m < nums.size(); m++) {
                        if (m != i && m != j) {
                            nextNums.add(nums.get(m));
                            nextExprs.add(exprs.get(m));
                        }
                    }
                    String result = solve24(nextNums, nextExprs);
                    if (result != null) return result;
                }
            }
        }
        return null;
    }

    /**
     * Sets up click events for each card image.
     * When a card is clicked, the card's rank is shown in a styled alert.
     */
    private void setupCardClickHandlers() {
        cardImage1.setOnMouseClicked(e -> showCardValue(0));
        cardImage2.setOnMouseClicked(e -> showCardValue(1));
        cardImage3.setOnMouseClicked(e -> showCardValue(2));
        cardImage4.setOnMouseClicked(e -> showCardValue(3));
    }

    /**
     * Displays an alert showing the rank of the clicked card.
     */
    private void showCardValue(int cardIndex) {
        int rank = (currentCards[cardIndex] - 1) % 13 + 1;
        showAlert(Alert.AlertType.INFORMATION, "Card Value", "This card's value is: " + rank);
    }

    // --- ExpressionParser Inner Class ---
    /**
     * A simple recursive-descent parser for arithmetic expressions.
     * Supports addition, subtraction, multiplication, division, and parentheses.
     */
    private static class ExpressionParser {
        private final String str;
        private int pos = -1;
        private int ch;

        ExpressionParser(String str) { this.str = str; }

        // Moves to the next character in the string.
        private void nextChar() {
            pos++;
            ch = (pos < str.length()) ? str.charAt(pos) : -1;
        }

        // If the current character matches charToEat, consume it and return true.
        private boolean eat(int charToEat) {
            while (ch == ' ') nextChar();
            if (ch == charToEat) {
                nextChar();
                return true;
            }
            return false;
        }

        /**
         * Starts parsing the expression and returns its evaluated value.
         */
        public double parse() throws Exception {
            nextChar();
            double x = parseExpression();
            if (pos < str.length()) {
                throw new Exception("Unexpected character: " + (char) ch);
            }
            return x;
        }

        /**
         * Parses addition and subtraction.
         */
        private double parseExpression() throws Exception {
            double x = parseTerm();
            while (true) {
                if (eat('+')) { x += parseTerm(); }
                else if (eat('-')) { x -= parseTerm(); }
                else { return x; }
            }
        }

        /**
         * Parses multiplication and division.
         */
        private double parseTerm() throws Exception {
            double x = parseFactor();
            while (true) {
                if (eat('*')) { x *= parseFactor(); }
                else if (eat('/')) { x /= parseFactor(); }
                else { return x; }
            }
        }

        /**
         * Parses a number, parenthesis, or unary plus/minus.
         */
        private double parseFactor() throws Exception {
            if (eat('+')) return parseFactor(); // Unary plus
            if (eat('-')) return -parseFactor(); // Unary minus
            double x;
            int startPos = pos;
            if (eat('(')) {
                x = parseExpression();
                if (!eat(')')) {
                    throw new Exception("Missing closing parenthesis");
                }
            } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                String numberStr = str.substring(startPos, pos);
                try {
                    x = Double.parseDouble(numberStr);
                } catch (NumberFormatException e) {
                    throw new Exception("Invalid number: " + numberStr);
                }
            } else {
                throw new Exception("Unexpected character: " + (char) ch);
            }
            return x;
        }
    }
}
