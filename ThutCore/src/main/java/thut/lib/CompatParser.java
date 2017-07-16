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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

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

            Set<Class<?>> classes = Sets.newHashSet();

            classes.addAll(findInFolder(new File("./mods/"), scannedPackage));

            if (scannedDir.exists()) for (File file : scannedDir.listFiles())
            {
                classes.addAll(findInFolder(file, scannedPackage));
            }
            return Lists.newArrayList(classes);
        }

        private static List<Class<?>> findInFolder(File file, String scannedPackage)
        {
            List<Class<?>> classes = new ArrayList<Class<?>>();

            if (file.toString().endsWith(".jar"))
            {
                try
                {
                    String name = file.toString();
                    String pack = scannedPackage.replace(DOT, SLASH) + SLASH;
                    name = name.replace("file:", "");
                    name = name.replaceAll("(.jar)(.*)", ".jar");
                    file = new File(name);
                    ZipFile zip = new ZipFile(file);
                    Enumeration<? extends ZipEntry> entries = zip.entries();
                    while (entries.hasMoreElements())
                    {
                        ZipEntry entry = entries.nextElement();
                        String s = entry.getName();
                        if (s.startsWith(pack) && s.endsWith(CLASS_SUFFIX))
                        {
                            try
                            {
                                classes.add(Class.forName(s.replace(CLASS_SUFFIX, "").replace(SLASH, DOT)));
                            }
                            catch (Throwable ignore)
                            {
                            }
                        }
                    }
                    zip.close();
                }
                catch (Throwable e)
                {
                    e.printStackTrace();
                }
            }
            else
            {
                String resource = file.toString().replaceAll("\\" + System.getProperty("file.separator"), ".");
                if (resource.indexOf(scannedPackage) != -1)
                    resource = resource.substring(resource.indexOf(scannedPackage), resource.length());
                if (file.isDirectory())
                {
                    for (File child : file.listFiles())
                    {
                        classes.addAll(findInFolder(child, scannedPackage));
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
                    catch (Throwable ignore)
                    {
                        System.out.println(ignore);
                    }
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
                try
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
                catch (Throwable e)
                {
                    System.err.println("Error with " + c);
                }
            }
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }
}
