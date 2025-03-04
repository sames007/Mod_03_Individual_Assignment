package edu.farmingdale.mod_03_individual_assignment;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Pair;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CardGameController {

    @FXML
    private ImageView cardImage1; // Card 1 image view
    @FXML
    private ImageView cardImage2; // Card 2 image view
    @FXML
    private ImageView cardImage3; // Card 3 image view
    @FXML
    private ImageView cardImage4; // Card 4 image view
    @FXML
    private TextField expressionField; // Text field for user expression input
    @FXML
    private TextField extraField; // This field displays the solution for "Find a Solution"
    @FXML
    private Button findSolutionButton; // Button to find a solution automatically
    @FXML
    private Button refreshButton;      // Button to refresh and generate new cards
    @FXML
    private Button verifyButton;       // Button to verify the user's expression

    // Array to store the 4 card numbers (each is a number from 1 to 52)
    private final int[] currentCards = new int[4];

    /**
     * Called automatically when the FXML is loaded.
     * This method generates 4 random cards at startup.
     */
    @FXML
    public void initialize() {
        handleRefresh(null); // Generate new cards immediately
    }

    /**
     * "Refresh" button: Randomly generates 4 new card numbers (from 1 to 52)
     * and updates the card images on the screen.
     */
    @FXML
    void handleRefresh(ActionEvent event) {
        Random rand = new Random();
        for (int i = 0; i < 4; i++) {
            // Generate a random card number between 1 and 52
            currentCards[i] = rand.nextInt(52) + 1;
        }
        updateCardImages(); // Update UI images based on new card numbers
    }

    /**
     * "Find a Solution" button: Attempts to find an arithmetic expression
     * that evaluates to 24 using the current card values.
     * The found solution is displayed in the extraField.
     */
    @FXML
    void handleFindSolution(ActionEvent event) {
        String solution = findSolution();
        if (solution != null) {
            extraField.setText(solution); // Show the solution in the extraField
        } else {
            extraField.setText("No valid solution found.");
        }
    }

    /**
     * "Verify" button: Checks if the user's entered expression:
     * 1. Uses the four card values exactly once.
     * 2. Evaluates to 24.
     * Displays an alert dialog with the result.
     */
    @FXML
    void handleVerify(ActionEvent event) {
        String expression = expressionField.getText().trim();
        if (expression.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Verification Error", "Please enter an expression first.");
            return;
        }

        // Step 1: Get the 4 card ranks from the currentCards array.
        // Each card number (1..52) is converted to its rank (1..13) using math.
        List<Integer> cardRanks = getCardRanks(currentCards);
        cardRanks.sort(Integer::compareTo);

        // Step 2: Extract all numbers (tokens) from the user's expression.
        List<Integer> expressionNums = parseNumbers(expression);
        expressionNums.sort(Integer::compareTo);

        // Step 3: Check that the numbers in the expression match the card ranks exactly.
        if (!cardRanks.equals(expressionNums)) {
            showAlert(Alert.AlertType.ERROR, "Verification Error",
                    "Your expression does not use the four card values exactly once.\n"
                            + "Cards are: " + cardRanks + "\n"
                            + "You used: " + expressionNums);
            return;
        }

        // Step 4: Evaluate the arithmetic expression using our custom parser.
        double result;
        try {
            result = evaluateExpression(expression);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Verification Error",
                    "Invalid expression.\n" + e.getMessage());
            return;
        }

        // Step 5: Check if the evaluated result is 24 (allowing a very small margin for floating-point errors)
        if (Math.abs(result - 24.0) < 1e-6) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Congratulations! Your expression evaluates to 24.");
        } else {
            showAlert(Alert.AlertType.INFORMATION, "Result", "Your expression evaluates to " + result + ", not 24.");
        }
    }

    /**
     * Converts each card number (1..52) into its rank (1..13).
     * Math Explanation:
     * - There are 52 cards in a deck and 13 ranks per suit.
     * - Subtracting 1 from the card number makes it zero-based.
     * - Taking (card - 1) % 13 gives a number from 0 to 12.
     * - Adding 1 converts it to a rank from 1 to 13.
     *
     * Example: For card number 14 (first card in Hearts), (14 - 1) % 13 + 1 = 13 % 13 + 1 = 0 + 1 = 1 (Ace)
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
     * Extracts all integer tokens (numbers) from the given expression string.
     * Math is minimal here: it simply reads sequences of digits as numbers.
     *
     * Example: In the expression "(6+6)*12/9", this method will extract [6, 6, 12, 9].
     */
    private List<Integer> parseNumbers(String expression) {
        List<Integer> nums = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\d+"); // Regex to find one or more digits
        Matcher matcher = pattern.matcher(expression);
        while (matcher.find()) {
            nums.add(Integer.parseInt(matcher.group()));
        }
        return nums;
    }

    /**
     * Evaluates an arithmetic expression using a custom recursive-descent parser.
     * The parser handles basic arithmetic operations: addition, subtraction,
     * multiplication, division, and supports parentheses.
     */
    private double evaluateExpression(String expression) throws Exception {
        return new ExpressionParser(expression).parse();
    }

    /**
     * Loads card images into the ImageViews.
     * Builds the image path using the unique card number.
     */
    private void updateCardImages() {
        for (int i = 0; i < 4; i++) {
            String imagePath = "/edu/farmingdale/mod_03_individual_assignment/cards/card" + currentCards[i] + ".png";
            Image img = new Image(getClass().getResourceAsStream(imagePath));
            switch (i) {
                case 0 -> cardImage1.setImage(img);
                case 1 -> cardImage2.setImage(img);
                case 2 -> cardImage3.setImage(img);
                case 3 -> cardImage4.setImage(img);
            }
        }
    }

    /**
     * Displays an alert dialog with the given type, title, and content.
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null); // No header text for simplicity
        alert.setContentText(content);
        alert.showAndWait(); // Wait for the user to close the dialog
    }

    /**
     * Finds an arithmetic expression that evaluates to 24 using the current card values.
     * Math Explanation:
     * - Each card number is converted to its rank (value).
     * - These values are stored in a list (as doubles) and a corresponding list of strings.
     * - The recursive search tries every possible combination of applying operations (+, -, *, /)
     *   between pairs of numbers.
     * - If a combination produces a result of 24 (within a small margin), the corresponding expression is returned.
     *
     * @return A string representing the expression if a solution is found; otherwise, null.
     */
    private String findSolution() {
        List<Double> numbers = new ArrayList<>();
        List<String> exprs = new ArrayList<>();
        for (int card : currentCards) {
            int rank = (card - 1) % 13 + 1; // Convert card number to rank
            numbers.add((double) rank);
            exprs.add(String.valueOf(rank)); // Start with the number as a string
        }
        return search(numbers, exprs);
    }

    /**
     * Recursively searches for an expression that evaluates to 24.
     * Math Explanation:
     * - The method takes two lists: one of numbers (as doubles) and one of their corresponding expression strings.
     * - It picks every possible pair of numbers and tries all operations:
     *   addition, subtraction (both orders), multiplication, and division (both orders).
     * - It then replaces the pair with the result of the operation and recurses.
     * - The base case is when only one number is left; if it is 24 (allowing for floating point tolerance),
     *   the corresponding expression is returned.
     *
     * @param nums  List of current numbers.
     * @param exprs List of corresponding expressions as strings.
     * @return The expression string if a solution is found; otherwise, null.
     */
    private String search(List<Double> nums, List<String> exprs) {
        if (nums.size() == 1) {
            if (Math.abs(nums.get(0) - 24.0) < 1e-6) {
                return exprs.get(0);
            }
            return null;
        }
        // Try every pair of numbers in the list
        for (int i = 0; i < nums.size(); i++) {
            for (int j = i + 1; j < nums.size(); j++) {
                double a = nums.get(i);
                double b = nums.get(j);
                String expA = exprs.get(i);
                String expB = exprs.get(j);

                // Build a list of possible results from applying each operator on a and b.
                // Note: Subtraction and division are not commutative, so both orders are tried.
                List<Pair<Double, String>> possibilities = new ArrayList<>();
                possibilities.add(new Pair<>(a + b, "(" + expA + "+" + expB + ")"));
                possibilities.add(new Pair<>(a - b, "(" + expA + "-" + expB + ")"));
                possibilities.add(new Pair<>(b - a, "(" + expB + "-" + expA + ")"));
                possibilities.add(new Pair<>(a * b, "(" + expA + "*" + expB + ")"));
                if (Math.abs(b) > 1e-6) { // Prevent division by zero
                    possibilities.add(new Pair<>(a / b, "(" + expA + "/" + expB + ")"));
                }
                if (Math.abs(a) > 1e-6) { // Prevent division by zero
                    possibilities.add(new Pair<>(b / a, "(" + expB + "/" + expA + ")"));
                }

                // For each possibility, create new lists with the selected pair replaced by the result.
                for (Pair<Double, String> p : possibilities) {
                    List<Double> newNums = new ArrayList<>(nums);
                    List<String> newExprs = new ArrayList<>(exprs);
                    // Remove the two numbers at indices j and i (remove j first because it has a larger index)
                    newNums.remove(j);
                    newNums.remove(i);
                    newExprs.remove(j);
                    newExprs.remove(i);
                    // Add the new number and its expression
                    newNums.add(p.getKey());
                    newExprs.add(p.getValue());
                    // Recurse: try to find a solution with this new list
                    String res = search(newNums, newExprs);
                    if (res != null) {
                        return res;
                    }
                }
            }
        }
        return null; // No solution found
    }

    /**
     * A simple recursive-descent parser to evaluate arithmetic expressions.
     * Math Explanation:
     * - This parser reads the expression character by character.
     * - It handles numbers (including decimals), parentheses, and basic operators (+, -, *, /).
     * - The parser uses recursion to deal with nested expressions (inside parentheses).
     * - It follows the order of operations: first multiplication/division, then addition/subtraction.
     */
    private static class ExpressionParser {
        private final String str; // The arithmetic expression string
        private int pos = -1;     // Current position in the string
        private int ch;           // Current character

        ExpressionParser(String str) {
            this.str = str;
        }

        // Move to the next character in the string
        private void nextChar() {
            pos++;
            ch = (pos < str.length()) ? str.charAt(pos) : -1;
        }

        // If the current character equals the target, move to the next character and return true
        private boolean eat(int charToEat) {
            while (ch == ' ') { // Skip spaces
                nextChar();
            }
            if (ch == charToEat) {
                nextChar();
                return true;
            }
            return false;
        }

        /**
         * Parses the full expression and returns its evaluated numeric value.
         * @return The result of the expression.
         * @throws Exception if there is a parsing error.
         */
        public double parse() throws Exception {
            nextChar(); // Start at the first character
            double x = parseExpression();
            if (pos < str.length()) {
                throw new Exception("Unexpected character: " + (char) ch);
            }
            return x;
        }

        // Parses addition and subtraction
        private double parseExpression() throws Exception {
            double x = parseTerm();
            while (true) {
                if (eat('+')) {
                    x += parseTerm(); // Add the next term
                } else if (eat('-')) {
                    x -= parseTerm(); // Subtract the next term
                } else {
                    return x; // No more + or -
                }
            }
        }

        // Parses multiplication and division
        private double parseTerm() throws Exception {
            double x = parseFactor();
            while (true) {
                if (eat('*')) {
                    x *= parseFactor(); // Multiply by the next factor
                } else if (eat('/')) {
                    x /= parseFactor(); // Divide by the next factor
                } else {
                    return x; // No more * or /
                }
            }
        }

        // Parses numbers, parentheses, and unary operators (+, -)
        private double parseFactor() throws Exception {
            if (eat('+')) {
                return parseFactor(); // Unary plus
            }
            if (eat('-')) {
                return -parseFactor(); // Unary minus
            }
            double x;
            int startPos = pos;
            if (eat('(')) {
                // If an open parenthesis is encountered, parse the expression inside
                x = parseExpression();
                if (!eat(')')) {
                    throw new Exception("Missing closing parenthesis");
                }
            } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                // Parse a number: continue until no digit or decimal point is found
                while ((ch >= '0' && ch <= '9') || ch == '.') {
                    nextChar();
                }
                String numberStr = str.substring(startPos, pos);
                try {
                    x = Double.parseDouble(numberStr);
                } catch (NumberFormatException e) {
                    throw new Exception("Invalid number: " + numberStr);
                }
            } else {
                // If no valid number or expression is found, throw an error
                throw new Exception("Unexpected character: " + (char) ch);
            }
            return x;
        }
    }
}