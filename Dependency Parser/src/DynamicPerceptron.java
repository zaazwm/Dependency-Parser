import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

public class DynamicPerceptron implements Serializable {

	private static final long serialVersionUID = 7849504885176176606L;
	
	protected Integer nFeatureFull;
	protected HashMap<String, Integer> feamap;
	protected Weights[] wts;
	protected Integer nLabel;
	
	protected Random rnd;
	
	public static final int maxIter = 15;
	
	protected static final double pIncorrect = 0.9D;
	protected static final int dynamicIteration = 1;
	
	public DynamicPerceptron(int nLabels) {
		wts=new Weights[nLabels];  //save weights for each label
		feamap = new HashMap<String, Integer>();  //map feature name to uid
		nFeatureFull=0;  //amount of features
		nLabel=nLabels;  //number of labels
		
		for(int i=0;i<nLabels;i++) {
			wts[i]=new Weights();
			wts[i].setLabel(i);
		}
		
		rnd = new Random();
	}
	
	public void inputFeature(Feature f, int correctLabel, int predictLabel, int[] correctLabelList) {
		if(correctLabelList[predictLabel]!=-1)  //correct[] contains predict 
			return;
		
		String[] str=new String[Feature.nFeature];
		for(int i=0;i<Feature.nFeature;i++) {
			str[i]=f.getNameOf(i)+"_"+f.getValueOf(i);  //generate name of feature
		}
		for(int k=0;k<Feature.nFeature;k++) {
			if(!feamap.containsKey(str[k]) && f.getValueOf(k)!=null) {
				feamap.put(str[k], nFeatureFull);
				nFeatureFull++;
			}
		}
		
		for(int j=0;j<Feature.nFeature;j++) {
			//for predicted label
			if(wts[predictLabel].getWeightList().containsKey(feamap.get(str[j]))) {
				wts[predictLabel].getWeightList().put(feamap.get(str[j]), wts[predictLabel].getWeightList().get(feamap.get(str[j]))-0.5D);
			}
			else {  //if weight was 0
				wts[predictLabel].getWeightList().put(feamap.get(str[j]), -0.5D);
			}
			
			//for correct label
			if(wts[correctLabel].getWeightList().containsKey(feamap.get(str[j]))) {
				wts[correctLabel].getWeightList().put(feamap.get(str[j]), wts[correctLabel].getWeightList().get(feamap.get(str[j]))+0.5D);
			}
			else {  //if weight was 0
				wts[correctLabel].getWeightList().put(feamap.get(str[j]), 0.5D);
			}
		}
	}
	
	public int explore(int nCorrect, int nPredict) {
		if(nCorrect==nPredict)
			return nCorrect;
		
		if(rnd.nextDouble()<pIncorrect)
			return nPredict;
		else
			return nCorrect;
	}
	
	public int explore(int nCorrect, int nPredict, int itNr) {
		if(nCorrect==nPredict)
			return nCorrect;
		
		if(rnd.nextDouble()<pIncorrect && itNr>dynamicIteration)
			return nPredict;
		else
			return nCorrect;
	}
	
	protected int findMax(double[] lst) {  //find max double in an array
		int maxn=-1;
		double maxs=Double.NEGATIVE_INFINITY;
		for(int i=0;i<lst.length;i++) {
			if(lst[i]>maxs) {
				maxn=i;
				maxs=lst[i];
			}
		}
		return maxn;
	}
	
	protected int[] findMaxList(double[] lst) {  //find max double in an array
		int[] maxn=new int[lst.length];
		boolean[] used=new boolean[lst.length];
		Arrays.fill(used, false);
		for(int pos=0;pos<lst.length;pos++) {
			int maxn1=-1;
			double maxs=Double.NEGATIVE_INFINITY;
			for(int i=0;i<lst.length;i++) {
				if(used[i])
					continue;
				if(lst[i]>maxs) {
					maxn1=i;
					maxs=lst[i];
				}
			}
			maxn[pos]=maxn1;
			used[maxn1]=true;
		}
		return maxn;
	}
	
	public int findBest(Feature f) {  //find best label given features
		String[] str=new String[Feature.nFeature];
		for(int i=0;i<Feature.nFeature;i++) {
			str[i]=f.getNameOf(i)+"_"+f.getValueOf(i);
		}
		
		double[] tmpW = new double[nLabel];
		
		for(int i=0;i<nLabel;i++) {
			tmpW[i]=0D;
			for(int j=0;j<Feature.nFeature;j++) {
				if(feamap.containsKey(str[j])) {
					if(wts[i].getWeightList().containsKey(feamap.get(str[j]))) {
						tmpW[i]+=wts[i].getWeightList().get(feamap.get(str[j]));
					}
				}
			}
		}
		
		return findMax(tmpW);
	}
	
	public int[] findBestList(Feature f) {
		String[] str=new String[Feature.nFeature];
		for(int i=0;i<Feature.nFeature;i++) {
			str[i]=f.getNameOf(i)+"_"+f.getValueOf(i);
		}
		
		double[] tmpW = new double[nLabel];
		
		for(int i=0;i<nLabel;i++) {
			tmpW[i]=0D;
			for(int j=0;j<Feature.nFeature;j++) {
				if(feamap.containsKey(str[j])) {
					if(wts[i].getWeightList().containsKey(feamap.get(str[j]))) {
						tmpW[i]+=wts[i].getWeightList().get(feamap.get(str[j]));
					}
				}
			}
		}
		
		return findMaxList(tmpW);
	}

	public int getnFeature() {
		return nFeatureFull;
	}

	public void setnFeature(int nFeature) {
		this.nFeatureFull = nFeature;
	}
	
	public HashMap<String, Integer> getFeamap() {
		return feamap;
	}

	public void setFeamap(HashMap<String, Integer> feamap) {
		this.feamap = feamap;
	}

	public Weights[] getWts() {
		return wts;
	}

	public void setWts(Weights[] wts) {
		this.wts = wts;
	}

	public Integer getnLabel() {
		return nLabel;
	}

	public void setnLabel(Integer nLabel) {
		this.nLabel = nLabel;
	}
	
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
