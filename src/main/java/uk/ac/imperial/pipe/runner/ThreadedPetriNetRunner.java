package uk.ac.imperial.pipe.runner;

public class ThreadedPetriNetRunner implements Runnable {

    private PetriNetRunner runner;

    public ThreadedPetriNetRunner(PetriNetRunner petriNetRunner) {
        this.runner = petriNetRunner;
    }

    @Override
    public void run() {
        runner.run();
    }

}
