package fyi.lorentz.tempbot.engine;

public class UnitRangeException extends Exception {

    private UnitValue rangeLimitingValue;
    private UnitValue offendingValue;

    public UnitRangeException(
            UnitValue rangeLimitingValue,
            UnitValue offendingValue
    ) {
        super();
        initValues(rangeLimitingValue, offendingValue);
    }

    public UnitRangeException(
            String message,
            UnitValue rangeLimitingValue,
            UnitValue offendingValue
    ) {
        super(message);
        initValues(rangeLimitingValue, offendingValue);
    }

    public UnitRangeException(
            String message,
            Throwable cause,
            UnitValue rangeLimitingValue,
            UnitValue offendingValue
    ) {
        super(message, cause);
        initValues(rangeLimitingValue, offendingValue);
    }

    public UnitRangeException(
            Throwable cause,
            UnitValue rangeLimitingValue,
            UnitValue offendingValue
    ) {
        super(cause);
        initValues(rangeLimitingValue, offendingValue);
    }

    public UnitValue getRangeLimitingValue() {
        return rangeLimitingValue;
    }

    public UnitValue getOffendingValue() {
        return offendingValue;
    }

    public boolean isMaximum() {
        return offendingValue.getValue() > rangeLimitingValue.getValue();
    }

    public boolean isMinimum() {
        return offendingValue.getValue() < rangeLimitingValue.getValue();
    }

    private void initValues(
            UnitValue rangeLimitingValue,
            UnitValue offendingValue
    ) {
        this.rangeLimitingValue = rangeLimitingValue;
        this.offendingValue = offendingValue;
    }

}
