package fi.evident.apina.spring;

import fi.evident.apina.java.model.JavaAnnotation;
import fi.evident.apina.java.model.type.JavaBasicType;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SpringAnnotationUtilsTest {

    private static final JavaBasicType REQUEST_PARAM = new JavaBasicType("org.springframework.web.bind.annotation.RequestParam");

    @Test
    public void undefinedRequestParamName() {
        JavaAnnotation annotation = new JavaAnnotation(REQUEST_PARAM);
        assertThat(SpringAnnotationUtils.getRequestParamName(annotation), is(Optional.empty()));
    }

    @Test
    public void requestParamNameAsValueAttribute() {
        JavaAnnotation annotation = new JavaAnnotation(REQUEST_PARAM);
        annotation.setAttribute("value", "foo");

        assertThat(SpringAnnotationUtils.getRequestParamName(annotation), is(Optional.of("foo")));
    }

    @Test
    public void requestParamNameAsNameAttribute() {
        JavaAnnotation annotation = new JavaAnnotation(REQUEST_PARAM);
        annotation.setAttribute("name", "foo");

        assertThat(SpringAnnotationUtils.getRequestParamName(annotation), is(Optional.of("foo")));
    }
}
