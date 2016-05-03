import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;

import org.apache.commons.lang3.StringUtils;

public class Reader {
	private String path;
	private FileInputStream fis;
	private InputStreamReader isr;
	private BufferedReader br;
	
	private Sentence tst;
	
	private static final int fieldID=0;
	private static final int fieldForm=1;
	private static final int fieldLemma=2;
	private static final int fieldPos=3;
	private static final int fieldMorph=5;
	private static final int fieldHead=6;
	private static final int fieldRel=7;
	
	public Reader(String path) throws FileNotFoundException, UnsupportedEncodingException {
		this.path=path;
		fis=new FileInputStream(this.path);
		isr=new InputStreamReader(fis, "UTF-8");
		br=new BufferedReader(isr);
	}
	
	public Sentence readNext() throws IOException {
		String line;
		LinkedList<Word> wl = new LinkedList<Word>();
		while((line = br.readLine())!=null) {
			if(StringUtils.isBlank(line)) {
				break;
			}
			String[] fields = line.split("\t");
			fields[fieldID]=fields[fieldID].substring(fields[fieldID].lastIndexOf('_')+1);
			@SuppressWarnings("unused")
			String morph=fields[fieldMorph];  //need to parse the content before use
			if(ApplicationControl.predictArcTag || ApplicationControl.NonMonotonic) {
				//only read id, form, lemma, pos, head information, arc-tag
				wl.add(new Word(Integer.parseInt(fields[fieldID]),fields[fieldForm],fields[fieldLemma],fields[fieldPos],Integer.parseInt(fields[fieldHead]),fields[fieldRel]));
			}
			else {
				//only read id, form, lemma, pos, head information (or a special tag if will predict)
				if(ApplicationControl.newPredArcTag)
					wl.add(new Word(Integer.parseInt(fields[fieldID]),fields[fieldForm],fields[fieldLemma],fields[fieldPos],Integer.parseInt(fields[fieldHead]),fields[fieldRel],true));
				else
					wl.add(new Word(Integer.parseInt(fields[fieldID]),fields[fieldForm],fields[fieldLemma],fields[fieldPos],Integer.parseInt(fields[fieldHead])));
				
			}
		}
		
		tst = new Sentence(wl, true);
		
		return new Sentence(wl);
	}
	
	public Sentence readLast() {
		//read the cached sentence
		return tst;
	}
	
	public Sentence readNextTest() throws IOException {
		String line;
		LinkedList<Word> wl = new LinkedList<Word>();
		while((line = br.readLine())!=null) {
			if(StringUtils.isBlank(line)) {
				break;
			}
			String[] fields = line.split("\t");
			fields[fieldID]=fields[fieldID].substring(fields[fieldID].lastIndexOf('_')+1);
			if(ApplicationControl.predictArcTag || ApplicationControl.NonMonotonic) {
				wl.add(new Word(Integer.parseInt(fields[fieldID]),fields[fieldForm],fields[fieldLemma],fields[fieldPos],-1,fields[fieldRel]));
				if(ApplicationControl.RelCount && ApplicationControl.devMark)
					wl.peekLast().setHead(Integer.parseInt(fields[fieldHead]), true);
			}
			else {
				if(ApplicationControl.newPredArcTag)
					wl.add(new Word(Integer.parseInt(fields[fieldID]),fields[fieldForm],fields[fieldLemma],fields[fieldPos],Integer.parseInt(fields[fieldHead]),fields[fieldRel],true));
				else
					wl.add(new Word(Integer.parseInt(fields[fieldID]),fields[fieldForm],fields[fieldLemma],fields[fieldPos],-1));
			}
		}
		
		return new Sentence(wl);
	}
	
	public boolean hasNext() throws IOException {
		return br.ready();
	}
	
	public void close() throws IOException {
		br.close();
		isr.close();
		fis.close();
	}
	
	public static void main(String[] args) throws IOException {
		//test entry to reader & writer
		//--read 10 sentences from file and write to another file
		Reader rd = new Reader("/Users/zaa/Desktop/VIS hiwi/dep_parsing/wsj_train.only-projective.first-5k.conll06");
		Writer wt = new Writer("/Users/zaa/Desktop/dp.test.conll06");
		int count=0;
		LinkedList<Configuration> cl = new LinkedList<Configuration>();
		LinkedList<TagFeature> tfl = new LinkedList<TagFeature>();
		while(rd.hasNext()) {
			count++;
			Sentence st = rd.readNext();
			ArcEagerDecoder.buildConfiguration(st, cl, tfl);
			int co=1;
			for(Configuration cf : cl) {
				System.out.println("state#"+co+" s: "+(cf.getState().getStack().isEmpty()?"":cf.getState().getStack().peekLast().getForm())
						+" b: "+(cf.getState().getBuffer().isEmpty()?"":cf.getState().getBuffer().peekFirst().getForm())
						+" cf: "+cf.getConfToString());
				co++;
			}
			
			wt.write(st);
			
			cl.clear();
			if(count>10)
				break;
			else
				System.out.println("\n----------------------------------------\n");
		}
		
		rd.close();
		wt.close();
	}
}
