package bugs;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import tree.Tree;

/**
 * Class to test the interpreter.
 * @author theresabreiner
 *
 */
public class InterpreterTest {

	Interpreter in;
	
	@Before
	public void setUp() {
		in = new Interpreter();
		in.program = tree("program", tree("Allbugs", tree("list", tree("var", "hey", "ho")), tree("list", tree("function", "doIt", tree("var", "a", "b"), tree("block", tree("+", "a", "5.0"))))), tree("list", tree("Bug", "tt", tree("list"), tree("initially", tree("block")), tree("block"), tree("list"))));
	}
	
	@Test
	public void testInterpreter() {
		in = new Interpreter();
		assertEquals(in.variables.size(), 0);
		assertEquals(in.functions.size(), 0);
		assertEquals(in.program, null);
		assertEquals(in.bugsMap.size(), 0);
		assertEquals(in.bugs.size(), 0);
		assertEquals(in.toDraw.size(), 0);
	}
	
	@Test
	public void testsetUp() {
		in.setUp();
		assertTrue(eq(in.variables.get("hey"), 0.0));
		assertTrue(eq(in.variables.get("ho"), 0.0));
		assertEquals(in.functions.get("doIt"), tree("function", "doIt", tree("var", "a", "b"), tree("block", tree("+", "a", "5.0"))));
		assertFalse(in.bugsMap.get("tt") == null);
		

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
