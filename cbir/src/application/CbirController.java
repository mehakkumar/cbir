package application;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class CbirController {
	
	//declarations
	Button sel=new Button();
	@FXML
	Button fileSelectionButton;
	@FXML
	Button searchOnlineButton;
	@FXML
	Button scanSystemButton;
	@FXML
	Label infoLabel;
	
	//methods for button actions
	
	public void aboutCbir() throws Exception{
		//add information to the label
		infoLabel.setText("Image search looks for an image in your system and\nreturns the most visually similar image. Scan the\nsystem when using for the first time or after\nadding new images to the system. jpg only");
	}

	public void openBrowser() {
		//reset label
		infoLabel.setText("Image Search");
		
		SearchOnline browserWindow = new SearchOnline();
		browserWindow.showWebsite();
	}
	
	//select a directory and create histograms of images present in that directory
	public void scanSystem() {
		//reset label
		infoLabel.setText("Image Search");
		DirectoryChooser dirChooser = new DirectoryChooser();
		File dirForScan = dirChooser.showDialog(null);
		if (dirForScan != null) {
			//scan selected directory
			String dirPath = dirForScan.getAbsolutePath();
			GetImage allImages = new GetImage();
			long numberOfImages = allImages.getPathForImagesInDirectory(dirPath);
			System.out.println(numberOfImages);
			//store count in a file
			storeCount(numberOfImages);
			//create histograms for the images whose path has been saved
			try{
				Stage stage = new Stage();
		        stage.setTitle("My New Stage Title");
		        FlowPane root=new FlowPane(10,10);
				stage.setScene(new Scene(root, 450, 450));
		        Histogram histoObj = new Histogram();
		        int filesSaved = histoObj.createHistograms(stage);
		        System.out.println(filesSaved);
		        infoLabel.setText("Scanned "+filesSaved+" images!");
			}catch(Exception e) {
				System.out.println("here: "+e);
				e.printStackTrace();
			}
		} else {
			infoLabel.setText("No directory selected");
		}
	}
	
	//select an image, compare histogram of selected image with existing ones to find the images closest to it
	public void searchImage() {
		//reset label
		infoLabel.setText("Image Search");
		FileChooser fc = new FileChooser();
		File selectedFile = fc.showOpenDialog(null);
		if(selectedFile == null){
			infoLabel.setText("File format not supported!");
		} else {
			String pathOfFile = selectedFile.getAbsolutePath();
			pathOfFile = "file:" + pathOfFile.replaceAll("\\\\", "/");
			try{
				Stage searchRes = new Stage();
				searchRes.setTitle("Search Result");
				MatchImage matchObj = new MatchImage(pathOfFile);
				matchObj.compareHistograms(searchRes);
			}catch(Exception e) {
				System.out.println(e);
				e.printStackTrace();
			}
		}	
	}
	
	//add tooltips for each button : onhover action
	public void showTooltip(){
		
		Tooltip scanSystemTip = new Tooltip();
		Tooltip fileSelectionTip = new Tooltip();
		Tooltip searchOnlineTip = new Tooltip();

		searchOnlineTip.setText("Search for your image online");
		fileSelectionTip.setText("Choose a jpg from the system that you want to search");
		scanSystemTip.setText("Scan the system to detect all jpg files");
		
		searchOnlineButton.setTooltip(searchOnlineTip);
		fileSelectionButton.setTooltip(fileSelectionTip);
		scanSystemButton.setTooltip(scanSystemTip);

	}
	
	//private methods
	 private void storeCount(long count) {
		 try {
			FileOutputStream fos = new FileOutputStream("file_count.txt");
			PrintStream ps = new PrintStream(fos);
			ps.print(count);
			ps.close();
			fos.close();
		 }catch(Exception e) {
			System.out.println("couldnot write:" +e); 
		 }
	 }
}
