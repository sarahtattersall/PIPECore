package uk.ac.imperial.pipe.models.petrinet;

import java.awt.Color;

public interface Token extends PetriNetComponent {
    /**
     * Message fired when the token is enabled/disabled
     */
    String TOKEN_ENABLED_CHANGE_MESSAGE = "enabled";
    /**
     * Message fired when the token color changes
     */
    String COLOR_CHANGE_MESSAGE = "color";

    Color getColor();

    void setColor(Color color);
}
