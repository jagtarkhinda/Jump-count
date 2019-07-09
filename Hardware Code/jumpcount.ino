
#include "InternetButton/InternetButton.h"
#include "math.h"


InternetButton b = InternetButton();
int jmpcnt;
float jumps;
int percentage;

int initialZ;

void setup() {
    b.begin();
    jmpcnt = 0;
    jumps = 0;
    initialZ = b.readZ();
     Particle.function("totalJumps", getJumps);
}

void loop(){

    if(jumps > 0)
    {
         int zValue = b.readZ();

   
        if(zValue > (initialZ + 40))
        {
             zValue = initialZ;
               jmpcnt += 1;
                     percentage = (jmpcnt / jumps) * 100;
                     //publishing the jump count
                     Particle.publish("jmpCount", String(jmpcnt));
                //     Particle.publish("percent", String(abs(percentage)));
             delay(930);
             
             //turning on leds according to percentage
             for (int i = 1; i <= (percentage / 10); i++) {
                 b.ledOn(i, 0, 50, 50);
                }

              // if goal is complete, stop the loop
              if(percentage == 100)
              {
                  jumps = 0;
                  jmpcnt = 0;
                  percentage = 0;
                  delay(300);
                  b.allLedsOff();
              }
        }
    
    }
}
    //method to get jump goal from mobile device
    int getJumps(String command)
    {
        //if jump goal already set, do not accept another input
        if( jumps == 0 )
        {
        jumps = atof(command.c_str());
       
         Particle.publish("jumps", String(jumps));
        }
       
    }