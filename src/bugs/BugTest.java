package bugs;

import static org.junit.Assert.*;

import java.awt.Color;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import tree.Tree;

/**
 * Test class for testing the Bug class
 * @author theresabreiner
 *
 */
public class BugTest {

	//global variable for unit-testing purposes
	Bug bug;
	Bug bug2;
	Interpreter intr;
	Tree<Token> program = tree("program", tree("Allbugs", tree("list"), tree("list")), tree("list", tree("Bug", "tt", tree("list"), tree("initially"), tree("block"), tree("list"))));
	
	@Before
	public void setUp() {
		intr = new Interpreter();
		Parser parser = new Parser("Bug bug {\n var a, b\n turnto 0.0\n }\n");
		parser.isProgram();
		Tree<Token> bugTree = parser.stack.peek().getChild(1).getChild(0);
		bug = new Bug(bugTree, intr);
		intr.bugsMap.put("bug", bug);
		bug.variableMap.put("var1", 10.0);
		bug.testing = true;
		
		Parser parser2 = new Parser("Bug bug2 {\n var a, b\n initially {\n turnto 45.0\n moveto 10.0, 10.0\n }\n turnto 45.0\n }\n");
		parser2.isProgram();
		Tree<Token> bugTree2 = parser2.stack.peek().getChild(1).getChild(0);
		bug2 = new Bug(bugTree2, intr);
		intr.bugsMap.put("bug2", bug2);
		bug2.testing = true;
	}
	@Test
	public void testBug() {
		Interpreter intr2 = new Interpreter();
		Tree<Token> p = tree("program", tree("Allbugs", tree("list"), tree("list")), tree("list", tree("Bug", "tt", tree("list"), tree("initially"), tree("block"), tree("list"))));
		Parser parser3 = new Parser("Bug theresa {\n var a, b\n initially {\n}\n turnto 0.0\n }\n");
		parser3.isProgram();
		Tree<Token> bugTree3 = parser3.stack.peek().getChild(1).getChild(0);
		Bug theresa = new Bug(bugTree3, intr2);
		theresa.testing = true;
		assertTrue(eq(theresa.x, 0.0));
		assertTrue(eq(theresa.y, 0.0));
		assertTrue(eq(theresa.angle, 0.0));
		assertEquals(theresa.color, Color.BLACK);
		assertEquals(theresa.name, "theresa");
		assertEquals(theresa.variableMap.size(), 2);
		assertEquals(theresa.functions.size(), 0);
		assertEquals(theresa.scopes.size(), 1);
		
	}
	
	@Test
	public void testGetX() {
		assertTrue(eq(bug.getX(), 0.0));
		bug.x = 15.0;
		assertTrue(eq(bug.getX(), 15.0));
	}
	
	@Test
	public void testSetX() {
		assertTrue(eq(bug.x, 0.0));
		bug.setX(15.0);
		assertTrue(eq(bug.x, 15.0));
	}
	
	@Test
	public void testGetY() {
		assertTrue(eq(bug.getY(), 0.0));
		bug.y = 15.0;
		assertTrue(eq(bug.getY(), 15.0));
	}
	
	@Test
	public void testSetY() {
		assertTrue(eq(bug.y, 0.0));
		bug.setY(15.0);
		assertTrue(eq(bug.y, 15.0));
	}
	
	@Test
	public void testGetAngle() {
		assertTrue(eq(bug.getAngle(), 0.0));
		bug.angle = 15.0;
		assertTrue(eq(bug.getAngle(), 15.0));
	}
	
	@Test
	public void testSetAngle() {
		assertTrue(eq(bug.angle, 0.0));
		bug.setAngle(15.0);
		assertTrue(eq(bug.angle, 15.0));
	}
	
	@Test
	public void testGetName() {
		assertEquals(bug.getBugName(), "bug");
		bug.name = "myName";
		assertTrue("myName".equals(bug.getBugName()));
	}
	
	@Test
	public void testSetName() {
		assertEquals(bug.name, "bug");
		bug.setBugName("super");
		assertTrue(bug.name.equals("super"));
	}

	@Test
	public void testStore() {
		bug.store("var1", 13.5);
		assertTrue(eq(bug.variableMap.get("var1"), 13.5));
	}
	
	@Test (expected = RuntimeException.class)
	public void testStoreUndeclared() {
		bug.store("something",  13.0);
	}
	

	@Test
	public void testFetch() {
		double f = bug.fetch("var1");
		assertTrue(eq(f, 10.0));
		bug.variableMap.put("myvar", 12345.67532);
		double g = bug.fetch("myvar");
		assertTrue(eq(g, 12345.67532));
	}
	
	@Test(expected = RuntimeException.class)
	public void testFetchRuntime() {
		bug.fetch("hello");
	}
	
	@Test
	public void testDistance() {
		assertTrue(eq(bug.distance("bug2"), 14.1421));
	}
	
	@Test (expected = RuntimeException.class)
	public void testDistanceBad() {
		bug.distance("bug3");
	}
	
	@Test
	public void testDirection() {
		assertTrue(eq(bug.direction("bug2"), 360.0 - 45.0));
		Parser parser3 = new Parser("Bug bug3 {\n var a, b\n turnto 0.0\n }\n");
		parser3.isProgram();
		Tree<Token> bugTree3 = parser3.stack.peek().getChild(1).getChild(0);
		Bug bug3 = new Bug(bugTree3, intr);
		bug3.testing = true;
		intr.bugsMap.put("bug3", bug3);
		bug3.x = 5.0;
		bug3.y = 20.0;
		assertTrue(eq(bug2.direction("bug3"), 360.0 - 116.565051));
		assertTrue(eq(bug.direction("bug3"), 360.0 - 75.96375));
		assertTrue(eq(bug2.direction("bug"), 360.0 - 225.0));
		assertTrue(eq(bug3.direction("bug2"), 360.0 - 296.565051));
	}
	
	@Test (expected = RuntimeException.class)
	public void testDirectionBad() {
		bug.direction("myBug");
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void testInterpretIllegal() {
		Tree<Token> tree = tree("hello", "45.0");
		bug.interpret(tree);
	}
	
	@Test
	public void testEvaluateVar() {
		Tree<Token> tree = tree("<", "var1", "45.0");
		double val = bug.evaluate(tree);
		assertTrue(eq(val, 1.0));
		tree = tree("assign", "var1", "12.0");
		bug.interpret(tree);
		assertTrue(eq(bug.fetch("var1"), 12.0));
		tree = tree(">", "20.0", "var1");
		val = bug.evaluate(tree);
		assertTrue(eq(val, 1.0));
		tree = tree("=", "var1", "100.0");
		val = bug.evaluate(tree);
		assertTrue(eq(val, 0.0));
		
	}
	
	@Test (expected = RuntimeException.class)
	public void testEvalIllegal() {
		Tree<Token> tree = tree("hello");
		bug.evaluate(tree);
	}
	
	@Test
	public void testinterpretProgram() {
		//fail(); Not in first assignment

	}
	
	@Test
	public void testinterpretAllbugs() {
		//fail(); Not in first assignment

	}
	
	@Test
	public void testSetupBug() {
		Tree<Token> tree = tree("Bug", "theresa", tree("list", tree("var", "something")), tree("initially", tree("block")), tree("block", tree("move", "10.0")), tree("list", tree("function", "func", tree("var"), tree("block", tree("turnto", "90.0")))));
		Bug theresa = new Bug(tree, intr);
		theresa.testing = true;
		assertTrue("theresa".equals(theresa.getBugName()));
		assertTrue(eq(theresa.fetch("something"), 0.0));
		assertTrue(eq(theresa.getX(), 0.0));
		assertTrue(eq(theresa.getY(), 0.0));
		
		Tree<Token> tree2 = tree("Bug", "breiner", tree("list", tree("var", "mars")), tree("initially", tree("block", tree("assign", "mars", "15.0"))), tree("block", tree("turnto", "14.0")), tree("list"));
		Bug breiner = new Bug(tree2, intr);
		breiner.testing = true;
		assertTrue("breiner".equals(breiner.getBugName()));
		assertTrue(eq(breiner.fetch("mars"), 15.0));
		assertTrue(eq(breiner.getX(), 0.0));
		assertTrue(eq(breiner.getY(), 0.0));
		assertTrue(eq(breiner.getAngle(), 0.0));
		
		Tree<Token> tree3 = tree("Bug", "withFunc", tree("list", tree("var", "something")), tree("initially", tree("block")), tree("block", tree("move", "10.0"), tree("assign", "something", tree("call", "thefunction", tree("var", "50.0", "0.0")))), tree("list", tree("function", "thefunction", tree("var", "hi", "low"), tree("block", tree("return", tree("+", "10.0", tree("-", "hi", "low")))))));
		Bug withFunc = new Bug(tree3, intr);
		withFunc.testing = true;
		assertTrue("withFunc".equals(withFunc.getBugName()));
		assertTrue(eq(withFunc.fetch("something"), 0.0));

	}
	
	@Test
	public void testInterpretBug() {
		Tree<Token> tree = tree("Bug", "theresa", tree("list", tree("var", "something")), tree("initially", tree("block")), tree("block", tree("move", "10.0")), tree("list", tree("function", "func", tree("var"), tree("block", tree("turnto", "90.0")))));
		Bug theresa = new Bug(tree, intr);
		theresa.testing = true;
		theresa.interpret(tree);
		assertTrue("theresa".equals(theresa.getBugName()));
		assertTrue(eq(theresa.fetch("something"), 0.0));
		assertTrue(eq(theresa.getX(), 10.0));
		assertTrue(eq(theresa.getY(), 0.0));
		
		Tree<Token> tree2 = tree("Bug", "breiner", tree("list", tree("var", "mars")), tree("initially", tree("block", tree("assign", "mars", "15.0"))), tree("block", tree("turnto", "14.0")), tree("list"));
		Bug breiner = new Bug(tree2, intr);
		breiner.testing = true;
		breiner.interpret(tree2);
		assertTrue(eq(breiner.fetch("mars"), 15.0));
		assertTrue(eq(breiner.getX(), 0.0));
		assertTrue(eq(breiner.getY(), 0.0));
		assertTrue(eq(breiner.getAngle(), 14.0));
		
		Tree<Token> tree3 = tree("Bug", "withFunc", tree("list", tree("var", "something")), tree("initially", tree("block")), tree("block", tree("move", "10.0"), tree("assign", "something", tree("call", "thefunction", tree("var", "50.0", "0.0")))), tree("list", tree("function", "thefunction", tree("var", "hi", "low"), tree("block", tree("return", tree("+", "10.0", tree("-", "hi", "low")))))));
		Bug withFunc = new Bug(tree3, intr);
		withFunc.testing = true;
		withFunc.interpret(tree3);
		assertTrue("withFunc".equals(withFunc.getBugName()));
		assertTrue(eq(withFunc.fetch("something"), 60.0));
	}

	@Test
	public void testinterpretList() {
		Tree<Token> tree = tree("list", tree("var", "theresa"), tree("var", "breiner", "awesome"));
		bug.interpret(tree);
		assertTrue(eq(bug.fetch("theresa"), 0.0));
		assertTrue(eq(bug.fetch("breiner"), 0.0));
		assertTrue(eq(bug.fetch("awesome"), 0.0));
		
		tree = tree("list");
		bug.interpret(tree);
		assertTrue(eq(bug.getX(), 0.0));
		assertTrue(eq(bug.getY(), 0.0));
		assertTrue(eq(bug.getAngle(), 0.0));

	}

	@Test
	public void testinterpretVar() {
		Tree<Token> tree = tree("var", "theresa", "breiner");
		bug.interpret(tree);
		assertTrue(eq(bug.fetch("theresa"), 0.0));
		assertTrue(eq(bug.fetch("breiner"), 0.0));
		
		tree = tree("var", "howdy");
		bug.interpret(tree);
		assertTrue(eq(bug.fetch("howdy"), 0.0));

	}

	@Test
	public void testinterpretInitially() {
		Tree<Token> tree = tree("initially", tree("block"));
		bug.interpret(tree);
		assertTrue(eq(bug.getX(), 0.0));
		assertTrue(eq(bug.getY(), 0.0));
		assertTrue(eq(bug.getAngle(), 0.0));
		
		tree = tree("initially", tree("block", tree("moveto", "10.0", "10.0")));
		bug.interpret(tree);
		assertTrue(eq(bug.getX(), 10.0));
		assertTrue(eq(bug.getY(), 10.0));
		assertTrue(eq(bug.getAngle(), 0.0));

	}

	@Test
	public void testinterpretBlock() {
		Tree<Token> tree = tree("block", tree("move", "10.0"), tree("turn", "90.0"), tree("move", tree("-", "10.0")));
		bug.interpret(tree);
		assertTrue(eq(bug.getX(), 10.0));
		assertTrue(eq(bug.getY(), 10.0));
		assertTrue(eq(bug.getAngle(), 90.0));
		
		tree = tree("block", tree("moveto", "50.0", "50.0"), tree("turnto", "180.0"), tree("move", tree("*", "3.0", "4.0")));
		bug.interpret(tree);
		assertTrue(eq(bug.getX(), 38.0));
		assertTrue(eq(bug.getY(), 50.0));
		assertTrue(eq(bug.getAngle(), 180.0));
		
		tree = tree("block");
		bug.interpret(tree);
		assertTrue(eq(bug.getX(), 38.0));
		assertTrue(eq(bug.getY(), 50.0));
		assertTrue(eq(bug.getAngle(), 180.0));
		
		tree = tree("block", tree("turnto", "12.0"));
		bug.interpret(tree);
		assertTrue(eq(bug.getX(), 38.0));
		assertTrue(eq(bug.getY(), 50.0));
		assertTrue(eq(bug.getAngle(), 12.0));
		
		tree = tree("block", tree("turnto", "185.0"), tree("return", "10.0"), tree("turnto", "0.0"));
		bug.interpret(tree);
		assertTrue(eq(bug.getAngle(), 185.0));
		assertTrue(eq(bug.returnValue, 10.0));
	}

	@Test
	public void testinterpretMove() {
		//reset the toDraw arraylist for this test
		intr.toDraw = new ArrayList<Drawings>();
		Tree<Token> tree = tree("move", "5.0");
		bug.interpret(tree);
		assertTrue(eq(bug.getX(), 5.0));
		assertTrue(eq(bug.getY(), 0.0));
		assertEquals(1, intr.toDraw.size());
		Drawings d = intr.toDraw.get(0);
		assertTrue(eq(d.x1, 0.0));
		assertTrue(eq(d.y1, 0.0));
		assertTrue(eq(d.x2, 5.0));
		assertTrue(eq(d.y2, 0.0));
		
		bug.setX(10.0);
		bug.setY(10.0);
		bug.setAngle(270.0);
		tree = tree("move", "5.0");
		bug.interpret(tree);
		assertTrue(eq(bug.getX(), 10.0));
		assertTrue(eq(bug.getY(), 15.0));
		assertTrue(eq(bug.getAngle(), 270.0));
		assertEquals(2, intr.toDraw.size());
		Drawings d2 = intr.toDraw.get(1);
		assertTrue(eq(d2.x1, 10.0));
		assertTrue(eq(d2.y1, 10.0));
		assertTrue(eq(d2.x2, 10.0));
		assertTrue(eq(d2.y2, 15.0));
		
		bug.setAngle(180.0);
		bug.interpret(tree);
		assertTrue(eq(bug.getX(), 5.0));
		assertTrue(eq(bug.getY(), 15.0));
		assertEquals(3, intr.toDraw.size());
		Drawings d3 = intr.toDraw.get(2);
		assertTrue(eq(d3.x1, 10.0));
		assertTrue(eq(d3.y1, 15.0));
		assertTrue(eq(d3.x2, 5.0));
		assertTrue(eq(d3.y2, 15.0));
		
		bug.setAngle(45.0);
		bug.interpret(tree);
		assertTrue(eq(bug.getX(), 5.0 + 5.0 * .707));
		assertTrue(eq(bug.getY(), 15.0 - 5.0 * .707));
		assertEquals(4, intr.toDraw.size());
		Drawings d4 = intr.toDraw.get(3);
		assertTrue(eq(d4.x1, 5.0));
		assertTrue(eq(d4.y1, 15.0));
		assertTrue(eq(d4.x2, 5.0 + 5.0 * .707));
		assertTrue(eq(d4.y2, 15.0 - 5.0 * .707));
		
		bug.setAngle(225.0);
		bug.interpret(tree);
		assertTrue(eq(bug.getX(), 5.0));
		assertTrue(eq(bug.getY(), 15.0));
		assertEquals(5, intr.toDraw.size());
		Drawings d5 = intr.toDraw.get(4);
		assertTrue(eq(d5.x1, 5.0 + 5.0 * .707));
		assertTrue(eq(d5.y1, 15.0 - 5.0 * .707));
		assertTrue(eq(d5.x2, 5.0));
		assertTrue(eq(d5.y2, 15.0));
		
		bug.setAngle(300);
		bug.interpret(tree);
		assertTrue(eq(bug.getX(), 5.0 + 5.0 * .5));
		assertTrue(eq(bug.getY(), 15.0 + 5.0 * .866));
		assertEquals(6, intr.toDraw.size());
		Drawings d6 = intr.toDraw.get(5);
		assertTrue(eq(d6.x1, 5.0));
		assertTrue(eq(d6.y1, 15.0));
		assertTrue(eq(d6.x2, 5.0 + 5.0 * .5));
		assertTrue(eq(d6.y2, 15.0 + 5.0 * .866));
		
		
		double currX = bug.getX();
		double currY = bug.getY();
		
		bug.setAngle(160.0);
		bug.interpret(tree);
		assertTrue(eq(bug.getX(), currX - 5.0 * .93969));
		assertTrue(eq(bug.getY(), currY - 5.0 * .34202));
		assertEquals(7, intr.toDraw.size());
		Drawings d7 = intr.toDraw.get(6);
		assertTrue(eq(d7.x1, currX));
		assertTrue(eq(d7.y1, currY));
		assertTrue(eq(d7.x2, currX - 5.0 * .93969));
		assertTrue(eq(d7.y2, currY - 5.0 * .34202));

	}

	@Test
	public void testinterpretMoveTo() {
		Tree<Token> tree = tree("moveto", "30.0", "20.0");
		bug.interpret(tree);
		assertTrue(eq(bug.getX(), 30.0));
		assertTrue(eq(bug.getY(), 20.0));
		assertTrue(eq(bug.getAngle(), 0.0));
		
		tree = tree("moveto", tree("+", "5.0", "5.0"), tree("/", "100.0", "4.0"));
		bug.interpret(tree);
		assertTrue(eq(bug.getX(), 10.0));
		assertTrue(eq(bug.getY(), 25.0));
		assertTrue(eq(bug.getAngle(), 0.0));

	}

	@Test
	public void testinterpretTurn() {
		Tree<Token> tree = tree("turn", "90.0");
		bug.interpret(tree);
		assertTrue(eq(bug.getAngle(), 90.0));
		tree = tree("turn", "10.0");
		bug.interpret(tree);
		assertTrue(eq(bug.getAngle(), 100.0));
		tree = tree("turn", tree("*", "15.0", tree("-", "3.0")));
		bug.interpret(tree);
		assertTrue(eq(bug.getAngle(), 55.0));
		tree = tree("turn", "400.0");
		bug.interpret(tree);
		assertTrue(eq(bug.getAngle(), 95.0));
		tree = tree("turn", "100.0");
		bug.interpret(tree);
		assertTrue(eq(bug.getAngle(), 195.0));
		tree = tree("turn", "180.0");
		bug.interpret(tree);
		assertTrue(eq(bug.getAngle(), 15.0));
		tree = tree("turn", tree("-", "50.0"));
		bug.interpret(tree);
		assertTrue(eq(bug.getAngle(), 325.0));

	}

	@Test
	public void testinterpretTurnTo() {
		Tree<Token> tree = tree("turnto", "37.0");
		bug.interpret(tree);
		assertTrue(eq(bug.getAngle(), 37.0));
		tree = tree("turnto", tree("+", "200.0", "13.0"));
		bug.interpret(tree);
		assertTrue(eq(bug.getAngle(), 213.0));
		tree = tree("turnto", "400.0");
		bug.interpret(tree);
		assertTrue(eq(bug.getAngle(), 40.0));
		tree = tree("turnto", tree("-", "100.0"));
		bug.interpret(tree);
		assertTrue(eq(bug.getAngle(), 260.0));
	}

	@Test
	public void testinterpretReturn() {
		Tree<Token> tree = tree("return", "5.0");
		bug.interpret(tree);
		assertTrue(eq(bug.returnValue, 5.0));
	}

	@Test
	public void testinterpretLine() {
		//reset the toDraw arraylist for this test
		intr.toDraw = new ArrayList<Drawings>();
		bug.color = Color.GREEN;
		Tree<Token> tree = tree("line", "0.0", "0.0", "100.0", "100.0");
		bug.interpret(tree);
		assertEquals(intr.toDraw.size(), 1);
		assertTrue(eq(intr.toDraw.get(0).x1, 0.0));
		assertTrue(eq(intr.toDraw.get(0).y1, 0.0));
		assertTrue(eq(intr.toDraw.get(0).x2, 100.0));
		assertTrue(eq(intr.toDraw.get(0).y2, 100.0));
		assertEquals(intr.toDraw.get(0).color, Color.GREEN);
		
		bug.color = Color.RED;
		tree = tree("line", tree("+", "10.0", "10.0"), "20.0", "x", "y");
		bug.interpret(tree);
		assertEquals(intr.toDraw.size(), 2);
		assertTrue(eq(intr.toDraw.get(1).x1, 20.0));
		assertTrue(eq(intr.toDraw.get(1).y1, 20.0));
		assertTrue(eq(intr.toDraw.get(1).x2, 0.0));
		assertTrue(eq(intr.toDraw.get(1).y2, 0.0));
		assertEquals(intr.toDraw.get(1).color, Color.RED);
		

	}

	@Test
	public void testinterpretAssign() {
		Tree<Token> tree = tree("assign", "var1", "14.0");
		bug.interpret(tree);
		assertTrue(eq(bug.fetch("var1"), 14.0));
		bug.variableMap.put("mars", 0.0);
		tree = tree("assign", "mars", tree("-", "8.0"));
		bug.interpret(tree);
		assertTrue(eq(bug.fetch("mars"), -8.0));
		tree = tree("assign", "mars", tree("*", "2.0", "3.0"));
		bug.interpret(tree);
		assertTrue(eq(bug.fetch("mars"), 6.0));

	}
	
	@Test(expected = RuntimeException.class)
	public void testinterpretAssignError() {
		Tree<Token> tree = tree("assign", "jupiter", "10.0");
		bug.interpret(tree);
	}


	@Test
	public void testinterpretLoop() {
		Tree<Token> tree = tree("loop", tree("block", tree("exit", tree("=", "2.0", "2.0"), tree("move", "10.0"))));
		bug.interpret(tree);
		assertTrue(eq(bug.getX(), 0.0));
		assertTrue(eq(bug.getY(), 0.0));
		assertTrue(eq(bug.getAngle(), 0.0));
		
		tree = tree("loop", tree("block", tree("turnto", "140.0"), tree("moveto", "7.0", "7.0"), tree("exit", tree("<", "2.0", "4.0")), tree("turnto", "2.0")));
		bug.interpret(tree);
		assertTrue(eq(bug.getX(), 7.0));
		assertTrue(eq(bug.getY(), 7.0));
		assertTrue(eq(bug.getAngle(), 140.0));
		
		tree = tree("loop", tree("block", tree("assign", "x", tree("-", "x", "1.0")), tree("exit", tree("=", "x", "0.0")), tree("moveto", "x", "y")));
		bug.interpret(tree);
		assertTrue(eq(bug.getX(), 0.0));
		assertTrue(eq(bug.getY(), 7.0));
		assertTrue(eq(bug.getAngle(), 140.0));
	}

	@Test
	public void testinterpretSwitch() {
		Tree<Token> tree = tree("switch", tree("case", tree("<", "1.0", "3.0"), tree("move", "10.0")));
		bug.interpret(tree);
		assertTrue(eq(bug.getX(), 10.0));
		assertTrue(eq(bug.getY(), 0.0));
		
		tree = tree("switch", tree("case", tree(">", "1.0", "3.0"), tree("move", "10.0")), tree("case", tree("<=", "4.0", "4.0"), tree("turnto", "180.0")));
		bug.interpret(tree);
		assertTrue(eq(bug.getX(), 10.0));
		assertTrue(eq(bug.getY(), 0.0));
		assertTrue(eq(bug.getAngle(), 180.0));
		
		tree = tree("switch", tree("case", tree(">=", "1.0", "3.0"), tree("move", "10.0")), tree("case", tree("=", "4.0", "4.000001"), tree("turnto", "80.0")), tree("case", tree("<", "2.0", "4.0"), tree("moveto", "13.0", "13.0")));
		bug.interpret(tree);
		assertTrue(eq(bug.getX(), 10.0));
		assertTrue(eq(bug.getY(), 0.0));
		assertTrue(eq(bug.getAngle(), 80.0));
	}

	@Test
	public void testinterpretColor() {
		Tree<Token> tree = tree("color", "red");
		bug.interpret(tree);
		assertEquals(bug.getColor(), Color.RED);
		tree = tree("color", "black");
		bug.interpret(tree);
		assertEquals(bug.getColor(), Color.BLACK);
		tree = tree("color", "blue");
		bug.interpret(tree);
		assertEquals(bug.getColor(), Color.BLUE);
		tree = tree("color", "cyan");
		bug.interpret(tree);
		assertEquals(bug.getColor(), Color.CYAN);
		tree = tree("color", "darkGray");
		bug.interpret(tree);
		assertEquals(bug.getColor(), Color.DARK_GRAY);
		tree = tree("color", "gray");
		bug.interpret(tree);
		assertEquals(bug.getColor(), Color.GRAY);
		tree = tree("color", "green");
		bug.interpret(tree);
		assertEquals(bug.getColor(), Color.GREEN);
		tree = tree("color", "lightGray");
		bug.interpret(tree);
		assertEquals(bug.getColor(), Color.LIGHT_GRAY);
		tree = tree("color", "magenta");
		bug.interpret(tree);
		assertEquals(bug.getColor(), Color.MAGENTA);
		tree = tree("color", "orange");
		bug.interpret(tree);
		assertEquals(bug.getColor(), Color.ORANGE);
		tree = tree("color", "pink");
		bug.interpret(tree);
		assertEquals(bug.getColor(), Color.PINK);
		tree = tree("color", "white");
		bug.interpret(tree);
		assertEquals(bug.getColor(), Color.WHITE);
		tree = tree("color", "yellow");
		bug.interpret(tree);
		assertEquals(bug.getColor(), Color.YELLOW);
		tree = tree("color", "brown");
		bug.interpret(tree);
		assertEquals(bug.getColor(), new Color(135, 62, 3));
		tree = tree("color", "purple");
		bug.interpret(tree);
		assertEquals(bug.getColor(), new Color(143, 49, 214));
		tree = tree("color", "none");
		bug.interpret(tree);
		assertEquals(bug.getColor(), null);
	}

	@Test
	public void testinterpretFunction() {
		Tree<Token> tree = tree("function", "myfunc", tree("var"), tree("block"));
		bug.interpret(tree);
		assertEquals(bug.functions.get("myfunc"), tree);
		
		Tree<Token> tree2 = tree("function", "second", tree("var", "theresa"), tree("block", tree("moveto", "10.0", "10.0")));
		bug.interpret(tree2);
		assertEquals(bug.functions.get("second"), tree2);
		
	}
	
	@Test (expected = RuntimeException.class)
	public void testinterpretFunctionXVariable() {
		Tree<Token> tree = tree("function", "third", tree("var", "x"), tree("block"));
		bug.interpret(tree);
	}

	@Test (expected = RuntimeException.class)
	public void testinterpretFunctionYVariable() {
		Tree<Token> tree = tree("function", "third", tree("var", "y"), tree("block"));
		bug.interpret(tree);
	}
	
	@Test (expected = RuntimeException.class)
	public void testinterpretFunctionAngleVariable() {
		Tree<Token> tree = tree("function", "third", tree("var", "angle"), tree("block"));
		bug.interpret(tree);
	}
	
	@Test
	public void testevalArithmetic() {
		Tree<Token> tree = tree("+", "5.0");
		assertTrue(eq(5.0, bug.evaluate(tree)));
		tree = tree("-", "12.0");
		assertTrue(eq(-12.0, bug.evaluate(tree)));
		
		tree = tree("+", "4.0", "2.0");
		assertTrue(eq(6.0, bug.evaluate(tree)));
		tree = tree("-", "5.0", "1.0");
		assertTrue(eq(4.0, bug.evaluate(tree)));
		
		tree = tree("*", "3.0", "4.0");
		assertTrue(eq(12.0, bug.evaluate(tree)));
		tree = tree("*", tree("-", "1.0"), "10.0");
		assertTrue(eq(-10.0, bug.evaluate(tree)));
		
		tree = tree("/", "15.0", "5.0");
		assertTrue(eq(3.0, bug.evaluate(tree)));
		tree = tree("/", "3.0", tree("-", "2.0"));
		assertTrue(eq(-1.5, bug.evaluate(tree)));
	}

	@Test
	public void testevalCompare() {
		Tree<Token> tree = tree("<", "3.0", "5.0");
		assertTrue(eq(1.0, bug.evaluate(tree)));
		tree = tree("<", "12.0", "3.0");
		assertTrue(eq(0.0, bug.evaluate(tree)));
		tree = tree("<=", "1.0", "5.0");
		assertTrue(eq(1.0, bug.evaluate(tree)));
		tree = tree("<=", "4.0", "4.0");
		assertTrue(eq(1.0, bug.evaluate(tree)));
		tree = tree("<=", "6.0", "3.0");
		assertTrue(eq(0.0, bug.evaluate(tree)));
		tree = tree("=", "3.0", "3.0");
		assertTrue(eq(1.0, bug.evaluate(tree)));
		tree = tree("=", "2.0", "2.2");
		assertTrue(eq(0.0, bug.evaluate(tree)));
		tree = tree("!=", "2.0", "4.0");
		assertTrue(eq(1.0, bug.evaluate(tree)));
		tree = tree("!=", "5.0", "5.0002");
		assertTrue(eq(0.0, bug.evaluate(tree)));
		tree = tree(">", "7.0", "2.0");
		assertTrue(eq(1.0, bug.evaluate(tree)));
		tree = tree(">", "1.0", "3.0");
		assertTrue(eq(0.0, bug.evaluate(tree)));
		tree = tree(">=", "6.0", "4.0");
		assertTrue(eq(1.0, bug.evaluate(tree)));
		tree = tree(">=", "2.0", "2.0");
		assertTrue(eq(1.0, bug.evaluate(tree)));
		tree = tree(">=", "3.0", "10.0");
		assertTrue(eq(0.0, bug.evaluate(tree)));

	}

	@Test
	public void testevalCase() {
		Tree<Token> tree = tree("case", tree(">", "1.0", "3.0"), tree("block", tree("move", "10.0")));
		bug.evaluate(tree);
		assertTrue(eq(bug.getX(), 0.0));
		assertTrue(eq(bug.getY(), 0.0));
		assertTrue(eq(bug.getAngle(), 0.0));
		
		tree = tree("case", tree("<", "1.0", "3.0"), tree("block", tree("move", "10.0")));
		bug.evaluate(tree);
		assertTrue(eq(bug.getX(), 10.0));
		assertTrue(eq(bug.getY(), 0.0));
		assertTrue(eq(bug.getAngle(), 0.0));
		
		tree = tree("case", tree("=", "2.0", "2.00001"), tree("block", tree("turnto", "270.0"), tree("move", "10.0")));
		bug.evaluate(tree);
		assertTrue(eq(bug.getX(), 10.0));
		assertTrue(eq(bug.getY(), 10.0));
		assertTrue(eq(bug.getAngle(), 270.0));
				
	}

	@Test
	public void testevalCall() {
		Tree<Token> myfunc = tree("function", "myfunction", tree("var", "a", "b"), tree("block", tree("return", tree("+", "a", "b"))));
		bug.interpret(myfunc);
		bug.variableMap.put("argA", 2.0);
		bug.variableMap.put("argB", 3.0);
		Tree<Token> funcCall = tree("call", "myfunction", tree("var", "argA", "argB"));
		double answer = bug.evaluate(funcCall);
		assertTrue(eq(answer, 5.0));
		
		//function defined in allbugs
		Tree<Token> bugFunc = tree("function", "bugFunc", tree("var", "first", "second", "third"), tree("block", tree("assign", "third", tree("*", "first", "second")), tree("return", "third")));
		bug.interpreter.functions.put("bugFunc", bugFunc);
		Tree<Token> caller = tree("call", "bugFunc", tree("var", "argA", "argB", "0.0"));
		answer = bug.evaluate(caller);
		assertTrue(eq(answer, 6.0));
		
		//special function
		Tree<Token> distFunc = tree("call", "distance", tree("var", "bug2"));
		answer = bug.evaluate(distFunc);
		assertTrue(eq(answer, 14.1421));
		
		Tree<Token> dirFunc = tree("call", "direction", tree("var", "bug2"));
		answer = bug.evaluate(dirFunc);
		assertTrue(eq(answer, 315.0));
	}
	
	@Test (expected = RuntimeException.class)
	public void testevalCallNameError() {
		Tree<Token> calling = tree("call", "something", tree("var", "argA", "argB"));
		bug.evaluate(calling);
	}
	
	@Test (expected = RuntimeException.class)
	public void testevalCallBad() {
		Tree<Token> myfunc = tree("function", "myfunction", tree("var", "a", "b"), tree("block", tree("return", tree("+", "a", "b"))));
		bug.interpret(myfunc);
		Tree<Token> caller = tree("call", "myfunction", tree("var", "2.0"));
		bug.evaluate(caller);
	}
	
	/**
	 * Helper method to compare doubles with our .001 standard
	 * @param a
	 * @param b
	 * @return true if a and b are within .001 of each other, false if not.
	 */
	private static boolean eq(double a, double b) {
		if (a - b < .001 && a - b > -.001) return true;
		return false;
	}

	@Test
	public void testEq() {
		assertTrue(eq(1.0, 1.0));
		assertTrue(eq(1.00004, 1.0));
		assertTrue(eq(1.0, 1.00004));
		assertFalse(eq(1.0, 2.0));
		assertFalse(eq(1.002, 1.0));
		assertFalse(eq(1.0, 1.002));
	}
	
	 /**
     * Returns a Tree node consisting of a single leaf; the
     * node will contain a Token with a String as its value. <br>
     * Given a Tree, return the same Tree.<br>
     * Given a Token, return a Tree with the Token as its value.<br>
     * Given a String, make it into a Token, return a Tree
     * with the Token as its value.
     * 
     * @param value A Tree, Token, or String from which to
              construct the Tree node.
     * @return A Tree leaf node containing a Token whose value
     *         is the parameter.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	private Tree<Token> createNode(Object value) {
        if (value instanceof Tree) {
            return (Tree) value;
        }
        if (value instanceof Token) {
            return new Tree<Token>((Token) value);
        }
        else if (value instanceof String) {
            return new Tree<Token>(new Token((String) value));
        }
        assert false: "Illegal argument: tree(" + value + ")";
        return null; 
    }
    
	/**
     * Builds a Tree that can be compared with the one the
     * Parser produces. Any String or Token arguments will be
     * converted to Tree nodes containing Tokens.
     * 
     * @param op The String value to use in the Token in the root.
     * @param children The objects to be made into children.
     * @return The resultant Tree.
     */
    private Tree<Token> tree(String op, Object... children) {
        @SuppressWarnings("unchecked")
		Tree<Token> tree = new Tree<Token>(new Token(op));
        for (int i = 0; i < children.length; i++) {
            tree.addChild(createNode(children[i]));
        }
        return tree;
    }

}
