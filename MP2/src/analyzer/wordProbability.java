package analyzer;

public class wordProbability {
	public String word;
	public double probability;
	
	public wordProbability() {
		word = "";
		probability = 0.0;
	}
	
	public wordProbability(String word, double probability) {
		this.word = word;
		this.probability = probability;
	}

}
