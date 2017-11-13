import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

import fyi.lorentz.tempBot.service.TemperatureProcessor;

public class TestTemperatureProcessor {

    @Test
    public void
    emptyStringShouldReturnEmpty() {
        String inputString = "";
        String[] expected = new String[0];
        TemperatureProcessor testObject = new TemperatureProcessor(inputString);

        String[] output = testObject.getTemperatureStrings();

        assertArrayEquals(expected, output);
    }

    @Test
    public void
    temperatureByItselfShouldReturnSame() {
        String inputString = "32F";
        String[] expected = new String[]{"32F"};
        TemperatureProcessor testObject = new TemperatureProcessor(inputString);

        String[] output = testObject.getTemperatureStrings();

        assertArrayEquals(expected, output);
    }

    @Test
    public void
    multipleTemperaturesShouldAllBeRecognized() {
        String inputString = "32 F and some lorem ipsum and 0C and nothing else";
        String[] expected = new String[]{"32 F", "0C"};
        TemperatureProcessor testObject = new TemperatureProcessor(inputString);

        String[] output = testObject.getTemperatureStrings();

        assertArrayEquals(expected, output);
    }

    @Test
    public void
    wordsThatStartWithCFShouldNotReturn() {
        // F/Fahrenheit
        String inputString = "32 forgotten dogs";
        String[] expected = new String[]{};
        TemperatureProcessor testObject = new TemperatureProcessor(inputString);

        String[] output = testObject.getTemperatureStrings();

        assertArrayEquals(expected, output);

        // C/Celsius
        inputString = "0 casual cats";
        expected = new String[]{};
        testObject = new TemperatureProcessor(inputString);

        output = testObject.getTemperatureStrings();

        assertArrayEquals(expected, output);
    }

    @Test
    public void
    numbersWithSignsAndDecimalPointsShouldAllReturn() {
        String inputString = "+32F -40C 20.09 F -.75C";
        String[] expected = new String[]{
            "+32F", "-40C", "20.09 F", "-.75C" };
        TemperatureProcessor testObject = new TemperatureProcessor(inputString);

        String[] output = testObject.getTemperatureStrings();

        assertArrayEquals(expected, output);
    }
}
