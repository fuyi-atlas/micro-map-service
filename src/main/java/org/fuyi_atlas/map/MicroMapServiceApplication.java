package org.fuyi_atlas.map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class MicroMapServiceApplication {

	public static void main(String[] args) {
		// Setting the system-wide default at startup time
        System.setProperty("org.geotools.referencing.forceXY", "true");
		SpringApplication.run(MicroMapServiceApplication.class, args);
	}

}
