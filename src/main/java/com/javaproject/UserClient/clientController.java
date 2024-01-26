package com.javaproject.UserClient;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class clientController {

    public void setLoggedInUserId(String loggedInUserId) {
    }

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
}
