package com.benayn.ustyle;

import java.io.IOException;

import org.apache.commons.codec.digest.DigestUtils;

import sun.misc.BASE64Decoder;


public class SixtytwoScale {
        public static final String Keys = "oTu8VgY4PAhjL7Xz9QWmUxBc3S5qFaCsIiOGtZ6yHwDr1JKvdf2EepRbnNklM";// 0用作分割符号了，补充了_
        private static final int radix = 61;
        private static final char separatorChar = '0';

        /**
         * 62进制数向10进制转换
         * 
         * @param value
         * @return
         */
        public static long SixtyTwoScale(String value) {
                int idxSep = value.indexOf(separatorChar);
                if (idxSep > -1) {
                        value = value.substring(idxSep + 1);
                }
                int length = value.length();
                long result = 0;
                for (int i = 0; i < length; i++) {
                        long val = (long) Math.pow(radix, (length - i - 1));
                        char c = value.charAt(i);
                        int tmp = Keys.indexOf(c);
                        result += (tmp * val);
                }
                return result;
        }
        public static byte[] fromBase64(String value) throws IOException{
                BASE64Decoder decoder = new BASE64Decoder();
                //Base64解码
                byte[] b = decoder.decodeBuffer(value);
                for(int i=0;i<b.length;++i){
                    if(b[i]<0){//调整异常数据
                        b[i]+=256;
                    }
                }
                return b;
        }
        /**
         * 10进制向62进制转换
         * 
         * @param value
         * @return
         */
        public static String SixtyTwoScale(long value) {
                return SixtyTwoScale(value,8);
        }

        /**
         * 返回62进制数据，并且补充填充字符
         * 
         * @param value
         * @param totalLen
         * @return
         */
        public static String SixtyTwoScale(long value, int totalLen) {
                if (value == 0) 
                        return null;
                String result = "";
                while (value > 0) {
                        long val = value % radix;
                        result = Keys.charAt((int) val) + result;
                        value = value / radix;
                }
                int resultLen = result.length();
            if (resultLen == (totalLen - 1)) {
                        result = separatorChar + result;
                } else {
                        result = fillChar(result, totalLen - 1 - resultLen) + separatorChar
                                        + result;
                }
                return result;
        }

        private static String fillChar(String str, int len) {
                String uuid = DigestUtils.md5Hex(str).replace('0', 'a');
                return uuid.substring(uuid.length() - len);
        }

}