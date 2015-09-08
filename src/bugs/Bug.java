package bugs;

import java.awt.Color;
import java.util.HashMap;
import java.util.Stack;

import tree.Tree;

/**
 * A class representing a Bug that provides methods for interpreting the Bug's
 * programming following the Bugs Grammar.
 * 
 * @author theresabreiner
 *
 */
public class Bug extends Thread {
	public String name;
	public Color color;
	public double x;
	public double y;
	public double angle;
	private Color brown = new Color(135, 62, 3);
	private Color purple = new Color(143, 49, 214);
	public double returnValue = 0.0;

	public HashMap<String, Double> variableMap;
	public HashMap<String, Tree<Token>> functions;
	public Stack<HashMap<String, Double>> scopes;
	public Tree<Token> program;

	public Interpreter interpreter;
	private boolean blocked;
	// set when we have executed a command
	private boolean didAction = false;
	// true if executing code during initial block, false if depends on work
	// permit
	private boolean initialBlock = true;
	public boolean testing = false;

	/**
	 * Constructor for a Bug object that sets the x, y, and angle instance
	 * variables to default 0.0 each and initializes the variableMap to record
	 * other variables and their values.
	 */
	public Bug(Tree<Token> p, Interpreter intr) {
		this.x = 0.0;
		this.y = 0.0;
		this.angle = 0.0;
		this.color = Color.BLACK;
		this.name = null;
		variableMap = new HashMap<String, Double>();
		functions = new HashMap<String, Tree<Token>>();
		scopes = new Stack<HashMap<String, Double>>();
		scopes.push(variableMap);
		interpreter = intr;
		program = p;
		setUpBug();
	}

	/**
	 * Helper method to setUp the Bug's name, var list, interpret init block and
	 * interpret the functions list to add to its functions map.
	 */
	private void setUpBug() {
		// save bug name into name instance variable
		String name = program.getChild(0).getValue().value;
		this.name = name;
		// interpret var list
		interpret(program.getChild(1));
		// interpret init block
		interpret(program.getChild(2));
		// interpret functions list first
		interpret(program.getChild(4));
	}

	// //////*Setter and Getter methods*//////////

	/**
	 * Getter method for this bug's name
	 * 
	 * @return bug's name as a String
	 */
	public String getBugName() {
		return this.name;
	}

	/**
	 * Setter method for this bug's name
	 * 
	 * @param n
	 *            the name as a String
	 */
	public void setBugName(String n) {
		this.name = n;
	}

	/**
	 * Getter method for double x
	 * 
	 * @return double x
	 */
	public double getX() {
		return x;
	}

	/**
	 * Getter method for double y
	 * 
	 * @return double y
	 */
	public double getY() {
		return y;
	}

	/**
	 * Getter method for angle
	 * 
	 * @return double angle
	 */
	public double getAngle() {
		return angle;
	}

	/**
	 * Setter method for variable x (double)
	 * 
	 * @param x
	 */
	public void setX(double x) {
		this.x = x;
	}

	/**
	 * Setter method for variable y (double)
	 * 
	 * @param y
	 */
	public void setY(double y) {
		this.y = y;
	}

	/**
	 * Setter method for angle variable (double)
	 * 
	 * @param angle
	 */
	public void setAngle(double angle) {
		this.angle = angle;
	}

	/**
	 * Getter method for this bug's color
	 * 
	 * @return Color of this bug
	 */
	public Color getColor() {
		return this.color;
	}

	/**
	 * Getter method for blocked variable.
	 * 
	 * @return boolean blocked
	 */
	public boolean isBlocked() {
		return this.blocked;
	}

	/**
	 * Setter method for blocked variable.
	 * 
	 * @param boolean b
	 */
	public void setBlocked(boolean b) {
		this.blocked = b;
	}

	// /////////////////* public methods *//////////////////

	/**
	 * Evaluates the tree based on the operation in its root node. Throws
	 * IllegalArgumentException if the root node does not contain an operation
	 * or comparison or terminal numeric value.
	 * 
	 * @param tree
	 * @return double value of the tree
	 */
	public double evaluate(Tree<Token> tree) {
		String root = tree.getValue().value;
		Token.Type rootType = tree.getValue().type;
		if ("+".equals(root) || "-".equals(root) || "*".equals(root)
				|| "/".equals(root)) {
			return evalArithmetic(tree);
		}
		if ("<".equals(root) || "<=".equals(root) || "=".equals(root)
				|| "!=".equals(root) || ">".equals(root) || ">=".equals(root)) {
			return evalCompare(tree);
		}
		if (".".equals(root))
			return evalDot(tree);
		if ("case".equals(root))
			return evalCase(tree);
		if ("call".equals(root))
			return evalCall(tree);
		if (rootType == Token.Type.NUMBER)
			return Double.parseDouble(root);
		// else, it should be a variable, so look up its value
		return fetch(root);
	}

	/**
	 * Interprets the tree based on the keyword in its root node. Throws an
	 * IllegalArgumentException if the keyword is invalid.
	 * 
	 * @param tree
	 */
	public void interpret(Tree<Token> tree) {
		String root = tree.getValue().value;
		if ("Bug".equals(root))
			interpretBug(tree);
		else if ("list".equals(root))
			interpretList(tree);
		else if ("var".equals(root))
			interpretVar(tree);
		else if ("initially".equals(root))
			interpretInitially(tree);
		else if ("block".equals(root))
			interpretBlock(tree);
		else if ("move".equals(root))
			interpretMove(tree);
		else if ("moveto".equals(root))
			interpretMoveTo(tree);
		else if ("turn".equals(root))
			interpretTurn(tree);
		else if ("turnto".equals(root))
			interpretTurnTo(tree);
		else if ("return".equals(root))
			interpretReturn(tree);
		else if ("line".equals(root))
			interpretLine(tree);
		else if ("assign".equals(root))
			interpretAssign(tree);
		else if ("loop".equals(root))
			interpretLoop(tree);
		else if ("exit".equals(root))
			interpretExit(tree);
		else if ("switch".equals(root))
			interpretSwitch(tree);
		else if ("color".equals(root))
			interpretColor(tree);
		else if ("function".equals(root))
			interpretFunction(tree);
		else if ("call".equals(root))
			evalCall(tree);
		else
			throw new IllegalArgumentException();
	}

	/**
	 * Takes the given variable and maps it to its value in whichever map on the
	 * stack it has been declared in, including checking the allbugs variables.
	 * If the variable is either "x", "y", or "angle", does not insert them into
	 * the map, but updates this Bug's instance variables. Throws a
	 * RuntimeException if the variable has not been declared in any of the maps
	 * on the stack or in the allbugs variables.
	 * 
	 * @param variable
	 * @param value
	 */
	public void store(String variable, double value) {
		if ("x".equals(variable)) {
			this.x = value;
			return;
		}
		if ("y".equals(variable)) {
			this.y = value;
			return;
		}
		if ("angle".equals(variable)) {
			this.angle = value;
			return;
		}
		HashMap<String, Double> currentMap;
		// for every variable map on the stack
		for (int i = 1; i < scopes.size(); i++) {
			currentMap = scopes.get(i);
			// if the variable is declared in that map, update its value
			if (currentMap.get(variable) != null) {
				currentMap.put(variable, value);
				return;
			}
		}
		// check bottom map on stack
		currentMap = scopes.get(0);
		if (currentMap.get(variable) != null) {
			currentMap.put(variable, value);
			return;
		}
		// check allbugs shared variables
		currentMap = interpreter.variables;
		if (currentMap.get(variable) == null) {
			// if it's not in the allbugs variables either, it hasn't been
			// declared
			throw new RuntimeException();
		}
		// if it is there, update it and return.
		currentMap.put(variable, value);
		return;
	}

	/**
	 * Looks up the given variable's value in the different variable maps on the
	 * stack, starting from the top going down until it finds the variable
	 * defined. Also checks the allbugs variables. If the variable is either
	 * "x", "y" or "angle", does not look in map but just returns the
	 * corresponding instance variable's value. Throws a RuntimeException if the
	 * variable is not in any of the maps on the stack or in the allbugs
	 * variables.
	 * 
	 * @param variable
	 * @return double value of the given variable
	 */
	public double fetch(String variable) {
		if ("x".equals(variable))
			return this.x;
		if ("y".equals(variable))
			return this.y;
		if ("angle".equals(variable))
			return this.angle;

		// check every map on the stack from top of stack downwards
		HashMap<String, Double> currentMap;
		for (int i = scopes.size() - 1; i >= 0; i--) {
			currentMap = scopes.get(i);
			Double val = currentMap.get(variable);
			if (val != null) {
				// if the variable was in the map, return its value
				return val.doubleValue();
			}
		}
		// check allbugs variables
		currentMap = interpreter.variables;
		Double val = currentMap.get(variable);
		if (val == null) {
			// the variable was not declared in any of the maps
			throw new RuntimeException("Variable not defined");
		}
		return val.doubleValue();

	}

	/**
	 * Method to calculate the distance from this bug to the named bug. Throws a
	 * RuntimeException if the named bug is not in the Interpreter's list of
	 * Bugs.
	 * 
	 * @param nameOfBug
	 *            as a String
	 * @return double distance from this bug to named bug
	 */
	public double distance(String nameOfBug) {
		Bug other = interpreter.bugsMap.get(nameOfBug);
		if (other == null)
			throw new RuntimeException(
					"Bug is not in Interpreter's list of Bugs");
		double changeX = other.x - this.x;
		double changeY = other.y - this.y;
		return Math.sqrt(changeX * changeX + changeY * changeY);
	}

	/**
	 * Method to calculate the angle from this bug to the named bug. Throws a
	 * RuntimeException if the named bug is not in the Interpreter's list of
	 * Bugs.
	 * 
	 * @param nameOfBug
	 *            as a String
	 * @return double distance from this bug to named bug
	 */
	public double direction(String nameOfBug) {
		Bug other = interpreter.bugsMap.get(nameOfBug);
		if (other == null)
			throw new RuntimeException(
					"Bug is not in Interpreter's list of Bugs");
		double changeX = other.x - this.x;
		double changeY = other.y - this.y;
		double rads = Math.atan(changeY / changeX);
		double angle = rads * 360.0 / (2 * Math.PI);
		if (changeX / changeY > 0) {
			if (changeX < 0)
				return 180.0 - angle;
			return 360.0 - angle;
		}
		if (changeX > 0)
			return -1 * angle;
		return 180.0 - angle;
	}

	// ////////////////* thread related methods *///////////////////

	/**
	 * This run method waits for permission to work and then begins interpreting
	 * this bug's program. Within the interpret implementation the bug
	 * repeatedly informs the interpreter that it has completed an action and
	 * then waits for another work permit. Once the bug's program has been
	 * completely interpreted, the bug tells the interpreter and is terminated.
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		// get permission to work
		interpreter.getWorkPermit(this);
		// do the actual work - interpret program
		// while interpreting the program, the bug will
		// notify that it has finished its current task if an action
		// is performed, and then wait for another work permit
		interpret(program);
		// once the entire program has been gone through,
		interpreter.terminateBug(this);
	}

	/**
	 * Helper method to communicate with the interpreter. Useful for keeping all
	 * thread-related work in one place for testing purposes.
	 */
	private void alertAndPause() {
		if (!testing) {
			interpreter.completeCurrentTask(this);
			interpreter.getWorkPermit(this);
		}
	}

	// /////////////////* helper methods */////////////////////

	// /////////* helper methods for calculations *///////////////

	/**
	 * Helper method to compare doubles with our .001 standard
	 * 
	 * @param a
	 * @param b
	 * @return true if a and b are within .001 of each other, false if not.
	 */
	private static boolean eq(double a, double b) {
		if (a - b < .001 && a - b > -.001)
			return true;
		return false;
	}

	/**
	 * Helper method for calculating change in x value based on the bug's
	 * current angle and given distance
	 * 
	 * @param distance
	 * @return double change in x
	 */
	private double deltaX(double distance) {
		double rads = angle * 2 * Math.PI / 360.0;
		return distance * Math.cos(rads);
	}

	/**
	 * Helper method for calculating change in y value based on the bug's
	 * current angle and given distance
	 * 
	 * @param distance
	 * @return double change in y
	 */
	private double deltaY(double distance) {
		double rads = angle * 2 * Math.PI / 360.0;
		return distance * Math.sin(rads) * -1.0;
	}

	// ////////////* helper methods for interpreting *////////////////

	/**
	 * Helper method to interpret a tree with "Bug" as its root node. Interprets
	 * its commands in order.
	 * 
	 * @param tree
	 */
	private void interpretBug(Tree<Token> tree) {
		// interpret command block which should now have access to functions
		interpret(tree.getChild(3));
	}

	/**
	 * Helper method to interpret a tree with "list" as its root node.
	 * Interprets all children of the tree in order.
	 * 
	 * @param tree
	 */
	private void interpretList(Tree<Token> tree) {
		for (int i = 0; i < tree.getNumberOfChildren(); i++) {
			interpret(tree.getChild(i));
		}
	}

	/**
	 * Helper method to interpret a tree with "var" as its root node by storing
	 * all of the children variables in the variableMap with default values of
	 * 0.0.
	 * 
	 * @param tree
	 */
	private void interpretVar(Tree<Token> tree) {
		for (int i = 0; i < tree.getNumberOfChildren(); i++) {
			String variable = tree.getChild(i).getValue().value;
			variableMap.put(variable, 0.0);
		}
	}

	/**
	 * Helper method to interpret a tree with "initially" as its root node.
	 * Interprets its one block child.
	 * 
	 * @param tree
	 */
	private void interpretInitially(Tree<Token> tree) {
		interpret(tree.getChild(0));
		// set initialBlock so that future code will depend on work permit
		initialBlock = false;
		// reset didAction in case there were actions in initially block
		didAction = false;
	}

	/**
	 * Helper method to interpret a tree that has "block" as its root node.
	 * Interprets each child of the tree in order. If an action is interpreted,
	 * the bug informs the interpreter that its task has been completed and
	 * waits for another work permit before continuing.
	 * 
	 * @param tree
	 */
	private void interpretBlock(Tree<Token> tree) {
		for (int i = 0; i < tree.getNumberOfChildren(); i++) {
			if ("return".equals(tree.getChild(i).getValue().value)) {
				interpret(tree.getChild(i));
				return;
			}
			if (didAction && !initialBlock) {
				// reset the didAction for the next time
				didAction = false;
				// pause here, save where we are - unless in initial block
				alertAndPause();
			}
			interpret(tree.getChild(i));
		}
	}

	/**
	 * Helper method to interpret a tree with "move" as its root node by
	 * evaluating the expression child and moving this bug that number of spaces
	 * by updatings its x and y coordinates.
	 * 
	 * @param tree
	 */
	private void interpretMove(Tree<Token> tree) {
		double distance = evaluate(tree.getChild(0));
		double newX = getX() + deltaX(distance);
		double newY = getY() + deltaY(distance);
		didAction = true;
		interpreter.addDrawing(new Drawings(getX(), getY(), newX, newY, this.color));
		setX(newX);
		setY(newY);
	}

	/**
	 * Helper method to interpret a tree with "moveto" as its root node by
	 * evaluating its two children and updating this bug's x and y values to
	 * those values.
	 * 
	 * @param tree
	 */
	private void interpretMoveTo(Tree<Token> tree) {
		// set first child as new x value
		double newX = evaluate(tree.getChild(0));
		// set second child as new y value
		double newY = evaluate(tree.getChild(1));
		interpreter.addDrawing(new Drawings(getX(), getY(), newX, newY, color));
		didAction = true;
		setX(newX);
		setY(newY);
	}

	/**
	 * Helper method to interpret a tree with "turn" as its root node by
	 * evaluating its expression child and adding that value to its angle, and
	 * updating the angle to be within 0.0 and 360.0
	 * 
	 * @param tree
	 */
	private void interpretTurn(Tree<Token> tree) {
		double newAngle = getAngle() + evaluate(tree.getChild(0));
		if (newAngle > 360.0)
			newAngle = newAngle % 360.0;
		while (newAngle < 0)
			newAngle += 360.0;
		setAngle(newAngle);
		didAction = true;

	}

	/**
	 * Helper method to interpret a tree with "turnto" as its root node by
	 * evaluating its expression child and setting its angle to that value.
	 * Updates angle to be within 0.0 and 360.0.
	 * 
	 * @param tree
	 */
	private void interpretTurnTo(Tree<Token> tree) {
		double newAngle = evaluate(tree.getChild(0));
		if (newAngle > 360.0)
			newAngle = newAngle % 360.0;
		while (newAngle < 0.0)
			newAngle += 360.0;
		setAngle(newAngle);
		didAction = true;
	}

	/**
	 * Helper method to interpret a tree with "return" as its root node.
	 * Evaluates the value of the child and saves it in the instance variable
	 * returnValue to be read by the caller.
	 * 
	 * @param tree
	 */
	private void interpretReturn(Tree<Token> tree) {
		returnValue = evaluate(tree.getChild(0));
		return;
	}

	/**
	 * Helper method to interpret a tree with "line" as its root node. Evaluates
	 * each expression and then records that a line must be drawn from the first
	 * two values (x1, y1) to the second two values (x2, y2) in the color of
	 * this bug.
	 * 
	 * @param tree
	 */
	private void interpretLine(Tree<Token> tree) {
		double x1 = evaluate(tree.getChild(0));
		double y1 = evaluate(tree.getChild(1));
		double x2 = evaluate(tree.getChild(2));
		double y2 = evaluate(tree.getChild(3));
		interpreter.toDraw.add(new Drawings(x1, y1, x2, y2, this.color));
		didAction = true;
		return;
	}

	/**
	 * Helper method to interpret a tree with "assign" as its root node. Updates
	 * the value of the variable in the first child in the variableMap to be the
	 * value of the second child when evaluated. Throws a RuntimeException if
	 * the variable has not been already declared and is not found in the map.
	 * 
	 * @param tree
	 */
	private void interpretAssign(Tree<Token> tree) {
		String variable = tree.getChild(0).getValue().value;
		// tries to fetch, throws RuntimeException if not in map
		fetch(variable);
		store(variable, evaluate(tree.getChild(1)));
	}

	/**
	 * Helper method to interpret a tree with "loop" as its root node. Instead
	 * of interpreting the block tree directly, this method interprets every
	 * child of the block node until it interprets an exit if statement that
	 * evaluates to true.
	 * 
	 * @param tree
	 */
	private void interpretLoop(Tree<Token> tree) {
		// look at every child of the block node
		Tree<Token> blockTree = tree.getChild(0);
		while (true) {
			for (int i = 0; i < blockTree.getNumberOfChildren(); i++) {
				// if the child is an exit node, interpret it
				if ("exit".equals(blockTree.getChild(i).getValue().value)) {
					// if it was interpreted to be true, stop loop
					if (interpretExit(blockTree.getChild(i))) {
						return;
					}
				}
				// if node is not exit, interpret
				else if (didAction) {
					didAction = false;
					alertAndPause();
				}
				interpret(blockTree.getChild(i));
			}
		}
	}

	/**
	 * Helper method to decide if the exit if tree is evaluated to true or
	 * false.
	 * 
	 * @param tree
	 * @return true if the child expression is true, false if not
	 */
	private boolean interpretExit(Tree<Token> tree) {
		if (eq(evaluate(tree.getChild(0)), 0.0))
			return false;
		return true;
	}

	/**
	 * Helper method to interpret a tree with "switch" as its root node.
	 * Evaluates each child until one evaluates to be true and then returns.
	 * 
	 * @param tree
	 */
	private void interpretSwitch(Tree<Token> tree) {
		for (int i = 0; i < tree.getNumberOfChildren(); i++) {
			// once a child is evaluated to be true, stop the loop
			if (!eq(evaluate(tree.getChild(i)), 0.0)) {
				break;
			}
		}
	}

	/**
	 * Helper method to interpret a tree with "color" as its root node. Gets the
	 * string color from the value of tree's child and assigns a Color object to
	 * this bug based on the string. If string is "none" the color is set to
	 * null. Throws an IllegalArgumentException if the string is not one of the
	 * color keywords defined in the Token class.
	 * 
	 * @param tree
	 */
	private void interpretColor(Tree<Token> tree) {
		String color = tree.getChild(0).getValue().value;
		if ("red".equals(color))
			this.color = Color.RED;
		else if ("black".equals(color))
			this.color = Color.BLACK;
		else if ("blue".equals(color))
			this.color = Color.BLUE;
		else if ("cyan".equals(color))
			this.color = Color.CYAN;
		else if ("darkGray".equals(color))
			this.color = Color.DARK_GRAY;
		else if ("gray".equals(color))
			this.color = Color.GRAY;
		else if ("green".equals(color))
			this.color = Color.GREEN;
		else if ("lightGray".equals(color))
			this.color = Color.LIGHT_GRAY;
		else if ("magenta".equals(color))
			this.color = Color.MAGENTA;
		else if ("orange".equals(color))
			this.color = Color.ORANGE;
		else if ("pink".equals(color))
			this.color = Color.PINK;
		else if ("white".equals(color))
			this.color = Color.WHITE;
		else if ("yellow".equals(color))
			this.color = Color.YELLOW;
		else if ("brown".equals(color))
			this.color = brown;
		else if ("purple".equals(color))
			this.color = purple;
		else if ("none".equals(color))
			this.color = null;
		else
			throw new IllegalArgumentException();
	}

	/**
	 * Helper method to interpret a tree that has "function" in its root node.
	 * The name of the function from its first child is added to the functions
	 * HashMap with the whole tree, rooted at the "function" node, as its
	 * corresponding value. Throws a RuntimeException if the function definition
	 * tries to use variables with the names x, y, or angle.
	 * 
	 * @param tree
	 */
	private void interpretFunction(Tree<Token> tree) {
		// first make sure none of the var names were chosen as x, y, or angle
		for (int i = 0; i < tree.getChild(1).getNumberOfChildren(); i++) {
			String varname = tree.getChild(1).getChild(i).getValue().value;
			if ("x".equals(varname) || "y".equals(varname)
					|| "angle".equals(varname)) {
				throw new RuntimeException(
						"Can't have a variable within a function definition that"
								+ "matches 'x', 'y', or 'angle'.");
			}
		}
		// adds this function to the functions map
		String function = tree.getChild(0).getValue().value;
		functions.put(function, tree);
	}

	// /////////////////* helper methods for evaluating *//////////////////

	/**
	 * Helper method to evaluate a tree with an arithmetic symbol '+', '-', '*',
	 * or '/' as the root node. Throws an IllegalArgumentException if the root
	 * of the tree is not an arithmetic symbol.
	 * 
	 * @param tree
	 * @return the evaluation of the tree as a double
	 */
	private double evalArithmetic(Tree<Token> tree) {
		String root = tree.getValue().value;
		if ("+".equals(root)) {
			if (tree.getNumberOfChildren() == 1) {
				// unary +
				return evaluate(tree.getChild(0));
			}
			// just addition
			return evaluate(tree.getChild(0)) + evaluate(tree.getChild(1));
		}
		if ("-".equals(root)) {
			if (tree.getNumberOfChildren() == 1) {
				// unary -, ie negative number
				return evaluate(tree.getChild(0)) * -1;
			}
			// just subtraction
			return evaluate(tree.getChild(0)) - evaluate(tree.getChild(1));
		}
		if ("*".equals(root)) {
			// multiply children
			return evaluate(tree.getChild(0)) * evaluate(tree.getChild(1));
		}
		if ("/".equals(root)) {
			// divide children
			return evaluate(tree.getChild(0)) / evaluate(tree.getChild(1));
		}
		throw new IllegalArgumentException();
	}

	/**
	 * Helper method to evaluate a tree with a comparator as the root Throws an
	 * IllegalArgumentException if the root value is not a comparator
	 * 
	 * @param tree
	 * @return double value 0.0 or 1.0 after evaluating the tree
	 */
	private double evalCompare(Tree<Token> tree) {
		String val = tree.getValue().value;
		if ("<".equals(val)) {
			if (evaluate(tree.getChild(0)) < evaluate(tree.getChild(1))) {
				return 1.0;
			}
			return 0.0;
		}
		if ("<=".equals(val)) {
			// less than or almost equals
			if (evaluate(tree.getChild(0)) < evaluate(tree.getChild(1))
					|| eq(evaluate(tree.getChild(0)),
							evaluate(tree.getChild(1)))) {
				return 1.0;
			}
			return 0.0;
		}
		if ("=".equals(val)) {
			// using almost equals
			if (eq(evaluate(tree.getChild(0)), evaluate(tree.getChild(1)))) {
				return 1.0;
			}
			return 0.0;
		}
		if ("!=".equals(val)) {
			// if almost equals, return false
			if (eq(evaluate(tree.getChild(0)), evaluate(tree.getChild(1)))) {
				return 0.0;
			}
			// if not almost equals, return true
			return 1.0;
		}
		if (">".equals(val)) {
			if (evaluate(tree.getChild(0)) > evaluate(tree.getChild(1))) {
				return 1.0;
			}
			return 0.0;
		}
		if (">=".equals(val)) {
			// greater than or almost equals
			if (evaluate(tree.getChild(0)) > evaluate(tree.getChild(1))
					|| eq(evaluate(tree.getChild(0)),
							evaluate(tree.getChild(1)))) {
				return 1.0;
			}
			return 0.0;
		}
		// not a comparator in root of tree
		throw new IllegalArgumentException();

	}

	/**
	 * Helper method to evaluate a tree with "." as its root node. Finds the bug
	 * to which the desired variable belongs and returns the value of that
	 * variable in that bug's variableMap, or its x, y, or angle value if
	 * specified.
	 * 
	 * @param tree
	 * @return double value of this tree
	 */
	private double evalDot(Tree<Token> tree) {
		String bugName = tree.getChild(0).getValue().value;
		Bug other = interpreter.bugsMap.get(bugName);
		String var = tree.getChild(1).getValue().value;
		if ("x".equals(var))
			return other.x;
		if ("y".equals(var))
			return other.y;
		if ("angle".equals(var))
			return other.angle;
		return other.variableMap.get(var).doubleValue();
	}

	/**
	 * Helper method to evaluate a tree whose root node has "case". If the first
	 * child evaluates to true, the second child is interpreted. The value of
	 * the first child is returned whether or not the second child was
	 * interpreted.
	 * 
	 * @param tree
	 * @return double value of this tree's first child
	 */
	private double evalCase(Tree<Token> tree) {
		double ret = evaluate(tree.getChild(0));
		// if not true, don't interpret second child
		if (eq(ret, 0.0))
			return 0.0;
		// else, interpret second child and return the value of first child
		interpret(tree.getChild(1));
		return ret;
	}

	/**
	 * Helper method for evaluating a tree with "call" as its root node. Puts a
	 * new HashMap on the stack and sets the variables to the values given as
	 * arguments. After executing the function, looks at the return value left
	 * in the instance variable and returns it if this function resulted in a
	 * return value. If not, returns 0.0.
	 * 
	 * @param tree
	 * @return double return value of the function or 0.0 if no return value
	 */
	private double evalCall(Tree<Token> tree) {
		// if it's a special function, do special things
		String funcName = tree.getChild(0).getValue().value;
		String bugName;
		if ("direction".equals(funcName)) {
			bugName = tree.getChild(1).getChild(0).getValue().value;
			return direction(bugName);
		}
		if ("distance".equals(funcName)) {
			bugName = tree.getChild(1).getChild(0).getValue().value;
			return distance(bugName);
		}
		// make new variables map for this function
		HashMap<String, Double> thisFunction = new HashMap<String, Double>();
		scopes.push(thisFunction);
		// find function definition tree in functions map by its name
		Tree<Token> funcDef = functions.get(funcName);
		if (funcDef == null) {
			// if not in the bug's functions, check the allbugs functions
			funcDef = interpreter.functions
					.get(tree.getChild(0).getValue().value);
			if (funcDef == null) {
				// if not in the allbugs functions either, a function with this
				// name has
				// not been defined
				throw new RuntimeException("Can't find function definition for " + funcName);
			}
		}
		Tree<Token> args = tree.getChild(1);
		if (args.getNumberOfChildren() != funcDef.getChild(1)
				.getNumberOfChildren()) {
			throw new RuntimeException(
					"Number of arguments given does not match function definition signature");
		}
		// for every input argument, save its value to this function's variables
		// but corresponding to the formal variable name from definition
		for (int i = 0; i < args.getNumberOfChildren(); i++) {
			Double argValue = evaluate(args.getChild(i));
			// find ith formal variable name
			String formalVar = funcDef.getChild(1).getChild(i).getValue().value;
			// store this name with the arg value in this function's map
			thisFunction.put(formalVar, argValue);
		}
		// execute the function
		interpretBlock(funcDef.getChild(2));
		// when function is done, return the return value
		scopes.pop();
		double retVal = returnValue;
		// if the return value was not set, just return 0.0
		if (eq(retVal, 0.0))
			return 0.0;
		// if there was something saved in the returnValue,
		// reset it to 0.0 and return the previous value
		returnValue = 0.0;
		return retVal;
	}

}
