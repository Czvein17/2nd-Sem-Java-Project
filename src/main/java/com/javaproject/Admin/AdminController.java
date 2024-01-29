package com.javaproject.Admin;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.javaproject.ConnectDB;
import com.javaproject.User.UserController;
import com.jfoenix.controls.JFXButton;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class AdminController {

    private String loggedInUserId;

    public void setLoggedInUserId(String loggedInUserId) {
        this.loggedInUserId = loggedInUserId;
        initialize();
    }

    public void initialize() {
        if (loggedInUserId != null) {
            System.out.println("Logged-in user ID: " + loggedInUserId);

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
                                userImage.setImage(image);

                                Circle circle = new Circle(25);
                                circle.setCenterX(userImage.getFitWidth() / 2);
                                circle.setCenterY(userImage.getFitHeight() / 2);
                                userImage.setClip(circle);
                            }
                        } else {
                            System.out.println("Image Blob is null");
                        }
                    } else {
                        System.out.println("No image found for the user with ID: " + loggedInUserId);
                    }
                }
            } catch (SQLException | IOException e) {
                e.printStackTrace(); // Handle the error if an exception occurs during the database query or image
                                     // processing
            }
        } else {
            System.out.println("Logged-in user ID is not set");
            // Handle the case where the loggedInUserId is not set
        }
    }

    @FXML
    private Circle Circle;
    @FXML
    private ImageView userImage;

    @FXML
    private BorderPane bp;

    @FXML
    private AnchorPane ap;

    @FXML
    private Button btnCheckId;

    @FXML
    void onCheckId(ActionEvent event) {
        if (loggedInUserId != null) {
            System.out.println("Logged-in user ID: " + loggedInUserId);
        } else {
            System.out.println("Logged-in user ID is not set"); // Handle the case where the loggedInUserId is not set
        }
    }

    @FXML
    private JFXButton btnHome;

    @FXML
    private JFXButton btnDatabase;

    @FXML
    private JFXButton btnAccount;

    @FXML
    private JFXButton btnLogout;

    @FXML
    private JFXButton btnUser;

    @FXML
    void onHome(MouseEvent event) {
        bp.setCenter(ap);
    }

    @FXML
    void onDatabase(ActionEvent event) {
        // loadPage("page2");
    }

    @FXML
    void onAccount(ActionEvent event) {
        // loadPage("Account");
    }

    @FXML
    void onUser(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/javaproject/User/userProfile.fxml"));
        try {
            Parent userProfileRoot = loader.load();

            // Transfer the logged-in user ID
            UserController userProfileController = loader.getController();
            userProfileController.setLoggedInUserId(loggedInUserId);

            // Replace the contents of the admin root with the userProfile
            bp.setCenter(userProfileRoot);
        } catch (IOException ex) {
            Logger.getLogger(AdminController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // private void loadPage(String page) {
    // Parent root = null;
    // try {
    // root = FXMLLoader.load(getClass().getResource("/FXML/" + page +
    // ".fxml"));
    // } catch (IOException ex) {
    // Logger.getLogger(AdminController.class.getName()).log(Level.SEVERE, null,
    // ex);
    // }

    // bp.setCenter(root);
    // }

    @FXML
    void onLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/javaproject/login.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();

            // Close the current page
            Stage currentPage = (Stage) btnLogout.getScene().getWindow();
            currentPage.close();
            System.out.println("User Logged Out");
            JOptionPane.showMessageDialog(null, "User Logged Out");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}