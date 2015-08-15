package structures;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by cheta_000 on 4/14/2015.
 */
public class wordInfo implements Serializable{
    public ConcurrentHashMap<String, PosFreqPair> docsInfo;
    private double infogain;
    private double chisq;

    public double getIG() {
        return infogain;
    }
    public double getChiSq(){
        return chisq;
    }

    public wordInfo() {
        docsInfo = new ConcurrentHashMap<String, PosFreqPair>();
    }

    public wordInfo(String doc, double rating, Boolean testDirec) { // when wordInfo hasn't been made before
        docsInfo = new ConcurrentHashMap<String, PosFreqPair>();
        docsInfo.put(doc, new PosFreqPair(1, ratingToBool(rating), testDirec));
    }

    public void updateInfo(String doc, double rating, Boolean testDirec) {
        if (docsInfo.containsKey(doc)) {
            docsInfo.get(doc).incFreq();
            if (ratingToBool(rating) != docsInfo.get(doc).pos | testDirec != docsInfo.get(doc).test)
                System.out.println("Error - doc rating or test was written incorrectly");
        }
        else
            docsInfo.put(doc, new PosFreqPair(1, ratingToBool(rating), testDirec));
    }

    public static boolean ratingToBool(double rating) {
        return rating > 3.0;
    }

    public int getDocFreq() {
        return docsInfo.keySet().size();
    }

    public int getFreqInDoc(String doc) {
        return docsInfo.get(doc).freq;
    }

    public int getTTF() {
        return docsInfo.values().stream().mapToInt(p -> p.freq).sum();
    }

    public int getPosCount() {
        return docsInfo.values().stream().map(p -> p.pos ? 1 : 0).mapToInt(Integer::intValue).sum();
    }

    public int getNegCount() {
        return docsInfo.size() - this.getPosCount();
    }

    public String toString() {
        return docsInfo.toString();
    }

    public void saveInfoGain(int totDocNum, int totPosNum) {
        int docPosNum = this.getPosCount();
        this.infogain = calcInfoGain(totDocNum, totPosNum, this.getDocFreq(), this.getPosCount());
    }

    public static double calcInfoGain(int totDocNum, int totPosNum, int wordDocNum, int wordPosNum) {
        return calcEntropy(totDocNum, totPosNum) -
        ((double)wordDocNum)/totDocNum*calcEntropy(wordDocNum, wordPosNum) -
                (1-((double)wordDocNum)/totDocNum)*calcEntropy(totDocNum - wordDocNum, totPosNum - wordPosNum);

    }

    public static double calcEntropy(int N, int Pos) {
        double probPositive = ((double)Pos)/N;
        if (probPositive == 1.0 | probPositive == 0.0 | Double.isNaN(probPositive))
            return 0;
        else
            return -probPositive*log2(probPositive) - (1-probPositive)*log2(1 - probPositive);
    }

    public static double log2(double x) {
        return Math.log(x)/Math.log(2);
    }

    public void saveChiSq(int totDocNum, int totPosNum) {
        this.chisq = calcChiSq(totDocNum, totPosNum, this.getDocFreq(), this.getPosCount());
    }

    public static double calcChiSq(int tDN, int tPN, int wDN, int wPN){
        double B = tPN - wPN;
        double C = wDN - wPN;
        double D = tDN - tPN - C;
        double chisq = (wPN + B + C + D)*Math.pow(wPN*D-B*C, 2)/((wPN+C)*(B+D)*(wPN+B)*(C+D));
        if (Double.isNaN(chisq))
            return 0.0;
        else
            return chisq;
    }


}
