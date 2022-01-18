package com.agent;

import com.tencent.polaris.factory.api.DiscoveryAPIFactory;
import org.objectweb.asm.*;

public class ChangeClassAdapter extends ClassVisitor {

    public ChangeClassAdapter(ClassVisitor cv){
        super(Opcodes.ASM5, cv);
    }

    @Override
    public MethodVisitor visitMethod(
            int access,
            String name,
            String desc,
            String signature,
            String[] exceptions
    ){
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        if(cv != null && name.equals("select")){
            mv.visitCode();

            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitFieldInsn(Opcodes.GETSTATIC, "org/apache/dubbo/rpc/cluster/support/AbstractClusterInvoker", "CONSUMER_API", "Lcom/tencent/polaris/api/core/ConsumerAPI;");

            Label l1 = new Label();
            mv.visitJumpInsn(Opcodes.IFNONNULL, l1);

            Label l2 = new Label();
            mv.visitLabel(l2);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/tencent/polaris/factory/api/DiscoveryAPIFactory", "createConsumerAPI", "()Lcom/tencent/polaris/api/core/ConsumerAPI;", false);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, "org/apache/dubbo/rpc/cluster/support/AbstractClusterInvoker", "CONSUMER_API", "Lcom/tencent/polaris/api/core/ConsumerAPI;");
            mv.visitLabel(l1);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitLdcInsn("Dubbo");
            mv.visitVarInsn(Opcodes.ASTORE, 5);

            Label l3 = new Label();
            mv.visitLabel(l3);
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "org/apache/dubbo/rpc/Invocation", "getServiceName", "()Ljava/lang/String;", true);
            mv.visitVarInsn(Opcodes.ASTORE, 6);

            Label l4 = new Label();
            mv.visitLabel(l4);
            mv.visitTypeInsn(Opcodes.NEW, "com/tencent/polaris/api/rpc/GetOneInstanceRequest");
            mv.visitInsn(Opcodes.DUP);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "com/tencent/polaris/api/rpc/GetOneInstanceRequest", "<init>", "()V", false);
            mv.visitVarInsn(Opcodes.ASTORE, 7);

            Label l5 = new Label();
            mv.visitLabel(l5);
            mv.visitVarInsn(Opcodes.ALOAD, 7);
            mv.visitVarInsn(Opcodes.ALOAD, 5);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/tencent/polaris/api/rpc/GetOneInstanceRequest", "setNamespace", "(Ljava/lang/String;)V", false);

            Label l6 = new Label();
            mv.visitLabel(l6);
            mv.visitVarInsn(Opcodes.ALOAD, 7);
            mv.visitVarInsn(Opcodes.ALOAD, 6);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/tencent/polaris/api/rpc/GetOneInstanceRequest", "setService", "(Ljava/lang/String;)V", false);

            Label l7 = new Label();
            mv.visitLabel(l7);
            mv.visitFieldInsn(Opcodes.GETSTATIC, "org/apache/dubbo/rpc/cluster/support/AbstractClusterInvoker", "CONSUMER_API", "Lcom/tencent/polaris/api/core/ConsumerAPI;");
            mv.visitVarInsn(Opcodes.ALOAD, 7);
            mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "com/tencent/polaris/api/core/ConsumerAPI", "getOneInstance", "(Lcom/tencent/polaris/api/rpc/GetOneInstanceRequest;)Lcom/tencent/polaris/api/rpc/InstancesResponse;", true);
            mv.visitVarInsn(Opcodes.ASTORE, 8);

            Label l8 = new Label();
            mv.visitLabel(l8);
            mv.visitLineNumber(34, l8);
            mv.visitVarInsn(Opcodes.ALOAD, 8);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/tencent/polaris/api/rpc/InstancesResponse", "getInstances", "()[Lcom/tencent/polaris/api/pojo/Instance;", false);
            mv.visitVarInsn(Opcodes.ASTORE, 9);

            Label l9 = new Label();
            mv.visitLabel(l9);
            mv.visitVarInsn(Opcodes.ALOAD, 9);
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitInsn(Opcodes.AALOAD);
            mv.visitVarInsn(Opcodes.ASTORE, 10);

            Label l10 = new Label();
            mv.visitLabel(l10);
            mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            mv.visitLdcInsn("target instance is %s:%d%n");
            mv.visitInsn(Opcodes.ICONST_2);
            mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object");
            mv.visitInsn(Opcodes.DUP);
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitVarInsn(Opcodes.ALOAD, 10);
            mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "com/tencent/polaris/api/pojo/Instance", "getHost", "()Ljava/lang/String;", true);
            mv.visitInsn(Opcodes.AASTORE);
            mv.visitInsn(Opcodes.DUP);
            mv.visitInsn(Opcodes.ICONST_1);
            mv.visitVarInsn(Opcodes.ALOAD, 10);
            mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "com/tencent/polaris/api/pojo/Instance", "getPort", "()I", true);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
            mv.visitInsn(Opcodes.AASTORE);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "printf", "(Ljava/lang/String;[Ljava/lang/Object;)Ljava/io/PrintStream;", false);
            mv.visitInsn(Opcodes.POP);

            Label l11 = new Label();
            mv.visitLabel(l11);
            mv.visitVarInsn(Opcodes.ALOAD, 3);
            mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/List", "iterator", "()Ljava/util/Iterator;", true);
            mv.visitVarInsn(Opcodes.ASTORE, 11);

            Label l12 = new Label();
            mv.visitLabel(l12);
            mv.visitFrame(Opcodes.F_FULL, 12, new Object[]{"org/apache/dubbo/rpc/cluster/support/AbstractClusterInvoker", "org/apache/dubbo/rpc/cluster/LoadBalance", "org/apache/dubbo/rpc/Invocation", "java/util/List", "java/util/List", "java/lang/String", "java/lang/String", "com/tencent/polaris/api/rpc/GetOneInstanceRequest", "com/tencent/polaris/api/rpc/InstancesResponse", "[Lcom/tencent/polaris/api/pojo/Instance;", "com/tencent/polaris/api/pojo/Instance", "java/util/Iterator"}, 0, new Object[]{});
            mv.visitVarInsn(Opcodes.ALOAD, 11);
            mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z", true);

            Label l13 = new Label();
            mv.visitJumpInsn(Opcodes.IFEQ, l13);
            mv.visitVarInsn(Opcodes.ALOAD, 11);
            mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;", true);
            mv.visitTypeInsn(Opcodes.CHECKCAST, "org/apache/dubbo/rpc/Invoker");
            mv.visitVarInsn(Opcodes.ASTORE, 12);

            Label l14 = new Label();
            mv.visitLabel(l14);
            mv.visitVarInsn(Opcodes.ALOAD, 12);
            mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "org/apache/dubbo/rpc/Invoker", "getUrl", "()Lorg/apache/dubbo/common/URL;", true);
            mv.visitVarInsn(Opcodes.ASTORE, 13);

            Label l15 = new Label();
            mv.visitLabel(l15);
            mv.visitVarInsn(Opcodes.ALOAD, 13);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "org/apache/dubbo/common/URL", "getHost", "()Ljava/lang/String;", false);
            mv.visitVarInsn(Opcodes.ALOAD, 10);
            mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "com/tencent/polaris/api/pojo/Instance", "getHost", "()Ljava/lang/String;", true);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);

            Label l16 = new Label();
            mv.visitJumpInsn(Opcodes.IFEQ, l16);
            mv.visitVarInsn(Opcodes.ALOAD, 13);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "org/apache/dubbo/common/URL", "getPort", "()I", false);
            mv.visitVarInsn(Opcodes.ALOAD, 10);
            mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "com/tencent/polaris/api/pojo/Instance", "getPort", "()I", true);
            mv.visitJumpInsn(Opcodes.IF_ICMPNE, l16);

            Label l17 = new Label();
            mv.visitLabel(l17);
            mv.visitVarInsn(Opcodes.ALOAD, 12);
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitLabel(l16);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitJumpInsn(Opcodes.GOTO, l12);
            mv.visitLabel(l13);
            mv.visitFrame(Opcodes.F_CHOP, 1, null, 0, null);
            mv.visitVarInsn(Opcodes.ALOAD, 3);
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/List", "get", "(I)Ljava/lang/Object;", true);
            mv.visitTypeInsn(Opcodes.CHECKCAST, "org/apache/dubbo/rpc/Invoker");
            mv.visitInsn(Opcodes.ARETURN);

            Label l18 = new Label();
            mv.visitLabel(l18);
            mv.visitLocalVariable("url", "Lorg/apache/dubbo/common/URL;", null, l15, l16, 13);
            mv.visitLocalVariable("invoker", "Lorg/apache/dubbo/rpc/Invoker;", "Lorg/apache/dubbo/rpc/Invoker<TT;>;", l14, l16, 12);
            mv.visitLocalVariable("this", "Lorg/apache/dubbo/rpc/cluster/support/AbstractClusterInvoker;", "Lorg/apache/dubbo/rpc/cluster/support/AbstractClusterInvoker<TT;>;", l0, l18, 0);
            mv.visitLocalVariable("loadbalance", "Lorg/apache/dubbo/rpc/cluster/LoadBalance;", null, l0, l18, 1);
            mv.visitLocalVariable("invocation", "Lorg/apache/dubbo/rpc/Invocation;", null, l0, l18, 2);
            mv.visitLocalVariable("invokers", "Ljava/util/List;", "Ljava/util/List<Lorg/apache/dubbo/rpc/Invoker<TT;>;>;", l0, l18, 3);
            mv.visitLocalVariable("selected", "Ljava/util/List;", "Ljava/util/List<Lorg/apache/dubbo/rpc/Invoker<TT;>;>;", l0, l18, 4);
            mv.visitLocalVariable("namespace", "Ljava/lang/String;", null, l3, l18, 5);
            mv.visitLocalVariable("service", "Ljava/lang/String;", null, l4, l18, 6);
            mv.visitLocalVariable("getOneInstanceRequest", "Lcom/tencent/polaris/api/rpc/GetOneInstanceRequest;", null, l5, l18, 7);
            mv.visitLocalVariable("oneInstance", "Lcom/tencent/polaris/api/rpc/InstancesResponse;", null, l8, l18, 8);
            mv.visitLocalVariable("instances", "[Lcom/tencent/polaris/api/pojo/Instance;", null, l9, l18, 9);
            mv.visitLocalVariable("targetInstance", "Lcom/tencent/polaris/api/pojo/Instance;", null, l10, l18, 10);
            mv.visitMaxs(6, 14);

            mv.visitEnd();
            return null;
        }
        return mv;
    }

    @Override
    public void visitEnd(){
        FieldVisitor fv =cv.visitField(Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC, "CONSUMER_API", "Lcom/tencent/polaris/api/core/ConsumerAPI;", null, null);
        if (fv!=null){
            fv.visitEnd();
        }
        cv.visitEnd();
    }
}
