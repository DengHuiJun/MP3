
package com.zero.mp3.Utils;

/**
 * 获取汉字首字母
 * Author: Minggui.He
 * Company: feidee
 * Time: 2014年8月22日 上午11:00:08
 * Version: V1.0
 */
public class FirstLetterUtil {
    private static int BEGIN = 45217;
    private static int END = 63486;
    // 按照声母表示，这个表是在GB2312中的出现的第一个汉字，也就是说“啊”是代表首字母a的第一个汉字。
    // i, u, v都不做声母, 自定规则跟随前面的字母
    private static char[] chartable = {
            '啊', '芭', '擦', '搭', '蛾', '发', '噶', '哈',
            '哈', '击', '喀', '垃', '妈', '拿', '哦', '啪', '期', '然', '撒', '塌', '塌',
            '塌', '挖', '昔', '压', '匝',
    };
    // 二十六个字母区间对应二十七个端点
    // GB2312码汉字区间十进制表示
    private static int[] table = new int[27];
    // 对应首字母区间表
    private static char[] initialtable = {
            'a', 'b', 'c', 'd', 'e', 'f', 'g',
            'h', 'h', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
            't', 't', 'w', 'x', 'y', 'z',
    };

    // 初始化
    static {
        for (int i = 0; i < 26; i++) {
            table[i] = gbValue(chartable[i]);// 得到GB2312码的首字母区间端点表，十进制。
        }
        table[26] = END;// 区间表结尾
    }

    /**
     * 根据一个包含汉字的字符串返回一个汉字拼音首字母的字符串 最重要的一个方法，思路如下：一个个字符读入、判断、输出
     * 有多音字的时候，结果以逗号分割如：曾返回c,z
     */
    public static String getFirstLetter(String sourceStr) {
        String result = "";
        String str = sourceStr.toLowerCase();
        
        try {
            PinYin4j pinyin = new PinYin4j();
            result = pinyin.getPinyin(str).toString();
            result = result.replaceAll("[ \\[\\]]", "");
        } catch (Exception e) {
            result = "";
        }
        return result;
    }


    /**
     * 取出汉字的编码 cn 汉字
     */
    private static int gbValue(char ch) {// 将一个汉字（GB2312）转换为十进制表示。
        String str = new String();
        str += ch;
        try {
            byte[] bytes = str.getBytes("GB2312");
            if (bytes.length < 2) {
                return 0;
            }
            return (bytes[0] << 8 & 0xff00) + (bytes[1] & 0xff);
        } catch (Exception e) {
            return 0;
        }
    }
}
