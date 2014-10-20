
public class Configuration {
	private State st;
	private Sentence stc;
	private int conf;
	private String tag;
	
	public Configuration(State s,String cf, String tg) {
		st=s;
		tag=tg;
		if(cf.equalsIgnoreCase("Shift")) {
			conf=0;
		}
		else if(cf.equalsIgnoreCase("LeftArc")) {
			conf=1;
		}
		else if(cf.equalsIgnoreCase("RightArc")) {
			conf=2;
		}
		else if(cf.equalsIgnoreCase("Reduce") || cf.equalsIgnoreCase("Swap")) {
			conf=3;
		}
		else {
			conf=4;
		}
	}
	
	public Configuration(State s, Sentence stn, String cf, String tg) {
		st=s;
		stc=stn;
		tag=tg;
		if(cf.equalsIgnoreCase("Shift")) {
			conf=0;
		}
		else if(cf.equalsIgnoreCase("LeftArc")) {
			conf=1;
		}
		else if(cf.equalsIgnoreCase("RightArc")) {
			conf=2;
		}
		else if(cf.equalsIgnoreCase("Reduce") || cf.equalsIgnoreCase("Swap")) {
			conf=3;
		}
		else {
			conf=4;
		}
	}
	
	public Feature buildFeature() {  //build feature with this configuration
		String b0f = st.getBuffer().isEmpty()?null:st.getBuffer().getFirst().getForm();
		String b0p = st.getBuffer().isEmpty()?null:st.getBuffer().getFirst().getPos();
		
		String s0f = st.getStack().isEmpty()?null:st.getStack().getLast().getForm();
		String s0p = st.getStack().isEmpty()?null:st.getStack().getLast().getPos();
		
		String b1f = st.getBuffer().size()>1?st.getBuffer().get(1).getForm():null;
		String b1p = st.getBuffer().size()>1?st.getBuffer().get(1).getPos():null;
		String s1p = st.getStack().size()>1?st.getStack().get(st.getStack().size()-2).getPos():null;
		
		String b2f = st.getBuffer().size()>2?st.getBuffer().get(2).getForm():null;
		String b2p = st.getBuffer().size()>2?st.getBuffer().get(2).getPos():null;
		String s2p = st.getStack().size()>2?st.getStack().get(st.getStack().size()-3).getPos():null;
		
		
		String ldb0p = st.getBuffer().isEmpty()?null:(st.getLeftMost()[st.getBuffer().getFirst().getID()]==-1?null:stc.getWdList().get(st.getLeftMost()[st.getBuffer().getFirst().getID()]).getPos());
		String rdb0p = st.getBuffer().isEmpty()?null:(st.getRightMost()[st.getBuffer().getFirst().getID()]==-1?null:stc.getWdList().get(st.getRightMost()[st.getBuffer().getFirst().getID()]).getPos());
		
		String lds0p = st.getStack().isEmpty()?null:(st.getLeftMost()[st.getStack().getLast().getID()]==-1?null:stc.getWdList().get(st.getLeftMost()[st.getStack().getLast().getID()]).getPos());
		String rds0p = st.getStack().isEmpty()?null:(st.getRightMost()[st.getStack().getLast().getID()]==-1?null:stc.getWdList().get(st.getRightMost()[st.getStack().getLast().getID()]).getPos());
		
		
		return new Feature(b0f, b0p, s0f, s0p, b1f, b1p, s1p, b2f, b2p, s2p, ldb0p, rdb0p, lds0p, rds0p, conf, tag);
	}
	
	public State getState() {
		return st;
	}

	public int getConf() {
		return conf;
	}

	public void setConf(int conf) {
		this.conf = conf;
	}
	
	public String getConfToString() {
		if(conf==0) 
			return "Shift";
		if(conf==1)
			return "LeftArc";
		if(conf==2)
			return "RightArc";
		if(conf==3)
			return "Reduce-Swap";
		if(conf==4)
			return "Unknown";
		return "Error";
	}
	
	public static String getConfToString(int cf) {
		if(cf==0) 
			return "Shift";
		if(cf==1)
			return "LeftArc";
		if(cf==2)
			return "RightArc";
		if(cf==3)
			return "Reduce-Swap";
		if(cf==4)
			return "Unknown";
		return "Error";
	}
	
	public static int getConfToInt(String cf) {
		if(cf.equalsIgnoreCase("Shift")) {
			return 0;
		}
		else if(cf.equalsIgnoreCase("LeftArc")) {
			return 1;
		}
		else if(cf.equalsIgnoreCase("RightArc")) {
			return 2;
		}
		else if(cf.equalsIgnoreCase("Reduce-Swap")) {
			return 3;
		}
		else {
			return 4;
		}
	}

	public Sentence getStc() {
		return stc;
	}

	public void setStc(Sentence stc) {
		this.stc = stc;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}
}
