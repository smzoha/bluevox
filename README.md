![alt text](app/src/main/res/drawable-xhdpi/ic_launcher.png "BlueVox Logo") 
# BlueVox

An Android application that enables the user to control an Arduino-powered two-wheeled bot using his/her voice, or the manual control that is included under the hood. The Arduino bot is expected to contain a HC-05 Bluetooth module, over which the Android application connects to the bot and sends command to run it.

The Arduino code for such a bot is included in the `arduino` directory of the project. The bot utilizes DC motors to run, HC-05 Bluetooth module to communicate with the application, along with a HC-SR04 sonar sensor, which is used to detect obstacles that lies in front of the device. Upon encountering an obstacle, the bot is expected to inform the user through a buzzer that is attached to the Arduino.

The bot is able to move forward, reverse, left, right, and roll in clockwise or anticlockwise direction. Also, the bot was made as a part of an university project and utilizes the very basic, fundamental libraries of Android.

The application is licensed under GNU General Public License, version 3.0. A copy of the license can be found [here](https://www.gnu.org/licenses/gpl-3.0-standalone.html).

![alt text](https://www.gnu.org/graphics/gplv3-127x51.png "GPL License V3.0")