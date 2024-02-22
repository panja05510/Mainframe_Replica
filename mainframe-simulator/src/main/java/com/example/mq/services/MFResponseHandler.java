package com.example.mq.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.print.attribute.HashPrintJobAttributeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MFResponseHandler {

	@Autowired
	private MFRequestHandler request;
	
	@Autowired
	private EBCDICToJson2 e2j;
	
	@Autowired
	private ParseCopybook parseCopybook;
	

	public byte[] byteArray() {
		byte[] byteArray = request.JsonToEbc();
		return byteArray;
	}

	public String EbcdicToJson() {

		// Example EBCDIC byte array
		byte[] ebcdicData = byteArray();
		
		List<Map<String, String>> copybook = List.of(Map.of("name", "ACAI-STRUCT-ID", "display_length", "4"),
				Map.of("name", "ACAI-VERSION", "display_length", "4"),
				Map.of("name", "ACAI-CHANNEL", "display_length", "4"),
				Map.of("name", "ACAI-SERVICE-NAME", "display_length", "48"),
				Map.of("name", "ACAI-USER-ID", "display_length", "8"),
				Map.of("name", "ACAI-SESSION-ID", "display_length", "113"),
				Map.of("name", "ACAI-VERSION (1)", "display_length", "4"));
		
		try {
			List<HashMap<String, String>> copybookAsListOfMap = parseCopybook.getCopybookAsListOfMap("customer.cpy");
			List<Map<String, String>> printCopybook = printCopybook(copybookAsListOfMap);
			String json = e2j.convertToJSON(ebcdicData, printCopybook);
			return json;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public List<Map<String, String>> printCopybook(List<HashMap<String, String>> copybook) {
		List<Map<String, String>> copybookToListOfMap = new ArrayList<>();
		System.out.println("----------------------------------------------------------------");
        for (HashMap<String, String> field : copybook) {
        	Map<String, String> tempMap = new HashMap<>();
        	
            System.out.println("Field:");
            for (Map.Entry<String, String> entry : field.entrySet()) {
            	tempMap.put(entry.getKey(), entry.getValue());
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }
            copybookToListOfMap.add(tempMap);
            System.out.println();
        }
        System.out.println("--------------------------------------------------------------------------------");
        return copybookToListOfMap;
    }
}
