package IRParser;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.stream.Stream;


public class Utils {
	
	public static final String IMPERATOR_PATH = "C:\\Program Files (x86)\\Steam\\SteamApps\\common\\ImperatorRome\\game\\";
	
	public static String readNextLine(BufferedReader inFile) {
		return readNextLine(inFile, true);
	}

	public static String readNextLine(BufferedReader inFile, boolean inlineCommentsAllowed) {
		try {
			while(inFile.ready()) {
				String line = inFile.readLine().trim();
				// Weird thing appears at top of file
				if (line.indexOf("﻿") > -1) {
					line = (line.substring(0, line.indexOf("﻿")) + line.substring(line.indexOf("﻿") + 3)).trim();
				}
				// Remove comments
				int commentStart = line.indexOf('#');
				if (commentStart > -1 && (commentStart == 0 || inlineCommentsAllowed)) {
					line = line.substring(0, line.indexOf('#')).trim();
				}
				if (line.length() > 0) {
					return line;
				}
			}
			return "";
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}

	public static String readNextToken(BufferedReader inFile) {
		try {
			String currentLine = "";
			boolean isStringStarted = false;
			while(inFile.ready()) {
				inFile.mark(2);
				char nextChar = (char) inFile.read();
				if (Character.isWhitespace(nextChar)) {
					// Ignore starting whitespace
					if (!isStringStarted) {
						continue;
					}
					// End token when reaching whitespace
					else {
						break;
					}
				}
				// Remove comments
				if (nextChar == '#') {
					inFile.readLine();
					if (!isStringStarted) {
						continue;
					}
					else {
						break;
					}
				}
				// = and {} are always tokens of their own
				if (nextChar == '=' || nextChar == '{' || nextChar == '}') {
					if (!isStringStarted) {
						currentLine += nextChar;
						return currentLine;
					}
					else {		// Go back to the character before so we can properly read them as their own tokens
						inFile.reset();
						break;
					}
				}
				// If we find a quotation mark, the token spans from the mark until either the next quotation mark
				// or the end of the line
				if (nextChar == '"') {
					if (!isStringStarted) {
						nextChar = (char) inFile.read();
						while (nextChar != '"' && nextChar != '\n') {
							currentLine += nextChar;
							nextChar = (char) inFile.read();							
						}
						return currentLine;
					}
					else {		// Go back to the character before so we can properly read them as their own tokens
						inFile.reset();
						break;
					}					
				}
				currentLine += nextChar;
				isStringStarted = true;
			}
			// Weird thing appears at top of file
			if (currentLine.indexOf("﻿") > -1) {
				currentLine = (currentLine.substring(0, currentLine.indexOf("﻿")) + 
						currentLine.substring(currentLine.indexOf("﻿") + 3)).trim();
			}
			return currentLine;
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}
	
	public static void consumeBlock(BufferedReader inFile) throws IOException {
		int level = 0;
		while(inFile.ready()) {
			String token = Utils.readNextToken(inFile);
			if (token.equals("{")) {
				level += 1;
			}
			else if (token.equals("}")) {
				level -= 1;
			}
			if (level == 0 && !token.equals("hsv")) {
				break;
			}
		}		
	}
	
	public static Hashtable<String, String> readLocalisationFile(String fileName) {
		// Read in region localisations
		try {
			BufferedReader inLocFile = new BufferedReader(new FileReader(fileName));
			
			Hashtable<String, String> locTable = new Hashtable<String, String>();
			
			// Consume language tag
			readNextLine(inLocFile);
			
			// Read in the localisations
			while(inLocFile.ready()) {
				String line = readNextLine(inLocFile, false);
				if (line.length() > 0) {
					String key = line.substring(0, line.indexOf(':'));
					String name = line.substring(line.indexOf('"'));
					name = name.substring(name.indexOf('"') + 1, name.lastIndexOf('"')).trim();	
					locTable.put(key, name);
				}
			}
			inLocFile.close();
			
			return locTable;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}		
	}
	
	public static Hashtable<String, String> readLocalisationFolder(String folderName) {
		final File mainSetupFolder = new File(folderName);

		try (Stream<Path> walk = Files.walk(Paths.get(folderName))) {

	        List<String> files = new ArrayList<>();
	        List<String> folders = new ArrayList<>();
	        
	        for (final File f : mainSetupFolder.listFiles()) {
	            if (f.isFile()) {
	            	files.add(f.getAbsolutePath());
	            }
	            else if (f.isDirectory()) {
	            	folders.add(f.getAbsolutePath());	            	
	            }
	        }

			Hashtable<String, String> locTable = new Hashtable<String, String>();
			
			for (String file : files) {
				BufferedReader inLocFile = new BufferedReader(new FileReader(file));
				
				// Consume language tag
				readNextLine(inLocFile);
				
				// Read in the localisations
				while(inLocFile.ready()) {
					String line = readNextLine(inLocFile, false);
					if (line.length() > 0) {
						String key = line.substring(0, line.indexOf(':'));
						String name = line.substring(line.indexOf('"'));
						name = name.substring(name.indexOf('"') + 1, name.lastIndexOf('"')).trim();	
						locTable.put(key, name);
					}
				}
				inLocFile.close();		
			}
			
			for (String folder : folders) {
				locTable.putAll(readLocalisationFolder(folder));
			}
			
			return locTable;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}		
	}
	
	public static String dlcToIconName(String dlcName) {
		if (dlcName.equals("Hellenistic World Flavor Pack")) {
			return "[[Hellenistic World Flavor Pack|(DLC)]]";
		}
		else {
			System.err.println("Could not recognize DLC: " + dlcName);
			return "";
		}
	}
}
