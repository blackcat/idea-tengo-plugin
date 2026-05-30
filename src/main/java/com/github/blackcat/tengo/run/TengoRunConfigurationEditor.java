package com.github.blackcat.tengo.run;

import com.github.blackcat.tengo.TengoFileType;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.RawCommandLineEditor;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;

public class TengoRunConfigurationEditor extends SettingsEditor<TengoRunConfiguration> {

    private final Project project;
    private final TextFieldWithBrowseButton scriptField = new TextFieldWithBrowseButton();
    private final TextFieldWithBrowseButton workingDirField = new TextFieldWithBrowseButton();
    private final RawCommandLineEditor argumentsField = new RawCommandLineEditor();

    public TengoRunConfigurationEditor(@NotNull Project project) {
        this.project = project;

        FileChooserDescriptor scriptChooser = FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor()
                .withFileFilter(file -> TengoFileType.INSTANCE.equals(file.getFileType()));
        scriptField.addBrowseFolderListener(
                "Select Tengo Script",
                "Path to the .tengo script to run",
                project,
                scriptChooser);

        workingDirField.addBrowseFolderListener(
                "Select Working Directory",
                "Process working directory",
                project,
                FileChooserDescriptorFactory.createSingleFolderDescriptor());
    }

    @Override
    protected void resetEditorFrom(@NotNull TengoRunConfiguration cfg) {
        scriptField.setText(cfg.getScriptPath());
        argumentsField.setText(cfg.getArguments());
        workingDirField.setText(cfg.getWorkingDirectory());
    }

    @Override
    protected void applyEditorTo(@NotNull TengoRunConfiguration cfg) {
        cfg.setScriptPath(scriptField.getText().trim());
        cfg.setArguments(argumentsField.getText());
        cfg.setWorkingDirectory(workingDirField.getText().trim());
    }

    @Override
    protected @NotNull JComponent createEditor() {
        JPanel form = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JLabel("Script:"), scriptField, 1, false)
                .addLabeledComponent(new JLabel("Arguments:"), argumentsField, 1, false)
                .addLabeledComponent(new JLabel("Working directory:"), workingDirField, 1, false)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(JBUI.Borders.empty(8));
        root.add(form, BorderLayout.NORTH);
        return root;
    }
}
