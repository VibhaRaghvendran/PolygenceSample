import java.util.ArrayList;
import java.util.List;		// used by expression evaluator
import java.util.Scanner;

/**
 *	Uses stacks to emulate a simple arithmetic calculator
 *
 *	@author	Vibha Raghvendran
 *	@since	February 26 2025
 */
public class SimpleCalc {

    private ExprUtils utils;	// expression utilities

    private ArrayStack<Double> valueStack;		// value stack
    private ArrayStack<String> operatorStack; // operator stack
    private List<Identifier> variables; // arraylist of stored variables
    String original; // keeps track of initial variable to store value in
    private Scanner scan;

    // constructor
    public SimpleCalc() {
        // initializes everything
        scan = new Scanner(System.in);
        utils = new ExprUtils();
        variables = new ArrayList<>();
        // adds e and pi and sets them to unchangeable
        variables.add(new Identifier("e", Math.E, false));
        variables.add(new Identifier("pi", Math.PI, false));
    }

    public static void main(String[] args) {
        SimpleCalc sc = new SimpleCalc();
        sc.run();
    }

    /**
     *	Prints initial and final prompts (beginning and end of game)
     */
    public void run() {
        System.out.println("\nWelcome to SimpleCalc!!!");
        System.out.println();
        runCalc();
        System.out.println("\nThanks for using SimpleCalc! Goodbye.\n");
    }

    /**
     *	Prompt the user for expressions, run the expression evaluator,
     *	and display the answer.
     */
    public void runCalc() {
        String exp = "";
        // scans in expression
        System.out.println("Enter an expression. Press \"q\" to quit, \"h\" for help, and \"i\" to see the identifiers.");
        exp = scan.nextLine();
        String modify = "";
        // removes whitespace
        for (int i = 0; i < exp.length(); i++) {
            if (!(exp.charAt(i) == ' ')) {
                modify += exp.charAt(i);
            }
        }

        // while player chooses not to quit, keep going
        while (!modify.equalsIgnoreCase("q")) {
            // if they press "h", print out help menu
            if (modify.equalsIgnoreCase("h")) {
                printHelp();
            }
            // if they press "I", print out identifier menu
            else if (modify.equalsIgnoreCase("I")) {
                printVar();
            }
            else {
                List<String> tokens = utils.tokenizeExpression(modify);
                // if the expression has a variable in it
                if (Character.isLetter(tokens.get(0).charAt(0))) {
                    original = tokens.get(0); // stores original variable value
                    // if only the variable is entered
                    if (tokens.size() == 1)
                        singleVariable(tokens.get(0));
                    // other cases
                    else {
                        // checks for invalid variables
                        if (tokens.size() >= 2 && Character.isLetter(tokens.get(0).charAt(0)) && Character.isDigit(tokens.get(1).charAt(0)) ||
                                Character.isLetter(tokens.get(1).charAt(0)) && Character.isDigit(tokens.get(0).charAt(0))) {
                            System.out.println("Invalid expression. Enter again:");
                            exp = scan.nextLine();
                            modify = "";
                            for (int i = 0; i < exp.length(); i++) {
                                if (!(exp.charAt(i) == ' ')) {
                                    modify += exp.charAt(i);
                                }
                            }
                            tokens = utils.tokenizeExpression(modify);
                            original = tokens.get(0);
                        }
                        processVarExpression(tokens);
                    }
                }
                // only numbers in the expression
                else {
                    double result = evaluateExpression(tokens);
                    System.out.println(result);
                }
            }

            // keeps prompts going
            System.out.println("Enter an expression. Press \"q\" to quit, \"h\" for help, and \"i\" to see the identifiers.");
            exp = scan.nextLine();
            modify = "";
            for (int i = 0; i < exp.length(); i++) {
                if (!(exp.charAt(i) == ' ')) {
                    modify += exp.charAt(i);
                }
            }
        }
    }

    /**	Print help */
    public void printHelp() {
        System.out.println();
        System.out.println("Help:");
        System.out.println("  h - this message\n  q - quit\n");
        System.out.println("Expressions can contain:");
        System.out.println("  integers or decimal numbers");
        System.out.println("  arithmetic operators +, -, *, /, %, ^");
        System.out.println("  parentheses '(' and ')'");
        System.out.println();
    }

    /**	Print identifiers */
    public void printVar () {
        System.out.println();
        System.out.println("Identifiers:");
        for (int i = 0; i < variables.size(); i++) {
            System.out.println(variables.get(i).toString());
        }
        System.out.println();
    }

    /**
     *	Evaluate expression and return the value
     *	@param tokens	a List of String tokens making up an arithmetic expression
     *	@return			a double value of the evaluated expression
     */
    public double evaluateExpression(List<String> tokens) {
        double value = 0; // variable that stores final value

        // initializes stacks
        valueStack = new ArrayStack<>();
        operatorStack = new ArrayStack<>();

        // loops through all the tokens
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);

            // either number or variable
            if (!utils.isOperator(token.charAt(0))) {
                // if variable, push value of variable onto the value stack
                if (Character.isLetter(tokens.get(i).charAt(0))) {
                    valueStack.push(getVariable(tokens.get(i)).getValue());
                }
                // else push the number onto the value stack
                else {
                    valueStack.push(Double.parseDouble(token));
                }
            }
            // open parentheses: push onto the operator stack
            else if (token.equals("(")) {
                operatorStack.push(tokens.get(i));
            }
            // closed parentheses:
            else if (token.equals(")")) {
                // performs operations until the open parentheses is detected
                while (!operatorStack.isEmpty() && operatorStack.peek() != "(") {
                    // if open parentheses is detected in the middle, break the loop
                    if (operatorStack.peek().equals("(")) {
                        break;
                    }
                    // perform operation
                    operation(valueStack.pop(), valueStack.pop(), operatorStack.pop());
                }
                // removes open parentheses from operator stack
                operatorStack.pop();
            }
            // other operators:
            else if (utils.isOperator(token.charAt(0))) {
                // performs operations while the precedence is higher
                while (!operatorStack.isEmpty() && hasPrecedence(token, operatorStack.peek())) {
                    operation(valueStack.pop(), valueStack.pop(), operatorStack.pop());
                }
                // pushes new operator onto the operator stack
                operatorStack.push(token);
            }
        }
        // takes care of any operations that still need to be performed
        while (!operatorStack.isEmpty()) {
            operation(valueStack.pop(), valueStack.pop(), operatorStack.pop());
        }

        // last value in the value stack must be the final answer
        value = valueStack.pop();

        // returns the final answer
        return value;
    }

    /**
     *	Processes one operation and puts the new value onto valueStack
     */
    public void operation (double temp1, double temp2, String operator) {
        double result = 0.0; // final result

        // cases on operator
        if (operator.equals("+"))
            result = temp1+temp2;
        else if (operator.equals("-"))
            result = temp2-temp1;
        else if (operator.equals("*"))
            result = temp1*temp2;
        else if (operator.equals("/"))
            result = temp2/temp1;
        else if (operator.equals("^"))
            result = Math.pow(temp2, temp1);
        else if (operator.equals("%"))
            result = temp2%temp1;

        // pushes the final result onto the value stack
        valueStack.push(result);
    }

    /**
     *	Processes a variable equation
     *
     *  @param tokens       the tokenized expression with variables in it
     */
    public void processVarExpression (List<String> tokens) {
        // if the variable hasn't been encountered before:
        if (getVariable(tokens.get(0)) == null) {
            // if it is in the correct form with an equals sign:
            if (tokens.get(1).equals("=")) {
                // add new variable to arraylist
                variables.add(new Identifier(tokens.get(0)));
                // if it is just one value, make that the value of the variable and print it out
                if (tokens.size() == 3) {
                    if (getVariable(original).getChangeable())
                        getVariable(original).setValue(Double.parseDouble(tokens.get(2)));
                    System.out.println(getVariable(original).toString());
                }
                // if it is an expression, process it
                else {
                    List<String> tempTokens = new ArrayList<>();
                    for (int i = 2; i < tokens.size(); i++) {
                        if (Character.isLetter(tokens.get(i).charAt(0))) {
                            tempTokens.add(String.valueOf(getVariable(tokens.get(i)).getValue()));
                        } else {
                            tempTokens.add(tokens.get(i));
                        }
                    }

                    // change the value of the variable by putting it into evaluateExpression and print variable
                    double value = evaluateExpression(tempTokens);
                    if (getVariable(original).getChangeable())
                        getVariable(original).setValue(value);
                    System.out.println(getVariable(original).toString());
                }
            }
            // if it is another expression with a preexisting variable
            else {
                List<String> tempTokens = new ArrayList<>();
                for (int i = 0; i < tokens.size(); i++) {
                    if (Character.isLetter(tokens.get(i).charAt(0))) {
                        tempTokens.add(String.valueOf(getVariable(tokens.get(i)).getValue()));
                    } else {
                        tempTokens.add(tokens.get(i));
                    }
                }

                // evaluates the expression and prints it out
                double value = evaluateExpression(tempTokens);
                System.out.println(value);
            }
        }
        // if the variable has already been encountered:
        else {
            // detects equals sign to change the value
            if (tokens.get(1).equals("=")) {
                List<String> tempTokens = new ArrayList<>();
                for (int i = 2; i < tokens.size(); i++) {
                    if (Character.isLetter(tokens.get(i).charAt(0))) {
                        tempTokens.add(String.valueOf(getVariable(tokens.get(i)).getValue()));
                    } else {
                        tempTokens.add(tokens.get(i));
                    }
                }

                // changes value and prints it out
                double value = evaluateExpression(tempTokens);
                if (getVariable(original).getChangeable())
                    getVariable(original).setValue(value);
                System.out.println(getVariable(original).toString());
            }
            // expression without the equals sign
            else {
                List<String> tempTokens = new ArrayList<>();
                for (int i = 0; i < tokens.size(); i++) {
                    if (Character.isLetter(tokens.get(i).charAt(0)) && getVariable(tokens.get(i)) == (null)) {
                        tempTokens.add(String.valueOf(0.0));
                    }
                    else if (Character.isLetter(tokens.get(i).charAt(0)) && !(getVariable(tokens.get(i)) == (null))) {
                        tempTokens.add(String.valueOf(getVariable(tokens.get(i)).getValue()));
                    } else {
                        tempTokens.add(tokens.get(i));
                    }
                }

                // evaluates and prints out
                double value = evaluateExpression(tempTokens);
                System.out.println(value);
            }
        }
    }

    /**
     *	Prints out the appropriate vales uf a single variable is entered
     */
    public void singleVariable (String var) {
        if (getVariable(var) != null) {
            System.out.println(getVariable(var).getValue());
        }
        else {
            System.out.println("0.0");
        }
    }

    /**
     *	Searches through the identifier list to see if one with such a name exists
     *
     *  @param name      the name of the identifier
     *  @return          the name of the identifier if it exists, null otherwise
     */
    public Identifier getVariable (String name) {
        for (int i = 0; i < variables.size(); i++) {
            if (variables.get(i).getName().equals(name)) {
                return variables.get(i);
            }
        }

        return null;
    }

    /**
     *	Precedence of operators
     *	@param op1	operator 1
     *	@param op2	operator 2
     *	@return		true if op2 has higher or same precedence as op1; false otherwise
     *	Algorithm:
     *		if op1 is exponent, then false
     *		if op2 is either left or right parenthesis, then false
     *		if op1 is multiplication or division or modulus and
     *				op2 is addition or subtraction, then false
     *		otherwise true
     */
    private boolean hasPrecedence(String op1, String op2) {
        if (op1.equals("^")) return false;
        if (op2.equals("(") || op2.equals(")")) return false;
        if ((op1.equals("*") || op1.equals("/") || op1.equals("%"))
                && (op2.equals("+") || op2.equals("-")))
            return false;
        return true;
    }

}
