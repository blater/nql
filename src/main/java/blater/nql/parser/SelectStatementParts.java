package blater.nql.parser;

import blater.nql.parser.script.SelectBlueprint;

/** Immutable intermediate representation shared by SELECT assembly steps. */
record SelectStatementParts(
    SelectUsingMetadata using,
    SelectBlueprint blueprint,
    boolean hasHierarchyFields) {
}
