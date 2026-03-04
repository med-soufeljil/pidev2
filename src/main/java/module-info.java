module org.soa.tp1.pi_dev_s2 {
    // JavaFX
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;

    // Java standard
    requires java.sql;
    requires java.desktop;
    requires java.net.http;

    // Apache POI
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;

    // JSON
    requires com.fasterxml.jackson.databind;

    // HTTP Server
    requires jdk.httpserver;

    // EXPORTS : rendre visible aux autres modules
    exports org.soa.tp1.pi_dev_s2.com.esprit.main;
    exports org.soa.tp1.pi_dev_s2.controller;
    exports org.soa.tp1.pi_dev_s2.model;
    exports org.soa.tp1.pi_dev_s2.service;
    exports org.soa.tp1.pi_dev_s2.dao;
    exports org.soa.tp1.pi_dev_s2.config;
    exports org.soa.tp1.pi_dev_s2.mouhamd.api;
    // OPENS : nécessaire pour que JavaFX FXML accède aux classes privées
    opens org.soa.tp1.pi_dev_s2 to javafx.graphics, javafx.fxml;

    opens org.soa.tp1.pi_dev_s2.com.esprit.controllers to javafx.fxml;
    opens org.soa.tp1.pi_dev_s2.controller to javafx.fxml;
    opens org.soa.tp1.pi_dev_s2.model to javafx.base, javafx.fxml;
    opens org.soa.tp1.pi_dev_s2.mouhamd.controllers to javafx.fxml;
    exports org.soa.tp1.pi_dev_s2.mouhamd.test to javafx.fxml, javafx.graphics;
}