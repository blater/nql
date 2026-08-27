package blater.nql.cli.parse;

import blater.nql.cli.CatalogInvocation;
import blater.nql.cli.CatalogPattern;
import blater.nql.cli.ExecutionTarget;
import blater.nql.cli.InputSelection;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CliParserCatalogTest {
  private final CliParser parser = new CliParser();

  @Test
  void catalogWithoutInputUsesTheActiveCacheFallback() {
    var invocation = assertInstanceOf(
        CatalogInvocation.class,
        parser.parse("catalog", "*", "--cache-dir", "build/catalog-cache"));

    assertInstanceOf(InputSelection.Automatic.class, invocation.input());
    var pattern = assertInstanceOf(CatalogPattern.Matching.class, invocation.pattern());
    assertEquals("*", pattern.value());
    var target = assertInstanceOf(
        ExecutionTarget.InputOrActiveCache.class, invocation.target());
    assertEquals(
        Path.of("build/catalog-cache").toAbsolutePath().normalize(),
        target.cacheDirectory());
  }

  @Test
  void catalogRejectsTaskParametersWithoutInputData() {
    assertUsage("catalog", "*", "--param", "region=eu");
    assertUsage("catalog", "*", "--params-file", "params.json");
  }

  private void assertUsage(String... arguments) {
    assertThrows(CliUsageException.class, () -> parser.parse(arguments));
  }
}
