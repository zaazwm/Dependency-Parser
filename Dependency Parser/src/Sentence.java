import java.util.LinkedList;


public class Sentence {
	private LinkedList<Word> wdList;
	//to save words in a sentence
	public Sentence(LinkedList<Word> wl) {
		wdList=wl;
		wdList.addFirst(new Word(0,"ROOT","ROOT","ROOT",-1));
	}
	
	@SuppressWarnings("unchecked")
	public Sentence(LinkedList<Word> wl, boolean t) {
		wdList=(LinkedList<Word>) wl.clone();
	}

	public LinkedList<Word> getWdList() {
		return wdList;
	}

	public void setWdList(LinkedList<Word> wdList) {
		this.wdList = wdList;
	}
	
	public Sentence clone() {
		Sentence ret = new Sentence(this.getWdList(),true);
		return ret;
	}
}
