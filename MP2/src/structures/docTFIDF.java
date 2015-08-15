package structures;

import java.util.HashMap;

/**
 * Created by cheta_000 on 4/17/2015.
 */
public class docTFIDF {
    public boolean pos;
    public HashMap<String, Double> wordTfIdfMap;

    public double magnitude;

    public docTFIDF() {
        wordTfIdfMap = new HashMap<String, Double>();
        magnitude = 0;
    }

    public void addWord(String word, double freq, double df, double totDocNum) {
        double val = (1+Math.log(totDocNum/df))*(Math.log(freq)+1);
        wordTfIdfMap.put(word, val);
        magnitude += Math.pow(val, 2);
    }

}
