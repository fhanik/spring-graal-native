package org.apache.tomcat.util.digester;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

@TargetClass(className = "org.apache.tomcat.util.digester.Digester")
final class Target_Digester {

	@Substitute
	public SAXParserFactory getFactory() throws SAXNotRecognizedException, SAXNotSupportedException,
			ParserConfigurationException {

		return null;
	}
}
