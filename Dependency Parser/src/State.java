import java.util.Arrays;
import java.util.LinkedList;


public class State {
	private LinkedList<Word> buffer;
	private LinkedList<Word> stack;
	private int[] heads;
	private int[] leftMost;
	private int[] rightMost;
	private boolean[] unshift;
	
	private int size;
	//to save state in parsing process, one state per transition
	public State(Sentence s) {
		stack = new LinkedList<Word>();
		buffer = new LinkedList<Word>();
		size=s.getWdList().size();
		heads = new int[size];
		leftMost = new int[size];
		rightMost = new int[size];
		unshift = new boolean[size];
		
		Sentence ss = s.clone();
		
		stack.add(ss.getWdList().removeFirst());
		for(Word w : ss.getWdList()) {
			buffer.add(w);
		}
		
		for(int i=0;i<size;i++) {
			heads[i]=-1;
			leftMost[i]=-1;
			rightMost[i]=-1;
			unshift[i]=false;
		}
	}
	
	public State(LinkedList<Word> b, LinkedList<Word> s, int[] h, int[] l, int[] r) {
		buffer=b;
		stack=s;
		heads=h;
		leftMost=l;
		rightMost=r;
		size=h.length;
		unshift = new boolean[size];
		Arrays.fill(unshift, false);
	}
	
	public State(LinkedList<Word> b, LinkedList<Word> s, int[] h, int[] l, int[] r, boolean[] u) {
		buffer=b;
		stack=s;
		heads=h;
		leftMost=l;
		rightMost=r;
		unshift=u;
		size=h.length;
	}
	
	@SuppressWarnings("unchecked")
	public State clone() {
		return new State((LinkedList<Word>)buffer.clone(),(LinkedList<Word>)stack.clone(),heads.clone(),leftMost.clone(),rightMost.clone(),unshift.clone());
	}
	
	public Feature buildFeature(Sentence stc) {  //build feature with this state
		String b0f = buffer.isEmpty()?null:buffer.getFirst().getForm();
		String b0p = buffer.isEmpty()?null:buffer.getFirst().getPos();
		
		String s0f = stack.isEmpty()?null:stack.getLast().getForm();
		String s0p = stack.isEmpty()?null:stack.getLast().getPos();
		
		String b1f = buffer.size()>1?buffer.get(1).getForm():null;
		String b1p = buffer.size()>1?buffer.get(1).getPos():null;
		String s1p = stack.size()>1?stack.get(stack.size()-2).getPos():null;
		
		String b2f = buffer.size()>2?buffer.get(2).getForm():null;
		String b2p = buffer.size()>2?buffer.get(2).getPos():null;
		String s2p = stack.size()>2?stack.get(stack.size()-3).getPos():null;
		
		String ldb0p = buffer.isEmpty()?null:(leftMost[buffer.getFirst().getID()]==-1?null:stc.getWdList().get(leftMost[buffer.getFirst().getID()]).getPos());
		String rdb0p = buffer.isEmpty()?null:(rightMost[buffer.getFirst().getID()]==-1?null:stc.getWdList().get(rightMost[buffer.getFirst().getID()]).getPos());
		
		String lds0p = stack.isEmpty()?null:(leftMost[stack.getLast().getID()]==-1?null:stc.getWdList().get(leftMost[stack.getLast().getID()]).getPos());
		String rds0p = stack.isEmpty()?null:(rightMost[stack.getLast().getID()]==-1?null:stc.getWdList().get(rightMost[stack.getLast().getID()]).getPos());
		
		int dist = buffer.isEmpty()?-1:(Math.abs(stack.getLast().getID()-buffer.getFirst().getID()));
		String hds0f = heads[stack.getFirst().getID()]==-1?null:stc.getWdList().get(heads[stack.getFirst().getID()]).getPos();
		String hds0p = heads[stack.getFirst().getID()]==-1?null:stc.getWdList().get(heads[stack.getFirst().getID()]).getForm();
		String lds0f = stack.isEmpty()?null:(leftMost[stack.getLast().getID()]==-1?null:stc.getWdList().get(leftMost[stack.getLast().getID()]).getForm());
		String rds0f = stack.isEmpty()?null:(rightMost[stack.getLast().getID()]==-1?null:stc.getWdList().get(rightMost[stack.getLast().getID()]).getForm());
		String ldb0f = buffer.isEmpty()?null:(leftMost[buffer.getFirst().getID()]==-1?null:stc.getWdList().get(leftMost[buffer.getFirst().getID()]).getForm());
		String hd2s0f = heads[stack.getFirst().getID()]==-1?null:(heads[heads[stack.getFirst().getID()]]==-1?null:stc.getWdList().get(heads[heads[stack.getFirst().getID()]]).getForm());
		String hd2s0p = heads[stack.getFirst().getID()]==-1?null:(heads[heads[stack.getFirst().getID()]]==-1?null:stc.getWdList().get(heads[heads[stack.getFirst().getID()]]).getPos());
		
		return new Feature(b0f, b0p, s0f, s0p, b1f, b1p, s1p, b2f, b2p, s2p, ldb0p, rdb0p, lds0p, rds0p, dist, hds0f, hds0p, lds0f, rds0f, ldb0f, hd2s0f, hd2s0p, -1, null);
	}

	public LinkedList<Word> getBuffer() {
		return buffer;
	}

	public void setBuffer(LinkedList<Word> buffer) {
		this.buffer = buffer;
	}

	public LinkedList<Word> getStack() {
		return stack;
	}

	public void setStack(LinkedList<Word> stack) {
		this.stack = stack;
	}

	public int[] getLeftMost() {
		return leftMost;
	}

	public void setLeftMost(int[] leftMost) {
		this.leftMost = leftMost;
	}

	public int[] getRightMost() {
		return rightMost;
	}

	public void setRightMost(int[] rightMost) {
		this.rightMost = rightMost;
	}

	public int[] getHeads() {
		return heads;
	}

	public void setHeads(int[] heads) {
		this.heads = heads;
	}
	
	public void setUnshift(int id) {
		this.unshift[id]=true;
	}
	
	public boolean getUnshift(int id) {
		return this.unshift[id];
	}
}
