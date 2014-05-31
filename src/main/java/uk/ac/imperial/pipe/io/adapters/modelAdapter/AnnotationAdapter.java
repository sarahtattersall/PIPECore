package uk.ac.imperial.pipe.io.adapters.modelAdapter;

import uk.ac.imperial.pipe.io.adapters.model.AdaptedAnnotation;
import uk.ac.imperial.pipe.models.petrinet.Annotation;
import uk.ac.imperial.pipe.models.petrinet.AnnotationImpl;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class AnnotationAdapter extends XmlAdapter<AdaptedAnnotation, Annotation> {

    @Override
    public final AnnotationImpl unmarshal(AdaptedAnnotation adaptedAnnotation) {
        return new AnnotationImpl(adaptedAnnotation.getX(), adaptedAnnotation.getY(), adaptedAnnotation.getText(),
                        adaptedAnnotation.getWidth(), adaptedAnnotation.getHeight(), adaptedAnnotation.hasBoarder());
    }

    @Override
    public final AdaptedAnnotation marshal(Annotation annotation) {
        AdaptedAnnotation adaptedAnnotation = new AdaptedAnnotation();
        adaptedAnnotation.setText(annotation.getText());
        adaptedAnnotation.setX(annotation.getX());
        adaptedAnnotation.setY(annotation.getY());
        adaptedAnnotation.setBorder(annotation.hasBorder());
        adaptedAnnotation.setWidth(annotation.getWidth());
        adaptedAnnotation.setHeight(annotation.getHeight());
        return adaptedAnnotation;
    }
}
