import java.io.Serializable;
import java.util.HashMap;

public abstract class OnlinePerceptron implements Serializable{
	private static final long serialVersionUID = 2980548306205230812L;
	
	public static int maxIter = 15;
	protected static final double delta = 0.5D;

	public abstract void inputFeature(Feature f, int correctLabel, int predictLabel, int[] correctLabelList);
	public abstract int explore(int nCorrect, int nPredict);
	public abstract int explore(int nCorrect, int nPredict, int itNr);
	public abstract int findBest(Feature f);
	public abstract int[] findBestList(Feature f);
	public abstract int getnFeature();
	public abstract void averageWeights();
	
	protected abstract int findMax(double[] lst);
	protected abstract int[] findMaxList(double[] lst);

	public abstract void setnFeature(int nFeature);
	
	public abstract HashMap<String, Integer> getFeamap();

	public abstract void setFeamap(HashMap<String, Integer> feamap);

	public abstract Weights[] getWts();

	public abstract void setWts(Weights[] wts);

	public abstract Integer getnLabel();

	public abstract void setnLabel(Integer nLabel);
	
	protected class Weights implements Serializable{
		//use for saving weight vector

		private static final long serialVersionUID = 434242940165045156L;
		private int label;
		private HashMap<Integer,Double> wl;
		
		public Weights() {
			wl=new HashMap<Integer,Double>();
		}

		public int getLabel() {
			return label;
		}

		public void setLabel(int label) {
			this.label = label;
		}

		public HashMap<Integer, Double> getWeightList() {
			return wl;
		}

		public void setWeightList(HashMap<Integer, Double> wl) {
			this.wl = wl;
		}
	}
}
