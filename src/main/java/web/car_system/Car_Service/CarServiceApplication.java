package web.car_system.Car_Service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication(exclude = { RedisRepositoriesAutoConfiguration.class })
@EnableJpaRepositories(basePackages = {
		"web.car_system.Car_Service.repository",
		"web.car_system.Car_Service.repositories"
})
@EnableScheduling
// @EnableAsync đã chuyển sang AsyncConfig.java để quản lý executor tập trung
public class CarServiceApplication {
	List<String> names = new ArrayList<>();
	ArrayList<String> list = new ArrayList<>();
	public static void main(String[] args) {
		SpringApplication.run(CarServiceApplication.class, args);
	}

}
