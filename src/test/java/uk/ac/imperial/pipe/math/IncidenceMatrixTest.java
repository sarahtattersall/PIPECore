package uk.ac.imperial.pipe.math;

import org.junit.Before;
import org.junit.Test;
import uk.ac.imperial.pipe.models.petrinet.DiscretePlace;
import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.DiscreteTransition;
import uk.ac.imperial.pipe.models.petrinet.Transition;
import uk.ac.imperial.pipe.models.petrinet.IncidenceMatrix;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class IncidenceMatrixTest {

    private IncidenceMatrix incidenceMatrix;

    @Before
    public void setUp() {
        incidenceMatrix = new IncidenceMatrix();
    }

    @Test
    public void voidEntryReturnsZero() {
        Place place = mock(DiscretePlace.class);
        Transition transition = mock(DiscreteTransition.class);
        int value = incidenceMatrix.get(place, transition);
        assertEquals("Does not behave like matrix for tokens not put in matrix", 0, value);
    }


    @Test
    public void putEntryReturnsValue() {
        Place place = mock(DiscretePlace.class);
        Transition transition = mock(DiscreteTransition.class);
        int expected = 4;
        incidenceMatrix.put(place, transition, expected);
        int actual = incidenceMatrix.get(place, transition);
        assertEquals("Does not behave like matrix for tokens not put in matrix", expected, actual);
    }

    @Test
    public void putEntryOverwritesValue() {
        Place place = mock(DiscretePlace.class);
        Transition transition = mock(DiscreteTransition.class);
        int firstValue = 4;
        incidenceMatrix.put(place, transition, firstValue);
        int secondValue = 5;
        incidenceMatrix.put(place, transition, secondValue);
        int actual = incidenceMatrix.get(place, transition);
        assertEquals("Did not override tokens correctly", secondValue, actual);
    }

}
