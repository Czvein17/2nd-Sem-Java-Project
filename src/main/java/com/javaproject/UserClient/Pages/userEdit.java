package com.javaproject.UserClient.Pages;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JOptionPane;

import com.javaproject.ConnectDB;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

public class userEdit {
    public Connection dataConnect() {
        return new ConnectDB().getConnection();
    }

    private String loggedInUserId;

    // Setter for logged-in user ID
    public void setLoggedInUserId(String loggedInUserId) {
        this.loggedInUserId = loggedInUserId;
        System.out.println("USER EDIT Logged-in user ID: " + loggedInUserId);
        retieveData();
    }

    // Initialize method
    public void retieveData() {
        if (loggedInUserId != null) {
            try (Connection connection = dataConnect();
                    PreparedStatement statement = connection
                            .prepareStatement("SELECT * FROM javaproject.users WHERE id = ?")) {
                statement.setString(1, loggedInUserId);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    String userName = resultSet.getString("username");
                    String email = resultSet.getString("email");
                    String password = resultSet.getString("pass");
                    updateUsername.setPromptText(userName);
                    updateEmail.setPromptText(email);
                    updatePassword.setPromptText(password);
                    updateCfrmPassword.setPromptText(password);
                    shwUpdatePassword.setPromptText(password);

                    Blob imageBlob = resultSet.getBlob("image");
                    if (imageBlob != null) {
                        InputStream inputStream = imageBlob.getBinaryStream();
                        Image image = new Image(inputStream);
                        phImage.setImage(image);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            // Handle the case where loggedInUserId is null
        }
    }

    // FXML elements
    @FXML
    private Button imageUpload;
    @FXML
    private ImageView phImage;

    @FXML
    private Button onUpdateBTN;

    @FXML
    private TextField shwUpdatePassword;

    @FXML
    private PasswordField updateCfrmPassword;

    @FXML
    private TextField updateEmail;

    @FXML
    private TextField updateBirthday;

    @FXML
    private PasswordField updatePassword;

    @FXML
    private TextField updateUsername;

    private File selectedImageFile;

    @FXML
    void onUploadImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().add(new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

        File selectedImageFile = fileChooser.showOpenDialog(imageUpload.getScene().getWindow());

        if (selectedImageFile != null) {
            int choice = JOptionPane.showConfirmDialog(null, "Do you want to update the profile picture?", "Confirm",
                    JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                try {
                    Connection connection = dataConnect();
                    String query = "UPDATE `javaproject`.`users` SET image=? WHERE (`id` = ?)";
                    FileInputStream fileInputStream = new FileInputStream(selectedImageFile);
                    PreparedStatement preparedStatement = connection.prepareStatement(query);
                    preparedStatement.setBinaryStream(1, fileInputStream, (int) selectedImageFile.length());
                    preparedStatement.setString(2, loggedInUserId);
                    preparedStatement.executeUpdate();
                    System.out.println("Profile picture updated successfully");
                    preparedStatement.close();
                    connection.close();

                    // Update the ImageView to display the newly uploaded profile picture
                    Image image = new Image(selectedImageFile.toURI().toString());
                    System.out.println("New image URL: " + selectedImageFile.toURI().toString());
                    phImage.setImage(image);
                } catch (IOException | SQLException e) {
                    System.out.println("Error updating profile picture: " + e.getMessage());
                    // Handle the exception as per your application's requirements
                }
            }
        }
    }

    @FXML
    private Label errorUsername, errorEmail, errorPassword, errorPassMatch;

    @FXML
    void onUpdate(ActionEvent event) {
        String password = JOptionPane.showInputDialog("Enter your password:");
        if (password != null && !password.isEmpty()) {
            // Validate the entered password against the user's password in the database
            if (validatePassword(loggedInUserId, password)) {
                // Password match, proceed with the update
                String updatedUsername = updateUsername.getText();
                String updatedEmail = updateEmail.getText();
                String updatedBirthday = updateBirthday.getText();

                System.out.println("Check Logged In User ID: " + loggedInUserId);
                updateUserData(
                        loggedInUserId,
                        updatedUsername,
                        updatedEmail,
                        updatedBirthday,
                        errorUsername,
                        errorEmail,
                        selectedImageFile);
            } else {
                // Handle the case where the entered password does not match the user's password
                System.out.println("Entered password does not match the user's password");
            }
        } else {
            // Handle the case where the user cancels the input or leaves the password field
            // empty
            System.out.println("Password input was cancelled or empty");
        }
    }

    boolean validatePassword(String userId, String enteredPassword) {
        // Replace this with the actual database query to fetch the user's password
        try {
            Connection connection = dataConnect();
            PreparedStatement preparedStatement = connection
                    .prepareStatement("SELECT pass FROM users WHERE id = ?");
            preparedStatement.setString(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String userPassword = resultSet.getString("pass");
                return userPassword.equals(enteredPassword);
            } else {
                // Handle the case where the user ID is not found in the database
                return false;
            }
        } catch (SQLException e) {
            // Handle any potential exceptions related to the database query
            e.printStackTrace();
            return false;
        }
    }

    public void updateUserData(
            String loggedInUserId,
            String updatedUsername,
            String updatedEmail,
            String updatedBirthday,
            Label usernameErrorLabel,
            Label emailErrorLabel,
            File selectedImageFile) {

        if (isUserDataValid(updatedUsername, updatedEmail, usernameErrorLabel, emailErrorLabel)) {
            try {
                Connection connection = dataConnect();
                String query;
                PreparedStatement preparedStatement;

                if (selectedImageFile != null) {
                    query = "UPDATE `javaproject`.`users` SET username = COALESCE(?, username), " +
                            "email = COALESCE(?, email), birthday = COALESCE(?, birthday), image = ? WHERE (`id` = ?)";
                    FileInputStream fileInputStream = new FileInputStream(selectedImageFile);
                    preparedStatement = connection.prepareStatement(query);
                    preparedStatement.setString(1, updatedUsername.isEmpty() ? null : updatedUsername);
                    preparedStatement.setString(2, updatedEmail.isEmpty() ? null : updatedEmail);
                    preparedStatement.setString(3, updatedBirthday.isEmpty() ? null : updatedBirthday);
                    preparedStatement.setBinaryStream(4, fileInputStream, (int) selectedImageFile.length());
                    preparedStatement.setString(5, loggedInUserId);

                } else {
                    query = "UPDATE `javaproject`.`users` SET username = COALESCE(?, username), " +
                            "email = COALESCE(?, email), birthday = COALESCE(?, birthday) WHERE (`id` = ?)";
                    preparedStatement = connection.prepareStatement(query);
                    preparedStatement.setString(1, updatedUsername.isEmpty() ? null : updatedUsername);
                    preparedStatement.setString(2, updatedEmail.isEmpty() ? null : updatedEmail);
                    preparedStatement.setString(3, updatedBirthday.isEmpty() ? null : updatedBirthday);
                    preparedStatement.setString(4, loggedInUserId);

                    JOptionPane.showMessageDialog(null, "Data updated");
                }

                preparedStatement.executeUpdate();
                System.out.println("User data updated successfully");
                preparedStatement.close();
                connection.close();
            } catch (IOException | SQLException e) {
                System.out.println("Error updating user data: " + e.getMessage());
                // Handle the exception as per your application's requirements
            }
        } else {
            // Handle validation errors (e.g., display error messages to the user)
        }
    }

    private boolean isUserDataValid(String username, String email, Label usernameErrorLabel, Label emailErrorLabel) {
        boolean isUsernameValid = isUsernameValid(username, usernameErrorLabel);
        boolean isEmailUnique = isEmailUnique(email, emailErrorLabel);
        return isUsernameValid && isEmailUnique;
    }

    private boolean isUsernameValid(String username, Label usernameErrorLabel) {
        if (!username.isBlank()) {
            if (!isFieldValid("username", username)) {
                Platform.runLater(() -> {
                    usernameErrorLabel.setText("Username is already used");
                    PauseTransition pause = new PauseTransition(Duration.seconds(5));
                    pause.setOnFinished(event -> usernameErrorLabel.setText(""));
                    pause.play();
                });
                return false;
            }
            if (!username.matches(".*[a-zA-Z].*") || username.matches(".*[^a-zA-Z0-9].*")) {
                Platform.runLater(() -> {
                    usernameErrorLabel.setText("Invalid username format");
                    PauseTransition pause = new PauseTransition(Duration.seconds(5));
                    pause.setOnFinished(event -> usernameErrorLabel.setText(""));
                    pause.play();
                });
                return false;
            }
        }
        Platform.runLater(() -> usernameErrorLabel.setText(""));
        return true;
    }

    private boolean isEmailUnique(String email, Label emailErrorLabel) {
        if (!email.isBlank()) {
            if (!isFieldValid("email", email)) {
                Platform.runLater(() -> {
                    emailErrorLabel.setText("Email is already used");
                    PauseTransition pause = new PauseTransition(Duration.seconds(5));
                    pause.setOnFinished(event -> emailErrorLabel.setText(""));
                    pause.play();
                });
                return false;
            }
            if (!email.contains("@") || !email.contains(".")) {
                Platform.runLater(() -> {
                    emailErrorLabel.setText("Invalid email format");
                    PauseTransition pause = new PauseTransition(Duration.seconds(5));
                    pause.setOnFinished(event -> emailErrorLabel.setText(""));
                    pause.play();
                });
                return false;
            }
        }
        Platform.runLater(() -> emailErrorLabel.setText(""));
        return true;
    }

    // Method to check if a field is unique in the database
    private boolean isFieldValid(String fieldName, String value) {
        try {
            Connection connection = dataConnect();
            String checkQuery = "SELECT * FROM javaproject.users WHERE " + fieldName + " = ?";
            PreparedStatement checkStatement = connection.prepareStatement(checkQuery);
            checkStatement.setString(1, value);
            ResultSet resultSet = checkStatement.executeQuery();
            if (resultSet.next()) {
                System.out.println(fieldName + " is not unique");
                return false;
            }
            return true;
        } catch (SQLException e) {
            System.out.println("Error checking " + fieldName + " uniqueness: " + e.getMessage());
            return false;
        }
    }

}