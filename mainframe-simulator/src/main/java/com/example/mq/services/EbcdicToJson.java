package com.example.mq.services;


import com.example.mq.messaginglibrary.ResponseAbendError;
import com.example.mq.model.ResponseBaseModel;
import com.google.gson.*;
import net.minidev.json.JSONValue;
import net.sf.JRecord.*;
import net.sf.JRecord.Details.AbstractLine;
import net.sf.JRecord.Details.LayoutDetail;
import net.sf.JRecord.Details.RecordDetail;
import net.sf.JRecord.def.IO.builders.ICobolIOBuilder;
import net.sf.JRecord.detailsBasic.IItemDetails;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class EbcdicToJson {
	
	private final String OPEN_BRACE="{";
	private final String CLOSE_BRACE="{";
	private final ResponseAbendError abendError;
	
	private EbcdicToJson(ResponseAbendError abendError) {
		this.abendError=abendError;
	}
	
	/*******************************************************************
	 * * 	CONVERT MAINFRAME TO JSON
	 ******************************************************************/
	public  JsonObject mainframe2json(byte[] ebcdicBytes,ResponseBaseModel responseBaseModel) {
		try {
			Map<String,Map<String,String>> copybooks = responseBaseModel.getCopybooks();
			String ebcdicLength=Integer.toString(ebcdicBytes.length);
			String copybookName=getCopybookName(ebcdicLength,copybooks);
			
			Resource resource = new ClassPathResource(copybookName);
			InputStream inputStream=resource.getInputStream();
			ICobolIOBuilder iob=JRecordInterface1.COBOL.newIOBuilder(inputStream,copybookName).setFont("CP500");
			AbstractLine line=iob.newLine(ebcdicBytes);
			LayoutDetail layout=iob.getLayout();
			RecordDetail record=layout.getRecord(0);
			IItemDetails root=record.getCobolItems().get(0);
			JsonObject responseOrError=getResponseOrError(ebcdicLength,copybooks,root,layout,line,responseBaseModel);
			return responseOrError;
		}
		catch( Exception e) {
			System.err.println("error accured during parsing Ebcdic To JSON :");
			
		}
		
		
		
	}
	/*************************************************************************************
	   8 Call error method or regular parsing method based on MF response length
	 */

	private JsonObject getResponseOrError(String ebcdicLength, Map<String, Map<String, String>> copybooks,
			IItemDetails root, LayoutDetail layout, AbstractLine line, ResponseBaseModel responseBaseModel) {
		// TODO Auto-generated method stub
		return null;
	}

}
