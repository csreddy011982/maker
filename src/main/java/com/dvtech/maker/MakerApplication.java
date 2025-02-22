package com.dvtech.maker;



import com.dvtech.maker.service.MockAdviceDataFeedService;
import com.dvtech.maker.service.MockSplReportFeedGeneratorService;
import com.dvtech.maker.service.MockSplReportFeedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class MakerApplication {

	@Autowired
	private ApplicationContext applicationContext;

	public static void main(String[] args) {
		SpringApplication.run(MakerApplication.class, args);
	}

	public void run(String... args) throws Exception {
		// Accessing the service bean from ApplicationContext
		//MockAdviceDataFeedService myService = applicationContext.getBean(MockAdviceDataFeedService.class);
		MockSplReportFeedGeneratorService myService = applicationContext.getBean(MockSplReportFeedGeneratorService.class);
		myService.generateSplReportFeed();  // Calling the method on the service
	}
}
