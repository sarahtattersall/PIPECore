package uk.ac.imperial.pipe.models.petrinet;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import uk.ac.imperial.pipe.dsl.ANormalArc;
import uk.ac.imperial.pipe.dsl.APetriNet;
import uk.ac.imperial.pipe.dsl.APlace;
import uk.ac.imperial.pipe.dsl.AToken;
import uk.ac.imperial.pipe.dsl.AnImmediateTransition;
import uk.ac.imperial.pipe.models.petrinet.name.NormalPetriNetName;

@RunWith(MockitoJUnitRunner.class)
public class RenameIncludeAliasCommandTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private PropertyChangeListener mockListener;

	private IncludeHierarchy includes;
	private PetriNet net1;
	private PetriNet net2; 
	private PetriNet net3;
	@SuppressWarnings("unused")
	private PetriNet net4, net5, net6 ;

	//FIXME move fqn test elsewhere and delete
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
    public void returnsMesssageIfRenameWouldCauseDuplicateAtParentLevel() throws Exception {
    	includes.include(net2, "child");
    	includes.include(net2, "second-child");
    	Result<String> result = renameInPieces(includes.getChildInclude("child"), "second-child", "child"); 
    	assertEquals("IncludeHierarchy attempted rename at level top would cause duplicate: child", result.getEntry().message);
    	assertNotNull(includes.getChildInclude("second-child")); 
    }
	@Test
	public void verifyRenameOfHigherLevelCascadedIntoAllFullyQualifiedNames() throws Exception {
		includes.include(net2, "a").include(net3, "b");
		includes.getInclude("a").include(net3, "bb");
		Result<String> result = renameInPieces(includes, "top", "newtop"); 
		assertFalse(result.hasResult());
//		includes.rename("newtop"); 
		assertEquals("newtop",includes.getName());
		assertEquals("newtop",includes.getFullyQualifiedName());
		assertEquals("newtop.a",includes.getChildInclude("a").getFullyQualifiedName());
		assertEquals("newtop.a.b",includes.getChildInclude("a").getChildInclude("b").getFullyQualifiedName());
		IncludeHierarchy childInclude = includes.getChildInclude("a");
		result = renameInPieces(childInclude, "a", "x");
		assertFalse(result.hasResult());
		assertEquals("x",includes.getChildInclude("x").getName());
		assertEquals("newtop.x",includes.getChildInclude("x").getFullyQualifiedName());
		assertEquals("newtop.x.b",includes.getChildInclude("x").getChildInclude("b").getFullyQualifiedName());
		assertEquals("newtop.x.bb",includes.getChildInclude("x").getChildInclude("bb").getFullyQualifiedName());
		IncludeHierarchy grandchildInclude = childInclude.getChildInclude("bb");
		result = renameInPieces(grandchildInclude, "bb", "c");
		assertFalse(result.hasResult());
		assertEquals("c",includes.getChildInclude("x").getChildInclude("c").getName());
		assertEquals("newtop.x.c",includes.getChildInclude("x").getChildInclude("c").getFullyQualifiedName());
		assertEquals(1, includes.getIncludeMap().size());
		assertEquals(2, includes.getChildInclude("x").getIncludeMap().size());
		assertEquals(0, includes.getChildInclude("x").getChildInclude("b").getIncludeMap().size());
		assertEquals(0, includes.getChildInclude("x").getChildInclude("c").getIncludeMap().size());
//		assertEquals(4, includes.getIncludeMapAll().size());
//		assertEquals(3, includes.getChildInclude("x").getIncludeMapAll().size());
//		Set<String> keys = includes.getChildInclude("x").getIncludeMapAll().keySet(); 
//		Iterator<String> it = keys.iterator(); 
//		assertEquals("b", it.next()); 
//		assertEquals("c", it.next()); 
//		assertEquals("x", it.next()); 
		
//		fail("verify each of the maps is as expected; consider building fqn as a command"); 
	}
	protected static Result<String> renameInPieces(IncludeHierarchy include, String oldname, String newname) {
		IncludeHierarchyCommand<String> command = new RenameIncludeAliasCommand<String>(oldname, newname);  
		include.setName(newname); 
		include.parent(command); 
		include.self(command);
		include.children(command);
		return command.getResult(); 
	}


}
