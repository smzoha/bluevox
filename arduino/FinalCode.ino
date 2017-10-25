#include <SoftwareSerial.h>

SoftwareSerial BT(10, 11); // TX & RX
String cmdString;
boolean stopStatus = true;
long duration, cm;

void setup() {
  BT.begin(9600);
  Serial.begin(9600);

  // define output pins for the gear motor
  pinMode(3, OUTPUT);
  pinMode(4, OUTPUT);
  pinMode(5, OUTPUT);
  pinMode(6, OUTPUT);

  // define pins that are used by Ultrasound Sensor
  pinMode(13, OUTPUT);
  pinMode(12, INPUT);
}

void loop() {
  // if the device is not in stationary state,
  // the ultrasound sensor produces signal through
  // and the trig pin receives the reflected signal
  // though the echo pin.
  if(!stopStatus) {    
    digitalWrite(13, LOW);
    delayMicroseconds(2);

    digitalWrite(13, HIGH);
    delayMicroseconds(5);

    digitalWrite(13, LOW);
    
    duration = pulseIn(12, HIGH);
    
    // once the signal is received, the centimeter
    // value is calculated. this is the distance between
    // the sensor and the obstacle.
    cm = duration/29/2;

    // if the distance is less than 10cm, the robot is
    // stopped and a sound is produced using the buzzer
    if(cm <= 10) {
      tone(9, 500);
      delay(50);

      digitalWrite(3, LOW);
      digitalWrite(4, LOW);
      digitalWrite(5, LOW);
      digitalWrite(6, LOW);
      stopStatus = true;
      
      delay(50);
      
      noTone(9);
    }

    delay(200);
  }
  
  while(BT.available()) {
    delay(10); // delay for stable
    char tmp = BT.read(); // take a character at a time
    cmdString += tmp; // build string using character
  }

  if(cmdString.length() > 0) {

    //Forward
    if(cmdString == "FORWARD") {
      digitalWrite(3, HIGH);
      digitalWrite(4, HIGH);
      digitalWrite(5, LOW);
      digitalWrite(6, LOW);
      delay(100);
      stopStatus = false;

      //Backward
    } else if(cmdString == "BACKWARD") {
      digitalWrite(3, LOW);
      digitalWrite(4, LOW);
      digitalWrite(5, HIGH);
      digitalWrite(6, HIGH);
      delay(100);

      stopStatus = false;

      //Right
    } else if(cmdString == "RIGHT") {
      digitalWrite(3, LOW);
      digitalWrite(4, HIGH);
      digitalWrite(5, LOW);
      digitalWrite(6, LOW);
      delay(100);

      // if the robot was in stationary state,
      // the motion is halted after the robot has
      // moved left by an approximate factor
      if(stopStatus) {
        digitalWrite(4, LOW);
      } else {
        digitalWrite(3, HIGH);
      }

      //Left
    } else if(cmdString == "LEFT") {
      digitalWrite(3, HIGH);
      digitalWrite(4, LOW);
      digitalWrite(5, LOW);
      digitalWrite(6, LOW);
      delay(100);

      // if the robot was in stationary state,
      // the motion is halted after the robot has
      // moved right by an approximate factor
      if(stopStatus) {
        digitalWrite(3, LOW);
      } else {
        digitalWrite(4, HIGH);
      }
      
       //Clockwise
     } else if(cmdString == "CLOCKWISE") {
      digitalWrite(3, LOW);
      digitalWrite(4, HIGH);
      digitalWrite(5, LOW);
      digitalWrite(6, LOW);
      delay(100);

      stopStatus = false;

      //Anti Clockwise
    } else if(cmdString == "ANTI CLOCKWISE") {
      digitalWrite(3, HIGH);
      digitalWrite(4, LOW);
      digitalWrite(5, LOW);
      digitalWrite(6, LOW);
      delay(100);

      stopStatus = false;
      
      //Stop
    } else if(cmdString == "STOP") {
      digitalWrite(3, LOW);
      digitalWrite(4, LOW);
      digitalWrite(5, LOW);
      digitalWrite(6, LOW);
      delay(100);
      stopStatus = true;
    }

    // reset string for next command
    cmdString = "";
  }
}
