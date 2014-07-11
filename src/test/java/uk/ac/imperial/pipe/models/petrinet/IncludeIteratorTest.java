package uk.ac.imperial.pipe.models.petrinet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import uk.ac.imperial.pipe.dsl.ANormalArc;
import uk.ac.imperial.pipe.dsl.APetriNet;
import uk.ac.imperial.pipe.dsl.APlace;
import uk.ac.imperial.pipe.dsl.AToken;
import uk.ac.imperial.pipe.dsl.AnImmediateTransition;
import uk.ac.imperial.pipe.models.petrinet.name.NormalPetriNetName;
@RunWith(MockitoJUnitRunner.class)
public class IncludeIteratorTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private IncludeHierarchy includes;
	private PetriNet net1;
	private PetriNet net2; 
	private PetriNet net3;
	private PetriNet net4;
	private PetriNet net5;
	private PetriNet net6;

	private IncludeIterator iterator; 

	@Before
	public void setUp() throws Exception {
		net1 = createSimpleNet(1);
		net2 = createSimpleNet(2);
		net3 = createSimpleNet(3);
		net4 = createSimpleNet(4);
		net5 = createSimpleNet(5);
		net6 = createSimpleNet(6);
		includes = new IncludeHierarchy(net1, "top"); 
	}
	public PetriNet createSimpleNet(int i) {
		PetriNet net = 
				APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0")).and(
				APlace.withId("P1")).and(AnImmediateTransition.withId("T0")).and(
				AnImmediateTransition.withId("T1")).and(
				ANormalArc.withSource("T1").andTarget("P1")).andFinally(
				ANormalArc.withSource("T0").andTarget("P0").with("#(P0)", "Default").token());
		net.setName(new NormalPetriNetName("net"+i));
		return net; 
	}
	@Test
	public void currentOnlyWorksAfterNextForFirstRequest() throws Exception {
		expectedException.expect(NoSuchElementException.class);
		includes = new IncludeHierarchy(net1, "top"); 
		iterator = includes.iterator(); 
		assertTrue(iterator.hasNext()); 
		assertEquals("net1", iterator.next().getPetriNet().getName().getName()); 
		assertEquals("net1", iterator.current().getPetriNet().getName().getName()); 
		assertFalse(iterator.hasNext()); 
		iterator = includes.iterator(); 
		assertTrue(iterator.hasNext()); 
		assertNull("current not set til we do next", iterator.current()); 
		assertEquals("net1", iterator.next().getPetriNet().getName().getName()); 
		assertEquals("net1", iterator.current().getPetriNet().getName().getName()); 
		assertFalse(iterator.hasNext()); 
		iterator.next();  // throws 
		iterator.current(); 
	}
	@Test
	public void buildsListOfIncludesThroughWhichToIterate() throws Exception {
		includes.include(net2, "two"); 
		includes.include(net3, "three").include(net4, "four").include(net5, "five");
		includes.getInclude("three").getInclude("four").include(net6, "six");
		// one ("top")
		//    two
		//    three
		//       four
		//          five
		//          six 
		iterator = includes.iterator(); 
		assertEquals(6, iterator.getIncludes().size()); 
		Iterator<IncludeHierarchy> it = iterator.getIncludes().iterator(); 
		assertEquals("net1",it.next().getPetriNet().getName().getName()); 
		assertEquals("net2",it.next().getPetriNet().getName().getName()); 
		assertEquals("net3",it.next().getPetriNet().getName().getName()); 
		assertEquals("net4",it.next().getPetriNet().getName().getName()); 
		assertEquals("net5",it.next().getPetriNet().getName().getName()); 
		assertEquals("net6",it.next().getPetriNet().getName().getName()); 
		// another flavor
		includes = new IncludeHierarchy(net1, "top"); 
		includes.include(net2, "two").include(net3, "three").include(net4, "four");; 
		includes.getInclude("two").include(net5, "five"); 
		includes.include(net6, "six"); 
		// one ("top")
		//    two
		//       five
		//       three
		//           four
		//    six 
		iterator = includes.iterator(); 
		assertEquals(6, iterator.getIncludes().size()); 
		it = iterator.getIncludes().iterator(); 
		assertEquals("net1",it.next().getPetriNet().getName().getName()); 
		assertEquals("net2",it.next().getPetriNet().getName().getName()); 
		assertEquals("net5",it.next().getPetriNet().getName().getName()); 
		assertEquals("net3",it.next().getPetriNet().getName().getName()); 
		assertEquals("net4",it.next().getPetriNet().getName().getName()); 
		assertEquals("net6",it.next().getPetriNet().getName().getName()); 
	}
    @Test
	public void iteratesThroughHierarchyTrackingCurrentLevel() throws Exception {
        expectedException.expect(NoSuchElementException.class);
    	includes.include(net2, "two"); 
    	includes.include(net3, "three").include(net4, "four").include(net5, "five");
    	includes.getInclude("three").getInclude("four").include(net6, "six");
    	iterator = includes.iterator(); 
    	assertTrue(iterator.hasNext()); 
    	assertEquals("net1", iterator.next().getPetriNet().getName().getName()); 
    	assertEquals("net1", iterator.current().getPetriNet().getName().getName()); 
    	assertTrue(iterator.hasNext()); 
    	assertEquals("net2", iterator.next().getPetriNet().getName().getName()); 
    	assertEquals("net2", iterator.current().getPetriNet().getName().getName()); 
    	assertTrue(iterator.hasNext()); 
    	assertEquals("net3", iterator.next().getPetriNet().getName().getName()); 
    	assertEquals("net3", iterator.current().getPetriNet().getName().getName()); 
    	assertTrue(iterator.hasNext()); 
    	assertEquals("net4", iterator.next().getPetriNet().getName().getName()); 
    	assertEquals("net4", iterator.current().getPetriNet().getName().getName()); 
    	assertTrue(iterator.hasNext()); 
    	assertEquals("net5", iterator.next().getPetriNet().getName().getName()); 
    	assertEquals("net5", iterator.current().getPetriNet().getName().getName()); 
    	assertTrue(iterator.hasNext()); 
    	assertEquals("net6", iterator.next().getPetriNet().getName().getName()); 
    	assertEquals("net6", iterator.current().getPetriNet().getName().getName()); 
    	assertFalse(iterator.hasNext()); 
    	iterator.next();  // throws 
    	iterator.current(); 
	}
    @Test
    public void returnsOnceForSingleNet() throws Exception {
    	iterator = includes.iterator(); 
    	assertTrue(iterator.hasNext()); 
    	assertEquals("net1", iterator.next().getPetriNet().getName().getName()); 
    	assertEquals("net1", iterator.current().getPetriNet().getName().getName()); 
    	assertFalse(iterator.hasNext()); 
    }
}
