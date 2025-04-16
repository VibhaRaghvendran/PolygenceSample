/**
 *	Object identifier to store variables
 *
 *	@author	Vibha Raghvendran
 *	@since	February 26 2025
 */

public class Identifier {
    private String variable; // variable name
    private double value; // variable value
    private boolean isChangeable; // if the variable can change or not

    /**
     *	Constructor 1: used for storing e and pi
     *
     *  @param name      variable name
     *  @param value     variable value
     *  @param isChangeable  changeable status
     */
    public Identifier (String name, double value, boolean isChangeable) {
        variable = name;
        this.value = value;
        this.isChangeable = isChangeable;
    }

    /**
     *	Constructor 2: used for all other variables
     *
     *  @param name      variable name
     */
    public Identifier (String name) {
        variable = name;
        // sets defaults
        value = 0;
        isChangeable = true;
    }

    /**
     *	Returns the name of the variable
     *
     *  @return variable name
     */
    public String getName () {
        return variable;
    }

    /**
     *	Returns the value of the variable
     *
     *  @return variable value
     */
    public double getValue () {
        return value;
    }

    /**
     *	Returns the changeable status of the variable
     *
     *  @return changeable status
     */
    public boolean getChangeable () {
        return isChangeable;
    }

    /**
     *	Sets the value of the variable to a given decimal
     *
     *  @param value        decimal to change value to
     */
    public void setValue (double value) {
        this.value = value;
    }

    /**
     *	Formats the variable name and value
     *
     *  @return formatted string
     */
    public String toString () {
        return String.format("%-12s=%11.7f", variable, value);
    }
}
