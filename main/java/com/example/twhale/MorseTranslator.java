package com.example.twhale;

import java.util.HashMap;
import java.util.Map;

public class MorseTranslator {

    private final Map<Character, String> morseMap = new HashMap<Character, String>() {{
        put('A', ".-"); put('B', "-...");
        put('C', "-.-.");put('D', "-..");
        put('E', ".");put('F', "..-.");
        put('G', "--.");put('H', "....");
        put('I', "..");put('J', ".---");
        put('K', "-.-");put('L', ".-..");
        put('M', "--");put('N', "-.");
        put('O', "---");put('P', ".--.");
        put('Q', "--.-");put('R', ".-.");
        put('S', "...");put('T', "-");
        put('U', "..-");put('V', "...-");
        put('W', ".--");put('X', "-..-");
        put('Y', "-.--");put('Z', "--..");
        put('1', ".----");put('2', "..---");
        put('3', "...--");put('4', "....-");
        put('5', ".....");put('6', "-....");
        put('7', "--...");put('8', "---..");
        put('9', "----.");put('0', "-----");
        put(' ', " ");
    }};

    public String toMorse(String text) {
        StringBuilder morseText = new StringBuilder();
        text = text.toUpperCase();

        for (char letter : text.toCharArray()) {
            if (morseMap.containsKey(letter)) {
                morseText.append(morseMap.get(letter)).append(" ");
            }
        }
        return morseText.toString();
    }
}
