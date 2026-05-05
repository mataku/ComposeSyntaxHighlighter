// web-tree-sitter references Node-only modules (fs, path) when its bundle is
// loaded by webpack. They are unused at runtime in the browser, so mark them
// as unavailable to keep the production bundle resolvable.
config.resolve = config.resolve || {};
config.resolve.fallback = config.resolve.fallback || {};
config.resolve.fallback.fs = false;
config.resolve.fallback.path = false;
