
package main;

import main.javafx.JavaFX;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class Main {
    public static void main(String[] args) throws IOException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        // Ideally, just run the Battle Macro game loop to let the game go.
        BattleMacro battleMacro = new BattleMacro();
        battleMacro.mainGameLoop();

//        JavaFX gui = new JavaFX();
//        gui.main(args);
    }
}
