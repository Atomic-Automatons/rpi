import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.RaspiPin;

public class DetectSwitch extends Task{
    
    final GpioController gpio = GpioFactory.getInstance();
    GpioPinDigitalInput myButton = gpio.provisionDigitalInputPin(RaspiPin.GPIO_07, "MyButton");
    boolean buttonPressed;
    
    public DetectSwitch(){
    }

    public void initialize(){

    }

    public void exceute(){
        buttonPressed = myButton.isHigh();
        if(buttonPressed){
            System.out.println("Pushed");
        }
    }


}