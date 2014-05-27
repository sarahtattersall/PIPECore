package uk.ac.imperial.pipe.io.adapters.utils;

import uk.ac.imperial.pipe.models.petrinet.ColoredToken;
import uk.ac.imperial.pipe.models.petrinet.Token;

import java.awt.Color;
import java.util.Map;

public final class TokenUtils {
    /**
     * Hidden constructor for utility class since this class
     * is not designed to be instantiated
     */
    private TokenUtils() {}

    public static Token getOrCreateDefaultToken(Map<String, Token> tokens) {
        if (tokens.containsKey("Default")) {
            return tokens.get("Default");
        } else {
            Token token = new ColoredToken("Default", new Color(0, 0, 0));
            tokens.put(token.getId(), token);
            return token;
        }
    }
}
