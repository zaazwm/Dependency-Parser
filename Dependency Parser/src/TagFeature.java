
public class TagFeature {
	private String _tag;
	
	private String _hForm;
	private String _hPos;
	private String _tForm;
	private String _tPos;
	
	private String htForm;
	private String htPos;
	private String hfp;
	private String tfp;
	
	public static final int nFeature = 4+4;
	
	public TagFeature(String tag, String hForm, String hPos, String tForm, String tPos) {
		_tag=tag;
		
		_hForm=hForm;
		_hPos=hPos;
		_tForm=tForm;
		_tPos=tPos;
		
		this.composeFeature();
	}
	
	public TagFeature(String hForm, String hPos, String tForm, String tPos) {
		_tag=null;
		
		_hForm=hForm;
		_hPos=hPos;
		_tForm=tForm;
		_tPos=tPos;
		
		this.composeFeature();
	}
	
	private void composeFeature() {
		htForm=_hForm+"-"+_tForm;
		htPos=_hPos+"-"+_tPos;
		hfp=_hForm+"-"+_hPos;
		tfp=_tForm+"-"+_tPos;
	}
	
	public String getNameOf(int index) {
		if(index==0)
			return "hForm";
		if(index==1)
			return "hPos";
		if(index==2)
			return "tForm";
		if(index==3)
			return "tPos";
		if(index==4)
			return "htForm";
		if(index==5)
			return "htPos";
		if(index==6)
			return "hfp";
		if(index==7)
			return "tfp";
		return null;
	}
	
	public String getValueOf(int index) {
		if(index==0)
			return _hForm;
		if(index==1)
			return _hPos;
		if(index==2)
			return _tForm;
		if(index==3)
			return _tPos;
		if(index==4)
			return htForm;
		if(index==5)
			return htPos;
		if(index==6)
			return hfp;
		if(index==7)
			return tfp;
		return null;
	}

	public String get_tag() {
		return _tag;
	}

	public void set_tag(String _tag) {
		this._tag = _tag;
	}

	public String get_hForm() {
		return _hForm;
	}

	public void set_hForm(String _hForm) {
		this._hForm = _hForm;
	}

	public String get_hPos() {
		return _hPos;
	}

	public void set_hPos(String _hPos) {
		this._hPos = _hPos;
	}

	public String get_tForm() {
		return _tForm;
	}

	public void set_tForm(String _tForm) {
		this._tForm = _tForm;
	}

	public String get_tPos() {
		return _tPos;
	}

	public void set_tPos(String _tPos) {
		this._tPos = _tPos;
	}
	
	public String gethtForm() {
		return htForm;
	}
	
	public String gethtPos() {
		return htPos;
	}
	
	public String gethfp() {
		return hfp;
	}
	
	public String gettfp() {
		return tfp;
	}
}
