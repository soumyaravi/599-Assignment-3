import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.tika.exception.TikaException;
import org.apache.tika.language.LanguageIdentifier;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

public class LanguageDetect {
	static int count=0;
	static String output = "D:\\Big Data\\Assignment 3\\language_diversity.csv";
	static PrintWriter extract=null;
	static HashMap<String, Long> lang_detect = new HashMap<String, Long>();
	static ArrayList<String> detect = new ArrayList<String>();
	static HashMap<String,String> content = new HashMap<String,String>();
	public static void main(String[] args) throws NullPointerException, IOException, SAXException, TikaException {
		File out_file = new File(output);
		if(out_file.exists())
			out_file.delete();
		choice("D:\\download\\");
		System.out.println(count);
		writeToFile(lang_detect);
	}
	public static void choice(String from)throws IOException,SAXException, TikaException, NullPointerException {
		 final File directory = new File(from);
		// String fileName=null;
		 //recursive parsing to get to file
			if(directory.isDirectory()){
		   for(File fileOrFolder : directory.listFiles()){
		       if(fileOrFolder.isDirectory()){
		          for (File singleFileOrFolder : fileOrFolder.listFiles()){
		        	  //if(count<2000)
		        		  choice(singleFileOrFolder.toString()); 
		       }
		       }
		   
			
			else{
		    	   FileInputStream inputstream=new FileInputStream(fileOrFolder);
		    	    ParseContext context = new ParseContext();
		    	    Parser parser = new AutoDetectParser();
		    	   BodyContentHandler handler = new BodyContentHandler();
		    	    Metadata metadata = new Metadata();
		    	    
		    	    //parsing the file
		    	    try{
		    	    parser.parse(inputstream, handler, metadata, context);
		    	    String type = metadata.get("Content-Type");
		    	    LanguageIdentifier lang = new LanguageIdentifier(handler.toString());
		    	    if(type.contains(";"))
		    	    	type=type.substring(0, type.indexOf(';'));
		    	    long value=0;
		    	    if(content.containsKey(type) && content.containsValue(lang.getLanguage())){
		    	    if(lang_detect.containsKey(type+","+lang.getLanguage())){
		    	    	value = lang_detect.get(type+","+lang.getLanguage())+1;
		    	    }
		    	    }
		    	    else{
		    	    	value=1;
		    	    	detect.add(type+","+lang.getLanguage());
		    	    	content.put(type, lang.getLanguage());
		    	    }
		    	    
		    	    lang_detect.put(type+","+lang.getLanguage(), value);
		    	    
		    	    if(count%50==0)
		    	    	writeToFile(lang_detect);
		    	    //System.out.println(fileOrFolder+" "+lang.getLanguage());
		    	    count++;
		    	    }catch(Exception e){
		    	    	continue;
		    	    }
		    	   // writeToFile(lang_detect);
		   
			}
		   }
			}
	}
	private static void writeToFile(HashMap<String, Long> lang_detect) throws IOException {
		 
		extract = new PrintWriter(new BufferedWriter(new FileWriter(output)));
		for(int i=0,j=0;i<lang_detect.size() && j<content.size();i++,j++){
			String temp = detect.get(i);
			System.out.println(temp+","+lang_detect.get(temp));
			extract.println(temp+","+lang_detect.get(temp));
		}		
			System.out.println(count);
		extract.close();
	}
}
