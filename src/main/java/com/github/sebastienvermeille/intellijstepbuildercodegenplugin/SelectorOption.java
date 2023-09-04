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

public class SelectorOption {
  private final StepBuilderOption option;
  private final String caption;
  private final char mnemonic;
  private final String toolTip; //optional

  private SelectorOption(final Builder builder) {
    option = builder.option;
    caption = builder.caption;
    mnemonic = builder.mnemonic;
    toolTip = builder.toolTip;
  }

  public static IOption newBuilder() {
    return new Builder();
  }

  public StepBuilderOption getOption() {
    return option;
  }

  public String getCaption() {
    return caption;
  }

  public char getMnemonic() {
    return mnemonic;
  }

  public String getToolTip() {
    return toolTip;
  }

  interface IOption {
    ICaption withOption(StepBuilderOption option);
  }

  interface ICaption {
    IMnemonic withCaption(String caption);
  }

  interface IMnemonic {
    IBuild withMnemonic(char mnemonic);
  }

  interface IBuild {
    IBuild withTooltip(String tooltip);

    SelectorOption build();
  }

  public static final class Builder implements IOption, ICaption, IMnemonic, IBuild {
    private StepBuilderOption option;
    private String caption;
    private char mnemonic;
    private String toolTip;

    private Builder() {}

    public ICaption withOption(final StepBuilderOption option) {
      this.option = option;
      return this;
    }

    public IMnemonic withCaption(final String caption) {
      this.caption = caption;
      return this;
    }

    public IBuild withMnemonic(final char mnemonic) {
      this.mnemonic = mnemonic;
      return this;
    }

    public IBuild withTooltip(final String toolTip) {
      this.toolTip = toolTip;
      return this;
    }

    public SelectorOption build() {
      return new SelectorOption(this);
    }
  }
}
