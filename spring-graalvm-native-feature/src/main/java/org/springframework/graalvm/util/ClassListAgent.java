package org.springframework.graalvm.util;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

public class ClassListAgent implements ClassFileTransformer {
    public static void premain(String agentArgs, Instrumentation inst) {
        inst.addTransformer(new ClassListAgent(), true);
        System.out.println("Class-Agent: Transformer Added");
    }

    public ClassListAgent() {
        System.out.println("Class-Agent: Creating Transformer");
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        System.out.println("Class-Agent-Transform: "+className+".class");
        return classfileBuffer;
    }
}
