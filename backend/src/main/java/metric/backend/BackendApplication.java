/*
 * Copyright (c) 2026 Vidchayada
 * Licensed under the MIT License.
 */

package metric.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Entry point of the backend application. */
@SpringBootApplication
public class BackendApplication {

    /**
     * Starts the application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}