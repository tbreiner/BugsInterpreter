package bugs;

import static org.junit.Assert.*;

import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;

import org.junit.Before;
import org.junit.Test;


/**
 * Test class for Bugs recognizer.
 * @author David Matuszek and Theresa Breiner
 */
public class RecognizerTest {
    
    Recognizer r0, r1, r2, r3, r4, r5, r6, r7, r8;
    
    /**
     * Constructor for RecognizerTest.
     */
    public RecognizerTest() {
        r0 = new Recognizer("2 + 2");
        r1 = new Recognizer("");
    }


    @Before
    public void setUp() throws Exception {
        r0 = new Recognizer("");
        r1 = new Recognizer("250");
        r2 = new Recognizer("hello");
        r3 = new Recognizer("(xyz + 3)");
        r4 = new Recognizer("12 * 5 - 3 * 4 / 6 + 8");
        r5 = new Recognizer("12 * ((5 - 3) * 4) / 6 + (8)");
        r6 = new Recognizer("17 +");
        r7 = new Recognizer("22 *");
        r8 = new Recognizer("#");
    }

    @Test
    public void testRecognizer() {
        r0 = new Recognizer("");
        r1 = new Recognizer("2 + 2");
    }

    @Test
    public void testIsArithmeticExpression() {
        assertTrue(r1.isArithmeticExpression());
        assertTrue(r2.isArithmeticExpression());
        assertTrue(r3.isArithmeticExpression());
        assertTrue(r4.isArithmeticExpression());
        assertTrue(r5.isArithmeticExpression());

        assertFalse(r0.isArithmeticExpression());
        assertFalse(r8.isArithmeticExpression());

        try {
            assertFalse(r6.isArithmeticExpression());
            fail();
        }
        catch (SyntaxException e) {
        }
        try {
            assertFalse(r7.isArithmeticExpression());
            fail();
        }
        catch (SyntaxException e) {
        }
    }

    @Test
    public void testIsArithmeticExpressionWithUnaryMinus() {
        assertTrue(new Recognizer("-5").isArithmeticExpression());
        assertTrue(new Recognizer("12+(-5*10)").isArithmeticExpression());
        assertTrue(new Recognizer("+5").isArithmeticExpression());
        assertTrue(new Recognizer("12+(+5*10)").isArithmeticExpression());
    }

    @Test
    public void testIsTerm() {
        assertFalse(r0.isTerm()); // ""
        
        assertTrue(r1.isTerm()); // "250"
        
        assertTrue(r2.isTerm()); // "hello"
        
        assertTrue(r3.isTerm()); // "(xyz + 3)"
        followedBy(r3, "");
        
        assertTrue(r4.isTerm());  // "12 * 5 - 3 * 4 / 6 + 8"
        assertEquals(new Token(Token.Type.SYMBOL, "-"), r4.nextToken());
        assertTrue(r4.isTerm());
        followedBy(r4, "+ 8");

        assertTrue(r5.isTerm());  // "12 * ((5 - 3) * 4) / 6 + (8)"
        assertEquals(new Token(Token.Type.SYMBOL, "+"), r5.nextToken());
        assertTrue(r5.isTerm());
        followedBy(r5, "");
    }

    @Test
    public void testIsUnsignedFactor() {
        assertTrue(r1.isUnsignedFactor());
        assertTrue(r2.isUnsignedFactor());
        assertTrue(r3.isUnsignedFactor());
        assertTrue(r4.isUnsignedFactor()); followedBy(r4, "* 5 - 3 * 4 / 6 + 8");
        assertTrue(r5.isUnsignedFactor()); followedBy(r5, "* ((5");
        assertTrue(r6.isUnsignedFactor()); followedBy(r6, "+");
        assertTrue(r7.isUnsignedFactor()); followedBy(r7, "*");

        assertFalse(r0.isUnsignedFactor());
        assertFalse(r8.isUnsignedFactor()); followedBy(r8, "#");

        Recognizer r = new Recognizer("foo()");
        assertTrue(r.isUnsignedFactor());
        r = new Recognizer("bar(5, abc, 2+3)+");
        assertTrue(r.isUnsignedFactor()); followedBy(r, "+");

        r = new Recognizer("foo.bar$");
        assertTrue(r.isUnsignedFactor()); followedBy(r, "$");
        
        r = new Recognizer("123.123");
        assertEquals(new Token(Token.Type.NUMBER, "123.123"), r.nextToken());
        
        r = new Recognizer("5");
        assertEquals(new Token(Token.Type.NUMBER, "5.0"), r.nextToken());
    }
    
    @Test
    public void testIsParameterList() {
        Recognizer r = new Recognizer("() $");
        assertTrue(r.isParameterList()); followedBy(r, "$");
        r = new Recognizer("(5) $");
        assertTrue(r.isParameterList()); followedBy(r, "$");
        r = new Recognizer("(bar, x+3) $");
        assertTrue(r.isParameterList()); followedBy(r, "$");
        r = new Recognizer("return something");
        assertFalse(r.isParameterList());
    }
    
    @Test (expected = SyntaxException.class)
    public void testIsParameterListError1() {
    	Recognizer r = new Recognizer("(3 + 4,)");
    	r.isParameterList();
    }
    
    @Test (expected = SyntaxException.class)
    public void testIsParameterListError2() {
    	Recognizer r = new Recognizer("(3 + 4, 5 * 6");
    	r.isParameterList();
    }


    @Test
    public void testIsAddOperator() {
        Recognizer r = new Recognizer("+ - $");
        assertTrue(r.isAddOperator());
        assertTrue(r.isAddOperator());
        assertFalse(r.isAddOperator());
        followedBy(r, "$");
    }

    @Test
    public void testIsMultiplyOperator() {
        Recognizer r = new Recognizer("* / $");
        assertTrue(r.isMultiplyOperator());
        assertTrue(r.isMultiplyOperator());
        assertFalse(r.isMultiplyOperator());
        followedBy(r, "$");
    }

    @Test
    public void testIsVariable() {
        Recognizer r = new Recognizer("foo 23 bar +");
        assertTrue(r.isVariable());
        
        assertFalse(r.isVariable());
        assertTrue(r.isFactor());
        
        assertTrue(r.isVariable());
        
        assertFalse(r.isVariable());
        assertTrue(r.isAddOperator());
    }

    @Test
	public void testIsExpression() {
    	Recognizer r = new Recognizer("3 + 3 < 4 + 4");
    	assertTrue(r.isExpression());
    	r = new Recognizer("-4 * 3 != 12");
    	assertTrue(r.isExpression());
    	r = new Recognizer("hello < bye");
    	assertTrue(r.isExpression());
    	r = new Recognizer("\n yes");
    	assertFalse(r.isExpression());
    	r = new Recognizer("3 + 3");
    	assertTrue(r.isExpression());
    	r = new Recognizer("4 != 5 if");
    	assertTrue(r.isExpression());
    	followedBy(r, "if");
	}
    
    @Test (expected = SyntaxException.class)
    public void testIsExpressionError1() {
    	Recognizer r = new Recognizer("3 + 4 <=");
    	r.isExpression();
    }


	@Test
	public void testIsFactor() {
		Recognizer r = new Recognizer("+123");
		assertTrue(r.isFactor());
		r = new Recognizer("return 123");
		assertFalse(r.isFactor());
		r = new Recognizer("-pi");
		assertTrue(r.isFactor());
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsFactorError1() {
		Recognizer r = new Recognizer("+ return");
		r.isFactor();
	}


	@Test
	public void testIsAction() {
		Recognizer r = new Recognizer("move theresa \n");
		assertTrue(r.isAction());
		r = new Recognizer("moveto theresa, +7 \n");
		assertTrue(r.isAction());
		r = new Recognizer("turn around > bright\n\n eyes");
		assertTrue(r.isAction());
		followedBy(r, "eyes");
		r = new Recognizer("turnto neighbor\n");
		assertTrue(r.isAction());
		r = new Recognizer("line dance, prance, jingle, bell\n");
		assertTrue(r.isAction());
		r = new Recognizer("nothing");
		assertFalse(r.isAction());
	}
	

	@Test
	public void testIsAllbugsCode() {
		Recognizer r = new Recognizer("Allbugs { \n } \n");
		assertTrue(r.isAllbugsCode());
		r = new Recognizer("Allbugs { \n var theresa \n } \n");
		assertTrue(r.isAllbugsCode());
		r = new Recognizer("Allbugs { \n var theresa \n define thing {\n}\n } \n");
		assertTrue(r.isAllbugsCode());
		r = new Recognizer("Allbugs { \n define thing {\n}\n } \n");
		assertTrue(r.isAllbugsCode());
		r = new Recognizer("Allbugs { \n var theresa \n var something \n define other {\n}\n } \n");
		assertTrue(r.isAllbugsCode());
		r = new Recognizer("Allbugs { \n define thing {\n}\n define other {\n}\n } \n");
		assertTrue(r.isAllbugsCode());
		r = new Recognizer("Bug something");
		assertFalse(r.isAllbugsCode());
		
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsAllbugsCodeError1() {
		Recognizer r = new Recognizer("Allbugs { \n }");
		r.isAllbugsCode();
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsAllbugsCodeError2() {
		Recognizer r = new Recognizer("Allbugs { \n");
		r.isAllbugsCode();
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsAllbugsCodeError3() {
		Recognizer r = new Recognizer("Allbugs { \n var thing \n }");
		r.isAllbugsCode();
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsAllbugsCodeError4() {
		Recognizer r = new Recognizer("Allbugs { \n var thing \n define other {\n}\n }");
		r.isAllbugsCode();
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsAllbugsCodeError5() {
		Recognizer r = new Recognizer("Allbugs { \n var thing \n define other {\n}\n");
		r.isAllbugsCode();
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsAllbugsCodeError6() {
		Recognizer r = new Recognizer("Allbugs { \n define thing {\n}\n }");
		r.isAllbugsCode();
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsAllbugsCodeError7() {
		Recognizer r = new Recognizer("Allbugs { \n define thing {\n}\n");
		r.isAllbugsCode();
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsAllbugsCodeError8() {
		Recognizer r = new Recognizer("Allbugs { var thing \n");
		r.isAllbugsCode();
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsAllbugsCodeError9() {
		Recognizer r = new Recognizer("Allbugs {");
		r.isAllbugsCode();
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsAllbugsCodeError10() {
		Recognizer r = new Recognizer("Allbugs howdy");
		r.isAllbugsCode();
	}

	@Test
	public void testIsAssignmentStatement() {
		Recognizer r = new Recognizer("theresa = 3 + 4\n");
		assertTrue(r.isAssignmentStatement());
		r = new Recognizer("123 = 2 - 4\n");
		assertFalse(r.isAssignmentStatement());
		followedBy(r, "123");
		r = new Recognizer("var1 = hello < bye \n\n something");
		assertTrue(r.isAssignmentStatement());
		followedBy(r, "something");
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsAssignmentStatementError1() {
		Recognizer r = new Recognizer("theresa = 3 + 4");
		r.isAssignmentStatement();
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsAssignmentStatementError2() {
		Recognizer r = new Recognizer("theresa = 3 +");
		r.isAssignmentStatement();
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsAssignmentStatementError3() {
		Recognizer r = new Recognizer("theresa = move");
		r.isAssignmentStatement();
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsAssignmentStatementError4() {
		Recognizer r = new Recognizer("theresa \n");
		r.isAssignmentStatement();
	}

	@Test
	public void testIsBlock() {
		Recognizer r = new Recognizer("{\n turn left\n} \n");
		assertTrue(r.isBlock());
		r = new Recognizer("{\n}\n");
		assertTrue(r.isBlock());
		r = new Recognizer("turn left \n");
		assertFalse(r.isBlock());
	}
	
	@Test(expected = SyntaxException.class)
	public void testIsBlockError1() {
		Recognizer r = new Recognizer("{\n turn }");
		r.isBlock();
	}
	
	@Test(expected = SyntaxException.class)
	public void testIsBlockError2() {
		Recognizer r = new Recognizer("{\n turn");
		r.isBlock();
	}
	
	@Test(expected = SyntaxException.class)
	public void testIsBlockError3() {
		Recognizer r = new Recognizer("{\n}");
		r.isBlock();
	}
	
	@Test(expected = SyntaxException.class)
	public void testIsBlockError4() {
		Recognizer r = new Recognizer("{turn }");
		r.isBlock();
	}
	
	@Test(expected = SyntaxException.class)
	public void testIsBlockError5() {
		Recognizer r = new Recognizer("{theresa}");
		r.isBlock();
	}

	@Test
	public void testIsBugDefinition() {
		Recognizer r = new Recognizer("Bug theresa { \n turn left\n } \n");
		assertTrue(r.isBugDefinition());
		r = new Recognizer("Bug theresa { \n var thing \n turn left \n } \n");
		assertTrue(r.isBugDefinition());
		r = new Recognizer("Bug theresa { \n var thing \n initially {\n}\n turn left\n } \n");
		assertTrue(r.isBugDefinition());
		r = new Recognizer("Bug theresa { \n var thing \n initially {\n}\n turn left\n move down \n } \n");
		assertTrue(r.isBugDefinition());
		r = new Recognizer("Bug theresa { \n var thing \n initially {\n}\n turn left\n move down \n define function {\n}\n } \n");
		assertTrue(r.isBugDefinition());
		r = new Recognizer("Bug theresa { \n var thing \n var other \n initially {\n}\n turn left\n move down \n define function {\n}\n define second {\n}\n } \n");
		assertTrue(r.isBugDefinition());
		r = new Recognizer("theresa says");
		assertFalse(r.isBugDefinition());
		
	}
	@Test (expected = SyntaxException.class)
	public void testIsBugDefinitionError1() {
		Recognizer r = new Recognizer("Bug theresa { \n }");
		r.isBugDefinition();
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsBugDefinitionError2() {
		Recognizer r = new Recognizer("Bug theresa { \n");
		r.isBugDefinition();
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsBugDefinitionError3() {
		Recognizer r = new Recognizer("Bug theresa {");
		r.isBugDefinition();
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsBugDefinitionError4() {
		Recognizer r = new Recognizer("Bug theresa");
		r.isBugDefinition();
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsBugDefinitionError5() {
		Recognizer r = new Recognizer("Bug theresa { \n }");
		r.isBugDefinition();
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsBugDefinitionError6() {
		Recognizer r = new Recognizer("Bug theresa { \n turn right \n }");
		r.isBugDefinition();
	}

	@Test
	public void testIsColorStatement() {
		Recognizer r = new Recognizer("color blue \n");
		assertTrue(r.isColorStatement());
		r = new Recognizer("color red \n\n\n");
		assertTrue(r.isColorStatement());
		r = new Recognizer("not a color");
		assertFalse(r.isColorStatement());
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsColorStatementError1() {
		Recognizer r = new Recognizer("color blue 55");
		r.isColorStatement();
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsColorStatementError2() {
		Recognizer r = new Recognizer("color 64");
		r.isColorStatement();
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsColorStatementError3() {
		Recognizer r = new Recognizer("color \n");
		r.isColorStatement();
	}

	@Test
	public void testIsCommand() {
		Recognizer r = new Recognizer("turn right\n");
		assertTrue(r.isCommand());
		r = new Recognizer("finish = Up\n");
		assertTrue(r.isCommand());
		r = new Recognizer("line 1, 2, 3, 4\n");
		assertTrue(r.isCommand());
		r = new Recognizer("loop {\n}\n");
		assertTrue(r.isCommand());
		r = new Recognizer("switch { \n } \n");
		assertTrue(r.isCommand());
	}

	@Test
	public void testIsComparator() {
		Recognizer r = new Recognizer("< <= = != >= > 44");
		assertTrue(r.isComparator());
		assertTrue(r.isComparator());
		assertTrue(r.isComparator());
		assertTrue(r.isComparator());
		assertTrue(r.isComparator());
		assertTrue(r.isComparator());
		assertFalse(r.isComparator());
		followedBy(r, "44");
	}

	@Test
	public void testIsDoStatement() {
		Recognizer r = new Recognizer("do party \n");
		assertTrue(r.isDoStatement());
		r = new Recognizer("do party (lots, presents > 0) \n\n");
		assertTrue(r.isDoStatement());
		r = new Recognizer("theresa");
		assertFalse(r.isDoStatement());
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsDoStatementError1() {
		Recognizer r = new Recognizer("do party (lots, presents > 0)");
		r.isDoStatement();
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsDoStatementError2() {
		Recognizer r = new Recognizer("do party stuff");
		r.isDoStatement();
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsDoStatementError3() {
		Recognizer r = new Recognizer("do \n");
		r.isDoStatement();
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsDoStatementError4() {
		Recognizer r = new Recognizer("do (all, fun) \n");
		r.isDoStatement();
	}

	@Test
	public void testIsEol() {
		Recognizer r = new Recognizer("\n");
		assertTrue(r.isEol());
		r = new Recognizer("hello");
		assertFalse(r.isEol());
		followedBy(r, "hello");
		r = new Recognizer("\n\n15");
		assertTrue(r.isEol());
		followedBy(r, "15");
	}

	@Test
	public void testIsExitIfStatement() {
		Recognizer r = new Recognizer("exit if 3 < 4\n");
		assertTrue(r.isExitIfStatement());
		r = new Recognizer("exit if theresa\n\n\n");
		assertTrue(r.isExitIfStatement());
		r = new Recognizer("there's no way out");
		assertFalse(r.isExitIfStatement());
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsExitIfStatementError1() {
		Recognizer r = new Recognizer("exit if 3 < 4");
		r.isExitIfStatement();
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsExitIfStatementError2() {
		Recognizer r = new Recognizer("exit if loop");
		r.isExitIfStatement();
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsExitIfStatementError3() {
		Recognizer r = new Recognizer("exit if \n");
		r.isExitIfStatement();
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsExitIfStatementError4() {
		Recognizer r = new Recognizer("exit never");
		r.isExitIfStatement();
	}

	@Test
	public void testIsFunctionCall() {
		Recognizer r = new Recognizer("theresa()");
		assertTrue(r.isFunctionCall());
		r = new Recognizer("function13(3 + 3)");
		assertTrue(r.isFunctionCall());
		r = new Recognizer("14func(hi)");
		assertFalse(r.isFunctionCall());
		r = new Recognizer("name(param1, arg + arg2, 4 < 5)");
		assertTrue(r.isFunctionCall());
		r = new Recognizer("name(param) + 5");
		assertTrue(r.isFunctionCall());
		followedBy(r, "+ 5");
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsFunctionCallError1() {
		Recognizer r = new Recognizer("theresa says hi");
		r.isFunctionCall();
	}

	@Test
	public void testIsFunctionDefinition() {
		Recognizer r = new Recognizer("define fun1 {\n}\n");
		assertTrue(r.isFunctionDefinition());
		r = new Recognizer("define fun2 using myVariable { \n } \n");
		assertTrue(r.isFunctionDefinition());
		r = new Recognizer("something else + 5");
		assertFalse(r.isFunctionDefinition());
	}

	@Test (expected = SyntaxException.class)
	public void testIsFunctionDefinitionError1() {
		Recognizer r = new Recognizer("define fun2 using myVariable {\n}");
		r.isFunctionDefinition();
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsFunctionDefinitionError2() {
		Recognizer r = new Recognizer("define fun2 using myVariable {\n");
		r.isFunctionDefinition();
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsFunctionDefinitionError3() {
		Recognizer r = new Recognizer("define fun2 using myVariable {");
		r.isFunctionDefinition();
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsFunctionDefinitionError4() {
		Recognizer r = new Recognizer("define fun2 using myVariable");
		r.isFunctionDefinition();
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsFunctionDefinitionError5() {
		Recognizer r = new Recognizer("define fun2 using");
		r.isFunctionDefinition();
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsFunctionDefinitionError6() {
		Recognizer r = new Recognizer("define fun2");
		r.isFunctionDefinition();
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsFunctionDefinitionError7() {
		Recognizer r = new Recognizer("define return");
		r.isFunctionDefinition();
	}
	
	@Test
	public void testIsInitializationBlock() {
		Recognizer r = new Recognizer("initially {\n}\n");
		assertTrue(r.isInitializationBlock());
		r = new Recognizer("something else");
		assertFalse(r.isInitializationBlock());
		r = new Recognizer("initially {\n turn right\n}\n 45");
		assertTrue(r.isInitializationBlock());
		followedBy(r, "45");
		}
	
	@Test (expected = SyntaxException.class)
	public void testIsInitializationBlockError1() {
		Recognizer r = new Recognizer("initially {\n}");
		r.isInitializationBlock();
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsInitializationBlockError2() {
		Recognizer r = new Recognizer("initially {\n");
		r.isInitializationBlock();
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsInitializationBlockError3() {
		Recognizer r = new Recognizer("initially {");
		r.isInitializationBlock();
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsInitializationBlockError4() {
		Recognizer r = new Recognizer("initially var");
		r.isInitializationBlock();
	}

	@Test
	public void testIsLineAction() {
		Recognizer r = new Recognizer("line 3 + 4, 4 + 5, hello, bye \n");
		assertTrue(r.isLineAction());
		r = new Recognizer("something else");
		assertFalse(r.isLineAction());
		r = new Recognizer("line mary < beth, 5000, -43, 5 * 4 \n");
		assertTrue(r.isLineAction());
		r = new Recognizer("line a, b, c, d \n hello");
		assertTrue(r.isLineAction());
		followedBy(r, "hello");
		
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsLineActionError1() {
		Recognizer r = new Recognizer("line a, b, c, d");
		r.isLineAction();
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsLineActionError2() {
		Recognizer r = new Recognizer("line a, b, c,");
		r.isLineAction();
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsLineActionError3() {
		Recognizer r = new Recognizer("line a, b, c");
		r.isLineAction();
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsLineActionError4() {
		Recognizer r = new Recognizer("line a, b");
		r.isLineAction();
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsLineActionError5() {
		Recognizer r = new Recognizer("line a,");
		r.isLineAction();
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsLineActionError6() {
		Recognizer r = new Recognizer("line a");
		r.isLineAction();
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsLineActionError7() {
		Recognizer r = new Recognizer("line \n");
		r.isLineAction();
	}
	

	@Test
	public void testIsLoopStatement() {
		Recognizer r = new Recognizer("loop {\n}\n");
		assertTrue(r.isLoopStatement());
		r = new Recognizer("something else");
		assertFalse(r.isLoopStatement());
		r = new Recognizer("loop {\n turn left\n turn right\n }\n");
		assertTrue(r.isLoopStatement());
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsLoopStatementError1() {
		Recognizer r = new Recognizer("loop false");
		r.isLoopStatement();
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsLoopStatementError2() {
		Recognizer r = new Recognizer("loop {\n turn }");
		r.isLoopStatement();
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsLoopStatementError3() {
		Recognizer r = new Recognizer("loop {} \n");
		r.isLoopStatement();
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsLoopStatementError4() {
		Recognizer r = new Recognizer("loop {");
		r.isLoopStatement();
	}

	@Test
	public void testIsMoveAction() {
		Recognizer r = new Recognizer("move 4 + 5 \n");
		assertTrue(r.isMoveAction());
		r = new Recognizer("something else < hello");
		assertFalse(r.isMoveAction());
		r = new Recognizer("move +30\n something");
		assertTrue(r.isMoveAction());
		followedBy(r, "something");
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsMoveActionError1() {
		Recognizer r = new Recognizer("move 3 < 4");
		r.isMoveAction();
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsMoveActionError2() {
		Recognizer r = new Recognizer("move \n");
		r.isMoveAction();
	}

	@Test
	public void testIsMoveToAction() {
		Recognizer r = new Recognizer("moveto 4 + 5, hello \n");
		assertTrue(r.isMoveToAction());
		r = new Recognizer("something else < hello");
		assertFalse(r.isMoveToAction());
		r = new Recognizer("moveto +30, me < bob \n something");
		assertTrue(r.isMoveToAction());
		followedBy(r, "something");
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsMoveToActionError1() {
		Recognizer r = new Recognizer("moveto 3 < 4, hello");
		r.isMoveToAction();
	}

	@Test (expected = SyntaxException.class)
	public void testIsMoveToActionError2() {
		Recognizer r = new Recognizer("moveto 3 < 4,");
		r.isMoveToAction();
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsMoveToActionError3() {
		Recognizer r = new Recognizer("moveto 3 <");
		r.isMoveToAction();
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsMoveToActionError4() {
		Recognizer r = new Recognizer("moveto var");
		r.isMoveToAction();
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsMoveToActionError5() {
		Recognizer r = new Recognizer("moveto");
		r.isMoveToAction();
	}
	
	@Test
	public void testIsProgram() {
		Recognizer r = new Recognizer("Bug theresa { \n turn right \n } \n");
		assertTrue(r.isProgram());
		r = new Recognizer("Allbugs { \n var things \n } \n Bug theresa { \n turn right \n } \n");
		assertTrue(r.isProgram());
		r = new Recognizer("Allbugs { \n var things \n } \n Bug theresa { \n turn right \n } \n Bug zach { \n move up \n } \n");
		assertTrue(r.isProgram());
		r = new Recognizer("Bug theresa { \n turn right \n } \n Bug zach { \n move down \n } \n");
		assertTrue(r.isProgram());
		r = new Recognizer("theresa says hi");
		assertFalse(r.isProgram());
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsProgramError1() {
		Recognizer r = new Recognizer("Allbugs { \n var things \n } \n");
		r.isProgram();
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsProgramError2() {
		Recognizer r = new Recognizer("Allbugs { \n var things \n } \n Bug theresa");
		r.isProgram();
	}
	
	@Test(expected = SyntaxException.class)
	public void testIsProgramNoEOF() {
		Recognizer r = new Recognizer("Bug theresa { \n turn right \n } \n 12345");
		assertFalse(r.isProgram());
	}

	@Test
	public void testIsReturnStatement() {
		Recognizer r = new Recognizer("return yes <= no \n");
		assertTrue(r.isReturnStatement());
		r = new Recognizer("return +65 * maybe \n\n something");
		assertTrue(r.isReturnStatement());
		followedBy(r, "something");
		r = new Recognizer("not right");
		assertFalse(r.isReturnStatement());

	}
	
	@Test (expected = SyntaxException.class)
	public void testIsReturnStatementError1() {
		Recognizer r = new Recognizer("return \n");
		assertFalse(r.isReturnStatement());
	}
	@Test (expected = SyntaxException.class)
	public void testIsReturnStatementError2() {
	Recognizer r = new Recognizer("return true");
	assertFalse(r.isReturnStatement());
	}

	@Test
	public void testIsStatement() {
		Recognizer r = new Recognizer("theresa = 3 + 5\n");
		assertTrue(r.isStatement());
		r = new Recognizer("loop {\nturn left\n}\n");
		assertTrue(r.isStatement());
		r = new Recognizer("exit if less < big\n");
		assertTrue(r.isStatement());
		r = new Recognizer("switch { \n case 1 \n turn left \n } \n");
		assertTrue(r.isStatement());
		r = new Recognizer("return hello \n");
		assertTrue(r.isStatement());
		r = new Recognizer("do stuff(thing1, thing2)\n");
		assertTrue(r.isStatement());
		r = new Recognizer("color black \n");
		assertTrue(r.isStatement());
		r = new Recognizer("456 < me");
		assertFalse(r.isStatement());
	}

	@Test
	public void testIsSwitchStatement() {
		Recognizer r = new Recognizer("switch { \n } \n");
		assertTrue(r.isSwitchStatement());
		r = new Recognizer("switch { \n case 3 + 4 \n } \n");
		assertTrue(r.isSwitchStatement());
		r = new Recognizer("switch { \n case big < small \n turn right \n } \n");
		assertTrue(r.isSwitchStatement());
		r = new Recognizer("something else");
		assertFalse(r.isSwitchStatement());
		r = new Recognizer("switch{\n case 1 \n case 2 \n case 3 \n } \n");
		assertTrue(r.isSwitchStatement());
		r = new Recognizer("switch{\n case 1 \n turn right\n turn left\n case 2 \n turnto neighbor\n} \n");
		assertTrue(r.isSwitchStatement());
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsSwitchStatementError1() {
		Recognizer r = new Recognizer("switch { \n case big < small \n turn right \n }");
		r.isSwitchStatement();
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsSwitchStatementError2() {
		Recognizer r = new Recognizer("switch { \n case big < small \n turn right \n");
		r.isSwitchStatement();
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsSwitchStatementError3() {
		Recognizer r = new Recognizer("switch { \n case big < small \n }");
		r.isSwitchStatement();
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsSwitchStatementError4() {
		Recognizer r = new Recognizer("switch { \n case big < small}");
		r.isSwitchStatement();
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsSwitchStatementError5() {
		Recognizer r = new Recognizer("switch { \n case \n}");
		r.isSwitchStatement();
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsSwitchStatementError6() {
		Recognizer r = new Recognizer("switch { }");
		r.isSwitchStatement();
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsSwitchStatementError7() {
		Recognizer r = new Recognizer("switch case");
		r.isSwitchStatement();
	}

	@Test
	public void testIsTurnAction() {
		Recognizer r = new Recognizer("turn 4 + 5 \n");
		assertTrue(r.isTurnAction());
		r = new Recognizer("something else < hello");
		assertFalse(r.isTurnAction());
		r = new Recognizer("turn +30\n something");
		assertTrue(r.isTurnAction());
		followedBy(r, "something");
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsTurnActionError1() {
		Recognizer r = new Recognizer("turn 3 < 4");
		r.isTurnAction();
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsTurnActionError2() {
		Recognizer r = new Recognizer("turn 3 <");
		r.isTurnAction();
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsTurnActionError3() {
		Recognizer r = new Recognizer("turn move");
		r.isTurnAction();
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsTurnActionError4() {
		Recognizer r = new Recognizer("turn");
		r.isTurnAction();
	}

	@Test
	public void testIsTurnToAction() {
		Recognizer r = new Recognizer("turnto 4 + 5 \n");
		assertTrue(r.isTurnToAction());
		r = new Recognizer("something else < hello");
		assertFalse(r.isTurnToAction());
		r = new Recognizer("turnto +30\n something");
		assertTrue(r.isTurnToAction());
		followedBy(r, "something");
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsTurnToActionError1() {
		Recognizer r = new Recognizer("turnto 3 < 4");
		r.isTurnToAction();
	}
	
	@Test (expected = SyntaxException.class)
	public void testIsTurnToActionError2() {
		Recognizer r = new Recognizer("turnto");
		r.isTurnToAction();
	}

	@Test
	public void testIsVarDeclaration() {
		Recognizer r = new Recognizer("var theresa \n");
		assertTrue(r.isVarDeclaration());
		r = new Recognizer("var theresa, breiner, awesome \n");
		assertTrue(r.isVarDeclaration());
		r = new Recognizer("something else");
		assertFalse(r.isVarDeclaration());
	}
	
	@Test(expected = SyntaxException.class)
	public void testIsVarDeclarationError1() {
		Recognizer r = new Recognizer("var theresa");
		r.isVarDeclaration();
	}
	
	@Test(expected = SyntaxException.class)
	public void testIsVarDeclarationError2() {
		Recognizer r = new Recognizer("var theresa, \n");
		r.isVarDeclaration();
	}
	
	@Test(expected = SyntaxException.class)
	public void testIsVarDeclarationError3() {
		Recognizer r = new Recognizer("var \n");
		r.isVarDeclaration();
	}

    
    @Test
    public void testSymbol() {
        Recognizer r = new Recognizer("++");
        assertEquals(new Token(Token.Type.SYMBOL, "+"), r.nextToken());
    }

    @Test
    public void testNextTokenMatchesType() {
        Recognizer r = new Recognizer("++abc");
        assertTrue(r.nextTokenMatches(Token.Type.SYMBOL));
        assertFalse(r.nextTokenMatches(Token.Type.NAME));
        assertTrue(r.nextTokenMatches(Token.Type.SYMBOL));
        assertTrue(r.nextTokenMatches(Token.Type.NAME));
    }

    @Test
    public void testNextTokenMatchesTypeString() {
        Recognizer r = new Recognizer("+abc+");
        assertTrue(r.nextTokenMatches(Token.Type.SYMBOL, "+"));
        assertTrue(r.nextTokenMatches(Token.Type.NAME, "abc"));
        assertFalse(r.nextTokenMatches(Token.Type.SYMBOL, "*"));
        assertTrue(r.nextTokenMatches(Token.Type.SYMBOL, "+"));
    }

    @Test
    public void testNextToken() {
        // NAME, KEYWORD, NUMBER, SYMBOL, EOL, EOF };
        Recognizer r = new Recognizer("abc move 25 *\n");
        assertEquals(new Token(Token.Type.NAME, "abc"), r.nextToken());
        assertEquals(new Token(Token.Type.KEYWORD, "move"), r.nextToken());
        assertEquals(new Token(Token.Type.NUMBER, "25.0"), r.nextToken());
        assertEquals(new Token(Token.Type.SYMBOL, "*"), r.nextToken());
        assertEquals(new Token(Token.Type.EOL, "\n"), r.nextToken());
        assertEquals(new Token(Token.Type.EOF, "EOF"), r.nextToken());
        
        r = new Recognizer("foo.bar 123.456");
        assertEquals(new Token(Token.Type.NAME, "foo"), r.nextToken());
        assertEquals(new Token(Token.Type.SYMBOL, "."), r.nextToken());
        assertEquals(new Token(Token.Type.NAME, "bar"), r.nextToken());
        assertEquals(new Token(Token.Type.NUMBER, "123.456"), r.nextToken());
    }

    @Test
    public void testPushBack() {
        Recognizer r = new Recognizer("abc 25");
        assertEquals(new Token(Token.Type.NAME, "abc"), r.nextToken());
        r.pushBack();
        assertEquals(new Token(Token.Type.NAME, "abc"), r.nextToken());
        assertEquals(new Token(Token.Type.NUMBER, "25.0"), r.nextToken());
    }
    
//  ----- "Helper" methods

    /**
     * This method is given a String containing some or all of the
     * tokens that should yet be returned by the Tokenizer, and tests
     * whether the Tokenizer in fact has those Tokens. To succeed,
     * everything in the given String must still be in the Tokenizer,
     * but there may be additional (untested) Tokens to be returned.
     * This method is primarily to test whether rejected Tokens are
     * pushed back appropriately.
     * 
     * @param recognizer The Recognizer whose Tokenizer is to be tested.
     * @param expectedTokens The Tokens we expect to get from the Tokenizer.
     */
    private void followedBy(Recognizer recognizer, String expectedTokens) {
        int expectedType;
        int actualType;
        StreamTokenizer actual = recognizer.tokenizer;

        Reader reader = new StringReader(expectedTokens);
        StreamTokenizer expected = new StreamTokenizer(reader);
        expected.ordinaryChar('-');
        expected.ordinaryChar('/');

        try {
            while (true) {
                expectedType = expected.nextToken();
                if (expectedType == StreamTokenizer.TT_EOF) break;
                actualType = actual.nextToken();
                assertEquals(expectedType, actualType);
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

	

	

}
