package com.agent;

import org.objectweb.asm.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class MyClassFileTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer)
            throws IllegalClassFormatException {
        if (className == null) {
            return classfileBuffer;
        }
        try {
            if (!className.equals("org/apache/dubbo/rpc/cluster/support/AbstractClusterInvoker")) {
                return classfileBuffer;
            }
            System.out.println("premain load Class     :" + className);
            // Use ASM tree api...
            ClassReader cr = new ClassReader(classfileBuffer);
            ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
            ClassVisitor cv = new ChangeClassAdapter(cw);
            cr.accept(cv, 8);
            return cw.toByteArray();
        } catch (Exception e) {
            System.out.println("exception: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}