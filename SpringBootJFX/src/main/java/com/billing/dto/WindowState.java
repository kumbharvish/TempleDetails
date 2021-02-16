package com.billing.dto;

public class WindowState {
    private boolean maximized;
    private double xpos;
    private double ypos;
    private double width;
    private double height;
    
    public boolean isMaximized() {
        return maximized;
    }
    
    public void setMaximized(boolean value) {
        maximized = value;
    }
    
    public double getXPos() {
        return xpos;
    }
    
    public void setXPos(double val) {
        xpos = val;
    }
    
    public double getYPos() {
        return ypos;
    }
    
    public void setYPos(double val) {
        ypos = val;
    }
    
    public double getWidth() {
        return width;
    }
    
    public void setWidth(double val) {
        width = val;
    }
    
    public double getHeight() {
        return height;
    }
    
    public void setHeight(double val) {
        height = val;
    }

	@Override
	public String toString() {
		return "WindowState [maximized=" + maximized + ", xpos=" + xpos + ", ypos=" + ypos + ", width=" + width
				+ ", height=" + height + "]";
	}
    
}
