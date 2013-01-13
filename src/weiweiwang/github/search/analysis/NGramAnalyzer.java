package weiweiwang.github.search.analysis;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.ngram.NGramTokenFilter;
import org.apache.lucene.util.Version;

import java.io.Reader;

/**
 * @author wangweiwei
 *         Date: 8/5/12
 *         Time: 4:46 PM
 */
public class NGramAnalyzer extends Analyzer {
    protected final Version matchVersion;
    private  int minGram;
    private int maxGram;
    public NGramAnalyzer(Version version,int minGram,int maxGram)
    {
        matchVersion = version;
        this.minGram = minGram;
        this.maxGram= maxGram;
    }

    @Override
    protected TokenStreamComponents createComponents(String s, Reader reader) {
        Tokenizer source = new WhitespaceTokenizer(matchVersion,reader);
        TokenStream filter = new NGramTokenFilter(new LowerCaseFilter(matchVersion,source),minGram,maxGram);
        return new TokenStreamComponents(source, filter);
    }
}