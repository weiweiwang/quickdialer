
package weiweiwang.github.search.utils;

import android.content.Context;
import weiweiwang.github.quickdialer.R;

import java.io.DataInputStream;
import java.io.IOException;


public class PinyinConverter {

    private static final String[] PINYIN = {
            "a", "ai", "an", "ang", "ao", "ba", "bai", "ban", "bang", "bao",
            "bei", "ben", "beng", "bi", "bian", "biao", "bie", "bin", "bing", "bo",
            "bu", "ca", "cai", "can", "cang", "cao", "ce", "cen", "ceng", "cha",
            "chai", "chan", "chang", "chao", "che", "chen", "cheng", "chi", "chong", "chou",
            "chu", "chua", "chuai", "chuan", "chuang", "chui", "chun", "chuo", "ci", "cong",
            "cou", "cu", "cuan", "cui", "cun", "cuo", "da", "dai", "dan", "dang",
            "dao", "de", "dei", "den", "deng", "di", "dia", "dian", "diao", "die",
            "ding", "diu", "dong", "dou", "du", "duan", "dui", "dun", "duo", "e",
            "ei", "en", "eng", "er", "fa", "fan", "fang", "fei", "fen", "feng",
            "fiao", "fo", "fou", "fu", "ga", "gai", "gan", "gang", "gao", "ge",
            "gei", "gen", "geng", "gong", "gou", "gu", "gua", "guai", "guan", "guang",
            "gui", "gun", "guo", "ha", "hai", "han", "hang", "hao", "he", "hei",
            "hen", "heng", "hm", "hng", "hong", "hou", "hu", "hua", "huai", "huan",
            "huang", "hui", "hun", "huo", "ji", "jia", "jian", "jiang", "jiao", "jie",
            "jin", "jing", "jiong", "jiu", "ju", "juan", "jue", "jun", "ka", "kai",
            "kan", "kang", "kao", "ke", "kei", "ken", "keng", "kong", "kou", "ku",
            "kua", "kuai", "kuan", "kuang", "kui", "kun", "kuo", "la", "lai", "lan",
            "lang", "lao", "le", "lei", "leng", "li", "lia", "lian", "liang", "liao",
            "lie", "lin", "ling", "liu", "lo", "long", "lou", "lu", "luan", "lue",
            "lun", "luo", "lv", "m", "ma", "mai", "man", "mang", "mao", "me",
            "mei", "men", "meng", "mi", "mian", "miao", "mie", "min", "ming", "miu",
            "mo", "mou", "mu", "n", "na", "nai", "nan", "nang", "nao", "ne",
            "nei", "nen", "neng", "ng", "ni", "nian", "niang", "niao", "nie", "nin",
            "ning", "niu", "nong", "nou", "nu", "nuan", "nue", "nuo", "nv", "o",
            "ou", "pa", "pai", "pan", "pang", "pao", "pei", "pen", "peng", "pi",
            "pian", "piao", "pie", "pin", "ping", "po", "pou", "pu", "qi", "qia",
            "qian", "qiang", "qiao", "qie", "qin", "qing", "qiong", "qiu", "qu", "quan",
            "que", "qun", "ran", "rang", "rao", "re", "ren", "reng", "ri", "rong",
            "rou", "ru", "ruan", "rui", "run", "ruo", "sa", "sai", "san", "sang",
            "sao", "se", "sen", "seng", "sha", "shai", "shan", "shang", "shao", "she",
            "shei", "shen", "sheng", "shi", "shou", "shu", "shua", "shuai", "shuan", "shuang",
            "shui", "shun", "shuo", "si", "song", "sou", "su", "suan", "sui", "sun",
            "suo", "ta", "tai", "tan", "tang", "tao", "te", "tei", "teng", "ti",
            "tian", "tiao", "tie", "ting", "tong", "tou", "tu", "tuan", "tui", "tun",
            "tuo", "wa", "wai", "wan", "wang", "wei", "wen", "weng", "wo", "wu",
            "xi", "xia", "xian", "xiang", "xiao", "xie", "xin", "xing", "xiong", "xiu",
            "xu", "xuan", "xue", "xun", "ya", "yan", "yang", "yao", "ye", "yi",
            "yin", "ying", "yo", "yong", "you", "yu", "yuan", "yue", "yun", "za",
            "zai", "zan", "zang", "zao", "ze", "zei", "zen", "zeng", "zha", "zhai",
            "zhan", "zhang", "zhao", "zhe", "zhei", "zhen", "zheng", "zhi", "zhong", "zhou",
            "zhu", "zhua", "zhuai", "zhuan", "zhuang", "zhui", "zhun", "zhuo", "zi", "zong",
            "zou", "zu", "zuan", "zui", "zun", "zuo",
    };

    private static PinyinConverter sInst = null;

    public synchronized static PinyinConverter getInstance(Context context) {
        if (sInst == null) {
            sInst = new PinyinConverter(context.getApplicationContext());
        }
        return sInst;
    }

    private short[] mHanzi;
    private short mOffset;

    private PinyinConverter(Context context) {
        DataInputStream is = new DataInputStream(context.getResources()
                .openRawResource(R.raw.hanzi));
        try {
            short sz = is.readShort();
            mOffset = is.readShort();
            mHanzi = new short[sz];
            for (int i = 0; i < sz; i++) {
                mHanzi[i] = is.readShort();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getPinyin(char c) {
        int idx = c - mOffset;
        if (idx >= 0 && idx < mHanzi.length) {
            int idx1 = mHanzi[idx];
            if (idx1 >= 0 && idx1 < PINYIN.length) {
                return PINYIN[idx1];
            }
        }
        return null;
    }

    public String convert(String input) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            String py = getPinyin(c);
            if (py != null) {
                sb.append(StringUtils.capitalize(py));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
    
    
    public String convert(String input, boolean appendFirstLetter) {
        StringBuilder sb = new StringBuilder();
        StringBuilder firstLetters = new StringBuilder();
//        boolean previousHanzi = true;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            String py = getPinyin(c);
            if (py != null) {
                String capitializedPy = StringUtils.capitalize(py);
                sb.append(capitializedPy);
                if (appendFirstLetter) {
                    firstLetters.append(capitializedPy.charAt(0));
                }
//                previousHanzi = true;
            } else {
//                if (previousHanzi&&sb.length()>0) {
//                    sb.append('|');
//                }
                sb.append(c);
//                if (!Character.isLetter(c) && appendFirstLetter) {
//                    firstLetters.append(c);
//                }
//                previousHanzi = false;
            }
        }
        if (firstLetters.length() > 0) {
            sb.append('|').append(firstLetters);
        }
        return sb.toString();
    }

}
