import java.util.ArrayList;
import java.util.Arrays;

public class Feature {
	//B for buffer, S for stack, <number> for position, f for Form, p for Pos, l for LeftMostPos, r for RightMostPos
	//ld for leftMostDependent, rd for rightMostDependent, hd for head, hd2 for head of head, d for distance (after bucket)
	
	private String B0Form;  //original
	private String B0Pos;  //original
	private String S0Form;  //original
	private String S0Pos;  //original
	private String B1Form;  //original
	private String B1Pos;  //original
	private String S1Pos;  //original
	private String B2Form;  //original
	private String B2Pos;  //original
	@SuppressWarnings("unused")
	private String S2Pos;  //original
	private String ldB0Pos;  //original
	private String rdB0Pos;  //original
	private String ldS0Pos;  //original
	private String rdS0Pos;  //original
	private String Label;
	private int nLabel;
	private String LabelTag;
	
	private String B0fp;
	private String S0fp;
	private String B0pS0p;
	private String B0fS0f;
	private String B01p;
	private String S01p;
	private String B0fpS0p;
	private String B0fpS0f;
	private String B0fS0fp;
	private String B0pS0fp;
	private String B0fS0p;
	private String B0pS0f;
	private String B0fpS0fp;
	
	private String B1fp;
	private String B2fp;
	private String B0pB1pB2p;
	private String S0pB0pB1p;
	private String S0pS0lB0p;
	private String S0pS0rB0p;
	private String S0pB0pB0l;
	//---
	private String S0hpS0pB0p;
	
	private int distanceS0B0;  //original
	private String S0fd;
	private String S0pd;
	private String B0fd;
	private String B0pd;
	private String S0fB0fd;
	private String S0pB0pd;
	
	private String hdS0f;  //original
	private String hdS0p;  //original
	private String ldS0f;  //original
	private String rdS0f;  //original
	private String ldB0f;  //original
	
	private String hd2S0f;  //original
	private String hd2S0p;  //original
	private String S0pS0hpS0h2p;
	
	//morphology
	private String B0Morph;  //ignore
	private String S0Morph;  //ignore
	private String B0mS0m;
	private String B0pS0m;
	private String B0mS0p;
	private String B0mf;
	private String S0mf;
	
	//public static final int nFeature = 7;
	public static final int nFeature = 6+7+2+6+7+5+1+6+5+3+5;
	
	private static final String sep = "-"; 
	
	public Feature(String b0f, String b0p, String s0f, String s0p, String b1f, String b1p, String s1p, String b2f, String b2p, String s2p, String ldb0p, String rdb0p, String lds0p, String rds0p, int dist, String hds0f, String hds0p, String lds0f, String rds0f, String ldb0f, String hd2s0f, String hd2s0p, String b0m, String s0m, String label, String tag) {
		B0Form=b0f;
		B0Pos=b0p;
		S0Form=s0f;
		S0Pos=s0p;
		B1Form=b1f;
		B1Pos=b1p;
		S1Pos=s1p;
		B2Form=b2f;
		B2Pos=b2p;
		S2Pos=s2p;
		ldB0Pos=ldb0p;
		rdB0Pos=rdb0p;
		ldS0Pos=lds0p;
		rdS0Pos=rds0p;
		
		distanceS0B0=dist;
		hdS0f=hds0f;
		hdS0p=hds0p;
		ldS0f=lds0f;
		rdS0f=rds0f;
		ldB0f=ldb0f;
		hd2S0f=hd2s0f;
		hd2S0p=hd2s0p;
		
		B0Morph=b0f;
		S0Morph=s0f;
		
		Label=label;
		nLabel=Configuration.getConfToInt(Label);
		LabelTag = tag;
		
		this.composeFeature();
	}
	
	public Feature(String b0f, String b0p, String s0f, String s0p, String b1f, String b1p, String s1p, String b2f, String b2p, String s2p, String ldb0p, String rdb0p, String lds0p, String rds0p, int dist, String hds0f, String hds0p, String lds0f, String rds0f, String ldb0f, String hd2s0f, String hd2s0p, String b0m, String s0m, int label, String tag) {
		B0Form=b0f;
		B0Pos=b0p;
		S0Form=s0f;
		S0Pos=s0p;
		B1Form=b1f;
		B1Pos=b1p;
		S1Pos=s1p;
		B2Form=b2f;
		B2Pos=b2p;
		S2Pos=s2p;
		ldB0Pos=ldb0p;
		rdB0Pos=rdb0p;
		ldS0Pos=lds0p;
		rdS0Pos=rds0p;
		
		distanceS0B0=dist;
		hdS0f=hds0f;
		hdS0p=hds0p;
		ldS0f=lds0f;
		rdS0f=rds0f;
		ldB0f=ldb0f;
		hd2S0f=hd2s0f;
		hd2S0p=hd2s0p;

		B0Morph=b0f;
		S0Morph=s0f;
		
		nLabel=label;
		Label=Configuration.getConfToString(nLabel);
		LabelTag = tag;
		
		this.composeFeature();
	}
	
	private void composeFeature() {
		B0fp=B0Form+sep+B0Pos;
		S0fp=S0Form+sep+S0Pos;
		B0pS0p=B0Pos+sep+S0Pos;
		B0fS0f=B0Form+sep+S0Form;
		B01p=(B0Pos+sep+B1Pos);
		S01p=(S0Pos+sep+S1Pos);
		B0fpS0p=B0fp+sep+S0Pos;
		B0fpS0f=B0fp+sep+S0Form;
		B0fS0fp=B0Form+sep+S0fp;
		B0pS0fp=B0Pos+sep+S0fp;
		B0fS0p=B0Form+sep+S0Pos;
		B0pS0f=B0Pos+sep+S0Form;
		B0fpS0fp=B0fp+sep+S0fp;
		B1fp=(B1Form+sep+B1Pos);
		B2fp=(B2Form+sep+B2Pos);
		B0pB1pB2p=(B0Pos+sep+B1Pos+sep+B2Pos);
		S0pB0pB1p=(S0Pos+sep+B0Pos+sep+B1Pos);
		S0pS0lB0p=(S0Pos+sep+ldS0Pos+sep+B0Pos);
		S0pS0rB0p=(S0Pos+sep+rdS0Pos+sep+B0Pos);
		S0pB0pB0l=(S0Pos+sep+B0Pos+sep+ldB0Pos);
		//---
		S0hpS0pB0p=(hdS0p+sep+S0Pos+sep+B0Pos);
		
		String dist = bucket(distanceS0B0);
		S0fd=(S0Form+sep+dist);
		S0pd=(S0Pos+sep+dist);
		B0fd=(B0Form+sep+dist);
		B0pd=(B0Pos+sep+dist);
		S0fB0fd=(S0Form+sep+B0Form+sep+dist);
		S0pB0pd=(S0Pos+sep+B0Pos+sep+dist);
		
		S0pS0hpS0h2p=(S0Pos+sep+hdS0p+sep+hd2S0p);
		
		if(ApplicationControl.useMorph) {
			B0mS0m=B0Morph+sep+S0Morph;
			B0pS0m=B0Pos+sep+S0Morph;
			B0mS0p=B0Morph+sep+S0Pos;
			B0mf=B0Morph+sep+B0Form;
			S0mf=S0Morph+sep+S0Form;
		}
		else {
			B0mS0m=null;
			B0pS0m=null;
			B0mS0p=null;
			B0mf=null;
			S0mf=null;
		}
	}
	
	public String getValueOf(int index) {
//		if(index==0)
//			return B0Form;
//		if(index==1)
//			return B0Pos;
//		if(index==2)
//			return S0Form;
//		if(index==3)
//			return S0Pos;
//		if(index==4)
//			return B1Pos;
//		if(index==5)
//			return S1Pos;
//		if(index==6)
//			return ldB0Pos;
		switch(index) {
		case 0:
			return B0fp;
		case 1:
			return S0fp;
		case 2:
			return B0pS0p;
		case 3:
			return B0fS0f;
		case 4:
			return B01p;
		case 5:
			return S01p;
		
		case 6:
			return B0fpS0p;
		case 7:
			return B0fpS0f;
		case 8:
			return B0fS0fp;
		case 9:
			return B0pS0fp;
		case 10:
			return B0fS0p;
		case 11:
			return B0pS0f;
		case 12:
			return B0fpS0fp;
		
		case 13:
			return ldB0Pos;
		case 14:
			return rdB0Pos;
		
		case 15:
			return B0Form;
		case 16:
			return B0Pos;
		case 17:
			return S0Form;
		case 18:
			return S0Pos;
		case 19:
			return B1Pos;
		case 20:
			return S1Pos;
		
		case 21:
			return B1fp;
		case 22:
			return B1Form;
		case 23:
			return B2fp;
		case 24:
			return B2Form;
		case 25:
			return B2Pos;
		case 26:
			return ldS0Pos;
		case 27:
			return rdS0Pos;
		
		case 28:
			return B0pB1pB2p;
		case 29:
			return S0pB0pB1p;
		case 30:
			return S0pS0lB0p;
		case 31:
			return S0pS0rB0p;
		case 32:
			return S0pB0pB0l;
			
		case 33:
			return S0hpS0pB0p;
			
		case 34:
			return S0fd;
		case 35:
			return S0pd;
		case 36:
			return B0fd;
		case 37:
			return B0pd;
		case 38:
			return S0fB0fd;
		case 39:
			return S0pB0pd;
			
		case 40:
			return hdS0f;
		case 41:
			return hdS0p;
		case 42:
			return ldS0f;
		case 43:
			return rdS0f;
		case 44:
			return ldB0f;
			
		case 45:
			return hd2S0f;
		case 46:
			return hd2S0p;
		case 47:
			return S0pS0hpS0h2p;
			
		case 48:
			return B0mS0m;
		case 49:
			return B0pS0m;
		case 50:
			return B0mS0p;
		case 51:
			return B0mf;
		case 52:
			return S0mf;
			
		default:
			return null;
		}
	}
	
	public String getNameOf(int index) {
//		if(index==0)
//			return "B0Form";
//		if(index==1)
//			return "B0Pos";
//		if(index==2)
//			return "S0Form";
//		if(index==3)
//			return "S0Pos";
//		if(index==4)
//			return "B1Pos";
//		if(index==5)
//			return "S1Pos";
//		if(index==6)
//			return "ldB0Pos";
		switch(index) {
		case 0:
			return "B0fp";
		case 1:
			return "S0fp";
		case 2:
			return "B0pS0p";
		case 3:
			return "B0fS0f";
		case 4:
			return "B01p";
		case 5:
			return "S01p";
		
		case 6:
			return "B0fpS0p";
		case 7:
			return "B0fpS0f";
		case 8:
			return "B0fS0fp";
		case 9:
			return "B0pS0fp";
		case 10:
			return "B0fS0p";
		case 11:
			return "B0pS0f";
		case 12:
			return "B0fpS0fp";
		
		case 13:
			return "ldB0Pos";
		case 14:
			return "rdB0Pos";
		
		case 15:
			return "B0Form";
		case 16:
			return "B0Pos";
		case 17:
			return "S0Form";
		case 18:
			return "S0Pos";
		case 19:
			return "B1Pos";
		case 20:
			return "S1Pos";
		
		case 21:
			return "B1fp";
		case 22:
			return "B1Form";
		case 23:
			return "B2fp";
		case 24:
			return "B2Form";
		case 25:
			return "B2Pos";
		case 26:
			return "ldS0Pos";
		case 27:
			return "rdS0Pos";
		
		case 28:
			return "B0pB1pB2p";
		case 29:
			return "S0pB0pB1p";
		case 30:
			return "S0pS0lB0p";
		case 31:
			return "S0pS0rB0p";
		case 32:
			return "S0pB0pB0l";
			
		case 33:
			return "S0hpS0pB0p";
			
		case 34:
			return "S0fd";
		case 35:
			return "S0pd";
		case 36:
			return "B0fd";
		case 37:
			return "B0pd";
		case 38:
			return "S0fB0fd";
		case 39:
			return "S0pB0pd";
			
		case 40:
			return "hdS0f";
		case 41:
			return "hdS0p";
		case 42:
			return "ldS0f";
		case 43:
			return "rdS0f";
		case 44:
			return "ldB0f";
			
		case 45:
			return "hd2S0f";
		case 46:
			return "hd2S0p";
		case 47:
			return "S0pS0hpS0h2p";
			
		case 48:
			return "B0mS0m";
		case 49:
			return "B0pS0m";
		case 50:
			return "B0mS0p";
		case 51:
			return "B0mf";
		case 52:
			return "S0mf";
			
		default:
			return null;
		}
	}
	
	private static final Integer[] fibseed = {1,2};
	private static ArrayList<Integer> fibarray = new ArrayList<Integer>(Arrays.asList(fibseed));
	
	private String bucket(int distance) {
		if(distance<=0)
			return null;
		
		while(distance>fibarray.get(fibarray.size()-1)) {
			fibarray.add(fibarray.get(fibarray.size()-1)+fibarray.get(fibarray.size()-2));
		}
		
		for(Integer fib : fibarray) {
			if(fib>=distance)
				return fib.toString();
		}
		
		return null;
	}

	public String getLabel() {
		return Label;
	}

	public void setLabel(String label) {
		Label = label;
	}

	public int getnLabel() {
		return nLabel;
	}

	public void setnLabel(int nLabel) {
		this.nLabel = nLabel;
	}

	public String getLabelTag() {
		return LabelTag;
	}

	public void setLabelTag(String labelTag) {
		LabelTag = labelTag;
	}
}
