import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;


public class Main {
	public static void main(String[] args) throws IOException {
		String inputPath, outputPath;
		int minUtil;
		if(args.length == 0){
			inputPath = fileToPath("database.txt");
			minUtil = 40;
		}
		else if(args.length == 1){
			inputPath = args[0];
			minUtil =40;
		}
		else{
			inputPath = args[0];
			minUtil = Integer.parseInt(args[1]);
		}

		outputPath = "results.txt";

		System.out.println("\n=============  HUSPM ALGORITHM - STARTS ==========\n");

		AHUS4 ahus = new AHUS4();

		ahus.runAlgorithm(inputPath, outputPath, minUtil);

		System.out.println("\n--------------- STATISTICS ---------------------");
		System.out.println("MINIMUM UTILITY : " + minUtil);
		System.out.println("HIGH UTILITY PATTERN COUNT : " + ahus.totalPatterns);
		System.out.println("TOTAL EXECUTION TIME : " + ahus.timeElapsed + " ms");
		System.out.println("--------------------------------------------------");
		System.out.println("\n==================================================");
	}

	private static String fileToPath(String filename) throws UnsupportedEncodingException {
		URL url = Main.class.getResource(filename);

		return java.net.URLDecoder.decode(url.getPath(), "UTF-8");
	}
}