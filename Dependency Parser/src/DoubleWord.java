
public class DoubleWord {
	public Word first;
	public Word second;
	
	DoubleWord(Word f, Word s) {
		first=f;
		second=s;
	}
	
	@Override
	public boolean equals(Object o) 
	{
	    if (o instanceof DoubleWord) 
	    {
	      DoubleWord c = (DoubleWord) o;
	      if ( this.first.getID()==c.first.getID() && this.second.getID()==c.second.getID() ) //whatever here
	         return true;
	    }
	    return false;
	}
}
