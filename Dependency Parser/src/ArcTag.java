import java.io.Serializable;


public class ArcTag implements Serializable{
	
	//supporting arc-tag
	private static final long serialVersionUID = 1340020485152212866L;
	private int transition;
	private String tag;
	
	public ArcTag(int trans, String tg) {
		transition = trans;
		tag=tg;
	}

	public int getTransition() {
		return transition;
	}

	public void setTransition(int transition) {
		this.transition = transition;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}
	
	@Override
	public boolean equals(Object o) 
	{
	    if (o instanceof ArcTag) 
	    {
	    	ArcTag c = (ArcTag) o;
	    	if(this.transition==c.getTransition()) {
	    		if(this.tag==null && c.getTag()==null) {
	    			return true;
	    		}
	    		else if(this.tag==null || c.getTag()==null) {
	    			return false;
	    		}
	    		else {
	    			return this.tag.equals(c.getTag());
	    		}
	    	}
	    	else
	    		return false;
	    }
	    return false;
	}
	
	@Override
	public int hashCode() {
		if(this.tag==null) {
			return this.transition;
		}
		else {
			return new String(this.tag+"_"+(new Integer(this.transition).toString())).hashCode();
		}
	}
}
