package util;

import java.util.Random;

public class CodeUtil {
    private static final String LETTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"; // 字母表
    private static final String DIGITS = "0123456789";
    private static final Random RANDOM = new Random();

    private CodeUtil() {
    }

    public static String getCode() {
        return getCode(4, 1);
    }

    public static String getCode(int numLetters, int numDigits) {
        // 直接创建字符数组，提前确定长度，避免使用StringBuilder的多次扩容
        char[] resultArray = new char[numLetters + numDigits];
        // 1. 使用StringBuilder存储结果

        // 生成随机字母
        for (int i = 0; i < numLetters; i++) {
            resultArray[i] = LETTERS.charAt(RANDOM.nextInt(LETTERS.length()));
        }

        // 生成随机数字
        for (int i = 0; i < numDigits; i++) {
            resultArray[numLetters + i] = DIGITS.charAt(RANDOM.nextInt(DIGITS.length()));
        }

        // 拿着索引上的数字，跟随机索引上的数字进行交换 --> 洗牌算法。
        for (int i = resultArray.length - 1; i > 0; i--) {
            int posi = RANDOM.nextInt(i + 1);
            char aux = resultArray[i];
            resultArray[i] = resultArray[posi];
            resultArray[posi] = aux;
        }
        // 把字符数组再变回字符串
        return new String(resultArray);
    }
}
