package uk.ac.imperial.pipe.runner;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.runner.StateReport.TokenFiringRecord;
import uk.ac.imperial.state.State;

import com.google.common.hash.HashCode;

@RunWith(MockitoJUnitRunner.class)
public class StateReportTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private TestingState state;
    private StateReport stateReport;

    @Before
    public void setUp() throws Exception {
        Map<String, Map<String, Integer>> map = buildStateMap();
        state = new TestingState(map);
        stateReport = new StateReport(state);
    }

    private Map<String, Map<String, Integer>> buildStateMap() {
        Map<String, Map<String, Integer>> map = new HashMap<String, Map<String, Integer>>();
        map.put("P3", buildMap("Default", 0, "red", 1));
        map.put("P0", buildMap("red", 2, "Default", 1));
        map.put("P1", buildMap("Default", 3, "red", 4));
        return map;
    }

    //     P0  P1  P3
    // Def  1   3  0
    // red  2   4  1
    private Map<String, Map<String, Integer>> buildInvalidMapDifferentTokenColors() {
        Map<String, Map<String, Integer>> map = new HashMap<String, Map<String, Integer>>();
        map.put("P3", buildMap("Default", 0, "red", 1));
        map.put("P0", buildMap("blue", 2, "Default", 1));
        map.put("P1", buildMap("Default", 3, "red", 4));
        return map;
    }

    private Map<String, Map<String, Integer>> buildInvalidMapDifferentNumberOfTokens() {
        Map<String, Map<String, Integer>> map = new HashMap<String, Map<String, Integer>>();
        map.put("P3", buildMap("Default", 0, "red", 1));
        Map<String, Integer> countMap = new HashMap<>();
        countMap.put("Default", 0);
        countMap.put("red", 0);
        countMap.put("blue", 0);
        map.put("P4", countMap);
        return map;
    }

    private Map<String, Integer> buildMap(String token, int i, String token2,
            int j) {
        Map<String, Integer> countMap = new HashMap<>();
        countMap.put(token, i);
        countMap.put(token2, j);
        return countMap;
    }

    @Test
    public void returnsRecordsWithColumnsSortedByPlaceId() {
        assertEquals("P0", stateReport.getPlaces().get(0));
        assertEquals("P1", stateReport.getPlaces().get(1));
        assertEquals("P3", stateReport.getPlaces().get(2));
    }

    @Test
    public void returnsRecordsSortedByPlaceIdThenToken() {
        checkRecord(stateReport.getTokenFiringRecords().get(0), "Default", 0, 1);
        checkRecord(stateReport.getTokenFiringRecords().get(1), "red", 0, 2);
        checkRecord(stateReport.getTokenFiringRecords().get(0), "Default", 1, 3);
        checkRecord(stateReport.getTokenFiringRecords().get(1), "red", 1, 4);
        checkRecord(stateReport.getTokenFiringRecords().get(0), "Default", 2, 0);
        checkRecord(stateReport.getTokenFiringRecords().get(1), "red", 2, 1);
    }

    @Test
    public void throwsIfTokenColorsAreNotSameForAllPlaces() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("StateReport:  null count found for token: blue");
        Map<String, Map<String, Integer>> map = buildInvalidMapDifferentTokenColors();
        state = new TestingState(map);
        stateReport = new StateReport(state);
    }

    @Test
    public void throwsIfNumberOfTokensNotSameForAllPlaces() {
        expectedException.expect(IllegalStateException.class);
        expectedException
                .expectMessage("StateReport:  expected all places to have the same number of tokens: 2 but found: 3");
        Map<String, Map<String, Integer>> map = buildInvalidMapDifferentNumberOfTokens();
        state = new TestingState(map);
        stateReport = new StateReport(state);
    }

    private void checkRecord(TokenFiringRecord record, String token, int placeCol, int count) {
        assertEquals(token, record.token);
        assertEquals(count, (int) record.getCounts().get(placeCol));
    }

    private class TestingState implements State {

        private Map<String, Map<String, Integer>> map;

        public TestingState(Map<String, Map<String, Integer>> map) {
            this.map = map;
        }

        @Override
        public Map<String, Integer> getTokens(String id) {
            return null;
        }

        @Override
        public boolean containsTokens(String id) {
            return false;
        }

        @Override
        public Collection<String> getPlaces() {
            return null;
        }

        @Override
        public int primaryHash() {
            return 0;
        }

        @Override
        public HashCode secondaryHash() {
            return null;
        }

        @Override
        public Map<String, Map<String, Integer>> asMap() {
            return map;
        }

    }
}
