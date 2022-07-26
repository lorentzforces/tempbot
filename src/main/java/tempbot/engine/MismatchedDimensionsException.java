package tempbot.engine;

public class MismatchedDimensionsException extends Exception {
	private Dimension unitDimension;
	private Dimension destinationDimension;

	public MismatchedDimensionsException(
		Dimension unitDimension,
		Dimension destinationDimension
	) {
		super();
		initValues(unitDimension, destinationDimension);
	}

	public MismatchedDimensionsException(
		String message,
		Dimension unitDimension,
		Dimension destinationDimension
	) {
		super(message);
		initValues(unitDimension, destinationDimension);
	}

	public MismatchedDimensionsException(
		String message,
		Throwable cause,
		Dimension unitDimension,
		Dimension destinationDimension
	) {
		super(message, cause);
		initValues(unitDimension, destinationDimension);
	}

	public MismatchedDimensionsException(
		Throwable cause,
		Dimension unitDimension,
		Dimension destinationDimension
	) {
		super(cause);
		initValues(unitDimension, destinationDimension);
	}

	public Dimension getUnitDimension() {
		return unitDimension;
	}

	public Dimension getDestinationDimension() {
		return destinationDimension;
	}

	private void initValues(
		Dimension unitDimension,
		Dimension destinationDimension
	) {
		this.unitDimension = unitDimension;
		this.destinationDimension = destinationDimension;
	}

}
