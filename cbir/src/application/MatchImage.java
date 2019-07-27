package application;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.util.Arrays;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class MatchImage {

	private String fileToBeMatched;
	private long numberOfImages = 100;
	Image imageToFind;
	int columns_imageToFind, rows_imageToFind;
	float matchScoreGrid[][];

	public MatchImage(String str) {
		fileToBeMatched = str;
	}

	// compare histograms with fileToBeMatched and return closest images
	public void compareHistograms(Stage results) {
		numberOfImages = getImageCount();
		matchScoreGrid = new float[((int)numberOfImages+1)][2];
		float histogramData[][] = new float[256][4];
		histogramData = readImage();
		
		try {
			computeComparisionScore(histogramData); 
			//sort grid to get closest images
			sortScoreGrid();
			//get closest images to be shown - top 5 matching
			Image imageArray[] = new Image[6]; 
			imageArray[0]=new Image(fileToBeMatched); 
			String imagepath[] = new String[6];
			imagepath[0] = fileToBeMatched;
			int j = 1;
			 
			String fileName;
			for(int i = 1; i < imageArray.length; i++) { 
				int num = (int)matchScoreGrid[j][0];
				fileName = "file:" + getFileName(num-1); //to balance indexing
				imageArray[i] = new Image(fileName);
				imagepath[i] = fileName.replaceAll("\\\\", "/");
				imagepath[i] = imagepath[i].substring(0, imagepath[i].lastIndexOf('/'));
				System.out.println(num+ " "+imagepath[i]);
				j++;
		    }
			//show images
			showMatchedImages(results, imageArray, imagepath);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * get distance score for the image to be searched with all other images
	 * @param histogramData
	 * @throws Exception
	 */
	private void computeComparisionScore(float[][] histogramData) throws Exception {
		HistogramData hdObj = new HistogramData();
		float[][] histogramDataCopy = new float[256][4];
		float distance = 0;
		for (int img = 1; img <= numberOfImages; img++) {
			// create a copy of the image histogram data
			for (int i = 0; i < 256; i++) {
				for (int j = 0; j < 4; j++) {
					histogramDataCopy[i][j] = histogramData[i][j];
				}
			}
			// fetch histogram for an image
			FileInputStream fis = new FileInputStream("histogram" + img + ".dat");
			ObjectInputStream ois = new ObjectInputStream(fis);
			hdObj = (HistogramData) ois.readObject();
			// compute distance for each color component
			// temporary variables #TODO rename
			int[][] f1 = new int[256][256];
			int[] temp1 = new int[256];
			for (int j = 0; j < 255; j++)
				temp1[j] = j; // 0-255
			for (int i = 0; i < 256; i++) {
				for (int j = 0; j < 256; j++) {
					f1[i][j] = Math.abs(temp1[i] - temp1[j]);
				}
			}
			distance += computeDistanceForColorComponent(histogramDataCopy,hdObj.hdata,f1,1);
			distance += computeDistanceForColorComponent(histogramDataCopy,hdObj.hdata,f1,2);
			distance += computeDistanceForColorComponent(histogramDataCopy,hdObj.hdata,f1,3);

			matchScoreGrid[img][0] = img;
			matchScoreGrid[img][1] = distance;
			distance = 0;
			ois.close();
		    fis.close();
		}
	}
	
	/**
	 * compute difference for each color component between two histograms
	 * @param histogramDataCopy
	 * @param histogramFileRead
	 * @param f1
	 * @param colorComponent
	 * @return
	 */
	private float computeDistanceForColorComponent(float[][] histogramDataCopy, float[][] histogramFileRead, int[][] f1, int colorComponent) {
		float dist = 0;
		int[] t1 = new int[256];
		int[] t2 = new int[256];
		float colorValHolder1, colorValHolder2;
		Arrays.fill(t1, 0);
		Arrays.fill(t2, 0);
		
		for (int l = 0; l < 256; l++) {
			for (int i = 0; i < 256; i++) {
				for (int j = 0; j < 256; j++) {
					if (f1[i][j] == l && t1[i] == 0 && t2[j] == 0) {
						colorValHolder1 = histogramDataCopy[i][colorComponent];
						colorValHolder2 = histogramFileRead[j][colorComponent];
						if (colorValHolder1 < colorValHolder2) {
							dist = dist + l * colorValHolder1;
							histogramDataCopy[i][colorComponent] = 0;
							t1[i] = 1;
							histogramFileRead[j][colorComponent] = colorValHolder2 - colorValHolder1;
						} else if (colorValHolder1 > colorValHolder2) {
							dist = dist + l * colorValHolder2;
							histogramDataCopy[i][colorComponent] = colorValHolder1 - colorValHolder2;
							t2[j] = 1;
							histogramFileRead[j][colorComponent] = 0;
						} else {
							dist = dist + l * colorValHolder1;
							histogramDataCopy[i][colorComponent] = 0;
							t1[i] = 1;
							t2[j] = 1;
							histogramFileRead[j][colorComponent] = 0;
						}
					}
				}
			}
		}
		return dist;
	}

	// get count of scanned images
	private long getImageCount() {
		long count = 0;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("file_count.txt")));
			String line;
			while ((line = br.readLine()) != null) {
				count = Long.parseLong(line);
			}
			br.close();
		} catch (IOException e) {
			System.out.println("ERROR: unable to read file ");
			e.printStackTrace();
		}
		return count;
	}

	/**
	 * read the searched image and create histogram for it
	 * 
	 * @return
	 */
	private float[][] readImage() {
		float[][] histogramData = new float[256][4];
		for( float[] row: histogramData)
			Arrays.fill(row, 0);
		try {
			imageToFind = new Image(fileToBeMatched);
			columns_imageToFind = (int) imageToFind.getWidth();
			rows_imageToFind = (int) imageToFind.getHeight();
			// grab pixels of the image
			int pixels_1D_imageToFind[] = new int[(int) (columns_imageToFind * rows_imageToFind)];
			int pixels_3D_imageToFind[][][] = new int[(int) rows_imageToFind][(int) columns_imageToFind][4]; // 4 - ARGB (colors)
			try {
				PixelReader readerObj = imageToFind.getPixelReader();
				readerObj.getPixels(0, 0, (int) columns_imageToFind, (int) rows_imageToFind,
						PixelFormat.getIntArgbInstance(), pixels_1D_imageToFind, 0, (int) columns_imageToFind);
			} catch (Exception e) {
				System.out.println(e);
			}
			// convert 1D pixel data to 3D
			convert1DTo3DPixel(pixels_3D_imageToFind, pixels_1D_imageToFind, rows_imageToFind, columns_imageToFind);
			// get colour ratio
			histogramData = getColorRatio(histogramData, rows_imageToFind, columns_imageToFind, pixels_3D_imageToFind);
		} catch (Exception e) {
			System.out.println(e);
		}
		return histogramData;
	}

	/**
	 * convert 1 dimensional pixel data to 3 dimensional
	 * 
	 * @param pix3
	 * @param pix
	 * @param numRows
	 * @param numCols
	 */
	private void convert1DTo3DPixel(int[][][] pix3, int[] pix, int numRows, int numCols) {
		for (int row = 0; row < numRows; row++) {
			int[] singleRow = new int[numCols];
			// get data for this row from 1D pixel array
			for (int c = 0; c < numCols; c++) {
				int pixelData = (row * numCols) + c;
				singleRow[c] = pix[pixelData];
			}
			for (int col = 0; col < numCols; col++) {
				pix3[row][col][0] = (singleRow[col] >> 24) & 0xFF; // bitwise right shift and get 8 least significant bits
				pix3[row][col][1] = (singleRow[col] >> 16) & 0xFF; // masking (to avoid sign extension)
				pix3[row][col][2] = (singleRow[col] >> 8) & 0xFF;
				pix3[row][col][3] = (singleRow[col]) & 0xFF;
			}
		}
	}

	/**
	 * get ratio for each rgb value - amount of each component present in the
	 * image
	 * 
	 * @param histogram
	 * @param size
	 */
	private float[][] getColorRatio(float[][] histogram, int numRows, int numCols, int[][][] pix3) {
		int numberOfPixels = numCols * numRows;
		for (int row = 0; row < numRows; row++) {
			for (int col = 0; col < numCols; col++) {
				for (int k = 1; k < 4; k++) {
					int val = pix3[row][col][k];
					histogram[val][k] = histogram[val][k] + 1;
				}
			}
		}
		for (int i = 0; i < 256; i++) {
			for (int j = 1; j < 4; j++) {
				histogram[i][j] = histogram[i][j] / numberOfPixels;
			}
		}
		return histogram;
	}
	
	/**
	 * sort the distances in asc order
	 */
	private void sortScoreGrid() {
		for(int i = 1; i <= numberOfImages; i++){
			for(int j = 1; j <= numberOfImages-i-1; j++){
				if( matchScoreGrid[j][1] > matchScoreGrid[j+1][1]) {
					float tempScore = matchScoreGrid[j+1][1];
			        float tempImgNo = matchScoreGrid[j+1][0];
			        matchScoreGrid[j+1][1] = matchScoreGrid[j][1];
			        matchScoreGrid[j][1] = tempScore;
			        matchScoreGrid[j+1][0] = matchScoreGrid[j][0];
			        matchScoreGrid[j][0] = tempImgNo;
				}
			}
		}  
	}
	
	/**
	 * fetch name of file at given index from saved list of files
	 * @param index
	 * @return
	 * @throws Exception
	 */
	private String getFileName(int index)throws Exception{
		String iname;
		FileReader fr = new FileReader("file_list.txt");
		BufferedReader br = new BufferedReader(fr);
		for(int i=0;i<index;i++)br.readLine();
		iname=br.readLine();
		br.close();
		return iname;   
	}
	
	/**
	 * showcase matched images
	 * @param results
	 * @param imageArray
	 * @throws Exception
	 */
	private void showMatchedImages(Stage results, Image[] imageArray, String[] imagepath) throws Exception {
		GridPane root=new GridPane();
		root.setHgap(10);
		root.setVgap(10);
		Scene resultScene = new Scene (root,600,400);
		results.setScene(resultScene);
		ImageView imgViewArray[] = new ImageView[6];
		for (int i = 0; i < 6; i++) {
			imgViewArray[i] = new ImageView(imageArray[i]);
			imgViewArray[i].setFitHeight(200);
			imgViewArray[i].setFitWidth(200);
			imgViewArray[i].setPreserveRatio(true);
		}
		root.add(imgViewArray[0], 0, 0);
		root.add(imgViewArray[1], 1, 0);
		root.add(imgViewArray[2], 2, 0);
		root.add(imgViewArray[3], 0, 1);
		root.add(imgViewArray[4], 1, 1);
		root.add(imgViewArray[5], 2, 1);
		results.show();
		
		//onclick event for image - go to location
		FileOpen foObj = new FileOpen();
		for (int i = 0; i < 6; i++) {
			final Integer innerIndex = new Integer(i);  // i cannot be accessed in the event handler since it is an anonymous function
														//we create a final variable for it to access
			imgViewArray[i].setOnMouseClicked(new EventHandler<MouseEvent>() {
				public void handle(MouseEvent event) {
					foObj.open(imagepath[innerIndex]);
				}
			});
			imgViewArray[i].setOnMouseEntered(new EventHandler<MouseEvent>() {
				public void handle(MouseEvent event) {
					imgViewArray[innerIndex].setCursor(Cursor.HAND); //Change cursor to hand 
				}
			});
		}
	}
}

/**
 * class to open given location in the system
 * @author mehak
 *
 */
class FileOpen extends Application {
	public void open(String path){
		HostServices hs = getHostServices();
		hs.showDocument(path);
	}
	@Override
	public void start(Stage primaryStage) throws Exception {
		// TODO Auto-generated method stub
		
	}
	
}