module org.tovivi {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    exports org.tovivi.gui;
    opens org.tovivi.gui to javafx.fxml;
}
