import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;

public class LibSVM implements Serializable, LibClassifier{

	private static final long serialVersionUID = 5486636784394797294L;
	private Integer nFeatureFull;
	private Integer nTagFull;
	private HashMap<String, Integer> feamap;
	private HashMap<Integer, ArcTag> tagmap;
	private HashMap<ArcTag, Integer> itagmap;
	
	private HashMap<Integer, String> ntagmap;
	private HashMap<String, Integer> intagmap;
	
	private Integer nLabel;
	
	@SuppressWarnings("unused")
	private String modelPath;
	@SuppressWarnings("unused")
	private String featurePath;
	private svm_model model;
	
	public LibSVM(LinkedList<Feature> fl, int nLabels, String mPath, String fPath) throws IOException {
		feamap = new HashMap<String, Integer>();  //map feature name to uid
		tagmap = new HashMap<Integer, ArcTag>();  //map arcid to arctag
		itagmap = new HashMap<ArcTag, Integer>();  //map arctag to arcid
		nFeatureFull=1;  //amount of features
		nLabel=nLabels;  //number of labels
		modelPath=mPath;
		featurePath=fPath;
		nTagFull=nLabels+1;
		
		FileWriter fw = new FileWriter(fPath);
		for(Feature f : fl) {
			LinkedList<Integer> fealist = new LinkedList<Integer>();
			String[] str=new String[Feature.nFeature];
			
			//support predicting Arc-Tag
			ArcTag tmpArc = new ArcTag(f.getnLabel(),f.getLabelTag());
			if((f.getnLabel() == 1 || f.getnLabel() == 2) && ApplicationControl.predictArcTag) {
				if(!itagmap.containsKey(tmpArc)) {
					tagmap.put(nTagFull, tmpArc);
					itagmap.put(tmpArc, nTagFull);
					nTagFull++;
				}
			}
			else {
				if(!itagmap.containsKey(tmpArc)) {
					tagmap.put(f.getnLabel(), tmpArc);
					itagmap.put(tmpArc, f.getnLabel());
				}
			}
			
			for(int i=0;i<Feature.nFeature;i++) {
				str[i]=f.getNameOf(i)+"_"+f.getValueOf(i);  //generate name of feature
			}
			for(int k=0;k<Feature.nFeature;k++) {
				if(!feamap.containsKey(str[k])) {
					feamap.put(str[k], nFeatureFull);
					nFeatureFull++;
				}
				if(feamap.containsKey(str[k])) {
					//fw.write(" "+feamap.get(str[k])+":1.0");
					fealist.add(feamap.get(str[k]));
				}
			}
			
			if(!fealist.isEmpty()) {
				//fw.write(new Integer(f.getnLabel()).toString());
				fw.write(itagmap.get(tmpArc).toString());
				Collections.sort(fealist);
				for(Integer inte : fealist) {
					fw.write(" "+inte+":1.0");
				}
				fw.write("\n");
			}
		}
		fw.close();
		
		String trainArgs[] = { fPath, mPath };
		svm_train.main(trainArgs);
	}
	
	public LibSVM(LinkedList<TagFeature> tfl, String mPath, String fPath) throws IOException {
		//train model predicting tags
		feamap = new HashMap<String, Integer>();  //map feature name to uid
		ntagmap = new HashMap<Integer, String>();  //map arcid to arctag
		intagmap = new HashMap<String, Integer>();  //map arctag to arcid
		nFeatureFull=1;  //amount of features
		modelPath=mPath;
		featurePath=fPath;
		nTagFull=1;
		
		FileWriter fw = new FileWriter(fPath);
		for(TagFeature f : tfl) {
			LinkedList<Integer> fealist = new LinkedList<Integer>();
			String[] str=new String[TagFeature.nFeature];
			
			if(!intagmap.containsKey(f.get_tag())) {
				ntagmap.put(nTagFull, f.get_tag());
				intagmap.put(f.get_tag(), nTagFull);
				nTagFull++;
			}
			
			for(int i=0;i<TagFeature.nFeature;i++) {
				str[i]=f.getNameOf(i)+"_"+f.getValueOf(i);  //generate name of feature
			}
			for(int k=0;k<TagFeature.nFeature;k++) {
				if(!feamap.containsKey(str[k]) && f.getValueOf(k)!=null) {
					feamap.put(str[k], nFeatureFull);
					nFeatureFull++;
				}
				if(feamap.containsKey(str[k]) && f.getValueOf(k)!=null) {
					//fw.write(" "+feamap.get(str[k])+":1.0");
					fealist.add(feamap.get(str[k]));
				}
			}
			
			if(!fealist.isEmpty()) {
				//fw.write(new Integer(f.getnLabel()).toString());
				fw.write(intagmap.get(f.get_tag()).toString());
				Collections.sort(fealist);
				for(Integer inte : fealist) {
					fw.write(" "+inte+":1.0");
				}
				fw.write("\n");
			}
		}
		fw.close();
		
		String trainArgs[] = { fPath, mPath };
		svm_train.main(trainArgs);
	}
	
	@Override
	public void readModel(String mPath) throws IOException {
		model=svm.svm_load_model(mPath);
	}
	
	@Override
	public ArcTag findBest(Feature f) {  //find best label given features
		String[] str=new String[Feature.nFeature];
		for(int i=0;i<Feature.nFeature;i++) {
			str[i]=f.getNameOf(i)+"_"+f.getValueOf(i);
		}
		
		int feacount=0;
		for(int j=0;j<Feature.nFeature;j++) {
			if(feamap.containsKey(str[j])) {
				feacount++;
			}
		}
		svm_node[] x = new svm_node[feacount];
		LinkedList<Integer> fealist = new LinkedList<Integer>();
		for(int j=0;j<Feature.nFeature;j++) {
			if(feamap.containsKey(str[j])) {
				fealist.add(feamap.get(str[j]));
			}
		}
		feacount=0;
		for(Integer inte : fealist) {
			x[feacount]=new svm_node();
			x[feacount].index=inte;
			x[feacount].value=1.0D;
			feacount++;
		}
		
		Double v=svm.svm_predict(model, x);
		return tagmap.get(v.intValue());
	}
	
	@Override
	public String findBest(TagFeature f) {  //find best tag given feature
		String[] str=new String[TagFeature.nFeature];
		for(int i=0;i<TagFeature.nFeature;i++) {
			str[i]=f.getNameOf(i)+"_"+f.getValueOf(i);
		}
		
		int feacount=0;
		for(int j=0;j<TagFeature.nFeature;j++) {
			if(feamap.containsKey(str[j])) {
				feacount++;
			}
		}
		svm_node[] x = new svm_node[feacount];
		LinkedList<Integer> fealist = new LinkedList<Integer>();
		for(int j=0;j<TagFeature.nFeature;j++) {
			if(feamap.containsKey(str[j])) {
				fealist.add(feamap.get(str[j]));
			}
		}
		feacount=0;
		for(Integer inte : fealist) {
			x[feacount]=new svm_node();
			x[feacount].index=inte;
			x[feacount].value=1.0D;
			feacount++;
		}
		
		Double v=svm.svm_predict(model, x);
		return ntagmap.get(v.intValue());
	}
	
	@Override
	public int getnFeature() {
		return nFeatureFull-1;
	}

	@Override
	public int getnTag() {
		return nTagFull-nLabel-1;
	}
	
	@Override
	public int getnNewTag() {
		return nTagFull-1;
	}
}
