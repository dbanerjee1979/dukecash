module org.dukecash.dukecash {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    exports org.dukecash;
    opens org.dukecash to javafx.fxml;
}