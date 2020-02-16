package uk.ac.imperial.pipe.runner;

import static org.junit.Assert.assertEquals;

import java.awt.Color;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import uk.ac.imperial.pipe.dsl.ANormalArc;
import uk.ac.imperial.pipe.dsl.APetriNet;
import uk.ac.imperial.pipe.dsl.APlace;
import uk.ac.imperial.pipe.dsl.AToken;
import uk.ac.imperial.pipe.dsl.AnImmediateTransition;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;

public class PlaceReporterTest {

    private PlaceReporter pr;
    private PetriNetRunner runner;

    @Before
    public void setUp() throws Exception {
        runner = new PetriNetRunner(buildHaltingNet());
        runner.tryAfterNoEnabledTransitions = false;
        runner.setPlaceReporterParameters(true, true, 0);
        pr = runner.getPlaceReporter();
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void createsReportReflectingCurrentMarking() throws Exception {
        pr.setMarkedPlaces(false);
        pr.buildPlaceReport();
        assertEquals("P0: Default=1  \nP1: Default=0  \nP2: Default=0  \n", pr.getPlaceReport());
    }

    @Test
    public void wontCreateReportIfGeneratePlaceReportsIsFalse() throws Exception {
        pr.setGeneratePlaceReports(false);
        pr.buildPlaceReport();
        assertEquals(0, pr.getPlaceReports().size());
    }

    @Test
    public void lastReportRetrievedImplicitlyOrExplicitly() throws Exception {
        pr.setMarkedPlaces(false);
        pr.buildPlaceReport();
        assertEquals("P0: Default=1  \nP1: Default=0  \nP2: Default=0  \n", pr.getPlaceReport());
        assertEquals("P0: Default=1  \nP1: Default=0  \nP2: Default=0  \n", pr.getPlaceReport(0));
    }

    @Test
    public void settingControlsWhetherReturnsAllOrOnlyMarkedPlaces() throws Exception {
        pr.setMarkedPlaces(true);
        pr.buildPlaceReport();
        assertEquals("only marked places", "P0: Default=1  \n", pr.getPlaceReport());

        setUp();
        pr.setMarkedPlaces(false);
        pr.buildPlaceReport();
        assertEquals("all places", "P0: Default=1  \nP1: Default=0  \nP2: Default=0  \n", pr.getPlaceReport());
    }

    @Test
    public void eachFiringCreatesAnAdditionalReport() throws Exception {
        runner.fireOneTransition();
        assertEquals(1, runner.getPlaceReporter().getPlaceReports().size());
        assertEquals(1, runner.placeReportsSize());
        runner.markPlace("P2", "Default", 1);
        runner.fireOneTransition();
        assertEquals(2, runner.getPlaceReporter().getPlaceReports().size());
        assertEquals(2, runner.placeReportsSize());
        runner.fireOneTransition();
        assertEquals(3, runner.getPlaceReporter().getPlaceReports().size());
        assertEquals(3, runner.placeReportsSize());
    }

    @Test
    public void reportCreatedAfterPendingPlacesMarkedBeforeFiringHappens() throws Exception {
        pr.setMarkedPlaces(false);
        runner.fireOneTransition();
        assertEquals("P0: Default=1  \nP1: Default=0  \nP2: Default=0  \n", runner.getPlaceReporter().getPlaceReports()
                .get(0));
        assertEquals("public interface", "P0: Default=1  \nP1: Default=0  \nP2: Default=0  \n", runner
                .getPlaceReport(0));
        runner.markPlace("P2", "Default", 1);
        runner.fireOneTransition();
        assertEquals("P0: Default=0  \nP1: Default=1  \nP2: Default=1  \n", runner.getPlaceReporter().getPlaceReports()
                .get(1));
        assertEquals("P0: Default=0  \nP1: Default=1  \nP2: Default=1  \n", runner.getPlaceReport(1));
        runner.fireOneTransition();
        assertEquals("P0: Default=1  \nP1: Default=0  \nP2: Default=0  \n", runner.getPlaceReporter().getPlaceReports()
                .get(2));
        assertEquals("P0: Default=1  \nP1: Default=0  \nP2: Default=0  \n", runner.getPlaceReport(2));
        runner.fireOneTransition();
    }

    @Test
    public void requestForPlaceReportRetrievesMostRecentReport() throws Exception {
        pr.setMarkedPlaces(false);
        pr = runner.getPlaceReporter();
        runner.fireOneTransition();
        assertEquals("P0: Default=1  \nP1: Default=0  \nP2: Default=0  \n", pr.getPlaceReport());
        assertEquals("same as explicit request", "P0: Default=1  \nP1: Default=0  \nP2: Default=0  \n", runner
                .getPlaceReport(0));
        runner.markPlace("P2", "Default", 1);
        runner.fireOneTransition();
        assertEquals("P0: Default=0  \nP1: Default=1  \nP2: Default=1  \n", pr.getPlaceReport());
        assertEquals("P0: Default=0  \nP1: Default=1  \nP2: Default=1  \n", runner.getPlaceReport(1));
        runner.fireOneTransition();
        assertEquals("P0: Default=1  \nP1: Default=0  \nP2: Default=0  \n", pr.getPlaceReport());
        assertEquals("P0: Default=1  \nP1: Default=0  \nP2: Default=0  \n", runner.getPlaceReport(2));
        runner.fireOneTransition();
    }

    @Test
    public void requestOutOfIndexBoundsReturnsError() throws Exception {
        expectedException.expect(ArrayIndexOutOfBoundsException.class);
        pr.getPlaceReport();
    }

    @Test
    public void requestReturnsMessageIfNoReportsYetCreated() throws Exception {
        expectedException.expectMessage("Requested place report (-1) is invalid or no reports have been created.");
        pr.getPlaceReport();
    }

    @Test
    public void requestOutOfIndexBoundsReturnsMessage() throws Exception {
        expectedException.expectMessage("Requested place report (1) has not been created.");
        pr.buildPlaceReport(); // 0th report
        pr.getPlaceReport(1);
    }

    @Test
    public void reportsWontExceedReportLimit() throws Exception {
        pr.setReportLimit(2);
        pr.buildPlaceReport();
        assertEquals(1, pr.getPlaceReports().size());
        pr.buildPlaceReport();
        assertEquals(2, pr.getPlaceReports().size());
        pr.buildPlaceReport();
        assertEquals("no additional reports added", 2, pr.getPlaceReports().size());
    }

    @Test
    public void lastReportCreatedAtTheLimitStaysAsLastReport() throws Exception {
        pr.setMarkedPlaces(false);
        pr = runner.getPlaceReporter();
        pr.setReportLimit(2);
        runner.fireOneTransition();
        runner.markPlace("P2", "Default", 1);
        runner.fireOneTransition();
        assertEquals("P0: Default=0  \nP1: Default=1  \nP2: Default=1  \n", pr.getPlaceReport());
        runner.fireOneTransition();
        assertEquals("unchanged", "P0: Default=0  \nP1: Default=1  \nP2: Default=1  \n", pr.getPlaceReport());
    }

    @Test
    public void runnerSupportsLastReportInterface() throws Exception {
        pr.setMarkedPlaces(false);
        runner.fireOneTransition();
        assertEquals("P0: Default=1  \nP1: Default=0  \nP2: Default=0  \n", runner.getPlaceReport(0));
        assertEquals("same as explicit request", "P0: Default=1  \nP1: Default=0  \nP2: Default=0  \n", runner
                .getPlaceReport());
    }

    // P0 -> T0 -> P1 -> T1 -> P0
    //                  /
    //                P2
    private PetriNet buildHaltingNet() throws PetriNetComponentException {
        PetriNet net = APetriNet.with(AToken.called("Default").withColor(Color.BLACK))
                .and(APlace.withId("P0").containing(1, "Default").token())
                .and(APlace.withId("P1").externallyAccessible()).and(APlace.withId("P2").externallyAccessible())
                .and(AnImmediateTransition.withId("T0")).and(AnImmediateTransition.withId("T1"))
                .and(ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token())
                .and(ANormalArc.withSource("T0").andTarget("P1").with("1", "Default").token())
                .and(ANormalArc.withSource("P2").andTarget("T1").with("1", "Default").token())
                .and(ANormalArc.withSource("P1").andTarget("T1").with("1", "Default").token())
                .andFinally(ANormalArc.withSource("T1").andTarget("P0").with("1", "Default").token());
        return net;
    }

}
