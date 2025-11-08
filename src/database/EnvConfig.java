package database;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class EnvConfig {
    private static final Properties props = new Properties();

    static {
        try {
            FileInputStream fis = new FileInputStream(".env");
            props.load(fis);
        } catch (IOException e) {
            System.out.println("⚠️ Could not load .env file!");
        }
    }

    public static String get(String key) {
        return props.getProperty(key);
    }
}
