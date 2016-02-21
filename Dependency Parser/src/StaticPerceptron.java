import java.io.Serializable;

public class StaticPerceptron extends DynamicPerceptron implements Serializable {

	private static final long serialVersionUID = -2596623044233855453L;

	public StaticPerceptron(int nLabels) {
		super(nLabels);
	}

	@Override
	public int explore(int nCorrect, int nPredict) {
		return nCorrect;
	}
	
	@Override
	public int explore(int nCorrect, int nPredict, int itNr) {
		return nCorrect;
	}
}
