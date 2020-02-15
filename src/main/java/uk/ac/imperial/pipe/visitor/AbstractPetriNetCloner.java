package uk.ac.imperial.pipe.visitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import uk.ac.imperial.pipe.exceptions.InvalidRateException;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.models.petrinet.AbstractArc;
import uk.ac.imperial.pipe.models.petrinet.AbstractPetriNet;
import uk.ac.imperial.pipe.models.petrinet.Annotation;
import uk.ac.imperial.pipe.models.petrinet.Arc;
import uk.ac.imperial.pipe.models.petrinet.ArcPoint;
import uk.ac.imperial.pipe.models.petrinet.ColoredToken;
import uk.ac.imperial.pipe.models.petrinet.Connectable;
import uk.ac.imperial.pipe.models.petrinet.FunctionalRateParameter;
import uk.ac.imperial.pipe.models.petrinet.InboundArc;
import uk.ac.imperial.pipe.models.petrinet.InboundInhibitorArc;
import uk.ac.imperial.pipe.models.petrinet.InboundNormalArc;
import uk.ac.imperial.pipe.models.petrinet.InboundTestArc;
import uk.ac.imperial.pipe.models.petrinet.OutboundArc;
import uk.ac.imperial.pipe.models.petrinet.OutboundNormalArc;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;
import uk.ac.imperial.pipe.models.petrinet.PetriNetComponent;
import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.RateParameter;
import uk.ac.imperial.pipe.models.petrinet.RateType;
import uk.ac.imperial.pipe.models.petrinet.Token;
import uk.ac.imperial.pipe.models.petrinet.Transition;
import uk.ac.imperial.pipe.models.petrinet.name.FileNameVisitor;
import uk.ac.imperial.pipe.models.petrinet.name.NormalNameVisitor;
import uk.ac.imperial.pipe.models.petrinet.name.NormalPetriNetName;
import uk.ac.imperial.pipe.models.petrinet.name.PetriNetFileName;
import uk.ac.imperial.pipe.models.petrinet.name.PetriNetName;

public abstract class AbstractPetriNetCloner {

    /**
     * Class logger
     */
    private static final Logger LOGGER = Logger.getLogger(PetriNetCloner.class.getName());
    /**
     * Original Petri net to clone
     */
    protected PetriNet petriNet;
    /**
     * cloned rate parameters
     */
    private final Map<String, RateParameter> rateParameters = new HashMap<>();
    /**
     * cloned places
     */
    protected final Map<String, Place> places = new HashMap<>();
    /**
     * cloned transitions
     */
    private final Map<String, Transition> transitions = new HashMap<>();

    /**
     * @return the cloned Petri net
     *
     */
    protected abstract AbstractPetriNet getNewPetriNet();

    protected abstract AbstractPetriNetCloner getInstance();

    protected boolean simpleClone = false;

    /**
     * Visit the components of the source PetriNet, and add a cloned version of the component, (possibly with modifications),
     * to the new or refreshed AbstractPetriNet.
     */
    protected void visitAllComponents() {
        visit(petriNet.getName());

        for (Token token : petriNet.getTokens()) {
            visit(token);
        }

        for (Annotation annotation : petriNet.getAnnotations()) {
            visit(annotation);
        }

        for (Place place : petriNet.getPlaces()) {
            visit(place);
        }

        for (RateParameter rateParameter : petriNet.getRateParameters()) {
            visit(rateParameter);
        }
        for (Transition transition : petriNet.getTransitions()) {
            visit(transition);
        }

        for (InboundArc arc : petriNet.getInboundArcs()) {
            visit(arc);
        }

        for (OutboundArc arc : petriNet.getOutboundArcs()) {
            visit(arc);
        }
    }

    /**
     * Clone a Petri net name
     * @param name of the Petri net
     */
    private void visit(PetriNetName name) {
        if (name != null) {
            name.visit(new NameCloner());
        }
    }

    /**
     * Clones and adds a token to the new Petri net
     * @param token original token
     */
    public void visit(Token token) {
        Token newToken = new ColoredToken(token);
        getNewPetriNet().addToken(newToken);
    }

    /**
     * Clones and adds a rate parameter to the new Petri net
     * @param rate original rate
     */
    public void visit(RateParameter rate) {
        RateParameterCloner cloner = new RateParameterCloner();
        try {
            rate.accept(cloner);
        } catch (PetriNetComponentException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        }
        RateParameter rateParameter = cloner.cloned;
        try {
            getNewPetriNet().addRateParameter(rateParameter);
            rateParameters.put(rateParameter.getId(), rateParameter);
        } catch (InvalidRateException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        }

    }

    /**
     * Clones and adds an annotation to the new Petri net
     * @param annotation original annotation
     */
    public void visit(Annotation annotation) {
        AnnotationCloner cloner = new AnnotationCloner();
        try {
            annotation.accept(cloner);
        } catch (PetriNetComponentException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        }
        Annotation newAnnotation = cloner.cloned;
        prefixIdWithQualifiedName(newAnnotation);
        getNewPetriNet().addAnnotation(newAnnotation);

    }

    /**
     * Clones and adds the new place to the new Petri net
     *
     * @param place original place
     */
    public void visit(Place place) {
        PlaceBuilder builder = new PlaceBuilder(getInstance(), simpleClone);
        try {
            place.accept(builder);
        } catch (PetriNetComponentException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        }
        Place newPlace = builder.built;
        addPlaceToNet(place, newPlace);
    }

    protected void addPlaceToNet(Place place, Place newPlace) {
        getNewPetriNet().addPlace(newPlace);
    }

    protected void updatePlace(Place place, Place newPlace) {
        for (Map.Entry<String, Integer> entry : place.getTokenCounts().entrySet()) {
            newPlace.setTokenCount(entry.getKey(), entry.getValue());
        }
        places.put(place.getId(), newPlace);
    }

    /**
     * Clones and adds the new transition to the new Petri net
     * @param transition original transition
     */
    public void visit(Transition transition) {
        TransitionCloner cloner = new TransitionCloner();
        try {
            transition.accept(cloner);
        } catch (PetriNetComponentException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        }
        Transition newTransition = cloner.cloned;
        prefixIdWithQualifiedName(newTransition);
        if (transition.getRate().getRateType().equals(RateType.RATE_PARAMETER)) {
            FunctionalRateParameter rateParameter = (FunctionalRateParameter) transition.getRate();
            newTransition.setRate(rateParameters.get(rateParameter.getId()));
        }
        newTransition.addPropertyChangeListener(transition);
        transitions.put(transition.getId(), newTransition);
        getNewPetriNet().addTransition(newTransition);
    }

    /**
     * Clones and adds the new inbound arc to the new Petri net
     * @param arc original inbound arc
     */
    public void visit(InboundArc arc) {
        InboundArc newArc = buildInboundArc(arc, places.get(arc.getSource().getId()), transitions
                .get(arc.getTarget().getId()));
        rebuildNameWithQualifiedNames(newArc);
        getNewPetriNet().addArc(newArc);
    }

    /**
     * Clones and adds the new outbound arc to the new Petri net
     * @param arc original outbound arc
     */
    public void visit(OutboundArc arc) {
        OutboundArc newArc = buildOutboundArc(arc, transitions.get(arc.getSource().getId()), places
                .get(arc.getTarget().getId()));
        rebuildNameWithQualifiedNames(newArc);
        getNewPetriNet().addArc(newArc);
    }

    protected InboundArc buildInboundArc(InboundArc arc, Place source, Transition target) {
        InboundArc newArc;
        switch (arc.getType()) {
        case INHIBITOR:
            newArc = new InboundInhibitorArc(source, target);
            break;
        case TEST:
            newArc = new InboundTestArc(source, target);
            break;
        default:
            newArc = new InboundNormalArc(source, target, arc.getTokenWeights());
        }
        addIntermediatePoints(arc, newArc);
        newArc.setId(arc.getId());
        return newArc;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void addIntermediatePoints(AbstractArc arc, AbstractArc newArc) {
        List<ArcPoint> arcPoints = arc.getArcPoints();
        for (int i = 1; i < arcPoints.size() - 1; i++) {
            ArcPoint point = arcPoints.get(i);
            newArc.addIntermediatePoint(point);
            point.changeSupport.removePropertyChangeListener(newArc.getIntermediateListener());
        }
    }

    protected OutboundArc buildOutboundArc(OutboundArc arc, Transition source, Place target) {
        OutboundArc newArc = new OutboundNormalArc(source, target, arc.getTokenWeights());
        addIntermediatePoints(arc, newArc);
        newArc.setId(arc.getId());
        return newArc;
    }

    protected abstract void prefixIdWithQualifiedName(PetriNetComponent component);

    protected abstract void prepareExecutablePetriNetPlaceProcessing(Place place, Place newPlace);

    protected void rebuildNameWithQualifiedNames(Arc<? extends Connectable, ? extends Connectable> newArc) {
        newArc.setId(newArc.getSource().getId() + " TO " + newArc.getTarget().getId());
    }

    class NameCloner implements NormalNameVisitor, FileNameVisitor {

        /**
         * Clones a PetriNetFileName
         * @param name of the Petri net
         */
        @Override
        public void visit(PetriNetFileName name) {
            getNewPetriNet().setName(new PetriNetFileName(name.getFile()));
        }

        /**
         * Clones a NormalPetriNetName
         * @param name name to visit
         */
        @Override
        public void visit(NormalPetriNetName name) {
            getNewPetriNet().setName(new NormalPetriNetName(name.getName()));
        }
    }

}
