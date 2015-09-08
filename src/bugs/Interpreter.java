package bugs;

import java.util.ArrayList;
import java.util.HashMap;

import tree.Tree;

/**
 * Class to serve as the model and coordinator of all the Bugs in 
 * the program. Once the BugsGUI Controller has set the interpreter's
 * program to a parsed tree and starts the interpreter, it oversees
 * the work of the bugs by ensuring that each bug executes one action at a time
 * in turn. Once all of the Bugs have finished their programs, the interpreter is also finished.
 * @author theresabreiner
 *
 */
/**
 * @author theresabreiner
 *
 */
public class Interpreter extends Thread{
	public Tree<Token> program;
	public Tree<Token> allbugs;
	public ArrayList<Bug> bugs;
	public HashMap<String, Bug> bugsMap;
	public HashMap<String, Double> variables;
	public HashMap<String, Tree<Token>> functions;
	public ArrayList<Drawings> toDraw;
	
	public boolean isPaused, isStepped, okToContinue, verbose;
	long mySpeed;

	
	/**
	 * Constructor for an interpreter that initialized all of its maps
	 * and lists to new, empty ones.
	 */
	public Interpreter() {
		this.program = null;
		variables = new HashMap<String, Double>();
		functions = new HashMap<String, Tree<Token>>();
		bugsMap = new HashMap<String, Bug>();
		bugs = new ArrayList<Bug>();
		toDraw = new ArrayList<Drawings>();
		isPaused = true;
		isStepped = false;	
		okToContinue = true;
		verbose = false;
		mySpeed = 100L;
	}
	
	/**
	 * Sets up the interpreter by preprocessing the allbugs code
	 * and creating the bugs
	 */
	public void setUp() {
		//sets up the allbugs code
		allbugs = program.getChild(0);
		interpretAllbugs(allbugs);
		Tree<Token> bugList = program.getChild(1);
		//make new Bugs, set them to be blocked, and put in the bugs map
		for (int i = 0; i < bugList.getNumberOfChildren(); i++) {
			String name = bugList.getChild(i).getChild(0).getValue().value;
			Bug bug = new Bug(bugList.getChild(i), this);
			bug.setBlocked(true);
			bugsMap.put(name, bug);
			bugs.add(bug);
		}

	}
	
	/**
	 * Helper method to interpret the allbugs code and populate the interpreter's
	 * variables and functions maps that are accessible to all of the bugs
	 * @param tree
	 */
	private void interpretAllbugs(Tree<Token> tree) {
		Tree<Token> varListTree = tree.getChild(0);
		//for every var declaration child
		for (int i = 0; i < varListTree.getNumberOfChildren(); i++) {
			//for every variable in the varDeclaration
			for (int j = 0; j < varListTree.getChild(i).getNumberOfChildren(); j++) {
				//put that variable name in the variables map set to 0.0
				variables.put(varListTree.getChild(i).getChild(j).getValue().value, 0.0);
			}
		}
		Tree<Token> functionListTree = tree.getChild(1);
		//for every function list
		for (int i = 0; i < functionListTree.getNumberOfChildren(); i++) {
			//put the name of the function into the functions map with the value as the whole function tree
			functions.put(functionListTree.getChild(i).getChild(0).getValue().value, functionListTree.getChild(i));
		}
		
	}
	
	/**
	 * Sets up the interpreter by interpreting the Allbugs code, recording
	 * each Bug in the program and starting all of them running.
	 * Continuously unblocks all bugs until there are no more non-terminated bugs.
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		
		vPrint("Interpreter about to start bugs.");
		//start bugs going, still blocked
		for (Bug b : bugs) {
			b.start();
			vPrint("Bug " + b.getBugName() + " has been started.");
		}
		
		//while all bugs are blocked, unblock them
		vPrint("Bugs size is " + bugs.size());
		while (bugs.size() > 0 && okToContinue) {
			
			vPrint("Run Bugs size is " + bugs.size());
			unblockAllBugs();
		}
		vPrint("Interpreter dies.");
	}
	
	/**
	 * Method to get the given bug a work permit from the interpreter
	 * to continue its program.
	 * @param bug
	 */
	synchronized void getWorkPermit(Bug bug) {
		while (bug.isBlocked()) {
			try {
				vPrint("Bug " + bug.getBugName() + " is waiting for a work permit.");
				wait();
			}
			catch (InterruptedException e) {
				
			}
		}
		vPrint("Bug " + bug.getBugName() + " got a work permit.");
	}
	
	/**
	 * Alerts the interpreter that the given bug has finished a task and
	 * is now blocked waiting for another work permit.
	 * @param bug
	 */
	synchronized void completeCurrentTask(Bug bug) {
		bug.setBlocked(true);
		vPrint("Bug " + bug.getBugName() + " finished a task.");
		notifyAll();
	}
	
	/**
	 * Method to unblock all bugs. Waits until all bugs are blocked before
	 * unblocking all of them.
	 */
	synchronized void unblockAllBugs() {
		vPrint("Interpreter has entered unblocking stage.");
		vPrint("Before blocked bugs number = " + countBlockedBugs());
		vPrint("Before Bugs size is " + bugs.size());
		while (countBlockedBugs() < bugs.size()) {
			vPrint("blocked bugs number = " + countBlockedBugs());
			vPrint("Bugs size is " + bugs.size());
			try {
				vPrint("Interpreter is waiting for all bugs to be locked.");
				wait();
			}
			catch (InterruptedException e) {
				vPrint("Interpreter was interrupted");
				
			}
		}
		//if program has been paused, don't give out permits until restarted.
		while (isPaused) return;
		try {
			wait(mySpeed);
		} catch (InterruptedException e) {}
		if (isStepped) isPaused = true;
		for (Bug b : bugs) {
			b.setBlocked(false);
			vPrint("Bug " + b.getBugName() + " has been unblocked.");
		}
		vPrint("Interpreter is about to notify all that they've been unblocked.");
		notifyAll();
	}
	
	/**
	 * Helper method to add a drawings object to the toDraw list,
	 * synchronized to make sure Bugs don't try to update it at the
	 * same time.
	 * @param dr
	 */
	synchronized void addDrawing(Drawings dr) {
		this.toDraw.add(dr);
	}
	
	/**
	 * Helper method to count the number of bugs currently blocked.
	 * @return int count of blocked bugs
	 */
	private int countBlockedBugs() {
		int count = 0;
		for (Bug b : bugs) {
			if (b.isBlocked()) count++;
		}
		return count;
	}
	
	/**
	 * Alerts interpreter that the given bug has finished its program.
	 * Removes the bug from the list of bugs.
	 * @param bug
	 */
	synchronized void terminateBug(Bug bug) {
		bugs.remove(bug);
		vPrint("Bug size is " + bugs.size());
		vPrint("Bug " + bug.getBugName() + " has been terminated.");
		notifyAll();
	}
	
	private void vPrint(String s) {
	if (verbose) System.out.println(s);
	}
	
	/**
	 * Main method for testing the interpreter
	 * @param args
	 */
//	public static void main(String[] args) {
//		Interpreter i = new Interpreter();
//		//String text = "Allbugs {\nvar amount, delta\n}\nBug Sally {\ninitially {\ncolor red\nx = 50\ny = 50\namount = 1\nangle = 0\n}\nloop {\n\nmove amount\namount = amount + delta\nturn 90\nexit if x < 0\nexit if x > 100\n}\n}\nBug Fred {\ninitially {\ncolor blue\nx = 50\ny = 49\ndelta = 1\nangle = 180\n}\nloop {\nmove amount\nturn 90\nexit if x < 0\nexit if x > 100\n}\n}\n";
//		String text = "Bug Theresa { \n initially {\n color green \n x = 25\n y = 25\n }\n move 10 \n turn 90 \n move 10 \n turn 90 \n move 10 \n turn 90 \n move 10 \n }\n Bug Zach {\n initially {\n color blue \n x = 50\n y = 50\n angle = 45\n} \n move 10 \n turn 90 \n move 10 \n turn 90 \n move 10 \n turn 90 \n move 10 \n }\n";
//		Parser p = new Parser(text);
//		if (p.isProgram()) {
//			i.program = p.stack.pop();
//			i.run();
//		}
//	}
	
	
}
