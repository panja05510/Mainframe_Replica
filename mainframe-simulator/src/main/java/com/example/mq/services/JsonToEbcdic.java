package com.example.mq.services;

import com.ibm.as400.access.AS400PackedDecimal;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionMessage.ItemsBuilder;
import org.springframework.core.io.ClassPathResource;

import jakarta.annotation.Resource;
import net.sf.JRecord.JRecordInterface1;
import net.sf.JRecord.Details.RecordDetail;
import net.sf.JRecord.External.base.ExternalConversion;
import net.sf.JRecord.def.IO.builders.ICobolIOBuilder;
import net.sf.JRecord.detailsBasic.IItemDetails;
import net.sf.cb2xml.analysis.ItemBuilder;
import net.sf.cb2xml.sablecc.node.ABlankWhenZeroClause;
import net.sf.cb2xml.sablecc.node.Switch;

public class JsonToEbcdic {
	
	private final int ZERO = 0;
	private final HashMap<String, String> cobolNameValueMap;
	private final List<String> leaveAsHexFieldnameList;
	
	//constructor
	public JsonToEbcdic(HashMap<String, String> cobolNameValueMap, List<String> leaveAsHexFieldnameList) {
		this.cobolNameValueMap = cobolNameValueMap;
		this.leaveAsHexFieldnameList = leaveAsHexFieldnameList;
	}
	
	
	/************************************
	* convert request to aminframe format (byte array)
	* **********************************/
	
	public byte[] request2mainframe(String copybook) {
		ArrayList<HashMap<String,String>> intermediate_map = copybookToIntermediate(copybook);
		return getFixedLengthOutput(intermediate_map);
	}
	
	/*************************************************************
	*convert copybook to intermediate format (arraylist of hashmap)
	*************************************************************/
	public ArrayList<HashMap<String, String>> copybookToIntermediate(String copybookName)
	{
		try {
			ClassPathResource resource = new ClassPathResource(copybookName);
			InputStream inputStream = resource.getInputStream();
			ICobolIOBuilder iob = JRecordInterface1.COBOL.newIOBuilder(inputStream,copybookName);
			RecordDetail record = iob.getLayout().getRecord(0);
			IItemDetails root = record.getCobolItems().get(0);
			return getIntermediateList(root,new ArrayList<>(), new HashMap<>());
		}
		catch(Exception e) {
			System.out.println("error occured at JsonToEbcdic-->copybookToIntermedaite()" + e);
		}
	}
	
	/*******************************************************************************************
	*Recursive method to iterate through copybook tree and extract metadata for cobol variables
	*each symbol leaf value will be stored in hashmap
	*all hashmaps will go into arraylist
	*****************************************/
	
	public ArrayList<HashMap<String, String>> getIntermediateList(IItemDetails items, ArrayList<HashMap<String, String>> fields, HashMap<String, Integer> allNames)
	{
		if(items == null) {
			return null;
		}
		
		for(IItemDetails i : items.getChildItems()) {
			String fieldName = i.getFieldName();
			
			if(i.isLeaf()) {
				if(!i.isFieldRedefined()) {
					if(i.isFieldRedefined()) {
						fieldName = i.getRedefinesFieldName();
					}
					
					if(!allNames.containsKey(fieldName))
						allNames.put(fieldName, 0);
					else {
						int occurance = allNames.get(fieldName)+1;
						allNames.put(fieldName, occurance);
						fieldName = String.format("%s (%d)", fieldName, occurance);
					}
					
					//if cobol variable has occurs value
					if(i.getOccurs() > 1) {
						
						//add fieldname (k) to hashmap
						for(int k=0; k<i.getOccurs(); k++) {
							String fieldNameOccurance = String.format("%s (%d)", fieldName, k );
							HashMap<String, String>  hm=getRecordHashMap(i,fieldNameOccurance);
							fields.add(hm);
						}
					}
					else {
						HashMap<String, String> hm = getRecordHashMap(i,fieldName);
						fields.add(hm);
					}
				}
			}
			else {
				int occurs = i.getOccurs();
				if(occurs < 0) occurs=1;
				
				for(int j = 0; j< occurs; j++) {
					getIntermediateList(items, fields, allNames);
				}
			}
		}
		return fields;
	}
	
	/***********************************
	*get cobol variable metadata as hashmap
	*****************************************/
	public HashMap<String, String> getRecordHashMap(IItemDetails item, String fieldName){
		HashMap<String, String> hm = new HashMap();
		String fieldValue = calculateFieldValue(fieldName, item.getValue(), item.getDisplayLength(),item.getStorageLength(), item.getType(), item.getPicture());
		
		hm.put("name", fieldName);
		hm.put("value", fieldValue);
		hm.put("storage_length", Integer.toString(item.getStorageLength()));
		hm.put("display_length", Integer.toString(item.getDisplayLength()));
		hm.put("type_id", Integer.toString(item.getType()));
		hm.put("pic", item.getPicture());
		
		return hm;
	}
	/*******************************************
	 * calcualte value for cobol varibles lef-pad numbers with zeros right-pad char
	 * types with spaces pass in default of zero/space for variables non-required
	 * request fields
	 ********************************************/
	
	public String calculateFieldValue(String fieldName, String fieldValue, int displayLength, int storageLength, int typeId, String pic) {
		StringBuilder valueToAdd = new StringBuilder();
		
		if(cobolNameValueMap.containsKey(fieldName)) {
			if(cobolNameValueMap.get(fieldName) == null) {
				String messageDetail = fieldName+" is mapped with null value. It cannot be null";
			}
			valueToAdd = new StringBuilder(cobolNameValueMap.get(fieldName));
			int valueInitialLen = valueToAdd.length();
			
			//ifnumeric type
			if(typeId == NUMB_TYPE || typeId == SIGNED_NUMB_TYPE || typeId == V907_TYPE) {
				for(int j=0; j<storageLength-valueInitialLen; j++) {
					valueToAdd.insert(0, ZERO);
				}
			}
			
			else if(typeId == CHAR_TYPE) {
					valueToAdd.append(BLANK.repeat(Math.max(0,storageLength- valueInitialLen)));
			}
		}
		
		//pass in default/blank values for non-request fields
		else {
			if(fieldValue != null)
				return fieldValue.replace("\"", "");
			
			ExternalConversion.getTypeAsString(0, 31);
			switch (typeId){
			case CHAR_TYPE: {
				valueToAdd.append(ABlankWhenZeroClause.repeat(Math.max(0, storageLength)));
				break;
			}
			case SIGNED_NUMB_TYPE:
			case NUMB_TYPE:
			case V907_TYPE:
				for(int j=0;j<storageLength;j++)
					valueToAdd.insert(0, ZERO);
				break;
			default:
				valueToAdd = new StringBuilder(ZERO);
				break;
			}
			
		}
		return valueToAdd.toString();
		
	}
	
	/*******************************************
	 * Get fixed length output as byte array
	 *****************************************/
	public byte[] getFixedLengthOutput(ArrayList<HashMap<String, String>> intermediate_map) {
		try {
			StringBuilder fixedOutputEbcdic = new StringBuilder();
			
			for(HashMap<String, String> hm : intermediate_map) {
				int typeId = Integer.parseInt(hm.get("type_id"));
				String cobolvalue = hm.get("value");
				String pic = hm.get("picc");
				String fieldName = hm.get("name");
				
				switch (typeId) {
					case SIGNED_NUMB_TYPE:
					case NUMB_TYPE:
					case CHAR_TYPE:
					{
						if(leaveAsHexFieldnameList.contains(fieldName)) {
							StringBuilder builder = new StringBuilder();
							for(int i=0; i<cobolvalue.length(); i+=2) {
								String str = cobolvalue.substring(i,i+2);
								builder.append((char) Integer.parseInt(str,16));
							}
							fixedOutputEbcdic.append(builder.toString());
						} else {
							fixedOutputEbcdic.append( convertFormat(cobolValue, LATIN_1_CHARSET, EBCDIC_CHARSET) );
						}
						break;
					}
					case COMP_TYPE:
						String bytesSizeStr = pic.substring(pic.indexOf("(")+1,pic.indexOf(")")).trim();
						int bytesSize = Integer.parseInt(bytesSizeStr);
						
						if(bytesSize <= MAX_SHORT_SIZE) {
							int value = Integer.parseInt(cobolvalue);
							fixedOutputEbcdic.append(new String(longToBytes(value)));
						}
						break;
					case COMP_TYPE:
						String bytesSizeStr = pic.substring(pic.indexOf("(")+1,pic.indexOf(")")).trim();
						int bytesSize = Integer.parseInt(bytesSizeStr);
						
						if(bytesSize <= MAX_SHORT_SIZE) {
							short value = Short.parseShort(cobolvalue);
							fixedOutputEbcdic.append(new String(intToBytes(value)));
						}
						else if(bytesSize <= MAX_INT_SIZE) {
							int value = Integer.parseInt(cobolvalue);
							fixedOutputEbcdic.append(new String(intToBytes(value)));
						}
						else {
							long value = Long.parseLong(cobolvalue);
							fixedOutputEbcdic.append(new String(longToBytes(value)));
						}
						break;
					case COMP_3_5_TYPE:
					case COMP_3_4_TYPE:
					case COMP_3_3_TYPE:
						int disp = Integer.parseInt(hm.get("display_length"));
						AS400PackedDecimal packedDecimal = new AS400PackedDecimal(disp, 0);
						BigDecimal javaBigDecimal = new BigDecimal(cobolvalue);
						byte[] packedBytes = packedDecimal.toBytes(javaBigDecimal);
						fixedOutputEbcdic.append(new String(packedBytes, LATIN_1_CHARSET));
						break;
					default:
						fixedOutputEbcdic.append(convertFormat(cobolvalue, LATIN_1_CHARSET, EBCDIC_CHARSET));
				}
			}
			return fixedOutputEbcdic.toString().getBytes(LATIN_1_CHARSET);
		} catch(NumberFormatException ex) {
			System.out.println("number format exception at JsonToEbcdic"+ex);
		}
	}
	
	/*********************************
	 * convert string to different format
	 ******************************/
	
	public String convertFormat(String strToConvert, String in, String out) {
		try {
			Charset charset_in = Charset.forName(out);
			Charset charset_out = Charset.forName(in);
			CharsetDecoder decoder = charset_out.newDecoder();
			CharsetEncoder encoder = charset_in.newEncoder();
			CharBuffer uCharBuffer = CharBuffer.wrap(strToConvert);
			ByteBuffer bbuf = encoder.encode(uCharBuffer);
			CharBuffer cbuf = decoder.decode(bbuf);
			return cbuf.toString();
		}
		catch(CharacterCodingException ex) {
			System.out.println("character coding exception occur at JsonToEbcdic" + ex);
			return "";
		}
	}
	
	/***************************
	 * convert short to byte array
	 **************************/
	
	public byte[] shortToBytes(short x) {
		ByteBuffer buffer = ByteBuffer.allocate(Short.BYTES);
		buffer.putShort(x);
		return buffer.array();
	}
	
	/*
	 * convert int to byte array
	 */
	
	public byte[] intToBytes(int x) {
		ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
		buffer.putInt(x);
		return buffer.array();
	}
	
	/*
	 * convert long to bytes array
	 */
	public byte[] longToBytes(long x) {
		ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
		buffer.putLong(x);
		return buffer.array();
	}
	
}
