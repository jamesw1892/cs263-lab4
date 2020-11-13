import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class Program {
    private static ZonedDateTime startDateTime;
    private static Duration interval;
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final int NUM_DIGITS = 6;

    /**
    * The main entry for this application.
    * @param args The command-line arguments.
    */
    public static void main(String[] args) {
        // initialise the start time and the interval
        Instant i = Instant.ofEpochSecond(0);
        startDateTime = ZonedDateTime.ofInstant(i, ZoneId.of("UTC"));
        interval = Duration.ofMinutes(30);

        // print instructions to the standard output
        System.out.println("Welcome to the DCS AuThEnTiCaToR!");
        System.out.print("Enter the key: ");

        // read the key from the standard input and convert it
        // to a time-based one-time password
        try {
            // read the key
            BufferedReader reader = new BufferedReader( new InputStreamReader( System.in ) );
	        String key = reader.readLine();

            // generate the one-time password
            String otp = otp(key);
            System.out.println("The one-time password is: " + otp);
		}
		catch(Exception e){
            System.out.println("DCS AuThEnTiCaToR has made a sad :(");
			System.out.println(e.toString());
		}
    }

    /**
    * Calculates the number of intervals that have elapsed since
    * the start time.
    */
    private static long intervals() {
        ZonedDateTime endDateTime = ZonedDateTime.ofInstant(Instant.now(), ZoneId.of("UTC"));
        Duration elapsedTime = Duration.between(startDateTime, endDateTime);
        return elapsedTime.dividedBy(interval);
    }

    /**
    * Calculates a time-based one-time password given some key.
    * @param key The key to use.
    */
    private static String otp(String key) {
        // calculate the number of intervals which have elapsed
        // based on the TOTP configuration (startDateTime and
        // interval)
        long numIntervals = intervals();

        // Convert num_intervals from long to byte[] to use in HMAC
        ByteBuffer byteBuffer = ByteBuffer.allocate(Long.BYTES);
        byteBuffer.putLong(numIntervals);
        byte[] intervalBytes = byteBuffer.array();

        // Convert key from String to SecretKey to be used in HMAC
        SecretKeySpec k = new SecretKeySpec(key.getBytes(), HMAC_ALGORITHM);

        try {
            // Add the key to HMAC
            Mac hmac = Mac.getInstance(HMAC_ALGORITHM);
            hmac.init(k);

            // Calculate HMAC with time interval
            byte[] hmacBytes = hmac.doFinal(intervalBytes);

            // Convert HMAC to an easy-to-read integer with `NUM_DIGITS` digits
            String s = (new BigInteger(hmacBytes)).mod(BigInteger.TEN.pow(NUM_DIGITS)).toString();
            while (s.length() < NUM_DIGITS) {
                s = "0" + s;
            }
            return s;

        } catch (GeneralSecurityException e) {
            System.out.println("HMAC error: " + e.getMessage());
            return "";
        }
    }
}