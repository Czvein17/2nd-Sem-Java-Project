module com.javaproject {
    requires transitive javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires com.jfoenix;
    requires transitive javafx.graphics;
    requires transitive java.sql;

    opens com.javaproject to javafx.fxml;
    opens com.javaproject.Login to javafx.fxml;
    opens com.javaproject.Admin to javafx.fxml;
    opens com.javaproject.User to javafx.fxml;
    opens com.javaproject.UserClient to javafx.fxml;
    opens com.javaproject.Manager to javafx.fxml;
    opens com.javaproject.CreateUser to javafx.fxml;
    opens com.javaproject.UserClient.Pages to javafx.fxml;

    exports com.javaproject;
    exports com.javaproject.Login;
    exports com.javaproject.Admin;
    exports com.javaproject.User;
    exports com.javaproject.UserClient;
    exports com.javaproject.UserClient.Pages;
    exports com.javaproject.Manager;
    exports com.javaproject.CreateUser;
}