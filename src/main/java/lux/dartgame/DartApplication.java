package lux.dartgame;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class DartApplication {
    public static void main(final String[] args) {
        System.out.println("Starting Dart Game!");
    }
}
