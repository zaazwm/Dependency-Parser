import java.util.LinkedList;


public class ArcEagerDecoder {
	public static final int nLabel = 4;
	public static final String transition3rdName = "Reduce"; 
	
	public static void buildConfiguration(Sentence st,LinkedList<Configuration> cList) {
		State s = new State(st);
		while(!(s.getBuffer().isEmpty())) {
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
				s.getStack().add(s.getBuffer().removeFirst());
			}
			else if(canReduce(s)) {
				//add configuration to list
				cList.add(new Configuration(s.clone(),st,"Reduce", null));
				//do reduce
				s.getStack().removeLast();
			}
			else {
				//add configuration to list
				cList.add(new Configuration(s.clone(),st,"Shift", null));
				//do shift
				s.getStack().add(s.getBuffer().removeFirst());
			}
		}
	}
	
	public static void doParsing(Perceptron model, Sentence st) {
		State s = new State(st);
		while(!(s.getBuffer().isEmpty())) {
			int bestTrans = model.findBest(s.buildFeature(st));
			if(bestTrans==0) {  //shift
				if(!s.getBuffer().isEmpty()) {
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
					s.getStack().add(s.getBuffer().removeFirst());
				}
				else {
					System.out.println("RightArc Fail!");
				}
			}
			else if(bestTrans==3) {  //reduce
				if(!s.getStack().isEmpty()) {
					//do reduce
					s.getStack().removeLast();
				}
				else {
					System.out.println("Reduce Fail! : do Shift");
					
					if(!s.getBuffer().isEmpty()) {
						//if fail, do shift
						s.getStack().add(s.getBuffer().removeFirst());
					}
					else {
						System.out.println("Reduce-Shift Fail!");
					}
				}
			}
			else {
				System.out.println("Error Transition with: stack-"+s.getStack().peekLast().getForm()+" buffer-"+s.getBuffer().peekFirst().getForm());
			}
		}
		//try to solve no-head problem
		while(s.getStack().size()>1) {
			if(s.getStack().peekLast().getHead()==-1 && !s.getStack().peekLast().getPos().equals("ROOT")) {
				s.getBuffer().add(s.getStack().removeLast());
				System.out.println("Final: unShift -> RightArc (-> Reduce)");
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
				s.getStack().add(s.getBuffer().removeFirst());
			}
			else if(s.getStack().peekLast().getHead()!=-1) {
				s.getStack().removeLast();
			}
			else {
				break;
			}
		}
	}
	
	public static void doParsing(LibClassifier model, Sentence st) {
		State s = new State(st);
		while(!(s.getBuffer().isEmpty())) {
			ArcTag bestTrans = model.findBest(s.buildFeature(st));
			if(bestTrans.getTransition()==0) {  //shift
				if(!s.getBuffer().isEmpty()) {
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
					s.getStack().add(s.getBuffer().removeFirst());
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
			else if(bestTrans.getTransition()==3) {  //reduce
				if(!s.getStack().isEmpty()) {
					//do reduce
					s.getStack().removeLast();
				}
				else {
					System.out.println("Reduce Fail! : do Shift");
					
					if(!s.getBuffer().isEmpty()) {
						//if fail, do shift
						s.getStack().add(s.getBuffer().removeFirst());
					}
					else {
						System.out.println("Reduce-Shift Fail!");
					}
				}
			}
			else {
				System.out.println("Error Transition with: stack-"+s.getStack().peekLast().getForm()+" buffer-"+s.getBuffer().peekFirst().getForm());
			}
		}
		//try to solve no-head problem
		while(s.getStack().size()>1) {
			if(s.getStack().peekLast().getHead()==-1 && !s.getStack().peekLast().getPos().equals("ROOT")) {
				s.getBuffer().add(s.getStack().removeLast());
				System.out.println("Final: unShift -> RightArc (-> Reduce)");
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
				s.getStack().add(s.getBuffer().removeFirst());
			}
			else if(s.getStack().peekLast().getHead()!=-1) {
				s.getStack().removeLast();
			}
			else {
				break;
			}
		}
	}

	private static boolean canReduce(State s) {
		if(s.getStack().isEmpty())  //nothing to reduce
			return false;
		if(s.getHeads()[s.getStack().peekLast().getID()]!=-1){  //has head
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
			else  //having all children
				return true;
		}
		else
			return false;
	}

	private static boolean canLeftArc(State s) {
		if(s.getBuffer().isEmpty() || s.getStack().isEmpty())  //nothing to make arc
			return false;
		if(s.getStack().peekLast().getPos().equals("ROOT"))  //stack not root
			return false;
		if(s.getHeads()[s.getStack().peekLast().getID()]!=-1)  //top of stack has head
			return false;
		if(s.getStack().peekLast().getHead()==s.getBuffer().peekFirst().getID()) {  //found the arc
			return true;
		}
		else
			return false;
	}
	
	private static boolean canRightArc(State s) {
		if(s.getBuffer().isEmpty() || s.getStack().isEmpty())  //nothing to makr arc
			return false;
		if(s.getHeads()[s.getBuffer().peekFirst().getID()]!=-1)  //front of buffer has head
			return false;
		if(s.getBuffer().peekFirst().getHead()==s.getStack().peekLast().getID()) {  //found the arc
			return true;
		}
		else
			return false;
	}
	
	public static void main(String[] args) {
		//test entry to arceagerdecoder
		//--calc configuration from a sample sentence (from slides)
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
		int co=1;
		for(Configuration cf : cl) {
			System.out.println("state#"+co+" s: "+(cf.getState().getStack().isEmpty()?"":cf.getState().getStack().peekLast().getForm())
					+" b: "+(cf.getState().getBuffer().isEmpty()?"":cf.getState().getBuffer().peekFirst().getForm())
					+" cf: "+cf.getConfToString());
			co++;
		}
	}
}
