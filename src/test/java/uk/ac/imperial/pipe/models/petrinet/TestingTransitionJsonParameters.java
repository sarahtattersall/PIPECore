package uk.ac.imperial.pipe.models.petrinet;

public class TestingTransitionJsonParameters extends AbstractTransitionJsonParameters {

    public int getNum() {
        return getParameters().getInt("num");
    }

    @Override
    public void fire() {
    }

}
