package io.siggi.cubecore.bedrockapi;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ControllerButtons {
    public enum Button {
        FACE_DOWN,
        FACE_RIGHT,
        FACE_LEFT,
        FACE_UP,
        LEFT_BUMPER,
        RIGHT_BUMPER,
        LEFT_TRIGGER,
        RIGHT_TRIGGER,
        LEFT_STICK,
        RIGHT_STICK,
        SELECT_BUTTON,
        MENU_BUTTON,
        DPAD_UP,
        DPAD_DOWN,
        DPAD_LEFT,
        DPAD_RIGHT
    }

    private static final Map<BedrockDeviceInfo.BedrockDevice, Map<Button,String>> mappings = new HashMap<>();
    private static final Map<Button,String> buttonNames = new HashMap<>();

    static {
        buttonNames.put(Button.FACE_DOWN, "A");
        buttonNames.put(Button.FACE_RIGHT, "B");
        buttonNames.put(Button.FACE_LEFT, "X");
        buttonNames.put(Button.FACE_UP, "Y");
        buttonNames.put(Button.LEFT_BUMPER, "LB");
        buttonNames.put(Button.RIGHT_BUMPER, "RB");
        buttonNames.put(Button.LEFT_TRIGGER, "LT");
        buttonNames.put(Button.RIGHT_TRIGGER, "RT");
        buttonNames.put(Button.LEFT_STICK, "LS");
        buttonNames.put(Button.RIGHT_STICK, "RS");
        buttonNames.put(Button.SELECT_BUTTON, "Back");
        buttonNames.put(Button.MENU_BUTTON, "Start");
        buttonNames.put(Button.DPAD_UP, "Up");
        buttonNames.put(Button.DPAD_LEFT, "Left");
        buttonNames.put(Button.DPAD_DOWN, "Down");
        buttonNames.put(Button.DPAD_RIGHT, "Right");

        Map<Button,String> xboxMappings = new HashMap<>();
        mappings.put(BedrockDeviceInfo.BedrockDevice.XBOX, xboxMappings);
        mappings.put(null, xboxMappings);
        xboxMappings.put(Button.FACE_DOWN, "\ue000"); // A
        xboxMappings.put(Button.FACE_RIGHT, "\ue001"); // B
        xboxMappings.put(Button.FACE_LEFT, "\ue002"); // X
        xboxMappings.put(Button.FACE_UP, "\ue003"); // Y
        xboxMappings.put(Button.LEFT_BUMPER, "\ue004"); // LB
        xboxMappings.put(Button.RIGHT_BUMPER, "\ue005"); // RB
        xboxMappings.put(Button.LEFT_TRIGGER, "\ue006"); // LT
        xboxMappings.put(Button.RIGHT_TRIGGER, "\ue007"); // RT
        xboxMappings.put(Button.LEFT_STICK, "\ue00a"); // LS
        xboxMappings.put(Button.RIGHT_STICK, "\ue00b"); // RS
        xboxMappings.put(Button.SELECT_BUTTON, "\ue008"); // Back
        xboxMappings.put(Button.MENU_BUTTON, "\ue009"); // Start
        xboxMappings.put(Button.DPAD_UP, "\ue00c"); // Dpad Up
        xboxMappings.put(Button.DPAD_LEFT, "\ue00d"); // Dpad Left
        xboxMappings.put(Button.DPAD_DOWN, "\ue00e"); // Dpad Down
        xboxMappings.put(Button.DPAD_RIGHT, "\ue00f"); // Dpad Right

        Map<Button,String> playstationMappings = new HashMap<>();
        mappings.put(BedrockDeviceInfo.BedrockDevice.PS4, playstationMappings);
        playstationMappings.put(Button.FACE_DOWN, "\ue020"); // X
        playstationMappings.put(Button.FACE_RIGHT, "\ue021"); // Circle
        playstationMappings.put(Button.FACE_LEFT, "\ue022"); // Square
        playstationMappings.put(Button.FACE_UP, "\ue023"); // Triangle
        playstationMappings.put(Button.LEFT_BUMPER, "\ue024"); // L1
        playstationMappings.put(Button.RIGHT_BUMPER, "\ue025"); // R1
        playstationMappings.put(Button.LEFT_TRIGGER, "\ue026"); // L2
        playstationMappings.put(Button.RIGHT_TRIGGER, "\ue027"); // R2
        playstationMappings.put(Button.LEFT_STICK, "\ue02a"); // L3
        playstationMappings.put(Button.RIGHT_STICK, "\ue02b"); // R3
        playstationMappings.put(Button.SELECT_BUTTON, "\ue028"); // Touchpad
        playstationMappings.put(Button.MENU_BUTTON, "\ue029"); // Options
        playstationMappings.put(Button.DPAD_UP, "\ue02c"); // Dpad Up
        playstationMappings.put(Button.DPAD_LEFT, "\ue02d"); // Dpad Left
        playstationMappings.put(Button.DPAD_DOWN, "\ue02e"); // Dpad Down
        playstationMappings.put(Button.DPAD_RIGHT, "\ue02f"); // Dpad Right

        Map<Button,String> switchMappings = new HashMap<>();
        mappings.put(BedrockDeviceInfo.BedrockDevice.NX, switchMappings);
        switchMappings.put(Button.FACE_DOWN, "\ue041"); // B
        switchMappings.put(Button.FACE_RIGHT, "\ue040"); // A
        switchMappings.put(Button.FACE_LEFT, "\ue043"); // Y
        switchMappings.put(Button.FACE_UP, "\ue042"); // X
        switchMappings.put(Button.LEFT_BUMPER, "\ue044"); // L
        switchMappings.put(Button.RIGHT_BUMPER, "\ue045"); // R
        switchMappings.put(Button.LEFT_TRIGGER, "\ue046"); // ZL
        switchMappings.put(Button.RIGHT_TRIGGER, "\ue047"); // ZR
        switchMappings.put(Button.LEFT_STICK, "\ue04a"); // L Stick
        switchMappings.put(Button.RIGHT_STICK, "\ue04b"); // R Stick
        switchMappings.put(Button.SELECT_BUTTON, "\ue048"); // -
        switchMappings.put(Button.MENU_BUTTON, "\ue049"); // +
        switchMappings.put(Button.DPAD_UP, "\ue04c"); // Dpad Up
        switchMappings.put(Button.DPAD_LEFT, "\ue04d"); // Dpad Left
        switchMappings.put(Button.DPAD_DOWN, "\ue04e"); // Dpad Down
        switchMappings.put(Button.DPAD_RIGHT, "\ue04f"); // Dpad Right
    }

    public static Map<Button,String> getConsoleMapping(BedrockDeviceInfo.BedrockDevice device) {
        Map<Button, String> consoleMapping = getConsoleMapping0(device);
        return consoleMapping == null ? null : Collections.unmodifiableMap(consoleMapping);
    }

    private static Map<Button,String> getConsoleMapping0(BedrockDeviceInfo.BedrockDevice device) {
        Map<Button, String> mapping = mappings.get(device);
        if (mapping == null)
            mapping = mappings.get(null);
        return mapping;
    }

    public static String get(BedrockDeviceInfo.BedrockDevice device, Button button) {
        if (button == null) return null;
        return getConsoleMapping0(device).get(button);
    }

    public static String getButtonName(Button button) {
        return buttonNames.get(button);
    }
}
