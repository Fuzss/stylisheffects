package fuzs.stylisheffects;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.reflect.ClassPath;
import fuzs.puzzleslib.PuzzlesLib;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * mixin config plugin implementation for discovering all mixin classes on it's own
 * also checks if mixins can actually be applied, only really suppresses a warning though
 */
public interface IPuzzlesMixinConfigPlugin extends IMixinConfigPlugin {

    @Override
    default void onLoad(String mixinPackage) {

    }

    @Override
    default String getRefMapperConfig() {

        return null;
    }

    @Override
    default boolean shouldApplyMixin(String targetClassName, String mixinClassName) {

        try {

            // will throw an exception when class is not found
            Class.forName(targetClassName);
            return true;
        } catch (ClassNotFoundException ignored) {

        }

        return false;
    }

    @Override
    default void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    default List<String> getMixins() {

        List<String> mixinClasses = null;
        String packageName = this.getClass().getPackage().getName();
        try {
            mixinClasses = getClasses(this.getClass(), packageName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert mixinClasses != null;
        mixinClasses = mixinClasses.stream().map(s -> s.substring(packageName.length() + 1)).collect(Collectors.toList());
        PuzzlesLib.LOGGER.info("pre remove {}", mixinClasses);
        // remove self
        mixinClasses.removeIf(s -> s.equals(this.getClass().getSimpleName()));
        // mixin classes are sorted into sub-packages depending on dist compatibility
        if (FMLEnvironment.dist.isClient()) {

            mixinClasses.removeIf(s -> s.startsWith("server"));
        } else if (FMLEnvironment.dist.isDedicatedServer()) {

            mixinClasses.removeIf(s -> s.startsWith("client"));
        }

        PuzzlesLib.LOGGER.info("post remove {}", mixinClasses);
        return mixinClasses;
    }

    @Override
    default void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    default void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @SuppressWarnings("UnstableApiUsage")
    static List<String> getAllClasses(ClassLoader loader, final String pack) {

        try {

            final int length = pack.length();
            return ClassPath.from(loader).getTopLevelClassesRecursive(pack).stream()
                    .map(ClassPath.ClassInfo::getName)
                    // one more for "."
                    .map(s -> s.substring(length + 1))
                    .collect(Collectors.toList());
        } catch (IOException e) {

            PuzzlesLib.LOGGER.error("Failed to find mixin classes: ", e);
        }

        return Lists.newArrayList();
    }

    static List<String> getAllClasses(Class<?> clazz, final String pack, final String subpack) {

        PuzzlesLib.LOGGER.info("{}, {}, {}", clazz, pack, subpack);
        List<String> mixinClasses = new ArrayList<>();
        String currentPack = subpack.isEmpty() ? pack : pack.concat("/" + subpack);
        try {

            URL upackages = clazz.getResource(currentPack.replaceAll("[.]", "/"));
            PuzzlesLib.LOGGER.info("upackacge {}", upackages);
            assert upackages != null;
//            while (upackages.hasMoreElements()) {
//                URL resource = upackages.nextElement();
//                mixinClasses.add(resource.getFile());
//            }
//            BufferedReader reader = new BufferedReader(new InputStreamReader((InputStream) upackage.getContent()));
//            String line;
//            while ((line = reader.readLine()) != null) {
//
//                if (line.endsWith(".class")) {
//
//                    line = line.substring(0, line.length() - ".class".length());
//                    mixinClasses.add(subpack.isEmpty() ? line : subpack.concat("." + line));
//                    continue;
//                }
//
//                if (new File(upackage.getPath() + File.separator + line).isDirectory()) {
//
//                    mixinClasses.addAll(getAllClasses(loader, pack, subpack.isEmpty() ? line : subpack.concat("." + line)));
//                }
//            }

        } catch (Exception ignored) {

            PuzzlesLib.LOGGER.error("upackacge ", ignored);
        }

        return mixinClasses;
    }

    /**
     * Scans all classes accessible from the context class loader which belong to the given package and subpackages.
     *
     * @param packageName The base package
     * @return The classes
     * @throws ClassNotFoundException
     * @throws IOException
     */
    static ArrayList<String> getClasses(Class<?> clazz, String packageName)
            throws ClassNotFoundException, IOException {
//        ImmutableSet<ClassPath.ClassInfo> allClasses = ClassPath.from(clazz.getClassLoader()).getTopLevelClasses();
        ImmutableSet<ClassPath.ResourceInfo> resources = ClassPath.from(clazz.getClassLoader()).getResources();
        try {
            final File f = new File(clazz.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            PuzzlesLib.LOGGER.info("file {}", f.getAbsolutePath());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
//        PuzzlesLib.LOGGER.info("allClasses {}", allClasses);
        PuzzlesLib.LOGGER.info("resources {}", resources);
        PuzzlesLib.LOGGER.info("dhjkfhskjdhfjks {}", new File("hi").getAbsolutePath());
        String path = packageName.replace('.', '/');
        Set<ModFileScanData.ClassData> stylisheffects = ModList.get().getModFileById("stylisheffects").getFile().getScanResult().getClasses();
        URL resource = clazz.getResource("/" + path);
        List<File> dirs = new ArrayList<File>();
        dirs.add(new File(resource.getFile()));
        PuzzlesLib.LOGGER.info("getFile {}", resource.getFile());
//        while (resources.hasMoreElements()) {
//            URL resource = resources.nextElement();
//            dirs.add(new File(resource.getFile()));
//        }
        PuzzlesLib.LOGGER.info("dirs {}", dirs);
        ArrayList<String> classes = new ArrayList<>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return classes;
    }

    /**
     * Recursive method used to find all classes in a given directory and subdirs.
     *
     * @param directory   The base directory
     * @param packageName The package name for classes found inside the base directory
     * @return The classes
     * @throws ClassNotFoundException
     */
    static List<String> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<String> classes = new ArrayList<>();
        if (!directory.exists()) {
            PuzzlesLib.LOGGER.info("dir doesnt exist {}", directory);
            return classes;
        }
        File[] files = directory.listFiles();
        PuzzlesLib.LOGGER.info(Arrays.toString(files));
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(packageName + '.' + file.getName().substring(0, file.getName().length() - ".class".length()));
            }
        }
        return classes;
    }

}
