package net.lamida;


import javax.sql.DataSource;

import junit.framework.Assert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableAutoConfiguration
public class Application {
	private static final Logger log = LoggerFactory.getLogger(Application.class);
	
	@Bean
	BookingService bookingService() {
		return new BookingService();
	}
	
	@Bean
	DataSource dataSource() {
		return new SimpleDriverDataSource() {
			{
				setDriverClass(org.h2.Driver.class);
				setUsername("sa");
				setUrl("jdbc:h2:mem");
				setPassword("");	
			}
		};
	}
	
	@Bean
	JdbcTemplate jdbcTemplate(DataSource dataSource) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		log.info("Creating tables");
		jdbcTemplate.execute("drop table BOOKINGS if exists");
		jdbcTemplate.execute("create table BOOKINGS(ID serial, FIRST_NAME varchar(5) NOT NULL)");
		return jdbcTemplate;
	}
	
	public static void main(String[] args) {
		ApplicationContext ctx = SpringApplication.run(Application.class, args);
		
		BookingService bookingService = ctx.getBean(BookingService.class);
		bookingService.book("Alice", "Bob", "Carol");
		Assert.assertEquals("First booking should work with no problem", 3, bookingService.findAllBookings().size());
		
		try {
			bookingService.book("Chris", "Samuel");
		}catch (RuntimeException e) {
			log.info("v--- The following exception is expect because Samues is too big for database ---v");
			log.error(e.getMessage());
		}
		
		for (String person : bookingService.findAllBookings()) {
			log.info("So far, " + person + " is booked.");
		}
		log.info("You shouldn't see Chris or Samuel.");
		Assert.assertEquals("'Samuel' should have triggerred rollback", 3, bookingService.findAllBookings().size());
		
	}
}
