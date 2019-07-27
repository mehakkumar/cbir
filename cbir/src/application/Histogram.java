package application;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;

import javafx.scene.image.*;
//import javafx.scene.image.PixelFormat;
//import javafx.scene.image.PixelReader;
import javafx.stage.Stage;

@SuppressWarnings("serial")
class HistogramData implements Serializable {
	public int imgno;
	public String imgname;
	public float[][] hdata = new float[256][4];
	public HistogramData(){
		
	}
}

public class Histogram {

	Image rawImg;
	double image_num_cols, image_num_rows;
	int[] pixels_1D;
	int[][][] pixels_3D;
	static int fileNum = 1;
	static  float[][] colorData = new float[256][4];
	static String str; 
	
	public void createImgHistogram(String imageName) {
		imageName = "file:" + imageName; System.out.println(imageName);
		rawImg = new Image(imageName);
		image_num_cols = rawImg.getWidth();
		image_num_rows = rawImg.getHeight();

	    //grabbing pixels into the array
		pixels_1D = new int[(int) (image_num_cols*image_num_rows)];
		try {
	    	PixelReader readerObj = rawImg.getPixelReader();
	    	readerObj.getPixels(0, 0, (int)image_num_cols, (int)image_num_rows, PixelFormat.getIntArgbInstance(), pixels_1D, 0, (int)image_num_cols);
	    } catch(Exception e) {
	    	e.printStackTrace();
	    	
	    }
		//convert 1D pixel data to 3D
		pixels_3D = new int[(int) image_num_rows][(int) image_num_cols][4]; // 4 - ARGB (colors)
	    convert1DTo3DPixel(pixels_3D,pixels_1D,(int)image_num_rows,(int)image_num_cols);
	    for (float[] row : colorData) 
	    	Arrays.fill(row, 0);
	    getColorRatio((int)image_num_rows,(int) image_num_cols, pixels_3D);
	}
	
	/**
	 * read list of images (scanned) and create histogram for each
	 * @param stage
	 * @throws Exception
	 */
	public int createHistograms(Stage stage) throws Exception {
		FileReader fr = new FileReader("file_list.txt");
		BufferedReader br = new BufferedReader(fr);
		HistogramData hdObj = new HistogramData();
		while((str=br.readLine())!=null) {
			hdObj.imgno = fileNum;
			hdObj.imgname = str;
			createImgHistogram(str);
			FileOutputStream fos = new FileOutputStream("histogram"+fileNum+".dat");
		    ObjectOutputStream oos = new ObjectOutputStream(fos);
		    for(int i = 0; i < 256; i++) {
		    	for(int j = 0; j < 4; j++){    
		           hdObj.hdata[i][j] = colorData[i][j];
		        }
		     }
		     oos.writeObject(hdObj);
		     fileNum++;
		     oos.close();
		     fos.close();
		}
		br.close();
		return fileNum;
		
	}
	
	
	/**
	 * convert 1 dimensional pixel data to 3 dimensional
	 * @param pix3
	 * @param pix
	 * @param numRows
	 * @param numCols
	 */
	private void convert1DTo3DPixel(int[][][] pix3,int[] pix, int numRows, int numCols) {
		for(int row = 0; row < numRows; row++){
			int[] singleRow = new int[numCols];
			//get data for this row from 1D pixel array
			for(int c = 0; c < numCols; c++) {
				int pixelData = (row * numCols) + c;
				singleRow[c] = pix[pixelData];
			}
			for(int col = 0; col < numCols; col++) {
				pix3[row][col][0]=(singleRow[col]>>24)&0xFF; //bitwise right shift and get 8 least significant bits 
		        pix3[row][col][1]=(singleRow[col]>>16)&0xFF; // masking (to avoid sign extension)
		        pix3[row][col][2]=(singleRow[col]>>8)&0xFF;
		        pix3[row][col][3]=(singleRow[col])&0xFF;
			}
		}
	}
	
	/**
	 * get ratio for each rgb value - amount of each component present in the image
	 * @param numRows
	 * @param numCols
	 * @param pix3
	 */
	private void getColorRatio(int numRows, int numCols, int[][][] pix3) {
		int numberOfPixels = numCols * numRows;
		for(int row = 0; row < numRows; row++) {
			for(int col =0; col < numCols; col++) {
				for (int k= 1; k < 4; k++) {
					int val = pix3[row][col][k];
					colorData[val][k] = colorData[val][k] + 1;
				}
			}
		}
		for(int i = 0; i < 256; i++) {
		    for(int j = 1; j < 4; j++) {
		    	colorData[i][j] = colorData[i][j]/numberOfPixels;
		    }
	    }
	}
	
}
