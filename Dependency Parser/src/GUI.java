import java.awt.Button;
import java.awt.Checkbox;

import javax.swing.JFrame;


public interface GUI {
	public JFrame getMainwindow();
	public String getFilepath();
	public void setFilepath(String fPath);
	public Checkbox getPredcb();
	public Button getFilebtn();
	public Button getFilebtn2();
	public String getModelPath();
	public void setModelPath(String mPath);
	
	public void buildwindows();
	public void run();
	public void processCombo(String selected);
}
