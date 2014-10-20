import java.util.LinkedList;
import java.util.NoSuchElementException;


public class ArcStandardDecoder {
	
	public static final int nLabel = 4;
	private static int projectiveCount = 0;
	public static final String transition3rdName = "Swap"; 
	private static int nonProjectiveMemo = 0;
	
	private static final boolean SwapMPC = true;
	private static int swapCount = 0;
	
	public static void buildConfiguration(Sentence st,LinkedList<Configuration> cList) {
		preProcess(st);
		State s = new State(st);
		while(!(s.getBuffer().isEmpty() && s.getStack().size()==1)) {
		//while(!s.getBuffer().isEmpty()) {
			if(canLeftArc(s)) {
				//add configuration to list
				cList.add(new Configuration(s.clone(),st,"LeftArc",s.getStack().peekLast().getRel()));
				
				//add information to state: heads, leftmost, rightmost
				s.getHeads()[s.getStack().peekLast().getID()]=s.getBuffer().peekFirst().getID();
				if(s.getLeftMost()[s.getBuffer().peekFirst().getID()]==-1 
						|| s.getLeftMost()[s.getBuffer().peekFirst().getID()]>s.getStack().peekLast().getID())
					s.getLeftMost()[s.getBuffer().peekFirst().getID()]=s.getStack().peekLast().getID();
				if(s.getRightMost()[s.getBuffer().peekFirst().getID()]==-1 
						|| s.getRightMost()[s.getBuffer().peekFirst().getID()]<s.getStack().peekLast().getID())
					s.getRightMost()[s.getBuffer().peekFirst().getID()]=s.getStack().peekLast().getID();
				
				//do leftarc
				s.getStack().removeLast();
			}
			else if(canRightArc(s)) {
				//add configuration to list
				cList.add(new Configuration(s.clone(),st,"RightArc",s.getBuffer().peekFirst().getRel()));
				
				//add information to state: heads, leftmost, rightmost
				s.getHeads()[s.getBuffer().peekFirst().getID()]=s.getStack().peekLast().getID();
				if(s.getLeftMost()[s.getStack().peekLast().getID()]==-1 
						|| s.getLeftMost()[s.getStack().peekLast().getID()]>s.getBuffer().peekFirst().getID())
					s.getLeftMost()[s.getStack().peekLast().getID()]=s.getBuffer().peekFirst().getID();
				if(s.getRightMost()[s.getStack().peekLast().getID()]==-1 
						|| s.getRightMost()[s.getStack().peekLast().getID()]<s.getBuffer().peekFirst().getID())
					s.getRightMost()[s.getStack().peekLast().getID()]=s.getBuffer().peekFirst().getID();
				
				//do rightarc
				s.getBuffer().removeFirst();
				s.getBuffer().addFirst(s.getStack().removeLast());
			}
			else if(canSwap(s)) {
				//add configuration to list
				cList.add(new Configuration(s.clone(),st,"Swap",null));
				
				//do swap
				//s.getStack().add(s.getBuffer().removeFirst());
				//s.getBuffer().addFirst(s.getStack().remove(s.getStack().size()-2));
				//s.getBuffer().addFirst(s.getStack().removeLast());
				s.getBuffer().add(1, s.getStack().removeLast());
				swapCount++;
			}
			else {
				//add configuration to list
				cList.add(new Configuration(s.clone(),st,"Shift",null));
				//do shift
				try{
					s.getStack().add(s.getBuffer().removeFirst());
				}catch(NoSuchElementException e) {
					//to debug if buffer is empty
					System.out.println("------Shift Dead Loop!------");
					for(Word w : st.getWdList()) {
						System.out.println("Word#"+w.getID()+" form:"+w.getForm()+" head:"+w.getHead()+" projective:"+w.getProjectiveID());
					}
					System.out.println();
					int co=1;
					for(Configuration cf : cList) {
						System.out.println("state#"+co+" s: "+(cf.getState().getStack().isEmpty()?"":cf.getState().getStack().peekLast().getForm())
								+" b: "+(cf.getState().getBuffer().isEmpty()?"":cf.getState().getBuffer().peekFirst().getForm())
								+" cf: "+cf.getConfToString());
						co++;
					}
					break;
				}
			}
		}
	}
	
	public static void doParsing(Perceptron model, Sentence st) {
		State s = new State(st);
		LinkedList<DoubleWord> swapMemo = new LinkedList<DoubleWord>();
		while(!(s.getBuffer().isEmpty())) {
			int bestTrans = model.findBest(s.buildFeature(st));
			if(bestTrans==0) {  //shift
				if(!s.getStack().isEmpty() && s.getBuffer().size()==1 && s.getBuffer().peekFirst().getPos().equals("ROOT")) {
					System.out.println("Shift NoHead Fail! : do RightArc");
					//add information to state: heads, leftmost, rightmost
					s.getHeads()[s.getBuffer().peekFirst().getID()]=s.getStack().peekLast().getID();
					if(s.getLeftMost()[s.getStack().peekLast().getID()]==-1 
							|| s.getLeftMost()[s.getStack().peekLast().getID()]>s.getBuffer().peekFirst().getID())
						s.getLeftMost()[s.getStack().peekLast().getID()]=s.getBuffer().peekFirst().getID();
					if(s.getRightMost()[s.getStack().peekLast().getID()]==-1 
							|| s.getRightMost()[s.getStack().peekLast().getID()]<s.getBuffer().peekFirst().getID())
						s.getRightMost()[s.getStack().peekLast().getID()]=s.getBuffer().peekFirst().getID();
				
					//write arc to sentence
					st.getWdList().get(s.getBuffer().peekFirst().getID()).setHead(s.getStack().peekLast().getID());
					//do rightarc
					s.getBuffer().removeFirst();
					s.getBuffer().addFirst(s.getStack().removeLast());
				}	
				else if(!s.getBuffer().isEmpty()) {
					//do shift
					s.getStack().add(s.getBuffer().removeFirst());
				}
				else {
					System.out.println("Shift Fail!");
				}
			}
			else if(bestTrans==1) {  //leftArc
				if(!s.getBuffer().isEmpty() && !s.getStack().isEmpty()) {
					if(!s.getStack().peekLast().getPos().equals("ROOT")) {  //not making dep to root
						//add information to state: heads, leftmost, rightmost
						s.getHeads()[s.getStack().peekLast().getID()]=s.getBuffer().peekFirst().getID();
						if(s.getLeftMost()[s.getBuffer().peekFirst().getID()]==-1 
								|| s.getLeftMost()[s.getBuffer().peekFirst().getID()]>s.getStack().peekLast().getID())
							s.getLeftMost()[s.getBuffer().peekFirst().getID()]=s.getStack().peekLast().getID();
						if(s.getRightMost()[s.getBuffer().peekFirst().getID()]==-1 
								|| s.getRightMost()[s.getBuffer().peekFirst().getID()]<s.getStack().peekLast().getID())
							s.getRightMost()[s.getBuffer().peekFirst().getID()]=s.getStack().peekLast().getID();
						
						//write arc to sentence
						st.getWdList().get(s.getStack().peekLast().getID()).setHead(s.getBuffer().peekFirst().getID());
						//do leftarc
						s.getStack().removeLast();
					}
					else {
						System.out.println("LeftArc: Root");
					}
				}
				else {
					System.out.println("LeftArc Fail!");
				}
			}
			else if(bestTrans==2) {  //rightArc
				if(!s.getBuffer().isEmpty() && !s.getStack().isEmpty()) {
					//add information to state: heads, leftmost, rightmost
					s.getHeads()[s.getBuffer().peekFirst().getID()]=s.getStack().peekLast().getID();
					if(s.getLeftMost()[s.getStack().peekLast().getID()]==-1 
							|| s.getLeftMost()[s.getStack().peekLast().getID()]>s.getBuffer().peekFirst().getID())
						s.getLeftMost()[s.getStack().peekLast().getID()]=s.getBuffer().peekFirst().getID();
					if(s.getRightMost()[s.getStack().peekLast().getID()]==-1 
							|| s.getRightMost()[s.getStack().peekLast().getID()]<s.getBuffer().peekFirst().getID())
						s.getRightMost()[s.getStack().peekLast().getID()]=s.getBuffer().peekFirst().getID();
				
					//write arc to sentence
					st.getWdList().get(s.getBuffer().peekFirst().getID()).setHead(s.getStack().peekLast().getID());
					//do rightarc
					s.getBuffer().removeFirst();
					s.getBuffer().addFirst(s.getStack().removeLast());
				}
				else {
					System.out.println("RightArc Fail!");
				}
			}
			else if(bestTrans==3) {  //swap
				if(!s.getStack().isEmpty() && !swapMemo.contains(new DoubleWord(s.getStack().getLast(), s.getBuffer().getFirst()))) {
					//do swap
					//s.getStack().add(s.getBuffer().removeFirst());
					//s.getBuffer().addFirst(s.getStack().remove(s.getStack().size()-2));
					//s.getBuffer().addFirst(s.getStack().removeLast());
					s.getBuffer().add(1, s.getStack().removeLast());
					swapMemo.add(new DoubleWord(s.getBuffer().get(0), s.getBuffer().get(1)));
					swapMemo.add(new DoubleWord(s.getBuffer().get(1), s.getBuffer().get(0)));
				}
				else {
					System.out.println("Swap Fail! : do Shift");
					
					if(!s.getBuffer().isEmpty()) {
						//if fail, do shift
						s.getStack().add(s.getBuffer().removeFirst());
					}
					else {
						System.out.println("Swap-Shift Fail!");
					}
				}
			}
			else {
				System.out.println("Error Transition with: stack-"+s.getStack().peekLast().getForm()+" buffer-"+s.getBuffer().peekFirst().getForm());
			}
		}
	}
	
	public static void doParsing(LibClassifier model, Sentence st) {
		State s = new State(st);
		LinkedList<DoubleWord> swapMemo = new LinkedList<DoubleWord>();
		while(!(s.getBuffer().isEmpty())) {
			ArcTag bestTrans = model.findBest(s.buildFeature(st));
			if(bestTrans.getTransition()==0) {  //shift
				if(!s.getStack().isEmpty() && s.getBuffer().size()==1 && s.getBuffer().peekFirst().getPos().equals("ROOT")) {
					System.out.println("Shift NoHead Fail! : do RightArc");
					//add information to state: heads, leftmost, rightmost
					s.getHeads()[s.getBuffer().peekFirst().getID()]=s.getStack().peekLast().getID();
					if(s.getLeftMost()[s.getStack().peekLast().getID()]==-1 
							|| s.getLeftMost()[s.getStack().peekLast().getID()]>s.getBuffer().peekFirst().getID())
						s.getLeftMost()[s.getStack().peekLast().getID()]=s.getBuffer().peekFirst().getID();
					if(s.getRightMost()[s.getStack().peekLast().getID()]==-1 
							|| s.getRightMost()[s.getStack().peekLast().getID()]<s.getBuffer().peekFirst().getID())
						s.getRightMost()[s.getStack().peekLast().getID()]=s.getBuffer().peekFirst().getID();
				
					//write arc to sentence
					st.getWdList().get(s.getBuffer().peekFirst().getID()).setHead(s.getStack().peekLast().getID());
					//do rightarc
					s.getBuffer().removeFirst();
					s.getBuffer().addFirst(s.getStack().removeLast());
				}	
				else if(!s.getBuffer().isEmpty()) {
					//do shift
					s.getStack().add(s.getBuffer().removeFirst());
				}
				else {
					System.out.println("Shift Fail!");
				}
			}
			else if(bestTrans.getTransition()==1) {  //leftArc
				if(!s.getBuffer().isEmpty() && !s.getStack().isEmpty()) {
					if(!s.getStack().peekLast().getPos().equals("ROOT")) {  //not making dep to root
						//add information to state: heads, leftmost, rightmost
						s.getHeads()[s.getStack().peekLast().getID()]=s.getBuffer().peekFirst().getID();
						if(s.getLeftMost()[s.getBuffer().peekFirst().getID()]==-1 
								|| s.getLeftMost()[s.getBuffer().peekFirst().getID()]>s.getStack().peekLast().getID())
							s.getLeftMost()[s.getBuffer().peekFirst().getID()]=s.getStack().peekLast().getID();
						if(s.getRightMost()[s.getBuffer().peekFirst().getID()]==-1 
								|| s.getRightMost()[s.getBuffer().peekFirst().getID()]<s.getStack().peekLast().getID())
							s.getRightMost()[s.getBuffer().peekFirst().getID()]=s.getStack().peekLast().getID();
						
						//write arc to sentence
						st.getWdList().get(s.getStack().peekLast().getID()).setHead(s.getBuffer().peekFirst().getID());
						st.getWdList().get(s.getStack().peekLast().getID()).setRel(bestTrans.getTag());
						//do leftarc
						s.getStack().removeLast();
					}
					else {
						System.out.println("LeftArc: Root : do Shift");
						if(!s.getBuffer().isEmpty()) {
							//do shift
							s.getStack().add(s.getBuffer().removeFirst());
						}
						else {
							System.out.println("LeftArc.Root-Shift Fail!");
						}
					}
				}
				else {
					System.out.println("LeftArc Fail! : do Shift");
					if(!s.getBuffer().isEmpty()) {
						//do shift
						s.getStack().add(s.getBuffer().removeFirst());
					}
					else {
						System.out.println("LeftArc-Shift Fail!");
					}
				}
			}
			else if(bestTrans.getTransition()==2) {  //rightArc
				if(!s.getBuffer().isEmpty() && !s.getStack().isEmpty()) {
					//add information to state: heads, leftmost, rightmost
					s.getHeads()[s.getBuffer().peekFirst().getID()]=s.getStack().peekLast().getID();
					if(s.getLeftMost()[s.getStack().peekLast().getID()]==-1 
							|| s.getLeftMost()[s.getStack().peekLast().getID()]>s.getBuffer().peekFirst().getID())
						s.getLeftMost()[s.getStack().peekLast().getID()]=s.getBuffer().peekFirst().getID();
					if(s.getRightMost()[s.getStack().peekLast().getID()]==-1 
							|| s.getRightMost()[s.getStack().peekLast().getID()]<s.getBuffer().peekFirst().getID())
						s.getRightMost()[s.getStack().peekLast().getID()]=s.getBuffer().peekFirst().getID();
				
					//write arc to sentence
					st.getWdList().get(s.getBuffer().peekFirst().getID()).setHead(s.getStack().peekLast().getID());
					st.getWdList().get(s.getBuffer().peekFirst().getID()).setRel(bestTrans.getTag());
					//do rightarc
					s.getBuffer().removeFirst();
					s.getBuffer().addFirst(s.getStack().removeLast());
				}
				else {
					System.out.println("RightArc Fail! : do Shift");
					if(!s.getBuffer().isEmpty()) {
						//do shift
						s.getStack().add(s.getBuffer().removeFirst());
					}
					else {
						System.out.println("RightArc-Shift Fail!");
					}
				}
			}
			else if(bestTrans.getTransition()==3) {  //swap
				if(!s.getStack().isEmpty() && !swapMemo.contains(new DoubleWord(s.getStack().getLast(), s.getBuffer().getFirst()))) {
					//do swap
					//s.getStack().add(s.getBuffer().removeFirst());
					//s.getBuffer().addFirst(s.getStack().remove(s.getStack().size()-2));
					//s.getBuffer().addFirst(s.getStack().removeLast());
					s.getBuffer().add(1, s.getStack().removeLast());
					swapMemo.add(new DoubleWord(s.getBuffer().get(0), s.getBuffer().get(1)));
					swapMemo.add(new DoubleWord(s.getBuffer().get(1), s.getBuffer().get(0)));
				}
				else {
					System.out.println("Swap Fail! : do Shift");
					
					if(!s.getBuffer().isEmpty()) {
						//if fail, do shift
						s.getStack().add(s.getBuffer().removeFirst());
					}
					else {
						System.out.println("Swap-Shift Fail!");
					}
				}
			}
			else {
				System.out.println("Error Transition with: stack-"+s.getStack().peekLast().getForm()+" buffer-"+s.getBuffer().peekFirst().getForm());
			}
		}
	}

	private static boolean canLeftArc(State s) {
		if(s.getBuffer().isEmpty() || s.getStack().isEmpty())  //nothing to make arc
			return false;
		if(s.getStack().peekLast().getPos().equals("ROOT"))  //stack not root
			return false;
		if(s.getHeads()[s.getStack().peekLast().getID()]!=-1)  //top of stack has head
			return false;
		if(s.getStack().peekLast().getHead()==s.getBuffer().peekFirst().getID()) {  //found the arc
			int count=0;
			for(Word w : s.getStack()) {
				if(w.getHead()==s.getStack().peekLast().getID())
					count++;
			}
			for(Word w : s.getBuffer()) {
				if(w.getHead()==s.getStack().peekLast().getID())
					count++;
			}
			if(count>0)  //not having all children
				return false;
			else  //has all children
				return true;
		}
		else
			return false;
	}
	
	private static boolean canRightArc(State s) {
		if(s.getBuffer().isEmpty() || s.getStack().isEmpty())  //nothing to make arc
			return false;
		if(s.getHeads()[s.getBuffer().peekFirst().getID()]!=-1)  //front of buffer has head
			return false;
		if(s.getBuffer().peekFirst().getHead()==s.getStack().peekLast().getID()) {  //found the arc
			int count=0;
			for(Word w : s.getStack()) {
				if(w.getHead()==s.getBuffer().peekFirst().getID())
					count++;
			}
			for(Word w : s.getBuffer()) {
				if(w.getHead()==s.getBuffer().peekFirst().getID())
					count++;
			}
			if(count>0)  //not having all children
				return false;
			else  //has all children
				return true;
		}
		else
			return false;
	}
	
	private static boolean canSwap(State s) {
		if(SwapMPC)
			return canSwapMPC(s);
		
		if(s.getBuffer().isEmpty() || s.getStack().isEmpty())  //nothing to swap
			return false;
		if(s.getBuffer().peekFirst().getProjectiveID()<s.getStack().peekLast().getProjectiveID()) {  //if the stack top is after buffer first in projective
			return true;
		}
		return false;
	}
	
	private static boolean canSwapMPC(State s) {
		if(s.getBuffer().isEmpty() || s.getStack().isEmpty())  //nothing to swap
			return false;
		if(s.getBuffer().peekFirst().getProjectiveID()<s.getStack().peekLast().getProjectiveID()  //if the stack top is after buffer first in projective
				&& s.getBuffer().peekFirst().getMPCID()!=s.getStack().peekLast().getMPCID()) {  //and the MPCs are different
			return true;
		}
		return false;
	}
	
	private static void preProcess(Sentence st) {
		//analyze the projective tree
		for(Word w: st.getWdList()) {
			if(w.getHead()!=-1)
				st.getWdList().get(w.getHead()).addChildren(w.getID());
		}
		projectiveCount = 0;
		findProjective(0, st.getWdList());
		if(SwapMPC)
			findMPC(st);
		
		//checkProjective(st);
		checkProjectiveNoPrint(st);
	}
	
	private static void findProjective(int currentNode, LinkedList<Word> st) {
		//process left children (left arc)
		if(!st.get(currentNode).getChildren().isEmpty()) {
			for(Integer childNode : st.get(currentNode).getChildren()) {
				if(childNode<currentNode) {
					findProjective(childNode, st);
				}
				else {
					break;
				}
			}
		}
		
		//process itself
		st.get(currentNode).setProjectiveID(projectiveCount);
		projectiveCount++;
		
		//process right children (right arc)
		if(!st.get(currentNode).getChildren().isEmpty()) {
			for(Integer childNode : st.get(currentNode).getChildren()) {
				if(childNode>currentNode) {
					findProjective(childNode, st);
				}
			}
		}
	}
	
	@SuppressWarnings("unused")
	private static void checkProjective(Sentence st) {
		//check if the sentence is projective, count it & print the projective processed one if not
		boolean projective = true;
		for(Word w: st.getWdList()) {
			if(w.getID()!=w.getProjectiveID()) {
				projective = false;
			}
		}
		if(!projective) {
			nonProjectiveMemo++;
			
			for(Word w : st.getWdList()) {
				System.out.println("Word#"+w.getID()+" form:"+w.getForm()+" head:"+w.getHead()+" projective:"+w.getProjectiveID());
			}
			System.out.println();
		}
	}
	
	private static void checkProjectiveNoPrint(Sentence st) {
		//check if the sentence is projective, count it if not
		boolean projective = true;
		for(Word w: st.getWdList()) {
			if(w.getID()!=w.getProjectiveID()) {
				projective = false;
			}
		}
		if(!projective) {
			nonProjectiveMemo++;
		}
	}
	
	private static void findMPC(Sentence st) {
		State s = new State(st);
		int mpcCount=0;
		while(!s.getBuffer().isEmpty()) {
			if(canLeftArc(s)) {
				//add configuration to list
				s.getStack().peekLast().setMPChead(s.getBuffer().peekFirst().getID());
				
				//do leftarc
				s.getStack().removeLast();
			}
			else if(canRightArc(s)) {
				//add configuration to list
				s.getBuffer().peekFirst().setMPChead(s.getStack().peekLast().getID());
				
				//do rightarc
				s.getBuffer().removeFirst();
				s.getBuffer().addFirst(s.getStack().removeLast());
			}
			else {
				//add configuration to list
				
				//do shift
				s.getStack().add(s.getBuffer().removeFirst());	
			}
		}
		
		for(Word w : st.getWdList()) {
			if(w.getMPChead()==-1) {
				w.setMPCID(mpcCount);
				mpcCount++;
			}
		}
		for(Word w : st.getWdList()) {
			if(w.getMPChead()!=-1) {
				w.setMPCID(findMPCAncestor(st, w.getID()));
			}
		}
	}
	
	private static int findMPCAncestor(Sentence st, int current) {
		Word w = st.getWdList().get(current);
		if(w.getMPChead()==-1)
			return w.getMPCID();
		else
			return findMPCAncestor(st, w.getMPChead());
	}
	
	public static void resetMemo() {
		nonProjectiveMemo = 0;
	}
	
	public static int readMemo() {
		return nonProjectiveMemo;
	}
	
	public static int readSwco() {
		return swapCount;
	}
	
	public static void resetSwco() {
		swapCount = 0;
	}

	public static void main(String[] args) {
		//test entry to arcstandarddecoder
		//--calc configuration from a sample sentence (from slides)
		System.out.println("Sentence1: ");
		LinkedList<Configuration> cl = new LinkedList<Configuration>();
		LinkedList<Word> wl = new LinkedList<Word>();
		wl.add(new Word(1, "Not", "not", "---", 2));
		wl.add(new Word(2, "all", "all", "---", 6));
		wl.add(new Word(3, "those", "those", "---", 2));
		wl.add(new Word(4, "who", "who", "---", 5));
		wl.add(new Word(5, "wrote", "write", "---", 2));
		wl.add(new Word(6, "oppose", "oppose", "---", 0));
		wl.add(new Word(7, "the", "the", "---", 8));
		wl.add(new Word(8, "changes", "change", "---", 6));
		wl.add(new Word(9, ".", ".", "---", 6));
		Sentence s = new Sentence(wl);
		buildConfiguration(s,cl);
		
		for(Word w : s.getWdList()) {
			System.out.println("Word#"+w.getID()+" form:"+w.getForm()+" head:"+w.getHead()+" projective:"+w.getProjectiveID());
		}
		System.out.println();
		
		int co=1;
		for(Configuration cf : cl) {
			System.out.println("state#"+co+" s: "+(cf.getState().getStack().isEmpty()?"":cf.getState().getStack().peekLast().getForm())
					+" b: "+(cf.getState().getBuffer().isEmpty()?"":cf.getState().getBuffer().peekFirst().getForm())
					+" cf: "+cf.getConfToString());
			co++;
		}
		
		//--calc configuration from a non-projective sentence
		System.out.println("\n\nSentence2: ");
		
		LinkedList<Configuration> cl2 = new LinkedList<Configuration>();
		LinkedList<Word> wl2 = new LinkedList<Word>();
		wl2.add(new Word(1, "John", "john", "---", 2));
		wl2.add(new Word(2, "saw", "see", "---", 0));
		wl2.add(new Word(3, "a", "a", "---", 4));
		wl2.add(new Word(4, "dog", "dog", "---", 2));
		wl2.add(new Word(5, "yesterday", "yesterday", "---", 2));
		wl2.add(new Word(6, "which", "which", "---", 7));
		wl2.add(new Word(7, "was", "be", "---", 4));
		wl2.add(new Word(8, "a", "a", "---", 9));
		wl2.add(new Word(9, "Poddle", "poddle", "---", 7));
		s = new Sentence(wl2);
		buildConfiguration(s,cl2);
		
		for(Word w : s.getWdList()) {
			System.out.println("Word#"+w.getID()+" form:"+w.getForm()+" head:"+w.getHead()+" projective:"+w.getProjectiveID()+" MPC:"+w.getMPCID()+" MPChead:"+w.getMPChead());
		}
		System.out.println();
		
		co=1;
		for(Configuration cf : cl2) {
			System.out.println("state#"+co+" s: "+(cf.getState().getStack().isEmpty()?"":cf.getState().getStack().peekLast().getForm())
					+" b: "+(cf.getState().getBuffer().isEmpty()?"":cf.getState().getBuffer().peekFirst().getForm())
					+" cf: "+cf.getConfToString());
			co++;
		}
		System.out.println("swap count = "+readSwco());
		
		resetSwco();
		
		//--calc configuration from a non-projective sentence
		System.out.println("\n\nSentence3: ");
		
		LinkedList<Configuration> cl3 = new LinkedList<Configuration>();
		LinkedList<Word> wl3 = new LinkedList<Word>();
		wl3.add(new Word(1, "Who", "who", "---", 7));
		wl3.add(new Word(2, "did", "do", "---", 0));
		wl3.add(new Word(3, "you", "you", "---", 2));
		wl3.add(new Word(4, "send", "send", "---", 2));
		wl3.add(new Word(5, "the", "the", "---", 6));
		wl3.add(new Word(6, "letter", "letter", "---", 4));
		wl3.add(new Word(7, "to", "to", "---", 4));
		wl3.add(new Word(8, "?", "?", "---", 2));
		s = new Sentence(wl3);
		buildConfiguration(s,cl3);
				
		for(Word w : s.getWdList()) {
			System.out.println("Word#"+w.getID()+" form:"+w.getForm()+" head:"+w.getHead()+" projective:"+w.getProjectiveID()+" MPC:"+w.getMPCID()+" MPChead:"+w.getMPChead());
		}
		System.out.println();
				
		co=1;
		for(Configuration cf : cl3) {
			System.out.println("state#"+co+" s: "+(cf.getState().getStack().isEmpty()?"":cf.getState().getStack().peekLast().getForm())
					+" b: "+(cf.getState().getBuffer().isEmpty()?"":cf.getState().getBuffer().peekFirst().getForm())
					+" cf: "+cf.getConfToString());
			co++;
		} 
		System.out.println("swap count = "+readSwco());
		resetSwco();
	}
	
}
