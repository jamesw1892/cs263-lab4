# Lab 4 of CS263: Cyber Security

My code for lab 4, implementing 2FA in an existing web service.

# What happens

When the user logs in, a key is generated for their session which is presented to them on the 2FA page. They then have to input this into the authenticator application which outputs the one-time passcode based on the key and the current time interval. They then enter this back into the server which compares it to its own calculation. If they are the same, the user is allowed in. Unlike most authenticator systems where the user only has to type in 1 code, in this application, they have to enter two so both need to be human-readable. This is why I convert them to 6-digit integers.

# Exercises

## Ex10

Implement the `intervals` method in `authenticator/src/main/java/Program.java`. I used methods in the `java.time` package to calculate the current time, find the difference between them and divide that by the time interval.

## Ex11

Implement the `otp` method in `authenticator/src/main/java/Program.java` which calls the method we completed in Ex10 to generate the one time password based on the time interval and a password. I generate a SHA-256 HMAC and convert it to a 6-digit integer so it is easy to read and remember.

## Ex12

Move the `intervals` and `otp` methods from `authenticator/src/main/java/Program.java` to `server/src/main/java/dcs/LoginController.java`.

## Ex13

Implement the `generate2FAKey` method in `server/src/main/java/dcs/LoginController.java` to generate a specific key for the SHA_256 HMAC using a CSPRNG.

## Ex14

Complete the `handleLoginPost` method in `server/src/main/java/dcs/LoginController.java` to generate the key using the `generate2FAKey` method and store it in the session.

## Ex15

Complete the `server2FAPage` method in `server/src/main/java/dcs/LoginController.java` to put the key from the session in the model to serve the 2FA page if it exists (and therefore they have been authenticated).