package blater.nql.parser;

import blater.nql.domain.HierarchyPath;

/** Mapping details attached to one SELECT expression. */
record SelectMapping(HierarchyPath path, boolean absentOnNull, String appendText) {
}
