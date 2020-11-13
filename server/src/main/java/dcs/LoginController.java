package dcs;

import static dcs.SessionUtil.*;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import spark.*;

public class LoginController {
    private static final ZonedDateTime TWOFA_START_TIME = ZonedDateTime.ofInstant(Instant.ofEpochSecond(0), ZoneId.of("UTC"));
    private static final Duration TWOFA_TIME_INTERVAL = Duration.ofMinutes(30);
    private static final String TWOFA_ALGORITHM = "HmacSHA256";
    private static final int TWOFA_NUM_DIGITS = 6;

    // the DCS master database, very volatile, don't turn off the power
    private static Database database = new Database();

    /** Gets the database */
    public static Database getDatabase() {
        return database;
    }

    // Serve the registration page (GET request)
    public static Route serveRegisterPage = (Request request, Response response) -> {
        Map<String, Object> model = new HashMap<>();

        return ViewUtil.render(request, model, "/velocity/register.vm");
    };

    // Handle an attempt to register a new user
    public static Route handleRegistration = (Request request, Response response) -> {
        Map<String, Object> model = new HashMap<>();

        String username = request.queryParams("username");
        String password = request.queryParams("password");

        if(!LoginController.register(username, password)) {
            model.put("registrationFailed", true);
            return ViewUtil.render(request, model, "/velocity/register.vm");
        }

        // the user is now logged in with their new "account"
        request.session().attribute("currentUser", username);

        // redirect the user back to the front page
        response.redirect("/");
        return null;
    };

    // Serve the login page
    public static Route serveLoginPage = (Request request, Response response) -> {
        Map<String, Object> model = new HashMap<>();

        model.put("loggedOut", removeSessionAttrLoggedOut(request));
        model.put("loginRedirect", removeSessionAttrLoginRedirect(request));

        return ViewUtil.render(request, model, "/velocity/login.vm");
    };

    // Handle a login request
    public static Route handleLoginPost = (Request request, Response response) -> {
        Map<String, Object> model = new HashMap<>();

        String username = request.queryParams("username");

        // perform secure authentication
        if (!authenticate(username, request.queryParams("password"))) {
            model.put("authenticationFailed", true);
            return ViewUtil.render(request, model, "/velocity/login.vm");
        }

        // TODO: generate a key for 2FA; you probably want to
        // store it somewhere so that it is available in subsequent
        // requests

        response.redirect("/2fa/");
        return null;
    };

    public static Route serve2FAPage = (Request request, Response response) -> {
        Map<String, Object> model = new HashMap<>();

        // TODO: retrieve the 2FA key for this login attempt and
        // make sure there is one; otherwise, clients could just
        // request the /2fa/ page without having authenticated

        // TODO: the 2fa.vm view expects the model to contain an
        // entry named "twoFAcode" which contains the key

        return ViewUtil.render(request, model, "/velocity/2fa.vm");

    };

    public static Route handle2FA = (Request request, Response response) -> {
        Map<String, Object> model = new HashMap<>();

        // the OTP the client sent as part of the request
        String clientOTP = request.queryParams("otp");

        // TODO: retrieve the key from whereever it is stored
        String key = "key";

        // calculate our OTP
        String otp = otp(key);

        if(!otp.equals(clientOTP)) {
            response.redirect("/login/");
            return null;
        }

        // authentication "successful"
        model.put("authenticationSucceeded", true);

        // TODO: replace "username" with the user's actual username
        request.session().attribute("currentUser", "username");

        // redirect the user somewhere, if this was requested
        if (getQueryLoginRedirect(request) != null) {
            response.redirect(getQueryLoginRedirect(request));
        }

        // otherwise just redirect the user to the index
        response.redirect("/");
        return null;
    };

    // log a user out
    public static Route handleLogoutPost = (Request request, Response response) -> {
        request.session().removeAttribute("currentUser");
        request.session().attribute("loggedOut", true);
        response.redirect("/login/");
        return null;
    };

    /**
    * Registers a new user.
    * @param username The name of the user to register.
    * @param password The password of the user to register.
    */
    public static boolean register(String username, String password) {
        DCSUser user = new DCSUser(username);
        user.setPassword(password);
        database.addUser(user);

        return true;
    }

    /**
    * Performs the authentication process.
    * @param username The name of the user to authenticate.
    * @param password The password supplied by the client.
    */
    public static boolean authenticate(String username, String password) {
        // make sure the username and password aren't empty
        if (username.isEmpty() || password.isEmpty()) {
            return false;
        }

        DCSUser user = database.lookup(username);

        if(user != null && user.getPassword().equals(password)) {
            return true;
        }

        return false;
    }

    /**
    * Generates a key for 2FA.
    */
    private static String generate2FAKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(TWOFA_ALGORITHM);
            keyGenerator.init(new SecureRandom());
            return keyGenerator.generateKey().toString();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    /**
    * Calculates the number of intervals that have elapsed since
    * the start time.
    */
    private static long intervals() {
        ZonedDateTime endDateTime = ZonedDateTime.ofInstant(Instant.now(), ZoneId.of("UTC"));
        Duration elapsedTime = Duration.between(TWOFA_START_TIME, endDateTime);
        return elapsedTime.dividedBy(TWOFA_TIME_INTERVAL);
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
        SecretKeySpec k = new SecretKeySpec(key.getBytes(), TWOFA_ALGORITHM);

        try {
            // Add the key to HMAC
            Mac hmac = Mac.getInstance(TWOFA_ALGORITHM);
            hmac.init(k);

            // Calculate HMAC with time interval
            byte[] hmacBytes = hmac.doFinal(intervalBytes);

            // Convert HMAC to an easy-to-read integer with `NUM_DIGITS` digits
            String s = (new BigInteger(hmacBytes)).mod(BigInteger.TEN.pow(TWOFA_NUM_DIGITS)).toString();
            while (s.length() < TWOFA_NUM_DIGITS) {
                s = "0" + s;
            }
            return s;

        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }
}