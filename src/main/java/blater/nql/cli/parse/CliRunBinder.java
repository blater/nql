package blater.nql.cli.parse;

import blater.nql.cli.RunInvocation;

/** Binds run-specific script, data, execution-target, and output options. */
final class CliRunBinder {
  private CliRunBinder() {
  }

  static RunInvocation bind(CliBindingSupport support, CliParser.RawArguments raw) {
    return CliRunBinding.bind(support, raw);
  }
}
