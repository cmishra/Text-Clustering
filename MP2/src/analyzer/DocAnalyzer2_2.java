//package analyzer;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.io.ObjectInputStream;
//import java.io.ObjectOutputStream;
//import java.util.*;
//
//import structures.LanguageModel;
//
//public class DocAnalyzer2_2 {
//
//	LanguageModel unigramLM;
//	LanguageModel bigramLM;
//
//	public DocAnalyzer2_2() {
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
//	}
//
//	public static HashMap sortByValues(HashMap map, int order) {
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
//
//	public wordProbability generateUnigram() {
//		double randNum = Math.random();
//		wordProbability wp = new wordProbability();
//		for (String s : unigramLM.m_model.keySet()){
//			wp.probability = unigramLM.calcMLProbUnigram(s);
//			randNum -= wp.probability;
//			if (Double.compare(randNum, 0.0) < 0) {
//				wp.word = s;
//				break;
//			}
//		}
//		return wp;
//	}
//
//	public wordProbability generateBigram(String prev, boolean isLinear) {
//		double randNum = Math.random();
//		wordProbability wp = new wordProbability();
//		for (String s : unigramLM.m_model.keySet()) {
//			String twogram = prev + " " + s;
//			if (isLinear) {
//				wp.probability = bigramLM.calcLinearSmoothedProb(twogram);
//				randNum -= wp.probability;
//			}
//			else {
//				wp.probability = bigramLM.calcAbsoluteSmoothedProb(twogram);
//				randNum -= wp.probability;
//			}
//			if (Double.compare(randNum, 0.0) < 0) {
//				wp.word = s;
//				break;
//			}
//		}
//		return wp;
//	}
//
//	public void generateSentencesUnigram() {
//		String sentence = "";
//		double prob = 1.0;
//		wordProbability w;
//		for (int i = 0; i < 15; i++) {
//			w = generateUnigram();
//			sentence += w.word + " ";
//			prob *= w.probability;
//		}
//		System.out.println(sentence + "\t" + prob);
//	}
//
//	public void generateSentencesBigram(boolean isLinear) {
//		double prob = 1.0;
//		wordProbability w;
//		w = generateUnigram();
//		String sentence = w.word + " ";
//		prob *= w.probability;
//		for (int i = 0; i < 14; i++) {
//			w = generateBigram(w.word, isLinear);
//			sentence += w.word + " ";
//			prob *= w.probability;
//		}
//		System.out.println(sentence + "\t" + prob);
//	}
//
//	public static void main(String[] args) throws IOException {
//
//		DocAnalyzer2_2 obj = new DocAnalyzer2_2();
//		for (int i = 0; i < 10; i++)
//			obj.generateSentencesUnigram();
//		System.out.println("\n********************************************************\n");
//		for (int i = 0; i < 10; i++)
//			obj.generateSentencesBigram(true);
//		System.out.println("\n********************************************************\n");
//		for (int i = 0; i < 10; i++)
//			obj.generateSentencesBigram(false);
//
//
//
//	}
//
//}
//
