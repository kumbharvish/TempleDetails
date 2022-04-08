package com.billing.utils;

import com.billing.dto.UserDetails;

import javafx.beans.Observable;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

@SuppressWarnings("restriction")
public interface TabContent {
    public boolean shouldClose();
    public void putFocusOnNode();
    public boolean loadData();
    public void setMainWindow(Stage stage);
    public void setTabPane(TabPane tabPane);
    public void setUserDetails(UserDetails user);
    //Common Methods
    public void initialize();
    public boolean saveData();
    public void invalidated(Observable observable);
    public void closeTab();
    public boolean validateInput();
   
}
