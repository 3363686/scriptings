/**
 * Created by _ame_ on 12.07.2015 19:27.
 */
package com.simpleplugin;

import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.IncorrectOperationException;
import com.simpleplugin.psi.SimpleElementFactory;
import com.simpleplugin.psi.SimpleFile;
import com.simpleplugin.psi.SimpleProperty;
import com.simpleplugin.psi.SimpleTypes;
import org.jetbrains.annotations.NotNull;

class CreatePropertyQuickFix extends BaseIntentionAction {
    private String key;

    CreatePropertyQuickFix(String key) {
        this.key = key;
    }

    @NotNull
    @Override
    public String getText() {
        return "Create property";
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return "Simple properties";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        return true;
    }

    @Override
    public void invoke(@NotNull final Project project, final Editor editor, PsiFile file) throws IncorrectOperationException {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                final FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor(SimpleFileType.INSTANCE);
                descriptor.setRoots(project.getBaseDir());
                final VirtualFile file = FileChooser.chooseFile(descriptor, project, null);
                if (file != null) {
                    ApplicationManager.getApplication().runWriteAction(new Runnable() {
                        @Override
                        public void run() {
                            SimpleFile simpleFile = (SimpleFile) PsiManager.getInstance(project).findFile(file);
                            ASTNode lastChildNode = simpleFile.getNode().getLastChildNode();
                            if (lastChildNode != null && !lastChildNode.getElementType().equals(SimpleTypes.CRLF)) {
                                simpleFile.getNode().addChild(SimpleElementFactory.createCRLF(project).getNode());
                            }
                            SimpleProperty property = SimpleElementFactory.createProperty(project, key, "");
                            simpleFile.getNode().addChild(property.getNode());
                            ((Navigatable) property.getLastChild().getNavigationElement()).navigate(true);
                            FileEditorManager.getInstance(project).getSelectedTextEditor().getCaretModel().
                                    moveCaretRelatively(2, 0, false, false, false);
                        }
                    });
                }
            }
        });
    }
}
//========================================================
ApplicationManager.getApplication().runWriteAction(new Runnable() {
    @Overridepublic void run() {CommandProcessor.getInstance().executeCommand(project, new Runnable() {
        @Overridepublic void run() {SimpleFile simpleFile = (SimpleFile) PsiManager.getInstance(project).findFile(file);
            ...
