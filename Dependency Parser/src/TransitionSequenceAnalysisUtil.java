import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class TransitionSequenceAnalysisUtil {
	
	private static HashMap<String, MetaData> map = new HashMap<String, MetaData>();
	
	private static void StoreAnalysisData(ArrayList<TransitionSequence> list) {
		for(int i=0;i<list.size();i++) {
			TransitionSequence ts = list.get(i);
			if(ts.trans.equals("UN")) {
				//unigram
				MetaData innermap;
				if(map.containsKey(ts.trans)) {
					innermap = map.get(ts.trans);
				} else {
					innermap = new MetaData();
				}
				
				innermap.count++;
				
				if(innermap.posSMap.containsKey(ts.posS)) {
					innermap.posSMap.put(ts.posS, innermap.posSMap.get(ts.posS)+1);
				}
				else {
					innermap.posSMap.put(ts.posS, 1);
				}
				
				if(innermap.posBMap.containsKey(ts.posB)) {
					innermap.posBMap.put(ts.posB, innermap.posBMap.get(ts.posB)+1);
				}
				else {
					innermap.posBMap.put(ts.posB, 1);
				}
				
				if(innermap.posSBMap.containsKey(ts.posS+"_"+ts.posB)) {
					innermap.posSBMap.put(ts.posS+"_"+ts.posB, innermap.posSBMap.get(ts.posS+"_"+ts.posB)+1);
				}
				else {
					innermap.posSBMap.put(ts.posS+"_"+ts.posB, 1);
				}
				
				map.put(ts.trans, innermap);
			
				//bigram
				if(i<list.size()-1) {
					String t1 = list.get(i+1).trans;
					if(map.containsKey(ts.trans+"_"+t1)) {
						innermap = map.get(ts.trans+"_"+t1);
					} else {
						innermap = new MetaData();
					}
					
					innermap.count++;
					
					if(innermap.posSMap.containsKey(ts.posS)) {
						innermap.posSMap.put(ts.posS, innermap.posSMap.get(ts.posS)+1);
					}
					else {
						innermap.posSMap.put(ts.posS, 1);
					}
					
					if(innermap.posBMap.containsKey(ts.posB)) {
						innermap.posBMap.put(ts.posB, innermap.posBMap.get(ts.posB)+1);
					}
					else {
						innermap.posBMap.put(ts.posB, 1);
					}
					
					if(innermap.posSBMap.containsKey(ts.posS+"_"+ts.posB)) {
						innermap.posSBMap.put(ts.posS+"_"+ts.posB, innermap.posSBMap.get(ts.posS+"_"+ts.posB)+1);
					}
					else {
						innermap.posSBMap.put(ts.posS+"_"+ts.posB, 1);
					}
					
					map.put(ts.trans+"_"+t1, innermap);
					
				}
		
				//trigram
				if(i<list.size()-2) {
					String t1 = list.get(i+1).trans;
					String t2 = list.get(i+2).trans;
					
					if(map.containsKey(ts.trans+"_"+t1+"_"+t2)) {
						innermap = map.get(ts.trans+"_"+t1+"_"+t2);
					} else {
						innermap = new MetaData();
					}
					
					innermap.count++;
					
					if(innermap.posSMap.containsKey(ts.posS)) {
						innermap.posSMap.put(ts.posS, innermap.posSMap.get(ts.posS)+1);
					}
					else {
						innermap.posSMap.put(ts.posS, 1);
					}
					
					if(innermap.posBMap.containsKey(ts.posB)) {
						innermap.posBMap.put(ts.posB, innermap.posBMap.get(ts.posB)+1);
					}
					else {
						innermap.posBMap.put(ts.posB, 1);
					}
					
					if(innermap.posSBMap.containsKey(ts.posS+"_"+ts.posB)) {
						innermap.posSBMap.put(ts.posS+"_"+ts.posB, innermap.posSBMap.get(ts.posS+"_"+ts.posB)+1);
					}
					else {
						innermap.posSBMap.put(ts.posS+"_"+ts.posB, 1);
					}
					
					map.put(ts.trans+"_"+t1+"_"+t2, innermap);
				}
			}
		}
	}
	
	private static void PrintAnalysis() {
		ArrayList<String> transList = new ArrayList<String>(map.keySet());
		Collections.sort(transList, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				if(o1.length()!=o2.length()) {
					return o1.length()-o2.length();
				}
				return map.get(o2).count-map.get(o1).count;
			}
		});
		
		for(String t : transList) {
			final MetaData md = map.get(t);
			System.out.println("Trans: "+t+" = "+md.count);
			//posS
			ArrayList<String> posList = new ArrayList<String>(md.posSMap.keySet());
			Collections.sort(posList, new Comparator<String>() {

				@Override
				public int compare(String o1, String o2) {
					return md.posSMap.get(o2)-md.posSMap.get(o1);
				}
				
			});
			for(String p : posList) {
				System.out.println("\tposS: "+p+" = "+md.posSMap.get(p));
			}
			System.out.println();
			//posB
			posList = new ArrayList<String>(md.posBMap.keySet());
			Collections.sort(posList, new Comparator<String>() {

				@Override
				public int compare(String o1, String o2) {
					return md.posBMap.get(o2)-md.posBMap.get(o1);
				}
				
			});
			for(String p : posList) {
				System.out.println("\tposB: "+p+" = "+md.posBMap.get(p));
			}
			System.out.println();
			//posSB
			posList = new ArrayList<String>(md.posSBMap.keySet());
			Collections.sort(posList, new Comparator<String>() {

				@Override
				public int compare(String o1, String o2) {
					return md.posSBMap.get(o2)-md.posSBMap.get(o1);
				}
				
			});
			for(String p : posList) {
				System.out.println("\tposSB: "+p+" = "+md.posSBMap.get(p));
			}
			System.out.println();
			System.out.println();
		}
	}

	public static void main(String[] args) {
		if(args.length!=1 && args.length!=2) {
			System.out.println("Usage: [dev analysis file] [[output file]]");
			return;
		}
		
		String fileStr = args[0];
		File file = new File(fileStr);
		if(!file.exists()) {
			System.out.println("Usage: [dev analysis file]");
			return;
		}
		
		if(args.length==2) {
			String outputStr = args[1];
			File outfile = new File(outputStr);
			if(!outfile.exists()) {
				try {
					outfile.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			try {
				PrintStream out = new PrintStream(new FileOutputStream(outfile, true));
				System.setOut(out);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		try {
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader isr=new InputStreamReader(fis, "UTF-8");
			BufferedReader br=new BufferedReader(isr);
			
			String line = br.readLine();
			ArrayList<TransitionSequence> list = new ArrayList<TransitionSequence>();
			while((line = br.readLine())!=null) {
				String[] split = line.split("\t");
				if(split.length!=3) {
					StoreAnalysisData(list);
					list.clear();
					continue;
				}
				
				list.add(new TransitionSequence(split[0], split[1], split[2]));
			}
			
			br.close();
			
			PrintAnalysis();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}

class MetaData {
	public HashMap<String, Integer> posSMap;
	public HashMap<String, Integer> posBMap;
	public HashMap<String, Integer> posSBMap;
	public int count;
	
	public MetaData() {
		posSMap = new HashMap<String, Integer>();
		posBMap = new HashMap<String, Integer>();
		posSBMap = new HashMap<String, Integer>();
		count=0;
	}
}