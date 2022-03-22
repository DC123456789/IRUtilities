package IRParser;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import IRObjects.*;
import IRParser.TerritoryMapPainter.MapType;


public class ImperatorParser {
	
	// Temporary for storing data until we read the countries

	public static final String LOC_FOLDER_NAME = Utils.IMPERATOR_PATH + "localization\\english";
	
	public static final String CULTURE_PATH = Utils.IMPERATOR_PATH + "\\common\\cultures";
	public static final String RELIGION_PATH = Utils.IMPERATOR_PATH + "\\common\\religions";
	public static final String MIL_TRAD_PATH = Utils.IMPERATOR_PATH + "\\common\\military_traditions";
	public static final String GOVERNMENT_PATH = Utils.IMPERATOR_PATH + "\\common\\governments";
	public static final String SUBJECT_TYPES_PATH = Utils.IMPERATOR_PATH + "\\common\\subject_types";
	public static final String DEITIES_PATH = Utils.IMPERATOR_PATH + "\\common\\deities";
	public static final String MODIFIERS_PATH = Utils.IMPERATOR_PATH + "\\common\\modifiers";
	public static final String MODIFIER_ICONS_PATH = Utils.IMPERATOR_PATH + "\\common\\modifier_icons";
	public static final String PROVINCE_NAMES_PATH = Utils.IMPERATOR_PATH + "\\common\\province_names";
	public static final String TERRITORY_SETUP_PATH = Utils.IMPERATOR_PATH + "\\setup\\provinces";
	public static final String MAIN_SETUP_PATH = Utils.IMPERATOR_PATH + "\\setup\\main";
	public static final String REGION_PATH = Utils.IMPERATOR_PATH + "map_data\\regions.txt";
	public static final String PROVINCE_PATH = Utils.IMPERATOR_PATH + "map_data\\areas.txt";
	public static final String DEFAULT_MAP_PATH = Utils.IMPERATOR_PATH + "map_data\\default.map";
	public static final String LEVY_TEMPLATES_PATH = Utils.IMPERATOR_PATH + "\\common\\levy_templates";
	public static final String NAMED_COLOURS_PATH = Utils.IMPERATOR_PATH + "\\common\\named_colors";
	public static final String SCRIPT_VALUES_PATH = Utils.IMPERATOR_PATH + "\\common\\script_values";
	
	public static final String PROVINCE_MAP_PATH = Utils.IMPERATOR_PATH + "\\map_data\\provinces.png";
	public static final String PROVINCE_DEFINITION_PATH = Utils.IMPERATOR_PATH + "\\map_data\\definition.csv";
	public static final String COUNTRY_DEFINITION_PATH = Utils.IMPERATOR_PATH + "\\setup\\countries\\countries.txt";
	
	public static Hashtable<String, String> localisation;

	private List<CultureGroup> cultureGroups = new ArrayList<CultureGroup>();
	private List<Culture> cultures = new ArrayList<Culture>();
	private List<Religion> religions = new ArrayList<Religion>();
	private List<Government> governments = new ArrayList<Government>();
	private List<SubjectType> subjectTypes = new ArrayList<SubjectType>();
	private List<Region> regions = new ArrayList<Region>();
	private Hashtable<String, Region> keyToRegion = new Hashtable<String, Region>();
	private List<Province> provinces = new ArrayList<Province>();
	private List<Territory> territories = Arrays.asList(new Territory[10000]);	// Should be enough?
	private List<Deity> deities = new ArrayList<Deity>();
	private List<Modifier> modifiers = new ArrayList<Modifier>();
	private List<ModifierIcon> modifierIcons = new ArrayList<ModifierIcon>();
	private List<Country> countries = new ArrayList<Country>();
	private List<Dependency> dependencies = new ArrayList<Dependency>();
	private List<MilitaryTradition> militaryTraditions = new ArrayList<MilitaryTradition>();
	private List<Wonder> wonders = new ArrayList<Wonder>();
	private List<LevyTemplate> levyTemplates = new ArrayList<LevyTemplate>();
	private Hashtable<String, Color> namedColours = new Hashtable<String, Color>();
	private Hashtable<String, Double> scriptValues = new Hashtable<String, Double>();
	
	private BufferedImage provinceMap;
	private Hashtable<Integer, Territory> colourToTerritory = new Hashtable<Integer, Territory>();
	
	private TerritoryMapPainter mapPainter = new TerritoryMapPainter();
	private ImperatorFlagPainter flagPainter = new ImperatorFlagPainter();
	
	public void parse() {
		// Read in localisations
		localisation = Utils.readLocalisationFolder(LOC_FOLDER_NAME);

		// Read levy type files
		final File levyTemplatesFolder = new File(LEVY_TEMPLATES_PATH);
		
		try (Stream<Path> walk = Files.walk(Paths.get(LEVY_TEMPLATES_PATH))) {

	        List<String> result = new ArrayList<>();
	        
	        for (final File f : levyTemplatesFolder.listFiles()) {
	            if (f.isFile()) {
	                result.add(f.getAbsolutePath());
	            }
	        }

			for (String file : result) {
				BufferedReader inLevyTemplateFile = new BufferedReader(new FileReader(file));
				while(inLevyTemplateFile.ready()) {
					// Enter into levy template block
					String token = Utils.readNextToken(inLevyTemplateFile);
					if (token.length() == 0) {
						continue;
					}
					String levyTemplateKey = token;
					LevyTemplate newLevyTemplate = new LevyTemplate(levyTemplateKey);
					
					// Consume tokens
					Utils.readNextToken(inLevyTemplateFile);
					Utils.readNextToken(inLevyTemplateFile);
					
					// Read government parameters
					while(inLevyTemplateFile.ready()) {					
						token = Utils.readNextToken(inLevyTemplateFile);
						if (token.equals("default")) {					
							Utils.readNextToken(inLevyTemplateFile);
							Utils.consumeBlock(inLevyTemplateFile);
						}
						else if (!token.equals("}")) {
							String unitType = token;
							Utils.readNextToken(inLevyTemplateFile);
							Double proportion = Double.parseDouble(Utils.readNextToken(inLevyTemplateFile));
							newLevyTemplate.levies.add(new SimpleEntry<String, Double>(unitType, proportion));
						}
						else {		// Ending bracket
							break;
						}
					}
					levyTemplates.add(newLevyTemplate);
				}
				inLevyTemplateFile.close();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Read culture files
		final File cultureFolder = new File(CULTURE_PATH);
		
		try (Stream<Path> walk = Files.walk(Paths.get(CULTURE_PATH))) {

	        List<String> result = new ArrayList<>();
	        
	        for (final File f : cultureFolder.listFiles()) {
	            if (f.isFile()) {
	                result.add(f.getAbsolutePath());
	            }
	        }

			for (String file : result) {
				BufferedReader inCultureFile = new BufferedReader(new FileReader(file));
				while(inCultureFile.ready()) {
					// Enter into culture group block
					String token = Utils.readNextToken(inCultureFile);
					if (token.length() == 0) {
						continue;
					}
					String cultureGroupKey = token;
					
					// Consume tokens
					Utils.readNextToken(inCultureFile);
					Utils.readNextToken(inCultureFile);
					
					// Read culture group parameters
					String graphical_culture = null;
					String portraits = null;
					Color cultureGroupColour = null;
					List<Culture> groupCultures = new ArrayList<Culture>();
					String levyTemplateStr = null;
					while(inCultureFile.ready()) {					
						token = Utils.readNextToken(inCultureFile);
						if (token.equals("color")) {
							Utils.readNextToken(inCultureFile);
							String colourType = Utils.readNextToken(inCultureFile);
							Utils.readNextToken(inCultureFile);
							float c0 = Float.parseFloat(Utils.readNextToken(inCultureFile));
							float c1 = Float.parseFloat(Utils.readNextToken(inCultureFile));
							float c2 = Float.parseFloat(Utils.readNextToken(inCultureFile));
							Utils.readNextToken(inCultureFile);
							if (colourType.equals("hsv"))
								cultureGroupColour = Color.getHSBColor(c0, c1, c2);
							else if (colourType.equals("rgb"))
								cultureGroupColour = new Color(c0, c1, c2);
						}
						else if (token.equals("culture")) {
							Utils.readNextToken(inCultureFile);
							Utils.readNextToken(inCultureFile);
						
							// Read in each culture
							while(inCultureFile.ready()) {
								token = Utils.readNextToken(inCultureFile);
								if (!token.equals("}")) {
									Culture newCulture = new Culture(token, localisation.get(token));
									groupCultures.add(newCulture);
									cultures.add(newCulture);
									
									Utils.readNextToken(inCultureFile);
									Utils.readNextToken(inCultureFile);									
									// Look for levy template
									String cultureLevyTemplateStr = null;
									while(inCultureFile.ready()) {
										token = Utils.readNextToken(inCultureFile);
										if (token.equals("levy_template")) {
											Utils.readNextToken(inCultureFile);
											cultureLevyTemplateStr = Utils.readNextToken(inCultureFile);
										}
										else if (!token.equals("}")) {
											Utils.readNextToken(inCultureFile);
											Utils.consumeBlock(inCultureFile);
										}
										else {
											break;
										}
									}
									// Search for the appropriate levy template
									if (!Objects.isNull(cultureLevyTemplateStr)) {
										int levyTemplateIndex = levyTemplates.indexOf(new LevyTemplate(cultureLevyTemplateStr));
										LevyTemplate levyTemplate = null;
										if (levyTemplateIndex > -1) {
											levyTemplate = levyTemplates.get(levyTemplateIndex);
										}
										else {
											System.err.println("Could not find levy template for " + newCulture.name + 
													": " + cultureLevyTemplateStr);
										}
										newCulture.levyTemplate = levyTemplate;										
									}
								}
								else {
									break;
								}
							}
							Collections.sort(groupCultures);
						}
						else if (token.equals("graphical_culture")) {
							Utils.readNextToken(inCultureFile);
							graphical_culture = Utils.readNextToken(inCultureFile);
						}
						else if (token.equals("ethnicities")) {
							Utils.readNextToken(inCultureFile);
							Utils.readNextToken(inCultureFile);
							Utils.readNextToken(inCultureFile);
							Utils.readNextToken(inCultureFile);
							portraits = Utils.readNextToken(inCultureFile);
							while(inCultureFile.ready()) {
								token = Utils.readNextToken(inCultureFile);
								if (token.equals("}")) {
									break;
								}
							}
						}
						else if (token.equals("levy_template")) {
							Utils.readNextToken(inCultureFile);
							levyTemplateStr = Utils.readNextToken(inCultureFile);
						}
						else if (!token.equals("}")) {	
							Utils.readNextToken(inCultureFile);
							Utils.consumeBlock(inCultureFile);
						}
						else {		// Ending bracket
							break;
						}
					}
					// Search for the appropriate levy template
					int levyTemplateIndex = levyTemplates.indexOf(new LevyTemplate(levyTemplateStr));
					LevyTemplate levyTemplate = null;
					if (levyTemplateIndex > -1) {
						levyTemplate = levyTemplates.get(levyTemplateIndex);
					}
					else {
						System.err.println("Could not find levy template for " + localisation.get(cultureGroupKey) + 
													": " + levyTemplateStr);
					}
					
					CultureGroup newCultureGroup = new CultureGroup(cultureGroupKey, localisation.get(cultureGroupKey), 
							graphical_culture, portraits, groupCultures, cultureGroupColour, levyTemplate);
					for (Culture culture: newCultureGroup.cultureList) {
						culture.group = newCultureGroup;
					}
					cultureGroups.add(newCultureGroup);
				}
				inCultureFile.close();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for (Culture culture : cultures) {
			Collections.sort(culture.countries);
		}

		// Read religion files
		final File religionFolder = new File(RELIGION_PATH);
		
		try (Stream<Path> walk = Files.walk(Paths.get(RELIGION_PATH))) {

	        List<String> result = new ArrayList<>();
	        
	        for (final File f : religionFolder.listFiles()) {
	            if (f.isFile()) {
	                result.add(f.getAbsolutePath());
	            }
	        }

			for (String file : result) {
				BufferedReader inReligionFile = new BufferedReader(new FileReader(file));
				while(inReligionFile.ready()) {
					// Enter into religion block
					String token = Utils.readNextToken(inReligionFile);
					if (token.length() == 0) {
						continue;
					}
					String religionKey = token;
					
					// Consume tokens
					Utils.readNextToken(inReligionFile);
					Utils.readNextToken(inReligionFile);
					
					// Read religion parameters
					while(inReligionFile.ready()) {					
						token = Utils.readNextToken(inReligionFile);
						if (token.equals("color")) {		// Can have unexpected extra token
							while (!token.equals("}"))
								token = Utils.readNextToken(inReligionFile);
						}
						if (!token.equals("}")) {					
							Utils.readNextToken(inReligionFile);
							Utils.consumeBlock(inReligionFile);
						}
						else {		// Ending bracket
							break;
						}
					}
					Religion newReligion = new Religion(religionKey, localisation.get(religionKey));
					religions.add(newReligion);
				}
				inReligionFile.close();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Parse military traditions
		final File milTradFolder = new File(MIL_TRAD_PATH);

		try (Stream<Path> walk = Files.walk(Paths.get(MIL_TRAD_PATH))) {

	        List<String> result = new ArrayList<>();
	        
	        for (final File f : milTradFolder.listFiles()) {
	            if (f.isFile()) {
	                result.add(f.getAbsolutePath());
	            }
	        }

			for (String file : result) {
				BufferedReader inMilTradFile = new BufferedReader(new FileReader(file));
				while(inMilTradFile.ready()) {
					// Enter into military tradition block
					String token = Utils.readNextToken(inMilTradFile);
					if (token.length() == 0) {
						continue;
					}
					String milTradKey = token;
					MilitaryTradition newTradition = new MilitaryTradition(milTradKey, localisation.get(milTradKey));
					militaryTraditions.add(newTradition);

					// Consume tokens
					Utils.readNextToken(inMilTradFile);
					Utils.readNextToken(inMilTradFile);
					
					// Read military tradition parameters
					while(inMilTradFile.ready()) {	
						token = Utils.readNextToken(inMilTradFile);
						if (token.equals("allow")) {
							Utils.readNextToken(inMilTradFile);
							Utils.readNextToken(inMilTradFile);
							while(inMilTradFile.ready()) {		
								token = Utils.readNextToken(inMilTradFile);
								// Look for trigger_if block of culture groups
								if (token.equals("trigger_if") || token.equals("trigger_else_if")) {
									Utils.readNextToken(inMilTradFile);
									Utils.readNextToken(inMilTradFile);
									while(inMilTradFile.ready()) {		
										token = Utils.readNextToken(inMilTradFile);
										if (token.equals("country_culture_group")) {
											Utils.readNextToken(inMilTradFile);
											String cultureGroup = Utils.readNextToken(inMilTradFile);
											
											// Search for culture group
											int cultureGroupIndex = cultureGroups.indexOf(new CultureGroup(cultureGroup));
											if (cultureGroupIndex > -1) {
												cultureGroups.get(cultureGroupIndex).traditions = milTradKey;
											}
											else {
												System.err.println("Could not find culture group: " + cultureGroup);
											}
										}
										else if (!token.equals("}")) {
											// Consume the =
											Utils.readNextToken(inMilTradFile);
											
											// Digest the rest of the block
											Utils.consumeBlock(inMilTradFile);
										}
										else {		// Ending bracket
											break;
										}
									}
								}
								// Italic is at the top level
								else if (token.equals("country_culture_group")) {
									Utils.readNextToken(inMilTradFile);
									String cultureGroup = Utils.readNextToken(inMilTradFile);
									
									// Search for culture group
									int cultureGroupIndex = cultureGroups.indexOf(new CultureGroup(cultureGroup));
									if (cultureGroupIndex > -1) {
										cultureGroups.get(cultureGroupIndex).traditions = milTradKey;
									}
									else {
										System.err.println("Could not find culture group: " + cultureGroup);
									}
								}
								else if (!token.equals("}")) {
									// Consume the =
									Utils.readNextToken(inMilTradFile);
									
									// Digest the rest of the block
									Utils.consumeBlock(inMilTradFile);
								}
								else {		// Ending bracket
									break;
								}
							}
						}
						else if (!token.equals("}")) {
							// Consume the =
							Utils.readNextToken(inMilTradFile);
							
							// Digest the rest of the block
							Utils.consumeBlock(inMilTradFile);
						}
						else {		// Ending bracket
							break;
						}
					}
				}
				inMilTradFile.close();
			}
			
			Collections.sort(cultureGroups);

		} catch (IOException e) {
			e.printStackTrace();
		}

		// Read government files
		final File governmentFolder = new File(GOVERNMENT_PATH);
		
		try (Stream<Path> walk = Files.walk(Paths.get(GOVERNMENT_PATH))) {

	        List<String> result = new ArrayList<>();
	        
	        for (final File f : governmentFolder.listFiles()) {
	            if (f.isFile()) {
	                result.add(f.getAbsolutePath());
	            }
	        }

			for (String file : result) {
				BufferedReader inGovernmentFile = new BufferedReader(new FileReader(file));
				while(inGovernmentFile.ready()) {
					// Enter into government block
					String token = Utils.readNextToken(inGovernmentFile);
					if (token.length() == 0) {
						continue;
					}
					String governmentKey = token;
					
					// Consume tokens
					Utils.readNextToken(inGovernmentFile);
					Utils.readNextToken(inGovernmentFile);
					
					// Read government parameters
					String governmentType = "monarchy";		// Sometimes missing
					while(inGovernmentFile.ready()) {					
						token = Utils.readNextToken(inGovernmentFile);
						if (token.equals("color")) {		// Can have unexpected extra token
							while (!token.equals("}"))
								token = Utils.readNextToken(inGovernmentFile);
						}
						else if (token.equals("type")) {					
							Utils.readNextToken(inGovernmentFile);
							governmentType = Utils.readNextToken(inGovernmentFile);
						}
						else if (!token.equals("}")) {					
							Utils.readNextToken(inGovernmentFile);
							Utils.consumeBlock(inGovernmentFile);
						}
						else {		// Ending bracket
							break;
						}
					}
					Government newGovernment = new Government(governmentKey, localisation.get(governmentKey), 
							governmentType);
					governments.add(newGovernment);
				}
				inGovernmentFile.close();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		// Read subject type files
		final File subjectTypesFolder = new File(SUBJECT_TYPES_PATH);
		
		try (Stream<Path> walk = Files.walk(Paths.get(SUBJECT_TYPES_PATH))) {

	        List<String> result = new ArrayList<>();
	        
	        for (final File f : subjectTypesFolder.listFiles()) {
	            if (f.isFile()) {
	                result.add(f.getAbsolutePath());
	            }
	        }

			for (String file : result) {
				BufferedReader inSubjectTypeFile = new BufferedReader(new FileReader(file));
				while(inSubjectTypeFile.ready()) {
					// Enter into subject type block
					String token = Utils.readNextToken(inSubjectTypeFile);
					if (token.length() == 0) {
						continue;
					}
					String subjectTypeKey = token;
					
					// Consume tokens
					Utils.readNextToken(inSubjectTypeFile);
					Utils.readNextToken(inSubjectTypeFile);
					
					// Read government parameters
					while(inSubjectTypeFile.ready()) {					
						token = Utils.readNextToken(inSubjectTypeFile);
						if (!token.equals("}")) {					
							Utils.readNextToken(inSubjectTypeFile);
							Utils.consumeBlock(inSubjectTypeFile);
						}
						else {		// Ending bracket
							break;
						}
					}
					SubjectType newSubjectType = new SubjectType(subjectTypeKey, localisation.get(subjectTypeKey));
					subjectTypes.add(newSubjectType);
				}
				inSubjectTypeFile.close();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Read deity files
		final File deityFolder = new File(DEITIES_PATH);
		
		try (Stream<Path> walk = Files.walk(Paths.get(RELIGION_PATH))) {

	        List<String> result = new ArrayList<>();
	        
	        for (final File f : deityFolder.listFiles()) {
	            if (f.isFile()) {
	                result.add(f.getAbsolutePath());
	            }
	        }

			for (String file : result) {
				BufferedReader inDeityFile = new BufferedReader(new FileReader(file));
				while(inDeityFile.ready()) {
					// Enter into deity block
					String token = Utils.readNextToken(inDeityFile);
					if (token.length() == 0) {
						continue;
					}
					String deityKey = token.replace("deity_", "omen_");
					
					// Consume tokens
					Utils.readNextToken(inDeityFile);
					Utils.readNextToken(inDeityFile);
					
					// Read religion parameters
					Religion religion = null;
					while(inDeityFile.ready()) {					
						token = Utils.readNextToken(inDeityFile);
						if (token.equals("religion")) {
							Utils.readNextToken(inDeityFile);
							String relKey = Utils.readNextToken(inDeityFile);
							int religionIndex = religions.indexOf(new Religion(relKey));
							if (religionIndex > -1) {
								religion = religions.get(religionIndex);
							}
							else {
								System.err.println("Could not find religion " + relKey + " in deity " 
										+ deityKey + " in file " + file);
							}
						}
						else if (!token.equals("}")) {
							token = Utils.readNextToken(inDeityFile);
							Utils.consumeBlock(inDeityFile);
						}
						else {		// Ending bracket
							break;
						}
					}
					Deity newDeity = new Deity(deityKey, localisation.get(deityKey));
					newDeity.religion = religion;
					deities.add(newDeity);
				}
				inDeityFile.close();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}	

		// Read modifier icon files
		final File modifierIconFolder = new File(MODIFIER_ICONS_PATH);
		
		try (Stream<Path> walk = Files.walk(Paths.get(MODIFIER_ICONS_PATH))) {

	        List<String> result = new ArrayList<>();
	        
	        for (final File f : modifierIconFolder.listFiles()) {
	            if (f.isFile()) {
	                result.add(f.getAbsolutePath());
	            }
	        }

			for (String file : result) {
				BufferedReader inModifierIconFile = new BufferedReader(new FileReader(file));
				while(inModifierIconFile.ready()) {
					// Enter into icon block
					String token = Utils.readNextToken(inModifierIconFile);
					if (token.length() == 0) {
						continue;
					}
					String modifierIconKey = token;
					
					// Consume tokens
					Utils.readNextToken(inModifierIconFile);
					Utils.readNextToken(inModifierIconFile);
					
					// Read modifier parameters
					String icon = "";
					while(inModifierIconFile.ready()) {					
						token = Utils.readNextToken(inModifierIconFile);
						if (token.equals("positive")) {
							Utils.readNextToken(inModifierIconFile);
							icon = Utils.readNextToken(inModifierIconFile);
						}
						else if (!token.equals("}")) {
							Utils.readNextToken(inModifierIconFile);
							Utils.consumeBlock(inModifierIconFile);
						}
						else {		// Ending bracket
							break;
						}
					}
					ModifierIcon newModifierIcon = new ModifierIcon(modifierIconKey);
					icon = icon.substring(icon.lastIndexOf("/") + 1, icon.indexOf("."));
					icon = icon.replace('_', ' ');
					newModifierIcon.setIcon(icon);
					modifierIcons.add(newModifierIcon);
				}
				inModifierIconFile.close();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		ModifierEffect.modifierIcons = modifierIcons;
		
		// Read script value files - only the static ones for now
		File scriptValuesFolder = new File(SCRIPT_VALUES_PATH);
		
		try (Stream<Path> walk = Files.walk(Paths.get(MODIFIERS_PATH))) {

	        List<String> result = new ArrayList<>();
	        
	        for (final File f : scriptValuesFolder.listFiles()) {
	            if (f.isFile()) {
	                result.add(f.getAbsolutePath());
	            }
	        }

			for (String file : result) {
				if (file.indexOf("_script_values.info") == -1) {
					BufferedReader inScriptValueFile = new BufferedReader(new FileReader(file));
					while(inScriptValueFile.ready()) {
						// Get name of script value
						String token = Utils.readNextToken(inScriptValueFile);
						if (token.length() == 0) {
							continue;
						}
						String scriptValueKey = token;
						
						// Consume the "="
						token = Utils.readNextToken(inScriptValueFile);
						
						// Missing = in some modifiers
						if (!token.equals("=")) {
							token = Utils.readNextToken(inScriptValueFile);						
						}
						
						// Make sure we can come back to this in case we hit a block that we want to consume
						inScriptValueFile.mark(2);
						
						// Try to read in the value
						token = Utils.readNextToken(inScriptValueFile);
						if (!token.equals("{")) {
							try {
								double value = Double.parseDouble(token);	
								scriptValues.put(scriptValueKey, value);						
							}
							catch (NumberFormatException e) {
								System.err.println("Invalid script value " + token + " under key " + scriptValueKey + " in file " + file);							
							}			
						}
						else {	// If it's a block, ignore it
							inScriptValueFile.reset();
							Utils.consumeBlock(inScriptValueFile);
						}	
						
					}
					inScriptValueFile.close();
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Read modifier files
		final File modifiersFolder = new File(MODIFIERS_PATH);
		
		try (Stream<Path> walk = Files.walk(Paths.get(MODIFIERS_PATH))) {

	        List<String> result = new ArrayList<>();
	        
	        for (final File f : modifiersFolder.listFiles()) {
	            if (f.isFile()) {
	                result.add(f.getAbsolutePath());
	            }
	        }

			for (String file : result) {
				BufferedReader inModifierFile = new BufferedReader(new FileReader(file));
				while(inModifierFile.ready()) {
					// Enter into religion block
					String token = Utils.readNextToken(inModifierFile);
					if (token.length() == 0) {
						continue;
					}
					String modifierKey = token;
					Modifier newModifier = new Modifier(modifierKey);
					
					// Consume tokens
					token = Utils.readNextToken(inModifierFile);
					
					// Missing = in some modifiers
					if (token.equals("=")) {
						token = Utils.readNextToken(inModifierFile);						
					}
					
					// Read modifier parameters
					while(inModifierFile.ready()) {					
						token = Utils.readNextToken(inModifierFile);
						if (!token.equals("}")) {
							String effectKey = token;
							if (!effectKey.equals("show_in_outliner") && !effectKey.equals("cancellation_trigger") && 
									!effectKey.equals("on_cancellation_effect")) {
								int iconIndex = modifierIcons.indexOf(new ModifierIcon(effectKey));
								if (iconIndex == -1) {
									System.err.println("Could not find modifier effect " + effectKey + " in modifier " 
											+ modifierKey + " in file " + file);
									Utils.readNextToken(inModifierFile);
									Utils.consumeBlock(inModifierFile);	
								}
								else {
									Utils.readNextToken(inModifierFile);
									String value = Utils.readNextToken(inModifierFile);
									newModifier.effects.add(new ModifierEffect(effectKey, value, scriptValues));
								}
							}
							else {
								Utils.readNextToken(inModifierFile);
								Utils.consumeBlock(inModifierFile);								
							}
						}
						else {		// Ending bracket
							break;
						}
					}
					
					// Sort the effects by ingame order
					Collections.sort(newModifier.effects);
					
					modifiers.add(newModifier);
				}
				inModifierFile.close();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Read territory files
		final File territorySetupFolder = new File(TERRITORY_SETUP_PATH);
		
		try (Stream<Path> walk = Files.walk(Paths.get(TERRITORY_SETUP_PATH))) {

	        List<String> result = new ArrayList<>();
	        
	        for (final File f : territorySetupFolder.listFiles()) {
	            if (f.isFile()) {
	                result.add(f.getAbsolutePath());
	            }
	        }

			for (String file : result) {
				BufferedReader inTerritoryFile = new BufferedReader(new FileReader(file));
				while(inTerritoryFile.ready()) {
					// Enter into territory block
					String token = Utils.readNextToken(inTerritoryFile);
					if (token.length() == 0) {
						continue;
					}
					String territoryID = token;
					Territory newTerritory = new Territory(Integer.parseInt(territoryID));
					
					// Consume tokens
					Utils.readNextToken(inTerritoryFile);
					Utils.readNextToken(inTerritoryFile);
					
					// Read territory parameters
					String dominant_culture = null;
					String dominant_religion = null;
					String trade_good = null;
					boolean isPort = false;
					String rank = null;
					String terrain = null;
					Deity holy_site = null;
					while(inTerritoryFile.ready()) {					
						token = Utils.readNextToken(inTerritoryFile);
						if (token.equals("terrain")) {		// Ignore ocean tiles
							Utils.readNextToken(inTerritoryFile);
							terrain = Utils.readNextToken(inTerritoryFile).replace("\"", "");
						}
						else if (token.equals("culture")) {
							Utils.readNextToken(inTerritoryFile);
							dominant_culture = Utils.readNextToken(inTerritoryFile).replace("\"", "");
						}
						else if (token.equals("religion")) {
							Utils.readNextToken(inTerritoryFile);
							dominant_religion = Utils.readNextToken(inTerritoryFile).replace("\"", "");
						}
						else if (token.equals("nobles") || token.equals("citizen") || token.equals("freemen") 
								|| token.equals("tribesmen") || token.equals("slaves")) {
							String popType = token;
							Utils.readNextToken(inTerritoryFile);
							Utils.readNextToken(inTerritoryFile);
							
							// Read in each pop group
							String pop_culture = dominant_culture;
							String pop_religion = dominant_religion;
							int amount = 0;
							while(inTerritoryFile.ready()) {
								token = Utils.readNextToken(inTerritoryFile);
								if (token.equals("culture")) {
									Utils.readNextToken(inTerritoryFile);
									pop_culture = Utils.readNextToken(inTerritoryFile).replace("\"", "");
								}
								else if (token.equals("religion")) {
									Utils.readNextToken(inTerritoryFile);
									pop_religion = Utils.readNextToken(inTerritoryFile).replace("\"", "");
								}
								else if (token.equals("amount")) {
									Utils.readNextToken(inTerritoryFile);
									String popAmount = Utils.readNextToken(inTerritoryFile);
									try {
										amount = Integer.parseInt(popAmount);
										if (popType.equals("nobles"))
											newTerritory.numNobles += amount;
										else if (popType.equals("citizen"))
											newTerritory.numCitizens += amount;
										else if (popType.equals("freemen"))
											newTerritory.numFreemen += amount;
										else if (popType.equals("tribesmen"))
											newTerritory.numTribesmen += amount;
										else if (popType.equals("slaves"))
											newTerritory.numSlaves += amount;
									}
									catch(NumberFormatException e) {
										System.err.println("Invalid pop number in territory " + territoryID + ": " + popAmount);
									}
								}
								else if (!token.equals("}")) {
									Utils.readNextToken(inTerritoryFile);
									Utils.consumeBlock(inTerritoryFile);
								}
								else {		// Ending bracket
									break;
								}
							}
							
							newTerritory.pops.add(new Pop(popType, pop_culture, pop_religion, amount));
							
							// Find culture and increment its pop count
							int cultureIndex = cultures.indexOf(new Culture(pop_culture));
							if (cultureIndex > -1) {
								cultures.get(cultureIndex).num_pops += amount;
							}
							else {
								System.err.println("Could not find pop culture: " + pop_culture + " in province " 
										+ territoryID + " in file " + file);
							}
						}
						else if (token.equals("trade_goods")) {
							Utils.readNextToken(inTerritoryFile);
							trade_good = Utils.readNextToken(inTerritoryFile).replace("\"", "");
						}
						else if (token.equals("port_building")) {
							Utils.readNextToken(inTerritoryFile);
							if (Integer.parseInt(Utils.readNextToken(inTerritoryFile)) > 0)
								isPort = true;
						}
						else if (token.equals("province_rank")) {
							Utils.readNextToken(inTerritoryFile);
							rank = Utils.readNextToken(inTerritoryFile).replace("\"", "");
						}
						else if (token.equals("holy_site")) {
							Utils.readNextToken(inTerritoryFile);
							String holySiteKey = Utils.readNextToken(inTerritoryFile).replace("\"", "");
							int deityIndex = deities.indexOf(new Deity(holySiteKey));
							if (deityIndex > -1) {
								holy_site = deities.get(deityIndex);
							}
							else {
								System.err.println("Could not find holy site deity " + holySiteKey + " in territory " 
										+ territoryID + " in file " + file);
							}
						}
						else if (!token.equals("}")) {
							Utils.readNextToken(inTerritoryFile);
							Utils.consumeBlock(inTerritoryFile);
						}
						else {		// Ending bracket
							break;
						}
					}

					// Find culture and increment its territory count
					if (newTerritory.pops.size() > 0) {
						// Check that dominant culture is actually what it says in the files
						List<Culture> popCultures = new ArrayList<Culture>();
						for (Pop pop : newTerritory.pops) {
							Culture currentPopCulture = new Culture(pop.culture);
							int cultureIndex = popCultures.indexOf(currentPopCulture);
							if (cultureIndex > -1) {
								popCultures.get(cultureIndex).num_pops += pop.size;
							}
							else {
								currentPopCulture.num_pops += pop.size;
								popCultures.add(currentPopCulture);
							}							
						}
						
						int dominantCultureIndex = popCultures.indexOf(new Culture(dominant_culture));
						int dominantCultureSize = 0;
						// Check is required in case there are no pops of the "dominant" culture
						if (dominantCultureIndex > -1)
							dominantCultureSize = popCultures.get(popCultures.indexOf(new Culture(dominant_culture))).num_pops;
						for (Culture culture : popCultures) {
							if (culture.num_pops > dominantCultureSize) {
								dominant_culture = culture.key;
								dominantCultureSize = culture.num_pops;
							}
						}
					}
					
					int cultureIndex = cultures.indexOf(new Culture(dominant_culture));
					Culture culture = null;
					if (cultureIndex > -1) {
						culture = cultures.get(cultureIndex);
					}
					else if (Objects.nonNull(dominant_culture) && !dominant_culture.equals("")){
						System.err.println("Could not find province culture: " + dominant_culture + " in province " 
								+ territoryID + " in file " + file);
					}

					if (newTerritory.pops.size() > 0) {
						// Check that dominant religion is actually what it says in the files
						List<Religion> popReligions = new ArrayList<Religion>();
						for (Pop pop : newTerritory.pops) {
							Religion currentPopReligion = new Religion(pop.religion);
							int religionIndex = popReligions.indexOf(currentPopReligion);
							if (religionIndex > -1) {
								popReligions.get(religionIndex).num_pops += pop.size;
							}
							else {
								currentPopReligion.num_pops += pop.size;
								popReligions.add(currentPopReligion);
							}							
						}

						int religionIndex = popReligions.indexOf(new Religion(dominant_religion));
						int dominantReligionSize = 0;
						if (religionIndex > -1) {
							dominantReligionSize = popReligions.get(religionIndex).num_pops;
						}
						for (Religion religion : popReligions) {
							if (religion.num_pops > dominantReligionSize) {
								dominant_religion = religion.key;
								dominantReligionSize = religion.num_pops;
							}
						}
					}
					
					newTerritory.culture = culture;
					newTerritory.religion = new Religion(dominant_religion);
					newTerritory.tradeGood = trade_good;
					newTerritory.isPort = isPort;
					newTerritory.status = rank;
					newTerritory.terrain = terrain;
					newTerritory.holySite = holy_site;
					
					territories.set(Integer.parseInt(territoryID), newTerritory);
				}
				inTerritoryFile.close();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Parse map file for uninhabitable provinces
		File mapDefaultFile = new File(DEFAULT_MAP_PATH);
		try {			
			BufferedReader inDefaultMapFile = new BufferedReader(new FileReader(mapDefaultFile));
			//PrintWriter outFile = new PrintWriter(new FileWriter("new" + oldFile));
			while(inDefaultMapFile.ready()) {
				String token = Utils.readNextToken(inDefaultMapFile).toLowerCase();
				if (token.length() == 0) {
					continue;
				}
				
				// Check for uninhabitable province categories				
				if (token.equals("sea_zones") || token.equals("wasteland") || token.equals("impassable_terrain") || 
						token.equals("uninhabitable") || token.equals("lakes") || token.equals("river_provinces")) {
					String territoryType = token;
					Utils.readNextToken(inDefaultMapFile);
					token = Utils.readNextToken(inDefaultMapFile);
					
					if (token.equals("LIST")) {
						Utils.readNextToken(inDefaultMapFile);
						// Read in each province
						while(inDefaultMapFile.ready()) {
							token = Utils.readNextToken(inDefaultMapFile);
							if (!token.equals("}")) {
								Territory territory = territories.get(Integer.parseInt(token));
								if (Objects.nonNull(territory)) {
									territory.territoryType = territoryType;
								}
							}
							else {
								break;
							}
						}						
					}
					else if (token.equals("RANGE")) {
						Utils.readNextToken(inDefaultMapFile);
						// Read in the two provinces
						int rangeBegin = Integer.parseInt(Utils.readNextToken(inDefaultMapFile));
						int rangeEnd = Integer.parseInt(Utils.readNextToken(inDefaultMapFile));
						Utils.readNextToken(inDefaultMapFile);
						for (int i = rangeBegin; i <= rangeEnd; i++) {
							Territory territory = territories.get(i);
							if (Objects.nonNull(territory)) {
								territory.territoryType = territoryType;
							}							
						}				
					}
					else {
						System.err.println("Unknown territory set parameter " + token + " in default map file");						
					}
				
				}
				//System.out.println("");
			}
			
			inDefaultMapFile.close();
		} 
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Count territories for each culture
		for (Territory territory : territories) {
			if (Objects.nonNull(territory) && territory.territoryType.equals("inhabitable")) {
				if (Objects.isNull(territory.culture)) {
					System.err.println(territory.name + "has no culture");
				}
				territory.culture.num_provinces += 1;
			}
		}
		
		// Parse Province file
		File provinceFile = new File(PROVINCE_PATH);
		try {			
			BufferedReader inProvinceFile = new BufferedReader(new FileReader(provinceFile));
			//PrintWriter outFile = new PrintWriter(new FileWriter("new" + oldFile));
			while(inProvinceFile.ready()) {
				// Enter into region block
				String token = Utils.readNextToken(inProvinceFile);
				if (token.length() == 0) {
					continue;
				}
				Utils.readNextToken(inProvinceFile);
				Utils.readNextToken(inProvinceFile);
				
				Province newProvince = new Province(token, localisation.get(token));
				provinces.add(newProvince);				
				
				// Find "provinces" block
				while(inProvinceFile.ready()) {					
					token = Utils.readNextToken(inProvinceFile);
					if (token.equals("provinces")) {
						Utils.readNextToken(inProvinceFile);
						Utils.readNextToken(inProvinceFile);
					
						// Read in each province
						while(inProvinceFile.ready()) {
							token = Utils.readNextToken(inProvinceFile);
							if (!token.equals("}")) {
								int territoryIndex = territories.indexOf(new Territory(token));
								if (territoryIndex > -1) {
									Territory currentTerritory = territories.get(territoryIndex);
									newProvince.territories.add(currentTerritory);
									territories.get(territoryIndex).province = newProvince;
									
									// We also want to store the territories in the order of where they appear in the province file
									Territory.TerritoryToGeoIndex.put(currentTerritory, Territory.TerritoryToGeoIndex.size());
								}
								else {
									System.err.println("Could not find territory: " + token + " in province " + newProvince.key);									
								}
							}
							else {
								break;
							}
						}
						
						Collections.sort(newProvince.territories);
					}
					else if (token.equals("}")) {
						break;
					}
				}
				//System.out.println("");
			}
			
			inProvinceFile.close();
		} 
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Parse region file
		File regionFile = new File(REGION_PATH);
		try {			
			BufferedReader inRegionFile = new BufferedReader(new FileReader(regionFile));
			//PrintWriter outFile = new PrintWriter(new FileWriter("new" + oldFile));
			while(inRegionFile.ready()) {
				// Enter into region block
				String line = Utils.readNextLine(inRegionFile);
				String regionKey = line.split(" ")[0].trim();
				
				// Ignore ocean regions
				if (regionKey.equals("atlantic_region")) {
					break;
				}
				
				Region newRegion = new Region(regionKey, localisation.get(regionKey));
				regions.add(newRegion);
				
				// Find "areas" block
				while(inRegionFile.ready()) {					
					line = Utils.readNextLine(inRegionFile);
					if (line.equals("areas = {")) {
					
						// Read in each province
						while(inRegionFile.ready()) {
							line = Utils.readNextLine(inRegionFile);
							if (!line.equals("}")) {
								int provinceIndex = provinces.indexOf(new Province(line));
								if (provinceIndex > -1) {
									newRegion.provinces.add(provinces.get(provinceIndex));
									provinces.get(provinceIndex).region = newRegion;
								}
								else {
									System.err.println("Could not find province: " + line + " in region " + regionKey);									
								}
							}
							else {
								break;
							}
						}
						
						Collections.sort(newRegion.provinces);
					}
					else if (line.equals("}")) {
						break;
					}
				}
				//System.out.println("");
			}
			
			inRegionFile.close();

			Collections.sort(regions);
			for (Region region: regions) {
				keyToRegion.put(region.key, region);
			}
			// Make access table
		} 
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Read main setup files
		final File mainSetupFolder = new File(MAIN_SETUP_PATH);
		
		try (Stream<Path> walk = Files.walk(Paths.get(MAIN_SETUP_PATH))) {

	        List<String> result = new ArrayList<>();
	        
	        for (final File f : mainSetupFolder.listFiles()) {
	            if (f.isFile()) {
	                result.add(f.getAbsolutePath());
	            }
	        }

			for (String file : result) {
				BufferedReader inMainSetupFile = new BufferedReader(new FileReader(file));
				while(inMainSetupFile.ready()) {
					// Enter into block
					String token = Utils.readNextToken(inMainSetupFile);
					if (token.length() == 0) {
						continue;
					}
					
					if (token.equals("provinces")) {						
						// Consume tokens
						Utils.readNextToken(inMainSetupFile);
						Utils.readNextToken(inMainSetupFile);

						while(inMainSetupFile.ready()) {
							token = Utils.readNextToken(inMainSetupFile);
							if (token.length() == 0) {
								continue;
							}	
							else if (token.equals("}")) {
								break;
							}
							int territoryIndex = territories.indexOf(new Territory(token));
							if (territoryIndex == -1) {
								System.err.println("Could not find territory " + token + " in " + file);
								Utils.readNextToken(inMainSetupFile);
								Utils.consumeBlock(inMainSetupFile);
								continue;
							}
							Territory currentTerritory = territories.get(territoryIndex);
							
							Utils.readNextToken(inMainSetupFile);
							Utils.readNextToken(inMainSetupFile);				
							
							// Read territory setup
							while(inMainSetupFile.ready()) {			
								token = Utils.readNextToken(inMainSetupFile);
								if (token.equals("modifier")) {
									Utils.readNextToken(inMainSetupFile);
									Utils.readNextToken(inMainSetupFile);
									while(inMainSetupFile.ready()) {
										token = Utils.readNextToken(inMainSetupFile);
										if (token.equals("modifier")) {
											Utils.readNextToken(inMainSetupFile);
											token = Utils.readNextToken(inMainSetupFile);
											int modifierIndex = modifiers.indexOf(new Modifier(token));
											if (modifierIndex > -1) {
												Modifier currModifier = modifiers.get(modifierIndex);
												currentTerritory.modifiers.add(currModifier);
												currModifier.territories.add(currentTerritory);
											}
											else {
												System.err.println("Could not find modifier " + token + " in territory " + currentTerritory.id);									
											}
										}
										else if (!token.equals("}")) {
											Utils.readNextToken(inMainSetupFile);
											Utils.consumeBlock(inMainSetupFile);
										}
										else {
											break;
										}
									}
								}
								else if (token.equals("great_work")) {
									Utils.readNextToken(inMainSetupFile);
									currentTerritory.wonderIndex = Integer.parseInt(Utils.readNextToken(inMainSetupFile));
								}
								else if (!token.equals("}")) {
									Utils.readNextToken(inMainSetupFile);
									Utils.consumeBlock(inMainSetupFile);
								}
								else {		// Ending bracket
									break;
								}
							}
						}
					}
					else if (token.equals("diplomacy")) {
						// Consume tokens
						Utils.readNextToken(inMainSetupFile);
						Utils.readNextToken(inMainSetupFile);

						while(inMainSetupFile.ready()) {
							token = Utils.readNextToken(inMainSetupFile);
							if (token.equals("dependency")) {	
								Utils.readNextToken(inMainSetupFile);
								Utils.readNextToken(inMainSetupFile);			
								
								// Read dependency parameters
								String suzerain = null;
								String subject = null;
								String subjectType = null;
								while(inMainSetupFile.ready()) {			
									token = Utils.readNextToken(inMainSetupFile);
									if (token.equals("first")) {
										Utils.readNextToken(inMainSetupFile);
										suzerain = Utils.readNextToken(inMainSetupFile);
									}
									else if (token.equals("second")) {
										Utils.readNextToken(inMainSetupFile);
										subject = Utils.readNextToken(inMainSetupFile);
									}
									else if (token.equals("subject_type")) {
										Utils.readNextToken(inMainSetupFile);
										subjectType = Utils.readNextToken(inMainSetupFile);
									}
									else if (!token.equals("}")) {
										Utils.readNextToken(inMainSetupFile);
										Utils.consumeBlock(inMainSetupFile);
									}
									else {		// Ending bracket
										break;
									}
								}
		
								// Find and process dependency references
								dependencies.add(new Dependency(suzerain, subject, subjectType));
							}
							else if (!token.equals("}")) {
								Utils.readNextToken(inMainSetupFile);
								Utils.consumeBlock(inMainSetupFile);
							}
							else {		// Ending bracket
								break;
							}
						}
					}
					else if (token.equals("country")) {						
						// Consume tokens
						Utils.readNextToken(inMainSetupFile);
						Utils.readNextToken(inMainSetupFile);

						while(inMainSetupFile.ready()) {
							token = Utils.readNextToken(inMainSetupFile);
							if (token.equals("countries")) {	
								Utils.readNextToken(inMainSetupFile);
								Utils.readNextToken(inMainSetupFile);

								while(inMainSetupFile.ready()) {
									token = Utils.readNextToken(inMainSetupFile);
									if (token.length() == 0) {
										continue;
									}	
									else if (token.equals("}")) {
										break;
									}
									String countryTag = token;
									Country newCountry = new Country(countryTag, localisation.get(countryTag));
									countries.add(newCountry);
									Utils.readNextToken(inMainSetupFile);
									Utils.readNextToken(inMainSetupFile);				
									
									// Read country parameters
									String culture = null;
									String religion = null;
									String government = null;
									int capital = 0;
									while(inMainSetupFile.ready()) {			
										token = Utils.readNextToken(inMainSetupFile);
										if (token.equals("primary_culture")) {
											Utils.readNextToken(inMainSetupFile);
											culture = Utils.readNextToken(inMainSetupFile);
										}
										else if (token.equals("religion")) {
											Utils.readNextToken(inMainSetupFile);
											religion = Utils.readNextToken(inMainSetupFile);
										}
										else if (token.equals("government")) {
											Utils.readNextToken(inMainSetupFile);
											government = Utils.readNextToken(inMainSetupFile);
										}
										else if (token.equals("capital")) {
											Utils.readNextToken(inMainSetupFile);
											capital = Integer.parseInt(Utils.readNextToken(inMainSetupFile));
										}
										else if (token.equals("own_control_core")) {
											Utils.readNextToken(inMainSetupFile);
											Utils.readNextToken(inMainSetupFile);
											while(inMainSetupFile.ready()) {
												token = Utils.readNextToken(inMainSetupFile);
												if (!token.equals("}")) {
													int territoryIndex = territories.indexOf(new Territory(token));
													if (territoryIndex > -1) {
														Territory currentTerritory = territories.get(territoryIndex);
														currentTerritory.owner = newCountry;
														newCountry.territories.add(currentTerritory);
													}
													else {
														System.err.println("Could not find territory " + token + " owned by " + countryTag);									
													}
												}
												else {
													break;
												}
											}
										}
										else if (!token.equals("}")) {
											Utils.readNextToken(inMainSetupFile);
											Utils.consumeBlock(inMainSetupFile);
										}
										else {		// Ending bracket
											break;
										}
									}
			
									// Find culture and add the country to the list
									if (!countryTag.equals("BAR")) {	// Barbarians
										int cultureIndex = cultures.indexOf(new Culture(culture));
										if (cultureIndex > -1) {
											cultures.get(cultureIndex).countries.add(newCountry);
											newCountry.culture = cultures.get(cultureIndex);
										}
										else {
											System.err.println("Could not find country culture: " + culture + " of tag " 
													+ countryTag);
										}

										int religionIndex = religions.indexOf(new Religion(religion));
										if (religionIndex > -1) {
											religions.get(religionIndex).countries.add(newCountry);
											newCountry.religion = religions.get(religionIndex);
										}
										else {
											System.err.println("Could not find country religion: " + religion + " of tag " 
													+ countryTag);
										}

										int governmentIndex = governments.indexOf(new Government(government));
										if (governmentIndex > -1) {
											newCountry.government = governments.get(governmentIndex);
										}
										else {
											System.err.println("Could not find country government: " + government + " of tag " 
													+ countryTag);
										}

										try {
											newCountry.capital = territories.get(capital);
										}
										catch (Exception e) {
											System.err.println("Could not find country capital: " + capital + " of tag " 
													+ countryTag);
										}
									}
								}
							}
							else if (!token.equals("}")) {
								Utils.readNextToken(inMainSetupFile);
								Utils.consumeBlock(inMainSetupFile);
							}
							else {		// Ending bracket
								break;
							}
						}
					}
					else if (token.equals("great_work_manager")) {
						// Consume tokens
						Utils.readNextToken(inMainSetupFile);
						Utils.readNextToken(inMainSetupFile);

						while(inMainSetupFile.ready()) {
							token = Utils.readNextToken(inMainSetupFile);
							if (token.equals("great_works_database")) {	
								Utils.readNextToken(inMainSetupFile);
								Utils.readNextToken(inMainSetupFile);			

								while(inMainSetupFile.ready()) {
									token = Utils.readNextToken(inMainSetupFile);
									if (!token.equals("}")) {
										Utils.readNextToken(inMainSetupFile);
										Utils.readNextToken(inMainSetupFile);
										
										// Read wonder details
										int wonderIndex = Integer.parseInt(token);
										String key = null;
										String nameKey = null;
										String dlc = null;
										while(inMainSetupFile.ready()) {		
											token = Utils.readNextToken(inMainSetupFile);
											if (token.equals("key")) {
												Utils.readNextToken(inMainSetupFile);
												key = Utils.readNextToken(inMainSetupFile);
											}
											else if (token.equals("great_work_name")) {
												Utils.readNextToken(inMainSetupFile);
												Utils.readNextToken(inMainSetupFile);
												while(inMainSetupFile.ready()) {		
													token = Utils.readNextToken(inMainSetupFile);
													if (token.equals("name")) {
														Utils.readNextToken(inMainSetupFile);
														nameKey = Utils.readNextToken(inMainSetupFile);
													}
													else if (!token.equals("}")) {
														Utils.readNextToken(inMainSetupFile);
														Utils.consumeBlock(inMainSetupFile);
													}
													else {		// Ending bracket
														break;
													}
												}
											}
											else if (token.equals("dlc")) {
												Utils.readNextToken(inMainSetupFile);
												dlc = Utils.readNextToken(inMainSetupFile);
											}
											else if (!token.equals("}")) {
												Utils.readNextToken(inMainSetupFile);
												Utils.consumeBlock(inMainSetupFile);
											}
											else {		// Ending bracket
												break;
											}
										}
										
										// Add the wonder to the list
										Wonder newWonder = new Wonder(key, nameKey);
										newWonder.DLC = dlc;
										
										// Make sure list is big enough to hold the index (maybe make into hash table?)
										while (wonders.size() <= wonderIndex)
											wonders.add(null);
										wonders.set(wonderIndex, newWonder);
									}
									else {		// Ending bracket
										break;
									}
								}
							}
							else if (!token.equals("}")) {
								Utils.readNextToken(inMainSetupFile);
								Utils.consumeBlock(inMainSetupFile);
							}
							else {		// Ending bracket
								break;
							}
						}
					}
					else if (!token.equals("}")) {
						Utils.readNextToken(inMainSetupFile);
						Utils.consumeBlock(inMainSetupFile);
					}
					else {		// Ending bracket
						break;
					}
				}
				inMainSetupFile.close();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		// Sort the territories in each modifier by "geographical" order
		for (Modifier modifier: modifiers) {
			if (modifier.territories.size() > 0) {
				Collections.sort(modifier.territories, new Comparator<Territory>() {
				    @Override
				    public int compare(Territory lhs, Territory rhs) {
				    	return lhs.geoCompareTo(rhs);
				    }
				});						
			}
			
		}
		
		// Sort the modifiers (need to do this after parsing which modifiers are in each territory)
		Collections.sort(modifiers);
		
		// Disambiguate country names
		for (int i = 0; i < countries.size(); i++) {
			Country currentCountry = countries.get(i);
			for (int j = 0; j < i; j++) {
				Country otherCountry = countries.get(j);
				if (currentCountry.name.equals(otherCountry.name)) {
					currentCountry.properName = currentCountry.name + " (" + 
							currentCountry.capital.province.region.getGeneralArea() + ")";
					if (otherCountry.name.equals(otherCountry.properName)) {
						otherCountry.properName = otherCountry.name + " (" + 
								otherCountry.capital.province.region.getGeneralArea() + ")";
					}
				}
			}
			if (currentCountry.properName == null) {
				currentCountry.properName = currentCountry.name;
			}
		}
		Collections.sort(countries);
		
		// Read dependencies
		for (Dependency dependency : dependencies) {
			int subjectIndex = countries.indexOf(new Country(dependency.subject));
			if (subjectIndex > -1) {
				Country subjectCountry = countries.get(subjectIndex);

				int suzerainIndex = countries.indexOf(new Country(dependency.suzerain));
				if (suzerainIndex > -1) {
					subjectCountry.suzerain = countries.get(suzerainIndex);
				}
				else {
					System.err.println("Could not find dependency suzerain: " + dependency.suzerain);
				}

				int subjectTypeIndex = subjectTypes.indexOf(new SubjectType(dependency.subjectType));
				if (subjectTypeIndex > -1) {
					subjectCountry.subjectType = subjectTypes.get(subjectTypeIndex);
				}
				else {
					System.err.println("Could not find dependency type: " + dependency.subjectType);
				}
			}
			else {
				System.err.println("Could not find dependency subject: " + dependency.subject);
			}
		}

		// Read territory name files
		final File territoryNamesFolder = new File(PROVINCE_NAMES_PATH);
		
		try (Stream<Path> walk = Files.walk(Paths.get(PROVINCE_NAMES_PATH))) {

	        List<String> result = new ArrayList<>();
	        
	        for (final File f : territoryNamesFolder.listFiles()) {
	            if (f.isFile()) {
	                result.add(f.getAbsolutePath());
	            }
	        }

			for (String file : result) {
				BufferedReader inTerritoryNamesFile = new BufferedReader(new FileReader(file));
				while(inTerritoryNamesFile.ready()) {
					// Enter into territory name block
					String token = Utils.readNextToken(inTerritoryNamesFile);
					if (token.length() == 0) {
						continue;
					}
					String cultureOrCountryKey = token;
					CulturalNamer ownerParameter = null;
					
					// Look for culture group first
					int cultureGroupIndex = cultureGroups.indexOf(new CultureGroup(cultureOrCountryKey));
					if (cultureGroupIndex > -1) {
						ownerParameter = cultureGroups.get(cultureGroupIndex);
					}

					// Look for cultures next
					if (Objects.isNull(ownerParameter)) {
						int cultureIndex = cultures.indexOf(new Culture(cultureOrCountryKey));
						if (cultureIndex > -1) {
							ownerParameter = cultures.get(cultureIndex);
						}
					}

					// Look for countries last
					if (Objects.isNull(ownerParameter)) {
						int countryIndex = countries.indexOf(new Country(cultureOrCountryKey));
						if (countryIndex > -1) {
							ownerParameter = countries.get(countryIndex);
						}
						else {
							System.err.println("Could not find cultural namer tag " + cultureOrCountryKey + " in file " + file);
						}
					}
					
					// Consume tokens
					token = Utils.readNextToken(inTerritoryNamesFile);
					token = Utils.readNextToken(inTerritoryNamesFile);
					
					// Read cultural names
					while(inTerritoryNamesFile.ready()) {					
						token = Utils.readNextToken(inTerritoryNamesFile);
						if (!token.equals("}")) {
							int territoryIndex = territories.indexOf(new Territory(token));
							if (territoryIndex == -1) {
								System.err.println("Could not find territory " + token + " in " + file);
								Utils.readNextToken(inTerritoryNamesFile);
								Utils.consumeBlock(inTerritoryNamesFile);
								continue;
							}
							Territory currentTerritory = territories.get(territoryIndex);
							Utils.readNextToken(inTerritoryNamesFile);
							String culturalNameKey = Utils.readNextToken(inTerritoryNamesFile);
							CulturalName newCulturalName = new CulturalName(ownerParameter, culturalNameKey);
							currentTerritory.culturalNames.add(newCulturalName);
						}
						else {		// Ending bracket
							break;
						}
					}
				}
				inTerritoryNamesFile.close();
			}
			
			// Read province definitions
			File provinceDefinitionFile = new File(PROVINCE_DEFINITION_PATH);
			try {			
				BufferedReader inProvinceDefinitionFile = new BufferedReader(new FileReader(provinceDefinitionFile));
				//PrintWriter outFile = new PrintWriter(new FileWriter("new" + oldFile));
				while(inProvinceDefinitionFile.ready()) {
					// Read province definition line
					String line = Utils.readNextLine(inProvinceDefinitionFile);
					if (line.length() > 0) {
						String[] provinceDef = line.split(";");
						
						int provinceID = Integer.parseInt(provinceDef[0].trim());
						int provinceColour = new Color(Integer.parseInt(provinceDef[1].trim()), 
								Integer.parseInt(provinceDef[2].trim()),
								Integer.parseInt(provinceDef[3].trim())).getRGB();
						if (provinceID > 0) {
							territories.get(provinceID).colour = provinceColour;
							colourToTerritory.put(provinceColour, territories.get(provinceID));
							if ((provinceDef[4].equals("SEAZONE IMPASSABLE WASTELAND") 
									|| provinceDef[6].equals("SEAZONE IMPASSABLE WASTELAND")
									|| provinceDef[4].equals("IMPASSIBLE SEA"))
									&& territories.get(provinceID).territoryType.equals("wasteland")) {
								territories.get(provinceID).territoryType = "sea_wasteland";
							}
						}						
					}
				}
				
				inProvinceDefinitionFile.close();
			} 
			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// Read province map
			provinceMap = ImageIO.read(new File(PROVINCE_MAP_PATH));
			
			// Calculate coastal territories
			BufferedImage blankMap = new BufferedImage(provinceMap.getWidth(), provinceMap.getHeight(), provinceMap.getType());
			for (int x = 0; x < blankMap.getWidth(); x++) {
				for (int y = 0; y < blankMap.getHeight(); y++) {
					int currentColour = provinceMap.getRGB(x, y);
					Territory territory = colourToTerritory.get(currentColour);
					if ((y > 0) && colourToTerritory.get(provinceMap.getRGB(x, y - 1)).territoryType.equals("sea_zones") || 
							(y < blankMap.getHeight() - 1) && colourToTerritory.get(provinceMap.getRGB(x, y + 1)).territoryType.equals("sea_zones") ||
							(x > 0) && colourToTerritory.get(provinceMap.getRGB(x - 1, y)).territoryType.equals("sea_zones") ||
							(x < blankMap.getWidth() - 1) && colourToTerritory.get(provinceMap.getRGB(x + 1, y)).territoryType.equals("sea_zones"))
						territory.isCoastal = true;
				}
			}

			// Read country definitions
			File countryIndexFile = new File(COUNTRY_DEFINITION_PATH);
			ArrayList<String> missingTags = new ArrayList<String>();
			try {			
				BufferedReader inCountryIndexFile = new BufferedReader(new FileReader(countryIndexFile));
				//PrintWriter outFile = new PrintWriter(new FileWriter("new" + oldFile));
				while(inCountryIndexFile.ready()) {
					// Read in the actual file
					String token = Utils.readNextToken(inCountryIndexFile);
					
					// The first token is always the tag
					int countryIndex = countries.indexOf(new Country(token));
					if (countryIndex > -1) {
						Country country = countries.get(countryIndex);
						
						// Read in the actual file where the information is
						Utils.readNextToken(inCountryIndexFile);
						String filePath = Utils.readNextToken(inCountryIndexFile);
						File countryDefinitionFile = new File(Utils.IMPERATOR_PATH + filePath);
						BufferedReader inCountryDefFile = new BufferedReader(new FileReader(countryDefinitionFile));
						while(inCountryDefFile.ready()) {		
							token = Utils.readNextToken(inCountryDefFile);
							if (token.equals("color")) {
								Utils.readNextToken(inCountryDefFile);
								String colourType = Utils.readNextToken(inCountryDefFile);
								Utils.readNextToken(inCountryDefFile);
								float colour0 = Float.parseFloat(Utils.readNextToken(inCountryDefFile));
								float colour1 = Float.parseFloat(Utils.readNextToken(inCountryDefFile));
								float colour2 = Float.parseFloat(Utils.readNextToken(inCountryDefFile));
								Utils.readNextToken(inCountryDefFile);
								if (colourType.equals("rgb")) {
									int red = Math.min(Math.round(colour0), 255);
									int green = Math.min(Math.round(colour1), 255);
									int blue = Math.min(Math.round(colour2), 255);
									country.colour = (new Color(red, green, blue)).getRGB();
								}
								else if (colourType.equals("hsv")) {
									country.colour = Color.getHSBColor(colour0, colour1, colour2).getRGB();
								}
								else {
									System.err.println("Unknown colour type " + colourType + " in file " + countryDefinitionFile);									
								}
							}
							else if (token.equals("color2")) {
								Utils.readNextToken(inCountryDefFile);
								Utils.readNextToken(inCountryDefFile);		// Colour type
								Utils.consumeBlock(inCountryDefFile);
							}
							else if (token.length() > 0) {
								Utils.readNextToken(inCountryDefFile);
								Utils.consumeBlock(inCountryDefFile);
							}
						}
					}
					else if (token.length() > 0) {
						missingTags.add(token);
						Utils.readNextToken(inCountryIndexFile);
						Utils.readNextToken(inCountryIndexFile);
					}
					
				}
				
				inCountryIndexFile.close();
			} 
			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (missingTags.size() > 0) {
				System.err.print("Could not find country tags");
				for (String tag : missingTags)
					System.err.print(" " + tag);
				System.err.print(" in file " + COUNTRY_DEFINITION_PATH);
				System.err.println("");
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Read named colours
		File namedColoursFolder = new File(NAMED_COLOURS_PATH);

		try (Stream<Path> walk = Files.walk(Paths.get(NAMED_COLOURS_PATH))) {
	        List<String> result = new ArrayList<>();
	        
	        for (final File f : namedColoursFolder.listFiles()) {
	            if (f.isFile()) {
	                result.add(f.getAbsolutePath());
	            }
	        }

			for (String file : result) {
				BufferedReader inNamedColoursFile = new BufferedReader(new FileReader(file));
				//PrintWriter outFile = new PrintWriter(new FileWriter("new" + oldFile));
				while(inNamedColoursFile.ready()) {
					// Skip enclosing colours bracket
					String token = Utils.readNextToken(inNamedColoursFile);
					if (token.equals("colors")) {
						Utils.readNextToken(inNamedColoursFile);
						Utils.readNextToken(inNamedColoursFile);					
						
						while(inNamedColoursFile.ready()) {
							token = Utils.readNextToken(inNamedColoursFile);
							if (token.length() > 0 && !token.equals("}")) {
								String colourName = token;
								Utils.readNextToken(inNamedColoursFile);
								String colourType = Utils.readNextToken(inNamedColoursFile);
								
								// Colour type not always there
								if (colourType.equals("{")) {
									colourType = "rgb";
								}
								else {
									Utils.readNextToken(inNamedColoursFile);
								}
								float colour0 = Float.parseFloat(Utils.readNextToken(inNamedColoursFile));
								float colour1 = Float.parseFloat(Utils.readNextToken(inNamedColoursFile));
								float colour2 = Float.parseFloat(Utils.readNextToken(inNamedColoursFile));
								Utils.readNextToken(inNamedColoursFile);
								
								Color colour = null;
								if (colourType.equals("rgb")) {
									int red = Math.min(Math.round(colour0), 255);
									int green = Math.min(Math.round(colour1), 255);
									int blue = Math.min(Math.round(colour2), 255);
									colour = new Color(red, green, blue);
								}
								else if (colourType.equals("hsv")) {
									colour = Color.getHSBColor(colour0, colour1, colour2);
								}
								else {
									System.err.println("Unknown colour type " + colourType + " in file " + file);									
								}
								
								namedColours.put(colourName, colour);
							}
							else if (token.equals("}")) {
								break;
							}
						}
					}					
				}
				
				inNamedColoursFile.close();
			} 
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Make sure all error messages have been output
		System.err.flush();
		try {
			TimeUnit.MILLISECONDS.sleep(200);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void writeRegionTables(String region, FileWriter writer) throws IOException {
		int regionIndex = regions.indexOf(new Region(region));
		if (regionIndex > -1) {
			Region printRegion = regions.get(regionIndex);
			writer.write("== " + printRegion.name + " ==\n");
			for (Province province : printRegion.provinces) {
				if (province.territories.size() > 0)
					writeProvinceTable(province, writer);
			}
		}		
	}
	
	public void writeProvinceTable(Province province, FileWriter writer) throws IOException {
		writer.write("=== " + province.name + " === <!-- " + province.key + "-->\n");
		writer.write("{{Regions table\n");
		writer.write("|rows =\n");
		for (Territory territory : province.territories) {
			if (!territory.territoryType.equals("inhabitable")) {
				continue;
			}
			writer.write("{{RTR|" + territory.id);
			if (!Objects.isNull(territory.name))
				writer.write("|" + territory.name);
			else
				writer.write("|''Unnamed''");
			if (territory.status.equals("city_metropolis"))
				writer.write("|metropolis");
			else
				writer.write("|" + territory.status);
			if (territory.owner != null)
				writer.write("|" + territory.owner.properName);
			else
				writer.write("|Uncolonized");					
			writer.write("|" + territory.numNobles);
			writer.write("|" + territory.numCitizens);
			writer.write("|" + territory.numFreemen);
			writer.write("|" + territory.numTribesmen);
			writer.write("|" + territory.numSlaves);
			writer.write("|" + territory.getPopulation());
			writer.write("|" + territory.culture.name);
			writer.write("|" + territory.religion.name);
			writer.write("|{{icon|" + territory.terrain + "}} " + localisation.get(territory.terrain));
			writer.write("|" + localisation.get(territory.tradeGood));
			writer.write("|");
			if (territory.wonderIndex > -1) {
				Wonder wonder = territory.getWonder(wonders);
				writer.write(" {{icon|wonder}} " + wonder.getName());
				if (!Objects.isNull(wonder.DLC))
					writer.write(" " + Utils.dlcToIconName(wonder.DLC));
				writer.write(" <br>");
			}
			if (territory.isPort) {
				writer.write(" {{icon|port}} Port <br>");
			}
			if (!Objects.isNull(territory.holySite) && !Objects.isNull(territory.owner)) {
				writer.write(" {{icon|holy site}} " + territory.holySite.name + " ([[" +
						territory.holySite.religion.name + "]])<br>");
			}
			for (Modifier modifier : territory.modifiers) {
				writer.write(" [[File:" + modifier.getIcon(modifierIcons) + ".png|28px]] " + modifier.name + "<br>");				
			}
			writer.write("|");
			for (CulturalName culturalName : territory.culturalNames) {
				writer.write(" " + culturalName.name);	
				if (culturalName.cultureOrTag instanceof CultureGroup || culturalName.cultureOrTag instanceof Culture) {
					writer.write(" (" + culturalName.cultureOrTag.getName() + ")");
				}	
				else if (culturalName.cultureOrTag instanceof Country) {
					writer.write(" ({{flag|" + culturalName.cultureOrTag.getName() + "}})");
				}
				writer.write("<br>");	
			}
			writer.write("}}\n");
		}
		writer.write("}}\n\n");
	}
	
	public void printCultureTable() {
		System.out.println("{| class=\"wikitable sortable\"");
		System.out.println("|-");
		System.out.println("! Group");
		System.out.println("! Traditions");
		System.out.println("! Portraits");
		System.out.println("! Graphical culture");
		System.out.println("! Cultures");
		System.out.println("! Starting <br> Pops");
		System.out.println("! Starting <br> Territories");
		System.out.println("! Countries");
		System.out.println("! Levy Composition");
		for (CultureGroup cultureGroup : cultureGroups) {
			int numCultures = cultureGroup.cultureList.size();
			System.out.println("|-");
			Color cultureColour = cultureGroup.colour;
			String styleString = "\"background-color:rgb(" + cultureColour.getRed() + "," + 
					cultureColour.getGreen() + "," + cultureColour.getBlue() + 
					");";
			if ((cultureColour.getRed() + cultureColour.getGreen() + cultureColour.getBlue()) / 3 < 75 )
				styleString += " color:white;";
			styleString += " text-align: center\"";
			System.out.println("| style=" + styleString + " rowspan = " + numCultures + " | '''" + cultureGroup.name + "''' <br> ''" + 
					cultureGroup.key + "''");
			System.out.print("| rowspan = " + numCultures + " | [[File:" + 
					cultureGroup.getTradition(militaryTraditions, localisation).getCodeName().substring(0, 1).toUpperCase() + 
					cultureGroup.getTradition(militaryTraditions, localisation).getCodeName().substring(1) + "_start_bonus.png|150px]] <br> '''[[" + 
					cultureGroup.getTradition(militaryTraditions, localisation).getDisplayName() + " traditions]]'''");
			if (cultureGroup.key.equals("hellenic")) {
				System.out.print(" <ref>{{flag|Bactria}} and {{flag|Arachosia}} uniquely also have access to all [[Persian traditions]] at start," +
						" despite being in the Hellenistic culture group.</ref>");
			}
			System.out.println("");
			System.out.println("| rowspan = " + numCultures + " | " + cultureGroup.portraits);
			System.out.println("| rowspan = " + numCultures + " | " + cultureGroup.graphical_culture);
			for (int i = 0; i < numCultures; i++) {
				Culture currentCulture = cultureGroup.cultureList.get(i);
				if (i != 0)
					System.out.println("|-");
				System.out.println("| " + currentCulture.name);
				System.out.println("| " + currentCulture.num_pops);
				System.out.println("| " + currentCulture.num_provinces);
				if (currentCulture.countries.size() > 1)
					System.out.println("| {{MultiColumn|");
				else {
					System.out.println("| ");					
				}
				for (Country country : currentCulture.countries) {
					System.out.println("* {{flag|" + country.properName + "}}");					
				}
				if (currentCulture.countries.size() > 1)
					System.out.println("|3}}");
				System.out.println("| " + currentCulture.getLevyTemplate().toDisplayString(localisation));
			}
		}
		System.out.println("|}");
		
	}
	
	public void printCountryTable() {
		System.out.println("{| class=\"wikitable sortable\"");
		System.out.println("! Country !! Tag !! Capital Region !! Government !! Main Tradition" 
				+ "<ref>All countries start with access to both trees of their main [[military traditions]] group. Some cultures and countries may also additionally start with access to a single tree in another group.</ref>" 
				+ "!! Culture !! Culture Group !! Religion !! Starting Territories !! Starting Population !! Notes");
		for (Country country : countries) {
			if (!country.key.equals("BAR")) {
				System.out.println("|-");
				System.out.print("| {{flag|" + country.properName + "}}");
				System.out.print("||" + country.key);
				System.out.print("||" + country.capital.province.region.name);
				System.out.print("||" + country.government.getBracketedName());
				
				System.out.print("||" + country.getTraditionNames(militaryTraditions));
				System.out.print("||" + country.culture.name);
				System.out.print("||" + country.culture.group.name);
				System.out.print("||" + country.religion.name);
				System.out.print("||" + country.territories.size());
				System.out.print("||" + country.getTotalPopulation());
				System.out.print("||");
				if (country.subjectType != null)
					System.out.print(country.subjectType.name + " of {{flag|" + country.suzerain.properName + "}}");
				if (country.key.equals("MRY") || country.key.equals("SEL") || country.key.equals("PRY"))
					System.out.print("The name of {{flag|" + country.name + "}} changes based on its ruling dynasty and government form. See [[Country rename events]] for details.");
				System.out.println("");
			}
		}
		System.out.println("|}");
	}
	
	public void printRegionTable() {
		System.out.println("{|class=\"wikitable sortable\"\n|+\n!Region\n!Provinces\n!ID");
		for (Region region : regions) {
			System.out.println("|-");
			System.out.println("!style=\"background-color:rgb(146,165,145)\"|[[" + region.name + "]]");
			System.out.print(region.provinces.get(0).name);
			for (int i = 1; i < region.provinces.size(); i++)
				System.out.print(", " + region.provinces.get(i).name);
			System.out.print("\n");
			System.out.print("|" + region.key);
			System.out.println("");
		}
		System.out.println("|}");
	}
	
	public void writeTerritoryTables() {
		Iterator<Entry<String, String[]>> it = RegionGroups.REGION_GROUPS.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, String[]> regionGroup = (Map.Entry<String, String[]>)it.next();
			FileWriter myWriter;
			try {
				myWriter = new FileWriter(regionGroup.getKey());
				for (String region : regionGroup.getValue())
					this.writeRegionTables(region, myWriter);
				myWriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// it.remove();
		}
		
	}
	
	public void createProvinceMap(MapType type) {
		System.out.println("Drawing map...");
		mapPainter.createProvinceMap(provinceMap, colourToTerritory, type);
		System.out.println("Map drawn!");
	}
	
	public void drawCoas(String coaFile) {
		List<Coa> coas = new ArrayList<Coa>();
		
		BufferedReader inCoaFile;
		try {
			inCoaFile = new BufferedReader(new FileReader("coas.txt"));
			
			while(inCoaFile.ready()) {
				// Enter into coa block
				String token = Utils.readNextToken(inCoaFile);
				if (token.length() == 0) {
					continue;
				}
				String coaKey = token;
				
				// Consume tokens
				Utils.readNextToken(inCoaFile);
				Utils.readNextToken(inCoaFile);
				
				// Read coa parameters
				String pattern = null;
				String color1 = null;
				String color2 = null;
				List<CoaEmblem> emblems = new ArrayList<CoaEmblem>();
				while(inCoaFile.ready()) {					
					token = Utils.readNextToken(inCoaFile);
					if (token.equals("pattern")) {
						Utils.readNextToken(inCoaFile);
						pattern = Utils.readNextToken(inCoaFile);
					}
					else if (token.equals("color1")) {
						Utils.readNextToken(inCoaFile);
						color1 = Utils.readNextToken(inCoaFile);
					}
					else if (token.equals("color2")) {
						Utils.readNextToken(inCoaFile);
						color2 = Utils.readNextToken(inCoaFile);
					}
					else if (token.equals("color3")) {
						// Skip this
						Utils.readNextToken(inCoaFile);
						Utils.readNextToken(inCoaFile);
					}
					else if (token.equals("colored_emblem")) {
						Utils.readNextToken(inCoaFile);
						Utils.readNextToken(inCoaFile);

						String texture = null;
						String emblemColor1Name = null;
						String emblemColor2Name = null;
						boolean[] mask = {true, true, true};
						List<CoaEmblemInstance> instances = new ArrayList<CoaEmblemInstance>();
						while(inCoaFile.ready()) {
							token = Utils.readNextToken(inCoaFile);
							if (token.equals("texture")) {
								Utils.readNextToken(inCoaFile);
								texture = Utils.readNextToken(inCoaFile);
							}
							else if (token.equals("color1")) {
								Utils.readNextToken(inCoaFile);
								emblemColor1Name = Utils.readNextToken(inCoaFile);
							}
							else if (token.equals("color2")) {
								Utils.readNextToken(inCoaFile);
								emblemColor2Name = Utils.readNextToken(inCoaFile);
							}
							else if (token.equals("mask")) {
								Utils.readNextToken(inCoaFile);
								Utils.readNextToken(inCoaFile);
								boolean[] newMask = {false, false, false};
								while(inCoaFile.ready()) {
									token = Utils.readNextToken(inCoaFile);
									if (!token.equals("}")) {
										int maskIndex = Integer.parseInt(token);
										if (maskIndex > 0) {
											newMask[maskIndex - 1] = true;											
										}
									}
									else {
										break;
									}
								}
								
								mask = newMask;
							}
							else if (token.equals("instance")) {
								Utils.readNextToken(inCoaFile);
								Utils.readNextToken(inCoaFile);
								
								double rotation = 0;
								Point2D.Double scale = new Point2D.Double(1, 1);
								Point2D.Double position = new Point2D.Double(0.5, 0.5);
								
								while(inCoaFile.ready()) {
									token = Utils.readNextToken(inCoaFile);
									if (token.equals("rotation")) {
										Utils.readNextToken(inCoaFile);
										rotation = Double.parseDouble(Utils.readNextToken(inCoaFile));
									}
									else if (token.equals("scale")) {
										Utils.readNextToken(inCoaFile);
										Utils.readNextToken(inCoaFile);
										scale.x = Double.parseDouble(Utils.readNextToken(inCoaFile));
										scale.y = Double.parseDouble(Utils.readNextToken(inCoaFile));
										Utils.readNextToken(inCoaFile);
									}
									else if (token.equals("position")) {
										Utils.readNextToken(inCoaFile);
										Utils.readNextToken(inCoaFile);
										position.x = Double.parseDouble(Utils.readNextToken(inCoaFile));
										position.y = Double.parseDouble(Utils.readNextToken(inCoaFile));
										Utils.readNextToken(inCoaFile);
									}
									else if (token.equals("mask")) {
										Utils.readNextToken(inCoaFile);
										Utils.readNextToken(inCoaFile);
										position.x = Double.parseDouble(Utils.readNextToken(inCoaFile));
										position.y = Double.parseDouble(Utils.readNextToken(inCoaFile));
										Utils.readNextToken(inCoaFile);
									}
									else if (token.equals("depth")) {
										// Skip this
										Utils.readNextToken(inCoaFile);
										Utils.readNextToken(inCoaFile);
									}
									else if (token.equals("}")) {
										break;
									}
									else {
										System.err.println("Unknown CoA token: " + token);
										assert false;
									}
								}
								
								instances.add(new CoaEmblemInstance(rotation, scale, position));
							}
							else if (token.equals("}")) {
								break;
							}
							else {
								System.err.println("Unknown CoA token: " + token);
								assert false;
							}
						}
						
						Color emblemColor1 = namedColours.get(emblemColor1Name);
						Color emblemColor2 = Color.BLACK;
						if (emblemColor2Name != null) {
							emblemColor2 = namedColours.get(emblemColor2Name);
						}
						
						if (instances.size() > 0) {
							for (CoaEmblemInstance instance: instances) {
								emblems.add(
									new CoaEmblem(
										texture,
										emblemColor1,
										emblemColor2,
										instance.rotation,
										instance.scale,
										instance.position,
										mask
									)
								);
							}							
						}
						// Default emblem if no instances are explicitly stated
						else {
							emblems.add(
								new CoaEmblem(
									texture,
									emblemColor1,
									emblemColor2,
									0,
									new Point2D.Double(1, 1),
									new Point2D.Double(0.5, 0.5),
									mask									
								)
							);
						}
					}
					else if (token.equals("}")) {
						break;
					}
					else {
						System.err.println("Unknown CoA token: " + token);
						assert false;
					}
				}
				Coa newCoa = new Coa(coaKey);
				newCoa.pattern = pattern;
				newCoa.color1 = namedColours.get(color1);
				if (color2 != null) {
					newCoa.color2 = namedColours.get(color2);
				}
				newCoa.emblems = emblems;
				coas.add(newCoa);
			}
			inCoaFile.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Drawing flags...");
		for (Coa coa: coas) {
			flagPainter.drawFlag(coa);
		}
	}
	
	public void printPermanentModifiersTable() {
		System.out.println("{| class=\"wikitable sortable\"");
		System.out.println("! width=200px | Name !! Modifiers !! width=33% | Description !! Location !! Starting Owner");
		for (Modifier modifier: modifiers) {
			if (modifier.territories.size() > 0 && !modifier.key.equals("major_barbarian_spawn_place") && 
					!modifier.key.equals("minor_barbarian_spawn_place") && !modifier.key.equals("generic_barbarian_spawn_place") && 
					!modifier.key.equals("pirate_haven_modifier") && !modifier.key.equals("gate_modifier") && 
					!modifier.key.equals("lesser_pass_modifier")) {
				System.out.println("|-");
				System.out.print("| rowspan=" + Integer.toString(modifier.territories.size()) + " | [[File:" + modifier.getIcon(modifierIcons) + ".png|28px]] " + modifier.name);	
				System.out.print(" || rowspan=" + Integer.toString(modifier.territories.size()) + " | ");
				for (ModifierEffect effect : modifier.effects) {
					System.out.print(" {{icon|" + effect.iconName + "}} " + effect.valueString + " " + effect.name + "<br>");
				}
				System.out.print(" || rowspan=" + Integer.toString(modifier.territories.size()) + " | ''" + modifier.description + "''");
				for (int i = 0; i < modifier.territories.size(); i++) {
					Territory territory = modifier.territories.get(i);
					if (i == 0)
						System.out.print(" ||");
					else
						System.out.print("\n|-\n|");
					System.out.print(" " + territory.getStartingName() + " (" + Integer.toString(territory.id) + ")");
					if (territory.owner != null)
						System.out.print(" || {{flag|" + territory.owner.properName + "}}");
					else
						System.out.print(" || —");
				}
				System.out.println("");
			}		
		}
		System.out.println("|}");
	}
	
	public static void main(String[] args) {
		ImperatorParser newParser = new ImperatorParser();
		System.out.println("Parsing...");
		newParser.parse();
		System.out.println("Parsing done!");
		//newParser.writeTerritoryTables();
		//newParser.printCultureTable();
		//newParser.printCountryTable();
		//newParser.createProvinceMap(MapType.CUSTOM);
		newParser.printPermanentModifiersTable();
	}

}
