package application;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
/**
 * class to handle web browser instantiation and online search for image
 * @author mehak
 *
 */
public class SearchOnline {

	//declarations
	Stage stage;
	Pane root;
	Scene ms;
	
	SearchOnline() {
		stage = new Stage();
	    stage.setTitle("Search Online");
	    root=new Pane();
	    ms = new Scene (root,800,600);
		stage.setScene(ms);
	}
	
	public void showWebsite() {
		try {
				
				final WebView browser = new WebView();
			    final WebEngine webEngine = browser.getEngine();
			    webEngine.load("https://www.tineye.com/");
			    root.getChildren().add(browser);
				stage.show();
		}
		catch(Exception e){
			System.out.println(e);
		}
	}
}
