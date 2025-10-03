package web.car_system.Car_Service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication(scanBasePackages = "web.car_system.Car_Service") // Quét toàn bộ package gốc
public class CarServiceApplication {
	List<String> names = new ArrayList<>();
	ArrayList<String> list = new ArrayList<>();
	public static void main(String[] args) {
		SpringApplication.run(CarServiceApplication.class, args);
	}

}
