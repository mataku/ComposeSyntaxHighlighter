import io.github.treesitter.ktreesitter.Language;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Loads every generated TreeSitter* grammar binding found in the given jars and wraps it in a
 * ktreesitter Language, which loads the bundled native library and rejects an incompatible ABI.
 * Launched by scripts/verify-jvm-natives-load.sh.
 */
public class LoadGrammars {
  public static void main(String[] args) throws Exception {
    int loaded = 0;
    for (String jar : args) {
      List<String> classNames = grammarClassNames(jar);
      if (classNames.isEmpty()) {
        throw new IllegalStateException("No TreeSitter* grammar class in " + jar);
      }
      for (String className : classNames) {
        Class<?> binding = Class.forName(className);
        Object instance = binding.getField("INSTANCE").get(null);
        Language language = new Language(binding.getMethod("language").invoke(instance));
        System.out.println("OK: " + className + " (ABI " + language.getAbiVersion() + ")");
        loaded++;
      }
    }
    if (loaded == 0) {
      throw new IllegalStateException("No grammar loaded");
    }
  }

  private static List<String> grammarClassNames(String jar) throws Exception {
    List<String> names = new ArrayList<>();
    try (JarFile file = new JarFile(jar)) {
      for (JarEntry entry : Collections.list(file.entries())) {
        String name = entry.getName();
        String simpleName = name.substring(name.lastIndexOf('/') + 1);
        if (simpleName.startsWith("TreeSitter") && simpleName.endsWith(".class") && !simpleName.contains("$")) {
          names.add(name.substring(0, name.length() - ".class".length()).replace('/', '.'));
        }
      }
    }
    return names;
  }
}
