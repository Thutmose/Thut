package thut.api.blocks.multiparts;

import java.io.InputStream;
import java.net.URLClassLoader;

import net.minecraft.launchwrapper.LaunchClassLoader;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;


public class ByteClassLoader extends ClassLoader {
	String resName;
	Class superClass;
	public ByteClassLoader(Class superClass) {
		super(superClass.getClassLoader());
		resName = superClass.getSimpleName()+".class";
		this.superClass = superClass;
	}

//	@Override
	public Class<?> loadClass(String name, byte[] jarBytes, boolean resolve) throws ClassNotFoundException {
		name = name.replace("/", ".");
	    Class<?> clazz = null;
		try {
			clazz = super.loadClass(name, false);
		} catch (Exception e1) {
			
		}
	    if (clazz == null) {
	        try {
	            byte[] bytes = jarBytes;
	            clazz = defineClass(name, bytes, 0, bytes.length);
	            if (resolve) {
	                resolveClass(clazz);
	            }
	        } catch (Exception e) {
	            clazz = super.loadClass(name, resolve);
	        }
	    }
	    return clazz;
	}
	
	private byte[] genericClassBytes;
	
	public Class<?> generateClass(String identifier, String... args) throws ClassNotFoundException
	{
		try {
			InputStream is = superClass.getResourceAsStream(resName);
			ClassReader reader = new ClassReader(is);
			genericClassBytes = reader.b.clone();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		ClassReader reader = new ClassReader(genericClassBytes);
		ClassWriter writer;
		byte[] genericMob = reader.b.clone();
		byte[] old = genericMob.clone();
		
		ClassNode changer = new ClassNode();
		reader.accept(changer, 0);

		changer.sourceFile = changer.sourceFile.replace(".java", "")+identifier+".java";
		changer.name = changer.name+identifier;
		
		writer = new ClassWriter(ClassWriter.COMPUTE_MAXS|ClassWriter.COMPUTE_FRAMES);
		changer.accept(writer);
		writer.visitEnd();
		genericMob = writer.toByteArray();
        
        ClassReader cr=new ClassReader(genericMob);
        ClassNode classNode=new ClassNode();
        cr.accept(classNode, 0);
        
		Class c = loadClass(classNode.name, genericMob, true);
		
		return c;
		
		
	}

}
