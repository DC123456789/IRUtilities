package IRParser;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import ddsutil.DDSUtil;
import IRObjects.Coa;
import IRObjects.CoaEmblem;


public class ImperatorFlagPainter {

	public static final String PATTERNS_PATH = Utils.IMPERATOR_PATH + "gfx\\coat_of_arms\\patterns";
	public static final String TEXTURES_PATH = Utils.IMPERATOR_PATH + "gfx\\coat_of_arms\\colored_emblems";
	
	public static final int FLAG_WIDTH = 256;
	public static final int FLAG_HEIGHT = 256;
	
	public BufferedImage readImageFile(String filepath) throws IOException {
		String extension = filepath.substring(filepath.lastIndexOf(".") + 1);
		if (extension.equals("tga")) {
			return ImageIO.read(new File(filepath));			
		}
		else if (extension.equals("dds")) {
			return DDSUtil.decompressTexture(new File(filepath));
		}
		else {
			throw new IOException("Unknown file extension: " + extension);
		}
	}
	
	public Color applyMaskPixel(Color colour, int maskValue) {
		int red = (int)Math.round(colour.getRed() * maskValue / 255.0);
		int green = (int)Math.round(colour.getGreen() * maskValue / 255.0);
		int blue = (int)Math.round(colour.getBlue() * maskValue / 255.0);
		int alpha = maskValue;		
		
		return new Color(red, green, blue, alpha);
	}
	
	public BufferedImage overlay(BufferedImage bottom, BufferedImage top, double relativeX, double relativeY) {
		BufferedImage combined = new BufferedImage(bottom.getWidth(), bottom.getHeight(), BufferedImage.TYPE_INT_ARGB);

		// paint both images, preserving the alpha channels
		Graphics g = combined.getGraphics();
		g.drawImage(bottom, 0, 0, null);
		g.drawImage(
			top,
			(int)Math.round(relativeX * bottom.getWidth() - top.getWidth() * 0.5),
			(int)Math.round(relativeY * bottom.getHeight() - top.getHeight() * 0.5),
			null
		);
		g.dispose();
		
		return combined;
	}
	
	public BufferedImage overlay(BufferedImage bottom, BufferedImage top) {		
		return overlay(bottom, top, 0.5, 0.5);
	}
	
	public BufferedImage applyTransformations(BufferedImage original, double newWidth, double newHeight, double angle) {
		if (newWidth == original.getWidth() && newHeight == original.getHeight() && angle == 0) {
			return original;
		}
		else {
			int actualWidth = (int)Math.round(Math.abs(newWidth));
			int actualHeight = (int)Math.round(Math.abs(newHeight));
			
			BufferedImage transformed = new BufferedImage(actualWidth, actualHeight, BufferedImage.TYPE_INT_ARGB);
			AffineTransform at = new AffineTransform();
			
			// Reflection
			if (newWidth < 0 || newHeight < 0) {
				at.scale(newWidth / Math.abs(newWidth), newHeight / Math.abs(newHeight));
				// Reflections need to be translated back into the frame
				if (newWidth < 0) {
					at.translate(-original.getWidth(), 0);
				}
				if (newHeight < 0) {
					at.translate(0, -original.getHeight());
				}				
			}
			// Rotation
			if (angle > 0) {
				at.rotate(Math.toRadians(angle), original.getWidth() / 2.0, original.getHeight() / 2.0);
			}
			// Scaling
			if (actualWidth != original.getWidth() || actualHeight != original.getHeight()) {
				at.scale(((double)actualWidth) / original.getWidth(), ((double)actualHeight) / original.getHeight());
			}
			AffineTransformOp transformOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC);
			transformed = transformOp.filter(original, transformed);
			return transformed;
		}
	}
	
	public BufferedImage rotateImage(BufferedImage original, double angle) {
		if (angle == 0) {
			return original;
		}
		else {
			BufferedImage rotated = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_ARGB);
			AffineTransform at = new AffineTransform();
			at.rotate(Math.toRadians(angle), original.getWidth() / 2.0, original.getHeight() / 2.0);
			AffineTransformOp rotateOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC);
			rotated = rotateOp.filter(original, rotated);
			return rotated;
		}
	}
	
	public BufferedImage scaleImage(BufferedImage original, double newWidth, double newHeight) {
		int actualWidth = (int)Math.round(Math.abs(newWidth));
		int actualHeight = (int)Math.round(Math.abs(newHeight));
		if (actualWidth == original.getWidth() && actualHeight == original.getHeight()) {
			return original;
		}
		else {
			BufferedImage rescaled = new BufferedImage(actualWidth, actualHeight, BufferedImage.TYPE_INT_ARGB);
			AffineTransform at = new AffineTransform();
			at.scale(((double)actualWidth) / original.getWidth(), ((double)actualHeight) / original.getHeight());
			AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC);
			rescaled = scaleOp.filter(original, rescaled);
			return rescaled;
		}
	}
	
	// Only cares about the signs of the width and height
	public BufferedImage reflectImage(BufferedImage original, double newWidth, double newHeight) {
		if (newWidth > 0 && newHeight > 0) {
			return original;
		}
		else {
			BufferedImage reflected = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_ARGB);
			AffineTransform at = new AffineTransform();
			at.scale(newWidth / Math.abs(newWidth), newHeight / Math.abs(newHeight));
			// Reflections need to be translated back into the frame
			if (newWidth < 0) {
				at.translate(-original.getWidth(), 0);
			}
			if (newHeight < 0) {
				at.translate(0, -original.getHeight());
			}
			AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC);
			reflected = scaleOp.filter(original, reflected);
			return reflected;
		}
	}
	
	public BufferedImage translateToFlagPosition(BufferedImage original, double relativeX, double relativeY) {		
		BufferedImage translated = new BufferedImage(FLAG_WIDTH, FLAG_HEIGHT, BufferedImage.TYPE_INT_ARGB);

		// paint both images, preserving the alpha channels
		Graphics g = translated.getGraphics();
		g.drawImage(
			original,
			(int)Math.round(relativeX * FLAG_WIDTH - original.getWidth() * 0.5),
			(int)Math.round(relativeY * FLAG_HEIGHT - original.getHeight() * 0.5),
			null
		);
		g.dispose();
		
		return translated;
	}
	
	public void drawFlag(Coa coa) {
		try {
			// Handle pattern
			BufferedImage pattern = readImageFile(PATTERNS_PATH + "\\" + coa.pattern);
			int width = pattern.getWidth();
			int height = pattern.getHeight();
			BufferedImage patternColor1 = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
			BufferedImage patternColor2 = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
			
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					Color patternColour = new Color(pattern.getRGB(x, y));
					
					patternColor1.setRGB(x, y, applyMaskPixel(coa.color1, patternColour.getRed()).getRGB());
					patternColor2.setRGB(x, y, applyMaskPixel(coa.color2, patternColour.getGreen()).getRGB());
				}
			}

			BufferedImage scaledPattern = scaleImage(pattern, FLAG_WIDTH, FLAG_HEIGHT);
			BufferedImage flag = scaleImage(overlay(patternColor1, patternColor2), FLAG_WIDTH, FLAG_HEIGHT);
			
			// Handle each emblem
			for (CoaEmblem emblem: coa.emblems) {
				BufferedImage texture = readImageFile(TEXTURES_PATH + "\\" + emblem.pattern);
				ImageIO.write(texture, "png", new File("texture.png"));
				int emblemWidth = texture.getWidth();
				int emblemHeight = texture.getHeight();
				BufferedImage textureColor1 = new BufferedImage(emblemWidth, emblemHeight, BufferedImage.TYPE_4BYTE_ABGR);
				BufferedImage textureColor2 = new BufferedImage(emblemWidth, emblemHeight, BufferedImage.TYPE_4BYTE_ABGR);
				BufferedImage textureShadingDark = new BufferedImage(emblemWidth, emblemHeight, BufferedImage.TYPE_4BYTE_ABGR);
				BufferedImage textureShadingLight = new BufferedImage(emblemWidth, emblemHeight, BufferedImage.TYPE_4BYTE_ABGR);
				
				for (int x = 0; x < emblemWidth; x++) {
					for (int y = 0; y < emblemHeight; y++) {
						Color textureColour = new Color(texture.getRGB(x, y));
						
						textureColor1.setRGB(x, y, applyMaskPixel(emblem.color1, 255).getRGB());
						textureColor2.setRGB(x, y, applyMaskPixel(emblem.color2, textureColour.getGreen()).getRGB());
						
						int darkShadingA = 255 - Math.min(textureColour.getBlue() * 2, 255);
						textureShadingDark.setRGB(x, y, new Color(0, 0, 0, darkShadingA).getRGB());
						
						int lightShadingA = (int)Math.round(Math.max(Math.min((textureColour.getBlue() - 127) * 2, 255), 0));
						textureShadingLight.setRGB(x, y, new Color(255, 255, 255, lightShadingA).getRGB());
					}
				}
				ImageIO.write(textureShadingLight, "png", new File("embleamTextureShadingLight.png"));
				
				BufferedImage emblemImage = overlay(overlay(overlay(textureColor1, textureColor2), textureShadingDark), textureShadingLight);
				
				// Apply alpha to combined emblem
				for (int x = 0; x < emblemWidth; x++) {
					for (int y = 0; y < emblemHeight; y++) {
						Color colour = new Color(emblemImage.getRGB(x, y));
						Color textureColour = new Color(texture.getRGB(x, y), true);
						emblemImage.setRGB(x, y, new Color(colour.getRed(), colour.getGreen(), colour.getBlue(), textureColour.getAlpha()).getRGB());
					}
				}
				
				// Doesn't quite work yet
				// emblemImage = applyTransformations(emblemImage, FLAG_WIDTH * emblem.scale.x, FLAG_HEIGHT * emblem.scale.y, 
				//		emblem.rotation);

				emblemImage = reflectImage(emblemImage, emblem.scale.x, emblem.scale.y);
				emblemImage = rotateImage(emblemImage, emblem.rotation);
				emblemImage = scaleImage(emblemImage, FLAG_WIDTH * emblem.scale.x, FLAG_HEIGHT * emblem.scale.y);
				
				emblemImage = translateToFlagPosition(emblemImage, emblem.position.x, emblem.position.y);
				
				// Apply mask to emblem before overlaying
				for (int x = 0; x < emblemImage.getWidth(); x++) {
					for (int y = 0; y < emblemImage.getHeight(); y++) {
						Color colour = new Color(emblemImage.getRGB(x, y), true);
						Color patternColour = new Color(scaledPattern.getRGB(x, y), true);
						int alpha = 0;
						if (emblem.mask[0]) {
							alpha += patternColour.getRed() - patternColour.getGreen();
						}
						if (emblem.mask[1]) {
							alpha += patternColour.getGreen();
						}
						emblemImage.setRGB(x, y, new Color(colour.getRed(), colour.getGreen(), colour.getBlue(), 
								(int)Math.round(colour.getAlpha() * alpha / 255.0)).getRGB());
					}
				}

				flag = overlay(flag, emblemImage);
			}
			
			String countryName = ImperatorParser.localisation.get(coa.key);
			String flagName = "";
			if (countryName != null) {
				flagName = "flags\\" + countryName + ".png";
			}
			else {
				flagName = "flags\\" + coa.key + ".png";				
			}
			ImageIO.write(flag, "png", new File(flagName));
			System.out.println(flagName + " drawn");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		ImperatorParser newParser = new ImperatorParser();
		System.out.println("Parsing...");
		newParser.parse();
		System.out.println("Parsing done!");
		//newParser.writeTerritoryTables();
		//newParser.printCultureTable();
		//newParser.printCountryTable();
		newParser.drawCoas("coas.txt");
	}
}
