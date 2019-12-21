package com.visionvera.util;


import org.apache.commons.lang3.StringUtils;


/**
 * 设备号11位转20位
 */

public class ChangeNumberUtils { //2019/2/20 周逸芳合并16位与64位代码时增加 原16位没有


    /**
     * 获取前面的号码
     * @param ownerNumber    用户自己的号码，20位
     * @param prefixCount   取前多少位，只能是5或10或15
     * @return               取出的前多少位的结果
     */
    public static String getPrefix(String ownerNumber, int prefixCount){
        if (StringUtils.isBlank(ownerNumber)){
            throw new IllegalArgumentException("设备号为空");
        }else {
            ownerNumber = ownerNumber.replaceAll(" ", "");
            if (ownerNumber.length() != 20){
                throw new IllegalArgumentException("The number lenght is not 20..");
            }else {
                if (prefixCount == 5 || prefixCount == 10 || prefixCount == 15) {
                    return ownerNumber.substring(0, prefixCount);
                }else {
                    throw new IllegalArgumentException("The prefix count must be 5 or 10 or 15..");
                }
            }
        }
    }

    /**
     * 格式化number
     * @param number
     * @return
     */
    public static String numberFormcat(String number){
        if (StringUtils.isBlank(number)){
            throw new IllegalArgumentException("设备号为空");
        }
        number = number.replaceAll(" ", "");
        number = number.replaceAll("-", "");
        if (number.matches("[0]{" + number.length() + "}")){
            throw new IllegalArgumentException("The numer can't be all 0..");
        }

        if (number.length() == 5 || number.length() == 8
                || number.length() == 11 || number.length() == 14
                || number.length() == 20){

            if (number.length() == 5){
                // 输入5位号码 77891 时 将号码自动转换为00256 00256 00930 77891
                return number;
            } else if (number.length() == 8){
                //处理成10位
                if (number.endsWith("00000")){
                    throw new IllegalArgumentException("The number can't be end with 00000..");
                }

                if (number.startsWith("000")){
                    String before5 = "00000";
                    String end5 =  number.replaceFirst("000", "");
                    number = end5 + before5;
                }else {
                    number = "00" + number;
                }

                return number;
            } else if (number.length() == 11){
                //处理成15位长度
                if (number.endsWith("00000")){
                    throw new IllegalArgumentException("The number can't be end with 00000..");
                }

                String str1 = number.substring(0, 3);
                String str2 = number.substring(3, 6);
                String str3 = number.substring(6);

                if (str1.equals("000")){
                    //000 000 12345
                    if (str2.equals("000")){
                        number = str3 + "0000000000";
                        return number;
                    }else {
                        throw new IllegalArgumentException("设备号有误");
                    }
                }else {
                    if (str2.equals("000")){
                        number = "00" + str1 + str3 + "00000";
                        return number;
                    }else {
                        number = "00" + str1 + "00" + str2 + str3;
                        return number;
                    }
                }
            } else if (number.length() == 14){
                //处理成20位长度
                String str1 = number.substring(0, 3);
                String str2 = number.substring(3, 6);
                String str3 = number.substring(6, 9);
                String str4 = number.substring(9);

                return "00" + str1 + "00" + str2 + "00" + str3 + str4;
            } else {
                //20位不处理，直接返回
                return number;
            }
        }else {
            throw new IllegalArgumentException("The number length must be 5 or 8 or 11 or 14 or 20..");
        }
    }

    public static String getDeviceTo20(String number){
    	return numberTo20("",number) ;
    }
    
    public static String numberTo20(String ownerNumber, String number){
    	if(StringUtils.isBlank(ownerNumber)){
    		//从配置文件里面读取源设备号
    		ownerNumber = Util.getSysProp("srcDeviceNumber20");
    	}
    	ownerNumber = ownerNumber.replaceAll("-", "") ;
        if (StringUtils.isBlank(number)){
            throw new IllegalArgumentException("设备号为空");
        }

        number = numberFormcat(number);

        if (number.length() == 5){
            String prefix = getPrefix(ownerNumber, 20 - number.length());
            number = prefix + number;
        } else if (number.length() == 10){
            number = getPrefix(ownerNumber, 20 - number.length()) + number;
        } else if (number.length() == 15){
            number = getPrefix(ownerNumber, 20 - number.length()) + number;
        } else {

        }
        String s1 = number.substring(0, 5);
        String s2 = number.substring(5, 10);
        String s3 = number.substring(10, 15);
        String s4 = number.substring(15);
        return s1 + "-" + s2 + "-" + s3 + "-" + s4;
    }




    public static void main(String[] args){
//        System.out.println("--->" + getPrefix5(null));
//        System.out.println("--->" + getPrefix5(""));
//        System.out.println("--->" + getPrefix5("123456"));
//        System.out.println("--->" + getPrefix5("1234567890asdfghjklzx"));
//        System.out.println("--->" + getPrefix("1234567890asdfghjklz", 5));

    	System.err.println("+++++"+numberTo20("00100000240185500000","02400000012")) ;
/*      String ownerNumber = "00256-00256-00930-12345";
        ownerNumber = ownerNumber.replaceAll("-", "");

        String[] numbers = {"77891",
                "314 77891", "000 77891", "314 00000",
                "314-77891", "000-77891", "314-00000",
                "333 314 77891", "000 000 77891", "123 000 77891", "000 123 77891",
                "333-314-77891", "000-000-77891", "123-000-77891", "000-123-77891",
                "99999999999999999999",
                "11111-22222-33333-44444"};
        //5位
//        String number = "77891";
        //8位
//        String number = "314 77891";
//        String number = "000 77891";
//        String number = "314 00000";

        //11位
//        String number = "333 314 77891";
//        String number = "000 000 77891";
//        String number = "123 000 77891";
//        String number = "000 123 77891";

        for (String number : numbers) {
            try {
                System.out.println(number + " --to 20--> " + numberTo20(ownerNumber, number));
            }catch (IllegalArgumentException e){
                System.err.println(number + " --to 20--> ");
                e.printStackTrace();
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }*/
    }
}