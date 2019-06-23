package com.billing.main;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.billing.constants.AppConstants;
import com.billing.controllers.LoginController;
import com.billing.properties.AppProperties;
import com.billing.service.AppLicenseServices;
import com.billing.service.DBBackupService;
import com.billing.utils.PDFUtils;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;


public class MyStoreFxSplash extends Application {
    private Pane splashLayout;
    private ProgressBar loadProgress;
    private Label progressText;
    private static final int SPLASH_WIDTH = 590;
    private static final int SPLASH_HEIGHT = 106;
    
    private static final Logger logger = LoggerFactory.getLogger(MyStoreFxSplash.class);
	
	private Parent parent;

    public static void main(String[] args) throws Exception {
        launch(args);
    }

    @Override
    public void init() {
        ImageView splash = new ImageView(new Image(MyStoreFxSplash.class.getResourceAsStream("/images/MyStoreSplash.png")));
        loadProgress = new ProgressBar();
        loadProgress.setPrefWidth(SPLASH_WIDTH);
        progressText = new Label("");
        splashLayout = new VBox();
        splashLayout.getChildren().addAll(splash, loadProgress, progressText);
        progressText.setAlignment(Pos.CENTER);
        splashLayout.setStyle(
                "-fx-padding: 5; " +
                "-fx-background-color: cornsilk; " +
                "-fx-border-width:5; " +
                "-fx-border-color: " +
                    "linear-gradient(" +
                        "to bottom, " +
                        "chocolate, " +
                        "derive(chocolate, 50%)" +
                    ");"
        );
        splashLayout.setEffect(new DropShadow());
    }

    @Override
    public void start(final Stage initStage) throws Exception {
        final Task<ObservableList<String>> tasks = new Task<ObservableList<String>>() {
            @Override
            protected ObservableList<String> call() throws InterruptedException {
                ObservableList<String> loadingStepMsg = FXCollections.observableArrayList(
                                "Loading","Getting Properties", "Applying Properties", "Starting Application");
                for (int i = 0; i < loadingStepMsg.size(); i++) {
                    updateProgress(i + 1, loadingStepMsg.size());
                    String nextFriend = loadingStepMsg.get(i);
                    updateMessage(nextFriend + " ...");
                    Thread.sleep(1000);
                }
                Thread.sleep(1000);
                return loadingStepMsg;
            }
        };

        showSplash(initStage,tasks,() -> showLoginStage(initStage));
        new Thread(tasks).start();
    }

    private void showLoginStage(Stage initStage){
    	 try {
             if(!AppProperties.check()){
            	 PDFUtils.showWarningAlert(null, AppConstants.LICENSE_ERROR_1, AppConstants.LICENSE_ERROR);
 				System.exit(0);
 			}else{
 				if(AppLicenseServices.change()){
 					PDFUtils.showWarningAlert(null, AppConstants.COMP_DATE_ERROR, AppConstants.COMP_DATE);
 					System.exit(0);
 				}else{
 					if(!AppProperties.doCheck()){
 						PDFUtils.showWarningAlert(null, AppConstants.LICENSE_ERROR_2, AppConstants.LICENSE_EXPIRED);
 						System.exit(0);
 					}else{
 						logger.error(" --- Application Check Complete and Started --- ");
 						FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/shopbilling/fx/views/LoginScreen.fxml"));
 						parent = fxmlLoader.load();
 				        LoginController loginController = fxmlLoader.getController();
 				        loginController.show(parent);
 					}
 				}
 			}
 			
 		} catch (Exception e) {
 			logger.error("Application Startup Exception --> :" ,e);
 			e.printStackTrace();
 		}
    }

    private void showSplash(
            final Stage initStage,
            Task<?> task,
            InitCompletionHandler initCompletionHandler
    ) {
        progressText.textProperty().bind(task.messageProperty());
        loadProgress.progressProperty().bind(task.progressProperty());
        task.stateProperty().addListener((observableValue, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                loadProgress.progressProperty().unbind();
                loadProgress.setProgress(1);
                initStage.toFront();
                FadeTransition fadeSplash = new FadeTransition(Duration.seconds(1.2), splashLayout);
                fadeSplash.setFromValue(1.0);
                fadeSplash.setToValue(0.0);
                fadeSplash.setOnFinished(actionEvent -> initStage.hide());
                fadeSplash.play();

                initCompletionHandler.complete();
            } // todo add code to gracefully handle other task states.
        });

        Scene splashScene = new Scene(splashLayout, Color.TRANSPARENT);
        final Rectangle2D bounds = Screen.getPrimary().getBounds();
        initStage.setScene(splashScene);
        initStage.setX(bounds.getMinX() + bounds.getWidth() / 2 - SPLASH_WIDTH / 2);
        initStage.setY(bounds.getMinY() + bounds.getHeight() / 2 - SPLASH_HEIGHT / 2);
        initStage.initStyle(StageStyle.TRANSPARENT);
        initStage.setAlwaysOnTop(true);
        initStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/shop32X32.png")));
        initStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/shop48X48.png")));
        initStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/shop64X64.png")));
        initStage.show();
    }
    
    @Override
    public void stop() throws Exception {
    	DBBackupService.createDBDump();
    }

    public interface InitCompletionHandler {
        void complete();
    }
}