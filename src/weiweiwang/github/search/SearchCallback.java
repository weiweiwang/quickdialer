package weiweiwang.github.search;

import java.util.List;
import java.util.Map;

public interface SearchCallback {
    // runs in background
    void onSearchResult(String query, long hits, List<Map<String, Object>> result);
}
