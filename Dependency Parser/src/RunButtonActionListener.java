import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JOptionPane;


public class RunButtonActionListener implements ActionListener{
	
	private GUI gui;

	public RunButtonActionListener(GUI _gui) {
		gui=_gui;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(gui.getFilepath()==null) {
			JOptionPane.showMessageDialog(gui.getMainwindow(), "No data file selected!", "No Data File", JOptionPane.WARNING_MESSAGE);
			gui.getFilebtn().requestFocus();
			return;
		}
		if(gui instanceof GUI2) {
			if(gui.getModelPath()==null) {
				JOptionPane.showMessageDialog(gui.getMainwindow(), "No model folder selected!", "No Model", JOptionPane.WARNING_MESSAGE);
				gui.getFilebtn2().requestFocus();
				return;
			}
			File ml = new File(gui.getModelPath());
			if(!ml.exists()) {
				JOptionPane.showMessageDialog(gui.getMainwindow(), "Invalid model folder!", "Invalid Model", JOptionPane.WARNING_MESSAGE);
				gui.getFilebtn2().requestFocus();
				return;
			}
		}
		if(gui instanceof GUIThesis) {
			if(gui.getModelPath()==null) {
				JOptionPane.showMessageDialog(gui.getMainwindow(), "No model file selected!", "No Model", JOptionPane.WARNING_MESSAGE);
				gui.getFilebtn2().requestFocus();
				return;
			}
			File ml = new File(gui.getModelPath());
			if(ApplicationControl.testMark && !ml.exists()) {
				JOptionPane.showMessageDialog(gui.getMainwindow(), "Invalid model file!", "Invalid Model", JOptionPane.WARNING_MESSAGE);
				gui.getFilebtn2().requestFocus();
				return;
			}
		}
		File fl = new File(gui.getFilepath());
		if(!fl.exists()) {
			JOptionPane.showMessageDialog(gui.getMainwindow(), "Invalid data file!", "Invalid Data File", JOptionPane.WARNING_MESSAGE);
			gui.getFilebtn().requestFocus();
		}
		else
			gui.run();
	}
}
