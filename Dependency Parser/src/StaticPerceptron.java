import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;

public class StaticPerceptron extends OnlinePerceptron implements Serializable {

	private static final long serialVersionUID = -2596623044233855453L;

	protected Integer nFeatureFull;
	protected HashMap<String, Integer> feamap;
	protected Weights[] wts;
	protected Weights[] tls;
	protected Integer nLabel;
	protected int q;
	
	
	public StaticPerceptron(int nLabels) {
		tls=new Weights[nLabels];  //for average perceptron
		wts=new Weights[nLabels];  //save weights for each label
		feamap = new HashMap<String, Integer>();  //map feature name to uid
		nFeatureFull=0;  //amount of features
		nLabel=nLabels;  //number of labels
		q=0;  //for average perceptron
		
		for(int i=0;i<nLabels;i++) {
			wts[i]=new Weights();
			wts[i].setLabel(i);
			tls[i]=new Weights();
			tls[i].setLabel(i);
		}
		
	}
	
	@Override
	public void inputFeature(Feature f, int correctLabel, int predictLabel, int[] correctLabelList) {
		q++;
		if(correctLabelList[predictLabel]!=-1)  //correct[] contains predict 
			return;
		if(correctLabel==predictLabel)
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
				wts[predictLabel].getWeightList().put(feamap.get(str[j]), wts[predictLabel].getWeightList().get(feamap.get(str[j]))-delta);
				tls[predictLabel].getWeightList().put(feamap.get(str[j]), tls[predictLabel].getWeightList().get(feamap.get(str[j]))-delta*q);
			}
			else {  //if weight was 0
				wts[predictLabel].getWeightList().put(feamap.get(str[j]), -delta);
				tls[predictLabel].getWeightList().put(feamap.get(str[j]), -delta*q);
			}
			
			//for correct label
			if(wts[correctLabel].getWeightList().containsKey(feamap.get(str[j]))) {
				wts[correctLabel].getWeightList().put(feamap.get(str[j]), wts[correctLabel].getWeightList().get(feamap.get(str[j]))+delta);
				tls[correctLabel].getWeightList().put(feamap.get(str[j]), tls[correctLabel].getWeightList().get(feamap.get(str[j]))+delta*q);
			}
			else {  //if weight was 0
				wts[correctLabel].getWeightList().put(feamap.get(str[j]), delta);
				tls[correctLabel].getWeightList().put(feamap.get(str[j]), delta*q);
			}
		}
	}
	
	@Override
	public void averageWeights() {
		for(int i=0;i<nLabel;i++) {
			for(Integer key : wts[i].getWeightList().keySet()) {
				wts[i].getWeightList().put(key, wts[i].getWeightList().get(key)-tls[i].getWeightList().get(key)/q);
			}
		}
		q=0;
	}
	
	@Override
	public int explore(int nCorrect, int nPredict) {
		return explore(nCorrect, nPredict, Integer.MAX_VALUE);
	}
	
	@Override
	public int explore(int nCorrect, int nPredict, int itNr) {
		return nCorrect;
	}
	
	@Override
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
	
	@Override
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
	
	@Override
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
	
	@Override
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

	@Override
	public int getnFeature() {
		return nFeatureFull;
	}

	@Override
	public void setnFeature(int nFeature) {
		this.nFeatureFull = nFeature;
	}
	
	@Override
	public HashMap<String, Integer> getFeamap() {
		return feamap;
	}

	@Override
	public void setFeamap(HashMap<String, Integer> feamap) {
		this.feamap = feamap;
	}

	@Override
	public Weights[] getWts() {
		return wts;
	}

	@Override
	public void setWts(Weights[] wts) {
		this.wts = wts;
	}

	@Override
	public Integer getnLabel() {
		return nLabel;
	}

	@Override
	public void setnLabel(Integer nLabel) {
		this.nLabel = nLabel;
	}
}
