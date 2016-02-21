import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

@SuppressWarnings("unused")
public class ArcEagerOnlineDecoder {
	public static final int nLabel = 5;
	public static final String transition3rdName = "Reduce"; 
	public static final String transition4thName = "Unshift"; 
	public static int[] sentenceCount = new int[DynamicPerceptron.maxIter];
	
	public static void buildConfiguration(Sentence st, DynamicPerceptron model) {
		sentenceCount[0]++;
		State s = new State(st);
		while(!(s.getBuffer().isEmpty())) {
			Configuration conf;
			int nCorrect = -1;
			int nPredict = -1;
			int[] nlPredict = model.findBestList(s.buildFeature(st));
			
			//legal check for predict
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
				else if(p==Configuration.getConfToInt("Unshift")) {
					if(legalUnshift(s)) {
						nPredict=p;
						break;
					}
				}
				else if(p==Configuration.getConfToInt("Shift")) {
					nPredict=p;
					break;
				}
			}
			
			//correct assignment //TODO: need better cost function
			int[] nlCorrect = new int[nLabel];
			Arrays.fill(nlCorrect, -1);
			if(canLeftArc(s)) {
				nlCorrect[Configuration.getConfToInt("LeftArc")]=Configuration.getConfToInt("LeftArc");
			}
			if(canRightArc(s)) {
				nlCorrect[Configuration.getConfToInt("RightArc")]=Configuration.getConfToInt("RightArc");
			}
			if(canReduce(s)) {
				nlCorrect[Configuration.getConfToInt("Reduce")]=Configuration.getConfToInt("Reduce");
			}
			if(canUnshift(s)) {
				nlCorrect[Configuration.getConfToInt("Unshift")]=Configuration.getConfToInt("Unshift");
			}
			if(canShift(s)){
				nlCorrect[Configuration.getConfToInt("Shift")]=Configuration.getConfToInt("Shift");
			}
			
			//argmax of correct assignments
			for(int p : nlPredict) {
				if(nlCorrect[p]!=-1) {
					nCorrect=p;
					break;
				}
			}
			if(nCorrect==-1)
				nCorrect=Configuration.getConfToInt("Shift");
			
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
			nNext = model.explore(nCorrect, nPredict);
			
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
				s.setJustShift(false);
			}
			else if(nNext==Configuration.getConfToInt("RightArc")) {
				//add configuration to list
				conf = (new Configuration(s.clone(),st,"RightArc",s.getBuffer().peekFirst().getRel()));
				
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
				s.setJustShift(false);
			}
			else if(nNext==Configuration.getConfToInt("Reduce")) {
				//add configuration to list
				conf = (new Configuration(s.clone(),st,"Reduce", null));
				//do reduce
				s.getStack().removeLast();
				s.setJustShift(false);
			}
			else if(nNext==Configuration.getConfToInt("Unshift")) {
				//add configuration to list
				conf = (new Configuration(s.clone(),st,"Unshift", null));
				//do unshift
				s.getBuffer().add(s.getStack().removeLast());
				s.setJustShift(false);
			}
			else {
				//add configuration to list
				conf = (new Configuration(s.clone(),st,"Shift", null));
				//do shift
				s.getStack().add(s.getBuffer().removeFirst());
				s.setJustShift(true);
			}
			
		}
		
		if(sentenceCount[0]%1000==0)
			System.out.println("Iteration 1: "+sentenceCount[0]+" sentences processed");
	}
	
	public static void buildConfiguration(Sentence st, DynamicPerceptron model, int iterationNumber) {
		sentenceCount[iterationNumber-1]++;
		
		State s = new State(st);
		while(!(s.getBuffer().isEmpty())) {
			Configuration conf;
			int nCorrect = -1;
			int nPredict = -1;
			int[] nlPredict = model.findBestList(s.buildFeature(st));
			
			//legal check for predict
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
				else if(p==Configuration.getConfToInt("Unshift")) {
					if(legalUnshift(s)) {
						nPredict=p;
						break;
					}
				}
				else if(p==Configuration.getConfToInt("Shift")) {
					nPredict=p;
					break;
				}
			}
			
			//correct assignment //TODO: need better cost function
			int[] nlCorrect = new int[nLabel];
			Arrays.fill(nlCorrect, -1);
			if(canLeftArc(s)) {
				nlCorrect[Configuration.getConfToInt("LeftArc")]=Configuration.getConfToInt("LeftArc");
			}
			if(canRightArc(s)) {
				nlCorrect[Configuration.getConfToInt("RightArc")]=Configuration.getConfToInt("RightArc");
			}
			if(canReduce(s)) {
				nlCorrect[Configuration.getConfToInt("Reduce")]=Configuration.getConfToInt("Reduce");
			}
			if(canUnshift(s)) {
				nlCorrect[Configuration.getConfToInt("Unshift")]=Configuration.getConfToInt("Unshift");
			}
			if(canShift(s)){
				nlCorrect[Configuration.getConfToInt("Shift")]=Configuration.getConfToInt("Shift");
			}
			
			//argmax of correct assignments
			for(int p : nlPredict) {
				if(nlCorrect[p]!=-1) {
					nCorrect=p;
					break;
				}
			}
			if(nCorrect==-1)
				nCorrect=Configuration.getConfToInt("Shift");
			
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
				s.setJustShift(false);
			}
			else if(nNext==Configuration.getConfToInt("RightArc")) {
				//add configuration to list
				conf = (new Configuration(s.clone(),st,"RightArc",s.getBuffer().peekFirst().getRel()));
				
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
				s.setJustShift(false);
			}
			else if(nNext==Configuration.getConfToInt("Reduce")) {
				//add configuration to list
				conf = (new Configuration(s.clone(),st,"Reduce", null));
				//do reduce
				s.getStack().removeLast();
				s.setJustShift(false);
			}
			else if(nNext==Configuration.getConfToInt("Unshift")) {
				//add configuration to list
				conf = (new Configuration(s.clone(),st,"Unshift", null));
				//do unshift
				s.getBuffer().add(s.getStack().removeLast());
				s.setJustShift(false);
			}
			else {
				//add configuration to list
				conf = (new Configuration(s.clone(),st,"Shift", null));
				//do shift
				s.getStack().add(s.getBuffer().removeFirst());
				s.setJustShift(true);
			}
			
		}
		
		if(sentenceCount[iterationNumber-1]%1000==0)
			System.out.println("Iteration "+iterationNumber+": "+sentenceCount[iterationNumber-1]+" sentences processed");
	}
	
	public static void doParsing(DynamicPerceptron model, Sentence st) {
		//DEBUG: logging dead loop
//		int transitioncount=0;
//		ArrayList<String> transitionhistory = new ArrayList<String>();
		//END OF DEBUG
		
		State s = new State(st);
		while(!(s.getBuffer().isEmpty())) {
			//find best legal transition
			int[] bestTransList = model.findBestList(s.buildFeature(st));
			int bestTrans = Configuration.getConfToInt("Shift");
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
				else if(b==Configuration.getConfToInt("Unshift")) {
					if(legalUnshift(s)) {
						bestTrans=b;
						break;
					}
				}
				else if(b==Configuration.getConfToInt("Shift")) {
					bestTrans=b;
					break;
				}
			}
			
			
			if(bestTrans==0) {  //shift
				if(!s.getBuffer().isEmpty()) {
					//do shift
					s.getStack().add(s.getBuffer().removeFirst());
					s.setJustShift(true);
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
							s.setJustShift(false);
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
					if(!s.getBuffer().peekFirst().getPos().equals("ROOT")) {  //not making dep to root
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
						s.setJustShift(false);
					}
				}
				else {
					System.out.println("RightArc Fail!");
				}
			}
			else if(bestTrans==3) {  //reduce
				if(!s.getStack().isEmpty()) {
					//do reduce
					s.getStack().removeLast();
					s.setJustShift(false);
				}
				else {
					System.out.println("Reduce Fail! : do Shift");
					
					if(!s.getBuffer().isEmpty()) {
						//if fail, do shift
						s.getStack().add(s.getBuffer().removeFirst());
						s.setJustShift(true);
					}
					else {
						System.out.println("Reduce-Shift Fail!");
					}
				}
			}
			else if(bestTrans==4) {  //unshift
				if(!s.getStack().isEmpty()) {
					if(!s.getJustShift()) {
						//do unshift
						s.getBuffer().add(s.getStack().removeLast());
						s.setJustShift(false);
					}
					else {
						System.out.println("Unshift Fail! : do Reduce");
						//if fail, do reduce
						s.getStack().removeLast();
						s.setJustShift(false);
					}
				}
				else {
					System.out.println("Unshift-Reduce Fail! : do Shift");
					
					if(!s.getBuffer().isEmpty()) {
						//if fail, do shift
						s.getStack().add(s.getBuffer().removeFirst());
						s.setJustShift(true);
					}
					else {
						System.out.println("Unshift-Reduce-Shift Fail!");
					}
				}
			}
			else {
				System.out.println("Error Transition with: stack-"+s.getStack().peekLast().getForm()+" buffer-"+s.getBuffer().peekFirst().getForm());
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
//		while(s.getStack().size()>1) {
//			if(s.getStack().peekLast().getHead()==-1 && !s.getStack().peekLast().getPos().equals("ROOT")) {
//				s.getBuffer().add(s.getStack().removeLast());
//				System.out.println("Final: unShift -> RightArc (-> Reduce)");
//				//add information to state: heads, leftmost, rightmost
//				s.getHeads()[s.getBuffer().peekFirst().getID()]=s.getStack().peekLast().getID();
//				if(s.getLeftMost()[s.getStack().peekLast().getID()]==-1 
//						|| s.getLeftMost()[s.getStack().peekLast().getID()]>s.getBuffer().peekFirst().getID())
//					s.getLeftMost()[s.getStack().peekLast().getID()]=s.getBuffer().peekFirst().getID();
//				if(s.getRightMost()[s.getStack().peekLast().getID()]==-1 
//						|| s.getRightMost()[s.getStack().peekLast().getID()]<s.getBuffer().peekFirst().getID())
//					s.getRightMost()[s.getStack().peekLast().getID()]=s.getBuffer().peekFirst().getID();
//			
//				//write arc to sentence
//				st.getWdList().get(s.getBuffer().peekFirst().getID()).setHead(s.getStack().peekLast().getID());
//				//do rightarc
//				s.getStack().add(s.getBuffer().removeFirst());
//			}
//			else if(s.getStack().peekLast().getHead()!=-1) {
//				s.getStack().removeLast();
//			}
//			else {
//				break;
//			}
//		}
		
		//new: try to solve no-head problem with prediction
		s.setJustShift(false);
//		int finalcount=0;
		while(s.getStack().size()>1) {
			
//			finalcount++;
//			if(finalcount>10000)
//				break;
			
			int[] bestTransList = model.findBestList(s.buildFeature(st));
			int bestTrans = Configuration.getConfToInt("Reduce");
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
					if(legalFinalReduce(s)) {
						bestTrans=b;
						break;
					}
				}
				else if(b==Configuration.getConfToInt("Unshift")) {
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
			
//			System.out.println("Final Steps: "+Configuration.getConfToString(bestTrans));
			
			if(bestTrans==0) {  //shift
				//do shift
				s.getStack().add(s.getBuffer().removeFirst());
				s.setJustShift(true);
			}
			else if(bestTrans==1) {  //leftArc
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
				s.setJustShift(false);
			}
			else if(bestTrans==2) {  //rightArc
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
				s.setJustShift(false);
			}
			else if(bestTrans==3) {  //reduce
				//do reduce
				s.getStack().removeLast();
				s.setJustShift(false);
			}
			else if(bestTrans==4) {  //unshift
				//do unshift
				s.getBuffer().add(s.getStack().removeLast());
				s.setJustShift(false);
			}
			else {
				System.out.println("Error Transition with: stack-"+s.getStack().peekLast().getForm()+" buffer-"+s.getBuffer().peekFirst().getForm());
			}
		}
	}
	
	
	public static void resetCounter() {
		for(int i=0;i<sentenceCount.length;i++) {
			sentenceCount[i]=0;
		}
	}
	

	private static boolean canReduce(State s) {
		if(s.getStack().isEmpty())  //nothing to reduce
			return false;
		if(s.getHeads()[s.getStack().peekLast().getID()]!=-1){  //has head
			int count=0;
			boolean trueHeadInBuffer = false;
			for(Word w : s.getStack()) {
				if(w.getHead()==s.getStack().peekLast().getID())
					count++;
			}
			for(Word w : s.getBuffer()) {
				if(w.getHead()==s.getStack().peekLast().getID())
					count++;
				if(s.getStack().peekLast().getHead()==w.getID())
					trueHeadInBuffer=true;
			}
			if(count>0)  //not having all children
				return false;
			else { //having all children
				if(trueHeadInBuffer)
					return false;
				else
					return true;
			}
		}
		else
			return false;
	}

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
			if(tureHeadInStack)
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
	
	private static boolean canUnshift(State s) {
		if(s.getStack().isEmpty())  //nothing to unshift
			return false;
		if(s.getHeads()[s.getStack().peekLast().getID()]!=-1)  //top of stack has head
			return false;
		if(!s.getJustShift())
			return true;
		return false;
	}
	
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
		for(Word w : s.getBuffer()) {
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
	
	private static boolean legalLeftArc(State s) {
		if(s.getBuffer().isEmpty() || s.getStack().isEmpty())
			return false;
		if(s.getStack().peekLast().getPos().equals("ROOT"))
			return false;
		if(s.getHeads()[s.getStack().peekLast().getID()]==s.getBuffer().peekFirst().getID())
			return false;
		if(s.getHeads()[s.getBuffer().peekFirst().getID()]==s.getStack().peekLast().getID())
			return false;
		if(s.getHeads()[s.getStack().peekLast().getID()]!=-1)
			return false;
		return true;
	}
	
	private static boolean legalRightArc(State s) {
		if(s.getBuffer().isEmpty() || s.getStack().isEmpty())
			return false;
		if(s.getBuffer().peekFirst().getPos().equals("ROOT"))
			return false;
		if(s.getHeads()[s.getBuffer().peekFirst().getID()]==s.getStack().peekLast().getID())
			return false;
		if(s.getHeads()[s.getStack().peekLast().getID()]==s.getBuffer().peekFirst().getID())
			return false;
		if(s.getHeads()[s.getBuffer().peekFirst().getID()]!=-1)
			return false;
		return true;
	}
	
	private static boolean legalReduce(State s) {
		if(s.getStack().isEmpty())
			return false;
		return true;
	}
	
	private static boolean legalFinalReduce(State s) {
		if(s.getStack().isEmpty())
			return false;
		if(s.getHeads()[s.getStack().peekLast().getID()]==-1)
			return false;
		return true;
	}
	
	private static boolean legalUnshift(State s) {
		if(s.getStack().isEmpty())
			return false;
		if(s.getJustShift())
			return false;
		return true;
	}
	
	private static boolean legalShift(State s) {
		if(s.getBuffer().isEmpty())
			return false;
		return true;
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
