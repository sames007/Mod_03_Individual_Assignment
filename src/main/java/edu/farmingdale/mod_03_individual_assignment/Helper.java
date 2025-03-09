package edu.farmingdale.mod_03_individual_assignment;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Helper {
    public static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = Helper.class.getResourceAsStream("/edu/farmingdale/mod_03_individual_assignment/config.properties")) {
            properties.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return properties;
    }
}
