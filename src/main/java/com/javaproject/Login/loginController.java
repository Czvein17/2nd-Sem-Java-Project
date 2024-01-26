package com.javaproject.Login;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

import com.javaproject.ConnectDB;
import com.javaproject.Admin.AdminController;
import com.javaproject.Manager.managerController;
import com.javaproject.UserClient.clientController;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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
    private Button btnCreate;

    @FXML
    private Button onLogin;

    @FXML
    private PasswordField tfPassword;

    @FXML
    private TextField tfUsername;

    @FXML
    private Label remainingTimeLabel;
    private Timeline remainingTimeUpdater;
    private int remainingSeconds;

    private int loginAttempts = 0;
    private long lastLoginAttemptTime = 0;

    @FXML
    void onClickLogin(javafx.event.ActionEvent event) {
        String email = tfUsername.getText();
        String password = tfPassword.getText();

        if (email.equals("") || password.equals("")) {
            JOptionPane.showMessageDialog(null, "Username or Password Blank");
        } else {
            if (remainingSeconds > 0) {
                JOptionPane.showMessageDialog(null, "Cannot login, please wait for the remaining time.");
            } else {
                try {
                    ConnectDB connectDB = new ConnectDB();
                    Connection connection = connectDB.getConnection();
                    pst = connection
                            .prepareStatement("SELECT * FROM guidb.users WHERE (email=? OR username=? ) AND pass=?");

                    pst.setString(1, email);
                    pst.setString(2, email);
                    pst.setString(3, password);

                    rs = pst.executeQuery();

                    if (rs.next()) {
                        String role = rs.getString("role");

                        if (role.equals("admin")) {
                            FXMLLoader loader = new FXMLLoader(
                                    getClass().getResource("/com/javaproject/Admin/admin.fxml"));
                            Parent root = loader.load();
                            AdminController adminController = loader.getController();
                            adminController.setLoggedInUserId(getLoggedInUserId(email));
                            // Pass the logged-in user's ID to the admin controller
                            Stage stage = new Stage();
                            stage.setScene(new Scene(root));
                            stage.show();

                        } else if (role.equals("manager")) {

                            // Redirect to manager page
                            FXMLLoader loader = new FXMLLoader(
                                    getClass().getResource("com/javaproject/Manager/manager.fxml"));
                            Parent root = loader.load();
                            managerController managerController = loader.getController();
                            managerController.setLoggedInUserId(getLoggedInUserId(email));
                            // Pass the logged-in user's ID
                            // to the manager controller
                            Stage stage = new Stage();
                            stage.setScene(new Scene(root));
                            stage.show();

                        } else if (role.equals("user")) {

                            // Redirect to customer page
                            FXMLLoader loader = new FXMLLoader(
                                    getClass().getResource("/com/javaproject/UserClient/clientUser.fxml"));
                            Parent root = loader.load();
                            clientController clientController = loader.getController();
                            clientController.setLoggedInUserId(getLoggedInUserId(email));
                            // Pass the logged-in user's ID to
                            // the user controller
                            Stage stage = new Stage();
                            stage.setScene(new Scene(root));
                            stage.show();

                        } else {
                            // Invalid role, handle error
                            JOptionPane.showMessageDialog(null, "Invalid role");
                        }

                        // Close the login window
                        Stage loginStage = (Stage) onLogin.getScene().getWindow();
                        loginStage.close();

                    } else {
                        JOptionPane.showMessageDialog(null, "Login Failed");
                        tfUsername.setText("");
                        tfPassword.setText("");
                        tfUsername.requestFocus();
                    }

                } catch (SQLException ex) {
                    Logger.getLogger(loginController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        if (loginAttempts >= 2) {
            JOptionPane.showMessageDialog(null, "Cannot login, please wait for the remaining time.");
            long currentTime = System.currentTimeMillis();
            long timeSinceLastAttempt = currentTime - lastLoginAttemptTime;

            if (timeSinceLastAttempt < 60000) { // 60000 milliseconds = 1 minute
                long remainingTime = 60000 - timeSinceLastAttempt;
                remainingSeconds = (int) (remainingTime / 1000);
                remainingTimeLabel.setText("you can login after " + remainingSeconds + " seconds");
                remainingTimeUpdater = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateRemainingTime()));
                remainingTimeUpdater.setCycleCount(Timeline.INDEFINITE);
                remainingTimeUpdater.play();
                return; // Stop further login processing

            } else {
                loginAttempts = 0; // Reset login attempts
                remainingTimeLabel.setText(""); // Clear the remaining time label
            }
        }
        loginAttempts++;
        lastLoginAttemptTime = System.currentTimeMillis();
    }

    private void updateRemainingTime() {
        remainingSeconds--;
        if (remainingSeconds > 0) {
            remainingTimeLabel.setText("you can login after " + remainingSeconds + " seconds");
        } else {
            remainingTimeLabel.setText(""); // Clear the remaining time label
            remainingTimeUpdater.stop();
        }
    }

    private String getLoggedInUserId(String email) {
        // Your logic to get the logged-in user's ID from the database
        try {
            ConnectDB connectDB = new ConnectDB();
            Connection connection = connectDB.getConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT id FROM guidb.users WHERE email=?");
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getString("id");
            }
        } catch (SQLException ex) {
            Logger.getLogger(loginController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        return null; // Return null if the logged-in user's ID cannot be found
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
