package uk.ac.imperial.pipe.io;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class XMLUtils {

    private XMLUtils() {}

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


    public static String getTransitionFile() {
        return "/xml/transition/singleTransition.xml";
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

    public static String readFile(String path, Charset encoding)
            throws IOException, URISyntaxException
    {
    	byte[] encoded = Files.readAllBytes(Paths.get(XMLUtils.class.getResource(path).toURI()));
        return encoding.decode(ByteBuffer.wrap(encoded)).toString();
    }

}
