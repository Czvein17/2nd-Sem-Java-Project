package com.javaproject.Manager;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class managerController {
    @FXML
    private Button logoutButton;

    @FXML
    void onLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/javaproject/login.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();

            // Close the current page
            Stage currentPage = (Stage) logoutButton.getScene().getWindow();
            currentPage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setLoggedInUserId(String loggedInUserId) {
    }
}
