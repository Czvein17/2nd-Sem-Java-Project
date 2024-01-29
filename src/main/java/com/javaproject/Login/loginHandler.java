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

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class loginHandler {

    public void handleRoleRedirection(String role, String email, String username, String password, Button onLogin)
            throws IOException {
        FXMLLoader loader = new FXMLLoader();
        Parent root = null;
        switch (role) {
            case "admin":
                loader.setLocation(getClass().getResource("/com/javaproject/Admin/admin.fxml"));
                root = loader.load();
                AdminController adminController = loader.getController();
                adminController.setLoggedInUserId(getLoggedInUserId(email, username, password));
                break;
            case "manager":
                loader.setLocation(getClass().getResource("/com/javaproject/Manager/manager.fxml"));
                root = loader.load();
                managerController managerController = loader.getController();
                managerController.setLoggedInUserId(getLoggedInUserId(email, username, password));
                break;
            case "user":
                loader.setLocation(getClass().getResource("/com/javaproject/UserClient/clientUser.fxml"));
                root = loader.load();
                clientController clientController = loader.getController();
                clientController.setLoggedInUserId(getLoggedInUserId(email, username, password));
                break;
            default:
                showErrorDialog("Invalid role");
                return;
        }

        if (root != null) {
            Scene scene = new Scene(root);
            Stage stage = (Stage) onLogin.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } else {
            showErrorDialog("Failed to load the specified role");
        }
    }

    public void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(null, message);
    }

    public String getLoggedInUserId(String email, String username, String password) {
        try {
            Connection connection = new ConnectDB().getConnection();
            PreparedStatement statement = connection
                    .prepareStatement("SELECT id FROM javaproject.users WHERE (email=? or username=?) AND pass=?");
            statement.setString(1, email);
            statement.setString(2, username); // Replace with the actual username
            statement.setString(3, password); // Replace with the actual password
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getString("id");
            }
        } catch (SQLException ex) {
            Logger.getLogger(loginController.class.getName()).log(java.util.logging.Level.SEVERE,
                    "Error retrieving user ID for email: " + email, ex);
        }
        return null; // Return null if the logged-in user's ID cannot be found
    }

    public void clearAndFocusUsername(TextField tfUsername, PasswordField tfPassword) {
        tfUsername.setText("");
        tfPassword.setText("");
        tfUsername.requestFocus();
    }

    public void handleException(Exception ex) {
        if (ex instanceof SQLException) {
            Logger.getLogger(loginController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } else if (ex instanceof IOException) {
            ex.printStackTrace();
        }
    }
}