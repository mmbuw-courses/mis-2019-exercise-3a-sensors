# mis-2019-exercise-3a-sensors

Members:
- Arika Dodani
- Fauziah Permatasari

* 3a: Bonus *
The gesture we have in the app is to show on the Textview what activity the mobile user is doing,
based on their acceleration rate.
When the acceleration (before and after speed) has the value of <= 3, then it is considered as static.
Shown on the Textview as “Static”.
Meanwhile, when the value is measured for >3, then it is considered as moving.
Shown on the Textview as “Moving”.
We used the Switch on Enum in Java method to define cases.
[Source: “https://docs.oracle.com/javase/tutorial/java/javaOO/enum.html”]
To calculate the acceleration (and distance), we have to know the before speed, and the after speed.
In order to find out the before speed and the after speed,
we first have to figure out the location changes (distance between last and current location).
This is determined by onLocationChanged().
