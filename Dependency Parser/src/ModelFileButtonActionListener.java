import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;


public class ModelFileButtonActionListener implements ActionListener{
	private GUI gui;
	private JFileChooser dlg;
	
	public ModelFileButtonActionListener(GUI _gui, JFileChooser _dlg)
	{
		gui=_gui;
		dlg=_dlg;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		dlg.setDialogTitle("Choose a Model File");
		dlg.setDialogType(JFileChooser.SAVE_DIALOG);
		if(gui.getFilepath()!=null)
			dlg.setCurrentDirectory(new File(gui.getFilepath()));
		dlg.setSelectedFile(new File("dp.model"));
		int retVal = dlg.showDialog(gui.getMainwindow(), "Open");
		if (retVal == JFileChooser.APPROVE_OPTION) {
            File file = dlg.getSelectedFile();
            gui.setModelPath(file.getPath());
        } else {
        	gui.setModelPath(null);
        }
		
		if(gui instanceof GUI1)
			gui.getMainwindow().setTitle("Dependency Parser - Data: "+(gui.getFilepath()==null?"No Data File":gui.getFilepath()));
		if(gui instanceof GUI2 || gui instanceof GUIThesis)
			gui.getMainwindow().setTitle("Dependency Parser - Data: "+(gui.getFilepath()==null?"No Data File":gui.getFilepath())+" Model: "+(gui.getModelPath()==null?"No Model File":gui.getModelPath()));
		//System.out.println("Opened: "+gui.filepath);
	}
}
