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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import json.JSONArray;
import json.JSONException;
import json.JSONObject;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;
import org.tartarus.snowball.ext.porterStemmer;

import structures.*;

/**
 * @author hongning Sample codes for demonstrating OpenNLP package usage NOTE:
 *         the code here is only for demonstration purpose, please revise it
 *         accordingly to maximize your implementation's efficiency!
 */
public class DocAnalyzer2 {
	public Tokenizer tokenizer;
	// a list of stopwords
	HashSet<String> m_stopwords;
	// you can store the loaded reviews in this arraylist for further processing
    public Map<String, wordInfo> wordMap;
    public Map<String, docInfo> docMap;

	// you might need something like this to store the counting statistics for
	// validating Zipf's and computing IDF
	// HashMap<String, Token> m_stats;

	// we have also provided sample implementation of language model in
	// src.structures.LanguageModel

	public DocAnalyzer2() {
		try {
			tokenizer = new TokenizerME(new TokenizerModel(new FileInputStream(
					"./data/Model/en-token.bin")));
			wordMap = new ConcurrentHashMap<String, wordInfo>();
            docMap = new HashMap<String, docInfo>();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// sample code for loading a list of stopwords from file
	// you can manually modify the stopword file to include your newly selected
	// words
	public void LoadStopwords(String filename) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(filename), "UTF-8"));
			String line;

			while ((line = reader.readLine()) != null) {
				// it is very important that you perform the same processing
				// operation to the loaded stopwords
				// otherwise it won't be matched in the text content
				line = SnowballStemmingDemo(NormalizationDemo(line));
				if (!line.isEmpty())
					m_stopwords.add(line);
			}
			reader.close();
			System.out.format("Loading %d stopwords from %s\n",
					m_stopwords.size(), filename);
		} catch (IOException e) {
			System.err.format("[Error]Failed to open file %s!!", filename);
		}
	}

//	public void analyzeDocumentDemo(JSONObject json) {
//		try {
//			JSONArray jarray = json.getJSONArray("Reviews");
//			for (int i = 0; i < jarray.length(); i++) {
//				Post review = new Post(jarray.getJSONObject(i));
//				String[] tokens = tokenizer.tokenize(review.getContent());
//				HashSet<String> hs = new HashSet<String>();
//				for (int j = 0; j < tokens.length; j++) {
//					tokens[j] = NormalizationDemo(tokens[j]);
//					tokens[j] = SnowballStemmingDemo(tokens[j]);
//					if (!tokens[j].equals(""))
//						hs.add(tokens[j]);
//				}
//
////				for (int j = 0; j < tokens.length - 1; j++) {
////					if (!tokens[j].equals("") && !tokens[j + 1].equals(""))
////						hs.add(tokens[j] + " " + tokens[j + 1]);
////
////				}
//
//				for (String s : hs) {
//					if (dfmap.containsKey(s))
//						dfmap.put(s, dfmap.get(s) + 1);
//					else
//						dfmap.put(s, 1);
//				}
//
//				review.setContent(Arrays.toString(tokens));
//				m_reviews.add(review);
//			}
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//	}

	// sample code for loading a json file
	public JSONObject LoadJson(String filename) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(filename), "UTF-8"));
			StringBuffer buffer = new StringBuffer(1024);
			String line;

			while ((line = reader.readLine()) != null) {
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

	// sample code for demonstrating how to recursively load files in a
	// directory
//	public void LoadDirectory(String folder, String suffix) {
//		File dir = new File(folder);
//		int size = m_reviews.size();
//		for (File f : dir.listFiles()) {
//			if (f.isFile() && f.getName().endsWith(suffix)) {
//				analyzeDocumentDemo(LoadJson(f.getAbsolutePath()));
//			} else if (f.isDirectory())
//				LoadDirectory(f.getAbsolutePath(), suffix);
//		}
//		size = m_reviews.size() - size;
//		System.out.println("Loading " + size + " review documents from "
//				+ folder);
//		dfmap = sortByValues(dfmap); // descending order
//		//Remove all N-grams with DF < 50
//		for (Iterator<Map.Entry<String, Integer>> it = dfmap.entrySet()
//				.iterator(); it.hasNext();) {
//			Map.Entry<String, Integer> entry = it.next();
//			if (entry.getValue() < 50)
//				it.remove();
//		}
//	}

	// sample code for demonstrating how to use Snowball stemmer
	public String SnowballStemmingDemo(String token) {
		SnowballStemmer stemmer = new englishStemmer();
		stemmer.setCurrent(token);
		if (stemmer.stem())
			return stemmer.getCurrent();
		else
			return token;
	}

	// sample code for demonstrating how to use Porter stemmer
	public String PorterStemmingDemo(String token) {
		porterStemmer stemmer = new porterStemmer();
		stemmer.setCurrent(token);
		if (stemmer.stem())
			return stemmer.getCurrent();
		else
			return token;
	}

	// sample code for demonstrating how to perform text normalization
	public String NormalizationDemo(String token) {
		// remove all non-word characters
		// please change this to removing all English punctuation
		token = token.replaceAll("\\W+", "");
		// convert to lower case
		token = token.toLowerCase();
		token = token.replaceAll("\\d+", "NUM");

		// add a line to recognize integers and doubles via regular expression
		// and convert the recognized integers and doubles to a special symbol
		// "NUM"

		return token;
	}

	public void TokenizerDemon(String text) {
		try {

			/**
			 * HINT: instead of constructing the Tokenizer instance every time
			 * when you perform tokenization, construct a global Tokenizer
			 * instance once and evoke it everytime when you perform
			 * tokenization.
			 */
			Tokenizer tokenizer = new TokenizerME(new TokenizerModel(
					new FileInputStream("./data/Model/en-token.bin")));

			System.out
					.format("Token\tNormalization\tSnonball Stemmer\tPorter Stemmer\n");
			for (String token : tokenizer.tokenize(text)) {
				System.out.format("%s\t%s\t%s\t%s\n", token,
						NormalizationDemo(token), SnowballStemmingDemo(token),
						PorterStemmingDemo(token));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public HashMap sortByValues(HashMap map) {
		List list = new LinkedList(map.entrySet());
		// Defined Custom Comparator here
		Collections.sort(list, new Comparator() {
			public int compare(Object o1, Object o2) {
				return (-1 * ((Comparable) ((Map.Entry) (o1)).getValue())
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

    public static AtomicInteger fP = new AtomicInteger();
    public static AtomicInteger fN = new AtomicInteger();
    public static AtomicInteger tP = new AtomicInteger();
    public static AtomicInteger tN = new AtomicInteger();

    public static void resetConfusionNums() {
        fP = new AtomicInteger();
        fN = new AtomicInteger();
        tP = new AtomicInteger();
        tN = new AtomicInteger();
    }

    public static Map<String, docInfo> wordMapToDocMap(Map<String, wordInfo> wordMap) {
        Map<String, docInfo> docMap = new ConcurrentHashMap<String, docInfo>();
        wordMap.entrySet().stream()
                .forEach(
                        p -> p.getValue().docsInfo.keySet().stream()
                                .forEach(doc -> {
                                    if (docMap.containsKey(doc))
                                        docMap.get(doc).addWordFreq(p.getKey(), p.getValue().getFreqInDoc(doc));
                                    else
                                        docMap.put(doc, new docInfo(
                                                p.getKey(),
                                                p.getValue().getFreqInDoc(doc),
                                                p.getValue().docsInfo.get(doc).test,
                                                p.getValue().docsInfo.get(doc).pos));
                                })
                );
        return docMap;
    }

    public static void doTheNaiveBayes(DocAnalyzer2 analyzer, String fileName, Map<String, docInfo> predictionMap, boolean CVMode) {
        Unigram posDocs = new Unigram();
        Unigram negDocs = new Unigram();

        analyzer.docMap = analyzer.docMap.entrySet().stream()
                .filter(p -> p.getValue().wordsInfo.size() > 5)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        posDocs.loadMap(analyzer.docMap.entrySet().stream()
                .filter(p -> !p.getValue().test & p.getValue().pos)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        negDocs.loadMap(analyzer.docMap.entrySet().stream()
                .filter(p -> !p.getValue().test & !p.getValue().pos)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

        if (!CVMode) {
            Map<String, Double> logRatios = new HashMap<String, Double>();
            analyzer.wordMap.keySet().stream()
                    .forEach(p -> logRatios.put(p, Math.log(posDocs.getSmoothedProb(p)) - Math.log(negDocs.getSmoothedProb(p))));
            System.out.println("");
            System.out.println("Words identifying Pos:");
            logRatios.entrySet().stream()
                    .sorted((p1, p2) -> Double.compare(p2.getValue(), p1.getValue()))
                    .limit(20)
                    .forEach(p -> System.out.print(p.getKey() + ": " + p.getValue() + ", "));
            System.out.println("\nWords identifying Neg:");
            logRatios.entrySet().stream()
                    .sorted((p1, p2) -> Double.compare(p1.getValue(), p2.getValue()))
                    .limit(20)
                    .forEach(p -> System.out.print(p.getKey() + ": " + p.getValue() + ", "));
        }

        double trainNumPos = 0.0;
        for (docInfo val : analyzer.docMap.values()) {
            if (val.pos)
                trainNumPos++;
        }
        double trainNumTotal = analyzer.docMap.size();

        double overallLogRatio = Math.log((trainNumPos/trainNumTotal)) - Math.log((trainNumTotal - trainNumPos)/(trainNumTotal));
//        OutputStream chartOutput; whoops accidentally deleted rest of chart output stuff. it's okay, i already have it.
            predictionMap.entrySet().stream()
                    .forEach(p -> {
                        double sum = overallLogRatio;
                        sum += p.getValue().wordsInfo.keySet().stream()
                                .map(q -> Math.log(posDocs.getSmoothedProb(q)) - Math.log(negDocs.getSmoothedProb(q)))
                                .mapToDouble(Double::doubleValue)
                                .sum();
                        boolean queryPos = sum > 0;
                        if (queryPos == p.getValue().pos && queryPos)
                            tP.incrementAndGet();
                        else if (queryPos == p.getValue().pos && !queryPos)
                            tN.incrementAndGet();
                        else if (queryPos != p.getValue().pos && queryPos)
                            fN.incrementAndGet();
                        else if (queryPos != p.getValue().pos && !queryPos)
                            fP.incrementAndGet();
                    });
    }

	public static void main(String[] args) throws IOException {
        double startTime = System.nanoTime();
		DocAnalyzer2 analyzer = new DocAnalyzer2();
        ObjectInputStream oos = null;
        try {
            FileInputStream streamIn = new FileInputStream("./data/wordMap.JData");
            oos = new ObjectInputStream(streamIn);
            analyzer.wordMap = (HashMap<String, wordInfo>) oos.readObject();
            oos.close();
        } catch (ClassNotFoundException excp) {
            excp.printStackTrace();
        } catch (IOException excp) {
            excp.printStackTrace();
        }
        System.out.println((System.nanoTime() - startTime)/1e9 + " seconds to loading wordMap with word count of " + analyzer.wordMap.size() );
        startTime = System.nanoTime();

//        System.out.println("Love stats: " + analyzer.wordMap.get("love").getChiSq() + " " + analyzer.wordMap.get("love").getIG());
//        System.out.println("Love stats: " + analyzer.wordMap.get("love").getDocFreq() + " " + analyzer.wordMap.get("love").getPosCount());
        analyzer.docMap = DocAnalyzer2.wordMapToDocMap(analyzer.wordMap);

        System.out.println((System.nanoTime() - startTime)/1e9 + " seconds to creating docMap with " + analyzer.docMap.size() + " entries");
        startTime = System.nanoTime();

        DocAnalyzer2.doTheNaiveBayes(analyzer, "chartOutput", analyzer.docMap, Boolean.FALSE);
	}

}
