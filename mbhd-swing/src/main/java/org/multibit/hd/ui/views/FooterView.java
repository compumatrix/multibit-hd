package org.multibit.hd.ui.views;

import com.google.common.eventbus.Subscribe;
import net.miginfocom.swing.MigLayout;
import org.multibit.hd.core.services.CoreServices;
import org.multibit.hd.ui.events.SystemStatusChangedEvent;
import org.multibit.hd.ui.views.components.Panels;
import org.multibit.hd.ui.views.fonts.AwesomeDecorator;
import org.multibit.hd.ui.views.fonts.AwesomeIcon;
import org.multibit.hd.ui.views.themes.Themes;

import javax.swing.*;

/**
 * <p>View to provide the following to application:</p>
 * <ul>
 * <li>Provision of components and layout for the footer display</li>
 * </ul>
 *
 * @since 0.0.1
 *         
 */
public class FooterView {

  private final JPanel contentPanel;
  private final JProgressBar progressBar;
  private final JLabel messageLabel;
  private final JLabel statusLabel;
  private final JLabel statusIcon;


  public FooterView() {

    CoreServices.uiEventBus.register(this);

    contentPanel = Panels.newPanel(new MigLayout(
      "ins 7",
      "[][][]",
      "[]"
    ));

    // Apply the theme
    contentPanel.setBackground(Themes.currentTheme.headerPanelBackground());

    progressBar = new JProgressBar();
    messageLabel = new JLabel();

    statusLabel = new JLabel("OK");
    statusIcon = AwesomeDecorator.createIconLabel(
      AwesomeIcon.CIRCLE,
      "",
      false
    );
    statusIcon.setForeground(Themes.currentTheme.successText());

    contentPanel.add(progressBar, "shrink,left");
    contentPanel.add(messageLabel, "grow,push");
    contentPanel.add(statusLabel, "split,shrink,right");
    contentPanel.add(statusIcon, "right");

  }

  /**
   * @return The content panel for this View
   */
  public JPanel getContentPanel() {
    return contentPanel;
  }

  /**
   * <p>Handles the representation of a system status change</p>
   *
   * @param event The system status change event
   */
  @Subscribe
  public void onSystemStatusChangeEvent(SystemStatusChangedEvent event) {

    switch (event.getSeverity()) {
      case RED:
        statusLabel.setText("Problem");
        statusIcon.setForeground(Themes.currentTheme.dangerText());
        break;
      case AMBER:
        statusLabel.setText("Warning");
        statusIcon.setForeground(Themes.currentTheme.warningText());
        break;
      case GREEN:
        statusLabel.setText("OK");
        statusIcon.setForeground(Themes.currentTheme.successText());
        break;
      default:
        // Unknown status
        throw new IllegalStateException("Unknown event severity "+event.getSeverity());
    }

  }

}