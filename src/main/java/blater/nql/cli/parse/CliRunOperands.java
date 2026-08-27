package blater.nql.cli.parse;

import blater.nql.cli.ScriptSource;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Resolves named and positional script/data operands for the run command. */
final class CliRunOperands {
  static CliRunOperands read(CliParser.RawArguments raw) {
    String script = null;
    String data = null;
    List<String> unknown = new ArrayList<>();
    for (String positional : raw.positionals) {
      if (CliParser.isScriptFilename(positional)) {
        CliParser.reject(script != null || namedScript(raw),
            "positional script conflicts with another script source");
        script = positional;
      } else if (CliParser.isDataFilename(positional) || "-".equals(positional)) {
        CliParser.reject(data != null || namedData(raw),
            "positional data conflicts with another data source");
        data = positional;
      } else {
        unknown.add(positional);
      }
    }
    for (String positional : unknown) {
      if (script == null && !namedScript(raw)) {
        script = positional;
      } else if (data == null && !namedData(raw)) {
        data = positional;
      } else {
        throw CliParser.usage("run has too many positional operands");
      }
    }
    return new CliRunOperands(scriptSource(raw, script), data);
  }

  private static boolean namedScript(CliParser.RawArguments raw) {
    return raw.scriptFile != null || raw.scriptText != null;
  }

  private static boolean namedData(CliParser.RawArguments raw) {
    return raw.inputFile != null || raw.inputText != null;
  }

  private static ScriptSource scriptSource(CliParser.RawArguments raw, String positional) {
    if (raw.scriptFile != null) return new ScriptSource.File(Path.of(raw.scriptFile));
    if (raw.scriptText != null) return new ScriptSource.Text(raw.scriptText);
    if (positional == null) throw CliParser.usage("run requires a script");
    return CliParser.isScriptFilename(positional)
        ? new ScriptSource.File(Path.of(positional)) : new ScriptSource.Text(positional);
  }

  private final ScriptSource script;
  private final String data;

  private CliRunOperands(ScriptSource script, String data) {
    this.script = script;
    this.data = data;
  }

  ScriptSource script() {
    return script;
  }

  String data() {
    return data;
  }
}
