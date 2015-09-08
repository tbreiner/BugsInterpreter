package bugs;

import static org.junit.Assert.*;

import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;

import org.junit.Before;
import org.junit.Test;

import tree.Tree;


/**
 * Test class for Parser
 * @author theresabreiner
 *
 */
public class ParserTest {
    Parser parser;

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testParser() {
        parser = new Parser("");
        parser = new Parser("2 + 2");
    }

    @Test
    public void testIsFuncDef() {
    	use("exit if 300 >= 200 \n");
		assertTrue(parser.isExitIfStatement());
		assertEquals(tree("exit", tree(">=", "300.0", "200.0")), stackTop());
    }
    
    @Test
    public void testIsExpression() {
        Tree<Token> expected;
        
        use("250");
        assertTrue(parser.isExpression());
        assertStackTopEquals(createNode("250.0"));
        
        use("hello");
        assertTrue(parser.isExpression());
        assertStackTopEquals(createNode("hello"));

        use("(xyz + 3)");
        assertTrue(parser.isExpression());
        assertStackTopEquals(tree("+", "xyz", "3.0"));

        use("a + b + c");
        assertTrue(parser.isExpression());
        assertStackTopEquals(tree("+", tree("+", "a", "b"), "c"));

        use("a * b * c");
        assertTrue(parser.isExpression());
        assertStackTopEquals(tree("*", tree("*", "a", "b"), "c"));

        use("3 * 12.5 - 7");
        assertTrue(parser.isExpression());
        assertStackTopEquals(tree("-", tree("*", "3.0", "12.5"), createNode("7.0")));

        use("12 * 5 - 3 * 4 / 6 + 8");
        assertTrue(parser.isExpression());
        expected = tree("+",
                      tree("-",
                         tree("*", "12.0", "5.0"),
                         tree("/",
                            tree("*", "3.0", "4.0"),
                            "6.0"
                           )
                        ),
                      "8.0"
                     );
        assertStackTopEquals(expected);
                     
        use("12 * ((5 - 3) * 4) / 6 + (8)");
        assertTrue(parser.isExpression());
        expected = tree("+",
                      tree("/",
                         tree("*",
                            "12.0",
                            tree("*",
                               tree("-","5.0","3.0"),
                               "4.0")),
                         "6.0"),
                      "8.0");
        assertStackTopEquals(expected);
        
        use("");
        assertFalse(parser.isExpression());
        
        use("#");
        assertFalse(parser.isExpression());

        try {
            use("17 +");
            assertFalse(parser.isExpression());
            fail();
        }
        catch (SyntaxException e) {
        }
        try {
            use("22 *");
            assertFalse(parser.isExpression());
            fail();
        }
        catch (SyntaxException e) {
        }
    }

    @Test
    public void testUnaryOperator() {       
        use("-250");
        assertTrue(parser.isExpression());
        assertStackTopEquals(tree("-", "250.0"));
        
        use("+250");
        assertTrue(parser.isExpression());
        assertStackTopEquals(tree("+", "250.0"));
        
        use("- hello");
        assertTrue(parser.isExpression());
        assertStackTopEquals(tree("-", "hello"));

        use("-(xyz + 3)");
        assertTrue(parser.isExpression());
        assertStackTopEquals(tree("-", tree("+", "xyz", "3.0")));

        use("(-xyz + 3)");
        assertTrue(parser.isExpression());
        assertStackTopEquals(tree("+", tree("-", "xyz"), "3.0"));

        use("+(-xyz + 3)");
        assertTrue(parser.isExpression());
        assertStackTopEquals(tree("+",
                                        tree("+",
                                                   tree("-", "xyz"), "3.0")));
    }

    @Test
    public void testIsTerm() {        
        use("12");
        assertTrue(parser.isTerm());
        assertStackTopEquals(createNode("12.0"));
        
        use("12.5");
        assertTrue(parser.isTerm());
        assertStackTopEquals(createNode("12.5"));

        use("3*12");
        assertTrue(parser.isTerm());
        assertStackTopEquals(tree("*", "3.0", "12.0"));

        use("x * y * z");
        assertTrue(parser.isTerm());
        assertStackTopEquals(tree("*", tree("*", "x", "y"), "z"));
        
        use("20 * 3 / 4");
        assertTrue(parser.isTerm());
        assertEquals(tree("/", tree("*", "20.0", "3.0"), createNode("4.0")),
                     stackTop());

        use("20 * 3 / 4 + 5");
        assertTrue(parser.isTerm());
        assertEquals(tree("/", tree("*", "20.0", "3.0"), "4.0"),
                     stackTop());
        followedBy(parser, "+ 5");
        
        use("");
        assertFalse(parser.isTerm());
        followedBy(parser, "");
        
        use("#");
        assertFalse(parser.isTerm());followedBy(parser, "#");

    }

    @Test
    public void testIsFactor() {
        use("12");
        assertTrue(parser.isFactor());
        assertStackTopEquals(createNode("12.0"));

        use("hello");
        assertTrue(parser.isFactor());
        assertStackTopEquals(createNode("hello"));
        
        use("(xyz + 3)");
        assertTrue(parser.isFactor());
        assertStackTopEquals(tree("+", "xyz", "3.0"));
        
        use("12 * 5");
        assertTrue(parser.isFactor());
        assertStackTopEquals(createNode("12.0"));
        followedBy(parser, "* 5.0");
        
        use("17 +");
        assertTrue(parser.isFactor());
        assertStackTopEquals(createNode("17.0"));
        followedBy(parser, "+");

        use("");
        assertFalse(parser.isFactor());
        followedBy(parser, "");
        
        use("#");
        assertFalse(parser.isFactor());
        followedBy(parser, "#");
    }

    @Test
    public void testIsFactor2() {
        use("hello.world");
        assertTrue(parser.isFactor());
        assertStackTopEquals(tree(".", "hello", "world"));
        
        use("foo(bar)");
        assertTrue(parser.isFactor());
        assertStackTopEquals(tree("call", "foo",
                                        tree("var", "bar")));
        
        use("foo(bar, baz)");
        assertTrue(parser.isFactor());
        assertStackTopEquals(tree("call", "foo",
                                        tree("var", "bar", "baz")));
        
        use("foo(2*(3+4))");
        assertTrue(parser.isFactor());
        assertStackTopEquals(tree("call", "foo",
                                 tree("var",
                                     tree("*", "2.0",
                                         tree("+", "3.0", "4.0")))));
    }

    @Test
    public void testIsAddOperator() {
        use("+ - + $");
        assertTrue(parser.isAddOperator());
        assertTrue(parser.isAddOperator());
        assertTrue(parser.isAddOperator());
        assertFalse(parser.isAddOperator());
        followedBy(parser, "$");
    }

    @Test
    public void testIsMultiplyOperator() {
        use("* / $");
        assertTrue(parser.isMultiplyOperator());
        assertTrue(parser.isMultiplyOperator());
        assertFalse(parser.isMultiplyOperator());
        followedBy(parser, "$");
    }

    @Test
    public void testNextToken() {
        use("12 12.5 bogus switch + \n");
        assertEquals(new Token(Token.Type.NUMBER, "12.0"), parser.nextToken());
        assertEquals(new Token(Token.Type.NUMBER, "12.5"), parser.nextToken());
        assertEquals(new Token(Token.Type.NAME, "bogus"), parser.nextToken());
        assertEquals(new Token(Token.Type.KEYWORD, "switch"), parser.nextToken());
        assertEquals(new Token(Token.Type.SYMBOL, "+"), parser.nextToken());
        assertEquals(new Token(Token.Type.EOL, "\n"), parser.nextToken());
        assertEquals(new Token(Token.Type.EOF, "EOF"), parser.nextToken());
    }


	@Test
	public void testIsArithmeticExpression() {
		use("3 + 4");
		assertTrue(parser.isArithmeticExpression());
		assertStackTopEquals(tree("+", "3.0", "4.0"));
		
		use("12 * -4 + 1");
		assertTrue(parser.isArithmeticExpression());
		assertStackTopEquals(tree("+", tree("*", "12.0", tree("-", "4.0")), "1.0"));
		
		use("theresa hello");
		assertTrue(parser.isArithmeticExpression());
		assertStackTopEquals(tree("theresa"));
		followedBy(parser, "hello");
	}

	@Test
	public void testIsUnsignedFactor() {
		use("theresa.hello");
		assertTrue(parser.isUnsignedFactor());
		assertStackTopEquals(tree(".", "theresa", "hello"));
		
		use("func(arg1, arg2, arg3)");
		assertTrue(parser.isUnsignedFactor());
		assertStackTopEquals(tree("call", "func", tree("var", "arg1", "arg2", "arg3")));
		
		use("theresa hello");
		assertTrue(parser.isUnsignedFactor());
		assertStackTopEquals(tree("theresa"));
		followedBy(parser, "hello");
		
		use("12345");
		assertTrue(parser.isUnsignedFactor());
		assertStackTopEquals(tree("12345.0"));

		use("(1 + 2)");
		assertTrue(parser.isUnsignedFactor());
		assertStackTopEquals(tree("+", "1.0", "2.0"));
	}

	@Test
	public void testIsParameterList() {
		use("(arg1, arg2, arg3)");
		assertTrue(parser.isParameterList());
		assertStackTopEquals(tree("var", "arg1", "arg2", "arg3"));
		
		use("(arg1)");
		assertTrue(parser.isParameterList());
		assertStackTopEquals(tree("var", "arg1"));
		
		use("()");
		assertTrue(parser.isParameterList());
		assertStackTopEquals(tree("var"));
	}

	@Test
	public void testIsVariable() {
		use("theresa");
		assertTrue(parser.isVariable());
		assertStackTopEquals(tree("theresa"));
		
		use("theresa14");
		assertTrue(parser.isVariable());
		assertStackTopEquals(tree("theresa14"));
	}

	@Test
	public void testIsAction() {
		use("move this \n");
		assertTrue(parser.isAction());
		assertStackTopEquals(tree("move", "this"));
		
		use("moveto there, this \n");
		assertTrue(parser.isAction());
		assertStackTopEquals(tree("moveto", "there", "this"));
		
		use("turn left \n");
		assertTrue(parser.isAction());
		assertStackTopEquals(tree("turn", "left"));
		
		use("turnto me \n");
		assertTrue(parser.isAction());
		assertStackTopEquals(tree("turnto", "me"));
		
		use("line a, b, c, d \n");
		assertTrue(parser.isAction());
		assertStackTopEquals(tree("line", "a", "b", "c", "d"));
	}

	@Test
	public void testIsAllbugsCode() {
		use("Allbugs { \n } \n");
		assertTrue(parser.isAllbugsCode());
		assertStackTopEquals(tree("Allbugs", tree("list"), tree("list")));
		
		use("Allbugs { \n var var1, var2 \n } \n");
		assertTrue(parser.isAllbugsCode());
		assertStackTopEquals(tree("Allbugs", tree("list", tree("var", "var1", "var2")), tree("list")));
		
		use("Allbugs { \n var var1, var2 \n var var3, var4 \n } \n");
		assertTrue(parser.isAllbugsCode());
		assertStackTopEquals(tree("Allbugs", tree("list", tree("var", "var1", "var2"), tree("var", "var3", "var4")), tree("list")));
		
		use("Allbugs { \n var var1, var2 \n define func using something {\n }\n }\n");
		assertTrue(parser.isAllbugsCode());
		assertStackTopEquals(tree("Allbugs", tree("list", tree("var", "var1", "var2")), tree("list", tree("function", "func", tree("var", "something"), tree("block")))));
		
		use("Allbugs { \n var var1, var2 \n define func using something, else {\n }\n define func2 {\n turn right \n }\n }\n");
		assertTrue(parser.isAllbugsCode());
		assertStackTopEquals(tree("Allbugs", tree("list", tree("var", "var1", "var2")), tree("list", tree("function", "func", tree("var", "something", "else"), tree("block")), tree("function", "func2", tree("var"), tree("block", tree("turn", "right"))))));
		
		use("Allbugs {\n define func using something, else {\n turn left \n }\n }\n");
		assertTrue(parser.isAllbugsCode());
		assertStackTopEquals(tree("Allbugs", tree("list"), tree("list", tree("function", "func", tree("var", "something", "else"), tree("block", tree("turn", "left"))))));
	}

	@Test
	public void testIsAssignmentStatement() {
		use("theresa = hello \n");
		assertTrue(parser.isAssignmentStatement());
		assertStackTopEquals(tree("assign", "theresa", "hello"));
		
		use("theresa = 3 + 4 - 6 \n");
		assertTrue(parser.isAssignmentStatement());
		assertStackTopEquals(tree("assign", "theresa", tree("-", tree("+", "3.0", "4.0"), "6.0")));
		
		
	}

	@Test
	public void testIsBlock() {
		use("{\n}\n");
		assertTrue(parser.isBlock());
		assertStackTopEquals(tree("block"));
		
		use("{\n turn left \n }\n");
		assertTrue(parser.isBlock());
		assertStackTopEquals(tree("block", tree("turn", "left")));
		
		use("{\n turn left \n move right\n }\n");
		assertTrue(parser.isBlock());
		assertStackTopEquals(tree("block", tree("turn", "left"), tree("move", "right")));
		
		use("{\n turn left \n theresa = hello \n }\n");
		assertTrue(parser.isBlock());
		assertStackTopEquals(tree("block", tree("turn", "left"), tree("assign", "theresa", "hello")));
		
		use("{\n turn left \n move right \n line a, b, c, d \n }\n");
		assertTrue(parser.isBlock());
		assertStackTopEquals(tree("block", tree("turn", "left"), tree("move", "right"), tree("line", "a", "b", "c", "d")));
		
	}

	@Test
	public void testIsBugDefinition() {
		use("Bug theresa { \n turn left \n } \n");
		assertTrue(parser.isBugDefinition());
		assertStackTopEquals(tree("Bug", "theresa", tree("list"), tree("initially", tree("block")), tree("block", tree("turn", "left")), tree("list")));
		
		use("Bug theresa { \n turn left \n move right \n } \n");
		assertTrue(parser.isBugDefinition());
		assertStackTopEquals(tree("Bug", "theresa", tree("list"), tree("initially", tree("block")), tree("block", tree("turn", "left"), tree("move", "right")), tree("list")));
		
		use("Bug theresa { \n var var1, var2 \n turn left \n move right \n } \n");
		assertTrue(parser.isBugDefinition());
		assertStackTopEquals(tree("Bug", "theresa", tree("list", tree("var", "var1", "var2")), tree("initially", tree("block")), tree("block", tree("turn", "left"), tree("move", "right")), tree("list")));
		
		use("Bug theresa { \n var var1, var2 \n var v3, v4 \n turn left \n move right \n } \n");
		assertTrue(parser.isBugDefinition());
		assertStackTopEquals(tree("Bug", "theresa", tree("list", tree("var", "var1", "var2"), tree("var", "v3", "v4")), tree("initially", tree("block")), tree("block", tree("turn", "left"), tree("move", "right")), tree("list")));
		
		use("Bug theresa { \n initially {\n move away \n }\n turn left \n } \n");
		assertTrue(parser.isBugDefinition());
		assertStackTopEquals(tree("Bug", "theresa", tree("list"), tree("initially", tree("block", tree("move", "away"))), tree("block", tree("turn", "left")), tree("list")));
		
		use("Bug theresa { \n initially {\n move away \n }\n turn left \n define something using stuff {\n}\n } \n");
		assertTrue(parser.isBugDefinition());
		assertStackTopEquals(tree("Bug", "theresa", tree("list"), tree("initially", tree("block", tree("move", "away"))), tree("block", tree("turn", "left")), tree("list", tree("function", "something", tree("var", "stuff"), tree("block")))));
		
	}

	@Test
	public void testIsColorStatement() {
		use("color blue \n\n\n");
		assertTrue(parser.isColorStatement());
		assertStackTopEquals(tree("color", "blue"));
		
		use("color red \n");
		assertTrue(parser.isColorStatement());
		assertStackTopEquals(tree("color", "red"));
	}

	@Test
	public void testIsCommand() {
		use("turn left\n");
		assertTrue(parser.isCommand());
		assertStackTopEquals(tree("turn", "left"));
		
		use("exit if finished\n");
		assertTrue(parser.isCommand());
		assertStackTopEquals(tree("exit", "finished"));
	}

	@Test
	public void testIsComparator() {
		use("= != < > <= >= !4");
		assertTrue(parser.isComparator());
		assertStackTopEquals(tree("="));
		assertTrue(parser.isComparator());
		assertStackTopEquals(tree("!="));
		assertTrue(parser.isComparator());
		assertStackTopEquals(tree("<"));
		assertTrue(parser.isComparator());
		assertStackTopEquals(tree(">"));
		assertTrue(parser.isComparator());
		assertStackTopEquals(tree("<="));
		assertTrue(parser.isComparator());
		assertStackTopEquals(tree(">="));
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsComparatorBang() {
		use("!4");
		parser.isComparator();
	}

	@Test
	public void testIsDoStatement() {
		use("do stuff \n");
		assertTrue(parser.isDoStatement());
		assertStackTopEquals(tree("call", "stuff", tree("var")));
		
		use("do stuff(arg1) \n \n \n");
		assertTrue(parser.isDoStatement());
		assertStackTopEquals(tree("call", "stuff", tree("var", "arg1")));
		
		use("do stuff(arg1, arg2, arg3) \n");
		assertTrue(parser.isDoStatement());
		assertStackTopEquals(tree("call", "stuff", tree("var", "arg1", "arg2", "arg3")));
	}

	@Test
	public void testIsEol() {
		use("theresa \n");
		assertFalse(parser.isEol());
		assertTrue(parser.isVariable());
		assertTrue(parser.isEol());
		assertStackTopEquals(tree("theresa"));
		
		use("\n");
		assertTrue(parser.isEol());
	}

	@Test
	public void testIsExitIfStatement() {
		use("exit if something \n");
		assertTrue(parser.isExitIfStatement());
		assertStackTopEquals(tree("exit", "something"));
		
		use("exit if something + else < total \n");
		assertTrue(parser.isExitIfStatement());
		assertStackTopEquals(tree("exit", tree("<", tree("+", "something", "else"), "total")));
	}

	@Test
	public void testIsFunctionCall() {
		use("func(arg1, arg2, arg3)");
		assertTrue(parser.isFunctionCall());
		assertStackTopEquals(tree("call", "func", tree("var", "arg1", "arg2", "arg3")));

		use("func()");
		assertTrue(parser.isFunctionCall());
		assertStackTopEquals(tree("call", "func", tree("var")));
	}

	@Test
	public void testIsFunctionDefinition() {
		use("define theresa using var1, var2 {\n turn left \n }\n");
		assertTrue(parser.isFunctionDefinition());
		assertStackTopEquals(tree("function", "theresa", tree("var", "var1", "var2"), tree("block", tree("turn", "left"))));
		
		use("define theresa using var1 {\n move right \n }\n");
		assertTrue(parser.isFunctionDefinition());
		assertStackTopEquals(tree("function", "theresa", tree("var", "var1"), tree("block", tree("move", "right"))));
		
		use("define theresa {\n line a, b, c, d\n move left \n}\n");
		assertTrue(parser.isFunctionDefinition());
		assertStackTopEquals(tree("function", "theresa", tree("var"), tree("block", tree("line", "a", "b", "c", "d"), tree("move", "left"))));
	}

	@Test
	public void testIsInitializationBlock() {
		use("initially {\n move left\n }\n");
		assertTrue(parser.isInitializationBlock());
		assertStackTopEquals(tree("initially", tree("block", tree("move", "left"))));
		
		use("initially {\n move left\n turn right\n }\n");
		assertTrue(parser.isInitializationBlock());
		assertStackTopEquals(tree("initially", tree("block", tree("move", "left"), tree("turn", "right"))));
	}

	@Test
	public void testIsLineAction() {
		use("line hi, bye, in, out \n");
		assertTrue(parser.isLineAction());
		assertStackTopEquals(tree("line", "hi", "bye", "in", "out"));
		
		use("line 1 + 2, 3 != 4, 5 < 6, 7 / 1\n\n");
		assertTrue(parser.isLineAction());
		assertStackTopEquals(tree("line", tree("+", "1.0", "2.0"), tree("!=", "3.0", "4.0"), tree("<", "5.0", "6.0"), tree("/", "7.0", "1.0")));
		
		use("line the, end + 1, yes, no * 55\n");
		assertTrue(parser.isLineAction());
		assertStackTopEquals(tree("line", "the", tree("+", "end", "1.0"), "yes", tree("*", "no", "55.0")));
	}

	@Test
	public void testIsLoopStatement() {
		use("loop {\n move left\n }\n");
		assertTrue(parser.isLoopStatement());
		assertStackTopEquals(tree("loop", tree("block", tree("move", "left"))));
		
		use("loop {\n}\n");
		assertTrue(parser.isLoopStatement());
		assertStackTopEquals(tree("loop", tree("block")));
	}

	@Test
	public void testIsMoveAction() {
		use("move it \n");
		assertTrue(parser.isMoveAction());
		assertStackTopEquals(tree("move", "it"));
		
		use("move it + it \n \n");
		assertTrue(parser.isMoveAction());
		assertStackTopEquals(tree("move", tree("+", "it", "it")));
		
		use("move out < billy \n");
		assertTrue(parser.isMoveAction());
		assertStackTopEquals(tree("move", tree("<", "out", "billy")));
	}

	@Test
	public void testIsMoveToAction() {
		use("moveto rome, mama \n");
		assertTrue(parser.isMoveToAction());
		assertStackTopEquals(tree("moveto", "rome", "mama"));
		
		use("moveto 3 + 7, howdy != doody \n \n");
		assertTrue(parser.isMoveToAction());
		assertStackTopEquals(tree("moveto", tree("+", "3.0", "7.0"), tree("!=", "howdy", "doody")));
		
		use("moveto 12345, 56678 \n");
		assertTrue(parser.isMoveToAction());
		assertStackTopEquals(tree("moveto", "12345.0", "56678.0"));
	}

	@Test
	public void testIsProgram() {
		use("Bug theresa { \n turn left \n } \n");
		assertTrue(parser.isProgram());
		assertStackTopEquals(tree("program", tree("Allbugs", tree("list"), tree("list")), tree("list", tree("Bug", "theresa", tree("list"), tree("initially", tree("block")), tree("block", tree("turn", "left")), tree("list")))));
		
		use("Allbugs { \n } \n Bug theresa { \n var var1 \n turn left \n } \n");
		assertTrue(parser.isProgram());
		assertStackTopEquals(tree("program", tree("Allbugs", tree("list"), tree("list")), tree("list", tree("Bug", "theresa", tree("list", tree("var", "var1")), tree("initially", tree("block")), tree("block", tree("turn", "left")), tree("list")))));
		
		use("Allbugs { \n } \n Bug theresa { \n var var1 \n turn left \n } \n Bug zach { \n initially {\n turn right \n }\n move away \n } \n");
		assertTrue(parser.isProgram());
		assertStackTopEquals(tree("program", tree("Allbugs", tree("list"), tree("list")), tree("list", tree("Bug", "theresa", tree("list", tree("var", "var1")), tree("initially", tree("block")), tree("block", tree("turn", "left")), tree("list")), tree("Bug", "zach", tree("list"), tree("initially", tree("block", tree("turn", "right"))), tree("block", tree("move", "away")), tree("list")))));
		
		use("Bug theresa { \n var var1 \n turn left \n } \n Bug zach { \n initially {\n turn right \n }\n move away \n } \n");
		assertTrue(parser.isProgram());
		assertStackTopEquals(tree("program", tree("Allbugs", tree("list"), tree("list")), tree("list", tree("Bug", "theresa", tree("list", tree("var", "var1")), tree("initially", tree("block")), tree("block", tree("turn", "left")), tree("list")), tree("Bug", "zach", tree("list"), tree("initially", tree("block", tree("turn", "right"))), tree("block", tree("move", "away")), tree("list")))));
		
	}

	@Test
	public void testIsReturnStatement() {
		use("return theresa \n");
		assertTrue(parser.isReturnStatement());
		assertStackTopEquals(tree("return", "theresa"));
		
		use("return 3 < 5 + 1 \n \n");
		assertTrue(parser.isReturnStatement());
		assertStackTopEquals(tree("return", tree("<", "3.0", tree("+", "5.0", "1.0"))));
		
		use("return 4 * 3 / 1 != 6 \n");
		assertTrue(parser.isReturnStatement());
		assertStackTopEquals(tree("return", tree("!=", tree("/", tree("*", "4.0", "3.0"), "1.0"), "6.0")));
	}

	@Test
	public void testIsStatement() {
		use("theresa = 3 + 4 / 6 <= 15 \n");
		assertTrue(parser.isStatement());
		assertStackTopEquals(tree("assign", "theresa", tree("<=", tree("+", "3.0", tree("/", "4.0", "6.0")), "15.0")));
		
		use("loop {\n turn left \n }\n");
		assertTrue(parser.isStatement());
		assertStackTopEquals(tree("loop", tree("block", tree("turn", "left"))));
		
		use("exit if surprised \n");
		assertTrue(parser.isStatement());
		assertStackTopEquals(tree("exit", tree("surprised")));
		
		use("switch {\n case one \n }\n");
		assertTrue(parser.isStatement());
		assertStackTopEquals(tree("switch", tree("case", "one", tree("block"))));
		
		use("return never \n");
		assertTrue(parser.isStatement());
		assertStackTopEquals(tree("return", tree("never")));
		
		use("do something(arg1)\n");
		assertTrue(parser.isStatement());
		assertStackTopEquals(tree("call", "something", tree("var", "arg1")));
		
		use("color purple \n");
		assertTrue(parser.isStatement());
		assertStackTopEquals(tree("color", "purple"));
	}

	@Test
	public void testIsSwitchStatement() {

		use("switch {\n }\n");
		assertTrue(parser.isSwitchStatement());
		assertStackTopEquals(tree("switch"));
		
		use("switch {\n case 3 < 4 \n turn right \n } \n");
		assertTrue(parser.isSwitchStatement());
		assertStackTopEquals(tree("switch", tree("case", tree("<", "3.0", "4.0"), tree("block", tree("turn", "right")))));
		
		use("switch {\n case one\n turn left\n move right\n case two \n theresa = 3 \n} \n");
		assertTrue(parser.isSwitchStatement());
		assertStackTopEquals(tree("switch", tree("case", tree("one"), tree("block", tree("turn", "left"), tree("move", "right"))), tree("case", tree("two"), tree("block", tree("assign", "theresa", "3.0")))));
		
	}

	@Test
	public void testIsTurnAction() {
		use("turn left \n");
		assertTrue(parser.isTurnAction());
		assertStackTopEquals(tree("turn", "left"));
		
		use("turn 3 + 4 \n");
		assertTrue(parser.isTurnAction());
		assertStackTopEquals(tree("turn", tree("+", "3.0", "4.0")));
		
		use("turn theresa > zach \n \n");
		assertTrue(parser.isTurnAction());
		assertStackTopEquals(tree("turn", tree(">", "theresa", "zach")));
		
		use("turn 1 < 2 < 3 != 4 \n");
		assertTrue(parser.isTurnAction());
		assertStackTopEquals(tree("turn", tree("!=", tree("<", tree("<", "1.0", "2.0"), "3.0"), "4.0")));
		
	}

	@Test
	public void testIsTurnToAction() {
		use("turnto theleft \n");
		assertTrue(parser.isTurnToAction());
		assertStackTopEquals(tree("turnto", "theleft"));
		
		use("turnto theright != theleft \n");
		assertTrue(parser.isTurnToAction());
		assertStackTopEquals(tree("turnto", tree("!=", "theright", "theleft")));
		
		use("turnto 1 <= 2 <= 3 = 4 \n\n");
		assertTrue(parser.isTurnToAction());
		assertStackTopEquals(tree("turnto", tree("=", tree("<=", tree("<=", "1.0", "2.0"), "3.0"), "4.0")));
	}

	@Test
	public void testIsVarDeclaration() {
		use("var theresa \n");
		assertTrue(parser.isVarDeclaration());
		assertStackTopEquals(tree("var", "theresa"));
		
		use("var theresa, breiner, hello \n");
		assertTrue(parser.isVarDeclaration());
		assertStackTopEquals(tree("var", "theresa", "breiner", "hello"));
	}

    
//  ----- "Helper" methods
    
    /**
     * Sets the <code>parser</code> instance to use the given string.
     * 
     * @param s The string to be parsed.
     */
    private void use(String s) {
        parser = new Parser(s);
    }
    
    /**
     * Returns the current top of the stack.
     *
     * @return The top of the stack.
     */
    private Object stackTop() {
        return parser.stack.peek();
    }
    
    /**
     * Tests whether the top element in the stack is correct.
     *
     * @return <code>true</code> if the top element of the stack is as expected.
     */
    private void assertStackTopEquals(Tree<Token> expected) {
        assertEquals(expected, stackTop());
    }
    
    /**
     * This method is given a String containing some or all of the
     * tokens that should yet be returned by the Tokenizer, and tests
     * whether the Tokenizer in fact has those Tokens. To succeed,
     * everything in the given String must still be in the Tokenizer,
     * but there may be additional (untested) Tokens to be returned.
     * This method is primarily to test whether Tokens are pushed
     * back appropriately.
     * @param parser TODO
     * @param expectedTokens The Tokens we expect to get from the Tokenizer.
     */
    private void followedBy(Parser parser, String expectedTokens) {
        int expectedType;
        int actualType;
        StreamTokenizer actual = parser.tokenizer;

        Reader reader = new StringReader(expectedTokens);
        StreamTokenizer expected = new StreamTokenizer(reader);

        try {
            while (true) {
                expectedType = expected.nextToken();
                if (expectedType == StreamTokenizer.TT_EOF) break;
                actualType = actual.nextToken();
                assertEquals(typeName(expectedType), typeName(actualType));
                if (actualType == StreamTokenizer.TT_WORD) {
                    assertEquals(expected.sval, actual.sval);
                }
                else if (actualType == StreamTokenizer.TT_NUMBER) {
                    assertEquals(expected.nval, actual.nval, 0.001);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private String typeName(int type) {
        switch(type) {
            case StreamTokenizer.TT_EOF: return "EOF";
            case StreamTokenizer.TT_EOL: return "EOL";
            case StreamTokenizer.TT_WORD: return "WORD";
            case StreamTokenizer.TT_NUMBER: return "NUMBER";
            default: return "'" + (char)type + "'";
        }
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
        Tree<Token> tree = new Tree<Token>(new Token(op));
        for (int i = 0; i < children.length; i++) {
            tree.addChild(createNode(children[i]));
        }
        return tree;
    }
}