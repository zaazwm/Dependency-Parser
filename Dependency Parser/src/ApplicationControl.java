import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;

import de.bwaldvogel.liblinear.InvalidInputDataException;


public class ApplicationControl {
	
	//parameters to control application
	public static boolean testMark=false;  //true for test, false for training
	public static boolean ArcStandard=false;  //true for arcstandard, false for arceager
	public static boolean AvePerceptron=true;  //(modelLibSVM=false) true to use ave.perceptron
	public static boolean devMark=false;  //(testMark=true) true for dev.test to compare result
	public static boolean smallTrainData=false;  //true to use 5k data
	public static boolean modelLibSVM=true;  //true to use libSVM or libLinear, false to use Perceptron
	public static boolean modelLibLinear=true;  //(modelLibSVM=true) true to use libLinear, false to use libSVM
	public static boolean predictArcTag=false;  //(modelLibSVM=true) true to predict ArcTag, false to predict null
	public static boolean argsReader=false; //true to read args[], false to use default settings
	public static boolean newPredArcTag=false; //true to predict arc tags separately
	public static boolean ArcEagerOnline=false; //true to only use ArcEager, Dynamic Oracle, Unshift supported

	public static void main(String[] args) throws IOException, ClassNotFoundException, InvalidInputDataException, ParseException {
		//main entry to application
		
		//deal with commandline arguments
		Options para=new Options();
		BasicParser parser = new BasicParser();
		
		para.addOption("data", true, "filepath(<arg>) for data");
		para.addOption("test", false, "use for test");
		para.addOption("train", false, "use for train, <default>");
		para.addOption("AS", false, "use ArcStandard for decoder");
		para.addOption("AE", false, "use ArcEager for decoder, <default>");
		para.addOption("perceptron", false, "use Perceptron for training");
		para.addOption("aveperc", false, "use Average Perceptron for training");
		para.addOption("libsvm", false, "use LibSVM for training");
		para.addOption("liblinear", false, "use LibLinear for training, <default>");
		para.addOption("arcpred", false, "predict arc tags, need -libsvm or -liblinear");
		para.addOption("newarcpred", false, "predict arc tags separately, need -libsvm or -liblinear");
		para.addOption("dev", false, "use development data to compare result");
		para.addOption("h", "help", false, "print help message");
		para.addOption("AEO", "AEOnline", false, "use ArcEagerOnline for decoder, supporting Unshift & Dynamic Oracle");
		
		CommandLine cl = parser.parse(para, args);
		
		if(args.length<1) {
			System.out.println("invalid arguments, use -h or --help for help");
			System.exit(0);
		}
		
		if(cl.hasOption('h') || cl.hasOption("help")) {
           HelpFormatter f = new HelpFormatter();
           f.printHelp("OptionsTip", para);
           
           System.exit(0);
        }
		
		if(cl.hasOption("data"))
			argsReader=true;
		
		if(cl.hasOption("test"))
			testMark=true;
			
		if(cl.hasOption("train"))
			testMark=false;
		
		if(cl.hasOption("AS"))
			ArcStandard=true;
		
		if(cl.hasOption("AE"))
			ArcStandard=false;
		
		if(cl.hasOption("perceptron")) {
			modelLibSVM=false;
			AvePerceptron=false;
		}
		
		if(cl.hasOption("aveperc")) {
			modelLibSVM=false;
			AvePerceptron=true;
		}
		
		if(cl.hasOption("libsvm")) {
			modelLibSVM=true;
			modelLibLinear=false;
		}
		
		if(cl.hasOption("liblinear")) {
			modelLibSVM=true;
			modelLibLinear=true;
		}
		
		if(cl.hasOption("arcpred")) {
			modelLibSVM=true;
			predictArcTag=true;
		}
		
		if(cl.hasOption("newarcpred")) {
			modelLibSVM=true;
			newPredArcTag=true;
		}
		
		if(cl.hasOption("dev"))
			devMark=true;
		
		if(cl.hasOption("AEO") || cl.hasOption("AEOnline"))
			ArcEagerOnline=true;
		
		System.out.println("Arguments Readed: ");
		
		System.out.println("testMark = "+testMark);
		System.out.println("ArcStandard = "+ArcStandard);
		System.out.println("AvePerceptron = "+AvePerceptron);
		System.out.println("devMark = "+devMark);
		System.out.println("smallTrainData = "+smallTrainData);
		System.out.println("modelLibSVM = "+modelLibSVM);
		System.out.println("modelLibLinear = "+modelLibLinear);
		System.out.println("predictArcTag = "+predictArcTag);
		System.out.println("newPredArcTag = "+newPredArcTag);
		System.out.println("argsReader = "+argsReader);
		System.out.println("ArcEagerOnline = "+ArcEagerOnline);
		
		//run the program
		String dataPath = null;
		if(argsReader) {
			//if(args.length==1)
				dataPath = cl.getOptionValue("data");
				if(dataPath == null) {
					System.out.println("No Data File!");
					System.exit(0);
				}
				File dpp = new File(dataPath);
				if(!dpp.exists()) {
					System.out.println("File not exist!");
					System.exit(0);
				}
				System.out.println("dataPath = "+dataPath);
			//else {
			//	System.out.println("please use targetfile as argument (training-data | test-data)");
			//	System.exit(0);
			//}
		}
		if(!testMark) {
			TrainModel(dataPath);
			System.out.println("Training Finished!");
		}
		else {
			TestModel(dataPath);
			System.out.println("Test Finished!");
			if(devMark)
				CompareResult(dataPath);
		}
	}
	
	public static void run(String dataPath) throws IOException, InvalidInputDataException, ClassNotFoundException {
		//run the program, for extended usage
		if(!testMark) {
			TrainModel(dataPath);
			System.out.println("Training Finished!");
		}
		else {
			TestModel(dataPath);
			System.out.println("Test Finished!");
			if(devMark)
				CompareResult(dataPath);
		}
	}
	
	public static void run(String dataPath, String modelPath) throws IOException, InvalidInputDataException, ClassNotFoundException {
		//run the program, for extended usage
		if(!testMark) {
			TrainModel(dataPath, modelPath);
			System.out.println("Training Finished!");
		}
		else {
			TestModel(dataPath, modelPath);
			System.out.println("Test Finished!");
			if(devMark)
				CompareResult(dataPath);
		}
	}
	
	public static void TrainModel(String dataPath) throws IOException, InvalidInputDataException {
		ArcStandardDecoder.resetMemo();
		ArcStandardDecoder.resetSwco();
		LinkedList<Feature> fl = new LinkedList<Feature>();
		LinkedList<TagFeature> tfl = new LinkedList<TagFeature>();
		//read file
		String dataDir = null;
		if(argsReader)
			dataDir = new File(dataPath).getParent();
		Reader rd;
		if(argsReader)
			rd = new Reader(dataPath);
		else {
			if(smallTrainData)
				rd = new Reader("/Users/zaa/Desktop/VIS hiwi/dep_parsing/wsj_train.only-projective.first-5k.conll06");
			else
				rd = new Reader("/Users/zaa/Desktop/VIS hiwi/dep_parsing/wsj_train.only-projective.conll06");
		}
		
		DynamicPerceptron dpc = new DynamicPerceptron(ArcEagerOnlineDecoder.nLabel);
		LinkedList<Sentence> stList = new LinkedList<Sentence>();
		//read sentences and calc & save configurations
		while(rd.hasNext()) {
			LinkedList<Configuration> cl = new LinkedList<Configuration>();
			
			if(ArcEagerOnline) {
				Sentence st = rd.readNext();
				ArcEagerOnlineDecoder.buildConfiguration(st, dpc, 1);
				stList.add(st);
			}
			else if(ArcStandard) {
				ArcStandardDecoder.buildConfiguration(rd.readNext(), cl, tfl);
			}
			else {
				ArcEagerDecoder.buildConfiguration(rd.readNext(), cl, tfl);
			}
			
			for(Configuration cf : cl) {
				fl.add(cf.buildFeature());
			}
			
			//to build Tag Feature separately, not online (valid if more complex feature)
			//ArcTager.buildTagFeature(rd.readLast(), tfl);
		}
		
		rd.close();
		
		//online training iteration for ArcEagerOnline
		if(ArcEagerOnline) {
			for(int i=2;i<=DynamicPerceptron.maxIter;i++) {
				for(Sentence st : stList) {
					ArcEagerOnlineDecoder.buildConfiguration(st, dpc, i);
				}
			}
			
			ArcEagerOnlineDecoder.resetCounter();
		}
		
		if(ArcEagerOnline) {
			System.out.println("Feature number: "+dpc.getnFeature());
			//write model to file
			File modelPath;
			if(argsReader)
				modelPath = new File(dataDir+"/dp.model");
			else
				modelPath = new File("/Users/zaa/Desktop/VIS hiwi/dep_parsing/dp.model");
			ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(modelPath)));
			oos.writeObject(dpc);
			oos.close();
		}
		else if(!modelLibSVM) {
			//use saved configurations to do perceptron
			Perceptron pc = new Perceptron(fl, ArcStandard?ArcStandardDecoder.nLabel:ArcEagerDecoder.nLabel);
			System.out.println("Feature number: "+pc.getnFeature());
			//write model to file
			File modelPath;
			if(argsReader)
				modelPath = new File(dataDir+"/dp.model");
			else
				modelPath = new File("/Users/zaa/Desktop/VIS hiwi/dep_parsing/dp.model");
			ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(modelPath)));
			oos.writeObject(pc);
			oos.close();
		}
		else if(!modelLibLinear) {
			//use saved configurations to do libsvm
			
			LibClassifier pc;
			if(argsReader)
				pc = new LibSVM(fl, ArcStandard?ArcStandardDecoder.nLabel:ArcEagerDecoder.nLabel,
						dataDir+"/dp.model", dataDir+"/dp.feature");
			else
				pc = new LibSVM(fl, ArcStandard?ArcStandardDecoder.nLabel:ArcEagerDecoder.nLabel,
					"/Users/zaa/Desktop/VIS hiwi/dep_parsing/dp.model", "/Users/zaa/Desktop/VIS hiwi/dep_parsing/dp.feature");
			System.out.println("Feature number: "+pc.getnFeature()+(predictArcTag?(" ArcTag #: "+pc.getnTag()):"")+(ArcStandard?(" Non-Projective #: "+ArcStandardDecoder.readMemo()+" swap #: "+ArcStandardDecoder.readSwco()):"")+"\n\n");
			//write model to file
			File modelPath;
			if(argsReader)
				modelPath = new File(dataDir+"/dp.mapping");
			else
				modelPath = new File("/Users/zaa/Desktop/VIS hiwi/dep_parsing/dp.mapping");
			ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(modelPath)));
			oos.writeObject(pc);
			oos.close();
			
			//to train tagger model separately 
			if(newPredArcTag) {
				LibClassifier pc2;
				if(argsReader)
					pc2 = new LibSVM(tfl, dataDir+"/tg.model", dataDir+"/tg.feature");
				else
					pc2 = new LibSVM(tfl, "/Users/zaa/Desktop/VIS hiwi/dep_parsing/tg.model", "/Users/zaa/Desktop/VIS hiwi/dep_parsing/tg.feature");
				System.out.println("ArcTag Feature: "+pc2.getnFeature()+" ArcTag #: "+pc2.getnNewTag());
				//write model to file
				File modelPath2;
				if(argsReader)
					modelPath2 = new File(dataDir+"/tg.mapping");
				else
					modelPath2 = new File("/Users/zaa/Desktop/VIS hiwi/dep_parsing/tg.mapping");
				ObjectOutputStream oos2 = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(modelPath2)));
				oos2.writeObject(pc2);
				oos2.close();
			}
		}
		else {
			//use saved configurations to do liblinear
			LibClassifier pc;
			if(argsReader)
				pc = new LibLinear(fl, ArcStandard?ArcStandardDecoder.nLabel:ArcEagerDecoder.nLabel,
					dataDir+"/dp.model", dataDir+"/dp.feature");
			else
				pc = new LibLinear(fl, ArcStandard?ArcStandardDecoder.nLabel:ArcEagerDecoder.nLabel,
						"/Users/zaa/Desktop/VIS hiwi/dep_parsing/dp.model", "/Users/zaa/Desktop/VIS hiwi/dep_parsing/dp.feature");
			System.out.println("Feature number: "+pc.getnFeature()+(predictArcTag?(" ArcTag #: "+pc.getnTag()):"")+(ArcStandard?(" Non-Projective #: "+ArcStandardDecoder.readMemo()+" swap #: "+ArcStandardDecoder.readSwco()):"")+"\n\n");
			//write model to file
			File modelPath;
			if(argsReader)
				modelPath = new File(dataDir+"/dp.mapping");
			else
				modelPath = new File("/Users/zaa/Desktop/VIS hiwi/dep_parsing/dp.mapping");
			ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(modelPath)));
			oos.writeObject(pc);
			oos.close();
			
			//to train tagger model separately 
			if(newPredArcTag) {
				LibClassifier pc2;
				if(argsReader)
					pc2 = new LibLinear(tfl, dataDir+"/tg.model", dataDir+"/tg.feature");
				else
					pc2 = new LibLinear(tfl, "/Users/zaa/Desktop/VIS hiwi/dep_parsing/tg.model", "/Users/zaa/Desktop/VIS hiwi/dep_parsing/tg.feature");
				System.out.println("ArcTag Feature: "+pc2.getnFeature()+" ArcTag #: "+pc2.getnNewTag());
				//write model to file
				File modelPath2;
				if(argsReader)
					modelPath2 = new File(dataDir+"/tg.mapping");
				else
					modelPath2 = new File("/Users/zaa/Desktop/VIS hiwi/dep_parsing/tg.mapping");
				ObjectOutputStream oos2 = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(modelPath2)));
				oos2.writeObject(pc2);
				oos2.close();
			}
		}
	}
	
	public static void TestModel(String dataPath) throws FileNotFoundException, IOException, ClassNotFoundException {
		//read file
		String dataDir = null;
		if(argsReader)
			dataDir = new File(dataPath).getParent();
		Perceptron pc = null;
		DynamicPerceptron dpc = null;
		LibClassifier lc = null;
		LibClassifier lc2 = null;
		if(ArcEagerOnline) {
			//read model from file
			File modelPath;
			if(argsReader)
				modelPath = new File(dataDir+"/dp.model");
			else
				modelPath = new File("/Users/zaa/Desktop/VIS hiwi/dep_parsing/dp.model");
			ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(new FileInputStream(modelPath)));
			dpc = (DynamicPerceptron) ois.readObject();
			ois.close();
		}
		else if(!modelLibSVM) {
			//read model from file
			File modelPath;
			if(argsReader)
				modelPath = new File(dataDir+"/dp.model");
			else
				modelPath = new File("/Users/zaa/Desktop/VIS hiwi/dep_parsing/dp.model");
			ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(new FileInputStream(modelPath)));
			pc = (Perceptron) ois.readObject();
			ois.close();
		}
		else if(!modelLibLinear) {
			//read model from file
			File modelPath;
			if(argsReader)
				modelPath = new File(dataDir+"/dp.mapping");
			else
				modelPath = new File("/Users/zaa/Desktop/VIS hiwi/dep_parsing/dp.mapping");
			ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(new FileInputStream(modelPath)));
			lc = (LibSVM) ois.readObject();
			ois.close();
			if(argsReader)
				lc.readModel(dataDir+"/dp.model");
			else
				lc.readModel("/Users/zaa/Desktop/VIS hiwi/dep_parsing/dp.model");
			
			//to read tagger model separately 
			if(newPredArcTag) {
				File modelPath2;
				if(argsReader)
					modelPath2 = new File(dataDir+"/tg.mapping");
				else
					modelPath2 = new File("/Users/zaa/Desktop/VIS hiwi/dep_parsing/tg.mapping");
				ObjectInputStream ois2 = new ObjectInputStream(new GZIPInputStream(new FileInputStream(modelPath2)));
				lc2 = (LibSVM) ois2.readObject();
				ois2.close();
				if(argsReader)
					lc2.readModel(dataDir+"/tg.model");
				else
					lc2.readModel("/Users/zaa/Desktop/VIS hiwi/dep_parsing/tg.model");
			}
		}
		else {
			File modelPath;
			if(argsReader)
				modelPath = new File(dataDir+"/dp.mapping");
			else
				modelPath = new File("/Users/zaa/Desktop/VIS hiwi/dep_parsing/dp.mapping");
			ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(new FileInputStream(modelPath)));
			lc = (LibLinear) ois.readObject();
			ois.close();
			
			if(argsReader)
				lc.readModel(dataDir+"/dp.model");
			else
				lc.readModel("/Users/zaa/Desktop/VIS hiwi/dep_parsing/dp.model");
			
			//to read tagger model separately 
			if(newPredArcTag) {
				File modelPath2;
				if(argsReader)
					modelPath2 = new File(dataDir+"/tg.mapping");
				else
					modelPath2 = new File("/Users/zaa/Desktop/VIS hiwi/dep_parsing/tg.mapping");
				ObjectInputStream ois2 = new ObjectInputStream(new GZIPInputStream(new FileInputStream(modelPath2)));
				lc2 = (LibLinear) ois2.readObject();
				ois2.close();
				
				if(argsReader)
					lc2.readModel(dataDir+"/tg.model");
				else
					lc2.readModel("/Users/zaa/Desktop/VIS hiwi/dep_parsing/tg.model");
				
			}
		}
		
		Reader rd;
		if(argsReader)
			rd = new Reader(dataPath);
		else
			rd = new Reader("/Users/zaa/Desktop/VIS hiwi/dep_parsing/wsj_dev.conll06");
		Writer wt;
		if(argsReader)
			wt = new Writer(dataPath+".result");
		else
			wt = new Writer("/Users/zaa/Desktop/VIS hiwi/dep_parsing/wsj_dev.result.conll06");
		
		System.out.println("Reading model done.");
		//read sentences from test file and do parsing
		int count=0;
		while(rd.hasNext()) {
			count++;
			Sentence st = rd.readNextTest();
			if(ArcEagerOnline) {
				ArcEagerOnlineDecoder.doParsing(dpc, st);
			}
			else if(ArcStandard) {
				if(!modelLibSVM) 
					ArcStandardDecoder.doParsing(pc, st);
				else {
					ArcStandardDecoder.doParsing(lc, st);
					//predict tag
					if(newPredArcTag)
						ArcTager.tag(lc2, st);
				}
					
			}
			else {
				if(!modelLibSVM) 
					ArcEagerDecoder.doParsing(pc, st);
				else {
					ArcEagerDecoder.doParsing(lc, st);
					//predict tag
					if(newPredArcTag)
						ArcTager.tag(lc2, st);
				}
			}
			
			
			
			//write parsing result to file
			wt.write(st);
			
			if(count%100==0) {
				System.out.println(count+" sentences labeled.");
			}
		}
		
		rd.close();
		wt.close();
	}
	
	public static void TrainModel(String dataPath, String mPath) throws IOException, InvalidInputDataException {
		LinkedList<Feature> fl = new LinkedList<Feature>();
		LinkedList<TagFeature> tfl = new LinkedList<TagFeature>();
		ArcStandardDecoder.resetMemo();
		ArcStandardDecoder.resetSwco();
		//read file
		@SuppressWarnings("unused")
		String dataDir = null;
		if(argsReader)
			dataDir = new File(dataPath).getParent();
		Reader rd;
		if(argsReader)
			rd = new Reader(dataPath);
		else {
			if(smallTrainData)
				rd = new Reader("/Users/zaa/Desktop/VIS hiwi/dep_parsing/wsj_train.only-projective.first-5k.conll06");
			else
				rd = new Reader("/Users/zaa/Desktop/VIS hiwi/dep_parsing/wsj_train.only-projective.conll06");
		}
		
		DynamicPerceptron dpc = new DynamicPerceptron(ArcEagerOnlineDecoder.nLabel);
		LinkedList<Sentence> stList = new LinkedList<Sentence>();
		
		//read sentences and calc & save configurations
		while(rd.hasNext()) {
			LinkedList<Configuration> cl = new LinkedList<Configuration>();
			
			if(ArcEagerOnline) {
				Sentence st = rd.readNext();
				ArcEagerOnlineDecoder.buildConfiguration(st, dpc, 1);
				stList.add(st);
			}
			else if(ArcStandard) {
				ArcStandardDecoder.buildConfiguration(rd.readNext(), cl, tfl);
			}
			else {
				ArcEagerDecoder.buildConfiguration(rd.readNext(), cl, tfl);
			}
			
			for(Configuration cf : cl) {
				fl.add(cf.buildFeature());
			}
			
			//to build Tag Feature separately, not online (valid if more complex feature)
			//ArcTager.buildTagFeature(rd.readLast(), tfl);
		}
		
		rd.close();
		
		//online training iteration for ArcEagerOnline
		if(ArcEagerOnline) {
			for(int i=2;i<=DynamicPerceptron.maxIter;i++) {
				for(Sentence st : stList) {
					ArcEagerOnlineDecoder.buildConfiguration(st, dpc, i);
				}
			}
			ArcEagerOnlineDecoder.resetCounter();
		}
		
		//TEST-BUGPOINT
//		System.out.println("SWAP count: "+ArcStandardDecoder.readSwco());
//		System.exit(0);
		//END TEST-BUGPOINT
		
		if(ArcEagerOnline) {
			System.out.println("Feature number: "+dpc.getnFeature());
			//write model to file
			File modelPath;
			if(argsReader)
				modelPath = new File(mPath+"/dp.model");
			else
				modelPath = new File("/Users/zaa/Desktop/VIS hiwi/dep_parsing/dp.model");
			ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(modelPath)));
			oos.writeObject(dpc);
			oos.close();
		}
		else if(!modelLibSVM) {
			//use saved configurations to do perceptron
			Perceptron pc = new Perceptron(fl, ArcStandard?ArcStandardDecoder.nLabel:ArcEagerDecoder.nLabel);
			System.out.println("Feature number: "+pc.getnFeature());
			//write model to file
			File modelPath;
			if(argsReader)
				modelPath = new File(mPath+"dp.model");
			else
				modelPath = new File("/Users/zaa/Desktop/VIS hiwi/dep_parsing/dp.model");
			ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(modelPath)));
			oos.writeObject(pc);
			oos.close();
		}
		else if(!modelLibLinear) {
			//use saved configurations to do libsvm
			LibClassifier pc;
			if(argsReader)
				pc = new LibSVM(fl, ArcStandard?ArcStandardDecoder.nLabel:ArcEagerDecoder.nLabel,
						mPath+"dp.model", mPath+"dp.feature");
			else
				pc = new LibSVM(fl, ArcStandard?ArcStandardDecoder.nLabel:ArcEagerDecoder.nLabel,
					"/Users/zaa/Desktop/VIS hiwi/dep_parsing/dp.model", "/Users/zaa/Desktop/VIS hiwi/dep_parsing/dp.feature");
			System.out.println("Feature number: "+pc.getnFeature()+(predictArcTag?(" ArcTag #: "+pc.getnTag()):"")+(ArcStandard?(" Non-Projective #: "+ArcStandardDecoder.readMemo()+" swap #: "+ArcStandardDecoder.readSwco()):"")+"\n\n");
			//write model to file
			File modelPath;
			if(argsReader)
				modelPath = new File(mPath+"dp.mapping");
			else
				modelPath = new File("/Users/zaa/Desktop/VIS hiwi/dep_parsing/dp.mapping");
			ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(modelPath)));
			oos.writeObject(pc);
			oos.close();
			
			//to train tagger model separately 
			if(newPredArcTag) {
				LibClassifier pc2;
				if(argsReader)
					pc2 = new LibSVM(tfl, mPath+"/tg.model", mPath+"/tg.feature");
				else
					pc2 = new LibSVM(tfl, "/Users/zaa/Desktop/VIS hiwi/dep_parsing/tg.model", "/Users/zaa/Desktop/VIS hiwi/dep_parsing/tg.feature");
				System.out.println("ArcTag Feature: "+pc2.getnFeature()+" ArcTag #: "+pc2.getnNewTag());
				//write model to file
				File modelPath2;
				if(argsReader)
					modelPath2 = new File(mPath+"/tg.mapping");
				else
					modelPath2 = new File("/Users/zaa/Desktop/VIS hiwi/dep_parsing/tg.mapping");
				ObjectOutputStream oos2 = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(modelPath2)));
				oos2.writeObject(pc2);
				oos2.close();
			}
		}
		else {
			//use saved configurations to do liblinear
			LibClassifier pc;
			if(argsReader)
				pc = new LibLinear(fl, ArcStandard?ArcStandardDecoder.nLabel:ArcEagerDecoder.nLabel,
						mPath+"dp.model", mPath+"dp.feature");
			else
				pc = new LibLinear(fl, ArcStandard?ArcStandardDecoder.nLabel:ArcEagerDecoder.nLabel,
						"/Users/zaa/Desktop/VIS hiwi/dep_parsing/dp.model", "/Users/zaa/Desktop/VIS hiwi/dep_parsing/dp.feature");
			System.out.println("Feature number: "+pc.getnFeature()+(predictArcTag?(" ArcTag #: "+pc.getnTag()):"")+(ArcStandard?(" Non-Projective #: "+ArcStandardDecoder.readMemo()+" swap #: "+ArcStandardDecoder.readSwco()):"")+"\n\n");
			//write model to file
			File modelPath;
			if(argsReader)
				modelPath = new File(mPath+"dp.mapping");
			else
				modelPath = new File("/Users/zaa/Desktop/VIS hiwi/dep_parsing/dp.mapping");
			ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(modelPath)));
			oos.writeObject(pc);
			oos.close();
			
			//to train tagger model separately 
			if(newPredArcTag) {
				LibClassifier pc2;
				if(argsReader)
					pc2 = new LibLinear(tfl, mPath+"/tg.model", mPath+"/tg.feature");
				else
					pc2 = new LibLinear(tfl, "/Users/zaa/Desktop/VIS hiwi/dep_parsing/tg.model", "/Users/zaa/Desktop/VIS hiwi/dep_parsing/tg.feature");
				System.out.println("ArcTag Feature: "+pc2.getnFeature()+" ArcTag #: "+pc2.getnNewTag());
				//write model to file
				File modelPath2;
				if(argsReader)
					modelPath2 = new File(mPath+"/tg.mapping");
				else
					modelPath2 = new File("/Users/zaa/Desktop/VIS hiwi/dep_parsing/tg.mapping");
				ObjectOutputStream oos2 = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(modelPath2)));
				oos2.writeObject(pc2);
				oos2.close();
			}
		}
	}
	
	public static void TestModel(String dataPath, String mPath) throws FileNotFoundException, IOException, ClassNotFoundException {
		//read file
		@SuppressWarnings("unused")
		String dataDir = null;
		if(argsReader)
			dataDir = new File(dataPath).getParent();
		Perceptron pc = null;
		DynamicPerceptron dpc = null;
		LibClassifier lc = null;
		LibClassifier lc2 = null;
		if(ArcEagerOnline) {
			//read model from file
			File modelPath;
			if(argsReader)
				modelPath = new File(mPath+"/dp.model");
			else
				modelPath = new File("/Users/zaa/Desktop/VIS hiwi/dep_parsing/dp.model");
			ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(new FileInputStream(modelPath)));
			dpc = (DynamicPerceptron) ois.readObject();
			ois.close();
		}
		else if(!modelLibSVM) {
			//read model from file
			File modelPath;
			if(argsReader)
				modelPath = new File(mPath+"dp.model");
			else
				modelPath = new File("/Users/zaa/Desktop/VIS hiwi/dep_parsing/dp.model");
			ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(new FileInputStream(modelPath)));
			pc = (Perceptron) ois.readObject();
			ois.close();
		}
		else if(!modelLibLinear) {
			//read model from file
			File modelPath;
			if(argsReader)
				modelPath = new File(mPath+"dp.mapping");
			else
				modelPath = new File("/Users/zaa/Desktop/VIS hiwi/dep_parsing/dp.mapping");
			ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(new FileInputStream(modelPath)));
			lc = (LibSVM) ois.readObject();
			ois.close();
			if(argsReader)
				lc.readModel(mPath+"dp.model");
			else
				lc.readModel("/Users/zaa/Desktop/VIS hiwi/dep_parsing/dp.model");
			
			//to read tagger model separately 
			if(newPredArcTag) {
				File modelPath2;
				if(argsReader)
					modelPath2 = new File(mPath+"/tg.mapping");
				else
					modelPath2 = new File("/Users/zaa/Desktop/VIS hiwi/dep_parsing/tg.mapping");
				ObjectInputStream ois2 = new ObjectInputStream(new GZIPInputStream(new FileInputStream(modelPath2)));
				lc2 = (LibSVM) ois2.readObject();
				ois2.close();
				if(argsReader)
					lc2.readModel(mPath+"/tg.model");
				else
					lc2.readModel("/Users/zaa/Desktop/VIS hiwi/dep_parsing/tg.model");
			}
		}
		else {
			File modelPath;
			if(argsReader)
				modelPath = new File(mPath+"dp.mapping");
			else
				modelPath = new File("/Users/zaa/Desktop/VIS hiwi/dep_parsing/dp.mapping");
			ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(new FileInputStream(modelPath)));
			lc = (LibLinear) ois.readObject();
			ois.close();
			
			if(argsReader)
				lc.readModel(mPath+"dp.model");
			else
				lc.readModel("/Users/zaa/Desktop/VIS hiwi/dep_parsing/dp.model");
			
			//to read tagger model separately 
			if(newPredArcTag) {
				File modelPath2;
				if(argsReader)
					modelPath2 = new File(mPath+"/tg.mapping");
				else
					modelPath2 = new File("/Users/zaa/Desktop/VIS hiwi/dep_parsing/tg.mapping");
				ObjectInputStream ois2 = new ObjectInputStream(new GZIPInputStream(new FileInputStream(modelPath2)));
				lc2 = (LibLinear) ois2.readObject();
				ois2.close();
				
				if(argsReader)
					lc2.readModel(mPath+"/tg.model");
				else
					lc2.readModel("/Users/zaa/Desktop/VIS hiwi/dep_parsing/tg.model");
				
			}
		}
		
		Reader rd;
		if(argsReader)
			rd = new Reader(dataPath);
		else
			rd = new Reader("/Users/zaa/Desktop/VIS hiwi/dep_parsing/wsj_dev.conll06");
		Writer wt;
		if(argsReader)
			wt = new Writer(dataPath+".result");
		else
			wt = new Writer("/Users/zaa/Desktop/VIS hiwi/dep_parsing/wsj_dev.result.conll06");
		
		System.out.println("Reading model done.");
		//read sentences from test file and do parsing
		int count=0;
		while(rd.hasNext()) {
			count++;
			Sentence st = rd.readNextTest();
			if(ArcEagerOnline) {
				ArcEagerOnlineDecoder.doParsing(dpc, st);
			}
			else if(ArcStandard) {
				if(!modelLibSVM) 
					ArcStandardDecoder.doParsing(pc, st);
				else {
					ArcStandardDecoder.doParsing(lc, st);
					//predict tag
					if(newPredArcTag)
						ArcTager.tag(lc2, st);
				}
					
			}
			else {
				if(!modelLibSVM) 
					ArcEagerDecoder.doParsing(pc, st);
				else {
					ArcEagerDecoder.doParsing(lc, st);
					//predict tag
					if(newPredArcTag)
						ArcTager.tag(lc2, st);
				}
			}
			
			//write parsing result to file
			wt.write(st);
			
			if(count%100==0) {
				System.out.println(count+" sentences labeled.");
			}
		}
		
		rd.close();
		wt.close();
	}
	
	@SuppressWarnings("resource")
	public static void CompareResult(String dataPath) throws IOException {
		//compare parsing result with golden label
		String path;
		if(argsReader)
			path = dataPath;
		else
			path = new String("/Users/zaa/Desktop/VIS hiwi/dep_parsing/wsj_dev.conll06");
		String pathres;
		if(argsReader)
			pathres = dataPath+".result";
		else
			pathres = new String("/Users/zaa/Desktop/VIS hiwi/dep_parsing/wsj_dev.result.conll06");
		FileInputStream fis=new FileInputStream(path);
		InputStreamReader isr=new InputStreamReader(fis);
		BufferedReader br=new BufferedReader(isr);
		FileInputStream fisr=new FileInputStream(pathres);
		InputStreamReader isrr=new InputStreamReader(fisr);
		BufferedReader brr=new BufferedReader(isrr);
		String line,liner;
		int countfull=0;
		int countright=0;
		int counttagright=0;
		int fieldHead=6;
		int fieldRel=7;
		//only compare head and its tag
		while((line = br.readLine())!=null && (liner = brr.readLine())!=null) {
			if(StringUtils.isBlank(line) || StringUtils.isBlank(liner)) {
				continue;
			}
			String[] fields = line.split("\t");
			String[] fieldsr = liner.split("\t");
			if(fields[fieldHead].equals(fieldsr[fieldHead])) {
				countright++;
				if(fields[fieldRel].equals(fieldsr[fieldRel]))
					counttagright++;
			}
			countfull++;
		}
		
		Double red = ((double)countright*100D/(double)countfull);
		Double tagred = ((double)counttagright*100D/(double)countfull);
		NumberFormat formatter = new DecimalFormat("00.00"); 
		System.out.print("\n\n------------------------------\nResult: "+countright+" : "+countfull+" = "+formatter.format(red)+"%");
		System.out.print(predictArcTag?("   Arc-Tag: "+counttagright+" : "+countfull+" = "+formatter.format(tagred)+"%\n"):"\n");
		System.out.print(newPredArcTag?("   Arc-Tag: "+counttagright+" : "+countfull+" = "+formatter.format(tagred)+"%\n"):"\n");
		
	}
}
