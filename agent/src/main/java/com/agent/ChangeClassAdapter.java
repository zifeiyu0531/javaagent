package com.agent;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ChangeClassAdapter extends ClassVisitor {
    private ClassVisitor cv;

    public ChangeClassAdapter(int api, ClassVisitor cv){
        super(api, cv);
    }

    public ChangeClassAdapter(ClassVisitor cv){
        super(Opcodes.ASM4);
        this.cv = cv;
    }

    public ChangeClassAdapter(int asm){
        super(asm);
    }

    @Override
    public MethodVisitor visitMethod(
            int access,
            String name,
            String desc,
            String signature,
            String[] exceptions
    ){
        MethodVisitor mv = this.cv.visitMethod(access, name, desc, signature, exceptions);
        if(cv != null && name.equals("list")){
            System.out.println("list method visited");
        }
        return mv;
    }
}
