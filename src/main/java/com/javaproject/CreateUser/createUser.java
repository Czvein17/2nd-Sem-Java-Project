package com.javaproject.CreateUser;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JOptionPane;

import com.javaproject.ConnectDB;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class createUser {
    @FXML
    private Label ErrorUsername,
            errorEmail,
            errorPassword,
            errorPassMatch,
            tfShowPassword;

    @FXML
    private CheckBox showPassword;

    @FXML
    private TextField tfUsername,
            tfEmail,
            tfBirthday,
            tfPassword,
            tfConfirmPassword;

    @FXML
    private TextField tfShowPassword11;

    @FXML
    private PasswordField Pass;

    @FXML
    private Button btnCreate,
            btnReturn;

    private void validateAndSetError(
            String input, Label errorLabel,
            String errorMessage,
            boolean requireSpecialChar,
            String confirmPassword) {
        if (input.isEmpty()) {
            errorLabel.setText(errorMessage + " cannot be blank");
            System.out.println(errorMessage + " field is blank");

        } else if (errorMessage.equals("Username") && !requireSpecialChar && input.matches(".*\\W+.*")) {
            errorLabel.setText("Username should not contain special characters");
            System.out.println("Username contains a special character");

        } else if (errorMessage.equals("Email") && !input.matches("^(.+)@(.+)$")) {
            errorLabel.setText("Use a valid email address");
            System.out.println("Email is not a valid email address");

        } else if (errorMessage.equals("Password")) {
            if (!input.matches("^(?=.*\\W).{8,}$")) {
                errorLabel
                        .setText("Password should be at least 8 characters and contain at least one special character");

            } else if (!input.equals(confirmPassword)) {
                errorPassMatch.setText("Password doesn't match");
                System.out.println("Password doesn't match");

            } else {
                errorLabel.setText(""); // Clear the error message from the private label
                errorPassMatch.setText(""); // Clear the error message from the private label
                System.out.println(errorMessage + " is valid");
            }
        } else {
            errorLabel.setText(""); // Clear the error message from the private label
            System.out.println(errorMessage + " is valid");
        }
    }

    @FXML
    void onCreate(ActionEvent event) {
        String username = tfUsername.getText();
        String email = tfEmail.getText();
        String password = tfPassword.getText();
        String confirmPassword = tfConfirmPassword.getText();
        String birthday = tfBirthday.getText();

        // In your onCreate method
        validateAndSetError(username, ErrorUsername, "Username", false, "");
        validateAndSetError(email, errorEmail, "Email", false, "");
        validateAndSetError(password, errorPassword, "Password", true, confirmPassword);

        // Check if all validation checks pass
        if (!username.matches(".*\\W+.*") && email.matches("^(.+)@(.+)$")
                && (password.length() >= 8 && password.length() <= 60) && password.equals(confirmPassword)) {
            try {
                ConnectDB connectDB = new ConnectDB();
                Connection connection = connectDB.getConnection();

                // Check for existing username or email
                String checkQuery = "SELECT * FROM javaproject WHERE username = ? OR email = ?";
                PreparedStatement checkStatement = connection.prepareStatement(checkQuery);
                checkStatement.setString(1, username);
                checkStatement.setString(2, email);
                ResultSet resultSet = checkStatement.executeQuery();

                if (resultSet.next()) {
                    // Username or email already exists in the database
                    System.out.println("Username or email already has an account");
                    JOptionPane.showMessageDialog(null, "Username or email already has an account", "Error",
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    // Username and email are unique, proceed with insertion
                    String insertQuery = "INSERT INTO javaproject.users (username, email, pass, birthday, role) VALUES (?, ?, ?, ?, 'user')";
                    PreparedStatement statement = connection.prepareStatement(insertQuery);
                    statement.setString(1, username);
                    statement.setString(2, email);
                    statement.setString(3, password);
                    statement.setString(4, birthday);
                    statement.executeUpdate();

                    JOptionPane.showMessageDialog(null, "Data successfully inserted into the database", "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                // Handle database connection or query execution errors
            }
        } else {
            // Validation checks did not pass
            System.out.println("Validation checks did not pass. Data not inserted into the database.");
        }
    }

    @FXML
    void initialize() {
        showPassword.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            tfShowPassword.setVisible(isSelected);
            tfShowPassword11.setVisible(isSelected);

        });

        tfPassword.textProperty().addListener((obs, oldText, newText) -> {
            if (showPassword.isSelected()) {
                tfShowPassword.setText(newText);
                tfShowPassword11.setText(newText);
            }
        });

        tfShowPassword11.textProperty().addListener((obs, oldText, newText) -> {
            if (showPassword.isSelected()) {
                tfPassword.setText(newText);
                tfShowPassword.setText(newText);
            }
        });
    }

    @FXML
    void onShow(ActionEvent event) {
        boolean isSelected = showPassword.isSelected();

        // Toggle visibility based on showPassword state
        tfShowPassword.setVisible(isSelected);
        tfShowPassword.setManaged(isSelected);
        tfShowPassword11.setVisible(isSelected);
        tfShowPassword11.setManaged(isSelected);

        // if (!tfPassword.isVisible()) {
        // errorPassword.setVisible(false);
        // errorPassword.setManaged(false);
        // } else {
        // // Always show the errorPassword label
        // errorPassword.setVisible(true);
        // errorPassword.setManaged(true);
        // }

        tfShowPassword11.setText(tfPassword.getText());

        if (isSelected) {
            tfShowPassword.setText(tfPassword.getText());
        } else {
            tfShowPassword.setText("");
        }
    }

    @FXML
    void onReturn(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/javaproject/login.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();

            Stage currentPage = (Stage) btnReturn.getScene().getWindow();
            currentPage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
