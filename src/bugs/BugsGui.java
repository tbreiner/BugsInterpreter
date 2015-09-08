package bugs;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import tree.Tree;

/**
 * GUI for Bugs language.
 * 
 * @author Dave Matuszek and Theresa Breiner
 * @version 2015
 */
public class BugsGui extends JFrame {
	private static final long serialVersionUID = 1L;
	View display;
	JSlider speedControl;
	int speed;
	JButton stepButton;
	JButton runButton;
	JButton pauseButton;
	JButton resetButton;

	Interpreter interpreter;
	Timer timer;
	boolean firstRun = true;
	// program text
	Tree<Token> programTree;

	/**
	 * GUI constructor.
	 */
	public BugsGui() {
		super();
		interpreter = new Interpreter();
		setSize(600, 600);
		setLayout(new BorderLayout());
		createAndInstallMenus();
		createDisplayPanel();
		createControlPanel();
		initializeButtons();
		setVisible(true);
		listenToWindowSize();
		setScales();
		programTree = null;
	}

	private void createAndInstallMenus() {
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		JMenu helpMenu = new JMenu("Help");
		JMenuItem quitMenuItem = new JMenuItem("Quit");
		JMenuItem helpMenuItem = new JMenuItem("Help");
		JMenuItem loadMenuItem = new JMenuItem("Load");

		menuBar.add(fileMenu);
		fileMenu.add(loadMenuItem);
		fileMenu.add(quitMenuItem);
		quitMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				quit();
			}
		});

		menuBar.add(helpMenu);
		helpMenu.add(helpMenuItem);
		helpMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				help();
			}
		});

		loadMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					load();
				} catch (IOException ioe) {
					readingError();
				}
			}
		});

		this.setJMenuBar(menuBar);
	}

	private void readingError() {
		JOptionPane.showMessageDialog(this, "Error reading lines from file.");
	}

	private void createDisplayPanel() {
		display = new View(interpreter);
		add(display, BorderLayout.CENTER);
	}

	private void createControlPanel() {
		JPanel controlPanel = new JPanel();

		addSpeedLabel(controlPanel);
		addSpeedControl(controlPanel);
		addStepButton(controlPanel);
		addRunButton(controlPanel);
		addPauseButton(controlPanel);
		addResetButton(controlPanel);

		add(controlPanel, BorderLayout.SOUTH);
	}

	private void addSpeedLabel(JPanel controlPanel) {
		controlPanel.add(new JLabel("Speed:"));
	}

	private void addSpeedControl(JPanel controlPanel) {
		speedControl = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 50);
		speed = 50;
		speedControl.setMajorTickSpacing(10);
		speedControl.setMinorTickSpacing(5);
		speedControl.setPaintTicks(true);
		speedControl.setPaintLabels(true);
		speedControl.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				resetSpeed(100 - speedControl.getValue());
			}
		});
		controlPanel.add(speedControl);
	}

	private void listenToWindowSize() {
		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent arg0) {
				setScales();
			}
		});
	}

	private void setScales() {
		display.setXScale(display.getWidth() / 100.0);
		display.setYScale(display.getHeight() / 100.0);
	}

	private void addStepButton(JPanel controlPanel) {
		stepButton = new JButton("Step");
		stepButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stepAnimation();
			}
		});
		controlPanel.add(stepButton);
	}

	private void addRunButton(JPanel controlPanel) {
		runButton = new JButton("Run");
		runButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				runAnimation();
			}
		});
		controlPanel.add(runButton);
	}

	private void addPauseButton(JPanel controlPanel) {
		pauseButton = new JButton("Pause");
		pauseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pauseAnimation();
			}
		});
		controlPanel.add(pauseButton);
	}

	private void addResetButton(JPanel controlPanel) {
		resetButton = new JButton("Reset");
		resetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				resetAnimation();
			}
		});
		controlPanel.add(resetButton);
	}

	private void initializeButtons() {
		stepButton.setEnabled(false);
		runButton.setEnabled(false);
		pauseButton.setEnabled(false);
		resetButton.setEnabled(false);
	}

	private void resetSpeed(int value) {
		speed = value;
		interpreter.mySpeed = (long) speed;
	}

	protected void stepAnimation() {

		stepButton.setEnabled(true);
		runButton.setEnabled(true);
		pauseButton.setEnabled(false);
		resetButton.setEnabled(true);
		interpreter.isStepped = true;
		interpreter.isPaused = false;
	}

	protected void runAnimation() {
		stepButton.setEnabled(true);
		runButton.setEnabled(false);
		pauseButton.setEnabled(true);
		resetButton.setEnabled(true);
		interpreter.isPaused = false;
		interpreter.isStepped = false;
	}

	protected void pauseAnimation() {
		stepButton.setEnabled(true);
		runButton.setEnabled(true);
		pauseButton.setEnabled(false);
		resetButton.setEnabled(true);
		interpreter.isPaused = true;
	}

	protected void resetAnimation() {
		stepButton.setEnabled(true);
		runButton.setEnabled(true);
		pauseButton.setEnabled(false);
		resetButton.setEnabled(false);
		speedControl.setValue(50);
		speed = 50;
		interpreter.okToContinue = false;
		setUpInterpreter();
	}

	protected void help() {
		// TODO Auto-generated method stub
		JOptionPane
				.showMessageDialog(
						this,
						"Load a file containing a Bugs program "
								+ "by selecting Load from the File menu. Then, hit Run and watch your Bugs come to life!");
	}

	protected void load() throws IOException {
		JFileChooser chooser = new JFileChooser();
		File selectedFile = null;
		chooser.setApproveButtonText("Open");
		int returnVal = chooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			selectedFile = chooser.getSelectedFile();
			// read file text into a string
			FileReader fr = null;
			try {
				fr = new FileReader(selectedFile);
			} catch (FileNotFoundException e) {
				// can't be not found since we just selected it
			}
			BufferedReader reader = new BufferedReader(fr);
			String text = "";
			String line;
			while ((line = reader.readLine()) != null) {
				text += line + '\n';
			}
			Parser parser = new Parser(text);
			boolean check = false;
			try {
				check = parser.isProgram();
			}
			catch (SyntaxException se) {
				JOptionPane.showMessageDialog(this,
						"The file does not contain a parsable Bug program. " + "Error: " + se);
				return;
			}
			if (!check) {
				JOptionPane.showMessageDialog(this,
						"The file does not contain a parsable Bug program.");
				return;
			}
			// set the program tree to be the tree made by the parser on the
			// stack
			programTree = parser.stack.pop();
			setUpInterpreter();
			speedControl.setValue(50);
			speed = 50;
			pauseAnimation();
		}
	}

	/**
	 * Helper method for setting and resetting Interpreter
	 */
	protected void setUpInterpreter() {
		display.timer.stop();
		interpreter = new Interpreter();
		interpreter.program = programTree;
		interpreter.setUp();
		interpreter.mySpeed = (long) speed;
		interpreter.start();
		display.interpreter = interpreter;
		display.timer.start();

	}

	protected void quit() {
		System.exit(0);
	}

	/**
	 * Runs the GUI program by creating a new instance of BugsGui.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		new BugsGui();
	}

}