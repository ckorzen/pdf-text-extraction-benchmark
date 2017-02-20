package com.javazoid.functions;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.StringTokenizer;

/**
 * @author Gervase Gallant gervasegallant@yahoo.com
 *
 * Class: File, a collection of static methods to make file access easier.
 */
public class FileFunctions {

	/**
	 * Constructor for File.
	 */
	private FileFunctions() {
		super();
	}

	/**
	 * Method readTextFile. Uses a FileReader to populate a buffer of chars. The chars are appended to a StringBuffer
	 * and returned as a String to the caller. The FileReader uses the default file encoding scheme for the OS.
	 * @param fullPathFilename
	 * @return String
	 * @throws IOException
	 */
	public static String readTextFile(String fullPathFilename) throws IOException {
		StringBuffer sb = new StringBuffer(1024);
		BufferedReader reader = new BufferedReader(new FileReader(fullPathFilename));
				
		char[] chars = new char[1024];
		int numRead = 0;
		while( (numRead = reader.read(chars)) > -1){
			sb.append(String.valueOf(chars,0,numRead));	
		}
		
		reader.close();
		
		return sb.toString();
	}
	
	/**
	 * Method writeFile.
	 * @param contents
	 * @param fullPathFilename
	 * @throws IOException
	 */
	 public static void writeTextFile(String contents, String fullPathFilename) throws IOException{
	 	BufferedWriter writer = new BufferedWriter(new FileWriter(fullPathFilename));
	 	writer.write(contents);
	 	writer.flush();
	 	writer.close();	
	 }
	 
	 
	 public static void writeBinaryFile(byte[] contents, String fullPathFilename) throws IOException{
	 	BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(fullPathFilename));	
	 	bos.write(contents);
	 	bos.flush();
	 	bos.close();
	 	
	 }
	 
	 public static String tail(String fullPathFilename, int charsToRead, String charSet) throws IOException{
	 	if (charSet == null) charSet = "latin1";
	 	RandomAccessFile raf = new RandomAccessFile(fullPathFilename, "r");	
	 	long posToStart = raf.length() - charsToRead;
	 	byte[] bytes = new byte[charsToRead];
	 	
	 	raf.seek(posToStart);
	 	raf.readFully(bytes);
	 	raf.close();
	 	return new String( bytes, charSet);
	 	
	 	
	 }
	 
	 
	 
	/**
	 * Method countWords. This supposes that a word is a space-delimited String. This would be trivial but for
	 * the possibility of multiple spaces between words.
	 * @param fullPathFilename
	 * @return int
	 */
	 public static int countWords(String fullPathFilename) throws IOException{
	 	
		BufferedReader reader = new BufferedReader(new FileReader(fullPathFilename));
				
		String line;	
		char[] chars;
		int wordCount = 0;
		
		while( (line = reader.readLine()) != null){
			
			chars = line.toCharArray();	
			wordCount++;
			
			for (int i = 1 ; i < chars.length - 1; i++){
				if (Character.isSpace(chars[i]) &&  Character.isJavaLetterOrDigit(chars[i+1])) {	
					wordCount++;
					
				} 
			}	
					
		}
		
		reader.close();
		
		return wordCount;
	 	
	 }
	 
	 
	 public static String[][] readCSV(String fullPathFilename, int numToRead) throws IOException{
	 	BufferedReader reader = new BufferedReader(new FileReader(fullPathFilename));
		String line;	
		StringTokenizer st ;
		int size = 0;
		int index = 0;
		int pos = 1; //skip the first position.
		String[][] data;
		String[][] temp;
		final int DEFAULT_SIZE=250;
		final String DELIMITER = ",";
		int maxRecords;
		
		//read the first line to get the size of the array
		line = reader.readLine();
		st= new StringTokenizer(line, DELIMITER);
		while (st.hasMoreElements()){
			st.nextElement();
			size++;	
		}	
		
		//size the array, if parameter not set, read the entire file
		//otherwise stop as requested.
		if (numToRead == 0) { 
			numToRead = DEFAULT_SIZE;
			maxRecords = Integer.MAX_VALUE;	
		} else {
			maxRecords = numToRead;
		}	
		
		data = new String[numToRead][size];
		
		//do it again to add to the array
		st= new StringTokenizer(line, DELIMITER);
		while (st.hasMoreElements()){
			data[0][index]= st.nextElement().toString();
			index++;
		}
		index=0;
		
		//now do a bunch..
		while( (line = reader.readLine()) != null && pos < maxRecords ){
			st= new StringTokenizer(line, DELIMITER);
			while (st.hasMoreElements()){
				if (index == data[0].length) break;
				data[pos][index]= st.nextElement().toString();
				index++;
			}
			index=0;
			pos++;
			
			if (pos == data.length - 1){ //size array if needed.
				temp = new String[data.length + numToRead][size];
				for (int n=0;n < data.length;n++){
					System.arraycopy(data[n],0,temp[n],0,temp[n].length);
				}
				data = temp;	
				
			}		
					
		}
		
		//size the array correctly.. it may be too large.
		temp = new String[pos][size];// the size read
		for (int n=0;n < temp.length;n++){
			System.arraycopy(data[n],0,temp[n],0,temp[n].length);
		}
				
		reader.close();
		return temp;
		
	 }
	 
	 
	 public static void copy(File source, File destination) throws IOException{
	 	
	 	BufferedInputStream bis = new BufferedInputStream(new FileInputStream(source));
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(destination));
	    int numRead;                
        byte[] bytes = new byte[1024];
        while ((numRead = bis.read(bytes)) != -1) {
            bos.write(bytes,0,numRead);
        }
        
        try{
        	bis.close();
        } catch (Exception e){}
        	
        try{
            bos.close();
        } catch (Exception e){}
     }

	 	
	public static void appendToTextFile(String contents, String fullPathFilename) throws IOException{
	 	BufferedWriter writer = new BufferedWriter(new FileWriter(fullPathFilename, true));
	 	writer.write(contents);
	 	writer.flush();
	 	writer.close();
	 	
	 }
	 
}
