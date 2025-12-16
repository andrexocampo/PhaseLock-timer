package com.portafolio.PhaseLock_timer;

import com.portafolio.PhaseLock_timer.config.BlockConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableConfigurationProperties(BlockConfiguration.class)
@EnableAsync
public class PhaseLockTimerApplication {

	public static void main(String[] args) {
		SpringApplication.run(PhaseLockTimerApplication.class, args);
	}

}
