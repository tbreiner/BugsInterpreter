package bugs;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.*;

import tree.Tree;

/**
 * Parser for numeric expressions. Written from starter code for
 * the Bugs language parser in CIT594, Spring 2015.
 * 
 * @author Dave Matuszek and Theresa Breiner
 * @version February 2015
 */
public class Parser {
    /** The tokenizer used by this Parser. */
    StreamTokenizer tokenizer = null;
    /** The number of the line of source code currently being processed. */
    private int lineNumber = 1;

    /**
     * The stack used for holding Trees as they are created.
     */
    public Stack<Tree<Token>> stack = new Stack<Tree<Token>>();

    /**
     * Constructs a Parser for the given string.
     * @param text The string to be parsed.
     */
    public Parser(String text) {
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
     * Tries to build an &lt;expression&gt; on the global stack.
     * <pre>&lt;expression&gt; ::= &lt;arithmetic expression&gt; {  &lt;comparator&gt; &lt;arithmetic expression&gt; }
</pre>
     * A <code>SyntaxException</code> will be thrown if the add_operator
     * is present but not followed by a valid &lt;expression&gt;.
     * @return <code>true</code> if an expression is parsed.
     */
    public boolean isExpression() {
    	if (isArithmeticExpression()) {
    		while (isComparator()) {
    			if (!isArithmeticExpression()) {
    				error("No arithmetic expression after comparator");
    			}
    			makeTree(2, 3, 1);
    		}
    		return true;
    	}
    	return false;
    }

    /**
     * Tries to build an &lt;expression&gt; on the global stack.
     * <pre>&lt;expression&gt; ::= &lt;term&gt; { &lt;add_operator&gt; &lt;expression&gt; }</pre>
     * A <code>SyntaxException</code> will be thrown if the add_operator
     * is present but not followed by a valid &lt;expression&gt;.
     * @return <code>true</code> if an expression is recognized.
     */
    public boolean isArithmeticExpression() {
        if (!isTerm())
            return false;
        while (isAddOperator()) {
            if (!isTerm()) error("Error in expression after '+' or '-'");
            makeTree(2, 3, 1);
        }
        return true;
    }

    /**
     * Tries to build a &lt;term&gt; on the global stack.
     * <pre>&lt;term&gt; ::= &lt;factor&gt; { &lt;multiply_operator&gt; &lt;term&gt; }</pre>
     * A <code>SyntaxException</code> will be thrown if the multiply_operator
     * is present but not followed by a valid &lt;term&gt;.
     * @return <code>true</code> if a term is parsed.
     */

    public boolean isTerm() {
        if (!isFactor()) {
            return false;
        }
        while (isMultiplyOperator()) {
            if (!isFactor()) {
                error("No term after '*' or '/'");
            }
            makeTree(2, 3, 1);
        }
        return true;
    }

    /**
     * Tries to build a &lt;factor&gt; on the global stack.
     * <pre>&lt;factor&gt; ::= [ &lt;unsigned factor&gt; ] &lt;name&gt;</pre>
     * A <code>SyntaxException</code> will be thrown if the opening
     * parenthesis is present but not followed by a valid
     * &lt;expression&gt; and a closing parenthesis.
     * @return <code>true</code> if a factor is parsed.
     */
    public boolean isFactor() {
        if(symbol("+") || symbol("-")) {
            if (isUnsignedFactor()) {
                makeTree(2, 1);
                return true;
            }
            error("No factor following unary plus or minus");
            return false; // Can't ever get here
        }
        return isUnsignedFactor();
    }

    /**
     * Tries to build an &lt;unsigned factor&gt; on the global stack.
     * <pre>&lt;unsigned factor&gt; ::= &lt;variable&gt; . &lt;variable&gt;
     *                    | &lt;function call&gt;
     *                    | &lt;variable&gt;
     *                    | &lt;number&gt;
     *                    | "(" &lt;expression&gt; ")"</pre>
     * A <code>SyntaxException</code> will be thrown if the opening
     * parenthesis is present but not followed by a valid
     * &lt;expression&gt; and a closing parenthesis.
     * @return <code>true</code> if a factor is parsed.
     */
    public boolean isUnsignedFactor() {
        if (name()) {
            if (symbol(".")) {
                // reference to another Bug
                if (name()) {
                    makeTree(2, 3, 1);
                }
                else error("Incorrect use of dot notation");
            }
            else if (isParameterList()) {
                // function call
                pushNewNode("call");
                makeTree(1, 3, 2);
            }
            else {
                // just a variable; leave it on the stack
            }
        }
        else if (number()) {
            // leave the number on the stack
        }
        else if (symbol("(")) {
            stack.pop();
            if (!isExpression()) {
                error("Error in parenthesized expression");
            }
            if (!symbol(")")) {
                error("Unclosed parenthetical expression");
            }
            stack.pop();
        }
        else {
            return false;
        }
       return true;
    }
    
    /**
     * Tries to recognize a &lt;parameter list&gt;.
     * <pre>&ltparameter list&gt; ::= "(" [ &lt;expression&gt; { "," &lt;expression&gt; } ] ")"
     * @return <code>true</code> if a parameter list is recognized.
     */
    public boolean isParameterList() {
        if (!symbol("(")) return false;
        stack.pop(); // remove open paren
        pushNewNode("var");
        if (isExpression()) {
            makeTree(2, 1);
            while (symbol(",")) {
                stack.pop(); // remove comma
                if (!isExpression()) error("No expression after ','");
                makeTree(2, 1);
            }
        }
        if (!symbol(")")) error("Parameter list doesn't end with ')'");
        stack.pop(); // remove close paren
        return true;
    }

    /**
     * Tries to recognize an &lt;add_operator&gt; and put it on the global stack.
     * <pre>&lt;add_operator&gt; ::= "+" | "-"</pre>
     * @return <code>true</code> if an addop is recognized.
     */
    public boolean isAddOperator() {
        return symbol("+") || symbol("-");
    }

    /**
     * Tries to recognize a &lt;multiply_operator&gt; and put it on the global stack.
     * <pre>&lt;multiply_operator&gt; ::= "*" | "/"</pre>
     * @return <code>true</code> if a multiply_operator is recognized.
     */
    public boolean isMultiplyOperator() {
        return symbol("*") || symbol("/");
    }
    
    /**
     * Tries to parse a &lt;variable&gt;; same as &lt;isName&gt;.
     * <pre>&lt;variable&gt; ::= &lt;NAME&gt;</pre>
     * @return <code>true</code> if a variable is parsed.
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
    				stack.pop();
    				//make var decl list node
    				pushNewNode("list");
    				while (isVarDeclaration()) makeTree(2, 1);
    				//add var decl list as child of Allbugs tree
    				makeTree(2, 1);
    				//make function defs list node
    				pushNewNode("list");
    				while(isFunctionDefinition()) makeTree(2, 1);
    				//add func defs list as child of Allbugs tree
    				makeTree(2, 1);
    				if (symbol("}")) {
    					if (isEol()) {
    						stack.pop();
    						return true;
    					}
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
     * Tries to recognize &lt;assignment statement&gt; and push it to the stack.
     * <pre>&lt;assignment statement&gt; ::= &lt;variable&gt; "=" &lt;expression&gt; &lt;eol&gt;</pre>
     * @return <code> true </code> if recognizes assignment statement.
     */
    public boolean isAssignmentStatement() {
    	if (isVariable()) {
    		if (symbol("=")) {
    			stack.pop();
    			if (isExpression()) {
    				if (isEol()) {
    					pushNewNode("assign");
    					makeTree(1, 3, 2);
    					return true;
    				}
    				error("No EOL after assignment");
    			}
    			error("No expression after = sign");
    		}
    		error("No = sign after variable");
    	}
    	return false;
    }
    
    /**
     * Tries to recognize a &lt;block&gt; and push it to the stack
     * <pre>&lt;block&gt; ::= "{" &lt;eol&gt; { &lt;command&gt; } "}" &lt;eol&gt;</pre>
     * @return <code> true </code> if recognizes a block
     */
    public boolean isBlock() {
    	if (symbol("{")) {
    		if (isEol()) {
    			stack.pop();
    			pushNewNode("block");
    			makeTree(1);
    			while (isCommand()){
    				makeTree(2, 1);
    			}
    			if (symbol("}")) {
    				if (isEol()) {
    					stack.pop();
    					return true;
    				}
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
    					stack.pop();
    					//make Bug tree with name as child
    					makeTree(2, 1);
    					pushNewNode("list");
    					while (isVarDeclaration()){
    						//add varDecl to list tree
    						makeTree(2, 1);
    					}
    					//add list tree as child of Bug tree
    					makeTree(2, 1);
    					if (isInitializationBlock()) {
    						//add init block as child of Bug tree if exists
    						makeTree(2, 1);
    					}
    					else {
    						//make empty init block node otherwise
    						pushNewNode("block");
    						pushNewNode("initially");
    						makeTree(1, 2);
    						makeTree(2, 1);
    					}
    					pushNewNode("block");
    					if (isCommand()) {
    						//make commands children of block tree
    						makeTree(2, 1);
    						while (isCommand()) makeTree(2, 1);
    						//add block as child of Bug tree
    						makeTree(2, 1);
    						//make list node for function defs
    						pushNewNode("list");
    						while (isFunctionDefinition()) makeTree(2, 1);
    						//add list of function defs as child of Bug tree
    						makeTree(2, 1);
    						if (symbol("}")) {
    							if (isEol()) {
    								stack.pop();
    								return true;
    							}
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
     * Tries to recognize a &lt;color statement&gt; and push it on the stack.
     * <pre>&lt;color statement&gt; ::= "color" &lt;KEYWORD&gt; &lt;eol&gt;</pre>
     * @return <code> true </code> if recognizes a color statement
     */
    public boolean isColorStatement() {
    	if (keyword("color")) {
    		if (nextTokenMatches(Token.Type.KEYWORD)) {
    			if (isEol()) {
    				makeTree(2, 1);
    				return true;
    			}
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
    
    /**Tries to recognize a &lt;comparator&gt; and push whole comparator on the stack.
     * <pre>&lt;comparator&gt; ::= "<" | "<=" 
     * | "=" | "!=" | ">=" | ">"</pre>
     * @return <code>true</code> if a comparator is recognized.
     */
    public boolean isComparator() {
    	if (symbol("=")) return true;
    	if (symbol("<")) {
    		if (symbol("=")) {
    			stack.pop();
    			stack.pop();
    			pushNewNode("<=");
    			return true;
    		}
    		return true;
    	}
    	if (symbol(">")) {
    		if (symbol("=")) {
    			stack.pop();
    			stack.pop();
    			pushNewNode(">=");
    			return true;
    		}
    		return true;
    	}
    	if (symbol("!")) {
    		if (symbol("=")) {
    			stack.pop();
    			stack.pop();
    			pushNewNode("!=");
    			return true;
    		}
    		error("invalid use of ! as not a comparator");
    	}
    	return false;
    }
    
    /**
     * Tries to recognize a &lt;do statement&gt; and push it onto stack.
     * <pre>&lt;do statement&gt; ::= "do" &lt;variable&gt; [ &lt;parameter list&gt; ] &lt;eol&gt;</pre>
     * @return <code> true </code> if recognizes a do statement
     */
    public boolean isDoStatement() {
    	if (keyword("do")) {
    		stack.pop();
    		pushNewNode("call");
    		if (isVariable()) {
    			if (isParameterList()) {
    				if (isEol()) {
    					makeTree(3, 2, 1);
    					return true;
    				}
    				error("No EOL after parameter list in do statement");
    			}
    			if (isEol()) {
    				pushNewNode("var");
    				makeTree(3, 2, 1);
    				return true;
    			}
    			error("No EOL after variable in do statement");
    		}
    		error("No variable after do");
    	}
    	return false;
    }
    
    /**
     * Tries to recognize an &lt;eol&gt; and pops it from the stack.
     * <pre>&lt;eol&gt; ::= &lt;EOL&gt; { &lt;EOL&gt; }</pre>
     * @return <code> true </code> if an end of line is recognized.
     */
    public boolean isEol() {
    	int count = 0;
    	while (nextTokenMatches(Token.Type.EOL)) {
    		stack.pop();
    		count++;
    	}
    	if (count > 0) return true;
    	return false;
    }

    /**
     * Tries to recognize an &lt;exit if statement&gt; and push it to the stack.
     * <pre>&lt;exit if statement&gt; ::= "exit" "if" &lt;expression&gt; &lt;eol&gt;</pre>
     * @return <code> true </code> if an exit if statement is recognized.
     */
    public boolean isExitIfStatement() {
    	if (keyword("exit")) {
    		if (keyword("if")) {
    			stack.pop();
    			if (isExpression()) {
    				if (isEol()) {
    					makeTree(2, 1);
    					return true;
    				}
    				error("No EOL after exit if statement");
    			}
    			error("No expression after exit if");
    		}
    		error("No if after exit");
    	}
    	return false;
    }
    
    /**
     * Tries to recognize a &lt;function call&gt; and push it to the stack.
     * <pre>&lt;function call&gt; ::= &lt;NAME&gt; &lt;parameter list&gt;</pre>
     * @return <code> true </code> if a function call is recognized.
     */
    public boolean isFunctionCall() {
    	if (name()) {
    		if (isParameterList()) {
    			pushNewNode("call");
    			makeTree(1, 3, 2);
    			return true;
    		}
    		else error("No parameter list after name");
    	}
    	return false;
    }
    
    /**
     * Tries to recognize a &lt;function definition&gt; and push it to the stack.
     * <pre>&lt;function definition&gt; ::= "define" &lt;NAME&gt; [ "using" &lt;variable&gt; 
     * { "," &lt;variable&gt; } ] &lt;block&gt;</pre>
     * @return <code> true </code> if a function definition is recognized.
     *
     */
    public boolean isFunctionDefinition() {
    	if (keyword("define")) {
    		stack.pop();
    		pushNewNode("function");
    		if(name()) {
    			//start function tree
    			makeTree(2, 1);
    			if (keyword("using")) {
    				stack.pop();
    				if (isVariable()) {
    					//start var tree
    					pushNewNode("var");
    					makeTree(1, 2);
    					while (symbol(",") && isVariable()) {
    						Tree<Token> holder = stack.pop();
    						stack.pop();
    						stack.push(holder);
    						//add to var tree
    						makeTree(2, 1);
    					}
    					//add var tree as child of function tree
    					makeTree(2, 1);
    					if (isBlock()) {
    						//add block tree as child of function tree
    						makeTree(2, 1);
    						return true;
    					}
    					error("No block in function definition");
    				}
    				error("No variable after using in function definition");
    			}
    			//add empty var child to function tree
    			pushNewNode("var");
    			makeTree(2, 1);
    			if (isBlock()) {
    				//add block as child of function tree
    				makeTree(2, 1);
    				return true;
    			}
    			error("No block after name in function definition");
    		}
    		error("No name after define");
    	}
    	return false;
    }
    
    /**
     * Tries to recognize an &lt;initialization block&gt; and push it to the stack.
     * <pre>&lt;initialization block&gt; ::= "initially" &lt;block&gt; </pre>
     * @return <code> true </code> if an initialization block is recognized.
     * 
     */
    public boolean isInitializationBlock() {
    	if (keyword("initially")) {
    		if (isBlock()) {
    			makeTree(2, 1);
    			return true;
    		}
    		error("No block after initially");
    	}
    	return false;
    }
    
    /**
     * Tries to recognize a &lt;line action&gt; and push it to the stack.
     * <pre> &lt;line action&gt; ::= "line" &lt;expression&gt; "," 
     * &lt;expression&gt; "," &lt;expression&gt; "," &lt;expression&gt; &lt;eol&gt;</pre> 
     * @return <code> true </code> if recognizes a line action
     */
    public boolean isLineAction() {
    	if (keyword("line")) {
    		if (isExpression()) {
    			if (symbol(",")) {
    				stack.pop();
    				if (isExpression()) {
    					if (symbol(",")) {
    						stack.pop();
    						if (isExpression()) {
    							if (symbol(",")) {
    								stack.pop();
    								if (isExpression()) {
    									if (isEol()) {
    										makeTree(5, 4, 3, 2, 1);
    										return true;
    									}
    		        					error("missing EOL after last expression in line action");
    								}
    		    					error("Missing last expression in line action.");
    							}
    						}
    	    				error("Missing two expressions in line action");
    					}
    				}
        			error("Missing three expressions in line action");
    			}
    		}
    		error("No expression after line action");
    	}
    	return false;
    }
    
    /**
     * Tries to recognize a &lt;loop statement&gt; and push it to the stack.
     * <pre>&lt;loop statement&gt; ::= "loop" &lt;block&gt;</pre>
     * @return <code> true </code> if recognizes a loop statement.
     */
    public boolean isLoopStatement() {
    	if (keyword("loop")) {
    		if (isBlock()) {
    			makeTree(2, 1);
    			return true;
    		}
    		error("No block after loop");
    	}
    	return false;
    }
    
    /**
     * Tries to recognize a &lt;move action&gt; and push it to the stack.
     * <pre>&lt;move action&gt; ::= "move" &lt;expression&gt; &lt;eol&gt;</pre>
     * @return <code> true </code> if recognizes a move action.
     */
    public boolean isMoveAction() {
    	if (keyword("move")) {
    		if (isExpression()) {
    			if (isEol()) {
    				makeTree(2, 1);
    				return true;
    			}
    			else error("No EOL after move and expression");
    		}
    		error("No expression after move");
    	}
    	return false;
    }
    
    /**
     * Tries to recognize a &lt;moveto action&gt; and push it to the stack.
     * <pre>&lt;moveto action&gt; ::= "moveto" &lt;expression&gt; "," 
     * &lt;expression&gt; &lt;eol&gt;</pre>
     * @return <code> true </code> if recognizes a moveto action
     */
    public boolean isMoveToAction() {
    	if (keyword("moveto")) {
    		if (isExpression()) {
    			if (symbol(",")) {
    				stack.pop();
    				if (isExpression()) {
    					if (isEol()) {
    						makeTree(3, 2, 1);
    						return true;
    					}
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
    		//start program tree with allbugs code as child
    		pushNewNode("program");
    		makeTree(1, 2);
    		pushNewNode("list");
    		if (isBugDefinition()) {
    			//add bug def as child of list tree
    			makeTree(2, 1);
    			while (isBugDefinition()) makeTree(2, 1);
    			//add list tree as child of program tree
    			makeTree(2, 1);
    			Token t = nextToken();
    			if (t.type == Token.Type.EOF) {
    				return true;
    			}
    			error("Not EOF after bug definitions in program");
    		}
    		error("Allbugs code but no bug definition in program");
    	}
    	
    	if (isBugDefinition()) {
        	pushNewNode("list");
        	//add bug defs to list tree
        	makeTree(1, 2);
    		while (isBugDefinition()) makeTree(2, 1);
    		//start program tree with empty Allbugs node
    		pushNewNode("list");
    		pushNewNode("Allbugs");
    		makeTree(1, 2);
    		pushNewNode("list");
    		makeTree(2, 1);
        	pushNewNode("program");
        	makeTree(1, 2);
    		//add list tree as child of program tree
    		makeTree(1, 2);
    		Token t = nextToken();
    		if (t.type == Token.Type.EOF) {
    			return true;
    		}
    		error("Not EOF after bug definitions in program, no allbugs.");
    	}
    	return false;

    }
    
    /**
     * Tries to recognize a &lt;return statement&gt; and push it to the stack.
     * <pre>&lt;return statement&gt; ::= "return" &lt;expression&gt; &lt;eol&gt;</pre>
     * @return <code> true </code> if recognizes return statement
     */
    public boolean isReturnStatement() {
    	if (keyword("return")) {
    		if (isExpression()) {
    			if (isEol()) {
    				makeTree(2, 1);
    				return true;
    			}
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
     * Tries to recognize a &lt;switch statement&gt; and push it to the stack.
     * <pre>&lt;switch statement&gt; ::= "switch" "{" &lt;eol&gt; 
     * { "case" &lt;expression&gt; &lt;eol&gt; { &lt;command&gt; } } "}" &lt;eol&gt;</pre>
     * @return <code> true </code> if recognizes switch statement
     */
    public boolean isSwitchStatement() {
    	if (keyword("switch")) {
    		if (symbol("{")) {
    			if (isEol()) {
    				stack.pop();
    				while (keyword("case") && isExpression() && isEol()) {
    					//make case tree
    					makeTree(2, 1);
    					//make block tree
    					pushNewNode("block");
    					while (isCommand()) {
    						//add command as child of block tree
    						makeTree(2, 1);
    					}
    					//add block tree as child of case tree
    					makeTree(2, 1);
    					//add case tree as child of switch tree
    					makeTree(2, 1);
    				}
    				if (symbol("}")) {
    					if (isEol()) {
    						stack.pop();
    						return true;
    					}
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
     * Tries to recognize a &lt;turn action&gt; and push it on the stack.
     * <pre>&lt;turn action&gt; ::= "turn" &lt;expression&gt; &lt;eol&gt;</pre>
     * @return <code> true </code> if recognizes a turn action.
     */
    public boolean isTurnAction() {
    	if (keyword("turn")) {
    		if (isExpression()) {
    			if (isEol()) {
    				makeTree(2, 1);
    				return true;
    			}
    			error("No EOL after turn and expression");
    		}
    		error("No expression after turn");
    	}
    	return false;
    }
    
    /**
     * Tries to recognize a &lt;turnto action&gt; and push it to the stack.
     * <pre>&lt;turnto action&gt; ::= "turnto" &lt;expression&gt; &lt;eol&gt;</pre>
     * @return <code> true </code> if recognizes a turnto action.
     */
    public boolean isTurnToAction() {
    	if (keyword("turnto")) {
    		if (isExpression()) {
    			if (isEol()) {
    				makeTree(2, 1);
    				return true;
    			}
    			error("No EOL after turnto and expression");
    		}
    		error("No expression after turnto");
    	}
    	return false;
    }
    
    /**
     * Tries to recognize a &lt;var declaration&gt; and push it to the stack.
     * <pre>&lt;var declaration&gt; ::= "var" &lt;NAME&gt; { "," &lt;NAME&gt; } &lt;eol&gt;</pre>
     * @return <code> true </code> if recognizes a var declaration.
     */
    public boolean isVarDeclaration() {
    	if (keyword("var")) {
    		if (name()) {
    			makeTree(2, 1);
    			while (symbol(",")) {
    				stack.pop();
    				if (!name()) error("No name after comma in var declaration");
    				makeTree(2, 1);
    			}
    			if (isEol()) return true;
    			error("No EOL after var declaration");
    		}
    		error("No name after var");
    	}
    	return false;
    }

    //------------------------- Private "helper" methods
    
    /**
     * Creates a new Tree consisting of a single node containing a
     * Token with the correct type and the given <code>value</code>,
     * and pushes it onto the global stack. 
     *
     * @param value The value of the token to be pushed onto the global stack.
     */
    private void pushNewNode(String value) {
        stack.push(new Tree<Token>(new Token(Token.typeOf(value), value)));
    }

    /**
     * Tests whether the next token is a number. If it is, the token
     * is moved to the stack, otherwise it is not.
     * 
     * @return <code>true</code> if the next token is a number.
     */
    private boolean number() {
        return nextTokenMatches(Token.Type.NUMBER);
    }

    /**
     * Tests whether the next token is a name. If it is, the token
     * is moved to the stack, otherwise it is not.
     * 
     * @return <code>true</code> if the next token is a name.
     */
    private boolean name() {
        return nextTokenMatches(Token.Type.NAME);
    }

    /**
     * Tests whether the next token is the expected name. If it is, the token
     * is moved to the stack, otherwise it is not.
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
     * the token is moved to the stack, otherwise it is not.
     * 
     * @param expectedSymbol The single-character String that is expected
     *        as the next symbol.
     * @return <code>true</code> if the next token is the expected symbol.
     */
    private boolean symbol(String expectedSymbol) {
        return nextTokenMatches(Token.Type.SYMBOL, expectedSymbol);
    }

    /**
     * If the next Token has the expected type, it is used as the
     * value of a new (childless) Tree node, and that node
     * is then pushed onto the stack. If the next Token does not
     * have the expected type, this method effectively does nothing.
     * 
     * @param type The expected type of the next token.
     * @return <code>true</code> if the next token has the expected type.
     */
    private boolean nextTokenMatches(Token.Type type) {
        Token t = nextToken();
        if (t.type == type) {
            stack.push(new Tree<Token>(t));
            return true;
        }
        pushBack();
        return false;
    }

    /**
     * If the next Token has the expected type and value, it is used as
     * the value of a new (childless) Tree node, and that node
     * is then pushed onto the stack; otherwise, this method does
     * nothing.
     * 
     * @param type The expected type of the next token.
     * @param value The expected value of the next token; must
     *              not be <code>null</code>.
     * @return <code>true</code> if the next token has the expected type.
     */
    private boolean nextTokenMatches(Token.Type type, String value) {
        Token t = nextToken();
        if (type == t.type && value.equals(t.value)) {
            stack.push(new Tree<Token>(t));
            return true;
        }
        pushBack();
        return false;
    }

    /**
     * Returns the next Token. Increments the global variable
     * <code>lineNumber</code> when an EOL is returned.
     * 
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
     * Returns the most recent Token to the tokenizer. Decrements the global
     * variable <code>lineNumber</code> if an EOL is pushed back.
     */
    void pushBack() {
        tokenizer.pushBack();
        if (tokenizer.ttype == StreamTokenizer.TT_EOL) lineNumber--;
    }

    /**
     * Assembles some number of elements from the top of the global stack
     * into a new Tree, and replaces those elements with the new Tree.<p>
     * <b>Caution:</b> The arguments must be consecutive integers 1..N,
     * in any order, but with no gaps; for example, makeTree(2,4,1,5)
     * would cause problems (3 was omitted).
     * 
     * @param rootIndex Which stack element (counting from 1) to use as
     * the root of the new Tree.
     * @param childIndices Which stack elements to use as the children
     * of the root.
     */    
    void makeTree(int rootIndex, int... childIndices) {
        // Get root from stack
        Tree<Token> root = getStackItem(rootIndex);
        // Get other trees from stack and add them as children of root
        for (int i = 0; i < childIndices.length; i++) {
            root.addChild(getStackItem(childIndices[i]));
        }
        // Pop root and all children from stack
        for (int i = 0; i <= childIndices.length; i++) {
            stack.pop();
        }
        // Put the root back on the stack
        stack.push(root);
    }
    
    /**
     * Returns the n-th item from the top of the global stack (counting the
     * top element as 1).
     * 
     * @param n Which stack element to return.
     * @return The n-th element in the global stack.
     */
    private Tree<Token> getStackItem(int n) {
        return stack.get(stack.size() - n);
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