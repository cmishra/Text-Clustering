package analyzer;

import structures.docInfo;
import structures.wordInfo;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
* Created by cheta_000 on 4/17/2015.
*/
public class DoTheCV {

    public static void outputKnnResults() {

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

        int folds = 10;
        int k = 5;
        int partitionSize = analyzer.docMap.size()/folds;
        ArrayList<Map<String, docInfo>> partitions = new ArrayList<>();
        Map<String, docInfo> docMapBackup = new ConcurrentHashMap<>(analyzer.docMap);
        System.out.println("Docmap size decrementing (initial: " + analyzer.docMap.size() + ")");
        for (int i = 0; i < folds; i++) {
            Map<String, docInfo> partition = new ConcurrentHashMap<>(
                    analyzer.docMap.entrySet().stream().limit(partitionSize).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
            );
            partitions.add(partition);
            analyzer.docMap.entrySet().removeAll(partition.entrySet());
            System.out.print(" -> " + analyzer.docMap.size());
        }
        System.out.print("\n");
        analyzer.docMap = new ConcurrentHashMap<>(docMapBackup);
        System.out.println((System.nanoTime() - startTime) / 1e9 + " seconds to partition docMap");
        startTime = System.nanoTime();

        try {
            OutputStream iterationResultPrintouts = Files.newOutputStream(Paths.get("./data/" + "CVresults.txt"));
            iterationResultPrintouts.write(("IterationNum,ModelType,TP,TN,FP,FN").getBytes(Charset.defaultCharset()));
            ArrayList<Map<String, Double>> ranVecs = KNN.genRanVecs(analyzer.wordMap);
            for (int i = 0; i < folds; i++) {
                DocAnalyzer2 forThisRun = new DocAnalyzer2();
                forThisRun.docMap = analyzer.docMap = new ConcurrentHashMap<>(docMapBackup);
                forThisRun.docMap.entrySet().removeAll(partitions.get(i).entrySet());
                forThisRun.wordMap = analyzer.wordMap;
                System.out.println((System.nanoTime() - startTime) / 1e9 + " to update docMap for partition " + i);
                startTime = System.nanoTime();

                KNN.doTheKNN(forThisRun.docMap, ranVecs, partitions.get(i), k, i);
                iterationResultPrintouts.write((i + ",KNN," + KNN.tP + "," + KNN.tN + "," + KNN.fP + "," + KNN.fN + "\n").getBytes(Charset.defaultCharset()));
                DocAnalyzer2.doTheNaiveBayes(forThisRun, "Iteration" + (i + 1) + " LRs", partitions.get(i), Boolean.TRUE);
                iterationResultPrintouts.write((i + ",Naive Bayes," + DocAnalyzer2.tP + "," + DocAnalyzer2.tN + "," + DocAnalyzer2.fP + "," + DocAnalyzer2.fN + "\n").getBytes(Charset.defaultCharset()));
                System.out.println((System.nanoTime() - startTime) / 1e9 + " to for iteration " + (i+1) + " of both methods.");
                DocAnalyzer2.resetConfusionNums();
                KNN.resetConfusionNums();
                startTime = System.nanoTime();
            }
        } catch(IOException excp) {
            excp.printStackTrace();
        }
    }

}



