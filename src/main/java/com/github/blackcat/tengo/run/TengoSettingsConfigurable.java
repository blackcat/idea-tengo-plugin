package com.github.blackcat.tengo.run;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;

public class TengoSettingsConfigurable implements Configurable {

    private TextFieldWithBrowseButton binaryField;

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) @NotNull String getDisplayName() {
        return "Tengo";
    }

    @Override
    public @Nullable JComponent createComponent() {
        binaryField = new TextFieldWithBrowseButton();
        binaryField.addBrowseFolderListener(
                "Select Tengo Binary",
                "Path to the tengo executable used to run scripts",
                null,
                FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor());

        JPanel form = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JLabel("Tengo binary:"), binaryField, 1, false)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();

        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(JBUI.Borders.empty(8));
        root.add(form, BorderLayout.NORTH);
        reset();
        return root;
    }

    @Override
    public boolean isModified() {
        return binaryField != null
                && !binaryField.getText().equals(TengoSettings.getInstance().getTengoBinaryPath());
    }

    @Override
    public void apply() throws ConfigurationException {
        if (binaryField == null) return;
        TengoSettings.getInstance().setTengoBinaryPath(binaryField.getText().trim());
    }

    @Override
    public void reset() {
        if (binaryField == null) return;
        binaryField.setText(TengoSettings.getInstance().getTengoBinaryPath());
    }

    @Override
    public void disposeUIResources() {
        binaryField = null;
    }
}
