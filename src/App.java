import java.io.File;
import java.util.Formatter;
import java.util.Optional;
import java.util.Scanner;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
// import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
// import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class App extends Application {

    private DecisionTree decisionTree;
    private TextArea textArea;
    private TextField inputField;
    private Button playButton;
    private Button saveButton;
    private Button loadButton;
    private Button displayButton;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Animal Guessing  Game");

        decisionTree = new DecisionTree(new AnimalBehaviour());

        textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setWrapText(true);

        playButton = new Button("Play Game");
        playButton.setOnAction(e -> playGame());

        saveButton = new Button("Save Game");
        saveButton.setOnAction(e -> saveGame());

        loadButton = new Button("Load Game");
        loadButton.setOnAction(e -> loadGame());

        displayButton = new Button("Display Tree");
        displayButton.setOnAction(e -> displayTree());

        VBox hbox = new VBox();
        hbox.getChildren().addAll(playButton, saveButton, loadButton, displayButton);

        VBox vbox = new VBox();
        vbox.getChildren().addAll(textArea, hbox);

        Scene scene = new Scene(vbox, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void playGame() {

        textArea.clear(); // Clear the text area

        textArea.appendText("Think of an animal, and I will try to guess it!\n");
        if (decisionTree.isEmpty()) {
            // If the tree is empty, ask for the player's first animal using a dialog
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Animal Game");
            dialog.setHeaderText("Welcome to the Animal Game!");
            dialog.setContentText("Think of an animal: ");
            Optional<String> result = dialog.showAndWait();

            result.ifPresent(playerAnimal -> {
                // decisionTree.emptyTree(playerAnimal);
                TextInputDialog dialog1 = new TextInputDialog();
                dialog1.setContentText("you won ! play again");

            });
        } else {
            // If the tree is not empty, play the game
            textArea.appendText("I'm thinking of an animal...\n");
            boolean playerWins = decisionTree.execute();
            if (playerWins) {
                textArea.appendText("Congratulations! You win!\n");
            } else {
                textArea.appendText("I win! Let's play again.\n");
            }
            textArea.appendText("Press Play again to continue.\n");
        }
    }

    private void saveGame() {
        try {
            decisionTree.save("animal.txt");
            textArea.appendText("Game saved to 'animal.txt'.\n");
        } catch (Exception e) {
            textArea.appendText("Failed to save the game.\n");
            e.printStackTrace();
        }
    }

    private void loadGame() {
        try {
            decisionTree.load("animal.txt");
            textArea.appendText("Game loaded from 'animal.txt'.\n");
        } catch (Exception e) {
            textArea.appendText("Failed to load the game.\n");
            e.printStackTrace();
        }
    }

    private void displayTree() {
    };

    // DecistionTree class
    public class DecisionTree {
        private Node root;
        private IBehaviour behaviour;

        public DecisionTree(IBehaviour behaviour) {
            this.root = null;
            this.behaviour = behaviour;
        }

        public boolean isEmpty() {
            return root == null;
        }

        public void emptyTree(String playerAnimal) {
            root = behaviour.emptyTree(playerAnimal);
        }

        public boolean execute() {
            if (isEmpty()) {
                System.out.println("The tree is empty. Cannot play the game.");
                return false;
            }

            return execute(root);
        }

        private boolean execute(Node node) {
            if (node.isLeaf()) {
                boolean playerWins = behaviour.processLeafNode(node);
                return playerWins;
            } else {
                boolean answer = behaviour.processNonLeafNode(node);
                if (answer) {
                    return execute(node.getLeft());
                } else {
                    return execute(node.getRight());
                }
            }
        }

        public void save(String name) throws Exception {
            try (Formatter formatter = new Formatter(name)) {
                int labelCount = label(root, 1);
                formatter.format("%d\n", labelCount);
                save(root, formatter);
            }
        }

        private int label(Node node, int count) {
            if (node != null) {
                count = label(node.getLeft(), count);
                node.setLabel(count++);
                count = label(node.getRight(), count);
            }
            return count;
        }

        private void save(Node node, Formatter formatter) {
            if (node != null) {
                formatter.format("%d\n", node.getLabel());
                formatter.format("%s\n", node.getData());
                save(node.getLeft(), formatter);
                save(node.getRight(), formatter);
            }
        }

        public void load(String fname) throws Exception {
            try (Scanner scanner = new Scanner(new File(fname))) {
                int labelCount = Integer.parseInt(scanner.nextLine().trim());
                root = load(scanner, 1, labelCount);
            }
        }

        private Node load(Scanner scanner, int minLabel, int maxLabel) {
            if (scanner.hasNext()) {
                int label = Integer.parseInt(scanner.nextLine().trim());
                String data = scanner.nextLine().trim();
                Node node = new Node(data);
                node.setLabel(label);

                if (minLabel <= label && label <= maxLabel) {
                    node.setLeft(load(scanner, minLabel, label - 1));
                    node.setRight(load(scanner, label + 1, maxLabel));
                }

                return node;
            }

            return null;
        }

        public IBehaviour getBehaviour() {
            return behaviour;
        }
    }

    // Ibehaviour interface
    public interface IBehaviour {
        Node emptyTree(String playerAnimal);

        boolean processNonLeafNode(Node node);

        boolean processLeafNode(Node node);

        public void closeScanner();

        String ask(String question);
    }

    public class AnimalBehaviour implements IBehaviour {
        private Scanner scanner;

        public AnimalBehaviour() {
            // Initialize any required resources
            scanner = new Scanner(System.in);
        }

        @Override
        public Node emptyTree(String question) {
            // Implement behavior for an empty tree
            System.out.println("Welcome to the Animal Game!");
            System.out.print("Think of an animal: ");
            String animal = scanner.nextLine();

            // Create and return the root node
            return new Node(animal);
        }

        @Override
        public boolean processNonLeafNode(Node node) {
            // Implement behavior for processing a non-leaf node
            String question = node.getData();
            System.out.print(question + " (yes/no): ");
            String answer = scanner.nextLine().toLowerCase();

            // Return true if the answer is "yes" and false otherwise
            return answer.equals("yes");
        }

        @Override
        public boolean processLeafNode(Node node) {
            // Implement behavior for processing a leaf node
            String guessedAnimal = node.getData();
            System.out.print("Is your animal a " + guessedAnimal + "? (yes/no): ");
            String answer = scanner.nextLine().toLowerCase();

            if (answer.equals("yes")) {
                System.out.println("I guessed it! I win!");
                return true; // Computer wins
            } else {
                // Player wins, ask for the correct animal and a differentiating question
                System.out.print("You win! What is your animal? ");
                String playerAnimal = scanner.nextLine();
                System.out
                        .print("Please provide a question to differentiate your animal from a " + guessedAnimal + ": ");
                String question = scanner.nextLine();

                // Extend the tree with the player's animal and question
                node.extend(question, playerAnimal, guessedAnimal);

                System.out.println("Thanks for playing!");
                return false; // Player wins
            }
        }

        @Override
        public void closeScanner() {
            if (scanner != null) {
                scanner.close();
            }
        }

        @Override
        public String ask(String question) {
            // Use the scanner or other input method to ask the question and get the
            // player's response
            System.out.print(question + " "); // Display the question
            String response = scanner.nextLine(); // Read the player's response
            return response;
        }
    }

    // NOde class
    public class Node {
        private String data;
        private int label;
        private Node left;
        private Node right;

        public Node(String data) {
            this.data = data;
            this.label = 0;
            this.left = null;
            this.right = null;
        }

        public Node(String data, int label) {
            this.data = data;
            this.label = label;
            this.left = null;
            this.right = null;
        }

        public boolean isLeaf() {
            return left == null && right == null;
        }

        public Node getLeft() {
            return left;
        }

        public Node getRight() {
            return right;
        }

        public String getData() {
            return data;
        }

        public int getLabel() {
            return label;
        }

        public void setLabel(int label) {
            this.label = label;
        }

        public void setLeft(Node left) {
            this.left = left;
        }

        public void setRight(Node right) {
            this.right = right;
        }

        public String getQuestion() {
            if (isLeaf()) {
                return "Is your animal a(n) " + data + "?";
            } else {
                return data;
            }
        }

        public void extend(String newQuestion, String leftAnimal, String rightAnimal) {
            this.data = newQuestion;
            this.left = new Node(leftAnimal);
            this.right = new Node(rightAnimal);
        }
    }

    public class Game {
        private DecisionTree tree;
        private TextArea textArea;

        public Game(IBehaviour behaviour, TextArea textArea) {
            this.textArea = textArea;
            tree = new DecisionTree(behaviour);
        }

        public void play() {
            appendToTextArea("Welcome to the Animal Game!");

            while (true) {
                boolean playerWins = tree.execute();

                if (playerWins) {
                    appendToTextArea("Congratulations! You win!");
                } else {
                    appendToTextArea("I win! Better luck next time.");
                }

                String playAgain = tree.getBehaviour().ask("Do you want to play again? (yes/no): ");

                if (!playAgain.equalsIgnoreCase("yes")) {
                    appendToTextArea("Thanks for playing!");
                    break;
                }
            }

            saveGame();
        }

        public void saveGame() {
            try {
                tree.save("animal.txt");
                appendToTextArea("Game saved to 'animal.txt'.");
            } catch (Exception e) {
                appendToTextArea("Failed to save the game.");
                e.printStackTrace();
            }
        }

        public void loadGame() {
            try {
                tree.load("animal.txt");
                appendToTextArea("Game loaded from 'animal.txt'.");
            } catch (Exception e) {
                appendToTextArea("Failed to load the game.");
                e.printStackTrace();
            }
        }

        public void close() {
            tree.getBehaviour().closeScanner();
        }

        private void appendToTextArea(String message) {
            textArea.appendText(message + "\n");
        }
    }

    // Add the missing methods and functionality as per your assignment requirements
}