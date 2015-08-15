package structures;

import net.didion.jwnl.data.Word;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by cheta_000 on 4/14/2015.
 */
public class docInfo implements Serializable{

    public Map<String, Integer> wordsInfo;
    public Boolean pos;
    public Boolean test; //depreciated
    public int encodedVal;
    public docTFIDF tfidf;

    public docInfo() {
        wordsInfo = new ConcurrentHashMap<String, Integer>();
    }

    public docInfo(String word, int freq, boolean test, boolean pos) {
        wordsInfo = new HashMap<String, Integer>();
        this.addWordFreq(word, freq);
        this.test = test;
        this.pos = pos;
        encodedVal = Integer.MAX_VALUE;
    }

    public void addWordFreq(String word, int freq) {
        wordsInfo.put(word, freq);
    }


}
