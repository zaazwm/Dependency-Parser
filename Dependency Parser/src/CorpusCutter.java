import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class CorpusCutter {
protected static String filePath = null;
		
	protected static void run(String sizeText) throws IOException {
		if(filePath==null) {
			return;
		}
		
		int size = 0;
		try {
			size = Integer.parseInt(sizeText);
		} catch (NumberFormatException e) {
			return;
		}
		
		ApplicationControl.predictArcTag = true;
		Reader rd = new Reader(filePath);
		Writer wt = new Writer(filePath+".cut");
		int stcount = 0;
		while(rd.hasNext()) {
			stcount++;
			Sentence st = rd.readNext();
			wt.write(st);
			
			if(stcount>=size) {
				break;
			}
		}
		wt.close();
		System.out.println("Corpus cut to "+size+" sentence(s)");
	}

	public static void main(String[] args) {
		final JFrame mainwindow = new JFrame();
		mainwindow.setTitle("Corpus Cutter");
		mainwindow.setSize(350,60);
		mainwindow.setLocationRelativeTo(null);
		mainwindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel panel = new JPanel(new FlowLayout());
		panel.setSize(mainwindow.getSize());
		final FileDialog dlg = new FileDialog(mainwindow, "Open a Data File");
		JButton filebtn = new JButton("Open Data");
		panel.add(filebtn);
		filebtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				dlg.setVisible(true);
				filePath = dlg.getDirectory()+dlg.getFile();
				if(dlg.getDirectory()==null || dlg.getFile()==null)
					filePath = null;
			}
		});
		
		JLabel countLabel = new JLabel("Size:");
		panel.add(countLabel);
		
		final JTextField countText = new JTextField(5);
		panel.add(countText);
		countText.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(CorpusCutter.filePath==null)
					JOptionPane.showMessageDialog(mainwindow, "No data file selected!", "No Data File", JOptionPane.WARNING_MESSAGE);
				else
					try {
						CorpusCutter.run(countText.getText());
					} catch (IOException e1) {
						e1.printStackTrace();
					}
			}
			
		});
		
		JButton filebtn2 = new JButton("Cut");
		panel.add(filebtn2);
		filebtn2.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(CorpusCutter.filePath==null)
					JOptionPane.showMessageDialog(mainwindow, "No data file selected!", "No Data File", JOptionPane.WARNING_MESSAGE);
				else
					try {
						CorpusCutter.run(countText.getText());
					} catch (IOException e1) {
						e1.printStackTrace();
					}
			}
			
		});
		panel.setVisible(true);
		mainwindow.add(panel);
		mainwindow.setVisible(true);
	}
}
