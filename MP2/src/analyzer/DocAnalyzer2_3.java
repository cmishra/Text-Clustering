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
//import java.io.ObjectInputStream;
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
//
//public class DocAnalyzer2_3 {
//	public Tokenizer tokenizer;
//	LanguageModel unigramLM;
//	LanguageModel bigramLM;
//	ArrayList<Double> P_unigram = new ArrayList<Double>();
//	ArrayList<Double> P_Lbigram = new ArrayList<Double>();
//	ArrayList<Double> P_Abigram = new ArrayList<Double>();
//
//	public DocAnalyzer2_3() {
//		try {
//			tokenizer = new TokenizerME(new TokenizerModel(new FileInputStream(
//					"./data/Model/en-token.bin")));
//
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//		try {
//			FileInputStream fis = new FileInputStream("lm_unigram.ser");
//			ObjectInputStream ois = new ObjectInputStream(fis);
//			unigramLM = (LanguageModel) ois.readObject();
//			ois.close();
//			fis.close();
//		}
//		catch (IOException ioe) {
//			ioe.printStackTrace();
//			return;
//		} catch (ClassNotFoundException c) {
//			System.out.println("Class not found");
//			c.printStackTrace();
//			return;
//		}
//
//		try {
//			FileInputStream fis = new FileInputStream("lm_bigram.ser");
//			ObjectInputStream ois = new ObjectInputStream(fis);
//			bigramLM = (LanguageModel) ois.readObject();
//			ois.close();
//			fis.close();
//		}
//		catch (IOException ioe) {
//			ioe.printStackTrace();
//			return;
//		} catch (ClassNotFoundException c) {
//			System.out.println("Class not found");
//			c.printStackTrace();
//			return;
//		}
//
//	}
//
//	public void analyzeDocumentDemo(JSONObject json) {
//		try {
//			JSONArray jarray = json.getJSONArray("Reviews");
//			for(int i=0; i<jarray.length(); i++) {
//				Post review = new Post(jarray.getJSONObject(i));
//				String[] tokens = tokenizer.tokenize(review.getContent());
//				ArrayList<String> tokens_grams = new ArrayList<String>();
//				double unigram_perp = 0.0;
//				double Lbigram_perp = 0.0;
//				double Abigram_perp = 0.0;
//				for (int j = 0; j<tokens.length; j++) {
//					tokens[j] = NormalizationDemo(tokens[j]);
//					tokens[j] = SnowballStemmingDemo(tokens[j]);
//					if (!tokens[j].equals("")) {
//						tokens_grams.add(tokens[j]);
//						unigram_perp += Math.log(unigramLM.smoothedUnigram(tokens[j]));
//					}
//				}
//
//				for (int j = 0; j < tokens_grams.size() - 1; j++) {
//					String twogram = tokens_grams.get(j) + " " + tokens_grams.get(j+1);
//					Lbigram_perp += Math.log(bigramLM.calcLinearSmoothedProb(twogram));
//					Abigram_perp += Math.log(bigramLM.calcAbsoluteSmoothedProb(twogram));
//				}
//
//				if(tokens_grams.size() != 0) {
//					P_unigram.add(Math.exp((-1.0 / tokens_grams.size()) * unigram_perp));
//					P_Lbigram.add(Math.exp((-1.0 / tokens_grams.size()) * Lbigram_perp));
//					P_Abigram.add(Math.exp((-1.0 / tokens_grams.size()) * Abigram_perp));
//				}
//			}
//		}
//		catch (JSONException e) {
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
//
//		double mean = findAverage(P_unigram);
//		double stdDev = findStdDeviation(P_unigram, mean);
//		System.out.println("Unigram --- Mean: " + mean + "\tStdDev: " + stdDev);
//
//		mean = findAverage(P_Lbigram);
//		stdDev = findStdDeviation(P_Lbigram, mean);
//		System.out.println("Linear --- Mean: " + mean + "\tStdDev: " + stdDev);
//
//		mean = findAverage(P_Abigram);
//		stdDev = findStdDeviation(P_Abigram, mean);
//		System.out.println("Absolute --- Mean: " + mean + "\tStdDev: " + stdDev);
//	}
//
//	public double findAverage(ArrayList<Double> d) {
//		double sum = 0.0;
//		for (int i = 0; i < d.size(); i++)
//			sum += d.get(i);
//		return (sum / d.size());
//	}
//
//	public double findStdDeviation(ArrayList<Double> d, double mean) {
//		double sumofsquares = 0.0;
//		for (int i = 0; i < d.size(); i++)
//			sumofsquares += Math.pow(d.get(i) - mean, 2);
//		return Math.sqrt(sumofsquares / d.size());
//	}
//
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
//		DocAnalyzer2_3 obj = new DocAnalyzer2_3();
//		obj.LoadDirectory("C:/Users/Sugandha/Documents/MP1/test", ".json");
//	}
//
//}
