import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

@SuppressWarnings("unused")
public class ArcEagerOnlineDecoder {
	public static int nLabel = 4;
	public static final String transition3rdName = "Reduce"; 
	public static final String transition4thName = "Unshift"; 
	private static int[] sentenceCount = new int[OnlinePerceptron.maxIter];
	private static boolean useUnshift = false;
	
	public static void buildConfiguration(Sentence st, OnlinePerceptron model) {
		buildConfiguration(st, model, 1);
	}
	
	public static void buildConfiguration(Sentence st, OnlinePerceptron model, int iterationNumber) {
		sentenceCount[iterationNumber-1]++;
		
		State s = new State(st);
		while(!(s.getBuffer().isEmpty())) {
			Configuration conf;
			int nCorrect = -1;
			int nPredict = -1;
			int[] nlPredict = model.findBestList(s.buildFeature(st));
			
			//legal check for predict
			//argmax(p : legal(s))
			for(int p : nlPredict) {
				if(p==Configuration.getConfToInt("LeftArc")) {
					if(legalLeftArc(s)) {
						nPredict=p;
						break;
					}
				}
				else if(p==Configuration.getConfToInt("RightArc")) {
					if(legalRightArc(s)) {
						nPredict=p;
						break;
					}
				}
				else if(p==Configuration.getConfToInt("Reduce")) {
					if(legalReduce(s)) {
						nPredict=p;
						break;
					}
				}
				else if(useUnshift && p==Configuration.getConfToInt("Unshift")) {
					if(legalUnshift(s)) {
						nPredict=p;
						break;
					}
				}
				else if(p==Configuration.getConfToInt("Shift")) {
					if(legalShift(s)) {
						nPredict=p;
						break;
					}
				}
			}
			
			//correct assignment, illegal cost = MAX_INT
			int[] nlCorrect = new int[nLabel];
			Arrays.fill(nlCorrect, -1);
			nlCorrect[Configuration.getConfToInt("LeftArc")]=costLeftArc(s);
			nlCorrect[Configuration.getConfToInt("RightArc")]=costRightArc(s);
			nlCorrect[Configuration.getConfToInt("Reduce")]=costReduce(s);
			if(useUnshift) {
				nlCorrect[Configuration.getConfToInt("Unshift")]=costUnshift(s);
			}
			nlCorrect[Configuration.getConfToInt("Shift")]=costShift(s);
			
//			if(ApplicationControl.OnlineStaticPerceptron) {
//				nlCorrect[Configuration.getConfToInt("LeftArc")]=canLeftArc(s)?0:Integer.MAX_VALUE;
//				nlCorrect[Configuration.getConfToInt("RightArc")]=canRightArc(s)?0:Integer.MAX_VALUE;
//				nlCorrect[Configuration.getConfToInt("Reduce")]=canReduce(s)?0:Integer.MAX_VALUE;
//				if(useUnshift) {
//					nlCorrect[Configuration.getConfToInt("Unshift")]=canUnshift(s)?0:Integer.MAX_VALUE;
//				}
//				nlCorrect[Configuration.getConfToInt("Shift")]=canShift(s)?0:Integer.MAX_VALUE;
//			}
			
			//argmax of correct assignments
			int argminCost = Integer.MAX_VALUE;
			int minCount = 0;
			for(int cost : nlCorrect) {
				if(cost<argminCost) {
					argminCost=cost;
					minCount=1;
				}
				else if(cost==argminCost) {
					minCount++;
				}
			}
			//prefer zero-cost also with zero monotonic cost
			if(ApplicationControl.NonMonotonic && argminCost==0 && minCount>1) {
				ApplicationControl.NonMonotonic = false;
				if(nlCorrect[Configuration.getConfToInt("LeftArc")] == 0)
					nlCorrect[Configuration.getConfToInt("LeftArc")]=costLeftArc(s);
				if(nlCorrect[Configuration.getConfToInt("RightArc")] == 0)
					nlCorrect[Configuration.getConfToInt("RightArc")]=costRightArc(s);
				if(nlCorrect[Configuration.getConfToInt("Reduce")] == 0)
					nlCorrect[Configuration.getConfToInt("Reduce")]=costReduce(s);
				if(useUnshift && nlCorrect[Configuration.getConfToInt("Unshift")] == 0) {
					nlCorrect[Configuration.getConfToInt("Unshift")]=costUnshift(s);
				}
				if(nlCorrect[Configuration.getConfToInt("Shift")] == 0)
					nlCorrect[Configuration.getConfToInt("Shift")]=costShift(s);
				
				ApplicationControl.NonMonotonic = true;
				
				argminCost = Integer.MAX_VALUE;
				for(int cost : nlCorrect) {
					if(cost<argminCost) {
						argminCost=cost;
					}
				}
				//no zero monotonic cost found, back to only non-monotonic cost
				if(argminCost!=0) {
					nlCorrect[Configuration.getConfToInt("LeftArc")]=costLeftArc(s);
					nlCorrect[Configuration.getConfToInt("RightArc")]=costRightArc(s);
					nlCorrect[Configuration.getConfToInt("Reduce")]=costReduce(s);
					if(useUnshift) {
						nlCorrect[Configuration.getConfToInt("Unshift")]=costUnshift(s);
					}
					nlCorrect[Configuration.getConfToInt("Shift")]=costShift(s);
					
					argminCost = Integer.MAX_VALUE;
					for(int cost : nlCorrect) {
						if(cost<argminCost) {
							argminCost=cost;
						}
					}
				}
			}
			
			for(int i=0;i<nlCorrect.length;i++) {
				if(nlCorrect[i]==argminCost && argminCost!=Integer.MAX_VALUE)
					nlCorrect[i]=i;
				else
					nlCorrect[i]=-1;
			}
			//argmax(p : correct(s))
			for(int p : nlPredict) {
				if(nlCorrect[p]!=-1) {
					nCorrect=p;
					break;
				}
			}
			//should not reach
			if(nCorrect==-1 || (ApplicationControl.OnlineStaticPerceptron && argminCost!=0)) {
				System.out.println("Cannot find correct transition! : do Shift!");
				nlCorrect[Configuration.getConfToInt("Shift")]=Configuration.getConfToInt("Shift");
				nCorrect=Configuration.getConfToInt("Shift");
			}
			
			//update()
//			if(nCorrect != nPredict) {
//				System.out.println("C: "+Configuration.getConfToString(nCorrect)+" P: "+Configuration.getConfToString(nPredict));
//				try {
//					System.out.println("update found in b:"+s.getBuffer().peekFirst().getForm()+ " s:"+s.getStack().peekLast().getForm());
//				} catch (NullPointerException e) {
//					System.out.println("update found! NullPointerException");
//				}
//			}
			
			model.inputFeature(s.buildFeature(st), nCorrect, nPredict, nlCorrect);
			
			//explore()
			int nNext = -1;
			nNext = model.explore(nCorrect, nPredict, iterationNumber);
			
//			if(nCorrect != nNext) {
//				System.out.println("C: "+Configuration.getConfToString(nCorrect)+" P: "+Configuration.getConfToString(nPredict));
//				try {
//					System.out.println("dynamic found in b:"+s.getBuffer().peekFirst().getForm()+ " s:"+s.getStack().peekLast().getForm());
//				} catch (NullPointerException e) {
//					System.out.println("dynamic found! NullPointerException");
//				}
//			}
			
			//perform transition
			if(nNext==Configuration.getConfToInt("LeftArc")) {
				//add configuration to list
				conf = (new Configuration(s.clone(),st,"LeftArc",s.getStack().peekLast().getRel()));
				
				//system-1, system-3-overwrite, system-4-overwrite
				//add information to state: heads, leftmost, rightmost
				makeArc(s, s.getBuffer().peekFirst().getID(), s.getStack().peekLast().getID());
				
				//do leftarc
				s.getStack().removeLast();
			}
			else if(nNext==Configuration.getConfToInt("RightArc")) {
				//add configuration to list
				conf = (new Configuration(s.clone(),st,"RightArc",s.getBuffer().peekFirst().getRel()));
				
				//add information to state: heads, leftmost, rightmost
				makeArc(s, s.getStack().peekLast().getID(), s.getBuffer().peekFirst().getID());
				
				//do rightarc
				s.getStack().add(s.getBuffer().removeFirst());
			}
			else if(nNext==Configuration.getConfToInt("Reduce")) {
				//add configuration to list
				conf = (new Configuration(s.clone(),st,"Reduce", null));
				//do reduce
				if(ApplicationControl.NonMonotonic && !useUnshift && s.getHeads()[s.getStack().peekLast().getID()]==-1) {
					//system-3-headless
					Word topWord = s.getStack().removeLast();
					makeArc(s, s.getStack().peekLast().getID(), topWord.getID());
				} 
				else {
					//system-1, system-3-other, system-4
					s.getStack().removeLast();
				}
			}
			else if(useUnshift && nNext==Configuration.getConfToInt("Unshift")) {
				//add configuration to list
				conf = (new Configuration(s.clone(),st,"Unshift", null));
				//do unshift
				s.getBuffer().add(s.getStack().removeLast());
			}
			else { //shift
				//add configuration to list
				conf = (new Configuration(s.clone(),st,"Shift", null));
				//do shift
				s.getStack().add(s.getBuffer().removeFirst());
				
				//system-4
				if(ApplicationControl.NonMonotonic && useUnshift)
					s.setUnshift(s.getStack().peekLast().getID());
			}
			
		}
		
		if(sentenceCount[iterationNumber-1]%1000==0)
			System.out.println("Iteration "+iterationNumber+": "+sentenceCount[iterationNumber-1]+" sentences processed");
	}
	
	public static void doParsing(OnlinePerceptron model, Sentence st) {
		//DEBUG: logging dead loop
//		int transitioncount=0;
//		ArrayList<String> transitionhistory = new ArrayList<String>();
		//END OF DEBUG
		
		State s = new State(st);
		while(!(s.getBuffer().isEmpty())) {
			//find best legal transition
			int[] bestTransList = model.findBestList(s.buildFeature(st));
			int bestTrans = -1;
			for(int b : bestTransList) {
				if(b==Configuration.getConfToInt("LeftArc")) {
					if(legalLeftArc(s)) {
						bestTrans=b;
						break;
					}
				}
				else if(b==Configuration.getConfToInt("RightArc")) {
					if(legalRightArc(s)) {
						bestTrans=b;
						break;
					}
				}
				else if(b==Configuration.getConfToInt("Reduce")) {
					if(legalReduce(s)) {
						bestTrans=b;
						break;
					}
				}
				else if(useUnshift && b==Configuration.getConfToInt("Unshift")) {
					if(legalUnshift(s)) {
						bestTrans=b;
						break;
					}
				}
				else if(b==Configuration.getConfToInt("Shift")) {
					if(legalShift(s)) {
						bestTrans=b;
						break;
					}
				}
			}
			
			
			if(bestTrans==0) {  //shift
				//do shift
				s.getStack().add(s.getBuffer().removeFirst());
				
				if(useUnshift)
					s.setUnshift(s.getStack().peekLast().getID());
			}
			else if(bestTrans==1) {  //leftArc
				//add information to state: heads, leftmost, rightmost
				makeArc(s, s.getBuffer().peekFirst().getID(), s.getStack().peekLast().getID());
				
				//write arc to sentence
				st.getWdList().get(s.getStack().peekLast().getID()).setHead(s.getBuffer().peekFirst().getID());
				//do leftarc
				s.getStack().removeLast();
			}
			else if(bestTrans==2) {  //rightArc
				//add information to state: heads, leftmost, rightmost
				makeArc(s, s.getStack().peekLast().getID(), s.getBuffer().peekFirst().getID());
			
				//write arc to sentence
				st.getWdList().get(s.getBuffer().peekFirst().getID()).setHead(s.getStack().peekLast().getID());
				//do rightarc
				s.getStack().add(s.getBuffer().removeFirst());
			}
			else if(bestTrans==3) {  //reduce
				//do reduce
				if(ApplicationControl.NonMonotonic && !useUnshift && s.getHeads()[s.getStack().peekLast().getID()]==-1) {
					//system-3-headless
					Word topWord = s.getStack().removeLast();
					
					makeArc(s, s.getStack().peekLast().getID(), topWord.getID());
					st.getWdList().get(topWord.getID()).setHead(s.getStack().peekLast().getID());
				} 
				else {
					//system-1, system-3-other, system-4
					s.getStack().removeLast();
				}
			}
			else if(useUnshift && bestTrans==4) {  //unshift
				//do unshift
				s.getBuffer().add(s.getStack().removeLast());
			}
			else {
				try {
					System.out.println("Error Transition with: stack-"+s.getStack().peekLast().getForm()+" buffer-"+s.getBuffer().peekFirst().getForm());
				} catch (NullPointerException e) {
					System.out.println("Error Transition with empty stack/buffer");
				}
			}
			
			//DEBUG: find dead loop in transitions
//			transitionhistory.add(Configuration.getConfToString(bestTrans));
//			transitioncount++;
//			if(transitioncount>10000) {
//				System.err.println("Dead loop parsing, skip!");
//				for(int i=9800;i<10000;i++) {
//					System.err.print(transitionhistory.get(i) +" -> ");
//				}
//				System.err.println("\n"+st.toString());
//				return;
//			}
			//END OF DEBUG
		}
		
		//try to solve no-head problem
		//0 - "Ignore", 1 - "All Root", 2 - "All RightArc", 3 - "All LeftArc", 4 - "By Oracle"
		if(ApplicationControl.AfterEndSolution==0) {  //Ignore
			//nothing to do
		}
		else if(ApplicationControl.AfterEndSolution==1) {  //All Root
			while(s.getStack().size()>1) {
				if(s.getHeads()[s.getStack().peekLast().getID()]==-1 && !s.getStack().peekLast().getPos().equals("ROOT")) {
					System.out.println("Final: head=ROOT");
					//add information to state: heads, leftmost, rightmost
					makeArc(s, 0, s.getStack().peekLast().getID());
				
					//write arc to sentence
					st.getWdList().get(s.getStack().peekLast().getID()).setHead(0);
				}
				else if(s.getStack().peekLast().getHead()!=-1) {
					s.getStack().removeLast();
				}
				else {
					break;
				}
			}
		}
		else if(ApplicationControl.AfterEndSolution==2) {  //All RightArc
			while(s.getStack().size()>1) {
				if(s.getHeads()[s.getStack().peekLast().getID()]==-1 && !s.getStack().peekLast().getPos().equals("ROOT")) {
					s.getBuffer().add(s.getStack().removeLast());
					System.out.println("Final: unShift -> RightArc (-> Reduce)");
					//add information to state: heads, leftmost, rightmost
					makeArc(s, s.getStack().peekLast().getID(), s.getBuffer().peekFirst().getID());
				
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
		else if(ApplicationControl.AfterEndSolution==3) {  //All LeftArc
			if(s.getStack().size()>1) {
				s.getBuffer().add(s.getStack().removeLast());
			}
			while(s.getStack().size()>1) {
				if(s.getHeads()[s.getStack().peekLast().getID()]==-1 && !s.getStack().peekLast().getPos().equals("ROOT")) {
					System.out.println("Final: unShift -> LeftArc");
					//add information to state: heads, leftmost, rightmost
					makeArc(s, s.getBuffer().peekFirst().getID(), s.getStack().peekLast().getID());
					
					//write arc to sentence
					st.getWdList().get(s.getStack().peekLast().getID()).setHead(s.getBuffer().peekFirst().getID());
					//do leftarc
					s.getBuffer().removeFirst();
					s.getBuffer().add(s.getStack().removeLast());
				}
				else if(s.getStack().peekLast().getHead()!=-1) {
					s.getBuffer().removeFirst();
					s.getBuffer().add(s.getStack().removeLast());
				}
				else {
					break;
				}
			}
			if(!s.getBuffer().isEmpty() && !s.getStack().isEmpty()) {
				if(s.getHeads()[s.getBuffer().peekFirst().getID()]==-1) {
					System.out.println("Final: [(unShift -> LeftArc)*] -> RightArc");
					//add information to state: heads, leftmost, rightmost
					makeArc(s, s.getStack().peekLast().getID(), s.getBuffer().peekFirst().getID());
				
					//write arc to sentence
					st.getWdList().get(s.getBuffer().peekFirst().getID()).setHead(s.getStack().peekLast().getID());
					//do rightarc
					s.getStack().add(s.getBuffer().removeFirst());
					//do reduce
					s.getStack().removeLast();
				}
			}
		}
		else if(ApplicationControl.AfterEndSolution==4) {  //By Oracle (system-2)
			//new: try to solve no-head problem with prediction
			while(s.getStack().size()>1) {
				
				int[] bestTransList = model.findBestList(s.buildFeature(st));
				int bestTrans = -1;
				for(int b : bestTransList) {
					if(b==Configuration.getConfToInt("LeftArc")) {
						if(legalFinalLeftArc(s)) {
							bestTrans=b;
							break;
						}
					}
					else if(b==Configuration.getConfToInt("RightArc")) {
						if(legalFinalRightArc(s)) {
							bestTrans=b;
							break;
						}
					}
					else if(b==Configuration.getConfToInt("Reduce")) {
						if(legalFinalReduce(s)) {
							bestTrans=b;
							break;
						}
					}
					else if(b==Configuration.getConfToInt("Shift")) {
						if(legalFinalShift(s)) {
							bestTrans=b;
							break;
						}
					}
				}
				if(bestTrans==-1) {
					if(legalFinalUnshift(s)) {
						bestTrans=Configuration.getConfToInt("Unshift");
					}
				}
				
//				System.out.println("Final Steps: "+Configuration.getConfToString(bestTrans));
				
				if(bestTrans==0) {  //shift
					//do shift
					s.getStack().add(s.getBuffer().removeFirst());
				}
				else if(bestTrans==1) {  //leftArc
					//add information to state: heads, leftmost, rightmost
					makeArc(s, s.getBuffer().peekFirst().getID(), s.getStack().peekLast().getID());
					
					//write arc to sentence
					st.getWdList().get(s.getStack().peekLast().getID()).setHead(s.getBuffer().peekFirst().getID());
					//do leftarc
					s.getStack().removeLast();
				}
				else if(bestTrans==2) {  //rightArc
					//add information to state: heads, leftmost, rightmost
					makeArc(s, s.getStack().peekLast().getID(), s.getBuffer().peekFirst().getID());
				
					//write arc to sentence
					st.getWdList().get(s.getBuffer().peekFirst().getID()).setHead(s.getStack().peekLast().getID());
					//do rightarc
					s.getStack().add(s.getBuffer().removeFirst());
				}
				else if(bestTrans==3) {  //reduce
					//do reduce
					s.getStack().removeLast();
				}
				else if(bestTrans==4) {  //unshift
					//do unshift
					s.getBuffer().add(s.getStack().removeLast());
				}
				else {
					try {
						System.out.println("Error Transition with: stack-"+s.getStack().peekLast().getForm()+" buffer-"+s.getBuffer().peekFirst().getForm());
					} catch (NullPointerException e) {
						System.out.println("Error Transition with empty stack/buffer");
					}
				}
			}
		}
		else {  //Unknown
			System.out.println("Unknown After-End Solution!");
		}
		
	}
	
	
	public static void resetCounter() {
		for(int i=0;i<sentenceCount.length;i++) {
			sentenceCount[i]=0;
		}
	}
	
	private static void makeArc(State s, int headID, int dependentID) {
		if(headID<0 || dependentID<0)
			return;
		
		s.getHeads()[dependentID]=headID;
		
		if(s.getHeads()[dependentID]!=-1) {
			int originalHeadID=s.getHeads()[dependentID];
			
			if(s.getLeftMost()[originalHeadID]==dependentID) {
				//find leftmost dep
				s.getLeftMost()[originalHeadID]=-1;
				for(int i=0;i<s.getHeads().length;i++) {
					if(s.getHeads()[i]==originalHeadID) {
						s.getLeftMost()[originalHeadID]=i;
						break;
					}
				}
			}
			if(s.getRightMost()[originalHeadID]==dependentID) {
				//find rightmost dep
				s.getRightMost()[originalHeadID]=-1;
				for(int i=s.getHeads().length-1;i>=0;i--) {
					if(s.getHeads()[i]==originalHeadID) {
						s.getRightMost()[originalHeadID]=i;
						break;
					}
				}
			}
		}
		
		if(s.getLeftMost()[headID]==-1 || s.getLeftMost()[headID]>dependentID)
			s.getLeftMost()[headID]=dependentID;
		if(s.getRightMost()[headID]==-1 || s.getRightMost()[headID]<dependentID)
			s.getRightMost()[headID]=dependentID;
		
	}
	
	//zero-cost check for each transition
	@Deprecated
	private static boolean canReduce(State s) {
		if(s.getStack().isEmpty())  //nothing to reduce
			return false;
		if(s.getHeads()[s.getStack().peekLast().getID()]!=-1){  //has head
			int count=0;
			boolean trueHeadInBuffer = false;
			for(Word w : s.getStack()) {
				if(w.getHead()==s.getStack().peekLast().getID()) {
					if(s.getHeads()[w.getID()]==-1)
						count++;
				}
			}
			for(Word w : s.getBuffer()) {
				if(w.getHead()==s.getStack().peekLast().getID()) {
					if(s.getHeads()[w.getID()]==-1)
						count++;
				}
				if(s.getStack().peekLast().getHead()==w.getID())
					trueHeadInBuffer=true;
			}
			if(count>0)  //not having all children
				return false;
			else { //having all children
				if(useUnshift && trueHeadInBuffer)
					return false;
				else
					return true;
			}
		}
		else
			return false;
	}

	@Deprecated
	private static boolean canLeftArc(State s) {
		if(s.getBuffer().isEmpty() || s.getStack().isEmpty())  //nothing to make arc
			return false;
		if(s.getStack().peekLast().getPos().equals("ROOT"))  //stack not root
			return false;
		if(s.getStack().peekLast().getHead()==s.getBuffer().peekFirst().getID()) {  //found the arc
			return true;
		}
		if(s.getHeads()[s.getStack().peekLast().getID()]!=-1)  //top of stack has head
			return false;
		else {  //has no head
			//true head of stack is in stack, non-optimal
			boolean tureHeadInStack=false;
			for(Word w : s.getStack()) {
				if(s.getStack().peekLast().getHead()==w.getID())
					tureHeadInStack=true;
			}
			if(useUnshift && tureHeadInStack)
				return false;
			
			//real head of stack not in buffer, no real child of stack in buffer, optimal
			boolean trueHeadInBuffer=false;
			boolean trueDependentInBuffer=false;
			for(Word w : s.getBuffer()) {
				if(s.getStack().peekLast().getHead()==w.getID())
					trueHeadInBuffer=true;
				if(w.getHead()==s.getStack().peekLast().getID())
					trueDependentInBuffer=true;
			}
			
			if(!trueHeadInBuffer && !trueDependentInBuffer)
				return true;
			else
				return false;
		}
	}
	
	@Deprecated
	private static boolean canRightArc(State s) {
		if(s.getBuffer().isEmpty() || s.getStack().isEmpty())  //nothing to makr arc
			return false;
		if(s.getHeads()[s.getBuffer().peekFirst().getID()]!=-1)  //front of buffer has head
			return false;
		if(s.getBuffer().peekFirst().getHead()==s.getStack().peekLast().getID()) {  //found the arc
			return true;
		}
		else {
			//real head of buffer not in stack/buffer, no real child of buffer in stack, optimal
			boolean trueHeadInStBu=false;
			boolean trueDependentInStack=false;
			for(Word w : s.getStack()) {
				if(s.getBuffer().peekFirst().getHead()==w.getID())
					trueHeadInStBu=true;
				if(w.getHead()==s.getBuffer().peekFirst().getID())
					trueDependentInStack=true;
			}
			for(Word w : s.getBuffer()) {
				if(s.getBuffer().peekFirst().getHead()==w.getID())
					trueHeadInStBu=true;
			}
			
			if(!trueHeadInStBu && !trueDependentInStack)
				return true;
			else
				return false;
		}
	}
	
	@Deprecated
	private static boolean canUnshift(State s) {
		if(!useUnshift)
			return false;
		if(s.getStack().isEmpty())  //nothing to unshift
			return false;
		if(s.getHeads()[s.getStack().peekLast().getID()]!=-1)  //top of stack has head
			return false;
		if(s.getUnshift(s.getStack().peekLast().getID()))
			return true;
		return false;
	}
	
	@Deprecated
	private static boolean canShift(State s) {
		if(s.getBuffer().isEmpty())  //nothing to shift
			return false;
		
		//no head of buffer in stack, no headless child of buffer in stack, optimal
		boolean headInStack=false;
		boolean childInStack=false;
		if(s.getBuffer().peekFirst().getHead()!=-1) {
			for(Word w : s.getStack()) {
				if(s.getBuffer().peekFirst().getHead()==w.getID())
					headInStack=true;
			}
		}
		for(Word w : s.getStack()) {
			if(w.getHead()==s.getBuffer().peekFirst().getID()) {
				if(s.getHeads()[w.getID()]==-1)
					childInStack=true;
			}
		}
		if(!headInStack && !childInStack)
			return true;
		else
			return false;
	}
	
	//cost function for each transition
		private static int costReduce(State s) {
			if(!legalReduce(s))
				return Integer.MAX_VALUE;
			
			int cost = 0;
			
			//system-1
			for(Word w : s.getBuffer()) {
				if(w.getHead()==s.getStack().peekLast().getID())
					cost++;
			}
			
			if(ApplicationControl.NonMonotonic) {
				//system-3, system-4
				//NM_LA
				for(Word w : s.getBuffer()) {
					if(s.getStack().peekLast().getHead()==w.getID()) {
						cost++;
						break;
					}
				}
				//NM_RE = 0
				//Unshift = 0
			}
						
			return cost;
		}

		private static int costLeftArc(State s) {
			if(!legalLeftArc(s))
				return Integer.MAX_VALUE;
			
			int cost = 0;
			
			//system-1
			for(Word w : s.getBuffer()) {
				if(w==s.getBuffer().peekFirst())
					continue;
				
				if(s.getStack().peekLast().getHead()==w.getID())
					cost++;
				if(w.getHead()==s.getStack().peekLast().getID())
					cost++;
			}
			
			if(ApplicationControl.NonMonotonic) {
				//system-3
				//NM_LA
				if(s.getStack().peekLast().getHead()==s.getHeads()[s.getStack().peekLast().getID()])
					cost++;
				if(s.getStack().peekLast().getHead()!=s.getBuffer().peekFirst().getID()) {
					for(Word w : s.getBuffer()) {
						if(s.getStack().peekLast().getHead()==w.getID()) {
							cost++;
							break;
						}
					}
				}
				if(!useUnshift) {
					//system-3
					//NM_RE
					if(s.getHeads()[s.getStack().peekLast().getID()]==-1) {
						Word stackTop = s.getStack().removeLast();
						if(stackTop.getHead()==s.getStack().peekLast().getID())
							cost++;
						s.getStack().addLast(stackTop);
					}
				}
				else {
					//system-4
					//Unshift
					for(Word w : s.getStack()) {
						if(s.getStack().peekLast().getHead()==w.getID())
							cost++;
						break;
					}
				}
			}

			return cost;
		}
		
		private static int costRightArc(State s) {
			if(!legalRightArc(s))
				return Integer.MAX_VALUE;
			
			int cost = 0;
			
			//system-1
			for(Word w : s.getStack()) {
				if(w==s.getStack().peekLast())
					continue;
				
				if(s.getBuffer().peekFirst().getHead()==w.getID())
					cost++;
				if(w.getHead()==s.getBuffer().peekFirst().getID() && s.getHeads()[w.getID()]==-1)
					cost++;
			}
			for(Word w : s.getBuffer()) {
				if(w==s.getBuffer().peekFirst()) 
					continue;
				
				if(s.getBuffer().peekFirst().getHead()==w.getID()) {
					cost++;
					break;
				}
			}
			
			if(ApplicationControl.NonMonotonic) {
				//system-3
				//NM_LA
				for(Word w : s.getBuffer()) {
					if(w==s.getBuffer().peekFirst()) 
						continue;
					
					if(s.getBuffer().peekFirst().getHead()==w.getID()) {
						cost--;
						break;
					}
				}
				for(Word w : s.getStack()) {
					if(w==s.getStack().peekLast())
						continue;
					
					if(s.getHeads()[w.getID()]!=-1 && w.getHead()==s.getBuffer().peekFirst().getID())
						cost++;
				}
				//NM_RE = 0
				
				//system-4 Unshift = 0
			}
			
			if(cost<0)
				cost=0;
			return cost;
		}
		
		private static int costUnshift(State s) {
			if(!legalUnshift(s))
				return Integer.MAX_VALUE;
			
			return costReduce(s);
		}
		
		private static int costShift(State s) {
			if(!legalShift(s))
				return Integer.MAX_VALUE;
			
			int cost = 0;
			
			//system-1
			for(Word w : s.getStack()) {
				if(s.getBuffer().peekFirst().getHead()==w.getID())
					cost++;
				
				if(w.getHead()==s.getBuffer().peekFirst().getID() && s.getHeads()[w.getID()]==-1)
					cost++;
			}
			
			if(ApplicationControl.NonMonotonic) {
				//system-3
				//NM_LA
				for(Word w : s.getStack()) {
					if(s.getHeads()[w.getID()]!=-1 && w.getHead()==s.getBuffer().peekFirst().getID())
						cost++;
				}
				//NM_RE
				if(s.getBuffer().peekFirst().getHead()==s.getStack().peekLast().getID())
					cost--;
			}
			
			//system-4 Unshift = 0
			
			if(cost<0)
				cost=0;
			return cost;
		}
	
	//legal check for each transitions
	private static boolean legalLeftArc(State s) {
		//system-1, system-3, system-4
		if(s.getBuffer().isEmpty() || s.getStack().size()<=1)
			return false;
		//system-1
		if(!ApplicationControl.NonMonotonic && s.getHeads()[s.getStack().peekLast().getID()]!=-1)
			return false;
		
		return true;
	}
	
	private static boolean legalFinalLeftArc(State s) {
		//system-2
		return legalLeftArc(s);
	}
	
	private static boolean legalRightArc(State s) {
		//system-1, system-3, system-4
		if(s.getBuffer().isEmpty() || s.getStack().isEmpty())
			return false;
		
		if(s.getHeads()[s.getBuffer().peekFirst().getID()]!=-1)
			return false;
		
		return true;
	}
	
	private static boolean legalFinalRightArc(State s) {
		//system-2
		return legalRightArc(s);
	}
	
	private static boolean legalReduce(State s) {
		//system-1, system-3
		if(s.getStack().size()<=1)
			return false;
		//system-1
		if(!ApplicationControl.NonMonotonic && s.getHeads()[s.getStack().peekLast().getID()]==-1)
			return false;
		
		//system-4
		if(ApplicationControl.NonMonotonic && useUnshift && s.getHeads()[s.getStack().peekLast().getID()]==-1)
			return false;
		
		return true;
	}
	
	private static boolean legalFinalReduce(State s) {
		//system-2
		return legalReduce(s);
	}
	
	private static boolean legalUnshift(State s) {
		if(!useUnshift)
			return false;
		
		//system-4
		if(s.getStack().size()<=1)
			return false;
		if(s.getHeads()[s.getStack().peekLast().getID()]!=-1)
			return false;
		
		return true;
	}
	
	private static boolean legalFinalUnshift(State s) {
		//system-2
		if(s.getStack().size()<=1)
			return false;
		if(!s.getBuffer().isEmpty())
			return false;
		if(s.getHeads()[s.getStack().peekLast().getID()]!=-1)
			return false;
		
		return true;
	}
	
	private static boolean legalShift(State s) {
		//system-1, system-3
		if(s.getBuffer().isEmpty())
			return false;
		
		//system-4
		if(ApplicationControl.NonMonotonic && useUnshift) {
			if(s.getUnshift(s.getBuffer().peekFirst().getID()))
				return false;
		}
		
		return true;
	}
	
	private static boolean legalFinalShift(State s) {
		//system-2
		return false;
	}
	
	public static void enableUnshift() {
		useUnshift=true;
		nLabel=5;
	}
	
	public static void disableUnshift() {
		useUnshift=false;
		nLabel=4;
	}
	
	public static void main(String[] args) {
		//test entry to arceagerdecoder
		//--calc configuration from a sample sentence (from slides)
		LinkedList<Configuration> cl = new LinkedList<Configuration>();
		DynamicPerceptron model = new DynamicPerceptron(nLabel);
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
		buildConfiguration(s, model);
		int co=1;
		for(Configuration cf : cl) {
			System.out.println("state#"+co+" s: "+(cf.getState().getStack().isEmpty()?"":cf.getState().getStack().peekLast().getForm())
					+" b: "+(cf.getState().getBuffer().isEmpty()?"":cf.getState().getBuffer().peekFirst().getForm())
					+" cf: "+cf.getConfToString());
			co++;
		}
	}

	
}
