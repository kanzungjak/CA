package myCA;
import java.util.Arrays;

public class guessedKeysPair {

	private int[] fineKeys;
	private int[] bulkKeys;

	public guessedKeysPair(int[] fineKeys, int[] bulkKeys) {
		this.fineKeys = Arrays.copyOf(fineKeys, fineKeys.length);
		this.bulkKeys = Arrays.copyOf(bulkKeys, bulkKeys.length);
	}

	public int[] getBulk() {
		return bulkKeys;
	}

	public int[] getFine() {
		return fineKeys;
	}
}