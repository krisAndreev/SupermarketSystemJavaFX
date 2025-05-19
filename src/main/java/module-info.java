module org.supermarket.supermarketsystemjavafx {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires jdk.jfr;
    requires java.sql;
    //requires eu.hansolo.tilesfx;

    opens org.supermarket.supermarketsystemjavafx to javafx.fxml;
    exports org.supermarket.supermarketsystemjavafx;
    exports org.supermarket.supermarketsystemjavafx.controllers;
    opens org.supermarket.supermarketsystemjavafx.controllers to javafx.fxml;
    exports org.supermarket.supermarketsystemjavafx.db;
    opens org.supermarket.supermarketsystemjavafx.db to javafx.fxml;
}