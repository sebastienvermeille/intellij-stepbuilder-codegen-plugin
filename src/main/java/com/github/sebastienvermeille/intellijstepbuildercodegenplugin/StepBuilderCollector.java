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

import static com.github.sebastienvermeille.intellijstepbuildercodegenplugin.StepBuilderUtils.hasLowerCaseChar;
import static com.intellij.psi.PsiModifier.*;
import static com.intellij.psi.PsiSubstitutor.EMPTY;

import com.intellij.codeInsight.generation.PsiFieldMember;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.TypeConversionUtil;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;

public final class StepBuilderCollector {
  private StepBuilderCollector() {}

  @Nullable
  public static List<PsiFieldMember> collectFields(final PsiFile file, final Editor editor) {
    final int offset = editor.getCaretModel().getOffset();
    final PsiElement element = file.findElementAt(offset);
    if (element == null) {
      return null;
    }

    final PsiClass clazz = PsiTreeUtil.getParentOfType(element, PsiClass.class);
    if (clazz == null || clazz.hasModifierProperty(ABSTRACT)) {
      return null;
    }

    final List<PsiFieldMember> allFields = new ArrayList<PsiFieldMember>();

    PsiClass classToExtractFieldsFrom = clazz;
    while (classToExtractFieldsFrom != null) {
      if (classToExtractFieldsFrom.hasModifierProperty(STATIC)) {
        break;
      }

      final List<PsiFieldMember> classFieldMembers =
          collectFieldsInClass(element, clazz, classToExtractFieldsFrom);
      allFields.addAll(0, classFieldMembers);

      classToExtractFieldsFrom = classToExtractFieldsFrom.getSuperClass();
    }

    return allFields;
  }

  private static List<PsiFieldMember> collectFieldsInClass(
      final PsiElement element, final PsiClass accessObjectClass, final PsiClass clazz) {
    final List<PsiFieldMember> classFieldMembers = new ArrayList<>();
    final PsiResolveHelper helper =
        JavaPsiFacade.getInstance(clazz.getProject()).getResolveHelper();

    for (final PsiField field : clazz.getFields()) {

      // check access to the field from the builder container class (eg. private superclass fields)
      if (helper.isAccessible(field, accessObjectClass, clazz)
          && !PsiTreeUtil.isAncestor(field, element, false)) {

        // skip static fields
        if (field.hasModifierProperty(STATIC)) {
          continue;
        }

        // skip any uppercase fields
        if (!hasLowerCaseChar(field.getName())) {
          continue;
        }

        // skip eventual logging fields
        if (isLoggingField(field)) {
          continue;
        }

        if (field.hasModifierProperty(FINAL)) {
          if (field.getInitializer() != null) {
            continue; // skip final fields that are assigned in the declaration
          }

          if (!accessObjectClass.isEquivalentTo(clazz)) {
            continue; // skip final superclass fields
          }
        }

        final PsiClass containingClass = field.getContainingClass();
        if (containingClass != null) {
          classFieldMembers.add(buildFieldMember(field, containingClass, clazz));
        }
      }
    }

    return classFieldMembers;
  }

  private static boolean isLoggingField(PsiField field) {

    final String fieldType = field.getType().getCanonicalText();

    return "org.apache.log4j.Logger".equals(fieldType)
        || "org.apache.logging.log4j.Logger".equals(fieldType)
        || "java.util.logging.Logger".equals(fieldType)
        || "org.slf4j.Logger".equals(fieldType)
        || "ch.qos.logback.classic.Logger".equals(fieldType)
        || "net.sf.microlog.core.Logger".equals(fieldType)
        || "org.apache.commons.logging.Log".equals(fieldType)
        || "org.pmw.tinylog.Logger".equals(fieldType)
        || "org.jboss.logging.Logger".equals(fieldType)
        || "jodd.log.Logger".equals(fieldType);
  }

  private static PsiFieldMember buildFieldMember(
      final PsiField field, final PsiClass containingClass, final PsiClass clazz) {
    return new PsiFieldMember(
        field, TypeConversionUtil.getSuperClassSubstitutor(containingClass, clazz, EMPTY));
  }
}
