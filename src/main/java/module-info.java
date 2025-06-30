module org.dukecash.dukecash {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    exports org.dukecash;
    opens org.dukecash to javafx.fxml;
    exports org.dukecash.views;
    opens org.dukecash.views to javafx.fxml;
    exports org.dukecash.models;
    opens org.dukecash.models to javafx.fxml;
}