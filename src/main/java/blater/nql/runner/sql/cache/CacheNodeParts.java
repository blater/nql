package blater.nql.runner.sql.cache;

import blater.nql.domain.Node;

import java.util.List;
import java.util.Map;
import java.util.Set;

/** Classified direct children of one hierarchy node. */
record CacheNodeParts(
    Map<String, List<CacheScalarValue>> valuesByName,
    Set<String> repeatedNames,
    List<Node> objectChildren) {
}
