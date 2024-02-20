package com.example.mq.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.mq.services.JsonToEbcdic;

@RestController
@RequestMapping("/")
public class Simulator {

	@Autowired
	private JsonToEbcdic j2e;
	
	private String cobolFile = "customer.cpy";
	
	@GetMapping("send")
	public byte[] jsonToEbcdic() {
		return j2e.request2mainframe(cobolFile);
	}
	
	@GetMapping("test")
	public String temp() {
		return "hello world";
	}
	
}
