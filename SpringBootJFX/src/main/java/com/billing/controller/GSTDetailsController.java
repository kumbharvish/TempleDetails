package com.billing.controller;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.dto.GSTDetails;
import com.billing.dto.Product;
import com.billing.dto.Tax;
import com.billing.service.TaxesService;
import com.billing.utils.AppUtils;
import com.billing.utils.IndianCurrencyFormatting;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

@Controller
public class GSTDetailsController {

	@Autowired
	AppUtils appUtils;

	@Autowired
	TaxesService taxesService;

	ObservableList<Product> productList;

	HashMap<String, Double> gstMapWithTax;

	HashMap<String, Double> gstMapWithAmounts;

	List<Tax> taxList;

	@FXML
	private GridPane gridPaneAmounts;

	@FXML
	private GridPane gridPaneTax;

	@FXML
	private TextField txtCGSTAmount;

	@FXML
	private TextField txtSGSTAmount;

	@FXML
	private TextField txtGSTTotalAmount;

	public void initialize() {
	}

	public void loadData() {
		double gstTotalGSTAmount = 0.0;
		gstMapWithTax = new HashMap<>();
		gstMapWithAmounts = new HashMap<>();
		for (Product product : productList) {
			GSTDetails gst = product.getGstDetails();
			gstTotalGSTAmount = gstTotalGSTAmount + gst.getGstAmount();
			if (gstMapWithTax.containsKey(gst.getName())) {
				gstMapWithTax.put(gst.getName(), gstMapWithTax.get(gst.getName()) + gst.getGstAmount());
				gstMapWithAmounts.put(gst.getName(), gstMapWithAmounts.get(gst.getName()) + gst.getTaxableAmount());
			} else {
				gstMapWithTax.put(gst.getName(), gst.getGstAmount());
				gstMapWithAmounts.put(gst.getName(), gst.getTaxableAmount());
			}
		}
		taxList = taxesService.getAllTax();
		int i = 0;
		for (Tax t : taxList) {
			String taxName = t.getName() + " (" + t.getValue() + "%)";
			Label lbl = getNewLabel(t.getName() + " (" + t.getValue() + "%) :");
			TextField txt = getNewTextField();
			txt.setText(IndianCurrencyFormatting.applyFormatting(gstMapWithAmounts.get(taxName)));
			gridPaneAmounts.add(lbl, 0, i);
			gridPaneAmounts.add(txt, 1, i);
			Label lbl2 = getNewLabel(t.getName() + " (" + t.getValue() + "%) :");
			TextField txt2 = getNewTextField();
			txt2.setText(IndianCurrencyFormatting.applyFormatting(gstMapWithTax.get(taxName)));
			gridPaneTax.add(lbl2, 0, i);
			gridPaneTax.add(txt2, 1, i);
			i++;
		}
		txtCGSTAmount.setText(IndianCurrencyFormatting.applyFormatting(gstTotalGSTAmount / 2));
		txtSGSTAmount.setText(IndianCurrencyFormatting.applyFormatting(gstTotalGSTAmount / 2));
		txtGSTTotalAmount.setText(IndianCurrencyFormatting.applyFormatting(gstTotalGSTAmount));
	}

	private TextField getNewTextField() {
		TextField txt = new TextField();
		txt.getStyleClass().add("readOnlyField");
		txt.setAlignment(Pos.CENTER_RIGHT);
		txt.setEditable(false);
		return txt;
	}

	private Label getNewLabel(String name) {
		Label lbl = new Label(name);
		lbl.getStyleClass().add("nodeLabel");
		GridPane.setHalignment(lbl, HPos.RIGHT);
		return lbl;
	}

}
