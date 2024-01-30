package com.javaproject.Login;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.javaproject.ConnectDB;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class loginController {

    Connection con;
    PreparedStatement pst;
    ResultSet rs;

    @FXML
    private Button btnCreate, onLogin;

    @FXML
    private CheckBox onShowBTN;

    @FXML
    private Label errorUser, errorPassword, remainingTimeLabel;

    @FXML
    private PasswordField tfPassword;

    @FXML
    private TextField tfUsername, ShowPassword;

    private loginHandler loginHandler = new loginHandler(); // Create an instance of the LoginHandler class

    private Timeline remainingTimeUpdater;
    private int remainingSeconds;
    private int loginAttempts = 0;
    // private long lastLoginAttemptTime = 0;

    @FXML
    void onClickLogin(javafx.event.ActionEvent event) {
        String username = tfUsername.getText();
        String email = tfUsername.getText(); // Assuming this is intentional
        String password = tfPassword.getText();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            errorUser.setText("user or email or password is empty");
            errorPassword.setText("");
            // Set a 3-second delay before clearing the error messages
            scheduleErrorClearing();
        } else {
            if (remainingSeconds > 0) {
                loginHandler.showErrorDialog("Cannot login, please wait for the remaining time.");
            } else {
                try {
                    Connection connection = new ConnectDB().getConnection();
                    PreparedStatement pst = connection.prepareStatement(
                            "SELECT * FROM javaproject.users WHERE (email=? OR username=? )");
                    pst.setString(1, email);
                    pst.setString(2, username);
                    ResultSet rs = pst.executeQuery();
                    if (rs.next()) {
                        String storedPassword = rs.getString("pass");
                        if (password.equals(storedPassword)) {
                            String role = rs.getString("role");
                            loginHandler.handleRoleRedirection(role, email, username, password, onLogin);
                        } else {
                            errorUser.setText("");
                            errorPassword.setText("Incorrect Password"); // Set the error message for incorrect password
                            loginHandler.clearAndFocusUsername(tfUsername, tfPassword);
                            scheduleErrorClearing();
                            incrementLoginAttempts();
                        }
                    } else {
                        errorUser.setText("Invalid Email or Username");
                        errorPassword.setText("Invalid Password");
                        // loginHandler.clearAndFocusUsername(tfUsername, tfPassword);
                        scheduleErrorClearing();
                        incrementLoginAttempts();
                    }
                } catch (SQLException | IOException ex) {
                    loginHandler.handleException(ex);
                }
            }
        }
    }

    private void incrementLoginAttempts() {
        loginAttempts++;

        if (loginAttempts >= 5) {
            remainingSeconds = 60; // Set the remaining seconds to 60 (1 minute)
            remainingTimeLabel.setText("You can login after " + remainingSeconds + " seconds");
            remainingTimeUpdater = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateRemainingTime()));
            remainingTimeUpdater.setCycleCount(Timeline.INDEFINITE);
            remainingTimeUpdater.play();
            loginHandler.showErrorDialog("Exceeded maximum login attempts. Please try again later.");

        }
    }

    // private void disableLoginControls() {
    // // You can add logic here to disable login-related controls
    // // For example, you can disable the login button or text fields during the
    // // cooldown
    // tfUsername.setDisable(true);
    // tfPassword.setDisable(true);
    // // ... add more controls if needed
    // }

    // private void enableLoginControls() {
    // // You can add logic here to enable login-related controls
    // // For example, you can enable the login button or text fields after the
    // // cooldown
    // tfUsername.setDisable(false);
    // tfPassword.setDisable(false);
    // // ... add more controls if needed
    // }

    private void updateRemainingTime() {
        remainingSeconds--;

        if (remainingSeconds > 0) {
            remainingTimeLabel.setText("You can login after " + remainingSeconds + " seconds");
        } else {
            remainingTimeLabel.setText(""); // Clear the remaining time label
            remainingTimeUpdater.stop();
            // enableLoginControls(); // Enable login controls after the cooldown
            resetLoginAttempts();
        }
    }

    private void resetLoginAttempts() {
        loginAttempts = 0;
        remainingTimeLabel.setText(""); // Clear the remaining time label
        remainingTimeUpdater.stop();
    }

    @FXML
    void initialize() {
        onShowBTN.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            ShowPassword.setVisible(isSelected);
            tfPassword.setVisible(!isSelected); // Toggle visibility of tfPassword
            tfPassword.setManaged(!isSelected); // Toggle manageability of tfPassword
        });

        tfPassword.textProperty().addListener((obs, oldText, newText) -> {
            if (onShowBTN.isSelected()) {
                ShowPassword.setText(newText);
            }
        });

        ShowPassword.textProperty().addListener((obs, oldText, newText) -> {
            if (onShowBTN.isSelected()) {
                tfPassword.setText(newText);
            }
        });

        ShowPassword.setOnAction(event -> {
            if (onShowBTN.isSelected()) {
                tfPassword.setText(ShowPassword.getText());
                ShowPassword.setText(ShowPassword.getText());
            } else {
                tfPassword.setText("");
                ShowPassword.setText("");
            }
        });
    }

    @FXML
    void onShow(ActionEvent event) {
        boolean isSelected = onShowBTN.isSelected();

        // Toggle visibility of tfPassword based on showPassword state
        tfPassword.setVisible(!isSelected);
        tfPassword.setManaged(!isSelected);

        if (!tfPassword.isVisible()) {
            errorPassword.setVisible(true);
            errorPassword.setManaged(true);
        } else {
            // Always show the errorPassword label
            errorPassword.setVisible(true);
            errorPassword.setManaged(true);
        }

        if (isSelected) {
            ShowPassword.setText(tfPassword.getText());
        } else {
            ShowPassword.setText("");
        }
    }

    // New method to schedule error clearing after 5 seconds
    private void scheduleErrorClearing() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), e -> {
            errorUser.setText("");
            errorPassword.setText("");
        }));
        timeline.play();
    }

    @FXML
    void onCreate(ActionEvent event) throws IOException {
        // Close the current stage
        Node source = (Node) event.getSource();
        Stage stageToClose = (Stage) source.getScene().getWindow();
        stageToClose.close();

        // Redirect to manager page
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/javaproject/CreateUser/createUser.fxml"));
        Parent root = loader.load();

        Stage newStage = new Stage();
        newStage.setScene(new Scene(root));
        newStage.show();
    }
}
