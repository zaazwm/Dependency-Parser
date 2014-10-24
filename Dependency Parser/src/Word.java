import java.util.LinkedList;


public class Word {
	private int _ID;
	private String _form;
	private String _lemma;
	private String _pos;
	private String _morph;
	private int _head;
	private String _rel;
	private int _projectiveID;
	private LinkedList<Integer> _children;
	private int _MPCID;
	private int _MPChead;
	private String _tag;
	//to save information of each word/token
	public Word(int id, String form, String lemma, String pos, String morph, int head, String rel) {
		_ID=id;
		_form=form;
		_lemma=lemma;
		_pos=pos;
		_morph=morph;
		_head=head;
		_rel=rel;
		_tag=null;
		_children=new LinkedList<Integer>();
		_MPCID=-1;
		_MPChead=-1;
	}
	
	public Word(int id, String form, String lemma, String pos, int head, String rel) {
		_ID=id;
		_form=form;
		_lemma=lemma;
		_pos=pos;
		_morph=null;
		_head=head;
		_rel=rel;
		_tag=null;
		_children=new LinkedList<Integer>();
		_MPCID=-1;
		_MPChead=-1;
	}
	
	public Word(int id, String form, String lemma, String pos, int head) {
		_ID=id;
		_form=form;
		_lemma=lemma;
		_pos=pos;
		_morph=null;
		_head=head;
		_rel=null;
		_tag=null;
		_children=new LinkedList<Integer>();
		_MPCID=-1;
		_MPChead=-1;
	}
	
	public Word(int id, String form, String lemma, String pos, int head, String tag, boolean marker) {
		_ID=id;
		_form=form;
		_lemma=lemma;
		_pos=pos;
		_morph=null;
		_head=head;
		_rel=null;
		_tag=tag;
		_children=new LinkedList<Integer>();
		_MPCID=-1;
		_MPChead=-1;
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

	public int getProjectiveID() {
		return _projectiveID;
	}

	public void setProjectiveID(int _projectiveID) {
		this._projectiveID = _projectiveID;
	}

	public LinkedList<Integer> getChildren() {
		return _children;
	}

	public void addChildren(Integer cid) {
		this._children.add(cid);
	}

	public int getMPCID() {
		return _MPCID;
	}

	public void setMPCID(int MPCID) {
		this._MPCID = MPCID;
	}

	public int getMPChead() {
		return _MPChead;
	}

	public void setMPChead(int MPChead) {
		this._MPChead = MPChead;
	}

	public String getTag() {
		return _tag;
	}

	public void setTag(String tag) {
		this._tag = tag;
	}
	
}
