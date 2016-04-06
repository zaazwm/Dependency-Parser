import java.io.Serializable;
import java.util.Random;

public class DynamicPerceptron extends StaticPerceptron implements Serializable {

	private static final long serialVersionUID = 7849504885176176606L;
	
	protected Random rnd;
	
	protected static final double pIncorrect = 0.9D;
	protected static final int dynamicIteration = 1;
	
	public DynamicPerceptron(int nLabels) {
		super(nLabels);
		
		rnd = new Random(serialVersionUID);
	}
	
	@Override
	public int explore(int nCorrect, int nPredict) {
		return explore(nCorrect, nPredict, Integer.MAX_VALUE);
	}
	
	@Override
	public int explore(int nCorrect, int nPredict, int itNr) {
		if(nCorrect==nPredict)
			return nCorrect;
		
		if(rnd.nextDouble()<pIncorrect && itNr>dynamicIteration)
			return nPredict;
		else
			return nCorrect;
	}
}
