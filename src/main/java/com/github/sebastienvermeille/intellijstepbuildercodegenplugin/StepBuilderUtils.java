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

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiParameterList;
import com.intellij.psi.PsiPrimitiveType;
import com.intellij.psi.PsiStatement;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.PsiUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class StepBuilderUtils {
  @NonNls static final String JAVA_DOT_LANG = "java.lang.";

  private StepBuilderUtils() {}

  /**
   * Does the string have a lowercase character?
   *
   * @param str the string to test.
   * @return true if the string has a lowercase character, false if not.
   */
  public static boolean hasLowerCaseChar(String str) {
    for (int i = 0; i < str.length(); i++) {
      if (Character.isLowerCase(str.charAt(i))) {
        return true;
      }
    }

    return false;
  }

  public static String capitalize(String str) {
    return Character.toUpperCase(str.charAt(0)) + str.substring(1);
  }

  static String stripJavaLang(String typeString) {
    return typeString.startsWith(JAVA_DOT_LANG)
        ? typeString.substring(JAVA_DOT_LANG.length())
        : typeString;
  }

  static boolean areParameterListsEqual(PsiParameterList paramList1, PsiParameterList paramList2) {
    if (paramList1.getParametersCount() != paramList2.getParametersCount()) {
      return false;
    }

    final PsiParameter[] param1Params = paramList1.getParameters();
    final PsiParameter[] param2Params = paramList2.getParameters();
    for (int i = 0; i < param1Params.length; i++) {
      final PsiParameter param1Param = param1Params[i];
      final PsiParameter param2Param = param2Params[i];

      if (!areTypesPresentableEqual(param1Param.getType(), param2Param.getType())) {
        return false;
      }
    }

    return true;
  }

  static boolean areTypesPresentableEqual(PsiType type1, PsiType type2) {
    if (type1 != null && type2 != null) {
      final String type1Canonical = stripJavaLang(type1.getPresentableText());
      final String type2Canonical = stripJavaLang(type2.getPresentableText());
      return type1Canonical.equals(type2Canonical);
    }

    return false;
  }

  @Nullable
  public static PsiClass getTopLevelClass(Project project, PsiFile file, Editor editor) {
    final int offset = editor.getCaretModel().getOffset();
    final PsiElement element = file.findElementAt(offset);
    if (element == null) {
      return null;
    }

    return PsiUtil.getTopLevelClass(element);
  }

  public static boolean isPrimitive(PsiField psiField) {
    return (psiField.getType() instanceof PsiPrimitiveType);
  }

  static PsiStatement createReturnThis(
      @NotNull PsiElementFactory psiElementFactory, @Nullable PsiElement context) {
    return psiElementFactory.createStatementFromText("return this;", context);
  }
}
