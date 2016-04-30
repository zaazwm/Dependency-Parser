import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

public class ArcEagerOnlineDecoder {
	public static int nLabel = 4;
	public static final String transition3rdName = "Reduce"; 
	public static final String transition4thName = "Unshift-NMReduce"; 
	private static int[] sentenceCount = new int[OnlinePerceptron.maxIter];
	private static boolean useUnshift = false;
	private static boolean useNMReduce = false;
	
	//toPrint transition-details
	private static Random rnd = new Random();
	private static int[] printConfCount = new int[OnlinePerceptron.maxIter];
	private static final int maxPrintPerIter = 0;
	private static final double probPrintPerConf = 0.0001D;
	private static final int printBeginIter = 1;
	private static final int printEndIter = 15;
	
	private static HashMap<String, Integer> relCountMap = new HashMap<String, Integer>();
	private static int automataState = 0;
	
//	public static int[] specialcounter = new int[4];
	
	//toPrint analysis-counts
	private static int[][] printTransitionAnalysis = new int[OnlinePerceptron.maxIter+1][8];
	//0:#LeftArc(headless), 1:#LeftArc(overwrite) 2:#RightArc(S(B)=0), 3:#RightArc(S(b)=1), 4:#Shift, 5:#Reduce, 6:#Unshift, 7:#2N
	
	public static void buildConfiguration(Sentence st, OnlinePerceptron model) {
		buildConfiguration(st, model, 1);
	}
	
	public static void buildConfiguration(Sentence st, OnlinePerceptron model, int iterationNumber) {
		sentenceCount[iterationNumber-1]++;
		printTransitionAnalysis[iterationNumber-1][7]+=2*(st.getWdList().size()-1);
		automataState=0;
				
		State s = new State(st);
		while(true) {
			Configuration conf = null;
			int nCorrect = -1;
			int nPredict = -1;
			int[] nlPredict = model.findBestList(s.buildFeature(st));
			
			boolean toPrint=false;
			if(iterationNumber>=printBeginIter && iterationNumber<=printEndIter
					&& printConfCount[iterationNumber-1]<maxPrintPerIter && rnd.nextDouble()<probPrintPerConf) {
				printConfCount[iterationNumber-1]++;
				toPrint=true;
			}
			
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
					boolean legalUnshift = false;
					switch(ApplicationControl.UnshiftCostSwitch) {
					case 0:
					case 1:
					case 2:
					case 6:
						//same class
						break;
					case 3:
					case 4:
					case 5:
					case 7:
						//single class
						if(legalUnshift(s)) {
							legalUnshift=true;
						}
						break;
					default:
						break;
					}
					if(legalUnshift) {
						nPredict=p;
						break;
					}
				}
				else if(useNMReduce && p==Configuration.getConfToInt("NMReduce")) {
					if(legalNMReduce(s)) {
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
			
			//terminal when no legal transition
			if(nPredict==-1)
				break;
			
			//correct assignment, illegal cost = MAX_INT
			int[] nlCorrect = new int[nLabel];
			Arrays.fill(nlCorrect, -1);
			nlCorrect[Configuration.getConfToInt("LeftArc")]=costLeftArc(s);
			nlCorrect[Configuration.getConfToInt("RightArc")]=costRightArc(s);
			nlCorrect[Configuration.getConfToInt("Reduce")]=costReduce(s);
			if(useUnshift) {
				switch(ApplicationControl.UnshiftCostSwitch) {
				case 0:
				case 1:
				case 2:
				case 6:
					//same class
					break;
				case 3:
				case 4:
				case 5:
				case 7:
					nlCorrect[Configuration.getConfToInt("Unshift")]=costUnshift(s);
					break;
				default:
					break;
				}
			}
			if(useNMReduce) {
				nlCorrect[Configuration.getConfToInt("NMReduce")]=costNMReduce(s);
			}
			nlCorrect[Configuration.getConfToInt("Shift")]=costShift(s);
			
			//DEBUG: find some example for cost of unshift
//			if(nlCorrect[Configuration.getConfToInt("Unshift")]==1) {
//				int countzerocost=0;
//				for(int nlii=0;nlii<nlCorrect.length;nlii++) {
//					if(nlCorrect[nlii]==0) {
//						countzerocost++;
//					}
//				}
//				int countzerocost2=0;
//				for(int nlii=0;nlii<nlCorrect.length;nlii++) {
////					System.out.print(nlCorrect[nlii]);
//					if(nlCorrect[nlii]==0) {
//						ApplicationControl.NonMonotonic=false;
//						switch(nlii) {
//						case 0:
//							if(costShift(s)==0)
//								countzerocost2++;
////							System.out.print("\t"+costShift(s));
//							break;
//						case 1:
//							if(costLeftArc(s)==0)
//								countzerocost2++;
////							System.out.print("\t"+costLeftArc(s));
//							break;
//						case 2:
//							if(costRightArc(s)==0)
//								countzerocost2++;
////							System.out.print("\t"+costRightArc(s));
//							break;
//						case 3:
//							if(costReduce(s)==0)
//								countzerocost2++;
////							System.out.print("\t"+costReduce(s)+"\tShould not reach!");
//							break;
//						default:
//							break;
//						}
//						ApplicationControl.NonMonotonic=true;
//					}
////					System.out.println();
//				}
//				if(countzerocost>0 && countzerocost2==0) {
//					for(int nlii=0;nlii<nlCorrect.length;nlii++) {
//						//System.out.print(nlCorrect[nlii]);
//						if(nlCorrect[nlii]==0) {
//							ApplicationControl.NonMonotonic=false;
//							switch(nlii) {
//							case 0:
//								//System.out.print("\t"+costShift(s));
//								specialcounter[0]+=nlCorrect[nlii]==0?1:0;
//								break;
//							case 1:
//								//System.out.print("\t"+costLeftArc(s));
//								specialcounter[1]+=nlCorrect[nlii]==0?1:0;
//								break;
//							case 2:
//								//System.out.print("\t"+costRightArc(s));
//								specialcounter[2]+=nlCorrect[nlii]==0?1:0;
//								break;
//							case 3:
//								System.out.print("\t"+costReduce(s)+"\tShould not reach!");
//								break;
//							default:
//								break;
//							}
//							ApplicationControl.NonMonotonic=true;
//						}
//						//System.out.println();
//					}
//					//System.out.println();
//				}
//			}
			//END OF DEBUG
			
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
				if(useUnshift) {
					switch(ApplicationControl.UnshiftCostSwitch) {
					case 0:
					case 1:
					case 2:
					case 6:
						//same class
						break;
					case 3:
					case 4:
					case 5:
					case 7:
						//single class
						if(nlCorrect[Configuration.getConfToInt("Unshift")] == 0)
							nlCorrect[Configuration.getConfToInt("Unshift")]=costUnshift(s);
						break;
					default:
						break;
					}
				}
				if(useNMReduce) {
					if(nlCorrect[Configuration.getConfToInt("NMReduce")] == 0)
						nlCorrect[Configuration.getConfToInt("NMReduce")]=costNMReduce(s);
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
						switch(ApplicationControl.UnshiftCostSwitch) {
						case 0:
						case 1:
						case 2:
						case 6:
							//same class
							break;
						case 3:
						case 4:
						case 5:
						case 7:
							//single class
							nlCorrect[Configuration.getConfToInt("Unshift")]=costUnshift(s);
							break;
						default:
							break;
						}
					}
					if(useNMReduce) {
						nlCorrect[Configuration.getConfToInt("NMReduce")]=costNMReduce(s);
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
			
			if(toPrint) {
				for(int i=0;i<nlCorrect.length;i++) {
					System.out.println("Cost("+Configuration.getConfToString(i)+")="+(nlCorrect[i]==Integer.MAX_VALUE?"MAX_VALUE":nlCorrect[i]));
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
				if(toPrint)
					conf = (new Configuration(s.clone(),st,"LeftArc",s.getStack().peekLast().getRel()));
				if(s.getHeads()[s.getStack().peekLast().getID()]==-1)
					printTransitionAnalysis[iterationNumber-1][0]++;
				else
					printTransitionAnalysis[iterationNumber-1][1]++;
				
				//system-1, system-3-overwrite, system-4-overwrite
				//add information to state: heads, leftmost, rightmost
				makeArc(s, s.getBuffer().peekFirst().getID(), s.getStack().peekLast().getID());
				
				//do leftarc
				s.getStack().removeLast();
				
				automataState=0;
			}
			else if(nNext==Configuration.getConfToInt("RightArc")) {
				//add configuration to list
				if(toPrint)
					conf = (new Configuration(s.clone(),st,"RightArc",s.getBuffer().peekFirst().getRel()));
				if(s.getUnshift(s.getBuffer().peekFirst().getID()))
					printTransitionAnalysis[iterationNumber-1][3]++;
				else
					printTransitionAnalysis[iterationNumber-1][2]++;
				
				//add information to state: heads, leftmost, rightmost
				makeArc(s, s.getStack().peekLast().getID(), s.getBuffer().peekFirst().getID());
				
				//do rightarc
				s.getStack().add(s.getBuffer().removeFirst());
				
				if(iterationNumber==OnlinePerceptron.maxIter && automataState==1) {
					automataState=2;
				} else {
					automataState=0;
				}
			}
			else if(nNext==Configuration.getConfToInt("Reduce")) {
				if(iterationNumber==OnlinePerceptron.maxIter && automataState==2) {
					if(s.getHeads()[s.getStack().peekLast().getID()]==s.getStack().peekLast().getHead()) {
						if(relCountMap.containsKey(s.getStack().peekLast().getRel())) {
							relCountMap.put(s.getStack().peekLast().getRel(), relCountMap.get(s.getStack().peekLast().getRel())+1);
						} else {
							relCountMap.put(s.getStack().peekLast().getRel(), 1);
						}
					}
					automataState=0;
				} else {
					automataState=0;
				}
				
				//add configuration to list
				if(toPrint)
					conf = (new Configuration(s.clone(),st,"Reduce", null));
				//do reduce
				if(ApplicationControl.NonMonotonic && !useUnshift && !useNMReduce && s.getHeads()[s.getStack().peekLast().getID()]==-1) {
					//system-3-headless
					Word topWord = s.getStack().removeLast();
					makeArc(s, s.getStack().peekLast().getID(), topWord.getID());
					printTransitionAnalysis[iterationNumber-1][5]++;
				} 
				else {
					if(useUnshift && s.getHeads()[s.getStack().peekLast().getID()]==-1) {
						switch(ApplicationControl.UnshiftCostSwitch) {
						case 0:
						case 1:
						case 2:
						case 6:
							//system-4-unshift
							//add configuration to list
							if(toPrint)
								conf = (new Configuration(s.clone(),st,"Unshift", null));
							printTransitionAnalysis[iterationNumber-1][6]++;
							//do unshift
							s.getBuffer().addFirst(s.getStack().removeLast());
							break;
						case 3:
						case 4:
						case 5:
						case 7:
							//should not reach
							System.out.println("Invalid unshift transition operation");
							break;
						default:
							break;
						}
					}
					else {
						//system-1, system-3-other, system-4-reduce
						printTransitionAnalysis[iterationNumber-1][5]++;
						s.getStack().removeLast();
					}
				}
			}
			else if(useUnshift && nNext==Configuration.getConfToInt("Unshift")) {
				//add configuration to list
				if(toPrint)
					conf = (new Configuration(s.clone(),st,"Unshift", null));
				printTransitionAnalysis[iterationNumber-1][6]++;
				//do unshift
				s.getBuffer().addFirst(s.getStack().removeLast());
				
				if(iterationNumber==OnlinePerceptron.maxIter && automataState==0) {
					automataState=1;
				} else {
					automataState=0;
				}
			}
			else if(useNMReduce && nNext==Configuration.getConfToInt("NMReduce")) {
				//system-3.5
				if(toPrint)
					conf = (new Configuration(s.clone(),st,"NMReduce", null));
				printTransitionAnalysis[iterationNumber-1][6]++;
				//do NM-reduce
				Word topWord = s.getStack().removeLast();
				makeArc(s, s.getStack().peekLast().getID(), topWord.getID());
				
				if(iterationNumber==OnlinePerceptron.maxIter && topWord.getHead()==s.getStack().peekLast().getID()) {
					if(relCountMap.containsKey(topWord.getRel())) {
						relCountMap.put(topWord.getRel(), relCountMap.get(topWord.getRel())+1);
					} else {
						relCountMap.put(topWord.getRel(), 1);
					}
				}
			}
			else if(nNext==Configuration.getConfToInt("Shift")) {
				//add configuration to list
				if(toPrint)
					conf = (new Configuration(s.clone(),st,"Shift", null));
				printTransitionAnalysis[iterationNumber-1][4]++;
				//do shift
				s.getStack().add(s.getBuffer().removeFirst());
				
				//system-4
				if(ApplicationControl.NonMonotonic && useUnshift)
					s.setUnshift(s.getStack().peekLast().getID());
				
				automataState=0;
			}
			else { //should not reach
				System.out.println("Cannot find perform transition!");
				if(toPrint)
					conf = (new Configuration(s.clone(),st,"Unknown", null));
			}
			
			if(toPrint) {
				System.out.println(conf.toString()+"\n");
			}
			
		}
		
		if(!ApplicationControl.CleanerOutput) {
			if(sentenceCount[iterationNumber-1]%1000==0) {
				System.out.println("Iteration "+iterationNumber+": "+sentenceCount[iterationNumber-1]+" sentences processed");
				if(printConfCount[iterationNumber-1]>0)
					System.out.println("\t"+printConfCount[iterationNumber-1]+" configurations printed");
			}
		}
	}
	
	public static void doParsing(OnlinePerceptron model, Sentence st) {
		//DEBUG: logging dead loop
//		int transitioncount=0;
//		ArrayList<String> transitionhistory = new ArrayList<String>();
		//END OF DEBUG
		printTransitionAnalysis[OnlinePerceptron.maxIter][7]+=2*st.getWdList().size();
		LinkedList<TransitionSequence> transSeq = new LinkedList<TransitionSequence>();
		State s = new State(st);
		while(true) {
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
				else if(useNMReduce && b==Configuration.getConfToInt("NMReduce")) {
					if(legalNMReduce(s)) {
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
			
			//terminal when no legal transition
			if(bestTrans==-1)
				break;
			
			if(bestTrans==0) {  //shift
				printTransitionAnalysis[OnlinePerceptron.maxIter][4]++;
				if(ApplicationControl.devAnalysisFile!=null)
					transSeq.add(new TransitionSequence("SH", 
						s.getStack().peekLast()==null?"null":s.getStack().peekLast().getPos(), 
						s.getBuffer().peekFirst()==null?"null":s.getBuffer().peekFirst().getPos(),
						s.getStack().size()>1?s.getStack().get(s.getStack().size()-2).getPos():"null"));
				
				//do shift
				s.getStack().add(s.getBuffer().removeFirst());
				
				if(useUnshift)
					s.setUnshift(s.getStack().peekLast().getID());
			}
			else if(bestTrans==1) {  //leftArc
				if(s.getHeads()[s.getStack().peekLast().getID()]==-1)
					printTransitionAnalysis[OnlinePerceptron.maxIter][0]++;
				else
					printTransitionAnalysis[OnlinePerceptron.maxIter][1]++;
				if(ApplicationControl.devAnalysisFile!=null)
					transSeq.add(new TransitionSequence("LA", 
						s.getStack().peekLast()==null?"null":s.getStack().peekLast().getPos(), 
						s.getBuffer().peekFirst()==null?"null":s.getBuffer().peekFirst().getPos(),
						s.getStack().size()>1?s.getStack().get(s.getStack().size()-2).getPos():"null"));
				
				//add information to state: heads, leftmost, rightmost
				makeArc(s, s.getBuffer().peekFirst().getID(), s.getStack().peekLast().getID());
				
				//write arc to sentence
				st.getWdList().get(s.getStack().peekLast().getID()).setHead(s.getBuffer().peekFirst().getID());
				//do leftarc
				s.getStack().removeLast();
			}
			else if(bestTrans==2) {  //rightArc
				if(s.getUnshift(s.getBuffer().peekFirst().getID()))
					printTransitionAnalysis[OnlinePerceptron.maxIter][3]++;
				else
					printTransitionAnalysis[OnlinePerceptron.maxIter][2]++;
				if(ApplicationControl.devAnalysisFile!=null)
					transSeq.add(new TransitionSequence("RA", 
						s.getStack().peekLast()==null?"null":s.getStack().peekLast().getPos(), 
						s.getBuffer().peekFirst()==null?"null":s.getBuffer().peekFirst().getPos(),
						s.getStack().size()>1?s.getStack().get(s.getStack().size()-2).getPos():"null"));
				
				//add information to state: heads, leftmost, rightmost
				makeArc(s, s.getStack().peekLast().getID(), s.getBuffer().peekFirst().getID());
			
				//write arc to sentence
				st.getWdList().get(s.getBuffer().peekFirst().getID()).setHead(s.getStack().peekLast().getID());
				//do rightarc
				s.getStack().add(s.getBuffer().removeFirst());
			}
			else if(bestTrans==3) {  //reduce
				//do reduce
				if(ApplicationControl.NonMonotonic && !useUnshift && !useNMReduce && s.getHeads()[s.getStack().peekLast().getID()]==-1) {
					printTransitionAnalysis[OnlinePerceptron.maxIter][5]++;
					if(ApplicationControl.devAnalysisFile!=null)
						transSeq.add(new TransitionSequence("RE", 
							s.getStack().peekLast()==null?"null":s.getStack().peekLast().getPos(), 
							s.getBuffer().peekFirst()==null?"null":s.getBuffer().peekFirst().getPos(),
							s.getStack().size()>1?s.getStack().get(s.getStack().size()-2).getPos():"null"));
					
					//system-3-headless
					Word topWord = s.getStack().removeLast();
					
					makeArc(s, s.getStack().peekLast().getID(), topWord.getID());
					st.getWdList().get(topWord.getID()).setHead(s.getStack().peekLast().getID());
				} 
				else {
					if(useUnshift && s.getHeads()[s.getStack().peekLast().getID()]==-1) {
						switch(ApplicationControl.UnshiftCostSwitch) {
						case 0:
						case 1:
						case 2:
						case 6:
							printTransitionAnalysis[OnlinePerceptron.maxIter][6]++;
							if(ApplicationControl.devAnalysisFile!=null)
								transSeq.add(new TransitionSequence("UN", 
									s.getStack().peekLast()==null?"null":s.getStack().peekLast().getPos(), 
									s.getBuffer().peekFirst()==null?"null":s.getBuffer().peekFirst().getPos(),
									s.getStack().size()>1?s.getStack().get(s.getStack().size()-2).getPos():"null"));
							
							//system-4-unshift
							s.getBuffer().addFirst(s.getStack().removeLast());
							break;
						case 3:
						case 4:
						case 5:
						case 7:
							break;
						default:
							break;
						}
					} 
					else {
						printTransitionAnalysis[OnlinePerceptron.maxIter][5]++;
						if(ApplicationControl.devAnalysisFile!=null)
							transSeq.add(new TransitionSequence("RE", 
								s.getStack().peekLast()==null?"null":s.getStack().peekLast().getPos(), 
								s.getBuffer().peekFirst()==null?"null":s.getBuffer().peekFirst().getPos(),
								s.getStack().size()>1?s.getStack().get(s.getStack().size()-2).getPos():"null"));
						
						//system-1, system-3-other, system-4-reduce
						s.getStack().removeLast();
					}
				}
			}
			else if(useUnshift && bestTrans==4) {  //unshift
				printTransitionAnalysis[OnlinePerceptron.maxIter][6]++;
				if(ApplicationControl.devAnalysisFile!=null)
					transSeq.add(new TransitionSequence("UN", 
						s.getStack().peekLast()==null?"null":s.getStack().peekLast().getPos(), 
						s.getBuffer().peekFirst()==null?"null":s.getBuffer().peekFirst().getPos(),
						s.getStack().size()>1?s.getStack().get(s.getStack().size()-2).getPos():"null"));
				
				//do unshift
				s.getBuffer().addFirst(s.getStack().removeLast());
			}
			else if(useNMReduce && bestTrans==4) {  //NM-reduce
				printTransitionAnalysis[OnlinePerceptron.maxIter][6]++;
				if(ApplicationControl.devAnalysisFile!=null)
					transSeq.add(new TransitionSequence("UN", 
						s.getStack().peekLast()==null?"null":s.getStack().peekLast().getPos(), 
						s.getBuffer().peekFirst()==null?"null":s.getBuffer().peekFirst().getPos(),
						s.getStack().size()>1?s.getStack().get(s.getStack().size()-2).getPos():"null"));
				
				//system-3.5
				//do NM-reduce
				Word topWord = s.getStack().removeLast();
				
				makeArc(s, s.getStack().peekLast().getID(), topWord.getID());
				st.getWdList().get(topWord.getID()).setHead(s.getStack().peekLast().getID());
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
					s.getBuffer().addFirst(s.getStack().removeLast());
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
				s.getBuffer().addFirst(s.getStack().removeLast());
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
					s.getBuffer().addFirst(s.getStack().removeLast());
				}
				else if(s.getStack().peekLast().getHead()!=-1) {
					s.getBuffer().removeFirst();
					s.getBuffer().addFirst(s.getStack().removeLast());
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
			while(true) {
				
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
					else {  //terminal
						if(s.getStack().size()>1 || !s.getBuffer().isEmpty())  //should not reach
							System.out.println("no legal transition available");
						break;
					}
				}
				
//				System.out.println("Final Steps: "+Configuration.getConfToString(bestTrans));
				
				if(bestTrans==0) {  //shift
					printTransitionAnalysis[OnlinePerceptron.maxIter][4]++;
					//do shift
					s.getStack().add(s.getBuffer().removeFirst());
				}
				else if(bestTrans==1) {  //leftArc
					printTransitionAnalysis[OnlinePerceptron.maxIter][1]++;
					//add information to state: heads, leftmost, rightmost
					makeArc(s, s.getBuffer().peekFirst().getID(), s.getStack().peekLast().getID());
					
					//write arc to sentence
					st.getWdList().get(s.getStack().peekLast().getID()).setHead(s.getBuffer().peekFirst().getID());
					//do leftarc
					s.getStack().removeLast();
				}
				else if(bestTrans==2) {  //rightArc
					printTransitionAnalysis[OnlinePerceptron.maxIter][3]++;
					//add information to state: heads, leftmost, rightmost
					makeArc(s, s.getStack().peekLast().getID(), s.getBuffer().peekFirst().getID());
				
					//write arc to sentence
					st.getWdList().get(s.getBuffer().peekFirst().getID()).setHead(s.getStack().peekLast().getID());
					//do rightarc
					s.getStack().add(s.getBuffer().removeFirst());
				}
				else if(bestTrans==3) {  //reduce
					printTransitionAnalysis[OnlinePerceptron.maxIter][5]++;
					//do reduce
					s.getStack().removeLast();
				}
				else if(bestTrans==4) {  //unshift
					printTransitionAnalysis[OnlinePerceptron.maxIter][6]++;
					//do unshift
					s.getBuffer().addFirst(s.getStack().removeLast());
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
		
		if(ApplicationControl.devAnalysisFile!=null) {
			TransitionSequence.writeDevAnalysis(transSeq);
		}
		
	}
	
	
	public static void resetCounter() {
		for(int i=0;i<sentenceCount.length;i++) {
			sentenceCount[i]=0;
			for(int j=0;j<6;j++) {
				printTransitionAnalysis[i][j]=0;
			}
		}
		
		if(ApplicationControl.RelCount && (!relCountMap.isEmpty())) {
			System.out.println("Rel Count NM-RE:");
			ArrayList<String> sortList = new ArrayList<String>(relCountMap.keySet());
			Collections.sort(sortList, new Comparator<String>() {

				@Override
				public int compare(String o1, String o2) {
					return relCountMap.get(o2)-relCountMap.get(o1);
				}
				
			});
			for(String k : sortList) {
				System.out.println(k+"\t"+relCountMap.get(k));
			}
			
			relCountMap.clear();
		}
	}
	
	public static final String analysisSeparator = "\t";
	
	public static String getAnalysis(int iterationNumber) {
		StringBuilder sb = new StringBuilder();
		int sum = 0;
		for(int i=0;i<7;i++) {
			sb.append(printTransitionAnalysis[iterationNumber-1][i]+analysisSeparator);
			sum+=printTransitionAnalysis[iterationNumber-1][i];
		}
		sb.append(""+sum+analysisSeparator);
		sb.append(printTransitionAnalysis[iterationNumber-1][7]);
		return sb.toString();
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
				for(int i=0;i<originalHeadID;i++) {
					if(s.getHeads()[i]==originalHeadID) {
						s.getLeftMost()[originalHeadID]=i;
						break;
					}
				}
			}
			if(s.getRightMost()[originalHeadID]==dependentID) {
				//find rightmost dep
				s.getRightMost()[originalHeadID]=-1;
				for(int i=s.getHeads().length-1;i>originalHeadID;i--) {
					if(s.getHeads()[i]==originalHeadID) {
						s.getRightMost()[originalHeadID]=i;
						break;
					}
				}
			}
		}
		
		if((s.getLeftMost()[headID]==-1 || s.getLeftMost()[headID]>dependentID) && headID>dependentID)
			s.getLeftMost()[headID]=dependentID;
		if((s.getRightMost()[headID]==-1 || s.getRightMost()[headID]<dependentID) && headID<dependentID)
			s.getRightMost()[headID]=dependentID;
		
	}
	
	//zero-cost check for each transition
	@SuppressWarnings("unused")
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
	
	@SuppressWarnings("unused")
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
	
	@SuppressWarnings("unused")
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
	
	@SuppressWarnings("unused")
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
	
	@SuppressWarnings("unused")
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
			//Unshift = ?
			if(useUnshift && legalUnshift(s)) {
				switch(ApplicationControl.UnshiftCostSwitch) {
				case 0:
					//cost-reduce
					break;
				case 1:
					//shifted
				case 2:
					//zero-cost
				case 6:
					//infinity shifted
					return costUnshift(s);
				case 3:
				case 4:
				case 5:
				case 7:
					//should not reach
					System.out.println("Invalid unshift cost!");
					return Integer.MAX_VALUE;
				default:
					break;
				}
			}
		}
		
		return cost;
	}
	
	private static int costReduceUnshift(State s) {
		if(!legalUnshift(s))
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
	
	private static int costNMReduce(State s) {
		if(!legalNMReduce(s))
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
			boolean cost_nm_la=false;
			if(s.getStack().peekLast().getHead()==s.getHeads()[s.getStack().peekLast().getID()])
				cost_nm_la=true;
			if(s.getHeads()[s.getStack().peekLast().getID()]!=-1 && s.getStack().peekLast().getHead()!=s.getBuffer().peekFirst().getID()) {
				for(Word w : s.getBuffer()) {
					if(s.getStack().peekLast().getHead()==w.getID()) {
						cost_nm_la=true;
						break;
					}
				}
			}
			if(cost_nm_la)
				cost++;
			if(!useUnshift) {
				//system-3
				//NM_RE
				if(s.getHeads()[s.getStack().peekLast().getID()]==-1) {
					Word stackTop = s.getStack().removeLast();
					if(stackTop.getHead()==s.getStack().peekLast().getID())
						cost++;
					s.getStack().add(stackTop);
				}
			}
			else {
				//system-4
				//NM_RE+Unshift
				if(s.getHeads()[s.getStack().peekLast().getID()]==-1) {
					for(Word w : s.getStack()) {
						if(w==s.getStack().peekLast())
							continue;
						
						if(s.getStack().peekLast().getHead()==w.getID())
							cost++;
						if(w.getHead()==s.getStack().peekLast().getID())
							cost++;
					}
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
			
			//system-4
			//Unshift = 0
//			if(ApplicationControl.NonMonotonic && useUnshift) {
//				if(!s.getUnshift(s.getBuffer().peekFirst().getID())) {
//					for(Word w : s.getBuffer()) {
//						if(w==s.getBuffer().peekFirst()) 
//							continue;
//						
//						if(s.getBuffer().peekFirst().getHead()==w.getID()) {
//							cost++;
//							break;
//						}
//					}
//				}
//			}
		}
		
		if(cost<0)
			cost=0;
		return cost;
	}
	
	private static int costUnshift(State s) {
		if(!legalUnshift(s))
			return Integer.MAX_VALUE;
		
		int cost = 0;
		switch(ApplicationControl.UnshiftCostSwitch) {
		case 0:
			//should not reach
			System.out.println("Invalid unshift cost");
			return Integer.MAX_VALUE;
		case 3:
			return costReduceUnshift(s);
		case 1:
		case 4:
			//system-5 (shifted)
			boolean dep_in_stack = false; 
			for(Word w : s.getStack()) {
				if(w.getHead()==s.getStack().peekLast().getID()) {
					dep_in_stack=true;
					break;
				}
			}
			if(dep_in_stack)
				break;
			for(Word w : s.getBuffer()) {
				if(s.getStack().peekLast().getHead()==w.getID()) {
					cost++;
					break;
				}
			}
			break;
		case 6:
		case 7:
			//system-5 (old shifted)
			for(Word w : s.getBuffer()) {
				if(s.getStack().peekLast().getHead()==w.getID()) {
					cost++;
					break;
				}
			}
			break;
		case 2:
		case 5:
			//zero-cost
			break;
		default:
			System.out.println("Invalid unshift cost");
			return Integer.MAX_VALUE;
		}
		
		return cost;
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
			
			if(!useUnshift) {
				//system-3-NM_RE
				if(s.getBuffer().peekFirst().getHead()==s.getStack().peekLast().getID())
					cost--;
			}
			else {
				//system-4
				//delete NM_RE cost delta
			}
		}
		
		if(cost<0)
			cost=0;
		return cost;
	}
	
	//legal check for each transitions
	private static boolean legalLeftArc(State s) {
		//system-1, system-3, system-4
		//|sigma|>1, |beta|>0
		if(s.getBuffer().isEmpty() || s.getStack().size()<=1)
			return false;
		//system-1
		//!HEAD(s)
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
		//|sigma|>0, |beta|>0
		if(s.getBuffer().isEmpty() || s.getStack().isEmpty())
			return false;
		
		//system-4 (should not reach by system-1 & system-3)
		//!HEAD(b)
		if(s.getHeads()[s.getBuffer().peekFirst().getID()]!=-1)
			return false;
		
		return true;
	}
	
	private static boolean legalFinalRightArc(State s) {
		//system-2
		return legalRightArc(s);
	}
	
	private static boolean legalReduce(State s) {
		//system-1, system-3, system-4-(reduce+unshift)
		//|sigma|>1
		if(s.getStack().size()<=1)
			return false;
		//system-1
		//HEAD(s)
		if(!ApplicationControl.NonMonotonic && s.getHeads()[s.getStack().peekLast().getID()]==-1)
			return false;
		
		if(ApplicationControl.NonMonotonic && useNMReduce && s.getHeads()[s.getStack().peekLast().getID()]==-1)
			return false;
		
		//system-5
		//HEAD(s)
		if(useUnshift) {
			switch(ApplicationControl.UnshiftCostSwitch) {
			case 0:
			case 1:
			case 2:
			case 6:
				break;
			case 3:
			case 4:
			case 5:
			case 7:
				if(s.getHeads()[s.getStack().peekLast().getID()]==-1)
					return false;
				break;
			default:
				break;
			}
		}
		
		return true;
	}
	
	private static boolean legalNMReduce(State s) {
		//system-3.5
		if(!useNMReduce)
			return false;
		
		if(!ApplicationControl.NonMonotonic)
			return false;
		
		if(s.getStack().size()<=1)
			return false;
		
		if(s.getHeads()[s.getStack().peekLast().getID()]!=-1)
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
		
		if(!ApplicationControl.NonMonotonic)
			return false;
		
		//system-5
		//|sigma|>1
		if(s.getStack().size()<=1)
			return false;
		//!HEAD(s)
		if(s.getHeads()[s.getStack().peekLast().getID()]!=-1)
			return false;
		
		return true;
	}
	
	private static boolean legalFinalUnshift(State s) {
		//system-2
		//|sigma|>1
		if(s.getStack().size()<=1)
			return false;
		//|beta|>0
		if(!s.getBuffer().isEmpty())
			return false;
		//!HEAD(s)
		if(s.getHeads()[s.getStack().peekLast().getID()]!=-1)
			return false;
		
		return true;
	}
	
	private static boolean legalShift(State s) {
		//system-1, system-3
		//|beta|>0
		if(s.getBuffer().isEmpty())
			return false;
		
		//system-4
		//S(b)=0
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
		switch(ApplicationControl.UnshiftCostSwitch) {
		case 0:
		case 1:
		case 2:
		case 6:
			//system-4-unshift
			nLabel=4;
			break;
		case 3:
		case 4:
		case 5:
		case 7:
			//system-5
			nLabel=5;
			break;
		default:
			nLabel=4;
			break;
		}
	}
	
	public static void disableUnshift() {
		useUnshift=false;
		nLabel=4;
	}
	
	public static void enableNMReduce() {
		useNMReduce=true;
		nLabel=5;
	}
	
	public static void disableNMReduce() {
		useNMReduce=false;
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

class TransitionSequence {
	public String trans;
	public String posS;
	public String posB;
	public String posS1;
	
	public TransitionSequence(String t, String s, String b, String s1) {
		trans=t;
		posS=s;
		posB=b;
		posS1=s1;
	}
	
	public String toString() {
		return trans+"\t"+posS+"\t"+posB+"\t"+posS1;
	}
	
	public static void writeDevAnalysis(LinkedList<TransitionSequence> list) {
		if(list.isEmpty())
			return;
		
		try {
			File file = new File(ApplicationControl.devAnalysisFile);
			boolean exist = true;
			if(!file.exists()) {
				exist=false;
				file.createNewFile();
			}
			BufferedWriter fw=new BufferedWriter( new OutputStreamWriter(new FileOutputStream(file, true),"UTF-8"));
			if(!exist)
				fw.write(new TransitionSequence("Trans", "POS(s)", "POS(b)", "POS(s1)").toString()+"\n\n");
			for(TransitionSequence ts : list) {
				fw.write(ts.toString()+"\n");
			}
			fw.write("\n");
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
