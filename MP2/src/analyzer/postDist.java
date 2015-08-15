package analyzer;

import structures.Post;

public class postDist implements Comparable<postDist>{
	
	Post p;
	double similarity;
	
	public postDist(Post p, double similarity) {
		this.p = p;
		this.similarity = similarity;
	}

	@Override
	public int compareTo(postDist pd) {
		return Double.compare(this.similarity, pd.similarity);
		
	}
	
	public String toString() {		
		return (this.p.getContent() + " " + this.similarity);
		
	}

}
