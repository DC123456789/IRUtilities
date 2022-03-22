package IRParser;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Scanner;


public class RegionFileParser {
	
	public class ImpRegion implements Comparable {
		public String name, key;
		public List<String> provinceList;
		
		public ImpRegion(String key, String name, List<String> provinceList) {
			this.key = key;
			this.name = name;
			this.provinceList = provinceList;
		}
		
		@Override
		public int compareTo(Object otherRegion) {
			return name.compareTo(((ImpRegion)otherRegion).name);
		}
	}
	
	public void parseRegions() {
		String regionFileName = "map_data\\regions.txt";
		String regionLocFileName = "localization\\english\\macroregions_l_english.yml";
		String provinceLocFileName = "localization\\english\\regionnames_l_english.yml";
		
		File regionFile = new File(Utils.IMPERATOR_PATH + regionFileName);

		try {
			// Read in region localisations
			Hashtable<String, String> regionLoc = Utils.readLocalisationFile(Utils.IMPERATOR_PATH + regionLocFileName);
			Hashtable<String, String> provinceLoc = Utils.readLocalisationFile(Utils.IMPERATOR_PATH + provinceLocFileName);			
			
			// Read in region file
			List<ImpRegion> regionList = new ArrayList<ImpRegion>();
			
			BufferedReader inRegionFile = new BufferedReader(new FileReader(regionFile));
			//PrintWriter outFile = new PrintWriter(new FileWriter("new" + oldFile));
			while(inRegionFile.ready()) {
				// Enter into region block
				String line = Utils.readNextLine(inRegionFile);
				String regionKey = line.split(" ")[0].trim();
				
				if (regionKey.equals("atlantic_region")) {
					break;
				}
				
				// Find "areas" block
				while(inRegionFile.ready()) {					
					line = Utils.readNextLine(inRegionFile);
					if (line.equals("areas = {")) {
					
						// Read in each province
						List<String> provinces = new ArrayList<String>();
						while(inRegionFile.ready()) {
							line = Utils.readNextLine(inRegionFile);
							if (!line.equals("}")) {
								provinces.add(provinceLoc.get(line));
							}
							else {
								break;
							}
						}
						
						Collections.sort(provinces);
						ImpRegion newRegion = new ImpRegion(regionKey, regionLoc.get(regionKey), provinces);
						regionList.add(newRegion);
						//System.out.println("Region: " + regionLoc.get(regionKey));
						//System.out.println("Areas: " + Arrays.toString(provinces.toArray()));
					}
					else if (line.equals("}")) {
						break;
					}
				}
				//System.out.println("");
			}
			
			inRegionFile.close();

			// Make
			Collections.sort(regionList);
			
			// Print out the sorted list
			for (ImpRegion region: regionList) {
				System.out.println("|-");
				System.out.println("!style=\"background-color:rgb(146,165,139)\"|" + region.name);
				System.out.print("|");
				for (int i = 0; i < region.provinceList.size(); i++) {
					System.out.print(region.provinceList.get(i));
					if (i != region.provinceList.size() - 1) {
						System.out.print(", ");		
					}
				}
				System.out.println("");
				System.out.println("|" + region.key);
			}
		} 
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		RegionFileParser newParser = new RegionFileParser();
		newParser.parseRegions();
	}

}
