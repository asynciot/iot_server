/** Author: Michael Wang
 * Date: 2015-06-26
 * Description: this class generates different code including uuid and md5.
 */

package controllers.common;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class CodeGenerator {
    
    /* Function: GenerateUUId
     * Parameters: None
     * Description: returns a random uuid.
     */
    
    public static String[] chars = new String[] { "a", "b", "c", "d", "e", "f",  
        "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s",  
        "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5",  
        "6", "7", "8", "9"};  
    
    public static java.security.SecureRandom random = new java.security.SecureRandom();
    
    private static long getBaseTime() {
        Calendar baseDate = Calendar.getInstance();
        baseDate.set(2015, 1, 1, 0, 0, 0);
        baseDate.set(Calendar.MILLISECOND, 0);        
        return baseDate.getTimeInMillis();
    }
    
    public static String generateUUId() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString().replace("-", "");
    }
    
    /* Function: GenerateUUId
     * Parameters: str - a string that will convert to md5.
     * Description: returns an md5 string
     */
    public static String generateMD5(String str) throws NoSuchAlgorithmException {
        byte [] buf = str.getBytes();
        
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(buf);

        byte [] tmp = md5.digest();
        StringBuilder sb = new StringBuilder();

        for (byte b:tmp) {
            sb.append(Integer.toHexString(b&0xff));
        }
        
        return sb.toString();
    }
    
    public static String generateMD5Utf8(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        byte [] buf = str.getBytes("UTF-8");
        
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(buf);

        byte [] tmp = md5.digest();
        StringBuilder sb = new StringBuilder();

        for (byte b:tmp) {
            sb.append(String.format("%02X", b&0xff));
        }
        
        return sb.toString();    
    }  
    
    public static String generateRandomNumber() throws NoSuchAlgorithmException {    
        int randomNum = (int)(random.nextDouble()*1000);
        
        //SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        long timeDiff = new Date().getTime() - getBaseTime();
        return String.format("%d%03d", timeDiff, randomNum);
    }
    
    public static String generateShortUUId() {  
        StringBuffer shortBuffer = new StringBuffer();  
        String uuid = UUID.randomUUID().toString().replace("-", "");  
        int length = 16;
        int step = 32/length;
        for (int i = 0; i < length; i++) {  
            String str = uuid.substring(i * step, i * step + 2);  
            int x = Integer.parseInt(str, 16);  
            shortBuffer.append(chars[x % chars.length]);  
        }  
        return shortBuffer.toString();
    }
    
    public static String calcSignature(String[] array) {
        if (null == array) {
            return null;
        }
        
        String[] tmpArray = array.clone();

        Arrays.sort(tmpArray);
        
        StringBuilder strBuilder = new StringBuilder();
        for (String str : tmpArray) {
            strBuilder.append(str);
        }
        
        MessageDigest md;
        String signature = null;
        try {
            md = MessageDigest.getInstance("SHA-1");
            md.update(strBuilder.toString().getBytes());
            signature = bytes2Hex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return signature;
    }
    
    private static String bytes2Hex(byte[] bts) {
        StringBuilder des = new StringBuilder();
        String tmp = null;
        for (int i = 0; i < bts.length; i++) {
            tmp = (Integer.toHexString(bts[i] & 0xFF));
            if (tmp.length() == 1) {
                tmp = "0" + tmp;
            }
            des.append(tmp);
        }
        return des.toString();
    }
}
