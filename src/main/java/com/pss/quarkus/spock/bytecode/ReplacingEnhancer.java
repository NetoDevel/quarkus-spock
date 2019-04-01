package com.pss.quarkus.spock.bytecode;

import com.pss.quarkus.spock.inject.InjectionOverride;
import com.pss.quarkus.spock.util.ClassUtil;
import org.objectweb.asm.*;

public class ReplacingEnhancer extends ClassVisitor {

    public ReplacingEnhancer(ClassVisitor classVisitor) {
        super(Opcodes.ASM6, classVisitor);
    }


    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        // todo: Make sure it is not a Bridge Method?
        if(access == Opcodes.ACC_PUBLIC && "create".equals(name)){
            return new ReplacingMethodEnhancer(super.visitMethod(access, name, descriptor, signature, exceptions));
        }
        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }



    static class ReplacingMethodEnhancer extends MethodVisitor {

        ReplacingMethodEnhancer(MethodVisitor methodVisitor) {
            super(Opcodes.ASM6, methodVisitor);
        }


        @Override
        public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {

        }

        @Override
        public void visitInsn(int opcode) {

        }

        @Override
        public void visitIntInsn(int opcode, int operand) {

        }

        @Override
        public void visitVarInsn(int opcode, int var) {

        }

        @Override
        public void visitTypeInsn(int opcode, String type) {
            // Replace new with
            if(Opcodes.NEW == opcode && InjectionOverride.contains(type)){
                super.visitVarInsn(Opcodes.ALOAD, 0);
                super.visitVarInsn(Opcodes.ALOAD, 1);
                super.visitMethodInsn(Opcodes.INVOKESTATIC,
                        ClassUtil.toJvm(InjectionOverride.class),
                        "getInjection",
                        "(Lio/quarkus/arc/InjectableBean;Ljavax/enterprise/context/spi/CreationalContext;)Ljava/lang/Object;",
                        false);
                super.visitTypeInsn(Opcodes.CHECKCAST, type);
                super.visitInsn(Opcodes.ARETURN);
                super.visitMaxs(2, 2);
            }
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor) {
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        }

        @Override
        public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
        }

        @Override
        public void visitJumpInsn(int opcode, Label label) {

        }

        @Override
        public void visitLabel(Label label) {

        }

        @Override
        public void visitLdcInsn(Object value) {

        }

        @Override
        public void visitIincInsn(int var, int increment) {

        }

        @Override
        public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
        }

        @Override
        public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {

        }

        @Override
        public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
        }

        @Override
        public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
            return null;
        }

        @Override
        public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        }

        @Override
        public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
            return null;
        }

        @Override
        public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
        }

        @Override
        public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String descriptor, boolean visible) {
            return null;
        }

        @Override
        public void visitLineNumber(int line, Label start) {

        }

        @Override
        public void visitMaxs(int maxStack, int maxLocals) {

        }

        @Override
        public void visitEnd() {
            super.visitEnd();
        }
    }
}
