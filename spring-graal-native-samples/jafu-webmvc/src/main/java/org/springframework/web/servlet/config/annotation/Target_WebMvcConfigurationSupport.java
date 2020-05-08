package org.springframework.web.servlet.config.annotation;

import java.util.List;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.http.converter.HttpMessageConverter;

@TargetClass(className = "org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport")
final class Target_WebMvcConfigurationSupport {

	@Substitute
	protected final void addDefaultHttpMessageConverters(List<HttpMessageConverter<?>> messageConverters) {
	}

}
