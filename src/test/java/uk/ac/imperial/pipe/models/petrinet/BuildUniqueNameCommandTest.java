package uk.ac.imperial.pipe.models.petrinet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.awt.Color;

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
public class BuildUniqueNameCommandTest extends AbstractMapEntryTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();


	private IncludeHierarchy includes;
	private PetriNet net1;
	private PetriNet net2; 
	private PetriNet net3;
	@SuppressWarnings("unused")
	private PetriNet net4, net5, net6 ;


	private IncludeHierarchyCommand<Object> uniqueNameCommand;


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
	public void uniqueNamesCreatedAtEachLevel() throws Exception {
    	assertEquals("top", includes.getUniqueName()); 
    	checkIncludeMapAllEntries("top level knows itself",includes, new ME("top", includes));

    	IncludeHierarchy aInclude = addBareInclude(includes, net2, "a"); 
    	checkIncludeMapAllEntries("top doesnt know 'a' yet",includes, new ME("top", includes));
    	checkIncludeMapAllEntries("'a' doesnt know itself yet",aInclude);
    	
    	uniqueNameCommand = new BuildUniqueNameCommand<Object>();  
    	Result<Object> result = aInclude.self(uniqueNameCommand); 
    	assertFalse(result.hasResult()); 
    	checkIncludeMapAllEntries(includes, new ME("top", includes), new ME("a", aInclude));
    	checkIncludeMapAllEntries(aInclude, new ME("a", aInclude));

    	IncludeHierarchy bInclude = addBareInclude(aInclude, net3, "b"); 
    	assertFalse(bInclude.self(new BuildUniqueNameCommand<Object>()).hasResult()); 
    	checkAllIncludesAllMapEntries(new ME("top", includes), new ME("a", aInclude), new ME("b", bInclude));
//    	checkIncludeMapAllEntries(includes, new ME("top", includes), new ME("a", aInclude), new ME("b", bInclude));
//    	checkIncludeMapAllEntries(aInclude, new ME("a", aInclude), new ME("b", bInclude));
//    	checkIncludeMapAllEntries(bInclude, new ME("b", bInclude));
	}
	@Test
    public void minimallyUniqueNameForSameNamedChildIsPrefixedWithNamesUpThroughSameNamedParent() throws Exception {
    	IncludeHierarchy aInclude = addIncludeAndBuildUniqueName(includes, net2, "a"); 
    	IncludeHierarchy bInclude = addIncludeAndBuildUniqueName(aInclude, net3, "b"); 
    	IncludeHierarchy abaInclude = addIncludeAndBuildUniqueName(bInclude, net4, "a"); 

    	checkAllIncludesAllMapEntries(new ME("top", includes), new ME("a", aInclude), new ME("b", bInclude), new ME("a.b.a", abaInclude));

    	assertEquals("parent keeps minimal name",includes.getChildInclude("a"), includes.getInclude("a")); 
    	assertEquals("childs name includes path to parent",includes.getChildInclude("a").getChildInclude("b").getChildInclude("a"), includes.getInclude("a.b.a")); 
    }
    @Test
    public void renamesAreReflectedInUniqueNames() throws Exception {
    	IncludeHierarchy aInclude = addIncludeAndBuildUniqueName(includes, net2, "a"); 
    	IncludeHierarchy bInclude = addIncludeAndBuildUniqueName(aInclude, net3, "b"); 
    	IncludeHierarchy cInclude = addIncludeAndBuildUniqueName(bInclude, net4, "c"); 
    	IncludeHierarchy dInclude = addIncludeAndBuildUniqueName(cInclude, net5, "d"); 
    	IncludeHierarchy abcdaInclude = addIncludeAndBuildUniqueName(dInclude, net6, "a"); 
    	
    	checkAllIncludesAllMapEntries(new ME("top", includes), new ME("a", aInclude), new ME("b", bInclude), new ME("c", cInclude), 
    			new ME("d", dInclude), new ME("a.b.c.d.a", abcdaInclude));
    	assertEquals(includes.getChildInclude("a").getChildInclude("b").getChildInclude("c")
    			.getChildInclude("d").getChildInclude("a"), includes.getInclude("a.b.c.d.a")); 
    	assertFalse(cInclude.renameBare("a").hasResult()); 
		checkAllIncludesMapEntries(new ME("top", includes), new ME("a", aInclude), new ME("b", bInclude), 
				new ME("a", cInclude), new ME("d", dInclude), new ME("a", abcdaInclude));    	
		
		uniqueNameCommand = new BuildUniqueNameCommand<Object>();  
		cInclude.getRoot().all(uniqueNameCommand); 
		assertFalse(uniqueNameCommand.getResult().hasResult()); 
    	
		checkAllIncludesAllMapEntries(new ME("top", includes), new ME("a", aInclude), new ME("b", bInclude), new ME("a.b.a", cInclude), 
				new ME("d", dInclude), new ME("a.b.a.d.a", abcdaInclude));
    	assertEquals("highest level blissfully unaware",includes.getChildInclude("a"), includes.getInclude("a")); 
    	assertEquals("was c now a.b.a",includes.getChildInclude("a").getChildInclude("b").getChildInclude("a"), includes.getInclude("a.b.a")); 
    	assertEquals("still references highest level, but reflects intermediate rename",
    			includes.getChildInclude("a").getChildInclude("b").getChildInclude("a").getChildInclude("d").getChildInclude("a"), 
    			includes.getInclude("a.b.a.d.a"));
    	
    	assertFalse(aInclude.renameBare("x").hasResult()); 
		uniqueNameCommand = new BuildUniqueNameCommand<Object>();  
		aInclude.getRoot().all(uniqueNameCommand); 
		assertFalse(uniqueNameCommand.getResult().hasResult()); 
    	checkAllIncludesAllMapEntries(new ME("top", includes), new ME("x", aInclude), new ME("b", bInclude), new ME("a", cInclude), 
    			new ME("d", dInclude), new ME("a.d.a", abcdaInclude));
    	assertEquals(includes.getChildInclude("x"), includes.getInclude("x")); 
    	assertEquals("mid-level was a.b.a now a because unique",includes.getChildInclude("x").getChildInclude("b").getChildInclude("a"), includes.getInclude("a")); 
    	assertEquals("unique name shortened to start at mid-level",
    			includes.getChildInclude("x").getChildInclude("b").getChildInclude("a").getChildInclude("d").getChildInclude("a"), 
    			includes.getInclude("a.d.a"));
    }
    @Test
    public void uniqueNameForLowerLevelIncludeWhereDuplicateIsNotAParentWillBeFullyQualifiedName() throws Exception {
    	IncludeHierarchy bInclude = addIncludeAndBuildUniqueName(includes, net2, "b"); 
    	IncludeHierarchy aInclude = addIncludeAndBuildUniqueName(includes, net2, "a"); 
    	IncludeHierarchy abInclude = addIncludeAndBuildUniqueName(aInclude, net3, "b"); 
    	assertEquals("minimially unique name for lower level include that is not child will just be fully qualified name",
    			includes.getChildInclude("a").getChildInclude("b"), includes.getInclude("top.a.b")); 
    	checkIncludeMapAllEntries(includes, new ME("top", includes), new ME("a", aInclude), new ME("b", bInclude), new ME("top.a.b", abInclude) );
    	checkIncludeMapAllEntries(bInclude, new ME("b", bInclude) );
    	checkIncludeMapAllEntries(aInclude, new ME("a", aInclude), new ME("top.a.b", abInclude) );
    	checkIncludeMapAllEntries(abInclude, new ME("top.a.b", abInclude) );
    	IncludeHierarchy cInclude = addIncludeAndBuildUniqueName(includes, net2, "c"); 
    	IncludeHierarchy cdInclude = addIncludeAndBuildUniqueName(cInclude, net3, "d"); 
    	checkIncludeMapAllEntries(includes, new ME("top", includes), new ME("a", aInclude), new ME("b", bInclude), new ME("top.a.b", abInclude),
    			new ME("c", cInclude), new ME("d", cdInclude));
    	IncludeHierarchy dInclude = addIncludeAndBuildUniqueName(includes, net2, "d"); 
    	assertEquals("same result if nets added in the opposite order",
    			includes.getChildInclude("c").getChildInclude("d"), includes.getInclude("top.c.d")); 
    	checkIncludeMapAllEntries(includes, new ME("top", includes), new ME("a", aInclude), new ME("b", bInclude), new ME("top.a.b", abInclude),
    			new ME("c", cInclude), new ME("top.c.d", cdInclude), new ME("d", dInclude) );
    	checkIncludeMapAllEntries(cInclude, new ME("c", cInclude), new ME("top.c.d", cdInclude) );
    	checkIncludeMapAllEntries(dInclude, new ME("d", dInclude) );
    }
//    @Test
    //FIXME
    public void renamesOutOfDirectParentHierarchyGenerateFullyQualifiedNames() throws Exception {
    	IncludeHierarchy bInclude = addIncludeAndBuildUniqueName(includes, net2, "b"); 
    	IncludeHierarchy aInclude = addIncludeAndBuildUniqueName(includes, net2, "a"); 
    	IncludeHierarchy cInclude = addIncludeAndBuildUniqueName(aInclude, net3, "c"); 
//    	assertEquals("minimially unique name for lower level include that is not child will just be fully qualified name",
//    			includes.getChildInclude("a").getChildInclude("b"), includes.getInclude("top.a.b")); 
//    	checkIncludeMapAllEntries(includes, new ME("top", includes), new ME("a", aInclude), new ME("b", bInclude), new ME("c", cInclude) );
//    	checkIncludeMapAllEntries(aInclude, new ME("a", aInclude), new ME("c", cInclude) );
//    	checkIncludeMapAllEntries(bInclude, new ME("b", bInclude));
//    	checkIncludeMapAllEntries(cInclude, new ME("c", cInclude) );
    	assertFalse(cInclude.renameBare("b").hasResult()); 
    	cInclude.buildFullyQualifiedName(); 
    	uniqueNameCommand = new BuildUniqueNameCommand<Object>("c"); 
    	cInclude.self(uniqueNameCommand); // FIXME  
//    	cInclude.getRoot().all(uniqueNameCommand); 
//    	checkIncludeMapAllEntries(includes, new ME("top", includes), new ME("a", aInclude), new ME("b", bInclude), new ME("top.a.b", cInclude) );
//    	checkIncludeMapAllEntries(aInclude, new ME("a", aInclude), new ME("top.a.b", cInclude) );
//    	checkIncludeMapAllEntries(bInclude, new ME("b", bInclude));
//    	checkIncludeMapAllEntries(cInclude, new ME("top.a.b", cInclude) );

    	assertFalse(cInclude.renameBare("c").hasResult()); 
    	cInclude.buildFullyQualifiedName(); 
    	uniqueNameCommand = new BuildUniqueNameCommand<Object>("top.a.b"); 
    	cInclude.self(uniqueNameCommand); // FIXME  
    	checkIncludeMapAllEntries(includes, new ME("top", includes), new ME("a", aInclude), new ME("b", bInclude), new ME("c", cInclude) );
    	checkIncludeMapAllEntries(aInclude, new ME("a", aInclude), new ME("c", cInclude) );
    	checkIncludeMapAllEntries(bInclude, new ME("b", bInclude));
    	checkIncludeMapAllEntries(cInclude, new ME("c", cInclude) );
//    	expecting: 
//    		top name top unique name top
//    		a name a unique name a
//    		b name b unique name b
//    		c name c unique name c
//    		actual map: 
//    		map: b name b unique name b
//    		map: c name c unique name c
//    		map: top.a.b name c unique name c
//    		map: a name a unique name a
//    		map: top name top unique name top

    	
    	
    	// cInclude back to c; all back to start
    	// bInclude to c; cInclude to top.a.c
//    	checkIncludeMapAllEntries(bInclude, new ME("b", bInclude) );
//    	checkIncludeMapAllEntries(aInclude, new ME("a", aInclude), new ME("top.a.b", abInclude) );
//    	checkIncludeMapAllEntries(abInclude, new ME("top.a.b", abInclude) );
//    	IncludeHierarchy cInclude = addIncludeAndBuildUniqueName(includes, net2, "c"); 
//    	IncludeHierarchy cdInclude = addIncludeAndBuildUniqueName(cInclude, net3, "d"); 
//    	checkIncludeMapAllEntries(includes, new ME("top", includes), new ME("a", aInclude), new ME("b", bInclude), new ME("top.a.b", abInclude),
//    			new ME("c", cInclude), new ME("d", cdInclude));
//    	IncludeHierarchy dInclude = addIncludeAndBuildUniqueName(includes, net2, "d"); 
//    	assertEquals("same result if nets added in the opposite order",
//    			includes.getChildInclude("c").getChildInclude("d"), includes.getInclude("top.c.d")); 
//    	checkIncludeMapAllEntries(includes, new ME("top", includes), new ME("a", aInclude), new ME("b", bInclude), new ME("top.a.b", abInclude),
//    			new ME("c", cInclude), new ME("top.c.d", cdInclude), new ME("d", dInclude) );
//    	checkIncludeMapAllEntries(cInclude, new ME("c", cInclude), new ME("top.c.d", cdInclude) );
//    	checkIncludeMapAllEntries(dInclude, new ME("d", dInclude) );
    }
    private IncludeHierarchy addBareInclude(IncludeHierarchy parentInclude, PetriNet net,
			String alias) {
		IncludeHierarchy newInclude = new IncludeHierarchy(net, parentInclude, alias); 
		Result<Object> result = parentInclude.self(new AddMapEntryCommand<>(IncludeHierarchyMapEnum.INCLUDE, alias, newInclude)); 
		if (result.hasResult()) throw new RuntimeException(result.getMessage()); 
		return newInclude; 
	}
    protected IncludeHierarchy addIncludeAndBuildUniqueName(IncludeHierarchy parentInclude, PetriNet net, String name) {
    	IncludeHierarchy include = addBareInclude(parentInclude, net, name); 
    	assertFalse(include.self(new BuildUniqueNameCommand<Object>()).hasResult());
    	return include;
    }

}

