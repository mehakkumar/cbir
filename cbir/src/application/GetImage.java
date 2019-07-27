package application;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class GetImage {
	
	//declarations
	FileOutputStream fos;
	PrintStream ps ;
	long numberOfImages;
	
	//traverses through files and saves path for jpegs
	private void scanFiles(File folder) {
		File listOfFiles[] = folder.listFiles();
		if (listOfFiles != null) {
			for(int i = 0; i<listOfFiles.length; i++) {
				if(listOfFiles[i].isDirectory()) {
					//traverse through the inner directory
					scanFiles(listOfFiles[i]);
				} else {
					//check if given file is an image
					if(listOfFiles[i].toString().endsWith(".jpg") || listOfFiles[i].toString().endsWith(".jpeg")) {
						ps.println(listOfFiles[i]);
						numberOfImages++;
					}			
				}
			}
		}
	}
	
	//scans given directory and saved path for all images present in it
	public long getPathForImagesInDirectory(String dirPath) {
		numberOfImages = 0;
		try {
			fos = new FileOutputStream("file_list.txt");
			ps = new PrintStream(fos);
			File folder = new File(dirPath);
			//traverse through all files in the directory and create a list of all images
			scanFiles(folder);
		} catch( Exception e) {
			System.out.println(e);
		}
		return numberOfImages;
	}
}
