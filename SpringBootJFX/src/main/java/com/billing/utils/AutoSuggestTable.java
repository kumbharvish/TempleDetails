package com.billing.utils;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

public class AutoSuggestTable<T> extends JTextField {

private List<T> items;

private final JComboBox combobox = new JComboBox();

private final DefaultComboBoxModel comboBoxModel;

private final BasicComboPopup suggestPopup;

public AutoSuggestTable() {
    this(new ArrayList<T>());
}

public AutoSuggestTable(List<T> items) {
    this.items = items;
    Collections.sort(items, new Compare());
    comboBoxModel = new DefaultComboBoxModel();
    combobox.setModel(comboBoxModel);
    suggestPopup = new BasicComboPopup(combobox);
    install();
}

private void addToSuggestList(List<Object> list) {
    comboBoxModel.removeAllElements();
    if (list.isEmpty()) {
        suggestPopup.hide();
    } else {
        for (Object elm : list) {
            comboBoxModel.addElement(elm);
        }
        suggestPopup.show(this, 0, getHeight());
        suggestPopup.setPopupSize(getWidth(), suggestPopup.getHeight());
    }
}

public void hidePopup() {
    suggestPopup.hide();
}

/**
 * The value to search for I'm checking for case insensitive contains Modify
 * it if you want
 *
 * @param value
 */
private void filter(String value) {
    List<Object> tempList = new ArrayList<Object>();
    for (Object item : items) {
        if (item.toString().toLowerCase().contains(value.toLowerCase())) {
            tempList.add(item);
        }
    }
    addToSuggestList(tempList);
}

public List<T> getItems() {
    return items;
}

public void withItems(List<T> items) {
    this.items = items;
    Collections.sort(items, new Compare());
}

public void addItem(T item) {
    items.add(item);
}

public void clearItems() {
    items.clear();
}

private void install() {
    setBorder(null);
    setFocusTraversalKeysEnabled(false);
    getDocument().addDocumentListener(new DocumentListener() {

        @Override
        public void insertUpdate(DocumentEvent e) {
            onTextChange(e);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            onTextChange(e);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            onTextChange(e);
        }

    });
    getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "commit");
    getActionMap().put("commit", new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            setSelectedItemFromList();
        }
    });
    getInputMap().put(KeyStroke.getKeyStroke("DOWN"), "traversedown");
    getActionMap().put("traversedown", new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            traversedown();
        }
    });
    getInputMap().put(KeyStroke.getKeyStroke("UP"), "traverseup");
    getActionMap().put("traverseup", new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            traverseup();
        }
    });
}

private void setSelectedItemFromList() {
    if (comboBoxModel.getSize() > 0) {
        setText(suggestPopup.getList().getSelectedValue().toString());
        suggestPopup.hide();
    }
}

private void traverseup() {
    if (comboBoxModel.getSize() > 0) {
        int index = suggestPopup.getList().getSelectedIndex() - 1;
        if (index >= 0) {
            suggestPopup.getList().setSelectedIndex(index);
            suggestPopup.getList().ensureIndexIsVisible(index);
        }
    }
}

private void traversedown() {
    if (comboBoxModel.getSize() > 0) {
        int index = suggestPopup.getList().getSelectedIndex() + 1;
        if (index < comboBoxModel.getSize()) {
            suggestPopup.getList().setSelectedIndex(index);
            suggestPopup.getList().ensureIndexIsVisible(index);
        }
    }
}

private void onTextChange(DocumentEvent e) {
    Document source = e.getDocument();
    int length = source.getLength();
    try {
        filter(source.getText(0, length));
    } catch (BadLocationException be) {
        System.out.println("Contents: Unknown");
    }
}

private static class Compare implements Comparator {

    @Override
    public int compare(Object o1, Object o2) {
        return Objects.toString(o1, "").compareTo(Objects.toString(o2, ""));
    }

}
}