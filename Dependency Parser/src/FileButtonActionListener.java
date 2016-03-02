import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class FileButtonActionListener implements ActionListener{
	private GUI gui;
	private FileDialog dlg;
	
	public FileButtonActionListener(GUI _gui, FileDialog _dlg)
	{
		gui=_gui;
		dlg=_dlg;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		dlg.setVisible(true);
		gui.setFilepath(dlg.getDirectory()+dlg.getFile());
		if(dlg.getDirectory()==null || dlg.getFile()==null)
			gui.setFilepath(null);
		if(gui instanceof GUI1)
			gui.getMainwindow().setTitle("Dependency Parser - Data: "+(gui.getFilepath()==null?"No Data File":gui.getFilepath()));
		if(gui instanceof GUI2 || gui instanceof GUIThesis)
			gui.getMainwindow().setTitle("Dependency Parser - Data: "+(gui.getFilepath()==null?"No Data File":gui.getFilepath())+" Model: "+(gui.getModelPath()==null?"No Model File":gui.getModelPath()));
		//System.out.println("Opened: "+gui.filepath);
	}
}
