package fi.evident.apina.spring;

import fi.evident.apina.java.model.JavaAnnotatedElement;
import fi.evident.apina.java.model.JavaAnnotation;
import fi.evident.apina.java.model.type.JavaBasicType;
import org.junit.Test;

import java.util.List;

import static fi.evident.apina.spring.SpringModelReader.findRequestMappingPath;
import static java.util.Arrays.asList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class SpringModelReaderTest {

    private static final JavaBasicType REQUEST_MAPPING = new JavaBasicType("org.springframework.web.bind.annotation.RequestMapping");

    @Test
    public void requestMappingPath() {
        assertThat(findRequestMappingPath(elementWithPathRequestMapping("")), is(""));
        assertThat(findRequestMappingPath(elementWithPathRequestMapping("/")), is("/"));
        assertThat(findRequestMappingPath(elementWithPathRequestMapping("/foo")), is("/foo"));
        assertThat(findRequestMappingPath(elementWithPathRequestMapping("/foo/bar")), is("/foo/bar"));
    }

    @Test
    public void emptyRequestMappingPathWithoutLeadingSlashGetsSlashAddedAutomatically() {
        assertThat(findRequestMappingPath(elementWithPathRequestMapping("foo")), is("/foo"));
        assertThat(findRequestMappingPath(elementWithPathRequestMapping("foo/bar")), is("/foo/bar"));
    }

    private static MockAnnotatedElement elementWithPathRequestMapping(String path) {
        JavaAnnotation requestMapping = new JavaAnnotation(REQUEST_MAPPING);
        requestMapping.setAttribute("value", path);
        return new MockAnnotatedElement(requestMapping);
    }

    private static final class MockAnnotatedElement implements JavaAnnotatedElement {

        private final List<JavaAnnotation> annotations;

        MockAnnotatedElement(JavaAnnotation... annotations) {
            this.annotations = asList(annotations);
        }

        @Override
        public List<JavaAnnotation> getAnnotations() {
            return annotations;
        }
    }
}
