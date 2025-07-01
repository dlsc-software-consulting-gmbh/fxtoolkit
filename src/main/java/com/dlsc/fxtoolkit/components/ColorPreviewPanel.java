package com.dlsc.fxtoolkit.components;

import com.intellij.icons.AllIcons;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;

/**
 * A panel to display a large preview of a color, along with its
 * original definition, HEX, and RGB values, with copy buttons for each.
 */
public class ColorPreviewPanel extends JPanel {

    public ColorPreviewPanel(Color color, String originalDefinition) {
        super(new BorderLayout(0, 5));
        setBorder(JBUI.Borders.empty(5, 15));
        setBackground(JBColor.PanelBackground);

        // 1. Large Color Swatch
        JPanel colorSwatch = new JPanel();
        colorSwatch.setBackground(color);
        colorSwatch.setBorder(JBUI.Borders.customLine(JBColor.border()));
        colorSwatch.setPreferredSize(new Dimension(0, 60));
        add(colorSwatch, BorderLayout.NORTH);

        // 2. Panel for Color Details using GridBagLayout for alignment
        JPanel detailsPanel = new JPanel(new GridBagLayout());
        detailsPanel.setBackground(JBColor.PanelBackground);

        //  Calculate color string values
        String hex = String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
        if (color.getAlpha() != 255) {
            hex += String.format("%02X", color.getAlpha());
        }
        String rgb = String.format("rgb(%d, %d, %d)", color.getRed(), color.getGreen(), color.getBlue());
        if (color.getAlpha() != 255) {
            rgb = String.format("rgba(%d, %d, %d, %.2f)", color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() / 255.0);
        }

        // Add rows with copy buttons
        addDetailRow(detailsPanel, 0, "Raw:", originalDefinition);
        addDetailRow(detailsPanel, 1, "Hex:", hex);
        addDetailRow(detailsPanel, 2, "RGB:", rgb);

        add(detailsPanel, BorderLayout.CENTER);
    }

    private void addDetailRow(JPanel parent, int row, String label, String value) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = row;
        gbc.insets = JBUI.insets(1);

        // Label
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        parent.add(new JBLabel(label), gbc);

        // Value (using a non-editable text field for easy selection)
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        JTextField valueField = new JTextField(value);
        valueField.setEditable(false);
        valueField.setBorder(null);
        valueField.setBackground(JBColor.PanelBackground);
        parent.add(valueField, gbc);

        // Copy Button
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        JButton copyButton = new JButton(AllIcons.Actions.Copy);
        copyButton.addActionListener(e -> {
            StringSelection stringSelection = new StringSelection(value);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
        });
        parent.add(copyButton, gbc);
    }
}
