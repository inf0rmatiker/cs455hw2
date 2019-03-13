package main.java.cs455.scaling.hash;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hash {

  // TODO: Return strings of the same length
  public String SHA1FromBytes(byte[] data) {
    MessageDigest digest = null;
    try {
      digest = MessageDigest.getInstance("SHA1");
    } catch (NoSuchAlgorithmException e){
      System.out.println("Could not find algorithm SHA1: " + e);
      return null;
    }
    byte[] hash = digest.digest(data);
    BigInteger hashInt = new BigInteger(1, hash);
    String returnValue = hashInt.toString(16);

    int amountToPad = 40 - returnValue.length();
    for (int i = 0; i < amountToPad; i++) {
      returnValue += '0';
    }
    //System.out.println(returnValue.length());
    return returnValue;
  }

}
