package uk.ac.imperial.pipe.io.adapters.modelAdapter;

import uk.ac.imperial.pipe.io.adapters.model.AdaptedToken;
import uk.ac.imperial.pipe.models.petrinet.ColoredToken;
import uk.ac.imperial.pipe.models.petrinet.Token;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

/**
 * Used to marshall tokens into and out of PNML format
 */
public final class TokenAdapter extends XmlAdapter<AdaptedToken, Token> {
    private final Map<String, Token> tokens;

    /**
     * Empty constructor needed for marshalling. Since the method to marshall does not actually
     * use these fields it's ok to initialise them as empty/null.
     */
    public TokenAdapter() {
        tokens = new HashMap<>();
    }

    /**
     * Constructor
     * @param tokens to marshal
     */
    public TokenAdapter(Map<String, Token> tokens) {

        this.tokens = tokens;
    }

    /**
     *
     * @param adaptedToken to unmarshal
     * @return unmarshaled token
     */
    @Override
    public Token unmarshal(AdaptedToken adaptedToken) {
        Color color = new Color(adaptedToken.getRed(), adaptedToken.getGreen(), adaptedToken.getBlue());
        Token token = new ColoredToken(adaptedToken.getId(), color);
        tokens.put(token.getId(), token);
        return token;
    }

    /**
     *
     * @param token to marshal
     * @return marshaled token
     */
    @Override
    public AdaptedToken marshal(Token token) {
        AdaptedToken adapted = new AdaptedToken();
        adapted.setId(token.getId());
        Color color = token.getColor();
        adapted.setRed(color.getRed());
        adapted.setGreen(color.getGreen());
        adapted.setBlue(color.getBlue());
        return adapted;
    }
}
