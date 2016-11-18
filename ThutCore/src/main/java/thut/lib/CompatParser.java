package thut.lib;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class CompatParser
{

    public static class ClassFinder
    {

        private static final char   DOT               = '.';

        private static final char   SLASH             = '/';

        private static final String CLASS_SUFFIX      = ".class";

        private static final String BAD_PACKAGE_ERROR = "Unable to get resources from path '%s'. Are you sure the package '%s' exists?";

        public static List<Class<?>> find(String scannedPackage) throws UnsupportedEncodingException
        {
            String scannedPath = scannedPackage.replace(DOT, SLASH);
            URL scannedUrl = Thread.currentThread().getContextClassLoader().getResource(scannedPath);
            if (scannedUrl == null) { throw new IllegalArgumentException(
                    String.format(BAD_PACKAGE_ERROR, scannedPath, scannedPackage)); }
            File scannedDir = new File(
                    java.net.URLDecoder.decode(scannedUrl.getFile(), Charset.defaultCharset().name()));

            List<Class<?>> classes = new ArrayList<Class<?>>();
            if (scannedDir.exists()) for (File file : scannedDir.listFiles())
            {
                classes.addAll(findInFolder(file, scannedPackage));
            }
            else if (scannedDir.toString().contains("file:") && scannedDir.toString().contains(".jar"))
            {
                String name = scannedDir.toString();
                String pack = name.split("!")[1].replace(File.separatorChar, SLASH).substring(1) + SLASH;
                name = name.replace("file:", "");
                name = name.replaceAll("(.jar)(.*)", ".jar");
                scannedDir = new File(name);
                try
                {
                    ZipFile zip = new ZipFile(scannedDir);
                    Enumeration<? extends ZipEntry> entries = zip.entries();
                    int n = 0;
                    while (entries.hasMoreElements() && n < 10)
                    {
                        ZipEntry entry = entries.nextElement();
                        String s = entry.getName();
                        if (s.contains(pack) && s.endsWith(CLASS_SUFFIX))
                        {
                            try
                            {
                                classes.add(Class.forName(s.replace(CLASS_SUFFIX, "").replace(SLASH, DOT)));
                            }
                            catch (ClassNotFoundException ignore)
                            {
                            }
                        }
                    }
                    zip.close();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            return classes;
        }

        private static List<Class<?>> findInFolder(File file, String scannedPackage)
        {
            List<Class<?>> classes = new ArrayList<Class<?>>();
            String resource = scannedPackage + DOT + file.getName();
            if (file.isDirectory())
            {
                for (File child : file.listFiles())
                {
                    classes.addAll(findInFolder(child, resource));
                }
            }
            else if (resource.endsWith(CLASS_SUFFIX))
            {
                int endIndex = resource.length() - CLASS_SUFFIX.length();
                String className = resource.substring(0, endIndex);
                try
                {
                    classes.add(Class.forName(className));
                }
                catch (ClassNotFoundException ignore)
                {
                }
            }
            return classes;
        }

    }

    public static void findClasses(String classPackage,
            Map<CompatClass.Phase, Set<java.lang.reflect.Method>> initMethods)
    {
        List<Class<?>> foundClasses;
        try
        {
            foundClasses = ClassFinder.find(classPackage);
            for (Class<?> c : foundClasses)
            {
                CompatClass comp = null;
                for (java.lang.reflect.Method m : c.getMethods())
                {
                    if ((comp = m.getAnnotation(CompatClass.class)) != null)
                    {
                        initMethods.get(comp.phase()).add(m);
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
