package weiweiwang.github.search.analysis;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LetterTokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.ngram.EdgeNGramTokenFilter;
import org.apache.lucene.util.Version;

import java.io.Reader;

/**
 * @author wangweiwei
 *         Date: 8/5/12
 *         Time: 4:46 PM
 */
final public class PinyinAnalyzer extends Analyzer {
//    private static NormalizeCharMap NORMALIZE_CHAR_MAP = null;
//
//    static {
//        NormalizeCharMap.Builder builder = new NormalizeCharMap.Builder();
//        for (char c = 'a'; c <= 'z'; c++) {
//            builder.add(String.valueOf(c), "");
//        }
//        NORMALIZE_CHAR_MAP = builder.build();
//    }

    protected final Version matchVersion;
    protected final boolean convertToT9;
//    protected final boolean lowercaseFilter;

    public PinyinAnalyzer(Version version, /*boolean lowercaseFilter,*/ boolean convertToT9) {
        matchVersion = version;
//        this.lowercaseFilter = lowercaseFilter;
        this.convertToT9 = convertToT9;
    }

    @Override
    protected TokenStreamComponents createComponents(String s, Reader reader) {
        Tokenizer source = new LetterTokenizer(matchVersion, reader);
        TokenStream filter =  new LowerCaseFilter(matchVersion, source);
        if (convertToT9) {
            filter = new T9Filter(matchVersion, filter);
        }
        filter = new EdgeNGramTokenFilter(filter, EdgeNGramTokenFilter.Side.FRONT, 1, 10);
        return new TokenStreamComponents(source, filter);
    }

    /**
     * Override this if you want to add a CharFilter chain.
     * <p/>
     * The default implementation returns <code>reader</code>
     * unchanged.
     *
     * @param fieldName IndexableField name being indexed
     * @param reader    original Reader
     * @return reader, optionally decorated with CharFilter(s)
     */
    protected Reader initReader(String fieldName, Reader reader) {
//        if (lowercaseFilter) {
//            return new MappingCharFilter(NORMALIZE_CHAR_MAP, reader);
//        }
        return reader;
    }
}