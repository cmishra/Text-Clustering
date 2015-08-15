///**
// *
// */
//package analyzer;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.io.ObjectOutputStream;
//import java.nio.charset.Charset;
//import java.io.*;
//import java.util.*;
//
//import json.JSONArray;
//import json.JSONException;
//import json.JSONObject;
//import opennlp.tools.tokenize.Tokenizer;
//import opennlp.tools.tokenize.TokenizerME;
//import opennlp.tools.tokenize.TokenizerModel;
//
//import org.tartarus.snowball.SnowballStemmer;
//import org.tartarus.snowball.ext.englishStemmer;
//import org.tartarus.snowball.ext.porterStemmer;
//
//import structures.LanguageModel;
//import structures.Post;
//
///**
// * @author hongning Sample codes for demonstrating OpenNLP package usage NOTE:
// *         the code here is only for demonstration purpose, please revise it
// *         accordingly to maximize your implementation's efficiency!
// */
//public class DocAnalyzer2_1 {
//	public Tokenizer tokenizer;
//	// a list of stopwords
//	HashSet<String> m_stopwords;
//	LanguageModel unigram = new LanguageModel(1);
//	LanguageModel bigram = new LanguageModel(2);
//	int totalWords = 0;
//
//	public DocAnalyzer2_1() {
//		try {
//			tokenizer = new TokenizerME(new TokenizerModel(new FileInputStream(
//					"./data/Model/en-token.bin")));
//
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		bigram.m_reference = unigram;
//	}
//
//	// sample code for loading a list of stopwords from file
//	// you can manually modify the stopword file to include your newly selected
//	// words
//	public void LoadStopwords(String filename) {
//		try {
//			BufferedReader reader = new BufferedReader(new InputStreamReader(
//					new FileInputStream(filename), "UTF-8"));
//			String line;
//
//			while ((line = reader.readLine()) != null) {
//				// it is very important that you perform the same processing
//				// operation to the loaded stopwords
//				// otherwise it won't be matched in the text content
//				line = SnowballStemmingDemo(NormalizationDemo(line));
//				if (!line.isEmpty())
//					m_stopwords.add(line);
//			}
//			reader.close();
//			System.out.format("Loading %d stopwords from %s\n",
//					m_stopwords.size(), filename);
//		} catch (IOException e) {
//			System.err.format("[Error]Failed to open file %s!!", filename);
//		}
//	}
//
//	public void analyzeDocumentDemo(JSONObject json) {
//		try {
//			JSONArray jarray = json.getJSONArray("Reviews");
//			for(int i=0; i<jarray.length(); i++) {
//				Post review = new Post(jarray.getJSONObject(i));
//				String[] tokens = tokenizer.tokenize(review.getContent());
//				ArrayList<String> tokens_bigrams = new ArrayList<String>();
//				for (int j = 0; j<tokens.length; j++) {
//					tokens[j] = NormalizationDemo(tokens[j]);
//					tokens[j] = SnowballStemmingDemo(tokens[j]);
//					if (!tokens[j].equals("")) {
//						totalWords++;
//						tokens_bigrams.add(tokens[j]);
//						if (unigram.m_model.containsKey(tokens[j])) {
//							unigram.m_model.put(tokens[j], unigram.m_model.get(tokens[j]) + 1.0);
//						}
//						else {
//							unigram.m_model.put(tokens[j], 1.0);
//						}
//					}
//				}
//
//				for (int j = 0; j < tokens_bigrams.size() - 1; j++) {
//					String twogram = tokens_bigrams.get(j) + " " + tokens_bigrams.get(j+1);
//					if (bigram.m_model.containsKey(twogram)) {
//						bigram.m_model.put(twogram, bigram.m_model.get(twogram) + 1.0);
//					}
//					else {
//						bigram.m_model.put(twogram, 1.0); }
//				}
//
//
//		}
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//	}
//
//	// sample code for loading a json file
//	public JSONObject LoadJson(String filename) {
//		try {
//			BufferedReader reader = new BufferedReader(new InputStreamReader(
//					new FileInputStream(filename), "UTF-8"));
//			StringBuffer buffer = new StringBuffer(1024);
//			String line;
//
//			while ((line = reader.readLine()) != null) {
//				buffer.append(line);
//			}
//			reader.close();
//
//			return new JSONObject(buffer.toString());
//		} catch (IOException e) {
//			System.err.format("[Error]Failed to open file %s!", filename);
//			e.printStackTrace();
//			return null;
//		} catch (JSONException e) {
//			System.err.format("[Error]Failed to parse json file %s!", filename);
//			e.printStackTrace();
//			return null;
//		}
//	}
//
//	// sample code for demonstrating how to recursively load files in a
//	// directory
//	public void LoadDirectory(String folder, String suffix) throws IOException {
//		File dir = new File(folder);
//		for (File f : dir.listFiles()) {
//			if (f.isFile() && f.getName().endsWith(suffix)) {
//				analyzeDocumentDemo(LoadJson(f.getAbsolutePath()));
//			} else if (f.isDirectory())
//				LoadDirectory(f.getAbsolutePath(), suffix);
//		}
//		System.out.println("Loading review documents from "
//				+ folder);
//		unigram.setTotal(totalWords);
//		bigram.calculateSeen();
//		getTopTen();
//
//	}
//
//	public void getTopTen() throws IOException {
//		HashMap<String, Double> linearTop = new HashMap<String, Double>();
//		HashMap<String, Double> absoluteTop = new HashMap<String, Double>();
//		for (Iterator<Map.Entry<String, Double>> it = unigram.m_model.entrySet()
//				.iterator(); it.hasNext(); ) {
//			Map.Entry<String, Double> entry = it.next();
//			String bi = "good" + " " + entry.getKey();
//			linearTop.put(entry.getKey(), bigram.calcLinearSmoothedProb(bi));
//			absoluteTop.put(entry.getKey(), bigram.calcAbsoluteSmoothedProb(bi));
//		}
//
//		linearTop = sortByValues(linearTop, -1);
//		absoluteTop = sortByValues(absoluteTop, -1);
//
//		String path1 = "C:\\Users\\Sugandha\\Documents\\linearTop2.csv";
//		String path2 = "C:\\Users\\Sugandha\\Documents\\absoluteTop2.csv";
//		 FileWriter writer1;
//		 writer1 = new FileWriter(path1, true); //True = Append to file, false
//		 int count = 0;
//		 for (Iterator<Map.Entry<String, Double>> it = linearTop.entrySet()
//					.iterator(); it.hasNext() && count < 10; count++ ) {
//				Map.Entry<String, Double> entry = it.next();
//				writer1.write(entry.getKey());
//				writer1.write(",");
//				writer1.write("" + entry.getValue());
//				writer1.write("\r\n");
//		 }
//		 writer1.close();
//
//		 FileWriter writer2;
//		 writer2 = new FileWriter(path2, true); //True = Append to file, false
//		 count = 0;
//		 for (Iterator<Map.Entry<String, Double>> it = absoluteTop.entrySet()
//					.iterator(); it.hasNext() && count < 10; count++ ) {
//				Map.Entry<String, Double> entry = it.next();
//				writer2.write(entry.getKey());
//				writer2.write(",");
//				writer2.write("" + entry.getValue());
//				writer2.write("\r\n");
//		 }
//		 writer2.close();
//
//	}
//
//	// sample code for demonstrating how to use Snowball stemmer
//	public String SnowballStemmingDemo(String token) {
//		SnowballStemmer stemmer = new englishStemmer();
//		stemmer.setCurrent(token);
//		if (stemmer.stem())
//			return stemmer.getCurrent();
//		else
//			return token;
//	}
//
//	// sample code for demonstrating how to use Porter stemmer
//	public String PorterStemmingDemo(String token) {
//		porterStemmer stemmer = new porterStemmer();
//		stemmer.setCurrent(token);
//		if (stemmer.stem())
//			return stemmer.getCurrent();
//		else
//			return token;
//	}
//
//	// sample code for demonstrating how to perform text normalization
//	public String NormalizationDemo(String token) {
//		// remove all non-word characters
//		// please change this to removing all English punctuation
//		token = token.replaceAll("\\W+", "");
//		// convert to lower case
//		token = token.toLowerCase();
//		token = token.replaceAll("\\d+", "NUM");
//
//		// add a line to recognize integers and doubles via regular expression
//		// and convert the recognized integers and doubles to a special symbol
//		// "NUM"
//
//		return token;
//	}
//
//	public void TokenizerDemon(String text) {
//		try {
//
//			/**
//			 * HINT: instead of constructing the Tokenizer instance every time
//			 * when you perform tokenization, construct a global Tokenizer
//			 * instance once and evoke it everytime when you perform
//			 * tokenization.
//			 */
//			Tokenizer tokenizer = new TokenizerME(new TokenizerModel(
//					new FileInputStream("./data/Model/en-token.bin")));
//
//			System.out
//					.format("Token\tNormalization\tSnonball Stemmer\tPorter Stemmer\n");
//			for (String token : tokenizer.tokenize(text)) {
//				System.out.format("%s\t%s\t%s\t%s\n", token,
//						NormalizationDemo(token), SnowballStemmingDemo(token),
//						PorterStemmingDemo(token));
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//
//	public HashMap sortByValues(HashMap map, int order) {
//		List list = new LinkedList(map.entrySet());
//		// Defined Custom Comparator here
//		Collections.sort(list, new Comparator() {
//			public int compare(Object o1, Object o2) {
//				return (order * ((Comparable) ((Map.Entry) (o1)).getValue())
//						.compareTo(((Map.Entry) (o2)).getValue()));
//			}
//		});
//
//		// Here I am copying the sorted list in HashMap
//		// using LinkedHashMap to preserve the insertion order
//		HashMap sortedHashMap = new LinkedHashMap();
//		for (Iterator it = list.iterator(); it.hasNext();) {
//			Map.Entry entry = (Map.Entry) it.next();
//			sortedHashMap.put(entry.getKey(), entry.getValue());
//		}
//		return sortedHashMap;
//	}
//
//	public static void main(String[] args) throws IOException {
//		DocAnalyzer2_1 obj = new DocAnalyzer2_1();
//		obj.LoadDirectory("C:/Users/Sugandha/Documents/MP1/train", ".json");
//
//		 try
//		 {
//			 FileOutputStream fos = new FileOutputStream("lm_unigram.ser");
//			 ObjectOutputStream oos = new ObjectOutputStream(fos);
//			 oos.writeObject(obj.unigram);
//			 oos.close();
//			 fos.close();
//			 System.out.println("Serialized Language model Unigram data is saved in lm_unigram.ser");
//		 }catch(IOException ioe)
//		 {
//			 ioe.printStackTrace();
//		 }
//
//		 try
//		 {
//			 FileOutputStream fos = new FileOutputStream("lm_bigram.ser");
//			 ObjectOutputStream oos = new ObjectOutputStream(fos);
//			 oos.writeObject(obj.bigram);
//			 oos.close();
//			 fos.close();
//			 System.out.println("Serialized Language model Bigram data is saved in lm_bigram.ser");
//		 }catch(IOException ioe)
//		 {
//			 ioe.printStackTrace();
//		 }
//
//
//
//	}
//
//}
