module edu.farmingdale.mod_03_individual_assignment {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;


    opens edu.farmingdale.mod_03_individual_assignment to javafx.fxml;
    exports edu.farmingdale.mod_03_individual_assignment;
}