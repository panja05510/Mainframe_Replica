package com.example.mq.services;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import net.sf.JRecord.JRecordInterface1;
import net.sf.JRecord.Details.RecordDetail;
import net.sf.JRecord.def.IO.builders.ICobolIOBuilder;
import net.sf.JRecord.detailsBasic.IItemDetails;

@Component
public class ParseCopybook {
	
	public List<HashMap<String, String>> getCopybookAsListOfMap(String copybook) throws InterruptedException {
		System.out.println("constcutor called");
		ArrayList<HashMap<String,String>> intermediate_map = copybookToIntermediate(copybook);
		System.out.println("request2mainframe()--> intermediate_map : "+ intermediate_map);
		System.out.println("intermidate from parseCopybook is : " );
//		printObjectDetails(intermediate_map);
		return intermediate_map;
	}
	
	public ArrayList<HashMap<String, String>> copybookToIntermediate(String copybookName)
	{
		System.out.println("copybookToIntermediate() called");
		try {
			ClassPathResource resource = new ClassPathResource(copybookName);
			InputStream inputStream = resource.getInputStream();
			ICobolIOBuilder iob = JRecordInterface1.COBOL.newIOBuilder(inputStream,copybookName);
			RecordDetail record = iob.getLayout().getRecord(0);
//			printObjectDetails(record);
//			System.out.println("copybookToIntermediate() --> record : "+record);
			IItemDetails root = record.getCobolItems().get(0);
//			System.out.println("copybookToIntermediate() --> root : "+root.toString());
//			printObjectDetails(root);
//			Thread.sleep(100000);
			return getIntermediateList(root,new ArrayList<>(), new HashMap<>());
		}
		catch(Exception e) {
			System.out.println("error occured at JsonToEbcdic-->copybookToIntermedaite()" + e.getMessage());
			return null;  
		}
	}
	
	public ArrayList<HashMap<String, String>> getIntermediateList(IItemDetails items, ArrayList<HashMap<String, String>> fields, HashMap<String, Integer> allNames) throws InterruptedException {
	    if (items == null) {
	        return null;
	    }

	    for (IItemDetails i : items.getChildItems()) {
	        String fieldName = i.getFieldName();

	        if (i.isLeaf()) {
	            if (!i.isFieldRedefined()) {
	                if (i.isFieldRedefined()) {
	                    fieldName = i.getRedefinesFieldName();
	                }

	                if (!allNames.containsKey(fieldName)) {
	                    allNames.put(fieldName, 0);
	                } else {
	                    int occurrence = allNames.get(fieldName) + 1;
	                    allNames.put(fieldName, occurrence);
	                    fieldName = String.format("%s (%d)", fieldName, occurrence);
	                }

	                HashMap<String, String> fieldData = getRecordHashMap(i, fieldName);
	                fields.add(fieldData);
	            }
	        } else {
	            int occurs = i.getOccurs();
	            if (occurs < 0) {
	                occurs = 1;
	            }

	            for (int j = 0; j < occurs; j++) {
	                // Corrected recursive call: pass 'i' instead of 'items'
	                getIntermediateList(i, fields, allNames);
	            }
	        }
	    }
	    System.out.println("fields are : "+ fields);
//	    Thread.sleep(10000);
	    return fields;
	}
	
	/***********************************
	*get cobol variable metadata as hashmap
	*****************************************/
	public HashMap<String, String> getRecordHashMap(IItemDetails item, String fieldName){
		HashMap<String, String> hm = new HashMap();
//		String fieldValue = calculateFieldValue(fieldName, item.getValue(), item.getDisplayLength(),item.getStorageLength(), item.getType(), item.getPicture());
		
		hm.put("name", fieldName);
//		hm.put("value", fieldValue);
		hm.put("storage_length", Integer.toString(item.getStorageLength()));
		hm.put("display_length", Integer.toString(item.getDisplayLength()));
		hm.put("type_id", Integer.toString(item.getType()));
		hm.put("pic", item.getPicture());
		
		return hm;
	}
	
	public static void printObjectDetails(Object obj) {
	    System.out.println("-----------------------------------------------------------");
	    if (obj instanceof String) { // Direct handling for Strings
	        System.out.println("Instance of String: " + obj);
	    } else {
	        Class<?> objClass = obj.getClass();
	        System.out.println("Class Name: " + objClass.getName());
	        Field[] fields = objClass.getDeclaredFields();

	        for (Field field : fields) {
	            field.setAccessible(true); // if fields are private
	            try {
	                System.out.println(field.getName() + ": " + field.get(obj));
	            } catch (IllegalArgumentException | IllegalAccessException e) {
	                System.out.println("Error getting field value: " + e.getMessage());
	            }
	        }
	    }
	    System.out.println("---------------------------------------------------------------");
	}
	
}
