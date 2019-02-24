package uk.ac.imperial.pipe.runner;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ThreadedPetriNetRunnerTest {

    @Mock
    private PetriNetRunner mockRunner;
    private ThreadedPetriNetRunner threadRunner;

    @Test
    public void startingThreadRunsPetriNet() {
        threadRunner = new ThreadedPetriNetRunner(mockRunner);
        threadRunner.run();
        verify(mockRunner).run();
    }

    @Test
    public void threadRunsThreadedRunner() throws Exception {
        TestingPetriNetRunner runner = new TestingPetriNetRunner();
        threadRunner = new ThreadedPetriNetRunner(runner);
        assertTrue(!runner.run);
        Thread thread = new Thread(threadRunner);
        thread.start();
        Thread.sleep(5);
        assertTrue(runner.run);
    }

    private class TestingPetriNetRunner extends PetriNetRunner {

        public boolean run = false;

        @Override
        public void run() {
            run = true;
        }

    }
}
