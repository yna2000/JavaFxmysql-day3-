package com.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

public class MainApp extends Application {
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/login_schema";
    private static final String USER = "root";
    private static final String PASSWORD = "ricky";

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Login Form");

        VBox vbox = new VBox(20);
        vbox.setPadding(new Insets(30, 50, 30, 50));
        vbox.setAlignment(Pos.CENTER);
        vbox.getStyleClass().add("vbox");


        try {
            InputStream imageStream = getClass().getResourceAsStream("/background.jpg");
            if (imageStream == null) {
                System.out.println("Background image not found.");
            } else {
                Image backgroundImage = new Image(imageStream);
                if (backgroundImage.getWidth() > 0 && backgroundImage.getHeight() > 0) {
                    BackgroundImage bgImage = new BackgroundImage(backgroundImage,
                            BackgroundRepeat.NO_REPEAT,
                            BackgroundRepeat.NO_REPEAT,
                            BackgroundPosition.CENTER,
                            new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, true, true, true, true));
                    vbox.setBackground(new Background(bgImage));
                } else {
                    System.out.println("Background image has zero width or height.");
                }
            }
        } catch (Exception e) {
            System.out.println(STR."Error loading background image: \{e.getMessage()}");
        }

        ImageView imageView = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Login.png"))));
        imageView.setFitHeight(100);
        imageView.setFitWidth(100);

        Label titleLabel = new Label("Login Form");
        titleLabel.getStyleClass().add("title-label");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setAlignment(Pos.CENTER);
        grid.getStyleClass().add("grid-pane");

        Label usernameLabel = new Label("Username:");
        usernameLabel.getStyleClass().add("label");
        GridPane.setConstraints(usernameLabel, 0, 0);
        TextField usernameField = new TextField();
        usernameField.getStyleClass().add("text-field");
        GridPane.setConstraints(usernameField, 1, 0);

        Label passwordLabel = new Label("Password:");
        passwordLabel.getStyleClass().add("label");
        GridPane.setConstraints(passwordLabel, 0, 1);
        PasswordField passwordField = new PasswordField();
        passwordField.getStyleClass().add("text-field");
        GridPane.setConstraints(passwordField, 1, 1);

        grid.getChildren().addAll(usernameLabel, usernameField, passwordLabel, passwordField);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        Button loginButton = new Button("Login");
        loginButton.getStyleClass().add("button");
        loginButton.setOnAction(_ -> performLogin(usernameField, passwordField));
        Button registerButton = new Button("Register");
        registerButton.getStyleClass().add("button");
        registerButton.setOnAction(_ -> showRegistrationDialog());
        Button adminButton = new Button("Admin Login");
        adminButton.getStyleClass().add("button");
        adminButton.setOnAction(_ -> showAdminDialog());
        buttonBox.getChildren().addAll(loginButton, registerButton, adminButton);

        vbox.getChildren().addAll(imageView, titleLabel, grid, buttonBox);

        Scene scene = new Scene(vbox, 400, 500);
        String css = Objects.requireNonNull(this.getClass().getResource("/styles.css")).toExternalForm();
        scene.getStylesheets().add(css);

        primaryStage.setScene(scene);
        primaryStage.show();
    }



    private void performLogin(TextField usernameField, PasswordField passwordField) {
        String usernameInput = usernameField.getText();
        String passwordInput = passwordField.getText();
        if (authenticateUser(usernameInput, passwordInput)) {
            if (isAdmin(usernameInput)) {
                showAlert(Alert.AlertType.INFORMATION, "Login Successful", "Admin login successful!");
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Login Successful", "User login successful!");
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid username or password.");
        }
    }

    private boolean authenticateUser(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isAdmin(String username) {
        String query = "SELECT is_admin FROM users WHERE username = ?";
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getBoolean("is_admin");
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void showRegistrationDialog() {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Register");
        dialog.setHeaderText("Register New User");

        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();
        PasswordField confirmPasswordField = new PasswordField();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(new Label("Confirm Password:"), 0, 2);
        grid.add(confirmPasswordField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        ButtonType okButtonType = new ButtonType("Register", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                return new Pair<>(usernameField.getText(), passwordField.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();
        if (result.isPresent()) {
            String usernameInput = result.get().getKey();
            String passwordInput = result.get().getValue();
            String confirmPasswordInput = confirmPasswordField.getText();

            if (passwordInput.equals(confirmPasswordInput)) {
                if (addUserToDatabase(usernameInput, passwordInput)) {
                    showAlert(Alert.AlertType.INFORMATION, "Registration Successful", "User registered successfully!");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Registration Failed", "User registration failed. Maybe the username already exists.");
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Registration Failed", "Passwords do not match.");
            }
        }
    }

    private boolean addUserToDatabase(String username, String password) {
        String query = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            statement.setString(2, password);
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void showAdminDialog() {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Admin Login");
        dialog.setHeaderText("Admin Login");

        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passwordField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        ButtonType okButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                return new Pair<>(usernameField.getText(), passwordField.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();
        if (result.isPresent()) {
            String usernameInput = result.get().getKey();
            String passwordInput = result.get().getValue();

            if (authenticateUser(usernameInput, passwordInput) && isAdmin(usernameInput)) {
                showAlert(Alert.AlertType.INFORMATION, "Admin Login Successful", "Admin login successful!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Admin Login Failed", "Invalid admin username or password.");
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.verbose", "true");
        launch(args);
    }
    }