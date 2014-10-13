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
			JOptionPane.showMessageDialog(gui.getMainwindow(), "No file selected!", "No File", JOptionPane.WARNING_MESSAGE);
			gui.getFilebtn().requestFocus();
			return;
		}
		if(gui instanceof GUI2) {
			File ml = new File(gui.getFilepath());
			if(!ml.exists()) {
				JOptionPane.showMessageDialog(gui.getMainwindow(), "Not valid model folder!", "Invalid Model", JOptionPane.WARNING_MESSAGE);
				gui.getFilebtn2().requestFocus();
				return;
			}
		}
		File fl = new File(gui.getFilepath());
		if(!fl.exists()) {
			JOptionPane.showMessageDialog(gui.getMainwindow(), "Not valid file!", "Invalid File", JOptionPane.WARNING_MESSAGE);
			gui.getFilebtn().requestFocus();
		}
		else
			gui.run();
	}
}
