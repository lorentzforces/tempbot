package fyi.lorentz.tempbot.engine;

import java.util.ArrayList;
import java.util.List;

public class ProcessingResult {

    public String valueString;
    public String labelString;
    public Dimension dimension;
    public Unit unit;
    public UnitValue sourceValue;
    public List<UnitValue> values = new ArrayList<>();
    public List<Exception> errors = new ArrayList<>();
    public boolean isSpecificConversion;

}
