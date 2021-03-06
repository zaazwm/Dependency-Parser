import java.awt.Button;
import java.awt.Checkbox;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.io.IOException;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.bwaldvogel.liblinear.InvalidInputDataException;

// A simple GUI saving/reading model in the same folder of the data file.

public class GUI1 implements GUI{
	
	public JFrame mainwindow;
	public String filepath = null;
	public String modelpath = null;
	public Checkbox predcb;
	public JComboBox<String> training;
	public Button filebtn;
	
	public static final String[] decoders = {"ArcStandard", "ArcEager", "ArcEagerOnline"};
	public static final String[] trainings = {"Perceptron", "AvePerceptron", "LibSVM", "LibLinear"};
	public static final String[] methods = {"Train", "Dev", "Test"};

	public static void main(String[] args) {
		//main entrance for GUI program
		GUI1 gui = new GUI1();
		gui.buildwindows();
	}

	@Override
	public void buildwindows() {
		//build the main window
		mainwindow = new JFrame();
		mainwindow.setTitle("Dependency Parser");
		mainwindow.setSize(900, 60);
		//mainwindow.setLocation(100, 100);
		mainwindow.setLocationRelativeTo(null);
		mainwindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel panel = new JPanel(new FlowLayout());
		panel.setSize(mainwindow.getSize());
		
		FileDialog dlg = new FileDialog(mainwindow, "Open");
		filebtn = new Button("Open File");
		panel.add(filebtn);
		filebtn.addActionListener(new FileButtonActionListener(GUI1.this, dlg));
		
		JComboBox<String> decoder = new JComboBox<String>(decoders);
		training = new JComboBox<String>(trainings);
		JComboBox<String> method = new JComboBox<String>(methods);
		decoder.addActionListener(new ComboBoxActionListener(GUI1.this));
		training.addActionListener(new ComboBoxActionListener(GUI1.this));
		method.addActionListener(new ComboBoxActionListener(GUI1.this));
		method.setVisible(true);
		decoder.setVisible(true);
		training.setVisible(true);
		JLabel dlbl= new JLabel("Decoder");
		JLabel tlbl= new JLabel("Training Model");
		JLabel mlbl= new JLabel("Usage");
		panel.add(mlbl);
		panel.add(method);
		panel.add(dlbl);
		panel.add(decoder);
		panel.add(tlbl);
		panel.add(training);
		
		predcb = new Checkbox("ArcTags", true);
		predcb.addItemListener(new CheckBoxItemListener());
		panel.add(predcb);
		predcb.setVisible(true);
		
		decoder.setSelectedIndex(0);
		training.setSelectedIndex(3);
		method.setSelectedIndex(0);
		predcb.setState(false);
		
		Button runbtn = new Button("Run");
		runbtn.addActionListener(new RunButtonActionListener(GUI1.this));
		panel.add(runbtn);
		runbtn.setVisible(true);
		
		panel.setVisible(true);
		mainwindow.add(panel);
		mainwindow.setVisible(true);
		
		mainwindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
		System.out.println("filePath = "+filepath);
		
		
		try {
			ApplicationControl.run(filepath);
		} catch (ClassNotFoundException | IOException | InvalidInputDataException e) {
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void processCombo(String selected) {
		//deal with combo boxes, change the parameters
		
		//if(selected.equals(decoders[0])) {
		if(selected.equals("ArcStandard")) {
			ApplicationControl.ArcStandard=true;
			training.setEnabled(true);
			return;
		}
		//if(selected.equals(decoders[1])) {
		if(selected.equals("ArcEager")) {
			ApplicationControl.ArcStandard=false;
			training.setEnabled(true);
			return;
		}
		//if(selected.equals(trainings[0])) {
		if(selected.equals("Perceptron")) {
			ApplicationControl.AvePerceptron=false;
			ApplicationControl.modelLibSVM=false;
			predcb.setEnabled(false);
			predcb.setState(false);
			return;
		}
		//if(selected.equals(decoders[2])) {
		if(selected.equals("ArcEagerOnline")) {
			ApplicationControl.ArcEagerOnline=true;
			ApplicationControl.AvePerceptron=false;
			ApplicationControl.modelLibSVM=false;
			predcb.setEnabled(false);
			predcb.setState(false);
			training.setSelectedIndex(0);
			training.setEnabled(false);
			return;
		}
		//if(selected.equals(trainings[1])) {
		if(selected.equals("AvePerceptron")) {
			ApplicationControl.AvePerceptron=true;
			ApplicationControl.modelLibSVM=false;
			predcb.setEnabled(false);
			predcb.setState(false);
			return;
		}
		//if(selected.equals(trainings[2])) {
		if(selected.equals("LibSVM")) {
			ApplicationControl.modelLibLinear=false;
			ApplicationControl.modelLibSVM=true;
			predcb.setEnabled(true);
			return;
		}
		//if(selected.equals(trainings[3])) {
		if(selected.equals("LibLinear")) {
			ApplicationControl.modelLibLinear=true;
			ApplicationControl.modelLibSVM=true;
			predcb.setEnabled(true);
			return;
		}
		//if(selected.equals(methods[0])) {
		if(selected.equals("Train")) {
			ApplicationControl.testMark=false;
			ApplicationControl.argsReader=true;
			return;
		}
		//if(selected.equals(methods[1])) {
		if(selected.equals("Dev")) {
			ApplicationControl.testMark=true;
			ApplicationControl.argsReader=true;
			ApplicationControl.devMark=true;
			return;
		}
		//if(selected.equals(methods[2])) {
		if(selected.equals("Test")) {
			ApplicationControl.testMark=true;
			ApplicationControl.argsReader=true;
			ApplicationControl.devMark=false;
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
		return null;
	}
}
