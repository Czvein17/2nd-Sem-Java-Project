package com.javaproject.User;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;

import com.javaproject.ConnectDB;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class UserController {

    private String loggedInUserId;

    // Existing code...

    public void setLoggedInUserId(String loggedInUserId) {
        this.loggedInUserId = loggedInUserId;
        initialize();
    }

    public Connection dataConnect() {
        return new ConnectDB().getConnection();
    }

    @FXML
    private ImageView DisplayImage;

    @FXML
    private Button loadImage;

    @FXML
    private Button btnUploadPicture;

    @FXML
    private ImageView phImage;

    @FXML
    private Label lblStatus;

    private File selectedImageFile;

    @FXML
    void onUploadPicture(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().add(new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

        selectedImageFile = fileChooser.showOpenDialog(btnUploadPicture.getScene().getWindow());
        ;
        if (selectedImageFile != null) {
            // Update the ImageView to display the newly uploaded profile picture
            Image image = new Image(selectedImageFile.toURI().toString());
            phImage.setImage(image);

        }
    }

    public void initialize() {
        if (loggedInUserId != null) {
            System.out.println("Logged-in user ID: " + loggedInUserId);

            try (Connection connection = new ConnectDB().getConnection();
                    PreparedStatement statement = connection
                            .prepareStatement("SELECT image FROM javaproject.users WHERE id = ?")) {
                statement.setString(1, loggedInUserId);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        Blob imageBlob = resultSet.getBlob("image");

                        if (imageBlob != null) {
                            try (InputStream inputStream = imageBlob.getBinaryStream()) {
                                Image image = new Image(inputStream);
                                DisplayImage.setImage(image);
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
    void onSaveImage(ActionEvent event) {
        if (loggedInUserId != null) {
            System.out.println("Logged-in user ID: " + loggedInUserId);
            // Perform actions with the loggedInUserId

            // Insert the picture into the database
            try {
                Connection connection = dataConnect();
                PreparedStatement statement = connection
                        .prepareStatement("UPDATE `javaproject`.`users` SET `image` = ? WHERE (`id` = ?);");
                FileInputStream fileInputStream = new FileInputStream(selectedImageFile);
                statement.setBinaryStream(1, fileInputStream);
                statement.setString(2, loggedInUserId);
                statement.executeUpdate();
                statement.close();
                connection.close();

                System.out.println("Picture saved successfully!");
                JOptionPane.showMessageDialog(null, "Profile Updated");

            } catch (SQLException | FileNotFoundException e) {
                e.printStackTrace();
                // Handle the error if an exception occurs during the database update or file
                // reading
            }

        } else {
            System.out.println("Logged-in user ID is not set");
            // Handle the case where the loggedInUserId is not set
        }
    }

    @FXML
    void onLoadImage(ActionEvent event) throws IOException {
        retrieveAndDisplayImage();
    }

    public void retrieveAndDisplayImage() throws IOException {
        try {
            ConnectDB connectDB = new ConnectDB();
            Connection connection = connectDB.getConnection();
            String sql = "SELECT image FROM javaproject.users WHERE id = ?";

            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, loggedInUserId);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                Blob imageBlob = resultSet.getBlob("image");

                InputStream inputStream = imageBlob.getBinaryStream();
                Image image = new Image(inputStream);
                DisplayImage.setImage(image);
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the error if an exception occurs during the database query or image
                                 // processing
        }
    }
}