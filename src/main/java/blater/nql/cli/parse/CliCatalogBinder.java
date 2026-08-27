package blater.nql.cli.parse;

import blater.nql.cli.CatalogInvocation;
/** Binds catalog-specific arguments after parsing and ownership validation. */
final class CliCatalogBinder {
  private CliCatalogBinder() {
  }

  static CatalogInvocation bind(CliBindingSupport support, CliParser.RawArguments raw) {
    return CliCatalogBinding.bind(support, raw);
  }
}
