module com.example.pixel_master {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;
    //requires eu.hansolo.tilesfx;

    opens com.example.pixel_master to javafx.fxml;
    exports com.example.pixel_master;
}