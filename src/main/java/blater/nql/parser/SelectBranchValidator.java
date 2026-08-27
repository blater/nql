package blater.nql.parser;

import blater.nql.domain.HierarchyPath;
import blater.nql.parser.script.SelectBlueprint;

import java.util.List;

import static blater.nql.util.Log.fatal;

/** Enforces the common document root required by hierarchy unions. */
final class SelectBranchValidator {
  private SelectBranchValidator() {
  }

  static void validateRoots(List<SelectBlueprint.Branch> branches) {
    String root = null;
    for (SelectBlueprint.Branch branch : branches) {
      for (SelectBlueprint.SelectItem item : branch.items()) {
        HierarchyPath path = item.outputPath();
        if (path == null) {
          continue;
        }
        if (root == null) {
          root = path.getRootName();
        } else if (!root.equals(path.getRootName())) {
          fatal(HiqlSyntaxException.class,
              "hierarchy union branches must share one document wrapper.");
        }
      }
    }
  }
}
