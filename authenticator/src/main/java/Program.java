import java.io.*;
import java.time.*;

public class Program {
    private static ZonedDateTime startDateTime;
    private static Duration interval;

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
        return 0; // TODO: not implemented
    }

    /**
    * Calculates a time-based one-time password given some key.
    * @param key The key to use.
    */
    private static String otp(String key) {
        // calculate the number of intervals which have elapsed
        // based on the TOTP configuration (startDateTime and
        // interval)
        long c = intervals();

        // TODO: Alan, please fix.
        return "time-based one-time password";
    }
}
