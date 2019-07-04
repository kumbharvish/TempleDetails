/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.billing.main;

import static com.billing.dto.UserPreferences.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import com.billing.dto.ApplicationAttributes;
import com.billing.dto.FinancialYear;
import com.billing.dto.UserPreferences;
import com.billing.dto.WindowState;
import com.billing.utils.DirectoryTreeCopy;
import com.billing.utils.DirectoryTreeDelete;
import com.billing.utils.TabContent;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 *
 * @author Dinesh
 */
public abstract class Global {

    public static final String APP_DATA_FOLDER_NAME = "fy";
    public static final String APP_NAME = "fxbilling";
    private static String appTitle = null;

    private static Logger logger = Logger.getLogger(Global.class.getName());
    private final static ObjectProperty<FinancialYear> activeYearProperty =
            new SimpleObjectProperty<FinancialYear>(null);
    
    public static String getAppTitle() {
        if (appTitle == null) {
             ApplicationAttributes attrib = getApplicationAttributes();
            appTitle = attrib.getApplicationName() + " " + attrib.getApplicationVersion();
        }
        return appTitle;
    }
    
    public static FinancialYear getActiveFinancialYear() {
        return activeYearProperty.get();
    }
    
    public static void setActiveFinancialYear(FinancialYear fy) {
        activeYearProperty.set(fy);
    }
    
    public static ObjectProperty<FinancialYear> activeYearProperty() {
        return activeYearProperty;
    }
    

    public static String getAppDataPath() {

        Preferences node = Preferences.systemNodeForPackage(Global.class);
        String path = node.get(KEY_APP_DATA_FOLDER, "");

        if (path.isEmpty()) {
            path = getDefaultAppDataPath();
        }

        return path;

    }

    private static String getDefaultAppDataPath() {
        String path = null;

        try {
            path = System.getenv("ProgramData");
            if (path == null) {
                path = System.getenv("ALLUSERSPROFILE");
            }
            if (path == null) {
                path = System.getenv("PUBLIC");
            }
            if (path == null) {
                path = System.getenv("USERPROFILE");
            }
            if (path == null) {
                path = System.getProperty("user.home");
            }
        } catch (SecurityException e) {
            logger.logp(Level.SEVERE, Global.class.getName(),
                    "getAppDataFolderPath", "Error on enquiring about Environment variables", e);
        }

        if (path == null) {
            path = "/"; //it represents the root of the current drive
        }

        return path;
    }

    /**
     *
     * @param path - The value to save in the backing store.
     * @return - True if the path is saved successfully, false otherwise.
     */
    public static boolean saveAppDataFolderPath(String path) {
        Preferences node = Preferences.systemNodeForPackage(Global.class);

        try {
             node.put(KEY_APP_DATA_FOLDER, path);
            node.flush();
        } catch (Exception ex) {
            logger.logp(Level.SEVERE, Global.class.getName(), "saveAppDataFolderPath",
                    "Error in saving application data path", ex);
            return false;
        }

        return true;
    }

    
    /**
     * This is the function to be called whilst getting the path to connect to
     * a database.
     * @return 
     */
    public static String getExtendedAppDataPath() {
        
        return getAppDataPath() + File.separator
                + APP_NAME + File.separator
                + APP_DATA_FOLDER_NAME;
    }

    public static List<FinancialYear> getExistingFinancialYears(final String lookupFolderPath)
            throws Exception {
        
        List<FinancialYear> years = new ArrayList<>(10);
        String path = null;
        
        if (lookupFolderPath == null) {
            path = getExtendedAppDataPath();
        } else {
            path = lookupFolderPath;
        }
        
        if (!Files.exists(Paths.get(path), LinkOption.NOFOLLOW_LINKS)) {
            return years;
        }
        
        final DirectoryStream.Filter<Path> filter
                = (Path file) -> (Files.isDirectory(file, LinkOption.NOFOLLOW_LINKS));

        final Path dir = Paths.get(path);
        Path file = null;
        String fileName = null;
        String[] nameParts = null;
        FinancialYear fy = null;
        
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, filter)) {
            Iterator<Path> iterator = stream.iterator();
            while(iterator.hasNext()) {
                file = iterator.next();
                fileName = file.getFileName().toString();
                fy = FinancialYear.parse(fileName);
                if (fy != null) {
                    years.add(fy);
                }
            }
        } catch (Exception ex) {
            logger.logp(Level.SEVERE, Global.class.getName(), "getExistingFinancialYears",
                    "Error occurred while looking for financial years in the directory " + path, ex);
            throw ex;
        }

        //sort the list in the descending order
        FinancialYear.sort(years, false);
        return years;

    }
    
    public static UserPreferences getUserPreferences() {
        
        UserPreferences preferences = new UserPreferences();
        Preferences node = Preferences.systemNodeForPackage(Global.class);
        
        boolean value = node.getBoolean(KEY_PRINT_ON_SAVE, false);
       preferences.setPrintOnSave(value);
       
        value = node.getBoolean(KEY_SHOW_PRINT_PREVIEW, true);
        preferences.setShowPrintPreview(value);
        
        value = node.getBoolean(KEY_SHOW_PRINT_DIALOG, true);
        preferences.setShowPrintDialog(value);
        
        value = node.getBoolean(KEY_AUTO_LOAD_LAST_OPENED_YEAR,
                true);
        preferences.setAutoOpenLastOpenedYear(value);
        
        return preferences;
        
    }
    
    public static boolean setUserPreferences(UserPreferences preferences){
        Preferences node = Preferences.systemNodeForPackage(Global.class);
        
        try {
             node.putBoolean(KEY_PRINT_ON_SAVE, preferences.getPrintOnSave());
            node.putBoolean(KEY_SHOW_PRINT_PREVIEW, preferences.getShowPrintPreview());
            node.putBoolean(KEY_SHOW_PRINT_DIALOG, preferences.getShowPrintDialog());
            node.putBoolean(KEY_AUTO_LOAD_LAST_OPENED_YEAR, 
                preferences.getAutoOpenLastOpenedYear());
            node.flush();
        } catch (Exception e) {
            logger.logp(Level.SEVERE, Global.class.getName(), "setUserPreferences",
                    "Error in saving user preferences", e);
            return false;
        }
        
        return true;
    }
    
    public static String getTempDirectoryPath() {
        String path = null;
        
        try {
            path = System.getenv("TEMP");
            if (path == null) {
                path = System.getProperty("java.io.tmpdir");
            }
        } catch (Exception e) {
            logger.logp(Level.SEVERE, Global.class.getName(), "getTempDirectoryPath",
                    "Error in enquiring for the temp directory", e);
        }
        
        if (path == null) {
            path = "/" + File.separator + "temp";
        }
        
        return path;
    }
    
     public static boolean createFinancialYearFolder(final FinancialYear fy) {
            
        final String folderPathString = getExtendedAppDataPath() +
                File.separator + fy.toEpochMillis();
        final Path folderPath = Paths.get(folderPathString);
        
        try {
            Files.createDirectories(folderPath);
        } catch (Exception ex) {
           logger.logp(Level.SEVERE, Global.class.getName(),
                  "createFinancialYearFolder" , 
                  "Error creating the folder for the new year", ex);
           return false;
        }
        
        return true;
        
    }
     
    public static boolean deleteFinancialYearFolder(final FinancialYear year) {
          final String folderPathString = getExtendedAppDataPath() +
                File.separator + year.toEpochMillis();
        final Path folderPath = Paths.get(folderPathString);
        
        try {
            Files.walkFileTree(folderPath, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                };

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.TERMINATE;
                }
                
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
                
                
            });
        } catch (Exception e) {
            logger.logp(Level.SEVERE, Global.class.getName(), 
                    "deleteFinancialYearFolder", 
                    "Error in deleting the folder for the year: " + year.toString(), e);
            return false;
        }
        
        return true;
       
    }
    
    public static boolean closeTabs(final TabPane tabPane, final Tab leaveItOpen) {
            final ObservableList<Tab> tabs = tabPane.getTabs();
            final List<Tab> tabsToRemove = new ArrayList<>(tabs.size());

            for (Tab tab : tabs) {
                if (!tab.equals(leaveItOpen)) {
                    tabPane.getSelectionModel().select(tab);
                    TabContent controller = (TabContent) tab
                            .getProperties().get("controller");
                    if (controller.shouldClose()) {
                        tabsToRemove.add(tab); //mark this tab to be removed
                    } else {
                        return false;
                    }
                }
            }

            tabs.removeAll(tabsToRemove); //actually remove the tags here
            return true;
        
    }
    
    public static FinancialYear getLastOpenedFinancialYear() {
         Preferences node = Preferences.systemNodeForPackage(Global.class);
        String value = node.get(KEY_LAST_OPENED_YEAR, "");
        
        if (value.isEmpty()) {
            return null;
        }
        
        return FinancialYear.parse(value);
    }
    
    public static boolean setLastOpenedFinancialYear(final FinancialYear year) {
         Preferences node = Preferences.systemNodeForPackage(Global.class);
         String value = (year == null ? "" : year.toEpochMillis());
         
           try {
                 node.put(KEY_LAST_OPENED_YEAR, value);
                node.flush();
        } catch (Exception ex) {
            logger.logp(Level.SEVERE, Global.class.getName(), "setLastOpenedFinancialYear",
                    "Error in saving last opened financial year", ex);
            return false;
        }

        return true;
    }
    
    /**
     * Saves the window state of the main application window. This method 
     * is meant to be called at during the shutdown of the application.
     * @param windowState - Window State of the main application window at application shutdown
     * @return - Ture if the state is saved successfully, false otherwise
     */
    public static boolean saveWindowLastState(WindowState windowState) {
         Preferences node = Preferences.systemNodeForPackage(Global.class);
         
           try {
                node.putBoolean(KEY_IS_WINDOW_MAXIMIZED, windowState.isMaximized());
                node.putDouble(KEY_XPOS, windowState.getXPos());
                node.putDouble(KEY_YPOS, windowState.getYPos());
                node.putDouble(KEY_WIDTH, windowState.getWidth());
                node.putDouble(KEY_HEIGHT, windowState.getHeight());
                
                node.flush();
        } catch (Exception ex) {
            logger.logp(Level.SEVERE, Global.class.getName(), "saveWindowState",
                    "Error in saving window state", ex);
            return false;
        }

        return true;
    }
    
     public static WindowState getWindowLastState() {
         final Preferences node = Preferences.systemNodeForPackage(Global.class);
         
          final WindowState defaultState = getDefaultWindowState();
          final WindowState windowState = new WindowState();
          
          windowState.setMaximized(node.getBoolean(KEY_IS_WINDOW_MAXIMIZED, 
                  defaultState.isMaximized()));
          windowState.setXPos(node.getDouble(KEY_XPOS, defaultState.getXPos()));
          windowState.setYPos(node.getDouble(KEY_YPOS, defaultState.getYPos()));
          windowState.setWidth(node.getDouble(KEY_WIDTH, defaultState.getWidth()));
          windowState.setHeight(node.getDouble(KEY_HEIGHT, defaultState.getHeight()));

        return windowState;
    }
     
     public static String getLastBackupLocation() {
          Preferences node = Preferences.systemNodeForPackage(Global.class);
          return node.get(KEY_LAST_BACKUP_LOCATION, getUserHomeDirectory());
     }
     
     public static void saveLastBackupLocation(String location) {
         Preferences node = Preferences.systemNodeForPackage(Global.class);
          
         try {
             node.put(KEY_LAST_BACKUP_LOCATION, location);
             node.flush();
         } catch (Exception e) {
              logger.logp(Level.SEVERE, Global.class.getName(), 
                     "setLastBackupLocation", 
                     "Error in saving Last Backup Location", e);
         }
     }
     
     public static String getUserHomeDirectory() {
         String homePath = null;
         
         try {
             homePath = System.getProperty("user.home", "/");
         } catch (Exception e) {
             logger.logp(Level.SEVERE, Global.class.getName(), 
                     "getUserHomeDirectory", 
                     "Error in reading User's Home Directory", e);
             homePath = "/";
         }
         
         return Paths.get(homePath).toAbsolutePath().toString();
     }
     
     /**
      * This method moves all existing databases from the current location
      * to the new location. The operation is atomic in the sense that 
      * either all the databases are moved, or none at all.
      * 
      * @param oldLocation - Current/Old database location
      * @param newLocation - New/Proposed database location
      * @return - True if the existing databases, if any, are moved 
      * successfully from the old location to the new location
      */
     public static boolean moveDatabases(final String oldLocation, 
             final String newLocation) {
        final String fromPath = oldLocation + File.separator + APP_NAME + 
                File.separator + APP_DATA_FOLDER_NAME;
        final String toPath = newLocation + File.separator + APP_NAME + 
                File.separator + APP_DATA_FOLDER_NAME;
        
        final Path destinationPath = Paths.get(toPath);
         try {
             if (Files.exists(destinationPath) &&
                     Files.isDirectory(destinationPath)) {
                 Files.walkFileTree(destinationPath, new DirectoryTreeDelete());
             }
         } catch (Exception e) {
             logger.logp(Level.SEVERE, Global.class.getName(), "moveDatabases",
                     "Error in Deleting the folder " + toPath, e);
             return false;
         }
         
         try {
            Files.createDirectories(destinationPath);
         } catch (Exception e) {
              logger.logp(Level.SEVERE, Global.class.getName(), "moveDatabases",
                     "Error in creating the folder " + toPath, e);
             return false;
         }
         
         final DirectoryTreeCopy copyVisitor = new DirectoryTreeCopy(fromPath, toPath);
         final Path sourcePath = Paths.get(fromPath);
         try {
             Files.walkFileTree(sourcePath, copyVisitor);
         } catch (Exception e) {
             logger.logp(Level.SEVERE, Global.class.getName(), "moveDatabases",
                     "Error in copying the databases to the folder " + toPath, e);
             
            try {
                Files.walkFileTree(destinationPath, new DirectoryTreeDelete());
            } catch (Exception innerEx) {
                logger.logp(Level.SEVERE, Global.class.getName(), "moveDatabases",
                    "Error in deleting the folder tree " + toPath, innerEx);
            }
             
             return false;
         }
         
         //now delete the source folder tree
         try {
             Files.walkFileTree(sourcePath, new DirectoryTreeDelete());
         } catch (Exception e) {
             logger.logp(Level.SEVERE, Global.class.getName(), "moveDatabases",
                     "Error in deleting the folder tree " + fromPath, e);
             return false;
         }
         
         return true;
         
     }
     
    /**
     * 
     * @return Returns the default position and size of the application's main window
     */
     public static WindowState getDefaultWindowState() {
         // set width / height values to be 90% of users screen resolution
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        
        WindowState windowState = new WindowState();
        windowState.setWidth(screenBounds.getWidth() * 0.9); 
        windowState.setHeight(screenBounds.getHeight() * 0.9); 
        
        windowState.setXPos( (screenBounds.getWidth() - windowState.getWidth()) / 2);
        windowState.setYPos( (screenBounds.getHeight() - windowState.getHeight()) / 2);
        
        windowState.setMaximized(false);
        System.out.println(" -- Window State : "+windowState);
         return windowState;
     }
     
     /**
      * 
      * @return The application folder path
      */
//     public static String getApplicationPath() {
//        final String jarFilePath = Global.class.getProtectionDomain()
//                    .getCodeSource().getLocation().getPath();
//         
//       final File jarFile = new File(jarFilePath);
//       
//       if (DEBUG) {
//           return jarFile.getParentFile().getParentFile().getParentFile().getAbsolutePath();
//       } else {
//           return jarFile.getParentFile().getAbsolutePath();
//       }
//         
//     }
     
     public static void styleAlertDialog(final Alert alert) {
        final String styleSheetPath = "/css/alertDialog.css";
        final DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
                Global.class.getResource(styleSheetPath).toExternalForm());
     }
     
      public static void setStageDefaultDimensions(final Stage stage) {
        final WindowState defaultWindowState = Global.getDefaultWindowState();
        stage.setWidth(defaultWindowState.getWidth());
        stage.setHeight(defaultWindowState.getHeight());
        stage.setX(defaultWindowState.getXPos());
        stage.setY(defaultWindowState.getYPos());
    }
      
    public static ApplicationAttributes getApplicationAttributes() {
        Attributes attributes = null;
        
        try(InputStream stream = Global.class.getClassLoader().getResourceAsStream(
					"META-INF/MANIFEST.MF")) {
            Manifest manifest = new Manifest(stream);
            attributes = manifest.getMainAttributes();
        } catch (Exception e) {
            logger.logp(Level.SEVERE, Global.class.getName(), 
                    "readManifestAttributes", 
                    "An error in reading the manifest attributes", e);
            return null;
        }
        
        return populateApplicationAttributes(attributes);
    }
    
    private static  ApplicationAttributes populateApplicationAttributes(
            Attributes manifest) {
        
        ApplicationAttributes attrib = new ApplicationAttributes();
        attrib.setApplicationName(manifest.getValue("Implementation-Title"));
        attrib.setApplicationVersion(manifest.getValue("Implementation-Version"));
        attrib.setApplicationCatchPhrase(manifest.getValue("Catch-Phrase"));
        
        attrib.setDeveloperName(manifest.getValue("Implementation-Vendor"));
        attrib.setDeveloperMobileNumber(manifest.getValue("Mobile-Number"));
        attrib.setDeveloperEmailAddress(manifest.getValue("Email-Address"));
        attrib.setDeveloperBlogURL(manifest.getValue("Blog-URL"));
        attrib.setDeveloperLocation(manifest.getValue("Location"));
        return attrib;
    }
    
}
