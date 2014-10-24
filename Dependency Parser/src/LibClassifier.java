import java.io.IOException;


public interface LibClassifier {
	//interface for LibLinear & LibSVM, used in decoder
	public void readModel(String mPath) throws IOException;
	public ArcTag findBest(Feature f);
	public String findBest(TagFeature f);
	public int getnFeature();
	public int getnTag();
	public int getnNewTag();
}
