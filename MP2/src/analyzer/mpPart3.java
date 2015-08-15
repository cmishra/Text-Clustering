///**
// *
// */
//package analyzer;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.io.ObjectInputStream;
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
//import structures.Post;
//
///**
// * @author hongning Sample codes for demonstrating OpenNLP package usage NOTE:
// *         the code here is only for demonstration purpose, please revise it
// *         accordingly to maximize your implementation's efficiency!
// */
//public class mpPart3 {
//	public Tokenizer tokenizer;
//	// a list of stopwords
//	HashSet<String> m_stopwords;
//	// you can store the loaded reviews in this arraylist for further processing
//	ArrayList<Post> m_reviews;
//	ArrayList<Post> q_reviews;
//	public HashMap<String, Integer> controlledDF;
//
//	// you might need something like this to store the counting statistics for
//	// validating Zipf's and computing IDF
//	// HashMap<String, Token> m_stats;
//
//	// we have also provided sample implementation of language model in
//	// src.structures.LanguageModel
//
//	public mpPart3() {
//		m_reviews = new ArrayList<Post>();
//		q_reviews = new ArrayList<Post>();
//		try {
//			tokenizer = new TokenizerME(new TokenizerModel(new FileInputStream(
//					"./data/Model/en-token.bin")));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//		try {
//			FileInputStream fis = new FileInputStream("hashmapControlled.ser");
//			ObjectInputStream ois = new ObjectInputStream(fis);
//			controlledDF = (HashMap) ois.readObject();
//			ois.close();
//			fis.close();
//		}
//
//		catch (IOException ioe) {
//			ioe.printStackTrace();
//			return;
//		} catch (ClassNotFoundException c) {
//			System.out.println("Class not found");
//			c.printStackTrace();
//			return;
//		}
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
//	public void analyzeDocumentDemo(JSONObject json, boolean x) {
//		try {
//			JSONArray jarray = json.getJSONArray("Reviews");
//			for (int i = 0; i < jarray.length(); i++) {
//				Post review = new Post(jarray.getJSONObject(i));
//				String[] tokens = tokenizer.tokenize(review.getContent());
//				HashMap<String, Double> vector = new HashMap<String, Double>();
//				for (int j = 0; j < tokens.length; j++) {
//					tokens[j] = NormalizationDemo(tokens[j]);
//					tokens[j] = SnowballStemmingDemo(tokens[j]);
//					if (controlledDF.containsKey(tokens[j]))
//						if (vector.containsKey(tokens[j]))
//								vector.put(tokens[j], vector.get(tokens[j]) + 1.0);
//						else
//							vector.put(tokens[j], 1.0);
//				}
//
//				for (int j = 0; j < tokens.length - 1; j++) {
//					String combo = tokens[j] + " " + tokens[j+1];
//					if (controlledDF.containsKey(combo))
//						if (vector.containsKey(combo))
//							vector.put(combo, vector.get(combo) + 1.0);
//						else
//							vector.put(combo, 1.0);
//				}
//
//				double sum = 0;
//				for (Iterator<Map.Entry<String, Double>> it = vector.entrySet()
//						.iterator(); it.hasNext();) {
//					Map.Entry<String, Double> entry = it.next();
//					double idf = 1 + Math.log10(629921/controlledDF.get(entry.getKey()));
//					double temp = (1 + Math.log10(entry.getValue()))*idf;
//					entry.setValue(temp);
//					sum += Math.pow(temp, 2);
//				}
//
//				review.setMagnitude(Math.sqrt(sum));
//				review.setMap(vector);
//				//review.setContent(Arrays.toString(tokens));
//				if (x)
//					q_reviews.add(review);
//				else
//					m_reviews.add(review);
//			}
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
//	public void LoadDirectory(String folder, String suffix, boolean x) {
//		File dir = new File(folder);
//		int size = 0;
//		if (x)
//			size = q_reviews.size();
//		else
//			size = m_reviews.size();
//		for (File f : dir.listFiles()) {
//			if (f.isFile() && f.getName().endsWith(suffix)) {
//				analyzeDocumentDemo(LoadJson(f.getAbsolutePath()), x);
//			} else if (f.isDirectory())
//				LoadDirectory(f.getAbsolutePath(), suffix, x);
//		}
//		if (x)
//			size = q_reviews.size() - size;
//		else
//			size = m_reviews.size() - size;
//		System.out.println("Loading " + size + " review documents from "
//				+ folder);
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
//	public ArrayList<postDist> computeSim(Post p) {
//		ArrayList<postDist> top3 = new ArrayList<postDist>();
//		for (int j = 0; j < m_reviews.size(); j++) {
//			double dist = p.similarity(m_reviews.get(j));
//			if (j > 2) {
//				Collections.sort(top3);
//				if (dist > top3.get(0).similarity) {
//					top3.remove(0);
//					postDist obj = new postDist(m_reviews.get(j), dist);
//					top3.add(obj);
//				}
//			}
//			else {
//				postDist obj = new postDist(m_reviews.get(j), dist);
//				top3.add(obj);
//			}
//
//		}
//		return top3;
//	}
//
//	public HashMap sortByValues(HashMap map) {
//		List list = new LinkedList(map.entrySet());
//		// Defined Custom Comparator here
//		Collections.sort(list, new Comparator() {
//			public int compare(Object o1, Object o2) {
//				return (-1 * ((Comparable) ((Map.Entry) (o1)).getValue())
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
//		mpPart3 obj = new mpPart3();
//		//String path1 = "C:\\Users\\Sugandha\\Documents\\mp7Excel.csv";
//		obj.LoadDirectory("C:/Users/Sugandha/Documents/MP1/test", ".json", false);
//		obj.LoadDirectory("C:/Users/Sugandha/Documents/MP1/query", ".json", true);
//
//		for (int i = 0; i < obj.q_reviews.size(); i++) {
//			ArrayList<postDist> temp = obj.computeSim(obj.q_reviews.get(i));
//			for (postDist pd : temp) {
//				System.out.println(pd);
//				System.out.println("Author: " + pd.p.getAuthor() + "\t|\tDate: " + pd.p.getDate());
//			}
//			System.out.println("***********************");
//		}
//
//
//
//	}
//
//}
