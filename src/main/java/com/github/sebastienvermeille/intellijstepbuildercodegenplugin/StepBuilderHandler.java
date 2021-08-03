package com.github.sebastienvermeille.intellijstepbuildercodegenplugin;


import static com.github.sebastienvermeille.intellijstepbuildercodegenplugin.StepBuilderCollector.collectFields;
import static com.github.sebastienvermeille.intellijstepbuildercodegenplugin.StepBuilderOptionSelector.selectFieldsAndOptions;

import com.intellij.codeInsight.generation.PsiFieldMember;
import com.intellij.lang.LanguageCodeInsightActionHandler;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.sun.istack.NotNull;
import java.util.ArrayList;
import java.util.List;

public class StepBuilderHandler implements LanguageCodeInsightActionHandler {

    private static boolean isApplicable(final PsiFile file, final Editor editor) {
        final List<PsiFieldMember> targetElements = collectFields(file, editor);
        return targetElements != null && !targetElements.isEmpty();
    }

    @Override
    public boolean isValidFor(final Editor editor, final PsiFile file) {
        if (!(file instanceof PsiJavaFile)) {
            return false;
        }

        final Project project = editor.getProject();
        if (project == null) {
            return false;
        }

        return StepBuilderUtils.getTopLevelClass(project, file, editor) != null && isApplicable(file, editor);
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

    @Override
    public void invoke(@NotNull final Project project, @NotNull final Editor editor, @NotNull final PsiFile file) {
        final PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        final Document currentDocument = psiDocumentManager.getDocument(file);
        if (currentDocument == null) {
            return;
        }

        psiDocumentManager.commitDocument(currentDocument);

        if (!EditorModificationUtil.checkModificationAllowed(editor)) {
            return;
        }

        if (!FileDocumentManager.getInstance().requestWriting(editor.getDocument(), project)) {
            return;
        }

        final List<PsiFieldMember> existingFields = collectFields(file, editor);
        if (existingFields != null) {
            final List<PsiFieldMember> selectedFields = selectFieldsAndOptions(existingFields, project);

            if (selectedFields == null) {
                return;
            }
            else{
                final List<PsiFieldMember> optionalFields = new ArrayList<PsiFieldMember>(existingFields);
                optionalFields.removeAll(selectedFields);

                StepBuilderGenerator.generate(project, editor, file, selectedFields, optionalFields);
            }
        }
    }

}
