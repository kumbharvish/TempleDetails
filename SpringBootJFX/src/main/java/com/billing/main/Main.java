/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.billing.main;

import java.io.IOException;
import java.net.URL;

import org.slf4j.Logger;
/*import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;*/
import org.slf4j.LoggerFactory;

import com.billing.controller.HomeController;
import com.billing.dto.FinancialYear;
import com.billing.utils.TabContent;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author Dinesh
 */
public class Main extends Application {

	private static final Logger logger = LoggerFactory.getLogger(Main.class);

    @Override
    public void init() throws Exception {
        super.init();
        logger.error("Inside Main Application Init");
        /*System.out.println("Init Method");
        initLogger();
        
        String appDataPath = Global.getAppDataPath();
        try {
            System.setProperty("derby.system.home", appDataPath);
        } catch (Exception e) {
            logger.logp(Level.SEVERE, Main.class.getName(),
                    "init", "Error in setting the derby.system.home property", e);
        }

        if (Global.getUserPreferences().getAutoOpenLastOpenedYear()) {
            final FinancialYear year = Global.getLastOpenedFinancialYear();
            if (year != null) {
                //Database.openAsActiveYear(year);
            }
        }
        */
    }

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader();
        URL resource = this.getClass().getResource("/com/shopbilling/fx/views/Home.fxml");
        loader.setLocation(resource);
        Parent root = null;
        try {
            root = loader.<BorderPane>load();
        } catch (IOException e) {
            logger.error("Error in loading the Home page view file : ", e);
            throw e;
        }

        final HomeController homeController = loader.getController();
        homeController.MainWindow = stage;

        final Scene scene = new Scene(root, 850, 700);
        addKeyFilter(scene);
        stage.setScene(scene);

        stage.getIcons().add(new Image("/resources/images/billing_32.png"));
        stage.getIcons().add(new Image("/resources/images/billing_48.png"));
        stage.getIcons().add(new Image("/resources/images/billing_64.png"));
        
        stage.getProperties().put("hostServices", getHostServices());
        stage.setTitle("My Store");
        stage.setMaximized(true);
        stage.setOnCloseRequest((WindowEvent event) -> {
            if (!homeController.closeAllTabs()) {
                event.consume();
                return;
            }
            
            /*if (!stage.isIconified()) {
                //save the window state and window size
                WindowState s = new WindowState();
                s.setMaximized(stage.isMaximized());
                
                if (!stage.isMaximized()) {
                    s.setXPos(stage.getX());
                    s.setYPos(stage.getY());
                    s.setWidth(stage.getWidth());
                    s.setHeight(stage.getHeight());
                }

                Global.saveWindowLastState(s);
            }*/
            
        });

       /* stage.titleProperty().bind(new StringBinding() {
            {
                this.bind(Global.activeYearProperty());
            }

            @Override
            protected String computeValue() {
                String title = Global.getAppTitle();
                FinancialYear activeYear = Global.getActiveFinancialYear();
                if (activeYear != null) {
                    title += " (" + activeYear.toString() + ")";
                }
                return title;
            }
        });*/

        /*final WindowState s = Global.getWindowLastState();
        stage.setX(s.getXPos());
        stage.setY(s.getYPos());
        stage.setWidth(s.getWidth());
        stage.setHeight(s.getHeight());
        stage.setMaximized(s.isMaximized());*/
        
       /* notifyPreloader(new Preloader.StateChangeNotification(
                Preloader.StateChangeNotification.Type.BEFORE_START));*/
        stage.show();
    }

    @Override
    public void stop() throws Exception {

        final FinancialYear year = Global.getActiveFinancialYear();
        Global.setLastOpenedFinancialYear(year);

        //shutdown all databases and the Derby engine
       // Database.shutDown(null);

        super.stop();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

   /* private static void initLogger() {
        Path path = createLogFolder();
        if (path == null) {
            return;
        }

        String fileName = path.toAbsolutePath() + File.separator
                + "log-%g.xml"; //g stands for generation number. Numbering starts from 0;
        FileHandler fileHandler = null;

        try {
            //specify file handler to create 5 rotating files, if required, of max 1 MB each.
            fileHandler = new FileHandler(fileName, 1024 * 1024, 5, true);
        } catch (Exception e) {
            Logger.getGlobal().log(Level.SEVERE,
                    "Couldn't create the log file handler's instance", e);
            return;
        }

        Logger rootLogger = Logger.getLogger("");
        rootLogger.addHandler(fileHandler);

    }*/

    /*private static Path createLogFolder() {
        String userHomeDir = null;
        Logger logger = Logger.getGlobal();

        try {
            userHomeDir = System.getenv("LOCALAPPDATA");
            if (userHomeDir == null) {
                userHomeDir = System.getenv("USERPROFILE");
            }
            if (userHomeDir == null) {
                userHomeDir = System.getenv("user.home");
            }
        } catch (SecurityException e) {
            logger.log(Level.SEVERE,
                    "Couldn't get the user home diretory", e);
            return null;
        }

        if (userHomeDir == null) {
            userHomeDir = "/";
        }

        String pathString = userHomeDir + File.separator + "fxbilling"
                + File.separator + "log";
        Path path = Paths.get(pathString);

        try {
            if (!Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
                Files.createDirectories(path);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Couldn't create the log directory", e);
            return null;
        }

        return path;
    }*/

    private void addKeyFilter(final Scene scene) {

        final KeyCombination f4Key = new KeyCodeCombination(KeyCode.F4,
                KeyCombination.SHORTCUT_DOWN);

        final KeyCombination leftKey = new KeyCodeCombination(KeyCode.LEFT,
                KeyCombination.SHORTCUT_DOWN);
        final KeyCombination leftNumPadKey = new KeyCodeCombination(KeyCode.KP_LEFT,
                KeyCombination.SHORTCUT_DOWN);

        final KeyCombination rightKey = new KeyCodeCombination(KeyCode.RIGHT,
                KeyCombination.SHORTCUT_DOWN);
        final KeyCombination rightNumPadKey = new KeyCodeCombination(KeyCode.KP_RIGHT,
                KeyCombination.SHORTCUT_DOWN);

        scene.addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent event) -> {

            //if shortcut(a.k.a. ctrl) + F4 key combination was pressed
            if (f4Key.match(event)) {
                final TabPane tabPane = (TabPane) scene.lookup("#tabPane");

                if (!tabPane.getSelectionModel().isEmpty()) {
                    final Tab tab = tabPane.getSelectionModel().getSelectedItem();
                    TabContent controller = (TabContent) tab.getProperties().get("controller");
                    if (controller.shouldClose()) {
                        tabPane.getTabs().remove(tab);
                    }
                }
                event.consume();
            } else if (leftKey.match(event) || leftNumPadKey.match(event)) {
                /*    control+ left arrow key is pressed
                 shift to the previous tab (The movement is circular) */
                final TabPane tabPane = (TabPane) scene.lookup("#tabPane");
                if (tabPane.getTabs().size() > 1 && !tabPane.getSelectionModel().isEmpty()) {
                    if (tabPane.getSelectionModel().getSelectedIndex() == 0) {
                        tabPane.getSelectionModel().selectLast();
                    } else {
                        tabPane.getSelectionModel().selectPrevious();
                    }
                }
                event.consume();
            } else if (rightKey.match(event) || rightNumPadKey.match(event)) {
                /*    control+ right arrow key is pressed
                 shift to the next tab (The movement is circular) */
                final TabPane tabPane = (TabPane) scene.lookup("#tabPane");
                if (tabPane.getTabs().size() > 1 && !tabPane.getSelectionModel().isEmpty()) {
                    if (tabPane.getSelectionModel().getSelectedIndex() == 
                            tabPane.getTabs().size() - 1) { //last tab
                        tabPane.getSelectionModel().selectFirst();
                    } else {
                        tabPane.getSelectionModel().selectNext();
                    }
                }
                event.consume();
            }
        });
        
        scene.addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent event) -> {
           if (event.getTarget()instanceof Button && event.getCode() == KeyCode.ENTER) {
               Button button =  (Button) event.getTarget();
                if (!button.isDisabled()) {
                    button.fire();
                }
                event.consume();
           }
        });
        
         scene.addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent event) -> {
           if (event.getTarget()instanceof CheckBox && event.getCode() == KeyCode.ENTER) {
               CheckBox checkBox =  (CheckBox) event.getTarget();
                if (!checkBox.isDisabled()) {
                    checkBox.setSelected(!checkBox.isSelected());
                }
                event.consume();
           }
        });
    }

}
