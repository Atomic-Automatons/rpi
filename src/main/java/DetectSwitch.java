import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.RaspiPin;

import edu.wpi.first.wpilibj.networktables.NetworkTable;

import com.pi4j.io.gpio.*;

public class DetectSwitch extends Task {

    final GpioController gpio = GpioFactory.getInstance();
    GpioPinDigitalInput myButton = gpio.provisionDigitalInputPin(RaspiPin.GPIO_12, "MyButton");
    GpioPinDigitalOutput pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_05, "MyLED");
    boolean buttonPressed;

    public DetectSwitch() {
    }

    public void initialize() {
        pin.setShutdownOptions(true, PinState.LOW);
    }

    public void exceute() {
      /*  buttonPressed = myButton.isHigh();
        if (buttonPressed) {
            System.out.println("Pushed");
            pin.high();
        } else {
            pin.low();
        }*/
        NetworkTable.getTable("SmartDashboard").putBoolean("button", myButton.isHigh());
    }

    public void end() {
        // pin.shutdown();
    }
}