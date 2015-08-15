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
//public class DocAnalyzer2part2 {
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
//	public static void main(String[] args) throws IOException {
//
//		HashMap<String, Integer> dfhash = new HashMap<String, Integer>();
//		// De-serialize the stored hashmap
//		try {
//			FileInputStream fis = new FileInputStream("hashmap2.ser");
//			ObjectInputStream ois = new ObjectInputStream(fis);
//			dfhash = (HashMap) ois.readObject();
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
//
//		// Create a HashSet from SmartStop list
//		HashSet<String> smartstop = new HashSet<String>();
//		File f = new File("english.stop.txt");
//		Scanner scr = new Scanner(f);
//		while (scr.hasNextLine())
//			smartstop.add(scr.nextLine());
//
//		// Get top 100 n-grams according to DF and store them in a set
//		ArrayList<String> top100 = new ArrayList<String>();
//		int count = 0;
//		for (Iterator<Map.Entry<String, Integer>> it = dfhash.entrySet()
//				.iterator(); it.hasNext() && count < 100; count++) {
//			Map.Entry<String, Integer> entry = it.next();
//			top100.add(entry.getKey());
//		}
//
//		HashSet<String> top = new HashSet<String>(top100);
//
//		// Find difference between top 100 ngrams by DF and SmartStop list
//		top.removeAll(smartstop);
//		//System.out.println("top after diff: " + top.size());
////		for (String s : top)
////			System.out.println(s);
//		// Now create a merged stopword list
//		top.addAll(smartstop);
//		//System.out.println("Afte rmerge top size: " + top.size());
//
//		//System.out.println("Size of hashmap before stopword removal: "
//				//+ dfhash.size());
//		for (Iterator<Map.Entry<String, Integer>> it = dfhash.entrySet()
//				.iterator(); it.hasNext();) {
//			Map.Entry<String, Integer> entry = it.next();
//			String[] s = entry.getKey().split(" ");
//
//			if (s.length == 1) {
//				if (top.contains(s[0]))
//					it.remove();
//			} else if (top.contains(entry.getKey()))
//				it.remove();
//			else if (top.contains(s[0]) && top.contains(s[1]))
//					it.remove();
//		}
//
//		System.out.println("Size of hashmap after removal: " + dfhash.size());
//
////		count = 0;
////		for (Iterator<Map.Entry<String, Integer>> it = dfhash.entrySet()
////				.iterator(); it.hasNext() && count < 100; count++) {
////			Map.Entry<String, Integer> entry = it.next();
////			System.out.println(entry.getKey() + " " + entry.getValue());
////		}
//
//
//		HashMap<String, Integer> ascended = new HashMap<String, Integer>();
//		// descended = sortByValues(dfhash, -1);
//		ascended = sortByValues(dfhash, 1);
//
//		HashMap<String, Double> top50 = new HashMap<String, Double>();
//		HashMap<String, Double> bottom50 = new HashMap<String, Double>();
//
//		count = 0;
//		for (Iterator<Map.Entry<String, Integer>> it = dfhash.entrySet()
//				.iterator(); it.hasNext() && count < 50; count++) {
//			Map.Entry<String, Integer> entry = it.next();
//			top50.put(entry.getKey(), 1 + Math.log10(629921 / entry.getValue()));
//		}
//
//		top50 = sortByValues(top50, -1);
//
//		count = 0;
//		for (Iterator<Map.Entry<String, Integer>> it = ascended.entrySet()
//				.iterator(); it.hasNext() && count < 50; count++) {
//			Map.Entry<String, Integer> entry = it.next();
//			bottom50.put(entry.getKey(),
//					1 + Math.log10(629921 / entry.getValue()));
//		}
//
//		bottom50 = sortByValues(bottom50, -1);
//
//		String path1 = "C:\\Users\\Sugandha\\Documents\\top50Excel.csv";
//		FileWriter writer1;
//		writer1 = new FileWriter(path1, true);  //True = Append to file, false = Overwrite
//		for (String s: top50.keySet()) {
//			writer1.write(s);
//			writer1.write(",");
//			writer1.write(""+top50.get(s));
//			writer1.write("\r\n");
//	    }
//
//		writer1.close();
//
//		String path2 = "C:\\Users\\Sugandha\\Documents\\bottom50Excel.csv";
//		FileWriter writer2;
//		writer2 = new FileWriter(path2, true);  //True = Append to file, false = Overwrite
//		for (String s: bottom50.keySet()) {
//			writer2.write(s);
//			writer2.write(",");
//			writer2.write(""+bottom50.get(s));
//			writer2.write("\r\n");
//	    }
//
//		writer2.close();
//
//		System.out.println("Size of top50: " + top50.size());
//
//		 try
//		 {
//		 FileOutputStream fos = new FileOutputStream("hashmapControlled.ser");
//		 ObjectOutputStream oos = new ObjectOutputStream(fos);
//		 oos.writeObject(dfhash);
//		 oos.close();
//		 fos.close();
//		 System.out.printf("Serialized HashMap data is saved in hashmapControlled.ser");
//		 }catch(IOException ioe)
//		 {
//		 ioe.printStackTrace();
//		 }
//
//	}
//
//}
