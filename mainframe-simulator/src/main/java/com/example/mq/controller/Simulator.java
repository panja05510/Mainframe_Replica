package com.example.mq.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.mq.messaginglibrary.ResponseBaseModel;
import com.example.mq.messaginglibrary.ResponseCBLModel;
import com.example.mq.messaginglibrary.ResponseErrorModel;

import com.example.mq.services.EbcdicToJson;
import com.example.mq.services.JsonToEbcdic;
import com.example.mq.services.MFRequestHandler;
import com.google.gson.JsonObject;

@RestController
@RequestMapping("/")
public class Simulator {

	@Autowired
	private EbcdicToJson e2j;
	
	@Autowired
	MFRequestHandler mfRequestHandler;
	
	@Autowired
	ResponseErrorModel responseErrorModel;
	

	@Autowired
	ResponseCBLModel responseCBLModel;

	ResponseBaseModel responseBaseModel = new ResponseBaseModel();

	@Autowired
	private JsonToEbcdic j2e;

	private String cobolFile = "customer.cpy";

	@GetMapping("send")
	public byte[] jsonToEbcdic() throws InterruptedException {
		return mfRequestHandler.JsonToEbc();
		//return j2e.request2mainframe(cobolFile);
	}

	@GetMapping("test")
	public String temp() {
		return "hello world";
	}

	@GetMapping("e2j")
	public String ebcdictoJson() throws InterruptedException {
		System.out.println(responseBaseModel.getCopybooks());

		Map<String, Map<String, String>> copybooks = new HashMap<>();

		if (responseBaseModel.getApiName() == null) {
			System.out.println("rsponse model is null");
			responseBaseModel.setApiName("savings/closing-balance/query");
			responseBaseModel.setResourceName("savings/closing-balance/query");

			responseBaseModel.setQueue("dev.queue.1");
			System.out.println("setting queue");

			// Default entry
			Map<String, String> defaultEntry = new HashMap<>();
			defaultEntry.put("responseType", "error");
			defaultEntry.put("copybookName", "customer.cpy");
			copybooks.put("default", defaultEntry);

			// Entry with key 2649
			Map<String, String> entry2649 = new HashMap<>();
			entry2649.put("responseType", "success");
			entry2649.put("copybookName", "customer.cpy");
			copybooks.put("181", entry2649);

			// Entry with key 3056
			Map<String, String> entry3056 = new HashMap<>();
			entry3056.put("responseType", "error");
			entry3056.put("copybookName", "customer.cpy");
			copybooks.put("3056", entry3056);
			responseBaseModel.setCopybooks(copybooks);

			Map<String, String> fieldMap = new HashMap<>();

			// Adding entries to the map
			fieldMap.put("SDSTMYM1-EXT-AMT", "amount");
			fieldMap.put("SDSTMYM1-EXT-AMT-LIT", "balanceType");
			responseCBLModel.setLeaves(fieldMap);

			List<String> headerList = new ArrayList<>();

			headerList.add("ALLTEL-MQ-HEADER");
			responseCBLModel.setParents(headerList);
			responseBaseModel.setCobolfieldsmap(responseCBLModel);
			
			Map<String, Map<String, String>> mfResponseValuesMapping = new HashMap<>();

	        // Adding entries to the map
	        Map<String, String> returnCodeMap = new HashMap<>();
	        returnCodeMap.put("00", "SUCCESS");
	        returnCodeMap.put("01", "INFO");
	        returnCodeMap.put("02", "ERROR");

	        mfResponseValuesMapping.put("returnCode", returnCodeMap);
	        
	        responseBaseModel.setMfResponseValuesMapping(mfResponseValuesMapping);
	        Map<String, String> errorFieldnameMapping = new HashMap<>();

	        // Adding entries to the map
	        errorFieldnameMapping.put("messageCode", "TSMDA-MESSAGE-CODE");
	        errorFieldnameMapping.put("messageDesc", "TSMDA-VARIABLE-1");
	        errorFieldnameMapping.put("errorCount", "TSMDA-OCCURRENCE-IN-PROC");
	        errorFieldnameMapping.put("messageSeverity", "TSMDA-HI-SEVERITY-CODE");
	        
	        responseErrorModel.setErrorFieldnameMapping(errorFieldnameMapping);
	        
	        Map<String, Map<String, Map<String, String>>> validErrorMetadata = new HashMap<>();

	        // Adding entries to the map
	        Map<String, Map<String, String>> errorCodesMap = new HashMap<>();
	        Map<String, String> errorCodeRM3510 = new HashMap<>();
	        errorCodeRM3510.put("errorMessage", "SAA1001: No account relationship found for the given request parameters.");
	        errorCodeRM3510.put("httpStatus", "404");
	        errorCodesMap.put("RM3510", errorCodeRM3510);

	        Map<String, Map<String, String>> messageDescriptionsMap = new HashMap<>();
	        Map<String, String> messageDescriptionSTMEM = new HashMap<>();
	        messageDescriptionSTMEM.put("errorMessage", "DAS1006: Account number not found.");
	        messageDescriptionSTMEM.put("httpStatus", "404");
	        messageDescriptionsMap.put("STMEM RECORD NOT FOUND", messageDescriptionSTMEM);

	        validErrorMetadata.put("errorCodes", errorCodesMap);
	        validErrorMetadata.put("messageDescriptions", messageDescriptionsMap);
	        
	        responseErrorModel.setValidErrorMetadata(validErrorMetadata);
	        
	        responseBaseModel.setError(responseErrorModel);
	        
	        

		}

		JsonObject json = e2j.mainframe2json(jsonToEbcdic(), responseBaseModel);
		System.out.println(json);

		
		//return json;
		return json.toString();
		
	}

}
