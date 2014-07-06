package uk.ac.imperial.pipe.models.petrinet;

import uk.ac.imperial.pipe.exceptions.PetriNetComponentNotFoundException;

public abstract class AbstractPetriNet {

    /**
     * @param id
     * @return true if any component in the Petri net has this id
     */
    public  abstract boolean containsComponent(String id);

    /**
     * @param id    component name
     * @param clazz PetriNetComponent class
     * @param <T>   type of Petri net component required
     * @return component with the specified id if it exists in the Petri net
     * @throws PetriNetComponentNotFoundException if component does not exist in Petri net
     */
    public abstract <T extends PetriNetComponent> T getComponent(String id, Class<T> clazz) throws PetriNetComponentNotFoundException;
 
	
}
