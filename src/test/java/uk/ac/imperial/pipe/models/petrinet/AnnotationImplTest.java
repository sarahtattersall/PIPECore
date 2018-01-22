package uk.ac.imperial.pipe.models.petrinet;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class AnnotationImplTest {

    @Mock
    private AnnotationVisitor visitor;

    @Mock
    private AnnotationImplVisitor implVisitor;

    private AnnotationImpl annotation;

    @Before
    public void setUp() {
        annotation = new AnnotationImpl(0, 0, "foo", 10, 10, false);
    }

    @Test
    public void acceptsAnnotationVisitor() {
        annotation.accept(visitor);
        verify(visitor).visit(annotation);
    }

    @Test
    public void acceptsAnnotationImplVisitor() {
        annotation.accept(implVisitor);
        verify(implVisitor).visit(annotation);
    }
}