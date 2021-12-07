import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.lang.instrument.IllegalClassFormatException;

public class MyTransformClass implements ClassFileTransformer {

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer)
            throws IllegalClassFormatException {
        // 定义重新编译之后的字符流数组
        byte[] newClassFileBuffer = new byte[classfileBuffer.length];
        String transClassName = "com.mrhu.opin.controller.TestController";//重定义指定类，也可以重定义指定package下的类，使用者自由发挥
        if (className.equals(transClassName)) {
            System.out.println("监控到目标类,重新编辑Class文件字符流...");
            // TODO 对目标类的Class文件字节流进行重新编辑
            // 对byte[]重新编译可以使用第三方工具如javassist,感兴趣的可自行研究
            // 本文图方便，直接返回旧的字节数组
            newClassFileBuffer = classfileBuffer;
        }
        return newClassFileBuffer;
    }

}