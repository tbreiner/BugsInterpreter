package bugs;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;

/**
 * This class consists of a number of methods that "recognize" strings
 * composed of Tokens that follow the indicated grammar rules for each
 * method.
 * <p>Each method may have one of three outcomes:
 * <ul>
 *   <li>The method may succeed, returning <code>true</code> and
 *      consuming the tokens that make up that particular nonterminal.</li>
 *   <li>The method may fail, returning <code>false</code> and not
 *       consuming any tokens.</li>
 *   <li>(Some methods only) The method may determine that an
 *       unrecoverable error has occurred and throw a
 *       <code>SyntaxException</code></li>.
 * </ul>
 * @author David Matuszek and Theresa Breiner
 * @version February 2015
 */
public class Recognizer {
    /** The tokenizer used by this Parser. */
    StreamTokenizer tokenizer = null;
    /** The number of the line of source code currently being processed. */
    int lineNumber;
    
    /**
     * Constructs a Recognizer for the given string.
     * @param text The string to be recognized.
     */
    public Recognizer(String text) {
        Reader reader = new StringReader(text);
        tokenizer = new StreamTokenizer(reader);
        tokenizer.parseNumbers();
        tokenizer.eolIsSignificant(true);
        tokenizer.slashStarComments(true);
        tokenizer.slashSlashComments(true);
        tokenizer.lowerCaseMode(false);
        tokenizer.ordinaryChars(33, 47);
        tokenizer.ordinaryChars(58, 64);
        tokenizer.ordinaryChars(91, 96);
        tokenizer.ordinaryChars(123, 126);
        tokenizer.quoteChar('\"');
        lineNumber = 1;
    }

    /**
     * Tries to recognize an &lt;expression&gt;.
     * <pre>&lt;expression&gt; ::= &lt;arithmetic expression&gt; { &lt;comparator&gt; &lt;arithmetic expression&gt; }</pre>
     * @return <code>true</code> if an expression is recognized.
     */
    public boolean isExpression() {
        // TODO You need to expand this definition!
        if (isArithmeticExpression()) {
        	while (isComparator()) {
        		if (!isArithmeticExpression()) {
        			error("No arithmetic expression after comparator");
        		}
        	}
        	return true;
        }
        return false;
    }

    /**
     * Tries to recognize an &lt;arithmetic expression&gt;.
     * <pre>&lt;arithmetic expression&gt; ::= &lt;term&gt; { &lt;add_operator&gt; &lt;term&gt; }</pre>
     * A <code>SyntaxException</code> will be thrown if the add_operator
     * is present but not followed by a valid &lt;expression&gt;.
     * @return <code>true</code> if an expression is recognized.
     */
    public boolean isArithmeticExpression() {
    	boolean startsWithUnary = symbol("+") || symbol("-");
        if (!isTerm())
            return false;
        while (isAddOperator()) {
            if (!isTerm()) error("Error in expression after '+' or '-'");
        }
        return true;
    }

    /**
     * Tries to recognize a &lt;term&gt;.
     * <pre>&lt;term&gt; ::= &lt;factor&gt; { &lt;multiply_operator&gt; &lt;term&gt;}</pre>
     * A <code>SyntaxException</code> will be thrown if the multiply_operator
     * is present but not followed by a valid &lt;term&gt;.
     * @return <code>true</code> if a term is recognized.
     */
    public boolean isTerm() {
        if (!isFactor()) return false;
        while (isMultiplyOperator()) {
            if (!isFactor()) error("No term after '*' or '/'");
        }
        return true;
    }

    /**
     * Tries to recognize a &lt;factor&gt;.
     * <pre>&lt;factor&gt; ::= [ &lt;add operator&gt; ] &lt;unsigned factor&gt;</pre>
     * @return <code>true</code> if a factor is parsed.
     */
    public boolean isFactor() {
        if(symbol("+") || symbol("-")) {
            if (isUnsignedFactor()) {
                return true;
            }
            error("No factor following unary plus or minus");
            return false; // Can't ever get here
        }
        return isUnsignedFactor();
    }

    /**
     * Tries to recognize an &lt;unsigned factor&gt;.
     * <pre>&lt;factor&gt; ::= &lt;variable&gt; "." &lt;variable&gt;
     *           | &lt;function call&gt;
     *           | &lt;variable&gt;
     *           | &lt;number&gt;
     *           | "(" &lt;expression&gt; ")"</pre>
     * A <code>SyntaxException</code> will be thrown if the opening
     * parenthesis is present but not followed by a valid
     * &lt;expression&gt; and a closing parenthesis.
     * @return <code>true</code> if a factor is recognized.
     */
    public boolean isUnsignedFactor() {
        if (isVariable()) {
            if (symbol(".")) {              // reference to another Bug
                if (name()) return true;
                error("Incorrect use of dot notation");
            }
            else if (isParameterList()) return true; // function call
            else return true;                        // just a variable
        }
        if (number()) return true;
        if (symbol("(")) {
            if (!isExpression()) error("Error in parenthesized expression");
            if (!symbol(")")) error("Unclosed parenthetical expression");
            return true;
       }
       return false;
    }

    /**
     * Tries to recognize a &lt;parameter list&gt;.
     * <pre>&ltparameter list&gt; ::= "(" [ &lt;expression&gt; { "," &lt;expression&gt; } ] ")"
     * @return <code>true</code> if a parameter list is recognized.
     */
    public boolean isParameterList() {
        if (!symbol("(")) return false;
        if (isExpression()) {
            while (symbol(",")) {
                if (!isExpression()) error("No expression after ','");
            }
        }
        if (!symbol(")")) error("Parameter list doesn't end with ')'");
        return true;
    }

    /**
     * Tries to recognize an &lt;add_operator&gt;.
     * <pre>&lt;add_operator&gt; ::= "+" | "-"</pre>
     * @return <code>true</code> if an addop is recognized.
     */
    public boolean isAddOperator() {
        return symbol("+") || symbol("-");
    }

    /**
     * Tries to recognize a &lt;multiply_operator&gt;.
     * <pre>&lt;multiply_operator&gt; ::= "*" | "/"</pre>
     * @return <code>true</code> if a multiply_operator is recognized.
     */
    public boolean isMultiplyOperator() {
        return symbol("*") || symbol("/");
    }

    /**
     * Tries to recognize a &lt;variable&gt;.
     * <pre>&lt;variable&gt; ::= &lt;NAME&gt;</pre>
     * @return <code>true</code> if a variable is recognized.
     */
    public boolean isVariable() {
        return name();
    }
    
    /**
     * Tries to recognize an &lt;action&gt;.
     * <pre>&lt;action&gt; ::= &lt;move action&gt; | &lt;moveto action&gt; 
     * | &lt;turn action&gt; | &lt;turnto action&gt; | &lt;line action&gt; </pre>
     * @return <code>true</code> if an action is recognized.
     */
    public boolean isAction() {
    	return isMoveAction() || isMoveToAction() || isTurnAction() 
    			|| isTurnToAction() || isLineAction();
    }
    
    /**
     * Tries to recognize an &lt;allbugs code&gt;.
     * <pre>&lt;allbugs code&gt; ::= "Allbugs" "{" &lt;eol&gt; 
     * { &lt;var declaration&gt; } { &lt;function definition&gt; } "}" &lt;eol&gt;</pre>
     * @return <code>true</code> if an allbugs code is recognized.
     */
    public boolean isAllbugsCode() {
    	if (keyword("Allbugs")) {
    		if (symbol("{")) {
    			if (isEol()) {
    				while (isVarDeclaration());
    				while(isFunctionDefinition());
    				if (symbol("}")) {
    					if (isEol()) return true;
    					error("No EOL after allbugs code");
    				}
    				error("No closing brace after allbugs code");
    			}
    			error("No EOL after Allbugs and open brace");
    		}
    		error("No open brace after Allbugs");
    	}
    	return false;
    }
    
    /**
     * Tries to recognize &lt;assignment statement&gt;.
     * <pre>&lt;assignment statement&gt; ::= &lt;variable&gt; "=" &lt;expression&gt; &lt;eol&gt;</pre>
     * @return <code> true </code> if recognizes assignment statement.
     */
    public boolean isAssignmentStatement() {
    	if (isVariable()) {
    		if (symbol("=")) {
    			if (isExpression()) {
    				if (isEol()) return true;
    				error("No EOL after assignment");
    			}
    			error("No expression after = sign");
    		}
    		error("No = sign after variable");
    	}
    	return false;
    }
    
    /**
     * Tries to recognize a &lt;block&gt;
     * <pre>&lt;block&gt; ::= "{" &lt;eol&gt; { &lt;command&gt; } "}" &lt;eol&gt;</pre>
     * @return <code> true </code> if recognizes a block
     */
    public boolean isBlock() {
    	if (symbol("{")) {
    		if (isEol()) {
    			while (isCommand());
    			if (symbol("}")) {
    				if (isEol()) return true;
    				error("No EOL after block with command");
    			}
    			error("No closing brace after command");
    		}
    		error("No EOL after first brace in block");
    	}
    	return false;
    }
    
    /**
     * Tries to recognize a &lt;bug definition&gt;
     * <pre>&lt;bug definition&gt; ::= "Bug" &lt;name&gt; "{" &lt;eol&gt;
     * { &lt;var declaration&gt; } [ &lt;initialization block&gt; ] &lt;command&gt;
     * { &lt;command&gt; } { &lt;function definition&gt; } "}" &lt;eol&gt;</pre>
     * @return <code> true </code> if recognizes a bug definition.
     */
    public boolean isBugDefinition() {
    	if (keyword("Bug")) {
    		if (name()) {
    			if (symbol("{")) {
    				if (isEol()) {
    					while (isVarDeclaration());
    					isInitializationBlock();
    					if (isCommand()) {
    						while (isCommand());
    						while (isFunctionDefinition());
    						if (symbol("}")) {
    							if (isEol()) return true;
    							error("No EOL after Bug Definition");
    						}
    						error("No closing brace after bug definition");
    					}
    					error("No command in bug definition");
    				}
    				error("No EOL after bug name {");
    			}
    			error("No opening brace after bug name");
    		}
    		error("No name after keyword Bug");
    	}
    	return false;
    }
    
    /**
     * Tries to recognize a &lt;color statement&gt;
     * <pre>&lt;color statement&gt; ::= "color" &lt;KEYWORD&gt; &lt;eol&gt;</pre>
     * @return <code> true </code> if recognizes a color statement
     */
    public boolean isColorStatement() {
    	if (keyword("color")) {
    		if (nextTokenMatches(Token.Type.KEYWORD)) {
    			if (isEol()) return true;
    			error("No EOL after color statement");
    		}
    		error("No keyword after color");
    	}
    	return false;
    }
    
    /**
     * Tries to recognize a &lt;command&gt;
     * <pre>&lt;commandt&gt; ::= &lt;action&gt; | &lt;statement&gt;</pre>
     * @return <code> true </code> if recognizes a command
     */
    public boolean isCommand() {
    	return isAction() || isStatement();
    }
    
    /**Tries to recognize a &lt;comparator&gt;.
     * <pre>&lt;comparator&gt; ::= "<" | "<=" 
     * | "=" | "!=" | ">=" | ">"</pre>
     * @return <code>true</code> if a comparator is recognized.
     */
    public boolean isComparator() {
    	if (symbol("=")) return true;
    	if (symbol("<")) {
    		if (symbol("=")) return true;
    		return true;
    	}
    	if (symbol(">")) {
    		if (symbol("=")) return true;
    		return true;
    	}
    	if (symbol("!")) {
    		if (symbol("=")) return true;
    		pushBack();
    	}
    	return false;
    }
    
    /**
     * Tries to recognize a &lt;do statement&gt;
     * <pre>&lt;do statement&gt; ::= "do" &lt;variable&gt; [ &lt;parameter list&gt; ] &lt;eol&gt;</pre>
     * @return <code> true </code> if recognizes a do statement
     */
    public boolean isDoStatement() {
    	if (keyword("do")) {
    		if (isVariable()) {
    			if (isParameterList()) {
    				if (isEol()) return true;
    				error("No EOL after parameter list in do statement");
    			}
    			if (isEol()) return true;
    			error("No EOL after variable in do statement");
    		}
    		error("No variable after do");
    	}
    	return false;
    }
    
    /**
     * Tries to recognize an &lt;eol&gt;.
     * <pre>&lt;eol&gt; ::= &lt;EOL&gt; { &lt;EOL&gt; }</pre>
     * @return <code> true </code> if an end of line is recognized.
     */
    public boolean isEol() {
    	int count = 0;
    	while (nextTokenMatches(Token.Type.EOL)) count++;
    	if (count > 0) return true;
    	return false;
    }

    /**
     * Tries to recognize an &lt;exit if statement&gt;.
     * <pre>&lt;exit if statement&gt; ::= "exit" "if" &lt;expression&gt; &lt;eol&gt;</pre>
     * @return <code> true </code> if an exit if statement is recognized.
     */
    public boolean isExitIfStatement() {
    	if (keyword("exit")) {
    		if (keyword("if")) {
    			if (isExpression()) {
    				if (isEol()) return true;
    				error("No EOL after exit if statement");
    			}
    			error("No expression after exit if");
    		}
    		error("No if after exit");
    	}
    	return false;
    }
    
    /**
     * Tries to recognize a &lt;function call&gt;.
     * <pre>&lt;function call&gt; ::= &lt;NAME&gt; &lt;parameter list&gt;</pre>
     * @return <code> true </code> if a function call is recognized.
     */
    public boolean isFunctionCall() {
    	if (name()) {
    		if (isParameterList()) return true;
    		else error("No parameter list after name");
    	}
    	return false;
    }
    
    /**
     * Tries to recognize a &lt;function definition&gt;.
     * <pre>&lt;function definition&gt; ::= "define" &lt;NAME&gt; [ "using" &lt;variable&gt; 
     * { "," &lt;variable&gt; } ] &lt;block&gt;</pre>
     * @return <code> true </code> if a function definition is recognized.
     *
     */
    public boolean isFunctionDefinition() {
    	if (keyword("define")) {
    		if(name()) {
    			if (keyword("using")) {
    				if (isVariable()) {
    					while (symbol(",") && isVariable());
    					if (isBlock()) return true;
    					error("No block in function definition");
    				}
    				error("No variable after using in function definition");
    			}
    			if (isBlock()) return true;
    			error("No block after name in function definition");
    		}
    		error("No name after define");
    	}
    	return false;
    }
    
    /**
     * Tries to recognize an &lt;initialization block&gt;.
     * <pre>&lt;initialization block&gt; ::= "initially" &lt;block&gt; </pre>
     * @return <code> true </code> if an initialization block is recognized.
     * 
     */
    public boolean isInitializationBlock() {
    	if (keyword("initially")) {
    		if (isBlock()) return true;
    		error("No block after initially");
    	}
    	return false;
    }
    
    /**
     * Tries to recognize a &lt;line action&gt;.
     * <pre> &lt;line action&gt; ::= "line" &lt;expression&gt; "," 
     * &lt;expression&gt; "," &lt;expression&gt; "," &lt;expression&gt; &lt;eol&gt;</pre> 
     * @return <code> true </code> if recognizes a line action
     */
    public boolean isLineAction() {
    	if (keyword("line")) {
    		if (isExpression()) {
    			if (symbol(",") && isExpression()) {
    				if (symbol(",") && isExpression()) {
    					if (symbol(",") && isExpression()) {
    						if (isEol()) return true;
        					error("missing EOL after last expression in line action");
    					}
    					error("Missing last expression in line action.");
    				}
    				error("Missing two expressions in line action");
    			}
    			error("Missing three expressions in line action");
    		}
    		error("No expression after line action");
    	}
    	return false;
    }
    
    /**
     * Tries to recognize a &lt;loop statement&gt;.
     * <pre>&lt;loop statement&gt; ::= "loop" &lt;block&gt;</pre>
     * @return <code> true </code> if recognizes a loop statement.
     */
    public boolean isLoopStatement() {
    	if (keyword("loop")) {
    		if (isBlock()) return true;
    		error("No block after loop");
    	}
    	return false;
    }
    
    /**
     * Tries to recognize a &lt;move action&gt;.
     * <pre>&lt;move action&gt; ::= "move" &lt;expression&gt; &lt;eol&gt;</pre>
     * @return <code> true </code> if recognizes a move action.
     */
    public boolean isMoveAction() {
    	if (keyword("move")) {
    		if (isExpression()) {
    			if (isEol()) return true;
    			else error("No EOL after move and expression");
    		}
    		error("No expression after move");
    	}
    	return false;
    }
    
    /**
     * Tries to recognize a &lt;moveto action&gt;.
     * <pre>&lt;moveto action&gt; ::= "moveto" &lt;expression&gt; "," 
     * &lt;expression&gt; &lt;eol&gt;</pre>
     * @return <code> true </code> if recognizes a moveto action
     */
    public boolean isMoveToAction() {
    	if (keyword("moveto")) {
    		if (isExpression()) {
    			if (symbol(",")) {
    				if (isExpression()) {
    					if (isEol()) return true;
    				}
    				error("No expression after moveto and expression ','");
    			}
    			error("No comma after moveto and expression");
    		}
    		error("No expression after moveto");
    	}
    	return false;
    }
    
    /**
     * Tries to recognize a &lt;program&gt;.
     * <pre>&lt;program&gt; ::= [ &lt;allbugs code&gt; ] &lt;bug definition&gt; { &lt;bug definition&gt; }</pre>
     * @return <code> true </code> if recognizes a program
     */
    public boolean isProgram() {
    	isEol(); //added because Dave said to on Piazza
    	if (isAllbugsCode()) {
    		if (isBugDefinition()) {
    			while (isBugDefinition());
    			Token t = nextToken();
    			if (t.type == Token.Type.EOF) {
    				return true;
    			}
    			error("Not EOF after bug definitions in program");
    		}
    		error("Allbugs code but no bug definition in program");
    	}
    	if (isBugDefinition()) {
    		while (isBugDefinition());
    		Token t = nextToken();
    		if (t.type == Token.Type.EOF) {
    			return true;
    		}
    		error("Not EOF after bug definitions in program, no allbugs.");
    	}
    	return false;

    }
    
    /**
     * Tries to recognize a &lt;return statement&gt;.
     * <pre>&lt;return statement&gt; ::= "return" &lt;expression&gt; &lt;eol&gt;</pre>
     * @return <code> true </code> if recognizes return statement
     */
    public boolean isReturnStatement() {
    	if (keyword("return")) {
    		if (isExpression()) {
    			if (isEol()) return true;
    			error("No EOL after return statement");
    		}
    		error("No expression after return");
    	}
    	return false;
    }
    
    /**
     * Tries to recognize a &lt;statement&gt;.
     * <pre>&lt;statement&gt; ::= &lt;assignment statement&gt; | 
     * &lt;loop statement&gt; | &lt;exit if statement&gt; | &lt;switch statement&gt; 
     * | &lt;return statement&gt; | &lt;do statement&gt; | &lt;color statement&gt;</pre>
     * @return <code> true </code> if recognizes statement
     */
    public boolean isStatement() {
    	return isAssignmentStatement() || isLoopStatement() || isExitIfStatement() 
    			|| isSwitchStatement() || isReturnStatement() || isDoStatement() || 
    			isColorStatement();
    }
    
    /**
     * Tries to recognize a &lt;switch statement&gt;.
     * <pre>&lt;switch statement&gt; ::= "switch" "{" &lt;eol&gt; 
     * { "case" &lt;expression&gt; &lt;eol&gt; { &lt;command&gt; } } "}" &lt;eol&gt;</pre>
     * @return <code> true </code> if recognizes switch statement
     */
    public boolean isSwitchStatement() {
    	if (keyword("switch")) {
    		if (symbol("{")) {
    			if (isEol()) {
    				while (keyword("case") && isExpression() && isEol()) {
    							while (isCommand());
    				}
    				if (symbol("}")) {
    					if (isEol()) return true;
    					error("No EOL at end of switch statement");
    				}
    				error("No end brace at end of switch statement");
    			}
    			error("No EOL after switch and open brace");
    		}
    		error("No open brace after switch");
    	}
    	return false;
    }
    
    /**
     * Tries to recognize a &lt;turn action&gt;.
     * <pre>&lt;turn action&gt; ::= "turn" &lt;expression&gt; &lt;eol&gt;</pre>
     * @return <code> true </code> if recognizes a turn action.
     */
    public boolean isTurnAction() {
    	if (keyword("turn")) {
    		if (isExpression()) {
    			if (isEol()) return true;
    			error("No EOL after turn and expression");
    		}
    		error("No expression after turn");
    	}
    	return false;
    }
    
    /**
     * Tries to recognize a &lt;turnto action&gt;.
     * <pre>&lt;turnto action&gt; ::= "turnto" &lt;expression&gt; &lt;eol&gt;</pre>
     * @return <code> true </code> if recognizes a turnto action.
     */
    public boolean isTurnToAction() {
    	if (keyword("turnto")) {
    		if (isExpression()) {
    			if (isEol()) return true;
    			error("No EOL after turnto and expression");
    		}
    		error("No expression after turnto");
    	}
    	return false;
    }
    
    /**
     * Tries to recognize a &lt;var declaration&gt;.
     * <pre>&lt;var declaration&gt; ::= "var" &lt;NAME&gt; { "," &lt;NAME&gt; } &lt;eol&gt;</pre>
     * @return <code> true </code> if recognizes a var declaration.
     */
    public boolean isVarDeclaration() {
    	if (keyword("var")) {
    		if (name()) {
    			while (symbol(",")) {
    				if (!name()) error("No name after comma in var declaration");
    			}
    			if (isEol()) return true;
    			error("No EOL after var declaration");
    		}
    		error("No name after var");
    	}
    	return false;
    }
    
//----- Private "helper" methods

    /**
     * Tests whether the next token is a number. If it is, the token
     * is consumed, otherwise it is not.
     *
     * @return <code>true</code> if the next token is a number.
     */
    private boolean number() {
        return nextTokenMatches(Token.Type.NUMBER);
    }

    /**
     * Tests whether the next token is a name. If it is, the token
     * is consumed, otherwise it is not.
     *
     * @return <code>true</code> if the next token is a name.
     */
    private boolean name() {
        return nextTokenMatches(Token.Type.NAME);
    }

    /**
     * Tests whether the next token is the expected name. If it is, the token
     * is consumed, otherwise it is not.
     *
     * @param expectedName The String value of the expected next token.
     * @return <code>true</code> if the next token is a name with the expected value.
     */
    private boolean name(String expectedName) {
        return nextTokenMatches(Token.Type.NAME, expectedName);
    }

    /**
     * Tests whether the next token is the expected keyword. If it is, the token
     * is moved to the stack, otherwise it is not.
     *
     * @param expectedKeyword The String value of the expected next token.
     * @return <code>true</code> if the next token is a keyword with the expected value.
     */
    private boolean keyword(String expectedKeyword) {
        return nextTokenMatches(Token.Type.KEYWORD, expectedKeyword);
    }

    /**
     * Tests whether the next token is the expected symbol. If it is,
     * the token is consumed, otherwise it is not.
     *
     * @param expectedSymbol The String value of the token we expect
     *    to encounter next.
     * @return <code>true</code> if the next token is the expected symbol.
     */
    boolean symbol(String expectedSymbol) {
        return nextTokenMatches(Token.Type.SYMBOL, expectedSymbol);
    }

    /**
     * Tests whether the next token has the expected type. If it does,
     * the token is consumed, otherwise it is not. This method would
     * normally be used only when the token's value is not relevant.
     *
     * @param type The expected type of the next token.
     * @return <code>true</code> if the next token has the expected type.
     */
    boolean nextTokenMatches(Token.Type type) {
        Token t = nextToken();
        if (t.type == type) return true;
        pushBack();
        return false;
    }

    /**
     * Tests whether the next token has the expected type and value.
     * If it does, the token is consumed, otherwise it is not. This
     * method would normally be used when the token's value is
     * important.
     *
     * @param type The expected type of the next token.
     * @param value The expected value of the next token; must
     *              not be <code>null</code>.
     * @return <code>true</code> if the next token has the expected type.
     */
    boolean nextTokenMatches(Token.Type type, String value) {
        Token t = nextToken();
        if (type == t.type && value.equals(t.value)) return true;
        pushBack();
        return false;
    }

    /**
     * Returns the next Token.
     * @return The next Token.
     */
    Token nextToken() {
        int code;
        try { code = tokenizer.nextToken(); }
        catch (IOException e) { throw new Error(e); } // Should never happen
        switch (code) {
            case StreamTokenizer.TT_WORD:
                if (Token.KEYWORDS.contains(tokenizer.sval)) {
                    return new Token(Token.Type.KEYWORD, tokenizer.sval);
                }
                return new Token(Token.Type.NAME, tokenizer.sval);
            case StreamTokenizer.TT_NUMBER:
                return new Token(Token.Type.NUMBER, tokenizer.nval + "");
            case StreamTokenizer.TT_EOL:
                lineNumber++;
                return new Token(Token.Type.EOL, "\n");
            case StreamTokenizer.TT_EOF:
                return new Token(Token.Type.EOF, "EOF");
            default:
                return new Token(Token.Type.SYMBOL, ((char) code) + "");
        }
    }

    /**
     * Returns the most recent Token to the tokenizer.
     */
    void pushBack() {
        tokenizer.pushBack();  
        if (tokenizer.ttype == tokenizer.TT_EOL) lineNumber--;
    }

    /**
     * Utility routine to throw a <code>SyntaxException</code> with the
     * given message.
     * @param message The text to put in the <code>SyntaxException</code>.
     */
    private void error(String message) {
        throw new SyntaxException("Line " + lineNumber + ": " + message);
    }
}
