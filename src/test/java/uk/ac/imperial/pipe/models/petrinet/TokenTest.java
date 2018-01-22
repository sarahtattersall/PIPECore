package uk.ac.imperial.pipe.models.petrinet;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.ac.imperial.pipe.models.petrinet.ColoredToken;
import uk.ac.imperial.pipe.models.petrinet.Token;

import java.awt.Color;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class TokenTest {

    Token token;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() {
        token = new ColoredToken("Default", Color.BLACK);
    }

    /**
     * Test tokens are compared via their tokenName and color
     */
    @Test
    public void tokenEquality() {
        String tokenName = "Default";
        Color sameColor = new Color(0, 0, 0);
        Token token1 = new ColoredToken(tokenName, sameColor);
        Token token2 = new ColoredToken(tokenName, sameColor);

        assertEquals(token1, token2);
    }

    @Test
    public void tokenNameInequality() {
        String tokenName = "Default";
        Color sameColor = new Color(0, 0, 0);
        Token token1 = new ColoredToken(tokenName, sameColor);
        Token token2 = new ColoredToken(tokenName + "different", sameColor);

        assertThat(token1, is(not(equalTo((token2)))));
    }

    @Test
    public void tokenColorInequality() {
        String tokenName = "Default";
        Color color1 = new Color(0, 0, 0);
        Token token1 = new ColoredToken(tokenName, color1);
        Color color2 = new Color(255, 255, 1);
        Token token2 = new ColoredToken(tokenName, color2);

        assertThat(token1, is(not(equalTo((token2)))));
    }

}
