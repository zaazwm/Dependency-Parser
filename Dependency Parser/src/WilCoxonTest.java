import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.stat.inference.WilcoxonSignedRankTest;

public class WilCoxonTest {
	
	private static boolean argsReader = true;
	
	public static double[] CompareResultInnerDev(String dataPath, int index) throws IOException {
		
		LinkedList<Double> xList = new LinkedList<Double>();
		
		//compare parsing result with golden label
		//XList
		String path;
		if(argsReader)
			path = dataPath;
		else
			path = new String("/Users/zaa/Desktop/VIS hiwi/dep_parsing/wsj_dev.conll06");
		String pathres;
		if(argsReader)
			pathres = dataPath+".result."+index;
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
		int fieldID=0;
		int fieldHead=6;
		//only compare head and its tag
		while((line = br.readLine())!=null && (liner = brr.readLine())!=null) {
			if(StringUtils.isBlank(line) || StringUtils.isBlank(liner)) {
				if(countfull>0) {
					Double red = ((double)countright/(double)countfull);
					xList.add(red);
				}
				
				countfull=0;
				countright=0;
				
				continue;
			}
			String[] fields = line.split("\t");
			String[] fieldsr = liner.split("\t");
			fields[fieldID]=fields[fieldID].substring(fields[fieldID].lastIndexOf('_')+1);
			fieldsr[fieldID]=fieldsr[fieldID].substring(fieldsr[fieldID].lastIndexOf('_')+1);
			if(!fields[fieldID].equals(fieldsr[fieldID]))
				System.out.println("Corpus not aligned, please check!");
			if(fields[fieldHead].equals(fieldsr[fieldHead])) {
				countright++;
			}
			countfull++;
		}
		
		br.close();
		brr.close();
		
		Double[] xArray = xList.toArray(new Double[0]);
		double[] ret = new double[xArray.length];
		int i=0;
		for(Double d : xArray) {
			ret[i++]=d;
		}
		return ret;
	}

	public static void main(String[] args) throws IOException {
		if(args.length<1)
			return;
		
		String datapath = args[0];
		if(datapath.charAt(0)=='\"' && datapath.charAt(datapath.length()-1)=='\"')
			datapath=datapath.substring(1,datapath.length()-1);
		
		double[] x = CompareResultInnerDev(datapath, 0);
		double[] y = CompareResultInnerDev(datapath, 1);
		
		if(x.length!=y.length) {
			System.out.println("Error: Arrays not matching!");
			return;
		}
		
		WilcoxonSignedRankTest test = new WilcoxonSignedRankTest();
		System.out.println("p-value: "+test.wilcoxonSignedRankTest(x, y, false));
	}

}
