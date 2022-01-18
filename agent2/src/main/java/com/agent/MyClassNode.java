package com.agent;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

public class MyClassNode extends ClassNode {

    public MyClassNode(ClassVisitor cv) {
        super(Opcodes.ASM9);
        this.cv = cv;
    }

    @Override
    public void visitEnd() {
        ClassTransformer ct = new MyClassTransformer(null);
        ct.transform(this);

        super.visitEnd();

        if (cv != null) {
            accept(cv);
        }
    }

    private static class MyClassTransformer extends ClassTransformer {

        private MyClassTransformer(ClassTransformer ct) {
            super(ct);
        }

        @Override
        public void transform(ClassNode cn) {
            for (MethodNode mn : cn.methods) {
                if (mn.name.equals("select")) {
                    try{
                        Class.forName("com.agent.Interceptor");
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    InsnList instructions = mn.instructions;
                    if (instructions.size() == 0) {
                        continue;
                    }
                    InsnList il = new InsnList();

                    il.add(new VarInsnNode(ALOAD, 0));
                    il.add(new VarInsnNode(ALOAD, 1));
                    il.add(new VarInsnNode(ALOAD, 2));
                    il.add(new VarInsnNode(ALOAD, 3));
                    il.add(new VarInsnNode(ALOAD, 4));
                    il.add(new MethodInsnNode(INVOKEVIRTUAL, "com/agent/Interceptor", "select2", "(Lorg/apache/dubbo/rpc/cluster/LoadBalance;Lorg/apache/dubbo/rpc/Invocation;Ljava/util/List;Ljava/util/List;)Lorg/apache/dubbo/rpc/Invoker;", false);
                    instructions.insert(il);
                }
            }
            super.transform(cn);
        }
    }
}
