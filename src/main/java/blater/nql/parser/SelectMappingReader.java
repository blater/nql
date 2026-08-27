package blater.nql.parser;

import blater.nql.core.parser.HiQLParser;
import blater.nql.domain.HierarchyPath;

import static blater.nql.util.ValueUtil.hasValue;

/** Reads a mapping alias and translates its append command. */
final class SelectMappingReader {
  private SelectMappingReader() {
  }

  static SelectMapping read(HiQLParser.MappingAliasContext context) {
    if (context == null) {
      return null;
    }
    String command = null;
    String argument = null;
    if (hasValue(context.name())) {
      command = context.name(0).getText();
      argument = context.name().size() > 1 ? context.name(1).getText() : null;
    }
    HierarchyPath path = PathContextMapper.toHierarchyPath(context.path());
    return new SelectMapping(path, context.nullOutputPolicy() != null,
        appendText(command, argument));
  }

  private static String appendText(String command, String argument) {
    if (!"append".equals(command) || argument == null) {
      return null;
    }
    return switch (argument.toLowerCase()) {
      case "space" -> " ";
      case "dash" -> "-";
      case "comma" -> ",";
      case "newline" -> "\n";
      default -> null;
    };
  }
}
