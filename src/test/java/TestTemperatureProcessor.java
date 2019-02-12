import org.junit.Test;

import fyi.lorentz.tempBot.service.TemperatureProcessor;

import static org.junit.Assert.assertEquals;

public class TestTemperatureProcessor {

    @Test
    public void
    emptyStringShouldReturnEmpty() {
        String inputString = "";
        String expected = "";
        TemperatureProcessor testObject = new TemperatureProcessor(inputString);

        String output = testObject.processMessage();

        assertEquals(expected, output);
    }

    @Test
    public void
    temperatureByItselfShouldReturnSame() {
        String inputString = "32F";
        String expected = "**32\u00b0F** = **0\u00b0C**";
        TemperatureProcessor testObject = new TemperatureProcessor(inputString);

        String output = testObject.processMessage();

        assertEquals(expected, output);
    }

    @Test
    public void
    multipleTemperaturesShouldAllBeRecognized() {
        String inputString = "32 F and some lorem ipsum and 0C and nothing else";
        String expected =
                "**32\u00b0F** = **0\u00b0C**\n" +
                "**0\u00b0C** = **32\u00b0F**";
        TemperatureProcessor testObject = new TemperatureProcessor(inputString);

        String output = testObject.processMessage();

        assertEquals(expected, output);
    }

    @Test
    public void
    wordsThatStartWithCFShouldNotReturn() {
        // F/Fahrenheit
        String inputString = "32 forgotten dogs";
        String expected = "";
        TemperatureProcessor testObject = new TemperatureProcessor(inputString);

        String output = testObject.processMessage();

        assertEquals(expected, output);

        // C/Celsius
        inputString = "0 casual cats";
        expected = "";
        testObject = new TemperatureProcessor(inputString);

        output = testObject.processMessage();

        assertEquals(expected, output);
    }

    @Test
    public void
    numbersWithSignsAndDecimalPointsShouldAllReturn() {
        String inputString = "+32F -40C 20.09 F -.75C";
        String expected =
                "**32\u00b0F** = **0\u00b0C**\n" +
                "**-40\u00b0C** = **-40\u00b0F**\n" +
                "**20.09\u00b0F** = **-6.617\u00b0C**\n" +
                "**-0.75\u00b0C** = **30.65\u00b0F**";
        TemperatureProcessor testObject = new TemperatureProcessor(inputString);

        String output = testObject.processMessage();

        assertEquals(expected, output);
    }

    @Test
    public void
    duplicatesShouldBeIgnored() {
        String inputString = "32F -40C 32f";
        String expected =
                "**32\u00b0F** = **0\u00b0C**\n" +
                "**-40\u00b0C** = **-40\u00b0F**";
        TemperatureProcessor testObject = new TemperatureProcessor(inputString);

        String output = testObject.processMessage();

        assertEquals(expected, output);
    }

    @Test
    public void
    belowAbsoluteZeroShouldBeIgnored() {
        String inputString = "-400C";
        String expected = "";
        TemperatureProcessor testObject = new TemperatureProcessor(inputString);

        String output = testObject.processMessage();

        assertEquals(expected, output);
    }
}
