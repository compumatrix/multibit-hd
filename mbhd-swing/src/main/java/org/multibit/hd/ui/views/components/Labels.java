package org.multibit.hd.ui.views.components;

import org.multibit.hd.ui.i18n.Languages;
import org.multibit.hd.ui.views.fonts.AwesomeDecorator;
import org.multibit.hd.ui.views.fonts.AwesomeIcon;
import org.multibit.hd.ui.views.themes.Themes;

import javax.swing.*;
import java.awt.*;

/**
 * <p>Utility to provide the following to UI:</p>
 * <ul>
 * <li>Provision of localised buttons</li>
 * </ul>
 *
 * @since 0.0.1
 *         
 */
public class Labels {

  private static final float BALANCE_LARGE_FONT_SIZE = 42.0f;
  private static final float BALANCE_NORMAL_FONT_SIZE = 28.0f;

  private static final String USE_LANGUAGE_LABEL = "showPreferencesPanel.useSpecific";
  private static final String HELP_LABEL = "multiBitFrame.helpMenuText";
  private static final String SETTINGS_LABEL = "showPreferencesPanel.title";

  // TODO Require keys
  private static final String SIGN_OUT_LABEL = "Sign Out";

  /**
   * Utilities have no public constructor
   */
  private Labels() {
  }

  /**
   * @return A new "Select language" label
   */
  public static JLabel newLanguageLabel() {

    return new JLabel(Languages.safeText(USE_LANGUAGE_LABEL));
  }

  /**
   * @return A new "Help" label with icon
   */
  public static JLabel newHelpLabel() {

    JLabel label = AwesomeDecorator.createIconLabel(AwesomeIcon.QUESTION, Languages.safeText(HELP_LABEL), true);
    label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    return label;
  }

  /**
   * @return A new "Settings" label with icon
   */
  public static JLabel newSettingsLabel() {

    JLabel label = AwesomeDecorator.createIconLabel(AwesomeIcon.GEAR, Languages.safeText(SETTINGS_LABEL), true);
    label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    return label;
  }

  /**
   * @return A new "Sign Out" label with icon
   */
  public static JLabel newSignOutLabel() {

    JLabel label = AwesomeDecorator.createIconLabel(AwesomeIcon.SIGN_OUT, Languages.safeText(SIGN_OUT_LABEL), true);
    label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    return label;
  }

  /**
   * <p>The balance labels</p>
   * <ul>
   * <li>[0]: Primary value, possibly decorated with leading symbol/code, to 2dp</li>
   * <li>[1]: Secondary value covering remaining decimal places</li>
   * <li>[2]: Placeholder for trailing symbol/code</li>
   * <li>[3]: Localised exchange rate display</li>
   * </ul>
   *
   * @return A new collection of labels that together form a balance display
   */
  public static JLabel[] newBalanceLabels() {

    JLabel primaryBalanceLabel = new JLabel();
    JLabel secondaryBalanceLabel = new JLabel();
    JLabel trailingSymbolLabel = new JLabel();
    JLabel exchangeLabel = new JLabel();

    // Font
    Font balanceFont = primaryBalanceLabel.getFont().deriveFont(BALANCE_LARGE_FONT_SIZE);
    Font decimalFont = primaryBalanceLabel.getFont().deriveFont(BALANCE_NORMAL_FONT_SIZE);

    primaryBalanceLabel.setFont(balanceFont);
    secondaryBalanceLabel.setFont(decimalFont);
    trailingSymbolLabel.setFont(balanceFont);
    exchangeLabel.setFont(decimalFont);

    // Theme
    primaryBalanceLabel.setForeground(Themes.H1.foreground);
    secondaryBalanceLabel.setForeground(Themes.H2.foreground);
    trailingSymbolLabel.setForeground(Themes.H1.foreground);
    exchangeLabel.setForeground(Themes.H1.foreground);

    return new JLabel[]{

      primaryBalanceLabel,
      secondaryBalanceLabel,
      trailingSymbolLabel,
      exchangeLabel
    };

  }
}
