package com.javaproject.UserClient;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.javaproject.ConnectDB;
import com.javaproject.Admin.AdminController;
import com.javaproject.UserClient.Pages.userEdit;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Circle;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.event.ActionEvent;

public class clientController {

    private String loggedInUserId;

    public void setLoggedInUserId(String loggedInUserId) {
        this.loggedInUserId = loggedInUserId;
        initialize();

    }

    @FXML
    private ImageView userProfile, userProfile1;
    @FXML
    private Button logoutButton, btnHome, profile;

    @FXML
    private BorderPane userClientUi;
    @FXML
    private AnchorPane pageLoad;

    public void initialize() {

        if (loggedInUserId != null) {
            System.out.println("CLIENT CONTROLLER Logged-in user ID: " + loggedInUserId);
            refreshUserProfileImage(); // Call the method to refresh the user profile image initially

            // Use a Timeline to periodically refresh the user profile image
            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(300), event -> {
                refreshUserProfileImage();
            }));
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.play();
        } else {
            System.out.println("Logged-in user ID is not set");
            // Handle the case where the loggedInUserId is not set
        }
    }

    private void refreshUserProfileImage() {
        try (Connection connection = new ConnectDB().getConnection();
                PreparedStatement statement = connection
                        .prepareStatement("SELECT * FROM javaproject.users WHERE id = ?")) {
            statement.setString(1, loggedInUserId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Blob imageBlob = resultSet.getBlob("image");

                    if (imageBlob != null) {
                        try (InputStream inputStream = imageBlob.getBinaryStream()) {
                            Image image = new Image(inputStream);
                            userProfile.setImage(image);
                            userProfile1.setImage(image);

                            Circle circle = new Circle(25);
                            circle.setCenterX(userProfile.getFitWidth() / 2);
                            circle.setCenterY(userProfile.getFitHeight() / 2);
                            userProfile.setClip(circle);

                        }
                    } else {
                        System.out.println("Image Blob is null");
                    }
                } else {
                    System.out.println("No image found for the user with ID: " + loggedInUserId);
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPage(String page) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/javaproject/UserClient/Pages/" + page + ".fxml"));
            Parent root = loader.load();
            userEdit userProfileController = loader.getController();
            userProfileController.setLoggedInUserId(loggedInUserId);

            userClientUi.setCenter(root);
        } catch (IOException ex) {
            Logger.getLogger(AdminController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    void onHome(MouseEvent event) {
        btnHome.getStyleClass().add("active");
        userClientUi.setCenter(pageLoad);
    }

    @FXML
    void onPage1(ActionEvent event) {
        profile.getStyleClass().add("armed");
        loadPage("page1");

    }

    @FXML
    void onPage2(ActionEvent event) {
        loadPage("page2");
    }

    @FXML
    void onPage3(ActionEvent event) {
        loadPage("page3");
    }

    @FXML
    void onPage4(ActionEvent event) {
        loadPage("page4");
    }

    @FXML
    void onLogout(ActionEvent event) {
        Alert alert = new Alert(AlertType.INFORMATION, "User logged out.");
        alert.setHeaderText(null);
        alert.show();

        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(e -> {
            alert.close();
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/javaproject/login.fxml"));
                Parent root = loader.load();
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.show();

                // Close the current page
                Stage currentPage = (Stage) logoutButton.getScene().getWindow();
                currentPage.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        delay.play();
    }
}
