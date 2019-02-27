package uk.ac.imperial.pipe.runner;

import java.util.ArrayList;
import java.util.List;

public class PlaceReporter {

    private PetriNetRunner runner;
    private List<String> reports;
    private boolean markedPlaces;
    private int reportLimit;
    private boolean limited;
    private boolean generatePlaceReports;

    public PlaceReporter(PetriNetRunner runner, boolean generatePlaceReports) {
        this.runner = runner;
        reports = new ArrayList<>();
        markedPlaces = false;
        limited = false;
        this.generatePlaceReports = generatePlaceReports;
    }

    public PlaceReporter(PetriNetRunner runner) {
        this(runner, false);
    }

    public void buildPlaceReport() {
        if ((limited) && (reports.size() == reportLimit)) {
            // at the limit, so don't add more
        } else {
            if (generatePlaceReports) {
                reports.add(runner.executablePetriNet.getPlaceReport(markedPlaces));
            }
        }
    }

    public String getPlaceReport() {
        return getPlaceReport(reports.size() - 1);
    }

    public String getPlaceReport(int index) {
        if (index < 0) {
            throw new ArrayIndexOutOfBoundsException(
                    "Requested place report (" + index + ") is invalid or no reports have been created.");
        } else if (index > reports.size() - 1) {
            throw new ArrayIndexOutOfBoundsException("Requested place report (" + index + ") has not been created.");
        }
        return reports.get(index);
    }

    public void setMarkedPlaces(boolean markedPlaces) {
        this.markedPlaces = markedPlaces;
    }

    protected List<String> getPlaceReports() {
        return reports;
    }

    public void setReportLimit(int reportLimit) {
        this.reportLimit = reportLimit;
        if (reportLimit > 0) {
            limited = true;
        } else {
            limited = false;
        }

    }

    protected boolean getMarkedPlaces() {
        return markedPlaces;
    }

    protected boolean getGeneratePlaceReports() {
        return generatePlaceReports;
    }

    protected boolean isLimited() {
        return limited;
    }

    public void setGeneratePlaceReports(boolean generatePlaceReports) {
        this.generatePlaceReports = generatePlaceReports;
    }

    public int size() {
        return reports.size();
    }

}
