package structures;

import java.io.Serializable;

/**
 * Created by cheta_000 on 4/14/2015.
 */
public class PosFreqPair implements Serializable{
    public int freq;
    public boolean pos;
    public boolean test;

    public PosFreqPair(int freq, boolean pos, boolean test) {
        this.freq = freq;
        this.pos = pos;
        this.test = test;
    }

    public void incFreq() {
        freq += 1;
    }

    public String toString() {
        return "freq: " + freq + ", pos?: " + pos;
    }

}
