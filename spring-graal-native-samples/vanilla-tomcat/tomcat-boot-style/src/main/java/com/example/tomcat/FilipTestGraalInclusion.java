package com.example.tomcat;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.net.AprEndpoint;

public class FilipTestGraalInclusion extends AprEndpoint {
    private static final Log log = LogFactory.getLog(FilipTestGraalInclusion.class);

    public FilipTestGraalInclusion() {
        log.info("test");
    }
}
