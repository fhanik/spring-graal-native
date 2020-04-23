package org.springframework.web.servlet.function.support;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

@TargetClass(className = "org.springframework.web.servlet.function.support.RouterFunctionMapping")
final class Target_RouterFunctionMapping {

	@Substitute
	private void initMessageConverters() {

	}

}
