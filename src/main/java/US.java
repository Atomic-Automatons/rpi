import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.serial.*;
import com.pi4j.*;

import edu.wpi.first.wpilibj.networktables.NetworkTable;

import java.io.IOException;
import java.util.Date;

public class US extends Task {
    int pin = 14;
    final GpioController gpio = GpioFactory.getInstance();
    // RaspiPin.GPIO_16
    final Serial serial = SerialFactory.createInstance();
    String buffer = "";
    int counter = 0;

    public US() {
        super();
    }

    private void fillBuffer() {
        char a = (char) serial.read();
        while (a != 'R' && serial.availableBytes() > 0) {
            if (a == 'R') {
                for (int i = 0; i < 3; i++)
                    buffer += serial.read() + "";
            }
            a = (char) serial.read();
        }
    }

    public void initialize() {
        serial.open(Serial.DEFAULT_COM_PORT, 9600);

    }

    public void execute() {
        fillBuffer();
        NetworkTable.getTable("SmartDashboard").getSubTable("Plus Ultra").putString("Ultra Sonic", buffer);
    }

    public void end() {
        serial.close();
    }
}