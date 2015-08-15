package analyzer;

import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;
import structures.*;
import sun.security.util.BitArray;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by cheta_000 on 4/16/2015.
 */
public class KNN {
    public HashMap<String, wordInfo> wordMap;

    public KNN() {
        wordMap = new HashMap<String, wordInfo>() ;
    }

    public static Map<String, Integer> makeWordDf(Map<String, docInfo> docMap) {
        Map<String, Integer> wordDf = new ConcurrentHashMap<>();
        docMap.entrySet().stream().forEach(p -> {
            p.getValue().wordsInfo.keySet().stream().forEach(q -> {
                if (wordDf.containsKey(q))
                    wordDf.put(q, wordDf.get(q) + 1);
                else
                    wordDf.put(q, 1);
            });
        });
        return wordDf;
    }

    public static docTFIDF retTfIdf(docInfo toEncode, Map<String, Integer> wordDf, double totDocSize) {
        docTFIDF docTfIdf = new docTFIDF();
        toEncode.wordsInfo.entrySet().stream().forEach(p -> docTfIdf.addWord(p.getKey(), p.getValue(), wordDf.get(p.getKey()), totDocSize));
        return docTfIdf;
    }

    public static Set findSetIntersection(Set set1, Set set2) {
        Set intersection = new HashSet<>();
        intersection.addAll(set1);
        intersection.retainAll(set2);
        return intersection;
    }


    public static int retDocumentEncoding(docTFIDF docTfIdf, ArrayList<Map<String, Double>> ranVecs) {
//        Integer.
        BitSet encoding = new BitSet();
        for (int i = 0; i < ranVecs.size(); i++) {
            Set<String> intersectingWords = KNN.findSetIntersection(docTfIdf.wordTfIdfMap.keySet(),ranVecs.get(i).keySet());
            double docProdSum = 0;
            for (String w : intersectingWords) {
                docProdSum += docTfIdf.wordTfIdfMap.get(w) * ranVecs.get(i).get(w);
            }
            if (docProdSum > 0)
                encoding.flip(i);
        }
        return KNN.toInt(encoding);
    }

    static int toInt(BitSet bs) { // http://stackoverflow.com/questions/2794802/convert-bit-vector-array-of-booleans-to-an-integer-and-integer-to-bit-vector
        int i = 0;
        for (int pos = -1; (pos = bs.nextSetBit(pos+1)) != -1; ) {
            i |= (1 << pos);
        }
        return i;
    }

    public static List<String> findMatchingDocs(ArrayList<Map<String, Double>> ranVec,
                                                Map<String, docInfo> docMap, docInfo query, Map<String, Integer> wordDf) {
        List<String> matchingFile = new ArrayList<String>();
        for (Map.Entry<String, docInfo> p : docMap.entrySet()) {
            if (p.getValue().encodedVal == query.encodedVal)
                matchingFile.add(p.getKey());
        }
        return matchingFile;
    }

    public static double cosineSimilarity(docTFIDF doc1, docTFIDF doc2) {
        Set<String> intersectWords = KNN.findSetIntersection(doc1.wordTfIdfMap.keySet(), doc2.wordTfIdfMap.keySet());

        double num = 0;
        for (String w : intersectWords) {
            num += doc1.wordTfIdfMap.get(w) * doc2.wordTfIdfMap.get(w);
        }

        num = num/(Math.pow(doc1.magnitude, 0.5)*Math.pow(doc2.magnitude, 0.5));
        return num;
    }

    public static void storeEncodedVals(Map<String, docInfo> docMap, Map<String, Integer> wordDf,
                                        ArrayList<Map<String, Double>> ranVec) {
        docMap.entrySet().stream().forEach(p ->
            p.getValue().encodedVal = KNN.retDocumentEncoding(KNN.retTfIdf(p.getValue(), wordDf, docMap.size()), ranVec));
    }

    public static void saveTfIdfValues(Map<String, docInfo> docMap, Map<String, Integer> wordDf, int totSize) {
        docMap.entrySet().stream().forEach(p -> {
            p.getValue().tfidf = KNN.retTfIdf(p.getValue(),wordDf, totSize);
        });
    }

    public static void doTheKNN(final Map<String, docInfo> docMap, ArrayList<Map<String, Double>> ranVec, Map<String, docInfo> queryMap, int k,
                                int CViterationNum) {
        Map<String, Integer> wordDf = KNN.makeWordDf(docMap);
        KNN.storeEncodedVals(docMap, wordDf, ranVec);
        KNN.storeEncodedVals(queryMap, wordDf, ranVec);
        KNN.saveTfIdfValues(docMap, wordDf, docMap.size());
        queryMap.keySet().stream()
                .parallel()
                .forEach(query -> {
                    List<String> matchingDocs = KNN.findMatchingDocs(ranVec, docMap, queryMap.get(query), wordDf);
                    HashSet<DocSimil> closestDocs = new HashSet<>();
                    for (String doc : matchingDocs) {
                        double simil = KNN.cosineSimilarity(docMap.get(doc).tfidf,
                                KNN.retTfIdf(queryMap.get(query), wordDf, docMap.size()));
                        if (closestDocs.size() < k)
                            closestDocs.add(new DocSimil(doc, simil, docMap.get(doc).pos));
                        else {
                            DocSimil furthest = closestDocs.stream()
                                    .max((p1, p2) -> Double.compare(p1.docSimil, p2.docSimil)).get();
                            if (furthest.docSimil > simil) {
                                closestDocs.remove(furthest);
                                closestDocs.add(new DocSimil(doc, simil, docMap.get(doc).pos));
                            }
                        }
                    }
                    if (CViterationNum == -1)
                        KNN.saveDocsToFile(query, closestDocs, docMap);
                    else {
                        boolean queryPos = KNN.knnVote(closestDocs);
                        if (queryPos == queryMap.get(query).pos && queryPos)
                            tP.incrementAndGet();
                        else if (queryPos == queryMap.get(query).pos && !queryPos)
                            tN.incrementAndGet();
                        else if (queryPos != queryMap.get(query).pos && queryPos)
                            fN.incrementAndGet();
                        else if (queryPos != queryMap.get(query).pos && !queryPos)
                            fP.incrementAndGet();
                    }

                });
//        System.out.println
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

    public static boolean knnVote(Set<DocSimil> closestDocs) {
        int counter = 0;
        for (DocSimil doc : closestDocs) {
            if (doc.pos)
                counter += 1;
            else
                counter -= 1;
        }
        if (counter == 0)
            return knnVote(closestDocs.stream()
                    .sorted((p1, p2) -> Double.compare(p2.docSimil, p1.docSimil))
                    .limit(closestDocs.size() - 1)
                    .collect(Collectors.toSet()));
        else
            return counter > 0.0;

    }

    public static void saveDocsToFile(String query, HashSet<DocSimil> closestDocs, Map<String, docInfo> docMap) {
        OutputStream similDocOutput;
        try {
            similDocOutput = Files.newOutputStream(Paths.get("./data/" + "QueryOutput" + query + ".txt"));
            for (DocSimil doc : closestDocs) {
//                docMap.get(doc.docName).wordsInfo.keySet().stream().forEach(p -> {
//                            try {
//                                similDocOutput.write((p + " ").getBytes(Charset.defaultCharset()));
//                            } catch (IOException excp) {
//                                excp.printStackTrace();
//                            }
//                        }
//                );
                similDocOutput.write((doc.docName + "\n").getBytes(Charset.defaultCharset()));
            }
            similDocOutput.close();
        } catch (IOException excp) {
            excp.printStackTrace();
        }
    }

    public static ArrayList<Map<String, Double>> genRanVecs(Map<String, wordInfo> wordMap) {
        ArrayList<Map<String, Double>> ranVec = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            ranVec.add(new ConcurrentHashMap<String, Double>());
        }
        wordMap.keySet().stream()
                .forEach(p ->
                        ranVec.stream().forEach(q -> q.put(p, genRanVecValue())));
        return ranVec;
    }

    public static void main(String[] args) {
        double startTime = System.nanoTime();
        DocAnalyzer2 analyzer = new DocAnalyzer2();
        ObjectInputStream oos = null;
        try {
            FileInputStream streamIn = new FileInputStream("./data/wordMap.JData");
            oos = new ObjectInputStream(streamIn);
            analyzer.wordMap = new ConcurrentHashMap<>((HashMap<String, wordInfo>) oos.readObject());
            oos.close();
        } catch (ClassNotFoundException excp) {
            excp.printStackTrace();
        } catch (IOException excp) {
            excp.printStackTrace();
        }
        System.out.println((System.nanoTime() - startTime)/1e9 + " seconds to loading wordMap with word count of " + analyzer.wordMap.size());
        startTime = System.nanoTime();

        analyzer.docMap = DocAnalyzer2.wordMapToDocMap(analyzer.wordMap);
        System.out.println((System.nanoTime() - startTime)/1e9 + " seconds to creating docMap with " + analyzer.docMap.size() + " entries");
        startTime = System.nanoTime();

        ArrayList<Map<String, Double>> ranVec = KNN.genRanVecs(analyzer.wordMap);
        System.out.println((System.nanoTime() - startTime) / 1e9 + " seconds to create random vectors");
        startTime = System.nanoTime();

        DocAnalyzer queryStuff = new DocAnalyzer();
        String queryPath = "C:\\Users\\cheta_000\\Dropbox\\College\\3_Third Year\\06_Sixth Semester\\CS 6501\\MP2\\Yelp_small\\query";
        queryStuff.LoadDirectory(queryPath, ".json", Boolean.FALSE);
        SnowballStemmer stemr = new englishStemmer();
        try {
            Set stopwords = Files.lines(Paths.get("./data/stopwords.txt")).
                    map(p -> DocAnalyzer.SnowballStemmingDemo(DocAnalyzer.NormalizationDemo(p), stemr))
                    .collect(Collectors.toSet());
            queryStuff.wordMap = queryStuff.wordMap.entrySet().stream()
                    .filter(p -> analyzer.wordMap.keySet().contains(p.getKey()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            Map<String, docInfo> queryMap = DocAnalyzer2.wordMapToDocMap(queryStuff.wordMap);
            KNN.doTheKNN(analyzer.docMap, ranVec, queryMap, 5, -1);
        } catch (IOException excp) {
            excp.printStackTrace();
        }
        System.out.println((System.nanoTime() - startTime) / 1e9 + " seconds to do everything else");
        startTime = System.nanoTime();
    }



    public static double genRanVecValue() {
        return Math.random()*2-1;
    }

}
