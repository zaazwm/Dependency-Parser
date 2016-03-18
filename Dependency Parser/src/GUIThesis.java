import java.awt.Button;
import java.awt.Checkbox;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.io.IOException;

import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.bwaldvogel.liblinear.InvalidInputDataException;

public class GUIThesis implements GUI {

	public JFrame mainwindow;
	public String filepath = null;
	public String modelpath = null;
	public Checkbox predcb;
	public JComboBox<String> training;
	public JComboBox<String> decoder;
	public JComboBox<String> afterend;
	public Button filebtn;
	public Button filebtn2;
	
	public static final String[] decoders = {"ArcEager", "ArcEager+NM", "ArcEager+NM+Unshift", "ArcEager+SglUnshift"};
	public static final String[] trainings = {"StaticPerceptron", "DynamicPerceptron"};
	public static final String[] methods = {"Train", "Dev", "Test"};
	public static final String[] afterends = {"Ignore", "All Root", "All RightArc", "All LeftArc", "By Oracle"};

	public static void main(String[] args) {
		//main entrance for GUI program
		GUIThesis gui = new GUIThesis();
		ApplicationControl.ArcEagerOnline=true;
		gui.buildwindows();
	}

	@Override
	public void buildwindows() {
		//build the main window
		mainwindow = new JFrame();
		mainwindow.setTitle("Dependency Parser");
		mainwindow.setSize(650, 95);
		//mainwindow.setLocation(100, 100);
		mainwindow.setLocationRelativeTo(null);
		mainwindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel panel = new JPanel(new FlowLayout());
		panel.setSize(mainwindow.getSize());
		
		FileDialog dlg = new FileDialog(mainwindow, "Open a Data File");
		filebtn = new Button("Open Data");
		panel.add(filebtn);
		filebtn.addActionListener(new FileButtonActionListener(GUIThesis.this, dlg));
		
		JFileChooser dlg2 = new JFileChooser();
		filebtn2 = new Button("Open Model");
		panel.add(filebtn2);
		filebtn2.addActionListener(new ModelFileButtonActionListener(GUIThesis.this, dlg2));
		
		decoder = new JComboBox<String>(decoders);
		training = new JComboBox<String>(trainings);
		JComboBox<String> method = new JComboBox<String>(methods);
		afterend = new JComboBox<String>(afterends);
		decoder.addActionListener(new ComboBoxActionListener(GUIThesis.this));
		training.addActionListener(new ComboBoxActionListener(GUIThesis.this));
		method.addActionListener(new ComboBoxActionListener(GUIThesis.this));
		afterend.addActionListener(new ComboBoxActionListener(GUIThesis.this));
		method.setVisible(true);
		decoder.setVisible(true);
		training.setVisible(true);
		afterend.setVisible(true);
		JLabel dlbl= new JLabel("Decoder");
		JLabel tlbl= new JLabel("Training Model");
		JLabel mlbl= new JLabel("Usage");
		JLabel albl= new JLabel("After End");
		panel.add(mlbl);
		panel.add(method);
		panel.add(dlbl);
		panel.add(decoder);
		panel.add(tlbl);
		panel.add(training);
		panel.add(albl);
		panel.add(afterend);
		
		predcb = new Checkbox("ArcTags", false);
		predcb.addItemListener(new CheckBoxItemListener());
		panel.add(predcb);
		predcb.setVisible(false);
		
		//default values
		decoder.setSelectedIndex(2);
		training.setSelectedIndex(1);
		method.setSelectedIndex(0);
		afterend.setSelectedIndex(0);
		predcb.setState(false);
		
		Button runbtn = new Button("Run");
		runbtn.addActionListener(new RunButtonActionListener(GUIThesis.this));
		panel.add(runbtn);
		runbtn.setVisible(true);
		
		panel.setVisible(true);
		mainwindow.add(panel);
		mainwindow.setVisible(true);
	}
	
	@Override
	public void run() {
		//run if clicked 'run' button
		System.out.println("Arguments Readed: ");
		
		System.out.println("testMark = "+ApplicationControl.testMark);
		System.out.println("ArcStandard = "+ApplicationControl.ArcStandard);
		System.out.println("AvePerceptron = "+ApplicationControl.AvePerceptron);
		System.out.println("devMark = "+ApplicationControl.devMark);
		System.out.println("smallTrainData = "+ApplicationControl.smallTrainData);
		System.out.println("modelLibSVM = "+ApplicationControl.modelLibSVM);
		System.out.println("modelLibLinear = "+ApplicationControl.modelLibLinear);
		System.out.println("predictArcTag = "+ApplicationControl.predictArcTag);
		System.out.println("newPredArcTag = "+ApplicationControl.newPredArcTag);
		System.out.println("argsReader = "+ApplicationControl.argsReader);
		System.out.println("ArcEagerOnline = "+ApplicationControl.ArcEagerOnline);
		System.out.println("OnlineStaticPerceptron = "+ApplicationControl.OnlineStaticPerceptron);
		System.out.println("OnlineDynamicPerceptron = "+ApplicationControl.OnlineDynamicPerceptron);
		System.out.println("UnshiftEnabled = "+ApplicationControl.UnshiftEnabled);
		System.out.println("NonMonotonic = "+ApplicationControl.NonMonotonic);
		System.out.println("SingleClassReUs = "+ApplicationControl.SingleClassReUs);
		System.out.println("AfterEndSolution = "+ApplicationControl.AfterEndSolution);
		System.out.println("filePath = "+filepath);
		System.out.println("modelPath = "+modelpath);
		
		
		try {
			ApplicationControl.run(filepath, modelpath, true);
		} catch (ClassNotFoundException | IOException | InvalidInputDataException e) {
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void processCombo(String selected) {
		//deal with combo boxes, change the parameters
		ApplicationControl.AfterEndSolution=afterend.getSelectedIndex();
		//if(selected.equals(decoders[3])) {
		if(selected.equals("ArcEager+SglUnshift")) {
			ApplicationControl.ArcEagerOnline=true;
			ApplicationControl.UnshiftEnabled=true;
			ApplicationControl.OnlineDynamicPerceptron=true;
			ApplicationControl.OnlineStaticPerceptron=false;
			ApplicationControl.NonMonotonic=true;
			ApplicationControl.SingleClassReUs=true;
			training.setEnabled(false);
			training.setSelectedIndex(1);
			return;
		}
		//if(selected.equals(decoders[2])) {
		if(selected.equals("ArcEager+NM+Unshift")) {
			ApplicationControl.ArcEagerOnline=true;
			ApplicationControl.UnshiftEnabled=true;
			ApplicationControl.OnlineDynamicPerceptron=true;
			ApplicationControl.OnlineStaticPerceptron=false;
			ApplicationControl.NonMonotonic=true;
			ApplicationControl.SingleClassReUs=false;
			training.setEnabled(false);
			training.setSelectedIndex(1);
			return;
		}
		//if(selected.equals(decoders[1])) {
		if(selected.equals("ArcEager+NM")) {
			ApplicationControl.ArcEagerOnline=true;
			ApplicationControl.UnshiftEnabled=false;
			ApplicationControl.OnlineDynamicPerceptron=true;
			ApplicationControl.OnlineStaticPerceptron=false;
			ApplicationControl.NonMonotonic=true;
			ApplicationControl.SingleClassReUs=false;
			training.setEnabled(false);
			training.setSelectedIndex(1);
			return;
		}
		//if(selected.equals(decoders[0])) {
		if(selected.equals("ArcEager")) {
			ApplicationControl.ArcEagerOnline=true;
			ApplicationControl.UnshiftEnabled=false;
			ApplicationControl.NonMonotonic=false;
			ApplicationControl.SingleClassReUs=false;
			if(training.getSelectedIndex()==0) {
				ApplicationControl.OnlineStaticPerceptron=true;
				ApplicationControl.OnlineDynamicPerceptron=false;
			}
			else if(training.getSelectedIndex()==1) {
				ApplicationControl.OnlineStaticPerceptron=false;
				ApplicationControl.OnlineDynamicPerceptron=true;
			}
			training.setEnabled(true);
			return;
		}
		//if(selected.equals(trainings[0])) {
		if(selected.equals("StaticPerceptron")) {
			ApplicationControl.UnshiftEnabled=false;
			ApplicationControl.OnlineDynamicPerceptron=false;
			ApplicationControl.OnlineStaticPerceptron=true;
			decoder.setSelectedIndex(0);
			decoder.setEnabled(false);
			return;
		}
		//if(selected.equals(trainings[1])) {
		if(selected.equals("DynamicPerceptron")) {
			ApplicationControl.OnlineStaticPerceptron=false;
			ApplicationControl.OnlineDynamicPerceptron=true;
			if(decoder.getSelectedIndex()<=1) {
				ApplicationControl.UnshiftEnabled=false;
			}
			else if(decoder.getSelectedIndex()==2) {
				ApplicationControl.UnshiftEnabled=true;
			}
			decoder.setEnabled(true);
			return;
		}
		//if(selected.equals(methods[0])) {
		if(selected.equals("Train")) {
			ApplicationControl.testMark=false;
			ApplicationControl.argsReader=true;
			afterend.setEnabled(false);
			return;
		}
		//if(selected.equals(methods[1])) {
		if(selected.equals("Dev")) {
			ApplicationControl.testMark=true;
			ApplicationControl.argsReader=true;
			ApplicationControl.devMark=true;
			afterend.setEnabled(true);
			return;
		}
		//if(selected.equals(methods[2])) {
		if(selected.equals("Test")) {
			ApplicationControl.testMark=true;
			ApplicationControl.argsReader=true;
			ApplicationControl.devMark=false;
			afterend.setEnabled(true);
			return;
		}
	}

	@Override
	public JFrame getMainwindow() {
		return this.mainwindow;
	}

	@Override
	public String getFilepath() {
		return this.filepath;
	}

	@Override
	public Checkbox getPredcb() {
		return this.predcb;
	}

	@Override
	public Button getFilebtn() {
		return this.filebtn;
	}

	@Override
	public String getModelPath() {
		return this.modelpath;
	}

	@Override
	public void setFilepath(String fPath) {
		filepath=fPath;
	}

	@Override
	public void setModelPath(String mPath) {
		modelpath=mPath;
	}

	@Override
	public Button getFilebtn2() {
		return this.filebtn2;
	}

}
