package com.ufo.mas.wsserver.datastation;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

public class Utils {

    public static int hex2Int(String hexStr) {
        return Integer.valueOf(hexStr, 16);
    }

    /**
     * 16进制的补码转10进制
     * 这里是4位的16进制 最大正数用7fff表示 最大负数用8000表示 ffff是负一
     * 所以用7fff即32767来划分用if判断大于这个即为负数
     * 如果大于7fff就用这个数-（ffff+1）；
     * @param hexStr
     * @return
     */
    public static int hex2bmInt(String hexStr){
        int x = Integer.parseUnsignedInt(hexStr, 16);
        if(x > 32767){ //32767是7fff的整型
            x -= 65536; //65536就是ffff+1的整型
        }
        return x;
    }

    public static double round(double v, int scale) {
        BigDecimal val = new BigDecimal(v);
        return val.setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }

    public static boolean isBitOn(int src, int idx) {
        int x = (int) (Math.pow(2, idx));
        return (src & x) == x;
    }

    public static String hexToAscii(String hex) {
        StringBuilder sb = new StringBuilder();
        //49204c6f7665204a617661 split into two characters 49, 20, 4c...
        for (int i = 0; i < hex.length() - 1; i += 2) {
            //grab the hex in pairs
            String output = hex.substring(i, (i + 2));
            //convert hex to decimal
            int decimal = Integer.parseInt(output, 16);
            //convert the decimal to character
            sb.append((char) decimal);
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        System.out.println(round(hex2bmInt("A")/100.0d, 2));
        System.out.println(round(hex2bmInt("EC19")/100.0d, 2));
        /*BigInteger bi = new BigInteger("FC17", 16);
        System.out.println(bi.intValue());
        System.out.println(Integer.valueOf("FC17", 16));
        System.out.println(isBitOn(3, 3));
        System.out.println(hexToAscii("50313553313030412D31323438302D312E3030"));
        System.out.println(round(hex2Int("0BB7") / 100.0d, 1));*/
    }

}
