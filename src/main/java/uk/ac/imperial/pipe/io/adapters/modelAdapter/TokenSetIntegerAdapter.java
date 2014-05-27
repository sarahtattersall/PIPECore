package uk.ac.imperial.pipe.io.adapters.modelAdapter;

import com.google.common.base.Joiner;
import uk.ac.imperial.pipe.models.petrinet.Token;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.HashMap;
import java.util.Map;

public class TokenSetIntegerAdapter
        extends XmlAdapter<TokenSetIntegerAdapter.AdaptedIntegerTokenSet, Map<Token, Integer>> {
    private final Map<String, Token> tokens;

    /**
     * Empty constructor needed formarshallingg. Since the method tomarshalll does not actually
     * use these fields it's ok to initialise them as empty/null.
     */
    public TokenSetIntegerAdapter() {
        tokens = new HashMap<>();
    }

    public TokenSetIntegerAdapter(Map<String, Token> tokens) {

        this.tokens = tokens;
    }

    @Override
    public Map<Token, Integer> unmarshal(AdaptedIntegerTokenSet adaptedTokenSet)  {
        Map<Token, Integer> tokenWeights = new HashMap<>();
        String weightInput = adaptedTokenSet.value;
        String[] commaSeparatedMarkings = weightInput.split(",");
        if (commaSeparatedMarkings.length == 1) {
            Token token = getDefaultToken();
            Integer weight = Integer.valueOf(commaSeparatedMarkings[0]);
            tokenWeights.put(token, weight);
        } else {
            for (int i = 0; i < commaSeparatedMarkings.length; i += 2) {
                Integer weight = Integer.valueOf(commaSeparatedMarkings[i + 1].replace("@", ","));
                String tokenName = commaSeparatedMarkings[i];
                Token token = getTokenIfExists(tokenName);
                tokenWeights.put(token, weight);
            }
        }
        return tokenWeights;
    }

    @Override
    public AdaptedIntegerTokenSet marshal(Map<Token, Integer> tokenIntegerMap) {
        AdaptedIntegerTokenSet adapted = new AdaptedIntegerTokenSet();
        adapted.value = Joiner.on(",").withKeyValueSeparator(",").join(tokenIntegerMap);
        return adapted;
    }

    /**
     * @param tokenName token to find in {@link this.tokens}
     * @return token if exists
     * @throws RuntimeException if token does not exist
     */
    private Token getTokenIfExists(String tokenName) {
        if (!tokens.containsKey(tokenName)) {
            throw new RuntimeException("No " + tokenName + " token exists!");
        }
        return tokens.get(tokenName);
    }

    /**
     * @return the default token to use if no token is specified in the
     * Arc weight XML.
     */
    private Token getDefaultToken() {
        return getTokenIfExists("Default");
    }

    public static class AdaptedIntegerTokenSet {
        public String value;
    }
}