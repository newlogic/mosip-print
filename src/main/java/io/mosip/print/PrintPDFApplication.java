package io.mosip.print;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import io.mosip.kernel.cbeffutil.impl.CbeffImpl;
import io.mosip.kernel.core.cbeffutil.spi.CbeffUtil;

@SpringBootApplication
public class PrintPDFApplication {

	@Bean
	@Primary
	public CbeffUtil getCbeffUtil() {
		return new CbeffImpl();
	}

	@Bean
	public ThreadPoolTaskScheduler getTaskScheduler() {
		return new ThreadPoolTaskScheduler();
	}

	public static void main(String[] args) {
		SpringApplication.run(PrintPDFApplication.class, args);
	}

}
