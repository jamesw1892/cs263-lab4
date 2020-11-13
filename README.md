# Lab 4 of CS263: Cyber Security

My code for lab 4, implementing 2FA in an existing web service.

# Ex10

Implement the `intervals` method in `authenticator/src/main/java/Program.java`. I used methods in the `java.time` package to calculate the current time, find the difference between them and divide that by the time interval.

# Ex11

Implement the `otp` method in `authenticator/src/main/java/Program.java` which calls the method we completed in Ex10 to generate the one time password based on the time interval and a password. I generate a SHA-256 HMAC and convert it to a 6-digit integer so it is easy to read and remember.