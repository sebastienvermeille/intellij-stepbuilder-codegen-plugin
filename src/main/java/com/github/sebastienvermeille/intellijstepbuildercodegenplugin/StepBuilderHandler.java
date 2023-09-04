/**
 * The MIT License Copyright © 2022 Sebastien Vermeille
 *
 * <p>Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * <p>The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * <p>THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
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

    return StepBuilderUtils.getTopLevelClass(project, file, editor) != null
        && isApplicable(file, editor);
  }

  @Override
  public boolean startInWriteAction() {
    return false;
  }

  @Override
  public void invoke(
      @NotNull final Project project, @NotNull final Editor editor, @NotNull final PsiFile file) {
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
      } else {
        final List<PsiFieldMember> optionalFields = new ArrayList<>(existingFields);
        optionalFields.removeAll(selectedFields);

        StepBuilderGenerator.generate(project, editor, file, selectedFields, optionalFields);
      }
    }
  }
}
