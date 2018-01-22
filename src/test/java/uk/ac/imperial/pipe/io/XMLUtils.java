package uk.ac.imperial.pipe.io;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class XMLUtils {

    private XMLUtils() {
    }

    public static String getAnnotationFile() {
        return "/xml/annotation/annotation.xml";
    }

    public static String getTokenFile() {
        return "/xml/token/token.xml";
    }

    public static String getArcNoWeightFile() {
        return "/xml/arc/arcNoWeight.xml";
    }

    public static String getArcWeightNoTokenFile() {
        return "/xml/arc/arcWeightNoToken.xml";
    }

    public static String getArcWithSourceAndTargetFile() {
        return "/xml/arc/arcWithSourceAndTarget.xml";
    }

    public static String getInhibitorArcFile() {
        return "/xml/arc/inhibitorArc.xml";
    }

    public static String getNormalArcWithWeight() {
        return "/xml/arc/normalArcWithWeight.xml";
    }

    public static String getArcWithoutPlaceFile() {
        return "/xml/arc/arcWithoutPlace.xml";
    }

    public static String getTransitionFile() {
        return "/xml/transition/singleTransition.xml";
    }

    public static String getExternalTransitionRateParameterFile() {
        return "/xml/transition/externalTransitionRateParameter.xml";
    }

    public static String getRateParameterFile() {
        return "/xml/rateParameter/rateParameter.xml";
    }

    public static String getTransitionRateParameterFile() {
        return "/xml/rateParameter/transitionRateParameter.xml";
    }

    public static String getSinglePlacePath() {
        return "/xml/place/singlePlace.xml";
    }

    public static String getInvalidPetriNetFile() {
        return "/xml/invalidPetriNet.xml";
    }

    public static String getTwoTokenFile() {
        return "/xml/token/two_token.xml";
    }

    public static String getRateParameterReferencesPlaceFile() {
        return "/xml/rateParameter/rateReferencesPlace.xml";
    }

    public static String getNoPlaceTokenPath() {
        return "/xml/place/noTokenPlace.xml";
    }

    public static String getSinglePlaceWithHomeInterfaceStatusPath() {
        return "/xml/place/singlePlaceHomeInterfaceStatus.xml";
    }

    public static String getSinglePlaceWithAvailableInterfaceStatusPath() {
        return "/xml/place/singlePlaceAvailableInterfaceStatus.xml";
    }

    public static String getTwoPlacesOneWithAwayInterfaceStatusPath() {
        return "/xml/place/twoPlacesOneWithAwayInterfaceStatus.xml";
    }

    public static String getExternalTransitionFile() {
        return "/xml/transition/singleExternalTransition.xml";
    }

    public static String getSingleIncludeHierarchyFile() {
        return "/xml/include/singleInclude.xml";
    }

    public static String getSingleIncludeHierarchyFileReadyToFire() {
        return "/xml/include/singleIncludeReadyToFire.xml";
    }

    public static String getMultipleIncludeHierarchyFile() {
        return "/xml/include/multipleIncludes.xml";
    }

    public static String getMultipleIncludeHierarchyFileWithRelativeLocations() {
        return "/multipleIncludesRelativeLocations.xml";
    }

    public static String getMultipleIncludeHierarchyWithInterfaceStatusFile() {
        return "/xml/include/multipleIncludesWithInterfaceStatus.xml";
    }

    public static String getIncludeWithInvalidPetriNet() {
        return "/xml/include/includeWithInvalidPetriNet.xml";
    }

    public static String getTwoNetsOneInterfaceStatus() {
        return "/xml/include/twoNetsOneInterfaceStatus.xml";
    }

    public static String getSimplePetriNet() {
        return "/xml/simpleNet.xml";
    }

    public static String getPetriNet() {
        return "/xml/petriNet.xml";
    }

    public static String getGeneralizedStochasticPetriNet() {
        return "/xml/gspn1.xml";
    }

    public static String getUnknownXml() {
        return "/xml/unknownXml.xml";
    }

    public static String getInvalidXml() {
        return "/xml/invalidXml.xml";
    }

    public static String getNonExistentFile() {
        return "/xml/nonExistentFile.xml";
    }

    public static String readFile(String path, Charset encoding)
            throws IOException, URISyntaxException {
        byte[] encoded = Files.readAllBytes(Paths.get(XMLUtils.class.getResource(path).toURI()));
        return encoding.decode(ByteBuffer.wrap(encoded)).toString();
    }

}
