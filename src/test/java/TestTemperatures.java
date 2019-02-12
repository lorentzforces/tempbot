import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import fyi.lorentz.tempBot.service.Temperature;

public class TestTemperatures {

    public static final double MAX_DELTA = 0.0000001;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void
    emptyStringShouldThrowException() {
        exception.expect(IllegalArgumentException.class);
        Temperature temp = new Temperature("");
    }

    @Test
    public void
    randomStringShouldThrowException() {
        exception.expect(IllegalArgumentException.class);
        Temperature temp = new Temperature("");
    }

    @Test
    public void
    invalidNumberFormatShouldThrowException() {
        exception.expect(IllegalArgumentException.class);
        Temperature temp = new Temperature("32..2F");
    }

    @Test
    public void
    minusFortyShouldBeTheSame() {
        Temperature temp = new Temperature("-40 F");
        assertEquals(temp.getUnitValue("C"), temp.getUnitValue("F"), MAX_DELTA);
        assertEquals(temp.getUnitValue("C"), -40.0, MAX_DELTA);
    }

    @Test
    public void
    zeroCelsiusShouldBe32Fahrenheit() {
        Temperature temp = new Temperature("0 C");
        assertEquals(temp.getUnitValue("F"), 32, MAX_DELTA);
    }

    @Test
    public void
    extremelyLargeTemperaturesShouldStillBeAccurate() {
        Temperature temp = new Temperature("9999999.8888 F");
        assertEquals(temp.getUnitValue("C"), 5555537.716, MAX_DELTA);
    }

}
