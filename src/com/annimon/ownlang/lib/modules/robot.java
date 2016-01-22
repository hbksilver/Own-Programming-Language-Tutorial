package com.annimon.ownlang.lib.modules;

import com.annimon.ownlang.exceptions.ArgumentsMismatchException;
import com.annimon.ownlang.lib.*;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author aNNiMON
 */
public final class robot implements Module {
    
    private static final int CLICK_DELAY = 200;
    private static final int TYPING_DELAY = 50;
    
    private static final Map<Character, Integer> SYMBOL_CODES;
    static {
        SYMBOL_CODES = new HashMap<>(10);
        SYMBOL_CODES.put('_', KeyEvent.VK_MINUS);
        SYMBOL_CODES.put(':', KeyEvent.VK_SEMICOLON);
    }
    
    private static Robot awtRobot;
    
    @Override
    public void init() {
        initialize();
        
        Functions.set("click", convertFunction(robot::click));
        Functions.set("delay", convertFunction(awtRobot::delay));
        Functions.set("setAutoDelay", convertFunction(awtRobot::setAutoDelay));
        Functions.set("keyPress", convertFunction(awtRobot::keyPress));
        Functions.set("keyRelease", convertFunction(awtRobot::keyRelease));
        Functions.set("mousePress", convertFunction(awtRobot::mousePress));
        Functions.set("mouseRelease", convertFunction(awtRobot::mouseRelease));
        Functions.set("mouseWheel", convertFunction(awtRobot::mouseWheel));
        Functions.set("mouseMove", (args) -> {
            if (args.length != 2) throw new ArgumentsMismatchException("Two arguments expected");
            try {
                awtRobot.mouseMove((int) args[0].asNumber(), (int) args[1].asNumber());
            } catch (IllegalArgumentException iae) { }
            return NumberValue.ZERO;
        });
        Functions.set("typeText", (args) -> {
            if (args.length != 1) throw new ArgumentsMismatchException("One argument expected");
            try {
                typeText(args[0].asString());
            } catch (IllegalArgumentException iae) { }
            return NumberValue.ZERO;
        });
        
        Variables.set("VK_DOWN", new NumberValue(KeyEvent.VK_DOWN));
        Variables.set("VK_LEFT", new NumberValue(KeyEvent.VK_LEFT));
        Variables.set("VK_RIGHT", new NumberValue(KeyEvent.VK_RIGHT));
        Variables.set("VK_FIRE", new NumberValue(KeyEvent.VK_ENTER));
        Variables.set("VK_ESCAPE", new NumberValue(KeyEvent.VK_ESCAPE));
        
        Variables.set("BUTTON1", new NumberValue(InputEvent.BUTTON1_MASK));
        Variables.set("BUTTON2", new NumberValue(InputEvent.BUTTON2_MASK));
        Variables.set("BUTTON3", new NumberValue(InputEvent.BUTTON3_MASK));
    }
    
    private static void initialize() {
        try {
            awtRobot = new Robot();
        } catch (AWTException awte) {
            throw new RuntimeException("Unable to create robot instance", awte);
        }
    }
    
    @FunctionalInterface
    private interface RobotIntConsumer {
        void accept(int value) throws IllegalArgumentException;
    }
    
    private static Function convertFunction(RobotIntConsumer consumer) {
        return args -> {
            if (args.length != 1) throw new ArgumentsMismatchException("One argument expected");
            try {
                consumer.accept((int) args[0].asNumber());
            } catch (IllegalArgumentException iae) { }
            return NumberValue.ZERO;
        };
    }
    
    private static synchronized void click(int buttons) {
        awtRobot.mousePress(buttons);
        awtRobot.delay(CLICK_DELAY);
        awtRobot.mouseRelease(buttons);
    }
 
    private static synchronized void typeText(String text) {
        for (char ch : text.toCharArray()) {
            typeSymbol(ch);
        }
    }

    private static void typeSymbol(char ch) {
        int code = KeyEvent.getExtendedKeyCodeForChar(ch);

        boolean isUpperCase = Character.isLetter(ch) && Character.isUpperCase(ch);
        boolean needPressShift = isUpperCase;
        if (!isUpperCase) {
            final int symbolIndex = "!@#$%^&*()".indexOf(ch);
            if (symbolIndex != -1) {
                needPressShift = true;
                code = '1' + symbolIndex;
            } else if (SYMBOL_CODES.containsKey(ch)) {
                needPressShift = true;
                code = SYMBOL_CODES.get(ch);
            }
        }

        if (code == KeyEvent.VK_UNDEFINED) return;

        if (needPressShift) {
            // press shift
            awtRobot.keyPress(KeyEvent.VK_SHIFT);
            awtRobot.delay(TYPING_DELAY);
        }

        awtRobot.keyPress(code);
        awtRobot.delay(TYPING_DELAY);
        awtRobot.keyRelease(code);

        if (needPressShift) {
            // release shift
            awtRobot.delay(TYPING_DELAY);
            awtRobot.keyRelease(KeyEvent.VK_SHIFT);
            awtRobot.delay(TYPING_DELAY);
        }
    }
}