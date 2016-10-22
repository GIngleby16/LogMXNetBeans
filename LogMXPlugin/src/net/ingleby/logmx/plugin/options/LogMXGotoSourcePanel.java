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

import java.awt.Color;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
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
import sun.net.util.IPAddressUtil;

// IPAddressUtil is an internal API - but google guava was using it to check hostname/ip formats.  
// Plugin is considerably smaller without the guava dependency

final public class LogMXGotoSourcePanel extends javax.swing.JPanel {
    private static Pattern HOSTNAME_PATTERN = Pattern.compile("^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9])$");

    private final LogMXGotoSourceOptionsPanelController controller;
    private final ValidationGroup group;

    LogMXGotoSourcePanel(LogMXGotoSourceOptionsPanelController controller) {
        this.controller = controller;
        initComponents();

        /*
        * Correct TextPane Background Color - not sure why I can't set this directly but it gets *changed* when I use the color returned from UIManager!
         */
        Color c = UIManager.getColor("OptionPane.background");
        jTextPane2.setBackground(new Color(c.getRed(), c.getGreen(), c.getBlue()));

        /**
         * Configure Text Pane
         */
        try {
            jTextPane2.setContentType("text/html");
            jTextPane2.setEditable(false);
            jTextPane2.setFocusable(false);
            HTMLDocument doc = (HTMLDocument) jTextPane2.getDocument();
            HTMLEditorKit editorKit = (HTMLEditorKit) jTextPane2.getEditorKit();
            String text = "<html>This plugin “LogMX GotoSource” allows LogMX (log analyzer tool) to communicate with NetBeans in order to open Java source files directly from LogMX: when LogMX detects a Java stack trace in a log entry, it creates a link for each stack trace line so that when this link is clicked, running instance of NetBeans opens the corresponding stack trace Java element at the right line.  Also simply hovering this link with the mouse will show the source directly from LogMX.<br><br>For more information see:<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href=‘http://www.logmx.com/ide-plugins#netbeans’>http://www.logmx.com/ide-plugins#netbeans</a></html>";
            editorKit.insertHTML(doc, doc.getLength(), text, 0, 0, null);
        } catch (BadLocationException | IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        /**
         * Hyper-link functionality
         */
        jTextPane2.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    try {
                        HtmlBrowser.URLDisplayer.getDefault().showURL(new URL("http://www.logmx.com/ide-plugins#netbeans"));
                    } catch (MalformedURLException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }

            }
        });

        /**
         * Custom validators to check the hostname/ip and port
         */
        Validator<String> addressValidator = new Validator<String>() {
            @Override
            public void validate(Problems prblms, String fieldName, String hostname) {
                boolean result = isValidHostname(hostname) || isValidIP(hostname);
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
         * Document Listener that triggers a controller change
         */
        DocumentListener documentChangedListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                //LogMXGotoSourcePanel.this.controller.changed();
                LogMXGotoSourcePanel.this.controller.performValidation();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
//                LogMXGotoSourcePanel.this.controller.changed();
                LogMXGotoSourcePanel.this.controller.performValidation();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
//                LogMXGotoSourcePanel.this.controller.changed();
                LogMXGotoSourcePanel.this.controller.performValidation();
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
//                LogMXGotoSourcePanel.this.controller.changed();
                LogMXGotoSourcePanel.this.controller.performValidation();
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
        validationPanel = new org.netbeans.validation.api.ui.swing.ValidationPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextPane2 = new javax.swing.JTextPane();

        jLabel1.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/net/ingleby/logmx/plugin/options/logmx_logo.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(LogMXGotoSourcePanel.class, "LogMXGotoSourcePanel.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(LogMXGotoSourcePanel.class, "LogMXGotoSourcePanel.jLabel3.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(LogMXGotoSourcePanel.class, "LogMXGotoSourcePanel.jLabel4.text")); // NOI18N

        addressTextField.setText(org.openide.util.NbBundle.getMessage(LogMXGotoSourcePanel.class, "LogMXGotoSourcePanel.addressTextField.text")); // NOI18N

        portTextField.setText(org.openide.util.NbBundle.getMessage(LogMXGotoSourcePanel.class, "LogMXGotoSourcePanel.portTextField.text")); // NOI18N

        pluginEnabledCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(pluginEnabledCheckBox, org.openide.util.NbBundle.getMessage(LogMXGotoSourcePanel.class, "LogMXGotoSourcePanel.pluginEnabledCheckBox.text")); // NOI18N

        validationPanel.setBorder(null);

        jScrollPane2.setBackground(new java.awt.Color(153, 255, 255));
        jScrollPane2.setBorder(null);
        jScrollPane2.setPreferredSize(new java.awt.Dimension(19, 20));

        jTextPane2.setBackground(UIManager.getColor("OptionPane.background"));
        jTextPane2.setBorder(null);
        jTextPane2.setContentType("text/html\n"); // NOI18N
        jTextPane2.setText(org.openide.util.NbBundle.getMessage(LogMXGotoSourcePanel.class, "LogMXGotoSourcePanel.jTextPane2.text")); // NOI18N
        jTextPane2.setPreferredSize(new java.awt.Dimension(10, 10));
        jScrollPane2.setViewportView(jTextPane2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 29, Short.MAX_VALUE)
                .addComponent(pluginEnabledCheckBox))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(portTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
                    .addComponent(addressTextField))
                .addGap(0, 0, Short.MAX_VALUE))
            .addComponent(validationPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
                .addGap(18, 18, 18)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 52, Short.MAX_VALUE)
                .addGap(8, 8, 8)
                .addComponent(validationPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
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
        // Save Preferences
        NbPreferences.forModule(LogMXGotoSourcePanel.class).putBoolean("enabled", pluginEnabledCheckBox.isSelected());
        NbPreferences.forModule(LogMXGotoSourcePanel.class).put("listenAddress", addressTextField.getText());
        try {
            NbPreferences.forModule(LogMXGotoSourcePanel.class).putInt("listenPort", Integer.parseInt(portTextField.getText()));
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

    boolean valid() {
        /**
         * Determine if we have changed state...
         */
        String oldAddress = NbPreferences.forModule(LogMXGotoSourcePanel.class).get("listenAddress", "127.0.0.1");
        String oldPort = NbPreferences.forModule(LogMXGotoSourcePanel.class).get("listenPort", "7789");
        boolean oldEnabled = NbPreferences.forModule(LogMXGotoSourcePanel.class).getBoolean("enabled", true);

        // Determine what new preference values will be...
        String newAddress = addressTextField.getText();
        String newPort = portTextField.getText();
        boolean newEnabled = pluginEnabledCheckBox.isSelected();

        if (!oldAddress.equals(newAddress) || !oldPort.equals(newPort) || oldEnabled != newEnabled) {
            LogMXGotoSourcePanel.this.controller.changed();
        } else {
            LogMXGotoSourcePanel.this.controller.resetchanged();
        }

        /**
         * Validate all form fields
         */
        Problem problem = group.performValidation();
        return !(problem != null && problem.isFatal());
    }

    /**
     * Hostname Validation
     * 
     * @param hostname
     * @return 
     */
    private static boolean isValidHostname(String hostname) {        
        if(hostname.length() > 253) 
            return false;
        String[] parts = hostname.split("\\.");
        for(String p : parts) {
            if(p.length() > 63)
                return false;
            if(!HOSTNAME_PATTERN.matcher(p).find())
                return false;
        }

        if(Character.isDigit(parts[parts.length-1].charAt(0)))
            return false;
        return true;
    }
    
    /**
     * IP Address Validation
     * 
     * @param hostname
     * @return 
     */
    
    private static boolean isValidIP(String hostname) {
        byte[] addr = IPAddressUtil.textToNumericFormatV4(hostname);
        if(addr == null) {
            // try V6
            addr = IPAddressUtil.textToNumericFormatV6(hostname);
        }
        return addr != null;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField addressTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextPane jTextPane2;
    private javax.swing.JCheckBox pluginEnabledCheckBox;
    private javax.swing.JTextField portTextField;
    private org.netbeans.validation.api.ui.swing.ValidationPanel validationPanel;
    // End of variables declaration//GEN-END:variables

}
