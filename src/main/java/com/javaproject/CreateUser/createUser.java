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
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class createUser {
    @FXML
    private Label ErrorUsername, errorEmail, errorPassword, errorPassMatch;

    @FXML
    private Button btnCreate;

    @FXML
    private Button btnReturn;

    @FXML
    private TextField tfBirthday;

    @FXML
    private TextField tfConfirmPassword;

    @FXML
    private TextField tfEmail;

    @FXML
    private TextField tfPassword;

    @FXML
    private TextField tfUsername;

    private void validateAndSetError(String input, Label errorLabel, String errorMessage, boolean requireSpecialChar) {
        if (input.isEmpty()) {
            errorLabel.setText(errorMessage + " cannot be blank");
            System.out.println(errorMessage + " field is blank");

        } else if (errorMessage.equals("Username") && !requireSpecialChar && input.matches(".*\\W+.*")) {
            errorLabel.setText("Username should not contain special characters");
            System.out.println("Username contains a special character");

        } else if (errorMessage.equals("Email") && !input.matches("^(.+)@(.+)$")) {
            errorLabel.setText("Use a valid email address");
            System.out.println("Email is not a valid email address");

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

        // if (username.isEmpty()) {
        // ErrorUsername.setText("Username cannot be blank");
        // } else if (username.matches(".*\\W+.*")) {
        // ErrorUsername.setText("Cannot Contain Special Character");
        // System.out.println("Username contains a special character");
        // } else {
        // System.out.println("Username is valid");
        // }

        // if (email.isEmpty()) {
        // errorEmail.setText("Email cannot be blank");
        // } else if (!email.matches("^(.+)@(.+)$")) {
        // errorEmail.setText("Use a valid email address");
        // System.out.println("Email is not valid");
        // } else {
        // System.out.println("Email is valid");
        // }

        // if (password.isEmpty()) {
        // errorPassword.setText("Password cannot be blank");
        // } else if (password.length() < 8 || password.length() > 60) {
        // // password does not meet length requirements
        // errorPassword.setText("Should contain 8 charaters and special character");
        // System.out.println("Password should be atleast 8 charaters and containin
        // special charactes");
        // } else {
        // if (!password.equals(confirmPassword)) {
        // // password and confirm password do not match
        // errorPassMatch.setText("Password doesn't match");
        // System.out.println("Password and confirm password do not match");
        // } else {
        // // password and confirm password match
        // System.out.println("Password and confirm password match");
        // }
        // }

        // In your onCreate method
        validateAndSetError(username, ErrorUsername, "Username", false);
        validateAndSetError(email, errorEmail, "Email", false);
        validateAndSetError(password, errorPassword, "Password", true);

        if (password.isEmpty()) {
            errorPassword.setText("Password cannot be blank");
        } else if (!password.matches("^(?=.*\\W).{8,}$")) {
            errorPassword
                    .setText("Password should be at least 8 characters and contain at least one special character");
        } else {
            if (!password.equals(confirmPassword)) {
                // password and confirm password do not match
                errorPassMatch.setText("Password doesn't match");
            } else {
                // password and confirm password match
                errorPassMatch.setText(""); // Clear the error message from the private label
            }
        }

        // Check if all validation checks pass
        if (!username.matches(".*\\W+.*") && email.matches("^(.+)@(.+)$")
                && (password.length() >= 8 && password.length() <= 60) && password.equals(confirmPassword)) {
            try {
                ConnectDB connectDB = new ConnectDB();
                Connection connection = connectDB.getConnection();

                // Check for existing username or email
                String checkQuery = "SELECT * FROM guidb.users WHERE username = ? OR email = ?";
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
                    String insertQuery = "INSERT INTO guidb.users (username, email, pass, birthday, role) VALUES (?, ?, ?, ?, 'user')";
                    PreparedStatement statement = connection.prepareStatement(insertQuery);
                    statement.setString(1, username);
                    statement.setString(2, email);
                    statement.setString(3, password);
                    statement.setString(4, birthday);
                    statement.executeUpdate();
                    System.out.println("Data successfully inserted into the database");
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
    void onReturn(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/javaproject/login.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();

            // Close the current page
            Stage currentPage = (Stage) btnReturn.getScene().getWindow();
            currentPage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
