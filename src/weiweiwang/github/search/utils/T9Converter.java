package weiweiwang.github.search.utils;

/**
 * Created with IntelliJ IDEA.
 * User: wangweiwei
 * Date: 13-1-8
 * Time: 下午7:17
 * To change this template use File | Settings | File Templates.
 */
public final class T9Converter {
    private static final char[] T9 = "22233344455566677778889999".toCharArray();

    public static char convert(char c) {
        if (c >= 'a' && c <= 'z') {
            return T9[c - 'a'];
        } else {
            return c;
        }
    }

    public static String convert(String input)
    {
        StringBuilder stringBuilder = new StringBuilder(input.length());
        for(int i=0;i<input.length();i++)
        {
            stringBuilder.append(convert(input.charAt(i)));
        }
        return stringBuilder.toString();
    }

}
