package structures;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Created by cheta_000 on 2/17/2015.
 */
public class Unigram {

    public Unigram() {
        m_model = new HashMap<String, Integer>();
        numWords = 0;
    }

    public HashMap<String, Integer> m_model; // sparse structure for storing the maximum likelihood estimation of LM with the seen N-grams
    private long numWords;

    public void addOrInc(String s) {
        if (!s.equals("")) {
            numWords++;
            if (m_model.containsKey(s)) {
                m_model.put(s, m_model.get(s) + 1);
            } else {
                m_model.put(s, 1);
            }
        }
    }

    public void loadMap(Map<String, docInfo> docMap) {
        docMap.entrySet().stream()
        .forEach(p -> {
            p.getValue().wordsInfo.entrySet().stream()
                    .forEach(q -> this.addWordFreq(q.getKey(), q.getValue()));
        });
    }

    public void addWordFreq(String s, int freq) {
        numWords += freq;
        if (m_model.containsKey(s))
            m_model.put(s, m_model.get(s) + freq);
        else
            m_model.put(s, freq);
    }



    public double getSmoothedProb(String s) {
        if (m_model.containsKey(s))
            return (m_model.get(s) + 0.1)/(numWords + m_model.size());
        else
            return 0.1/(numWords + m_model.size());
    }

    public long getFreq(String s) {
        if (m_model.containsKey(s))
            return m_model.get(s);
        else
            return 0;
    }

    public long size() {
        return m_model.size();
    }

    public boolean containsWord(String s) {
        return m_model.containsKey(s);
    }

    public Set<String> getWords() {
        return m_model.keySet();
    }

    public String genWord() { //generates word for unigram
        double d = (new Random()).nextDouble();
        for (String s : m_model.keySet()) {
            d -= getSmoothedProb(s);
            if (d < 0) {
                return s;
            }
        }
        return "";
    }



    public Set<String> keySet() {
        return m_model.keySet();
    }

    public String toString() {
        return m_model.toString();
    }

}
