module org.tovivi {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires org.tensorflow;
    requires org.tensorflow.framework;

    exports org.tovivi.gui;
    opens org.tovivi.gui to javafx.fxml;
}
