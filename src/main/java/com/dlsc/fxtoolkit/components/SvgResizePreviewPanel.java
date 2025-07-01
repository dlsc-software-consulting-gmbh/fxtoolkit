package com.dlsc.fxtoolkit.components;

import com.dlsc.fxtoolkit.icon.SvgIcon;
import com.dlsc.fxtoolkit.model.Size2D;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.function.Consumer;

public class SvgResizePreviewPanel extends JPanel {

    private final SvgIcon svgIcon;
    private final JBTextField widthField;
    private final JBTextField heightField;
    private final double aspectRatio;
    private final JButton applyButton;
    private boolean isUpdating = false;
    private final DecimalFormat formatter = new DecimalFormat("0.###");

    public SvgResizePreviewPanel(SvgIcon icon, Consumer<Size2D> applySizeAction) {
        super(new BorderLayout(15, 0));
        this.svgIcon = icon;

        Rectangle2D bounds = icon.getBounds();
        this.aspectRatio = (bounds != null && bounds.getHeight() > 0 && bounds.getWidth() > 0) ? bounds.getWidth() / bounds.getHeight() : 1.0;
        // 1. Preview Area (Left side)
        JPanel previewWrapper = createPreviewWrapper();
        add(previewWrapper, BorderLayout.CENTER);

        // 2. Controls Area (Right side)
        JPanel controlsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = JBUI.insets(2);

        // Width & Height fields
        JPanel sizePanel = new JPanel(new GridLayout(2, 2, 5, 5));

        double initialWidth = (bounds != null && bounds.getWidth() > 0) ? bounds.getWidth() : 100.0;
        double initialHeight = (bounds != null && bounds.getHeight() > 0) ? bounds.getHeight() : 100.0 / aspectRatio;

        widthField = new JBTextField(formatter.format(initialWidth));
        heightField = new JBTextField(formatter.format(initialHeight));

        sizePanel.add(new JBLabel("Width:"));
        sizePanel.add(widthField);
        sizePanel.add(new JBLabel("Height:"));
        sizePanel.add(heightField);

        gbc.gridy = 0;
        controlsPanel.add(sizePanel, gbc);

        //  Apply Button
        applyButton = new JButton("Apply Size");
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = JBUI.insetsTop(10);
        controlsPanel.add(applyButton, gbc);

        // Add an empty component to push everything up
        gbc.gridy = 2;
        gbc.weighty = 1.0;
        controlsPanel.add(new Box.Filler(new Dimension(0, 0), new Dimension(0, 0), new Dimension(0, Short.MAX_VALUE)), gbc);

        add(controlsPanel, BorderLayout.EAST);

        //  Logic
        setupListeners();
        ActionListener applyListener = e -> {
            try {
                double w = Double.parseDouble(widthField.getText().replace(',', '.'));
                double h = Double.parseDouble(heightField.getText().replace(',', '.'));
                applySizeAction.accept(new Size2D(w, h));
            } catch (NumberFormatException ignored) {
            }
        };
        applyButton.addActionListener(applyListener);
        widthField.addActionListener(applyListener);
        heightField.addActionListener(applyListener);

        setBorder(JBUI.Borders.empty(10));
    }

    private JPanel createPreviewWrapper() {
        int containerSize = 120;
        int iconSize = 100;
        JPanel previewWrapper = new JPanel(new BorderLayout());
        previewWrapper.setBorder(JBUI.Borders.customLine(JBColor.border()));
        previewWrapper.setBackground(JBColor.PanelBackground);
        Icon previewIcon = new SvgIcon(svgIcon.getPathData(), iconSize, iconSize);
        previewWrapper.add(new JBLabel(previewIcon), BorderLayout.CENTER);
        previewWrapper.setPreferredSize(new Dimension(containerSize, containerSize));
        return previewWrapper;
    }

    public JComponent getPreferredFocusedComponent() {
        return applyButton;
    }

    private void setupListeners() {
        DocumentListener listener = new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                update(e);
            }

            public void removeUpdate(DocumentEvent e) {
                update(e);
            }

            public void insertUpdate(DocumentEvent e) {
                update(e);
            }

            public void update(DocumentEvent e) {
                if (isUpdating) return;

                final JTextField sourceField = e.getDocument() == widthField.getDocument() ? widthField : heightField;

                SwingUtilities.invokeLater(() -> {
                    isUpdating = true;
                    try {
                        String text = sourceField.getText();
                        if (text != null && !text.isEmpty() && !text.equals(".") && !text.endsWith(",")) {
                            double value = Double.parseDouble(text.replace(',', '.'));
                            if (sourceField == widthField) {
                                heightField.setText(formatter.format(value / aspectRatio));
                            } else {
                                widthField.setText(formatter.format(value * aspectRatio));
                            }
                        }
                    } catch (NumberFormatException ignored) {
                    } finally {
                        isUpdating = false;
                    }
                });
            }
        };
        widthField.getDocument().addDocumentListener(listener);
        heightField.getDocument().addDocumentListener(listener);
    }

}
