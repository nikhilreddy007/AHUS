import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.text.ParseException;
import java.util.concurrent.TimeUnit;
import java.time.Duration;
import java.time.Instant;

public class Main {
	public static void main(String[] args) throws IOException {
		String inputPath = fileToPath("database.txt");
		String outputPath = "results.txt";

		int minUtil = 40;

		// System.out.println("=============  HUSPM ALGORITHM - STARTS ==========");

		AHUS4 ahus = new AHUS4();

		ahus.runAlgorithm(inputPath, outputPath, minUtil);
		
		// System.out.println("==================================================");
	}

	private static String fileToPath(String filename) throws UnsupportedEncodingException {
		URL url = Main.class.getResource(filename);

		return java.net.URLDecoder.decode(url.getPath(), "UTF-8");
	}
}