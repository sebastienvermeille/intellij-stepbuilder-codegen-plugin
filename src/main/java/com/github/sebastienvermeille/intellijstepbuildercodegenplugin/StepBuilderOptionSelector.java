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

import static com.github.sebastienvermeille.intellijstepbuildercodegenplugin.StepBuilderOption.*;

import com.intellij.codeInsight.generation.PsiFieldMember;
import com.intellij.ide.util.MemberChooser;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.NonFocusableCheckBox;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import javax.swing.JCheckBox;

public final class StepBuilderOptionSelector {
  private static final List<SelectorOption> OPTIONS = createGeneratorOptions();

  private StepBuilderOptionSelector() {}

  private static List<SelectorOption> createGeneratorOptions() {
    final List<SelectorOption> options = new ArrayList<SelectorOption>(8);

    options.add(
        SelectorOption.newBuilder()
            .withOption(FINAL_SETTERS)
            .withCaption("Generate builder methods for final fields")
            .withMnemonic('f')
            .build());

    options.add(
        SelectorOption.newBuilder()
            .withOption(COPY_CONSTRUCTOR)
            .withCaption("Generate builder copy constructor")
            .withMnemonic('o')
            .build());

    options.add(
        SelectorOption.newBuilder()
            .withOption(WITH_JAVADOC)
            .withCaption("Add Javadoc")
            .withMnemonic('c')
            .withTooltip("Add Javadoc to generated builder class and methods")
            .build());

    options.add(
        SelectorOption.newBuilder()
            .withOption(PUBLIC_INTERFACES)
            .withCaption("Make Interfaces public")
            .withMnemonic('p')
            .withTooltip("Make generated interfaces public")
            .build());
    return options;
  }

  @Nullable
  public static List<PsiFieldMember> selectFieldsAndOptions(
      final List<PsiFieldMember> members, final Project project) {
    if (members == null || members.isEmpty()) {
      return null;
    }

    if (ApplicationManager.getApplication().isUnitTestMode()) {
      return members;
    }

    final JCheckBox[] optionCheckBoxes = buildOptionCheckBoxes();

    final PsiFieldMember[] memberArray = members.toArray(new PsiFieldMember[members.size()]);

    final MemberChooser<PsiFieldMember> chooser =
        new MemberChooser<>(
            memberArray,
            false, // allowEmptySelection
            true, // allowMultiSelection
            project,
            null,
            optionCheckBoxes);

    chooser.setTitle("Select Mandatory Fields and Options for the Builder");
    chooser.selectElements(memberArray);
    if (chooser.showAndGet()) {
      return chooser.getSelectedElements();
    }

    return null;
  }

  private static JCheckBox[] buildOptionCheckBoxes() {
    final PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
    final int optionCount = OPTIONS.size();
    final JCheckBox[] checkBoxesArray = new JCheckBox[optionCount];
    for (int i = 0; i < optionCount; i++) {
      checkBoxesArray[i] = buildOptionCheckBox(propertiesComponent, OPTIONS.get(i));
    }

    return checkBoxesArray;
  }

  private static JCheckBox buildOptionCheckBox(
      final PropertiesComponent propertiesComponent, final SelectorOption selectorOption) {
    final StepBuilderOption option = selectorOption.getOption();

    final JCheckBox optionCheckBox = new NonFocusableCheckBox(selectorOption.getCaption());
    optionCheckBox.setMnemonic(selectorOption.getMnemonic());
    optionCheckBox.setToolTipText(selectorOption.getToolTip());

    final String optionProperty = option.getProperty();
    optionCheckBox.setSelected(propertiesComponent.isTrueValue(optionProperty));
    optionCheckBox.addItemListener(
        event ->
            propertiesComponent.setValue(
                optionProperty, Boolean.toString(optionCheckBox.isSelected())));
    return optionCheckBox;
  }
}
