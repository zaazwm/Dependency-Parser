import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;


public class Perceptron implements Serializable{
	
	private static final long serialVersionUID = 3513457841833955344L;
	private Integer nFeatureFull;
	private HashMap<String, Integer> feamap;
	private Weights[] wts;
	private Integer nLabel;
	
	private static final int maxIter = 10;
	
	public Perceptron(LinkedList<Feature> fl, int nLabels) {
		wts=new Weights[nLabels];  //save weights for each label
		Weights[] tls=new Weights[nLabels];  //for ave.perceptron
		feamap = new HashMap<String, Integer>();  //map feature name to uid
		nFeatureFull=0;  //amount of features
		nLabel=nLabels;  //number of labels
		
		for(int i=0;i<nLabels;i++) {
			wts[i]=new Weights();
			wts[i].setLabel(i);
			if(ApplicationControl.AvePerceptron) {
				tls[i]=new Weights();
				tls[i].setLabel(i);
			}
		}
		
		int q=0;
		for(int iter=1;iter<=maxIter;iter++) {  //do {10} iteration
		//start of iter
		int count = 0;
		for(Feature f : fl) {
			count++;
			q++;
			String[] str=new String[Feature.nFeature];
			for(int i=0;i<Feature.nFeature;i++) {
				str[i]=f.getNameOf(i)+"_"+f.getValueOf(i);  //generate name of feature
			}
			if(iter==1) {  //map feature to uid, only in first iteration
				for(int k=0;k<Feature.nFeature;k++) {
					if(!feamap.containsKey(str[k]) && f.getValueOf(k)!=null) {
						feamap.put(str[k], nFeatureFull);
						nFeatureFull++;
					}
				}
			}
			
			double[] tmpW = new double[nLabels];
			
			for(int i=0;i<nLabels;i++) {
				tmpW[i]=0D;
				for(int j=0;j<Feature.nFeature;j++) {  //find & calc weight for each feature
					if(wts[i].getWeightList().containsKey(feamap.get(str[j]))) {
						tmpW[i]+=wts[i].getWeightList().get(feamap.get(str[j]));
					}
				}
			}
			
			int pre = findMax(tmpW);  //predict label
			if(pre!=f.getnLabel()) {  //perceptron process
				for(int j=0;j<Feature.nFeature;j++) {
					//for predicted label
					if(wts[pre].getWeightList().containsKey(feamap.get(str[j]))) {
						wts[pre].getWeightList().put(feamap.get(str[j]), wts[pre].getWeightList().get(feamap.get(str[j]))-0.5D);
						if(ApplicationControl.AvePerceptron)
							tls[pre].getWeightList().put(feamap.get(str[j]), tls[pre].getWeightList().get(feamap.get(str[j]))-0.5D*q);
					}
					else {  //if weight was 0
						wts[pre].getWeightList().put(feamap.get(str[j]), -0.5D);
						if(ApplicationControl.AvePerceptron)
							tls[pre].getWeightList().put(feamap.get(str[j]), -0.5D*q);
					}
					
					//for correct label
					if(wts[f.getnLabel()].getWeightList().containsKey(feamap.get(str[j]))) {
						wts[f.getnLabel()].getWeightList().put(feamap.get(str[j]), wts[f.getnLabel()].getWeightList().get(feamap.get(str[j]))+0.5D);
						if(ApplicationControl.AvePerceptron)
							tls[f.getnLabel()].getWeightList().put(feamap.get(str[j]), tls[f.getnLabel()].getWeightList().get(feamap.get(str[j]))+0.5D*q);
					}
					else {  //if weight was 0
						wts[f.getnLabel()].getWeightList().put(feamap.get(str[j]), 0.5D);
						if(ApplicationControl.AvePerceptron)
							tls[f.getnLabel()].getWeightList().put(feamap.get(str[j]), 0.5D*q);
					}
				}
			}
			
			if(count%10000==0) {
				System.out.println("iteration: "+iter+" feature instances: "+count);
			}
		}
		//end of iter
		}
		if(ApplicationControl.AvePerceptron) {
			for(int i=0;i<nLabels;i++) {
				for(Integer key : wts[i].getWeightList().keySet()) {
					wts[i].getWeightList().put(key, wts[i].getWeightList().get(key)-tls[i].getWeightList().get(key)/q);
				}
			}
		}
	}
	
	private int findMax(double[] lst) {  //find max double in an array
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

	private class Weights implements Serializable{
		//use for saving weight vector

		private static final long serialVersionUID = 26192099206251056L;
		private int label;
		private HashMap<Integer,Double> wl;
		
		public Weights() {
			wl=new HashMap<Integer,Double>();
		}

		@SuppressWarnings("unused")
		public int getLabel() {
			return label;
		}

		public void setLabel(int label) {
			this.label = label;
		}

		public HashMap<Integer, Double> getWeightList() {
			return wl;
		}

		@SuppressWarnings("unused")
		public void setWeightList(HashMap<Integer, Double> wl) {
			this.wl = wl;
		}
	}
}
