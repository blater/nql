package blater.nql.parser;

/** Optional metadata attached to a SELECT statement. */
record SelectUsingMetadata(boolean schemaOrRootPresent, String namespace) {
  boolean hasValues() {
    return schemaOrRootPresent || namespace != null;
  }
}
