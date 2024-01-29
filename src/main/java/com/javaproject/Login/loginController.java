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
    private Label errorUser, errorPassword;

    @FXML
    private PasswordField tfPassword;

    @FXML
    private TextField tfUsername, ShowPassword;

    private loginHandler loginHandler = new loginHandler(); // Create an instance of the LoginHandler class

    @FXML
    private Label remainingTimeLabel;
    private Timeline remainingTimeUpdater;
    private int remainingSeconds;

    private int loginAttempts = 0;
    private long lastLoginAttemptTime = 0;

    @FXML
    void onClickLogin(javafx.event.ActionEvent event) {
        String username = tfUsername.getText();
        String email = tfUsername.getText();
        String password = tfPassword.getText();

        if (email.isEmpty() || password.isEmpty()) {
            // loginHandler.showErrorDialog("email and password cannot be blank");
            errorUser.setText("user or email is empty");
            errorPassword.setText("password is empty");
            // Set a 3-second delay before clearing the error messages
            scheduleErrorClearing();

        } else {
            if (remainingSeconds > 0) {
                loginHandler.showErrorDialog("Cannot login, please wait for the remaining time.");
            } else {
                try {
                    Connection connection = new ConnectDB().getConnection();
                    PreparedStatement pst = connection
                            .prepareStatement(
                                    "SELECT * FROM javaproject.users WHERE (email=? OR username=? ) AND pass=?");
                    pst.setString(1, email);
                    pst.setString(2, username);
                    pst.setString(3, password);
                    ResultSet rs = pst.executeQuery();

                    if (rs.next()) {
                        String storedPassword = rs.getString("pass");
                        if (!password.equals(storedPassword)) {
                            errorPassword.setText("Password is incorrect");
                            errorUser.setText(""); // Clear the username error
                            loginHandler.clearAndFocusUsername(tfUsername, tfPassword); // Pass the TextField and
                        } else {
                            String role = rs.getString("role");
                            loginHandler.handleRoleRedirection(role, email, username, password, onLogin);
                        }
                    } else {
                        errorPassword.setText("Password is incorrect");
                        // loginHandler.showErrorDialog("Login Failed");
                        loginHandler.clearAndFocusUsername(tfUsername, tfPassword); // Pass the TextField and
                    }

                } catch (SQLException | IOException ex) {
                    loginHandler.handleException(ex);
                }
            }
        }
        if (loginAttempts >= 2) {
            loginHandler.showErrorDialog("Exceeded maximum login attempts. Please try again later.");
            return;
        }

        long currentTime = System.currentTimeMillis();
        long timeSinceLastAttempt = currentTime - lastLoginAttemptTime;

        if (timeSinceLastAttempt < 60000) { // 60000 milliseconds = 1 minute
            long remainingTime = 60000 - timeSinceLastAttempt;
            remainingSeconds = (int) (remainingTime / 1000);
            remainingTimeLabel.setText("You can login after " + remainingSeconds + " seconds");
            remainingTimeUpdater = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateRemainingTime()));
            remainingTimeUpdater.setCycleCount(Timeline.INDEFINITE);
            remainingTimeUpdater.play();
            return; // Stop further login processing
        } else {
            loginAttempts = 0; // Reset login attempts
            remainingTimeLabel.setText(""); // Clear the remaining time label
        }

        loginAttempts++;
        lastLoginAttemptTime = System.currentTimeMillis();
    }

    private void updateRemainingTime() {
        remainingSeconds--;

        if (remainingSeconds > 0) {
            remainingTimeLabel.setText("You can login after " + remainingSeconds + " seconds");

        } else {
            remainingTimeLabel.setText(""); // Clear the remaining time label
            remainingTimeUpdater.stop();
        }
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

    // New method to schedule error clearing after 3 seconds
    private void scheduleErrorClearing() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(3), e -> {
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
