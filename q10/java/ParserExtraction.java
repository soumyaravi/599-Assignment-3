import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

import java.io.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParserExtraction {
    private static ArrayList<Double> ttr = new ArrayList<Double>();
    static ArrayList<String> measure = new ArrayList<String>();
    static PrintWriter extract = null;
    static String output = "/Users/minhpham/projects/Ass3-ContentDetection-2/data/km-measure.txt";
    static String stripped = null;
    private static int fileCount = 0;
    private static String temp = null;

    public static void main(final String[] args) throws IOException, SAXException, TikaException, NullPointerException {
        String elements[] = {"km", "m", "kg", "g", "mg", "lbs", "tonne", "tons", "cm", "ft", "yard", "hours", "minutes", "mi", "oz", "gal", "K", "C",
                "examples", "followers", "meters", "kilometers", "meters", "inch", "feet", "miles", "ounce", "gallon", "pound", "dollar",
                "kilogram", "kilos", "weeks", "days", "degree", "degrees"};

        String lengthType[] = {"km", "m", "meters", "kilometers"}; //change this for specific measurement type
        String from = "/Users/minhpham/projects/PolarData/text-plain";
        choice(from, lengthType);


        from = "/Users/minhpham/polar/com/twitter"; //change this for specific domain folder data
        choice(from, elements);

        from = "/Users/minhpham/projects/PolarData/text-plain"; //change this for specific MIME type folder data
        choice(from, elements);
    }

    public static void choice(String from, String[] elements) throws IOException, SAXException, TikaException, NullPointerException {
        final File directory = new File(from);
        //recursive parsing to get to file
        if (directory.isDirectory()) {
            for (File fileOrFolder : directory.listFiles()) {
                if (fileOrFolder.isDirectory()) {
                    for (File singleFileOrFolder : fileOrFolder.listFiles()) {
                        choice(singleFileOrFolder.toString(), elements);
                    }
                } else {
                    Tika tika = new Tika();
                    temp = fileOrFolder.toString();
                    String type = tika.detect(fileOrFolder);
                    if (!type.contains("image") || type.contains("video")) {
                        fileCount++;
                        FileInputStream inputStream = new FileInputStream(fileOrFolder);
                        ParseContext context = new ParseContext();
                        Parser parser = new AutoDetectParser();
                        BodyContentHandler handler = new BodyContentHandler();
                        Metadata metadata = new Metadata();
                        //parsing the file
                        try {
                            parser.parse(inputStream, handler, metadata, context);
                        } catch (Exception e) {
                            continue;
                        }
                        storeObject(handler.toString());
                        tagRatio();
                        ner(elements);
                    }
                }
            }
        }
    }

    //implement tag ratios
    public static void tagRatio() throws IOException, NullPointerException {
        BufferedReader br = new BufferedReader(new FileReader("temp"));
        long x, y;
        String line;
        while (null != (line = br.readLine())) {
            x = 0;
            y = 0;
            try {
                Pattern p = Pattern.compile("<(\"[^\"]*\"|'[^']*'|[^'\">])*>");
                Matcher m = p.matcher(line);
                while (m.find())
                    y++;
                stripped = line.replaceAll("<[^>]*>", "");
                if (!(stripped.trim().isEmpty())) {
                    x = stripped.length();
                }
                if (y == 0)
                    y = 1;
                ttr.add((double) x / y);
            } catch (StackOverflowError e) {
                continue;
            } catch (Exception e) {
                break;
            }
        }
        br.close();
    }

    //use OpenNLP NER
    public static void ner(String[] elements) throws FileNotFoundException, StackOverflowError {

        InputStream modelIn = new FileInputStream("en-token.bin");
        BufferedReader br = new BufferedReader(new FileReader("temp"));
        try {
            TokenizerModel model = new TokenizerModel(modelIn);
            Tokenizer tokenizer = new TokenizerME(model);


            for (int i = 0; i < ttr.size(); i++) {
                String line;
                if (null != (line = br.readLine())) {
                    if (ttr.get(i) > 0) {
                        String tokens[] = tokenizer.tokenize(line);
                        for (int j = 0; j < tokens.length; j++) {
                            String regex = "[0-9]+";
                            String dec_regex = "^[0-9]+(\\.[0-9]{1,2})?$";
                            if (tokens[j].matches(regex) || tokens[j].matches(dec_regex)) {

                                if (j != tokens.length - 1) {
                                    String temp = tokens[++j];
                                    for (int k = 0; k < elements.length; k++) {
                                        if (temp.equalsIgnoreCase(elements[k])) {
                                            temp = tokens[j - 1] + " " + temp;
                                            measure.add(temp);
                                        }
                                    }

                                }

                            }
                        }
                    }
                }
            }
            extract = new PrintWriter(new BufferedWriter(new FileWriter(output, true)));
            for (int i = 0; i < measure.size(); i++) {
                extract.println(temp.substring(temp.lastIndexOf("\\") + 1) + "," + measure.get(i));
            }
            measure.clear();
            ttr.clear();
            extract.close();

            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (modelIn != null) {
                try {
                    modelIn.close();
                } catch (IOException e) {
                }
            }
        }

    }

    //temporarily stores extracted text on file

    public static void storeObject(String temp) {

        OutputStream ops;
        ObjectOutputStream objOps = null;
        try {
            ops = new FileOutputStream("temp");
            objOps = new ObjectOutputStream(ops);
            objOps.writeObject(temp);
            objOps.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception ie) {
        } finally {
            try {
                if (objOps != null) objOps.close();
            } catch (Exception ex) {

            }
        }

    }
}