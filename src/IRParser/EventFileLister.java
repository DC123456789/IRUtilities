package IRParser;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class EventFileLister {

	public static void main(String[] args) {
		String pathname = Utils.IMPERATOR_PATH + "\\events\\great_work_events";
		String[] removeStart = {"rel", "dhe", "me", "greek"};
		List<String> removeStartList = Arrays.asList(removeStart);
		String ending = "events";
		final File folder = new File(pathname);
		
		try (Stream<Path> walk = Files.walk(Paths.get(pathname))) {

	        List<String> result = new ArrayList<>();
	        
	        for (final File f : folder.listFiles()) {
	            if (f.isFile()) {
	                result.add(f.getAbsolutePath());
	            }
	        }

			for (String file : result) {
				int startIndex = file.lastIndexOf("\\");
				int endIndex = file.lastIndexOf(".");
				String fileName = file.substring(startIndex + 1, endIndex);
				List<String> splitFileName = new ArrayList<>(Arrays.asList(fileName.split("_")));
				
				// Remove start if needed
				while (removeStartList.contains(splitFileName.get(0))) {
					splitFileName.remove(0);
				}
				
				// Add ending if not already there
				if (!splitFileName.get(splitFileName.size() - 1).equals("events")) {
					splitFileName.add(ending);
				}
				else {
					splitFileName.set(splitFileName.size() - 1, ending);
				}
				
				// Capitalize first letter
				if (Character.isLowerCase(splitFileName.get(0).charAt(0))) {
					String newFileNameStart = Character.toUpperCase(splitFileName.get(0).charAt(0)) + 
							splitFileName.get(0).substring(1);
					splitFileName.set(0, newFileNameStart);
				}
				
				String parsedFileName = "* [[";
				for (int i = 0; i < splitFileName.size(); i++) {
					parsedFileName += splitFileName.get(i);
					if (i < splitFileName.size() - 1)
						parsedFileName += " ";
				}
				parsedFileName += "]]";
				
				System.out.println(parsedFileName);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
