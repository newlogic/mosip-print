package io.mosip.print.util;

import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.print.logger.PrintLogger;

@Component
public class PrintInstializer  implements ApplicationListener<ApplicationEvent>{

	
	@Value("${retry-count:3}")
	private int retryCount;

	@Value("${resubscription-delay-secs:7200}") // Default is 60 * 60 * 2 = 2 hours
	private int reSubscriptionDelaySecs;

	@Autowired
	private WebSubSubscriptionHelper webSubSubscriptionHelper;

	@Autowired
	private ThreadPoolTaskScheduler taskScheduler;

	/** The Constant BIOMETRICS. */
	private static final String ONAPPLICATIONEVENT = "onApplicationEvent";

	/** The Constant ID_REPO_SERVICE_IMPL. */
	private static final String CREDENTIALINSTIALIZER = "CredentialInstializer";

	private static final Logger LOGGER = PrintLogger.getLogger(PrintInstializer.class);

	
	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		// TODO Auto-generated method stub
		LOGGER.info("NotifyPrint", CREDENTIALINSTIALIZER, ONAPPLICATIONEVENT,
				"Work around for web-sub notification issue after some time.");

		scheduleRetrySubscriptions();
		
	}

	private void scheduleRetrySubscriptions() {
		LOGGER.info("NotifyPrint", CREDENTIALINSTIALIZER, ONAPPLICATIONEVENT,
				"Scheduling re-subscription every " + reSubscriptionDelaySecs + " seconds");


		taskScheduler.scheduleAtFixedRate(this::retrySubscriptions, Instant.now().plusSeconds(reSubscriptionDelaySecs),
				Duration.ofSeconds(reSubscriptionDelaySecs));
	}

	private void retrySubscriptions() {
		// Call Init Subscriptions for the count until no error in the subscription
		for (int i = 0; i < retryCount; i++) {
			if (initSubsriptions()) {
				return;
			}
		}
	}

	private boolean initSubsriptions() {
		try {
			LOGGER.info("NotifyPrint", CREDENTIALINSTIALIZER, ONAPPLICATIONEVENT,
					"Initializing subscribptions..");
			webSubSubscriptionHelper.initSubsriptions();

			return true;
		} catch (Exception e) {
			LOGGER.error("NotifyPrint", CREDENTIALINSTIALIZER, ONAPPLICATIONEVENT,
					"Initializing subscribptions failed: " + e.getMessage());

			return false;
		}
	}
}
	

