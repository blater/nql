package blater.nql.parser;

import blater.nql.domain.HierarchyPath;
import blater.nql.parser.script.SelectBlueprint;

import java.util.List;

import static blater.nql.util.Log.fatal;

/** Validates ordering and shape constraints for explicit structure keys. */
final class SelectStructureValidator {
  private SelectStructureValidator() {
  }

  static void validate(HierarchyPath path, List<SelectBlueprint.StructureKey> existing) {
    if (path.isAttribute()) {
      fatal(HiqlSyntaxException.class,
          "structure keys must target object paths, not attributes: " + path);
    }
    if (existing.stream().anyMatch(key -> key.path().equals(path))) {
      fatal(HiqlSyntaxException.class, "duplicate structure path: " + path);
    }
    if (existing.stream().anyMatch(key -> path.isBelow(key.path()) == false
        && key.path().isBelow(path))) {
      fatal(HiqlSyntaxException.class,
          "structure paths must be declared parent before child: " + path);
    }
  }
}
