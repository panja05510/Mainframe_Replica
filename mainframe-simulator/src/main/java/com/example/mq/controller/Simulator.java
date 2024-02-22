package com.example.mq.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.mq.services.JsonToEbcdic;
import com.example.mq.services.MFRequestHandler;
import com.example.mq.services.MFResponseHandler;

@RestController
@RequestMapping("/")
public class Simulator {

	@Autowired
	private MFRequestHandler requestHandler;
	
	@Autowired
	private MFResponseHandler responseHandler;
	
	@GetMapping("send")
	public byte[] jsonToEbcdic() throws InterruptedException {
		return requestHandler.JsonToEbc();
	}
	
	@GetMapping("receive")
	public String temp() {
		String json = responseHandler.EbcdicToJson();
		return json;
	}
	
}
