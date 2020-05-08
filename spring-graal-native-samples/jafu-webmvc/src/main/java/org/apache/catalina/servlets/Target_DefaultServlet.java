package org.apache.catalina.servlets;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.Source;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import org.apache.catalina.WebResource;

@TargetClass(className = "org.apache.catalina.servlets.DefaultServlet")
final class Target_DefaultServlet {
	
	@Substitute
	protected InputStream renderXml(HttpServletRequest request, String contextPath, WebResource resource, Source xsltSource, String encoding)
			throws IOException, ServletException {
		return null;
	}

	@Substitute
	protected Source findXsltSource(WebResource directory)  throws IOException {
		return null;
	}
}
