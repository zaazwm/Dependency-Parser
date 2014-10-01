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
		if(gui.filepath==null) {
			JOptionPane.showMessageDialog(gui.mainwindow, "No file selected!", "No File", JOptionPane.WARNING_MESSAGE);
			gui.filebtn.requestFocus();
			return;
		}
		File fl = new File(gui.filepath);
		if(!fl.exists()) {
			JOptionPane.showMessageDialog(gui.mainwindow, "Not valid file!", "Invalid File", JOptionPane.WARNING_MESSAGE);
			gui.filebtn.requestFocus();
		}
		else
			gui.run();
	}
}
