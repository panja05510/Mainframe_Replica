package com.example.mq;

//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//import com.example.mq.services.JsonToEbcdic;

@SpringBootApplication
public class MqDataApplication {
	
	
	public static void main(String[] args) {
		SpringApplication.run(MqDataApplication.class, args);
		
		System.out.println("spring project started at port : http://localhost:8080 ");
		
	}

}
