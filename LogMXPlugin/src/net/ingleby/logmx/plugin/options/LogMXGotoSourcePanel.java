/*
* MIT License
* 
* Copyright (c) 2016 Graeme Ingleby - graeme@ingleby.net
* 
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
* 
* The above copyright notice and this permission notice shall be included in all
* copies or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
* SOFTWARE.
*/
package net.ingleby.logmx.plugin.options;

import com.google.common.net.InetAddresses;
import com.google.common.net.InternetDomainName;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import net.ingleby.logmx.plugin.Installer;
import org.netbeans.validation.api.Problem;
import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Severity;
import org.netbeans.validation.api.Validator;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.swing.SwingValidationGroup;
import org.openide.awt.HtmlBrowser;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

final public class LogMXGotoSourcePanel extends javax.swing.JPanel {
    private final LogMXGotoSourceOptionsPanelController controller;
    private final ValidationGroup group;

    LogMXGotoSourcePanel(LogMXGotoSourceOptionsPanelController controller) {
        this.controller = controller;
        initComponents();

        /**
         * Custom validators to check the hostname/ip and port
         */
        Validator<String> addressValidator = new Validator<String>() {
            @Override
            public void validate(Problems prblms, String fieldName, String hostname) {
                boolean result = InternetDomainName.isValid(hostname) || InetAddresses.isInetAddress(hostname);
                if (!result) {
                    prblms.add(new Problem(fieldName + " contains an invalid hostname or IP address", Severity.FATAL));
                }
            }

            @Override
            public Class<String> modelType() {
                return String.class;
            }
        };
        Validator<String> portValidator = new Validator<String>() {
            @Override
            public void validate(Problems prblms, String fieldName, String hostname) {
                try {
                    Integer.parseInt(portTextField.getText());
                } catch (NumberFormatException e) {
                    prblms.add(new Problem(fieldName + " contains an invalid port number", Severity.FATAL));
                }
            }

            @Override
            public Class<String> modelType() {
                return String.class;
            }
        };
        
        /**
         * Configure validation group
         */
        SwingValidationGroup.setComponentName(addressTextField, "Listen on address");
        SwingValidationGroup.setComponentName(portTextField, "Listen on port");
        group = validationPanel.getValidationGroup();
        group.add(addressTextField, addressValidator);
        group.add(portTextField, portValidator);


        /**
         * Imitate hyper-link functionality
         */
        urlLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    HtmlBrowser.URLDisplayer.getDefault().showURL(new URL("http://www.logmx.com/ide-plugins#netbeans"));
                } catch (MalformedURLException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });

        /**
         * Document Listener that triggers a controller change
         */
        DocumentListener documentChangedListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                LogMXGotoSourcePanel.this.controller.changed();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                LogMXGotoSourcePanel.this.controller.changed();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                LogMXGotoSourcePanel.this.controller.changed();
            }
        };
        // Bind to our text fields
        addressTextField.getDocument().addDocumentListener(documentChangedListener);
        portTextField.getDocument().addDocumentListener(documentChangedListener);
        
        /**
         * CheckBox Change Listener that triggers a controller change
         */
        pluginEnabledCheckBox.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                LogMXGotoSourcePanel.this.controller.changed();
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        addressTextField = new javax.swing.JTextField();
        portTextField = new javax.swing.JTextField();
        pluginEnabledCheckBox = new javax.swing.JCheckBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        jLabel2 = new javax.swing.JLabel();
        urlLabel = new javax.swing.JLabel();
        validationPanel = new org.netbeans.validation.api.ui.swing.ValidationPanel();

        jLabel1.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/net/ingleby/logmx/plugin/options/logmx_logo.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(LogMXGotoSourcePanel.class, "LogMXGotoSourcePanel.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(LogMXGotoSourcePanel.class, "LogMXGotoSourcePanel.jLabel3.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(LogMXGotoSourcePanel.class, "LogMXGotoSourcePanel.jLabel4.text")); // NOI18N

        addressTextField.setText(org.openide.util.NbBundle.getMessage(LogMXGotoSourcePanel.class, "LogMXGotoSourcePanel.addressTextField.text")); // NOI18N

        portTextField.setText(org.openide.util.NbBundle.getMessage(LogMXGotoSourcePanel.class, "LogMXGotoSourcePanel.portTextField.text")); // NOI18N

        pluginEnabledCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(pluginEnabledCheckBox, org.openide.util.NbBundle.getMessage(LogMXGotoSourcePanel.class, "LogMXGotoSourcePanel.pluginEnabledCheckBox.text")); // NOI18N

        jScrollPane1.setBorder(null);

        jTextPane1.setBackground(java.awt.SystemColor.window);
        jTextPane1.setBorder(null);
        jTextPane1.setText(org.openide.util.NbBundle.getMessage(LogMXGotoSourcePanel.class, "LogMXGotoSourcePanel.jTextPane1.text")); // NOI18N
        jScrollPane1.setViewportView(jTextPane1);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(LogMXGotoSourcePanel.class, "LogMXGotoSourcePanel.jLabel2.text")); // NOI18N

        urlLabel.setForeground(java.awt.SystemColor.controlHighlight);
        org.openide.awt.Mnemonics.setLocalizedText(urlLabel, org.openide.util.NbBundle.getMessage(LogMXGotoSourcePanel.class, "LogMXGotoSourcePanel.urlLabel.text")); // NOI18N
        urlLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel2)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(addressTextField)
                            .addComponent(portTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 166, Short.MAX_VALUE)
                .addComponent(pluginEnabledCheckBox))
            .addGroup(layout.createSequentialGroup()
                .addGap(47, 47, 47)
                .addComponent(urlLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addComponent(validationPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pluginEnabledCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(addressTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(portTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2)
                .addGap(3, 3, 3)
                .addComponent(urlLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 26, Short.MAX_VALUE)
                .addComponent(validationPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );
    }// </editor-fold>//GEN-END:initComponents

    void load() {
        /**
         * Populate current preference values
         */
        pluginEnabledCheckBox.setSelected(NbPreferences.forModule(LogMXGotoSourcePanel.class).getBoolean("enabled", true));
        addressTextField.setText(NbPreferences.forModule(LogMXGotoSourcePanel.class).get("listenAddress", "127.0.0.1"));
        try {
            portTextField.setText(Integer.toString(NbPreferences.forModule(LogMXGotoSourcePanel.class).getInt("listenPort", 7789)));
        } catch (NumberFormatException e) {
            //Not an integer
        }
    }

    void store() {
        // record old preference values..
        String oldAddress = NbPreferences.forModule(LogMXGotoSourcePanel.class).get("listenAddress", "127.0.0.1");
        String oldPort = NbPreferences.forModule(LogMXGotoSourcePanel.class).get("listenPort", "7789");
        boolean oldEnabled = NbPreferences.forModule(LogMXGotoSourcePanel.class).getBoolean("enabled", true);

        // Determine what new preference values will be...
        String newAddress = addressTextField.getText();
        String newPort = portTextField.getText();
        boolean newEnabled = pluginEnabledCheckBox.isSelected();

        // We only need to store preferences if something has actually changed!
        if (!oldAddress.equals(newAddress) || !oldPort.equals(newPort) || oldEnabled != newEnabled) {
            // Save Preferences
            NbPreferences.forModule(LogMXGotoSourcePanel.class).putBoolean("enabled", newEnabled);
            NbPreferences.forModule(LogMXGotoSourcePanel.class).put("listenAddress", newAddress);
            try {
                NbPreferences.forModule(LogMXGotoSourcePanel.class).putInt("listenPort", Integer.parseInt(newPort));
            } catch (NumberFormatException e) {
                //Not an integer
            }
            
            /**
             * Stop or start goto-source socket listener as needed
             */
            if (!pluginEnabledCheckBox.isSelected()) {
                Installer.stopGotoSource();
            } else {
                Installer.startGotoSource();
            }
        }
    }

    boolean valid() {
        /**
         * Validate all form fields
         */
        Problem problem = group.performValidation();
        return !(problem != null && problem.isFatal());
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField addressTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextPane jTextPane1;
    private javax.swing.JCheckBox pluginEnabledCheckBox;
    private javax.swing.JTextField portTextField;
    private javax.swing.JLabel urlLabel;
    private org.netbeans.validation.api.ui.swing.ValidationPanel validationPanel;
    // End of variables declaration//GEN-END:variables

}
