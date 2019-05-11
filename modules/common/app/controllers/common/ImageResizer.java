package controllers.common;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;

import com.google.common.io.Files;

import play.Logger;

//import com.sun.image.codec.jpeg.JPEGCodec;
//import com.sun.image.codec.jpeg.JPEGEncodeParam;
//import com.sun.image.codec.jpeg.JPEGImageEncoder;

public class ImageResizer {
      
    public static int MAX_MOBILE_SIZE = 1080;
    private static float COMPRESS_QUALITY = 0.50f;

    public static BufferedImage zoomImage(File srcfile, boolean isResize, int maxSize) {  
          
        BufferedImage result = null;  
  
        try {  
            if (!srcfile.exists()) {  
                return null;
            }  
            
            BufferedImage im = ImageIO.read(srcfile);
            int width = im.getWidth();  
            int height = im.getHeight();           
            
            int largerSize = width > height ? width : height;
            float resizeTimes = 1.0f;
            
            boolean bIsPng = srcfile.getName().toLowerCase().endsWith("png");
            
            if (largerSize <= maxSize || !isResize) {
                resizeTimes = 1.0f;
            }        
            else
            {
                resizeTimes = maxSize *1.0f / largerSize;                
            }
                        
              
            int toWidth = (int) (width * resizeTimes);  
            int toHeight = (int) (height * resizeTimes);

            if (bIsPng) {
                result = new BufferedImage(toWidth, toHeight,  
                        BufferedImage.TYPE_INT_ARGB);                  	
            }
            else {
                result = new BufferedImage(toWidth, toHeight,  
                        BufferedImage.TYPE_INT_RGB);             	
            }
  
            Graphics g = result.createGraphics();
            g.drawImage(im, 0, 0, toWidth, toHeight, null);
            g.dispose();            
            //It takes too long here!!!
            //result.getGraphics().drawImage(  
            //        im.getScaledInstance(toWidth, toHeight,  
            //                java.awt.Image.SCALE_FAST), 0, 0, null);  
              
  
        } catch (Exception e) {
        	e.printStackTrace();
        	Logger.error(e.getMessage());
            result = null;
        }  
          
        return result;  
  
    }  
      
     public static void writeHighQuality(BufferedImage im, String fileFullPath, boolean bCompress) throws IOException  {  
         String type = fileFullPath.substring(fileFullPath.lastIndexOf('.')+1);
         //        fileFullPath.length()).toLowerCase();
         if (type.toLowerCase().compareTo("png") == 0) {
              bCompress = false;
	 }
	 else {
	     type = "jpg";
         }

	 //String type = "jpg";
         
         File f = new File(fileFullPath);
         Files.createParentDirs(f);
         
         if (!bCompress) {
             ImageIO.write(im, type, f); 
             return;
         }
         
         RandomAccessFile raf = new RandomAccessFile(fileFullPath, "rw");
         raf.setLength(0);
         ImageWriter writer = ImageIO.getImageWritersBySuffix(type).next();
         ImageOutputStream ios = ImageIO.createImageOutputStream(raf);
         writer.setOutput(ios);
         
         ImageWriteParam param = writer.getDefaultWriteParam();
         param.setCompressionMode(JPEGImageWriteParam.MODE_EXPLICIT);
         param.setCompressionQuality(COMPRESS_QUALITY);
         //IIOMetadata imageMetaData = writer.getDefaultImageMetadata(new ImageTypeSpecifier(im), param);
         writer.write(null, new IIOImage(im, null, null), param);
         ios.flush();
         ios.close();         
         writer.dispose();
    }    
}  
