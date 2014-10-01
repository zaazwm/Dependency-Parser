
public class Word {
	private int _ID;
	private String _form;
	private String _lemma;
	private String _pos;
	private String _morph;
	private int _head;
	private String _rel;
	//to save information of each word/token
	public Word(int id, String form, String lemma, String pos, String morph, int head, String rel) {
		_ID=id;
		_form=form;
		_lemma=lemma;
		_pos=pos;
		_morph=morph;
		_head=head;
		_rel=rel;
	}
	
	public Word(int id, String form, String lemma, String pos, int head, String rel) {
		_ID=id;
		_form=form;
		_lemma=lemma;
		_pos=pos;
		_morph=null;
		_head=head;
		_rel=rel;
	}
	
	public Word(int id, String form, String lemma, String pos, int head) {
		_ID=id;
		_form=form;
		_lemma=lemma;
		_pos=pos;
		_morph=null;
		_head=head;
		_rel=null;
	}

	public int getID() {
		return _ID;
	}

	public void setID(int _ID) {
		this._ID = _ID;
	}

	public String getForm() {
		return _form;
	}

	public void setForm(String _form) {
		this._form = _form;
	}

	public String getLemma() {
		return _lemma;
	}

	public void setLemma(String _lemma) {
		this._lemma = _lemma;
	}

	public String getPos() {
		return _pos;
	}

	public void setPos(String _pos) {
		this._pos = _pos;
	}

	public String getMorph() {
		return _morph;
	}

	public void setMorph(String _morph) {
		this._morph = _morph;
	}

	public int getHead() {
		return _head;
	}

	public void setHead(int _head) {
		this._head = _head;
	}

	public String getRel() {
		return _rel;
	}

	public void setRel(String _rel) {
		this._rel = _rel;
	}
	
}
