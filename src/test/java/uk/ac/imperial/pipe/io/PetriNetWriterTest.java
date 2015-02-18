package uk.ac.imperial.pipe.io;

import java.awt.Color;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.xml.sax.SAXException;

import uk.ac.imperial.pipe.dsl.ANormalArc;
import uk.ac.imperial.pipe.dsl.APetriNet;
import uk.ac.imperial.pipe.dsl.APlace;
import uk.ac.imperial.pipe.dsl.AToken;
import uk.ac.imperial.pipe.dsl.AnImmediateTransition;
import uk.ac.imperial.pipe.exceptions.InvalidRateException;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.models.petrinet.AnnotationImpl;
import uk.ac.imperial.pipe.models.petrinet.ColoredToken;
import uk.ac.imperial.pipe.models.petrinet.DiscreteExternalTransition;
import uk.ac.imperial.pipe.models.petrinet.DiscretePlace;
import uk.ac.imperial.pipe.models.petrinet.DiscreteTransition;
import uk.ac.imperial.pipe.models.petrinet.FunctionalRateParameter;
import uk.ac.imperial.pipe.models.petrinet.IncludeHierarchy;
import uk.ac.imperial.pipe.models.petrinet.MergeInterfaceStatusAvailable;
import uk.ac.imperial.pipe.models.petrinet.MergeInterfaceStatusAway;
import uk.ac.imperial.pipe.models.petrinet.MergeInterfaceStatusHome;
import uk.ac.imperial.pipe.models.petrinet.NormalRate;
import uk.ac.imperial.pipe.models.petrinet.OutboundArc;
import uk.ac.imperial.pipe.models.petrinet.OutboundNormalArc;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;
import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.PlaceStatus;
import uk.ac.imperial.pipe.models.petrinet.PlaceStatusInterface;
import uk.ac.imperial.pipe.models.petrinet.Token;
import uk.ac.imperial.pipe.models.petrinet.Transition;
import uk.ac.imperial.pipe.models.petrinet.name.NormalPetriNetName;
import uk.ac.imperial.pipe.runner.PetriNetRunner;

public class PetriNetWriterTest extends XMLTestCase {
    PetriNetWriter writer;
    /**
     * main to make it convenient to persist test Petri nets
     * @param args
     * @throws Exception
     */
    @SuppressWarnings("unused")
	public static void main(String[] args) throws Exception {
    	PetriNet net = buildNet(); 
//	    writeSingleNet("src/test/resources/xml/someTestNet.xml", net); 
	    // or...
//	    writeIncludeHierarchy(); 
	}
    protected static void writeSingleNet(String where, PetriNet net) throws JAXBException {
    	PetriNetIO netIO = new PetriNetIOImpl(); 
    	netIO.writeTo(where, net);
    }
	@SuppressWarnings("unused")
	private static void writeIncludeHierarchy() throws Exception {
		PetriNet net = buildNet();
		net.setName(new NormalPetriNetName("net")); 
		PetriNet net2 = buildNet(); 
		IncludeHierarchy includes = new IncludeHierarchy(net, "top");
		includes.include(net2, "a");  
		net.setIncludeHierarchy(includes);
		Place originP1 = net2.getComponent("P1", Place.class); 
		includes.getInclude("a").addToInterface(originP1, true, false, false, false ); 
		includes.addAvailablePlaceToPetriNet(includes.getInterfacePlace("a.P1")); 
		Place topIP1 = includes.getInterfacePlace("a.P1"); 
		Transition topT0 = net.getComponent("T0", Transition.class);
		Map<String,String> tokenweights = new HashMap<String, String>(); 
		tokenweights.put("Default", "1"); 
		OutboundArc arcOut = new OutboundNormalArc(topT0, topIP1, tokenweights);
		net.add(arcOut); 
		writeSingleNet("src/test/resources/xml/include/topNet.xml", net); 
		writeSingleNet("src/test/resources/xml/include/aNet.xml", net2); 
		includes.setPetriNetLocation("src/test/resources/xml/include/topNet.xml");
		includes.getInclude("a").setPetriNetLocation("src/test/resources/xml/include/aNet.xml");
		IncludeHierarchyIO includeIO = new IncludeHierarchyIOImpl(); 
		String includePath = "src/test/resources/xml/include/twoNetsOneInterfaceStatus.xml"; 
		includeIO.writeTo(includePath, new IncludeHierarchyBuilder(includes));
	}

    private static PetriNet buildNet() {
//    	return PetriNetRunner.getPetriNet("testInterfacePlaces");
    	PetriNet net = null; 
    	// build net using DSL ....
		return net;
	}
    
    @Override
    public void setUp() throws JAXBException {
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreComments(true);
        writer = new PetriNetIOImpl();
    }

    public void testMarshalsPlace() throws IOException, SAXException, JAXBException {
        PetriNet petriNet = new PetriNet();
        Token token = new ColoredToken("Red", new Color(255, 0, 0));
        Place place = new DiscretePlace("P0", "P0");
        place.setX(255);
        place.setY(240);
        place.setNameXOffset(5);
        place.setNameYOffset(26);
        place.setTokenCount(token.getId(), 1);
        petriNet.addToken(token);
        petriNet.addPlace(place);

        assertResultsEqual(FileUtils.fileLocation(XMLUtils.getSinglePlacePath()), petriNet);
    }
    public void testMarshalsPlaceWithHomeInterfaceStatus() throws IOException, SAXException, JAXBException {
    	PetriNet petriNet = new PetriNet(new NormalPetriNetName("netb"));
    	Token token = new ColoredToken("Red", new Color(255, 0, 0));
    	Place place = new DiscretePlace("P0", "P0");
    	place.setX(255);
    	place.setY(240);
    	place.setNameXOffset(5);
    	place.setNameYOffset(26);
    	place.setTokenCount(token.getId(), 1);
    	
    	PlaceStatus status = new PlaceStatusInterface(place);
    	status.setIncludeHierarchy(new IncludeHierarchy(petriNet, "a")); 
    	status.setMergeStatus(true);
    	status.setMergeInterfaceStatus(new MergeInterfaceStatusHome(place, status)); 
    	status.setExternal(true); 
    	status.setInputOnlyArcConstraint(true); 
    	place.setStatus(status); 
    	
    	
    	petriNet.addToken(token);
    	petriNet.addPlace(place);
    	
    	assertResultsEqual(FileUtils.fileLocation(XMLUtils.getSinglePlaceWithHomeInterfaceStatusPath()), petriNet);
    }
    public void testMarshalsPlaceWithAwayInterfaceStatusAndOrdinaryPlace() throws IOException, SAXException, JAXBException {
    	PetriNet petriNet = new PetriNet();
    	Token token = new ColoredToken("Red", new Color(255, 0, 0));
    	Place place = new DiscretePlace("P0", "P0");
    	place.setX(255);
    	place.setY(240);
    	place.setNameXOffset(5);
    	place.setNameYOffset(26);
    	place.setTokenCount(token.getId(), 1);
    	
    	Place placebP0 = new DiscretePlace("b.P0", "b.P0");
    	placebP0.setX(255);
    	placebP0.setY(240);
    	placebP0.setNameXOffset(5);
    	placebP0.setNameYOffset(26);
    	placebP0.setTokenCount(token.getId(), 1);
    	PlaceStatus status = new PlaceStatusInterface(placebP0);
    	status.setIncludeHierarchy(new IncludeHierarchy(petriNet, "a")); 
    	status.setMergeStatus(true);
    	status.setMergeInterfaceStatus(new MergeInterfaceStatusAway(place, status, "b.P0")); 
    	status.setExternal(true); 
    	status.setInputOnlyArcConstraint(true); 
    	placebP0.setStatus(status); 
    	
    	
    	petriNet.addToken(token);
    	petriNet.addPlace(place);
    	petriNet.addPlace(placebP0);
    	
    	assertResultsEqual(FileUtils.fileLocation(XMLUtils.getTwoPlacesOneWithAwayInterfaceStatusPath()), petriNet);
    }
    public void testMarshalsPlaceWithAvailableInterfaceStatusAndOrdinaryPlace() throws IOException, SAXException, JAXBException {
    	PetriNet petriNet = new PetriNet(new NormalPetriNetName("neta"));
    	Token token = new ColoredToken("Red", new Color(255, 0, 0));
    	Place place = new DiscretePlace("P0", "P0");
    	place.setX(255);
    	place.setY(240);
    	place.setNameXOffset(5);
    	place.setNameYOffset(26);
    	place.setTokenCount(token.getId(), 1);
    	
    	Place placebP0 = new DiscretePlace("b.P0", "b.P0");
    	placebP0.setX(255);
    	placebP0.setY(240);
    	placebP0.setNameXOffset(5);
    	placebP0.setNameYOffset(26);
    	placebP0.setTokenCount(token.getId(), 1);
    	PlaceStatus status = new PlaceStatusInterface(placebP0);
    	status.setIncludeHierarchy(new IncludeHierarchy(petriNet, "a")); 
    	status.setMergeStatus(true);
    	status.setMergeInterfaceStatus(new MergeInterfaceStatusAvailable(place, status, "b.P0")); 
    	status.setExternal(true); 
    	status.setInputOnlyArcConstraint(true); 
    	placebP0.setStatus(status); 
    	
    	
    	petriNet.addToken(token);
    	petriNet.addPlace(place);
//    	petriNet.addPlace(placebP0);
    	// An available place does not (yet) exist in the PN, so won't be persisted.  Only the ordinary place will be written
    	assertResultsEqual(FileUtils.fileLocation(XMLUtils.getSinglePlaceWithAvailableInterfaceStatusPath()), petriNet);
    }

    private void assertResultsEqual(String expectedPath, PetriNet petriNet)
            throws IOException, SAXException, JAXBException {
        StringWriter stringWriter = new StringWriter();
        writer.writeTo(stringWriter, petriNet);

        String expected = XMLUtils.readFile(expectedPath, Charset.defaultCharset());

        String actual = stringWriter.toString();
//        System.out.println(actual );
        assertXMLEqual(expected, actual);
    }

    public void testMarshalsTransition() throws IOException, SAXException, JAXBException {
        PetriNet petriNet = new PetriNet();
        Transition transition = new DiscreteTransition("T0", "T0");
        transition.setX(375);
        transition.setY(225);
        transition.setNameXOffset(-5.0);
        transition.setNameYOffset(35.0);
        transition.setRate(new NormalRate("1.0"));
        transition.setTimed(false);
        transition.setInfiniteServer(false);
        transition.setPriority(1);
        petriNet.addTransition(transition);
        assertResultsEqual(FileUtils.fileLocation(XMLUtils.getTransitionFile()), petriNet);
    }

    public void testMarshalsTransitionWithRateParameter()
            throws IOException, SAXException, InvalidRateException, JAXBException {
        PetriNet petriNet = new PetriNet();
        FunctionalRateParameter rateParameter = new FunctionalRateParameter("6.0", "foo", "foo");

        Transition transition = new DiscreteTransition("T0", "T0");
        transition.setX(435);
        transition.setY(180);
        transition.setNameXOffset(-5.0);
        transition.setNameYOffset(35.0);
        transition.setRate(rateParameter);
        transition.setTimed(true);
        transition.setInfiniteServer(false);
        transition.setPriority(1);

        petriNet.addTransition(transition);
        petriNet.addRateParameter(rateParameter);

        assertResultsEqual(FileUtils.fileLocation(XMLUtils.getTransitionRateParameterFile()), petriNet);
    }
    public void testMarshalsExternalTransitionWithRateParameter()
    		throws IOException, SAXException, InvalidRateException, JAXBException {
    	PetriNet petriNet = new PetriNet();
    	FunctionalRateParameter rateParameter = new FunctionalRateParameter("6.0", "foo", "foo");
    	
    	Transition transition = new DiscreteExternalTransition("T0", "T0","uk.ac.imperial.pipe.models.petrinet.TestingExternalTransition");
    	transition.setX(435);
    	transition.setY(180);
    	transition.setNameXOffset(-5.0);
    	transition.setNameYOffset(35.0);
    	transition.setRate(rateParameter);
    	transition.setTimed(true);
    	transition.setInfiniteServer(false);
    	transition.setPriority(1);
    	
    	petriNet.addTransition(transition);
    	petriNet.addRateParameter(rateParameter);
    	
    	assertResultsEqual(FileUtils.fileLocation(XMLUtils.getExternalTransitionRateParameterFile()), petriNet);
    }

    public void testMarshalsArc() throws IOException, SAXException, JAXBException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(
                APlace.withId("P0").locatedAt(0, 0)).and(AnImmediateTransition.withId("T0").locatedAt(0, 0)).andFinally(
                ANormalArc.withSource("P0").andTarget("T0").and("4", "Default").tokens());


        assertResultsEqual(FileUtils.fileLocation(XMLUtils.getNormalArcWithWeight()), petriNet);
    }


    public void testMarshalsToken() throws IOException, SAXException, PetriNetComponentException, JAXBException {
        PetriNet petriNet = new PetriNet();
        Token token = new ColoredToken("red", new Color(255, 0, 0));
        petriNet.add(token);
        assertResultsEqual(FileUtils.fileLocation(XMLUtils.getTokenFile()), petriNet);
    }

    public void testMarshalsAnnotation() throws IOException, SAXException, JAXBException {
        PetriNet petriNet = new PetriNet();
        AnnotationImpl annotation = new AnnotationImpl(93, 145, "#P12s", 48, 20, false);
        petriNet.addAnnotation(annotation);
        assertResultsEqual(FileUtils.fileLocation(XMLUtils.getAnnotationFile()), petriNet);
    }
}


