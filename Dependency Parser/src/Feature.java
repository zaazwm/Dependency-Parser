
public class Feature {
	//B for buffer, S for stack, <number> for position, f for Form, p for Pos, l for LeftMostPos, r for RightMostPos
	private String B0Form;
	private String B0Pos;
	private String S0Form;
	private String S0Pos;
	private String B1Form;
	private String B1Pos;
	private String S1Pos;
	private String B2Form;
	private String B2Pos;
	private String S2Pos;
	private String ldB0Pos;
	private String rdB0Pos;
	private String ldS0Pos;
	private String rdS0Pos;
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
	
	
	//public static final int nFeature = 7;
	public static final int nFeature = 6+7+2+6+7+5;
	
	public Feature(String b0f, String b0p, String s0f, String s0p, String b1f, String b1p, String s1p, String b2f, String b2p, String s2p, String ldb0p, String rdb0p, String lds0p, String rds0p, String label, String tag) {
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
		Label=label;
		nLabel=Configuration.getConfToInt(Label);
		LabelTag = tag;
		
		B0fp=B0Form+"-"+B0Pos;
		S0fp=S0Form+"-"+S0Pos;
		B0pS0p=B0Pos+"-"+S0Pos;
		B0fS0f=B0Form+"-"+S0Form;
		B01p=(B1Pos==null?null:(B0Pos+"-"+B1Pos));
		S01p=(S1Pos==null?null:(S0Pos+"-"+S1Pos));
		B0fpS0p=B0fp+"-"+S0Pos;
		B0fpS0f=B0fp+"-"+S0Form;
		B0fS0fp=B0Form+"-"+S0fp;
		B0pS0fp=B0Pos+"-"+S0fp;
		B0fS0p=B0Form+"-"+S0Pos;
		B0pS0f=B0Pos+"-"+S0Form;
		B0fpS0fp=B0fp+"-"+S0fp;
		B1fp=(B1Pos==null?null:(B1Form+"-"+B1Pos));
		B2fp=(B2Pos==null?null:(B2Form+"-"+B2Pos));
		B0pB1pB2p=((B1Pos==null||B2Pos==null)?null:(B0Pos+"-"+B1Pos+"-"+B2Pos));
		S0pB0pB1p=(B1Pos==null?null:(S0Pos+"-"+B0Pos+"-"+B1Pos));
		S0pS0lB0p=(ldS0Pos==null?null:(S0Pos+"-"+ldS0Pos+"-"+B0Pos));
		S0pS0rB0p=(rdS0Pos==null?null:(S0Pos+"-"+rdS0Pos+"-"+B0Pos));
		S0pB0pB0l=(ldB0Pos==null?null:(S0Pos+"-"+B0Pos+"-"+ldB0Pos));
	}
	
	public Feature(String b0f, String b0p, String s0f, String s0p, String b1f, String b1p, String s1p, String b2f, String b2p, String s2p, String ldb0p, String rdb0p, String lds0p, String rds0p, int label, String tag) {
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
		nLabel=label;
		Label=Configuration.getConfToString(nLabel);
		LabelTag = tag;
		
		B0fp=B0Form+"-"+B0Pos;
		S0fp=S0Form+"-"+S0Pos;
		B0pS0p=B0Pos+"-"+S0Pos;
		B0fS0f=B0Form+"-"+S0Form;
		B01p=(B1Pos==null?null:(B0Pos+"-"+B1Pos));
		S01p=(S1Pos==null?null:(S0Pos+"-"+S1Pos));
		B0fpS0p=B0fp+"-"+S0Pos;
		B0fpS0f=B0fp+"-"+S0Form;
		B0fS0fp=B0Form+"-"+S0fp;
		B0pS0fp=B0Pos+"-"+S0fp;
		B0fS0p=B0Form+"-"+S0Pos;
		B0pS0f=B0Pos+"-"+S0Form;
		B0fpS0fp=B0fp+"-"+S0fp;
		B1fp=(B1Pos==null?null:(B1Form+"-"+B1Pos));
		B2fp=(B2Pos==null?null:(B2Form+"-"+B2Pos));
		B0pB1pB2p=((B1Pos==null||B2Pos==null)?null:(B0Pos+"-"+B1Pos+"-"+B2Pos));
		S0pB0pB1p=(B1Pos==null?null:(S0Pos+"-"+B0Pos+"-"+B1Pos));
		S0pS0lB0p=(ldS0Pos==null?null:(S0Pos+"-"+ldS0Pos+"-"+B0Pos));
		S0pS0rB0p=(rdS0Pos==null?null:(S0Pos+"-"+rdS0Pos+"-"+B0Pos));
		S0pB0pB0l=(ldB0Pos==null?null:(S0Pos+"-"+B0Pos+"-"+ldB0Pos));
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
		
		if(index==0)
			return B0fp;
		if(index==1)
			return S0fp;
		if(index==2)
			return B0pS0p;
		if(index==3)
			return B0fS0f;
		if(index==4)
			return B01p;
		if(index==5)
			return S01p;
		
		if(index==6)
			return B0fpS0p;
		if(index==7)
			return B0fpS0f;
		if(index==8)
			return B0fS0fp;
		if(index==9)
			return B0pS0fp;
		if(index==10)
			return B0fS0p;
		if(index==11)
			return B0pS0f;
		if(index==12)
			return B0fpS0fp;
		
		if(index==13)
			return ldB0Pos;
		if(index==14)
			return rdB0Pos;
		
		if(index==15)
			return B0Form;
		if(index==16)
			return B0Pos;
		if(index==17)
			return S0Form;
		if(index==18)
			return S0Pos;
		if(index==19)
			return B1Pos;
		if(index==20)
			return S1Pos;
		
		if(index==21)
			return B1fp;
		if(index==22)
			return B1Form;
		if(index==23)
			return B2fp;
		if(index==24)
			return B2Form;
		if(index==25)
			return B2Pos;
		if(index==26)
			return ldS0Pos;
		if(index==27)
			return rdS0Pos;
		
		if(index==28)
			return B0pB1pB2p;
		if(index==29)
			return S0pB0pB1p;
		if(index==30)
			return S0pS0lB0p;
		if(index==31)
			return S0pS0rB0p;
		if(index==32)
			return S0pB0pB0l;
		
		return null;
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
		
		if(index==0)
			return "B0fp";
		if(index==1)
			return "S0fp";
		if(index==2)
			return "B0pS0p";
		if(index==3)
			return "B0fS0f";
		if(index==4)
			return "B01p";
		if(index==5)
			return "S01p";
		
		if(index==6)
			return "B0fpS0p";
		if(index==7)
			return "B0fpS0f";
		if(index==8)
			return "B0fS0fp";
		if(index==9)
			return "B0pS0fp";
		if(index==10)
			return "B0fS0p";
		if(index==11)
			return "B0pS0f";
		if(index==12)
			return "B0fpS0fp";
		
		if(index==13)
			return "ldB0Pos";
		if(index==14)
			return "rdB0Pos";
		
		if(index==15)
			return "B0Form";
		if(index==16)
			return "B0Pos";
		if(index==17)
			return "S0Form";
		if(index==18)
			return "S0Pos";
		if(index==19)
			return "B1Pos";
		if(index==20)
			return "S1Pos";
		
		if(index==21)
			return "B1fp";
		if(index==22)
			return "B1Form";
		if(index==23)
			return "B2fp";
		if(index==24)
			return "B2Form";
		if(index==25)
			return "B2Pos";
		if(index==26)
			return "ldS0Pos";
		if(index==27)
			return "rdS0Pos";
		
		if(index==28)
			return "B0pB1pB2p";
		if(index==29)
			return "S0pB0pB1p";
		if(index==30)
			return "S0pS0lB0p";
		if(index==31)
			return "S0pS0rB0p";
		if(index==32)
			return "S0pB0pB0l";
		
		return null;
	}

	public String getB0Form() {
		return B0Form;
	}

	public void setB0Form(String b0Form) {
		B0Form = b0Form;
	}

	public String getB0Pos() {
		return B0Pos;
	}

	public void setB0Pos(String b0Pos) {
		B0Pos = b0Pos;
	}

	public String getS0Form() {
		return S0Form;
	}

	public void setS0Form(String s0Form) {
		S0Form = s0Form;
	}

	public String getS0Pos() {
		return S0Pos;
	}

	public void setS0Pos(String s0Pos) {
		S0Pos = s0Pos;
	}

	public String getB1Form() {
		return B1Form;
	}

	public void setB1Form(String b1Form) {
		B1Form = b1Form;
	}

	public String getB1Pos() {
		return B1Pos;
	}

	public void setB1Pos(String b1Pos) {
		B1Pos = b1Pos;
	}

	public String getS1Pos() {
		return S1Pos;
	}

	public void setS1Pos(String s1Pos) {
		S1Pos = s1Pos;
	}

	public String getB2Form() {
		return B2Form;
	}

	public void setB2Form(String b2Form) {
		B2Form = b2Form;
	}

	public String getB2Pos() {
		return B2Pos;
	}

	public void setB2Pos(String b2Pos) {
		B2Pos = b2Pos;
	}

	public String getS2Pos() {
		return S2Pos;
	}

	public void setS2Pos(String s2Pos) {
		S2Pos = s2Pos;
	}

	public String getLdB0Pos() {
		return ldB0Pos;
	}

	public void setLdB0Pos(String ldB0Pos) {
		this.ldB0Pos = ldB0Pos;
	}

	public String getLdS0Pos() {
		return ldS0Pos;
	}

	public void setLdS0Pos(String ldS0Pos) {
		this.ldS0Pos = ldS0Pos;
	}

	public String getRdS0Pos() {
		return rdS0Pos;
	}

	public void setRdS0Pos(String rdS0Pos) {
		this.rdS0Pos = rdS0Pos;
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
