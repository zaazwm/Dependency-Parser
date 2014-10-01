import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.InvalidInputDataException;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Parameter;
import de.bwaldvogel.liblinear.Problem;
import de.bwaldvogel.liblinear.SolverType;


@SuppressWarnings("unused")
public class LibLinear implements Serializable, LibClassifier{

	private static final long serialVersionUID = 241399644135265529L;
	private Integer nFeatureFull;
	private Integer nTagFull;
	private HashMap<String, Integer> feamap;
	private HashMap<Integer, ArcTag> tagmap;
	private HashMap<ArcTag, Integer> itagmap;
	
	private Integer nLabel;
	
	private String modelPath;
	private String featurePath;
	private Model model;
	
	@SuppressWarnings("static-access")
	public LibLinear(LinkedList<Feature> fl, int nLabels, String mPath, String fPath) throws IOException, InvalidInputDataException {
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
				fw.write(itagmap.get(tmpArc).toString());
				Collections.sort(fealist);
				for(Integer inte : fealist) {
					fw.write(" "+inte+":1.0");
				}
				fw.write("\n");
			}
		}
		fw.close();
		
		model=Linear.train(new Problem().readFromFile(new File(fPath), 0), new Parameter(SolverType.L2R_L2LOSS_SVC, 1.0, 0.01));
		model.save(new File(mPath));
	}
	
	@Override
	public void readModel(String mPath) throws IOException {
		model = Model.load(new File(mPath));
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
		FeatureNode[] x = new FeatureNode[feacount];
		LinkedList<Integer> fealist = new LinkedList<Integer>();
		for(int j=0;j<Feature.nFeature;j++) {
			if(feamap.containsKey(str[j])) {
				fealist.add(feamap.get(str[j]));
			}
		}
		feacount=0;
		for(Integer inte : fealist) {
			x[feacount]=new FeatureNode(inte,1.0D);
			feacount++;
		}
		
		Double v=Linear.predict(model, x);
		return tagmap.get(v.intValue());
	}
	
	@Override
	public int getnFeature() {
		return nFeatureFull-1;
	}

	@Override
	public int getnTag() {
		return nTagFull-nLabel-1;
	}
}
