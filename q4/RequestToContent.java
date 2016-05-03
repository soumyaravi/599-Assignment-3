import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.xml.sax.SAXException;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

public class RequestToContent {
	static int count=0;
	static String output = "D:\\Big Data\\Assignment 3\\request_to_content.json";
	static PrintWriter extract=null,store=null;
	static HashMap<String, Long> request = new HashMap<String, Long>();
	static HashMap<String, Long> response = new HashMap<String, Long>();
	static HashMap<String, Long> named = new HashMap<String, Long>();
	static ArrayList<String> detect_request = new ArrayList<String>();
	static ArrayList<String> detect_response = new ArrayList<String>();
	static ArrayList<String> ner = new ArrayList<String>();
	static String status=null;
	static long request_value=0,response_value;
	static int host = 0;
	static String request_url=null,hostname=null,main_content="";
	public static void main(String[] args) throws NullPointerException, IOException, SAXException, TikaException {
		// TODO Auto-generated method stub
		File out_file = new File(output);
		if(out_file.exists())
			out_file.delete();
			choice("D:\\cca\\572-team1");
			System.out.println(count);
	}
	public static void choice(String from)throws IOException,SAXException, TikaException, NullPointerException {
		 final File directory = new File(from);
		 //recursive parsing to get to file
			if(directory.isDirectory()){
		   for(File fileOrFolder : directory.listFiles()){
		       if(fileOrFolder.isDirectory()){
		          for (File singleFileOrFolder : fileOrFolder.listFiles()){
		        	 if(count<5000)
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
		    	    String meta[] = metadata.names();
		    	    for(int i=0;i<meta.length;i++){
		    	    	String met_content = metadata.get(meta[i]).trim();
		    	    	
		    	    if(!meta[i].contains("Content-Encoding") && !meta[i].contains("X-") && !meta[i].contains("Content-Type") && !meta[i].contains("url"))
		    	    {
		    	    	met_content=met_content.replaceAll("[^a-zA-Z0-9]+", " ");
		    	    	//met_content=met_content.replaceAll("[", "");
		    	    	//met_content=met_content.replaceAll("{", "");
		    	    	//System.out.println(meta[i]+":"+met_content);
		    	    	if(!met_content.contains("http"))
		    	    	main_content+=met_content;
		    	    }
		    	    }
		    	    
		    	      	    }catch(Exception e){
		    	    	continue;
		    	    }
		   storeObject(handler.toString());
		   findPath(fileOrFolder.toString());
			}
		   }
			}
	}
	//temporarily stores extracted text on file
	public static void storeObject(String temp){
		
		try {
			store = new PrintWriter(new BufferedWriter(new FileWriter("temp")));
			
			temp = temp.replaceAll("\t", " ");
			temp = temp.trim();
			store.write(temp);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		catch(Exception ie){}
		finally{
			try{
				store.close();
			} catch (Exception ex){
				
			}
		}
	}
	public static  void findPath(String fileName) throws FileNotFoundException, IOException,NullPointerException{
		  BufferedReader br= new BufferedReader(new FileReader("temp"));
		  String line ="";
		  host=0;
		  System.out.println(fileName);
		  while(null!=(line=br.readLine())){
			  //detect_request request and response url
			  findURL(line);
			//detect_request content type
			  contentType(line);
			  //System.out.println("in");
		  }
		//detect_request content in page  
			  getContent(main_content);
		  count++;
		  System.out.println(count);
		  //if(count%10==0)
  	    	writeToFile();
		  br.close();
	}
	
	private static void findURL(String line) {
		// TODO Auto-generated method stub
		
		if(line.contains("url")){
			String url[]=line.split("\"");
			if(url.length>=4)
			request_url = url[3];
			
		}
		if(line.contains("hostname")){
			
			String url[]=line.split("\"");
			if(host==1){
			hostname = url[3];	
			/*if(request_url.contains(hostname)){
				System.out.println(request_url+"\n"+hostname);
			}*/
			}
			host++;
		}
		
	}
	private static void contentType(String line) {
		// TODO Auto-generated method stub
		if(line.contains("headers")){
			  String type[] = line.split("\"");
			  for(int i=0;i<type.length;i++){
				  //only checking for request and not response
				  //System.out.println(type[i]+" "+i);
				  if(type[i].equalsIgnoreCase("accept")){
					  line = type[i+2];
					  String content[]=line.split(",");
					  for(int j=0;j<content.length-1;j++)
					  {
						  if(content[j].contains(";"))
							  {
							  if(request.containsKey(content[j].substring(0, content[j].indexOf(";")))){
					    	    	request_value = request.get(content[j].substring(0, content[j].indexOf(";")))+1;
					    	    }
					    	    else{
					    	    	request_value=1;
					    	    	detect_request.add(content[j].substring(0, content[j].indexOf(";")));
					    	    }
					    	    request.put(content[j].substring(0, content[j].indexOf(";")), request_value);
							  }
						  else
						  {
							  if(request.containsKey(content[j])){
					    	    	request_value = request.get(content[j])+1;
					    	    }
					    	    else{
					    	    	request_value=1;
					    	    	detect_request.add(content[j]);
					    	    }
					    	    request.put(content[j], request_value);
							  }
						  }
					  
				  }
				  if(type[i].equalsIgnoreCase("content-type")){
					  if(type[i+2].contains(";")){
						  if(response.containsKey(type[i+2].substring(0, type[i+2].indexOf(";")))){
							  response_value = response.get(type[i+2].substring(0, type[i+2].indexOf(";")))+1;
				    	    }
				    	    else{
				    	    	response_value=1;
				    	    	detect_response.add(type[i+2].substring(0, type[i+2].indexOf(";")));
				    	    }
				    	    response.put(type[i+2].substring(0, type[i+2].indexOf(";")), response_value);
				    	    System.out.println("i+2:"+type[i]+" "+type[i+2]);
						  }
					  else{
						  if(response.containsKey(type[i+2])){
				    	    	response_value = response.get(type[i+2])+1;
				    	    }
				    	    else{
				    	    	response_value=1;
				    	    	detect_response.add(type[i+2]);
				    	    }
				    	    response.put(type[i+2], response_value);
				    	    System.out.println("i"+type[i]+" "+type[i+2]);
						  }
				  }
			  }
		}
	}
	private static void getContent(String line) throws FileNotFoundException{
		InputStream modelIn = new FileInputStream("D:\\Big data\\en-token.bin");
		//InputStream percent = new FileInputStream("D:\\Big data\\en-ner-percentage.bin");
		try{
			 TokenNameFinderModel location = new TokenNameFinderModel(new FileInputStream("D:\\Big data\\en-ner-location.bin"));
			 TokenNameFinderModel person = new TokenNameFinderModel(new FileInputStream("D:\\Big data\\en-ner-person.bin"));
			 TokenNameFinderModel org = new TokenNameFinderModel(new FileInputStream("D:\\Big data\\en-ner-organization.bin"));
			 TokenNameFinderModel money = new TokenNameFinderModel(new FileInputStream("D:\\Big data\\en-ner-money.bin"));
			 TokenNameFinderModel date = new TokenNameFinderModel(new FileInputStream("D:\\Big data\\en-ner-date.bin"));
			 TokenNameFinderModel time = new TokenNameFinderModel(new FileInputStream("D:\\Big data\\en-ner-time.bin"));
			 NameFinderME nameFinder = new NameFinderME(location);
			 TokenizerModel model = new TokenizerModel(modelIn);
			 Tokenizer tokenizer = new TokenizerME(model);
			 String tokens[] = tokenizer.tokenize(line);
			 Span loc[] = nameFinder.find(tokens);
			 if(loc.length>0){
			 for(int i=0;i<loc.length;i++){
				 if(!tokens[i].contains(",") && !named.containsKey(tokens[i])){
					 ner.add("Location");
					 long val =1;
					 named.put(tokens[i], val);
					 i=loc.length;
				 }
			 }
			 }
			 for(int i=0;i<tokens.length;i++)
			 {
				 if(tokens[i].equalsIgnoreCase("moved"))
					 status="Moved";
				 else if(tokens[i].matches(""))
					status="400 error";
					else
						status="200 ok";
			 }
			 nameFinder.clearAdaptiveData();
			 nameFinder = new NameFinderME(person);
			 
			 loc = nameFinder.find(tokens);
			 if(loc.length!=0){
			 for(int i=0;i<loc.length;i++){
				 if(!tokens[i].contains(",") && !named.containsKey(tokens[i])){
					 ner.add("Person");
					 long val =1;
					 named.put(tokens[i], val);
					 i=loc.length;
				 }
				 }
			 }
			 nameFinder.clearAdaptiveData();
			 nameFinder = new NameFinderME(org);			 
			 loc = nameFinder.find(tokens);
			 if(loc.length!=0){
			 for(int i=0;i<loc.length;i++){
				 if(!tokens[i].contains(",") && !named.containsKey(tokens[i])){
					 ner.add("Organization");
					 long val =1;
					 named.put(tokens[i], val);
					 i=loc.length;
				 }
				 }
			 }
			 nameFinder.clearAdaptiveData();
			 nameFinder = new NameFinderME(money);
			 
			 loc = nameFinder.find(tokens);
			 if(loc.length!=0){
				 for(int i=0;i<loc.length;i++){
					 if(!tokens[i].contains(",") && !named.containsKey(tokens[i])){
						 ner.add("Money");
						 long val =1;
						 named.put(tokens[i], val);
						 i=loc.length;
					 }
			 }
			 }
			 nameFinder.clearAdaptiveData();
nameFinder = new NameFinderME(date);
			 
			 loc = nameFinder.find(tokens);
			 if(loc.length!=0){
				 for(int i=0;i<loc.length;i++){
					 if(!tokens[i].contains(",") && !named.containsKey(tokens[i])){
						 ner.add("Date");
						 long val =1;
						 named.put(tokens[i], val);
						 i=loc.length;
					 }
			 }
			 }
			 nameFinder.clearAdaptiveData();
nameFinder = new NameFinderME(time);
			 
			 loc = nameFinder.find(tokens);
			 if(loc.length!=0){
				 for(int i=0;i<loc.length;i++){
					 if(!tokens[i].contains(",") && !named.containsKey(tokens[i])){
						 ner.add("Time");
						 long val =1;
						 named.put(tokens[i], val);
						 i=loc.length;
					 }
			 }
			 }
			System.out.println("Size:"+ner.size());
			 modelIn.close();
		}
			 
		catch (IOException e) {
			  e.printStackTrace();
			}
			
	}
	private static void writeToFile() throws IOException {
		if(request_url.contains(hostname) && ner.size()!=0){
		JSONObject obj = new JSONObject();
		JSONArray request_content = new JSONArray();
		for(int i=0;i<detect_request.size();i++)
		request_content.add(detect_request.get(i));
		
		JSONArray response_content = new JSONArray();
		for(int i=0;i<detect_response.size();i++)
		response_content.add(detect_response.get(i));
		
		JSONArray named_entity = new JSONArray();
		for(int i=0;i<ner.size();i++)
			named_entity.add(ner.get(i));
		ner.clear();
		obj.put("Request url", request_url);
		obj.put("Response url", hostname);
		obj.put("Request Content Type", request_content);
		obj.put("Response Content Type", response_content);
		obj.put("Named Entities", named_entity);
		obj.put("Status", status);
		try (FileWriter file = new FileWriter(output,true)) {
			file.write(obj.toJSONString()+",");
			System.out.println("Successfully Copied JSON Object to File...");
			System.out.println("\nJSON Object: " + obj);
		}
		obj.clear();
		}
		/*extract = new PrintWriter(new BufferedWriter(new FileWriter(output)));
		for(int i=0;i<lang_detect.size();i++){
			String temp = detect_request.get(i);
			//System.out.println(fileName);
			System.out.println(temp+","+lang_detect.get(temp));
			extract.println(temp+","+lang_detect.get(temp));
		}		
			//System.out.println(count);
		extract.close();*/
	}
}
