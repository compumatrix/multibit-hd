package org.multibit.hd.ui.views.wizards.exchange_settings;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import net.miginfocom.swing.MigLayout;
import org.joda.money.CurrencyUnit;
import org.multibit.hd.core.config.BitcoinConfiguration;
import org.multibit.hd.core.config.Configuration;
import org.multibit.hd.core.config.Configurations;
import org.multibit.hd.core.config.LanguageConfiguration;
import org.multibit.hd.core.exceptions.ExceptionHandler;
import org.multibit.hd.core.exchanges.ExchangeKey;
import org.multibit.hd.ui.audio.Sounds;
import org.multibit.hd.ui.events.view.ViewEvents;
import org.multibit.hd.ui.languages.MessageKey;
import org.multibit.hd.ui.views.components.*;
import org.multibit.hd.ui.views.components.panels.PanelDecorator;
import org.multibit.hd.ui.views.fonts.AwesomeIcon;
import org.multibit.hd.ui.views.wizards.AbstractWizard;
import org.multibit.hd.ui.views.wizards.AbstractWizardPanelView;
import org.multibit.hd.ui.views.wizards.WizardButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.util.Locale;

/**
 * <p>View to provide the following to UI:</p>
 * <ul>
 * <li>Settings: Exchange rate provider display</li>
 * </ul>
 *
 * @since 0.0.1
 *  
 */
public class ExchangeSettingsPanelView extends AbstractWizardPanelView<ExchangeSettingsWizardModel, ExchangeSettingsPanelModel> implements ActionListener {

  private JComboBox<String> exchangeRateProviderComboBox;
  private JButton exchangeRateProviderBrowserButton;

  private JLabel exchangeErrorStatus;
  private JComboBox<String> currencyCodeComboBox;

  private JLabel apiKeyLabel;
  private JTextField apiKey;
  private JLabel apiKeyErrorStatus;

  /**
   * @param wizard    The wizard managing the states
   * @param panelName The panel name
   */
  public ExchangeSettingsPanelView(AbstractWizard<ExchangeSettingsWizardModel> wizard, String panelName) {

    super(wizard, panelName, MessageKey.SHOW_EXCHANGE_WIZARD, AwesomeIcon.DOLLAR);

  }

  @Override
  public void newPanelModel() {

    // Use a deep copy to avoid reference leaks
    Configuration configuration = Configurations.currentConfiguration.deepCopy();

    // Configure the panel model
    setPanelModel(new ExchangeSettingsPanelModel(
      getPanelName(),
      configuration
    ));

  }

  @Override
  public void initialiseContent(JPanel contentPanel) {

    contentPanel.setLayout(new MigLayout(
      Panels.migXYLayout(),
      "[][][]", // Column constraints
      "[][][]" // Row constraints
    ));

    LanguageConfiguration languageConfiguration = Configurations.currentConfiguration.getLanguageConfiguration().deepCopy();
    BitcoinConfiguration bitcoinConfiguration = Configurations.currentConfiguration.getBitcoinConfiguration().deepCopy();
    Locale locale = languageConfiguration.getLocale();
    ExchangeKey exchangeKey = ExchangeKey.valueOf(bitcoinConfiguration.getExchangeKey());

    Preconditions.checkNotNull(locale, "'locale' cannot be empty");

    exchangeRateProviderBrowserButton = Buttons.newLaunchBrowserButton(getExchangeRateProviderBrowserAction());

    exchangeRateProviderComboBox = ComboBoxes.newExchangeRateProviderComboBox(this, bitcoinConfiguration);
    currencyCodeComboBox = ComboBoxes.newCurrencyCodeComboBox(this, bitcoinConfiguration);

    exchangeErrorStatus = Labels.newErrorStatus(false);
    exchangeErrorStatus.setVisible(false);

    // API key
    apiKey = TextBoxes.newEnterApiKey();
    apiKeyErrorStatus = Labels.newErrorStatus(false);
    apiKeyLabel = Labels.newApiKeyLabel();

    // API key visibility
    boolean apiKeyVisible =ExchangeKey.OPEN_EXCHANGE_RATES.equals(exchangeKey);
    apiKey.setVisible(apiKeyVisible);
    apiKeyErrorStatus.setVisible(apiKeyVisible);
    apiKeyLabel.setVisible(apiKeyVisible);

    // API key value
    if (bitcoinConfiguration.getExchangeApiKeys().isPresent()) {
      apiKey.setText(bitcoinConfiguration.getExchangeApiKeys().get());
    }

    contentPanel.add(Labels.newExchangeSettingsNote(), "growx,push,span 3,wrap");

    contentPanel.add(Labels.newSelectExchangeRateProviderLabel(), "shrink");
    contentPanel.add(exchangeRateProviderComboBox, "growx,push");
    contentPanel.add(exchangeRateProviderBrowserButton, "shrink,wrap");

    contentPanel.add(Labels.newLocalCurrencyLabel(), "shrink");
    contentPanel.add(currencyCodeComboBox, "growx,push");
    contentPanel.add(exchangeErrorStatus, "grow,push,wrap");

    contentPanel.add(apiKeyLabel, "shrink");
    contentPanel.add(apiKey, "growx,push");
    contentPanel.add(apiKeyErrorStatus, "grow,push,wrap");

  }

  @Override
  protected void initialiseButtons(AbstractWizard<ExchangeSettingsWizardModel> wizard) {

    PanelDecorator.addCancelApply(this, wizard);

  }

  @Override
  public void fireInitialStateViewEvents() {

    // Apply button starts off enabled
    ViewEvents.fireWizardButtonEnabledEvent(getPanelName(), WizardButton.APPLY, true);

  }

  @Override
  public void afterShow() {

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {

        exchangeRateProviderComboBox.requestFocusInWindow();

      }
    });

  }

  @Override
  public boolean beforeHide(boolean isExitCancel) {

    if (!isExitCancel) {

      // If the user has selected OER then update the API key
      if (apiKey.isVisible() && !Strings.isNullOrEmpty(apiKey.getText())) {
        getWizardModel().getConfiguration().getBitcoinConfiguration().setExchangeApiKeys(apiKey.getText());
      }

      // Switch the main configuration over to the new one
      Configurations.switchConfiguration(getWizardModel().getConfiguration());

    }

    // Must be OK to proceed
    return true;

  }

  @Override
  public void updateFromComponentModels(Optional componentModel) {


  }


  /**
   * <p>Handle one of the combo boxes changing</p>
   *
   * @param e The action event
   */
  @Override
  public void actionPerformed(ActionEvent e) {

    if (ComboBoxes.EXCHANGE_RATE_PROVIDER_COMMAND.equals(e.getActionCommand())) {
      handleExchangeRateProviderSelection(e);
    }
    if (ComboBoxes.CURRENCY_COMMAND.equals(e.getActionCommand())) {
      handleCurrencySelection(e);
    }

  }

  /**
   * @return The "exchange rate provider browser" action
   */
  private Action getExchangeRateProviderBrowserAction() {

    return new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {

        int selectedIndex = exchangeRateProviderComboBox.getSelectedIndex();
        if (selectedIndex == -1) {
          return;
        }

        ExchangeKey exchangeKey = ExchangeKey.values()[selectedIndex];

        try {
          URI exchangeUri = URI.create("http://"+exchangeKey.getExchange().getExchangeSpecification().getHost());
          Desktop.getDesktop().browse(exchangeUri);
        } catch (IOException ex) {
          ExceptionHandler.handleThrowable(ex);
        }

      }
    };
  }

  /**
   * <p>The exchange rate provider selection has changed</p>
   *
   * @param e The action event
   */
  private void handleExchangeRateProviderSelection(ActionEvent e) {

    JComboBox source = (JComboBox) e.getSource();
    int exchangeIndex = source.getSelectedIndex();

    // Exchanges are presented in the same order as they are declared in the enum
    ExchangeKey exchangeKey = ExchangeKey.values()[exchangeIndex];

    // Test for Open Exchange Rates
    if (ExchangeKey.OPEN_EXCHANGE_RATES.equals(exchangeKey)) {
      apiKeyLabel.setVisible(true);
      apiKey.setVisible(true);
    } else {
      apiKeyLabel.setVisible(false);
      apiKey.setVisible(false);
    }

    exchangeErrorStatus.setVisible(false);

    // Update the model (even if in error)
    getWizardModel().getConfiguration().getBitcoinConfiguration().setExchangeKey(exchangeKey.name());

    // Reset the available currencies
    String[] allCurrencies = exchangeKey.allCurrencies();
    currencyCodeComboBox.setModel(new DefaultComboBoxModel<>(allCurrencies));
    currencyCodeComboBox.setSelectedIndex(-1);

    // Prevent application until the currency is selected (to allow ticker check)
    ViewEvents.fireWizardButtonEnabledEvent(
      getPanelName(),
      WizardButton.APPLY,
      false
    );

  }

  /**
   * <p>The currency selection has changed</p>
   *
   * @param e The action event
   */
  private void handleCurrencySelection(ActionEvent e) {

    JComboBox source = (JComboBox) e.getSource();

    // Ignore cascading events from an exchange selection
    if (source.getSelectedIndex() == -1) {
      return;
    }

    String isoCounterCode = String.valueOf(source.getSelectedItem()).substring(0,3);

    // Get the current exchange key
    ExchangeKey exchangeKey = ExchangeKey.valueOf(getWizardModel().getConfiguration().getBitcoinConfiguration().getExchangeKey());

    // Get the polling ticker
    boolean isTickerValid = true;

    // Apply any exchange quirks to the counter code (e.g. ISO "RUB" -> legacy "RUR")
    String exchangeCounterCode = ExchangeKey.exchangeCode(isoCounterCode, exchangeKey);

    try {
      if (ExchangeKey.OPEN_EXCHANGE_RATES.equals(exchangeKey)) {
        exchangeKey.getExchange().getPollingMarketDataService().getTicker(exchangeCounterCode,"USD");
      } else {
        exchangeKey.getExchange().getPollingMarketDataService().getTicker("BTC", exchangeCounterCode);
      }
    } catch (IOException e1) {
      ExceptionHandler.handleThrowable(e1);
      isTickerValid = false;
    }

    if (!isTickerValid) {
      Sounds.playBeep();
      exchangeErrorStatus.setVisible(true);
      ViewEvents.fireWizardButtonEnabledEvent(
        getPanelName(),
        WizardButton.APPLY,
        false
      );
    } else {
      exchangeErrorStatus.setVisible(false);
      ViewEvents.fireWizardButtonEnabledEvent(
        getPanelName(),
        WizardButton.APPLY,
        true
      );
    }

    CurrencyUnit currencyUnit = CurrencyUnit.getInstance(isoCounterCode);

    // Update the model (even if in error)
    getWizardModel().getConfiguration().getBitcoinConfiguration().setLocalCurrencySymbol(isoCounterCode);
    getWizardModel().getConfiguration().getBitcoinConfiguration().setLocalCurrencyUnit(currencyUnit);

  }

}