import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;


public class ComboBoxActionListener implements ActionListener{
	
	private GUI gui;
	
	public ComboBoxActionListener(GUI _gui) {
		gui = _gui;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		@SuppressWarnings("rawtypes")
		JComboBox cb = (JComboBox)e.getSource();
		String selected = (String)cb.getSelectedItem();
		gui.processCombo(selected);
		//System.out.println("ComboBox Selected: "+selected);
	}

}
