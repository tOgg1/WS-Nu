package org.ntnunotif.wsnu.examples.misc;

import org.ntnunotif.wsnu.services.general.ServiceUtilities;

/**
 * Example of using the InputManager to handle inputs
 */
public class InputManagerExample {

    // A variable we have for demonstrative value only.
    private boolean variableThatNeedsToBeSetFalseBeforeShutdown = true;

    private ServiceUtilities.InputManager inputManager;

    public InputManagerExample() {

    }

    public void createInputManager(){
        inputManager = new ServiceUtilities.InputManager();
        try {

            // Create our first method-reroute, routing the command "exit --soft" to the handleCommandExit method
            // The first argument is just an identifier, but has to be unique
            // The second argument is the command. If the third argument is set to true, it regexes the commands for matches,
            // if not it just checks for containment.
            // The fourth argument is the actual Method to reroute to.
            // The fifth argument is the object we are going to invoke the method on. In this scenario it is this.
            inputManager.addMethodReroute("reroute1", "exit --soft", false, InputManagerExample.class.getMethod("handleCommandExit", String.class), this);

            // A more advanced example. Lets say we want to reroute the regex "^exit --hard(.*)" to the method System.exit(1)
            // We here need to something we did not do in the last example. As System.exit() takes an integer argument
            // and can thus not accept the command String argument, we need to tell our inputManager to not call the method
            // with the argument. We also need to specify the default argument that is to go into the command, i.e. the value 1.

            // The first arguments are as before. The fourth argument specifies the method "exit" of the System class, taking an int parameter (the type not the class)
            // We can set the invokable to whatever, as the method is run statically.
            // The magic happens at the sixth argument. We have to specificy a list or array of [Integer, Object] tuples. The first index
            // says which index the parameter goes into. E.g. in a method(int a, int b), the parameter a has index 0 and the parameter b has index 1.
            // In our case we have on parameter, which is of type int, and goes into index 0.
            inputManager.addMethodReroute("reroute2", "exit --hard(.*)", true, System.class.getMethod("exit", Integer.TYPE), this, new ServiceUtilities.Tuple[]{new ServiceUtilities.Tuple(0, 0)});

            // Start the inputManager, you should now be able to exit the program in two ways.
            inputManager.start();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not find method");
        }
    }

    /**
     * Our exit-soft method. Note that it must take the parameter command, as we have not specified anything else with the inputManager
     */
    public void handleCommandExit(String command){
        // Exit graciously
        variableThatNeedsToBeSetFalseBeforeShutdown = false;
        System.out.println("You exited graciously!" + variableThatNeedsToBeSetFalseBeforeShutdown);
        System.exit(0);
    }

    public static void main(String[] args) {
        InputManagerExample example = new InputManagerExample();
        example.createInputManager();
    }
}
