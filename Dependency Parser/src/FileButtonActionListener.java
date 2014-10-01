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
		gui.filepath=dlg.getDirectory()+dlg.getFile();
		if(dlg.getDirectory()==null || dlg.getFile()==null)
			gui.filepath=null;
		gui.mainwindow.setTitle("Dependency Parser - Data: "+(gui.filepath==null?"No Data File":gui.filepath));
		//System.out.println("Opened: "+gui.filepath);
	}
}