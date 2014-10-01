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
		para.addOption("dev", false, "use development data to compare result");
		para.addOption("h", "help", false, "print help message");
		
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
		
		if(cl.hasOption("dev"))
			devMark=true;
		
		System.out.println("Arguments Readed: ");
		
		System.out.println("testMark = "+testMark);
		System.out.println("ArcStandard = "+ArcStandard);
		System.out.println("AvePerceptron = "+AvePerceptron);
		System.out.println("devMark = "+devMark);
		System.out.println("smallTrainData = "+smallTrainData);
		System.out.println("modelLibSVM = "+modelLibSVM);
		System.out.println("modelLibLinear = "+modelLibLinear);
		System.out.println("predictArcTag = "+predictArcTag);
		System.out.println("argsReader = "+argsReader);
		
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
	
	public static void TrainModel(String dataPath) throws IOException, InvalidInputDataException {
		LinkedList<Feature> fl = new LinkedList<Feature>();
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
			
		//read sentences and calc & save configurations
		while(rd.hasNext()) {
			LinkedList<Configuration> cl = new LinkedList<Configuration>();
			if(ArcStandard) {
				ArcStandardDecoder.buildConfiguration(rd.readNext(), cl);
			}
			else {
				ArcEagerDecoder.buildConfiguration(rd.readNext(), cl);
			}
			
			for(Configuration cf : cl) {
				fl.add(cf.buildFeature());
			}
		}
		
		rd.close();
		if(!modelLibSVM) {
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
			System.out.println("Feature number: "+pc.getnFeature()+" ArcTag #: "+pc.getnTag());
			//write model to file
			File modelPath;
			if(argsReader)
				modelPath = new File(dataDir+"/dp.mapping");
			else
				modelPath = new File("/Users/zaa/Desktop/VIS hiwi/dep_parsing/dp.mapping");
			ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(modelPath)));
			oos.writeObject(pc);
			oos.close();
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
			System.out.println("Feature number: "+pc.getnFeature()+" ArcTag #: "+pc.getnTag());
			//write model to file
			File modelPath;
			if(argsReader)
				modelPath = new File(dataDir+"/dp.mapping");
			else
				modelPath = new File("/Users/zaa/Desktop/VIS hiwi/dep_parsing/dp.mapping");
			ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(modelPath)));
			oos.writeObject(pc);
			oos.close();
		}
	}
	
	public static void TestModel(String dataPath) throws FileNotFoundException, IOException, ClassNotFoundException {
		//read file
		String dataDir = null;
		if(argsReader)
			dataDir = new File(dataPath).getParent();
		Perceptron pc = null;
		LibClassifier lc = null;
		if(!modelLibSVM) {
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
			if(ArcStandard) {
				if(!modelLibSVM) 
					ArcStandardDecoder.doParsing(pc, st);
				else
					ArcStandardDecoder.doParsing(lc, st);
					
			}
			else {
				if(!modelLibSVM) 
					ArcEagerDecoder.doParsing(pc, st);
				else
					ArcEagerDecoder.doParsing(lc, st);
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
	}
}
