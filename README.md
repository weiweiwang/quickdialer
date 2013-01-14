# quickdialer

A quick dialer for android users,functions include: fast T9 search, support 5000 contacts+500 calllogs

## index tools

I use lucene 4.0 in this project. Instead of using lucene-core.jar, I copy the lucene-core source code to my project as I have to deal with  ServiceLoader used in lucene 4.0 which locate services under META-INF/services.

## analyzer

phone number: NGramAnalyzer
contact name: convert to pinyin first(include the full pinyin and the first letters) first, then tokenize with LetterTokenizer, and filter with lowercase,t9,edgengram filter, please see PiniyinAnalyzer for detail information.

## search

I use MultiFieldQueryParser to query on multiple fields and set different boost according to the input pattern


## highlight

As I use ngram and edgengram, the lucene highlighter works very poor on mobile devices, as it need to iterate all tokens to form fragments. As a result, under the assumption that the input is always the substring of the tokenized terms in the index, I just do highlight with indexOf and substring, this method is a little bit ugly but runs very fast on mobile devices.



