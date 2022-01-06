package com.agent;

import com.agent.interceptor.AroundInterceptor;
import com.agent.interceptor.Impl.MyInterceptor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.List;

public class MyClassFileTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer)
            throws IllegalClassFormatException {
        if (className == null) {
            return classfileBuffer;
        }
        try {
//            if (!className.equals("org/apache/dubbo/rpc/protocol/AbstractInvoker") && !className.equals("org/apache/dubbo/rpc/proxy/AbstractProxyInvoker")) {
//                return classfileBuffer;
//            }
            System.out.println("premain load Class     :" + className);
            // Use ASM tree api...
            ClassReader classReader = new ClassReader(classfileBuffer);
            ClassNode classNode = new ClassNode();
            classReader.accept(classNode, 0);

//            String targetMethodName = "invoke";
//            MethodNode methodNode = getMethodNode(targetMethodName, classNode);
//            if (methodNode == null) {
//                System.out.println("there is no method named " + targetMethodName + "in class" + className);
//                return classfileBuffer;
//            }

//            System.out.println(methodNode.name);
//            final InsnList instructions = new InsnList();
//            final String description = Type.getMethodDescriptor(MyInterceptor.class.getMethod("before", Object.class, Object[].class));
//            instructions.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, Type.getInternalName(AroundInterceptor.class), "before", description, true));
//            methodNode.instructions.insertBefore(methodNode.instructions.getFirst(), instructions);
//            methodNode.accept(classNode);

            ClassWriter classWriter = new ClassWriter(0);
            classNode.accept(classWriter);

            return classWriter.toByteArray();
        } catch (Exception e) {
            System.out.println("exception: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private MethodNode getMethodNode(String methodName, ClassNode classNode) {
        List<MethodNode> methodNodes = classNode.methods;
        for (MethodNode methodNode : methodNodes) {
            if (methodNode.name.equals(methodName)) {
                return methodNode;
            }
        }
        return null;
    }
}