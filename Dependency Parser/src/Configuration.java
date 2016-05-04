import java.util.Iterator;

public class Configuration {
	private State st;
	private Sentence stc;
	private int conf;
	private String tag;
	
	public Configuration(State s, String cf, String tg) {
		st=s;
		tag=tg;
		switch(cf.toLowerCase()) {
		case "shift":
			conf=0;
			break;
		case "leftarc":
			conf=1;
			break;
		case "rightarc":
			conf=2;
			break;
		case "reduce":
		case "swap":
			conf=3;
			break;
		case "unshift":
		case "nmreduce":
			conf=4;
			break;
		default:
			conf=5;
			break;
		}
	}
	
	public Configuration(State s, Sentence stn, String cf, String tg) {
		st=s;
		stc=stn;
		tag=tg;
		switch(cf.toLowerCase()) {
		case "shift":
			conf=0;
			break;
		case "leftarc":
			conf=1;
			break;
		case "rightarc":
			conf=2;
			break;
		case "reduce":
		case "swap":
			conf=3;
			break;
		case "unshift":
		case "nmreduce":
			conf=4;
			break;
		default:
			conf=5;
			break;
		}
	}
	
	public Feature buildFeature() {  //build feature with this configuration
		String b0f = st.getBuffer().isEmpty()?null:st.getBuffer().getFirst().getForm();
		String b0p = st.getBuffer().isEmpty()?null:st.getBuffer().getFirst().getPos();
		String b0m = st.getBuffer().isEmpty()?null:st.getBuffer().getFirst().getMorph();
		
		String s0f = st.getStack().isEmpty()?null:st.getStack().getLast().getForm();
		String s0p = st.getStack().isEmpty()?null:st.getStack().getLast().getPos();
		String s0m = st.getStack().isEmpty()?null:st.getStack().getLast().getMorph();
		
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
		
		int dist = st.getBuffer().isEmpty()?-1:(Math.abs(st.getStack().getLast().getID()-st.getBuffer().getFirst().getID()));
		String hds0f = st.getHeads()[st.getStack().getFirst().getID()]==-1?null:stc.getWdList().get(st.getHeads()[st.getStack().getFirst().getID()]).getPos();
		String hds0p = st.getHeads()[st.getStack().getFirst().getID()]==-1?null:stc.getWdList().get(st.getHeads()[st.getStack().getFirst().getID()]).getForm();
		String lds0f = st.getStack().isEmpty()?null:(st.getLeftMost()[st.getStack().getLast().getID()]==-1?null:stc.getWdList().get(st.getLeftMost()[st.getStack().getLast().getID()]).getForm());
		String rds0f = st.getStack().isEmpty()?null:(st.getRightMost()[st.getStack().getLast().getID()]==-1?null:stc.getWdList().get(st.getRightMost()[st.getStack().getLast().getID()]).getForm());
		String ldb0f = st.getBuffer().isEmpty()?null:(st.getLeftMost()[st.getBuffer().getFirst().getID()]==-1?null:stc.getWdList().get(st.getLeftMost()[st.getBuffer().getFirst().getID()]).getForm());
		String hd2s0f = st.getHeads()[st.getStack().getFirst().getID()]==-1?null:(st.getHeads()[st.getHeads()[st.getStack().getFirst().getID()]]==-1?null:stc.getWdList().get(st.getHeads()[st.getHeads()[st.getStack().getFirst().getID()]]).getForm());
		String hd2s0p = st.getHeads()[st.getStack().getFirst().getID()]==-1?null:(st.getHeads()[st.getHeads()[st.getStack().getFirst().getID()]]==-1?null:stc.getWdList().get(st.getHeads()[st.getHeads()[st.getStack().getFirst().getID()]]).getPos());
		
		return new Feature(b0f, b0p, s0f, s0p, b1f, b1p, s1p, b2f, b2p, s2p, ldb0p, rdb0p, lds0p, rds0p, dist, hds0f, hds0p, lds0f, rds0f, ldb0f, hd2s0f, hd2s0p, b0m, s0m, conf, tag);
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
		switch(conf) {
		case 0:
			return "Shift";
		case 1:
			return "LeftArc";
		case 2:
			return "RightArc";
		case 3:
			return "Reduce-Swap";
		case 4:
			return "Unshift";
		case 5:
			return "Unknown";
		default:
			return "Error";
		}
	}
	
	public static String getConfToString(int cf) {
		switch(cf) {
		case 0:
			return "Shift";
		case 1:
			return "LeftArc";
		case 2:
			return "RightArc";
		case 3:
			return "Reduce-Swap";
		case 4:
			return "Unshift";
		case 5:
			return "Unknown";
		default:
			return "Error";
		}
	}
	
	public static int getConfToInt(String cf) {
		switch(cf.toLowerCase()) {
		case "shift":
			return 0;
		case "leftarc":
			return 1;
		case "rightarc":
			return 2;
		case "reduce-swap":
		case "reduce":
		case "swap":
			return 3;
		case "unshift":
		case "nmreduce":
			return 4;
		default:
			return 5;
		}
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("conf: "+getConfToString());
		
		sb.append("\nstack: ");
		for(Iterator<Word>it=st.getStack().descendingIterator();it.hasNext();) {
			Word next = it.next();
			sb.append(next.toString().substring(0, next.toString().length()-1)+",s:"+st.getHeads()[next.getID()]+"] ");
		}
		sb.append("\nbuffer: ");
		for(Word w : st.getBuffer()) {
			sb.append(w.toString().substring(0, w.toString().length()-1)+",s:"+st.getHeads()[w.getID()]+"] ");
		}
		
		return sb.toString();
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
