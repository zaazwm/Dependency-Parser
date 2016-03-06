import java.awt.Button;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class NonprojectiveTester {
	
	protected static String filePath = null;
	
	protected static boolean test(Sentence st) {
		for(Word w : st.getWdList()) {
			int begin = Math.min(w.getHead(), w.getID());
			int end = Math.max(w.getHead(), w.getID());
			
			for(Word wd : st.getWdList()) {
				if(wd.getID()<=w.getID())
					continue;
				
				int bg = Math.min(wd.getHead(), wd.getID());
				int ed = Math.max(wd.getHead(), wd.getID());
				
				if(begin<bg && end>bg && end<ed)
					return false;
				else if(begin>bg && begin<ed && end>ed)
					return false;
			}
		}
		return true;
	}
	
	protected static void run() throws IOException {
		if(filePath==null) {
			return;
		}
		
		Reader rd = new Reader(filePath);
		int stcount = 0;
		while(rd.hasNext()) {
			stcount++;
			Sentence st = rd.readNext();
			if(!NonprojectiveTester.test(st)) {
				System.out.println("Non-projective Sentence Found!");
				System.out.println("Sentence "+stcount+": "+st.toString());
				break;
			}
		}
		System.out.println("Congradulations! Non-projective Sentence NOT Found!");
	}

	public static void main(String[] args) {
		final JFrame mainwindow = new JFrame();
		mainwindow.setTitle("Non-projective Tester");
		mainwindow.setSize(250,60);
		mainwindow.setLocationRelativeTo(null);
		mainwindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel panel = new JPanel(new FlowLayout());
		panel.setSize(mainwindow.getSize());
		final FileDialog dlg = new FileDialog(mainwindow, "Open a Data File");
		Button filebtn = new Button("Open Data");
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
		
		Button filebtn2 = new Button("Run Test");
		panel.add(filebtn2);
		filebtn2.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(NonprojectiveTester.filePath==null)
					JOptionPane.showMessageDialog(mainwindow, "No data file selected!", "No Data File", JOptionPane.WARNING_MESSAGE);
				else
					try {
						NonprojectiveTester.run();
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
