/**
 * 
 */
package analyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
//ArrayList;
//import java.util.Collections;
//import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
//import java.util.HashSet;
//import java.util.Arrays;
//import java.util.LinkedHashMap;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;

import json.JSONArray;
import json.JSONException;
import json.JSONObject;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;
import org.tartarus.snowball.ext.porterStemmer;

import structures.Post;
import structures.docInfo;
import structures.wordInfo;

/**
 * @author hongning
 * Sample codes for demonstrating OpenNLP package usage 
 * NOTE: the code here is only for demonstration purpose, 
 * please revise it accordingly to maximize your implementation's efficiency!
 */
public class DocAnalyzer implements Serializable{
	public TokenizerModel tokenizerMaker;
	//a list of stopwords
	HashSet<String> m_stopwords;
	
	//you can store the loaded reviews in this arraylist for further processing
	ArrayList<Post> m_reviews;
	
	public Map<String, wordInfo> wordMap;

	public int totDocNum = 0;
    public int totPosNum = 0;

    public void incPosCount() {
        totPosNum += 1;
    }
	
	//you might need something like this to store the counting statistics for validating Zipf's and computing IDF
	//HashMap<String, Token> m_stats;	
	
	//we have also provided sample implementation of language model in src.structures.LanguageModel
	
	public DocAnalyzer() {
		m_reviews = new ArrayList<Post>();
		try {
			tokenizerMaker = new TokenizerModel(new FileInputStream("./data/Model/en-token.bin"));
            wordMap = new ConcurrentHashMap<>();
		}
		catch (IOException e) {
		  e.printStackTrace();
		}		
	}
	
	//sample code for loading a list of stopwords from file
	//you can manually modify the stopword file to include your newly selected words
//	public void LoadStopwords(String filename) {
//		try {
//			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"));
//			String line;
//
//			while ((line = reader.readLine()) != null) {
//				//it is very important that you perform the same processing operation to the loaded stopwords
//				//otherwise it won't be matched in the text content
//				line = SnowballStemmingDemo(NormalizationDemo(line));
//				if (!line.isEmpty())
//					m_stopwords.add(line);
//			}
//			reader.close();
//			System.out.format("Loading %d stopwords from %s\n", m_stopwords.size(), filename);
//		} catch(IOException e){
//			System.err.format("[Error]Failed to open file %s!!", filename);
//		}
//	}
	
	public void analyzeDocumentDemo(JSONObject json, Boolean testDirec) {
		try {
            TokenizerME tokenizer = new TokenizerME(tokenizerMaker);
            SnowballStemmer stemmer = new englishStemmer();
            JSONArray jarray = json.getJSONArray("Reviews");
			for(int i=0; i<jarray.length(); i++) {
				Post review = new Post(jarray.getJSONObject(i));
				String[] tokens = tokenizer.tokenize(review.getContent());
				for (int j = 0; j<tokens.length; j++) {
					tokens[j] = NormalizationDemo(tokens[j]);
					tokens[j] = SnowballStemmingDemo(tokens[j], stemmer);
					if (!tokens[j].equals("")) {
                        if (wordMap.containsKey(tokens[j]))
                            wordMap.get(tokens[j]).updateInfo(review.getID(), review.getRating(), testDirec);
                        else
                            wordMap.put(tokens[j], new wordInfo(review.getID(), review.getRating(), testDirec));
					}						
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	//sample code for loading a json file
	public JSONObject LoadJson(String filename) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"));
			StringBuffer buffer = new StringBuffer(1024);
			String line;
			
			while((line=reader.readLine())!=null) {
				buffer.append(line);
			}
			reader.close();
			
			return new JSONObject(buffer.toString());
		} catch (IOException e) {
			System.err.format("[Error]Failed to open file %s!", filename);
			e.printStackTrace();
			return null;
		} catch (JSONException e) {
			System.err.format("[Error]Failed to parse json file %s!", filename);
			e.printStackTrace();
			return null;
		}
	}
	
	// sample code for demonstrating how to recursively load files in a directory 
	public void LoadDirectory(String folder, String suffix, boolean testDirectory) {
		File dir = new File(folder);
        ArrayList<File> fileList = new ArrayList<File>(Arrays.asList(dir.listFiles()));
		int size = fileList.size();
        int counter = 1;
        fileList.stream()
                .parallel()
                .forEach(f -> {
                    if (f.isFile() && f.getName().endsWith(suffix)) {
                        analyzeDocumentDemo(LoadJson(f.getAbsolutePath()), testDirectory);
                    }
                });
//			else if (f.isDirectory())
//				LoadDirectory(f.getAbsolutePath(), suffix, Boolean.FALSE);

//		hm = sortByValues(hm);
//		hmdf = sortByValues(hmdf);
	}

	//sample code for demonstrating how to use Snowball stemmer
	public static String SnowballStemmingDemo(String token, SnowballStemmer stemmer) {
		stemmer.setCurrent(token);
		if (stemmer.stem())
			return stemmer.getCurrent();
		else
			return token;
	}
	
	//sample code for demonstrating how to use Porter stemmer
	public String PorterStemmingDemo(String token) {
		porterStemmer stemmer = new porterStemmer();
		stemmer.setCurrent(token);
		if (stemmer.stem())
			return stemmer.getCurrent();
		else
			return token;
	}
	
	//sample code for demonstrating how to perform text normalization
	public static String NormalizationDemo(String token) {
		// remove all non-word characters
		// please change this to removing all English punctuation
		token = token.replaceAll("\\W+", ""); 
		
		// convert to lower case
		token = token.toLowerCase();
		
		// add a line to recognize integers and doubles via regular expression
		// and convert the recognized integers and doubles to a special symbol "NUM"
        token = token.replaceAll("\\d+","NUM");
		
		return token;
	}
	
//	public void TokenizerDemon(String text) {
//		try {
//
//			/**
//			 * HINT: instead of constructing the Tokenizer instance every time when you perform tokenization,
//			 * construct a global Tokenizer instance once and evoke it everytime when you perform tokenization.
//			 */
//			Tokenizer tokenizer = new TokenizerME(new TokenizerModel(new FileInputStream("./data/Model/en-token.bin")));
//
//			System.out.format("Token\tNormalization\tSnonball Stemmer\tPorter Stemmer\n");
//			for(String token:tokenizer.tokenize(text)){
//				System.out.format("%s\t%s\t%s\t%s\n", token, NormalizationDemo(token), SnowballStemmingDemo(token), PorterStemmingDemo(token));
//			}
//		}
//		catch (IOException e) {
//		  e.printStackTrace();
//		}
//	}
	
	public HashMap sortByValues(HashMap map) { 
	       List list = new LinkedList(map.entrySet());
	       // Defined Custom Comparator here
	       Collections.sort(list, new Comparator() {
	            public int compare(Object o1, Object o2) {
	               return (-1*((Comparable) ((Map.Entry) (o1)).getValue())
	                  .compareTo(((Map.Entry) (o2)).getValue()));
	            }
	       });

	       // Here I am copying the sorted list in HashMap
	       // using LinkedHashMap to preserve the insertion order
	       HashMap sortedHashMap = new LinkedHashMap();
	       for (Iterator it = list.iterator(); it.hasNext();) {
	              Map.Entry entry = (Map.Entry) it.next();
	              sortedHashMap.put(entry.getKey(), entry.getValue());
	       } 
	       return sortedHashMap;
	  }

	
	public static void main(String[] args) throws IOException {
        double startTime = System.nanoTime();
		DocAnalyzer analyzer = new DocAnalyzer();
		String path1 = "C:\\Users\\cheta_000\\Dropbox\\College\\3_Third Year\\06_Sixth Semester\\CS 6501\\MP2\\Yelp_small\\train";
//        String path2 = "C:\\Users\\cheta_000\\Dropbox\\College\\3_Third Year\\06_Sixth Semester\\CS 6501\\MP2\\Yelp_small\\test";
		analyzer.LoadDirectory(path1, ".json", Boolean.FALSE);
//        analyzer.LoadDirectory(path2, ".json", Boolean.TRUE);

        Map<String, Integer> docPoses = new ConcurrentHashMap<>();
        analyzer.wordMap.values().stream().parallel()
                .forEach(p -> {
                    p.docsInfo.entrySet().stream()
                            .forEach(q -> docPoses.put(q.getKey(), q.getValue().pos ? 1 : 0));

                });
        analyzer.totDocNum = docPoses.size();
        analyzer.totPosNum = docPoses.values().stream().mapToInt(Integer::intValue).sum();
        docPoses.clear();
        System.out.println(analyzer.totDocNum + " " + analyzer.totPosNum);
        System.out.println((System.nanoTime() - startTime)/1e9 + " seconds to load documents");
        startTime = System.nanoTime();

        SnowballStemmer stemr = new englishStemmer();
        Set stopwords = Files.lines(Paths.get("./data/stopwords.txt")).
                map(p -> analyzer.SnowballStemmingDemo(analyzer.NormalizationDemo(p), stemr)).
                collect(Collectors.toSet());
        analyzer.wordMap = analyzer.wordMap.entrySet().stream()
                .filter(p ->  p.getValue().getDocFreq() > 50 && !stopwords.contains(p.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        analyzer.wordMap.values().forEach(p -> {
            p.saveChiSq(analyzer.totDocNum, analyzer.totPosNum);
            p.saveInfoGain(analyzer.totDocNum, analyzer.totPosNum);
        });
        Map<String, wordInfo> igLikes = analyzer.wordMap.entrySet().stream()
                .sorted((p1, p2) -> Double.compare(p2.getValue().getIG(), p1.getValue().getIG()))
                .limit(5000)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        System.out.println("Size of ig vocab:" + igLikes.size());
        Map<String, wordInfo> chisqLikes = analyzer.wordMap.entrySet().stream()
                .filter(p -> p.getValue().getChiSq() > 3.841)
                .sorted((p1, p2) -> Double.compare(p2.getValue().getChiSq(), p1.getValue().getChiSq()))
                .limit(5000)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        System.out.println("Size of chisq vocab:" + chisqLikes.size());
        chisqLikes.entrySet().stream()
                .sorted((p1, p2) -> Double.compare(p2.getValue().getChiSq(), p1.getValue().getChiSq()))
                .limit(20)
                .forEach(p -> {
                    System.out.print(p.getKey() + ": " + p.getValue().getChiSq() + ", ");
                });
        System.out.println("\nCHI ABOVE IG BELOW");
        igLikes.entrySet().stream()
                .sorted((p1, p2) -> Double.compare(p2.getValue().getIG(), p1.getValue().getIG()))
                .limit(20)
                .forEach(p -> {
                    System.out.print(p.getKey() + ": " + p.getValue().getIG() + ", ");
                });
        System.out.println("");
        analyzer.wordMap = analyzer.wordMap.entrySet().stream()
                .filter(p -> igLikes.containsKey(p.getKey()) | chisqLikes.containsKey(p.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        System.out.println("Size of controlled vocab:" + analyzer.wordMap.size());

        System.out.println("\n" + (System.nanoTime() - startTime)/1e9 + " seconds to manipulate wordMap");
        startTime = System.nanoTime();

        ObjectOutputStream anal = new ObjectOutputStream(Files.newOutputStream(Paths.get("./data/wordMap.JData")));
        anal.writeObject(analyzer.wordMap);
        anal.close();

        System.out.println((System.nanoTime() - startTime)/1e9 + " seconds saving wordMap to disk");
	}

}
