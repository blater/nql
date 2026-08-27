package blater.nql.runner.sql.cache;

import blater.jname.Jname;
import blater.jname.JnameOptions;
import blater.nql.cli.CacheName;
import blater.nql.cli.CacheNameSelection;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/** Allocates logical cache names and holds their creation claims. */
final class CacheFileAllocator {
  CachePreparation prepare(Path cacheDirectory, CacheNameSelection nameSelection) {
    Path root = CacheFileLayout.root(cacheDirectory);
    CacheFileLayout.createDirectories(root);
    return switch (nameSelection) {
      case CacheNameSelection.Generated ignored -> prepareGenerated(root);
      case CacheNameSelection.Named named -> prepareNamed(root, named.name());
    };
  }

  private CachePreparation prepareGenerated(Path root) {
    while (true) {
      CacheName name = new CacheName(generateCacheName());
      Path cacheFile = CacheFileLayout.cacheFile(root, name);
      if (!CacheFileLayout.hasArtifacts(cacheFile)) {
        CachePreparation preparation = tryClaim(cacheFile);
        if (preparation != null) {
          return preparation;
        }
      }
    }
  }

  private CachePreparation prepareNamed(Path root, CacheName name) {
    Path cacheFile = CacheFileLayout.cacheFile(root, name);
    if (CacheFileLayout.hasArtifacts(cacheFile)) {
      throw alreadyExists(name);
    }
    CachePreparation preparation = tryClaim(cacheFile);
    if (preparation == null) {
      throw new IllegalArgumentException("Cache is already being created: " + name.value());
    }
    if (CacheFileLayout.hasArtifacts(cacheFile)) {
      preparation.close();
      throw alreadyExists(name);
    }
    return preparation;
  }

  private CachePreparation tryClaim(Path cacheFile) {
    Path claimFile = Path.of(CacheFileLayout.databasePath(cacheFile) + ".claim");
    try {
      Files.writeString(
          claimFile,
          "nql cache creation in progress" + System.lineSeparator(),
          StandardCharsets.UTF_8,
          StandardOpenOption.CREATE_NEW,
          StandardOpenOption.WRITE);
      return new CachePreparation(
          new CacheHandle(cacheFile, CacheFileLayout.jdbcUrl(cacheFile), true), claimFile);
    } catch (FileAlreadyExistsException collision) {
      return null;
    } catch (IOException failure) {
      throw new IllegalStateException("Could not claim cache name: " + cacheFile, failure);
    }
  }

  private static IllegalArgumentException alreadyExists(CacheName name) {
    return new IllegalArgumentException("Cache already exists: " + name.value());
  }

  private static String generateCacheName() {
    return Jname.generate(JnameOptions.builder()
        .words(2)
        .maxLetters(8)
        .build());
  }
}
