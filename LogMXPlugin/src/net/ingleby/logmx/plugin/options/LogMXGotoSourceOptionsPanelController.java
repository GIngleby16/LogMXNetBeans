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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

@OptionsPanelController.SubRegistration(
        displayName = "#AdvancedOption_DisplayName_LogMXGotoSource",
        keywords = "#AdvancedOption_Keywords_LogMXGotoSource",
        keywordsCategory = "Advanced/LogMXGotoSource"
)
@org.openide.util.NbBundle.Messages({"AdvancedOption_DisplayName_LogMXGotoSource=LogMX", "AdvancedOption_Keywords_LogMXGotoSource=LogMX Goto Source Logging"})
public final class LogMXGotoSourceOptionsPanelController extends OptionsPanelController {

    private LogMXGotoSourcePanel panel;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private boolean changed;

    public void update() {
        getPanel().load();
        changed = false;
    }

    public void applyChanges() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                getPanel().store();
                changed = false;
            }
        });
    }

    public void cancel() {
        // need not do anything special, if no changes have been persisted yet
    }

    public boolean isValid() {
        return getPanel().valid();
    }

    public boolean isChanged() {
        return changed;
    }

    public HelpCtx getHelpCtx() {
        return null; // new HelpCtx("...ID") if you have a help set
    }

    public JComponent getComponent(Lookup masterLookup) {
        return getPanel();
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    private LogMXGotoSourcePanel getPanel() {
        if (panel == null) {
            panel = new LogMXGotoSourcePanel(this);
        }
        return panel;
    }

    void changed() {
        if (!changed) {
            changed = true;
            pcs.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
        }
        //pcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
    }

    void resetchanged() {
        if (changed) {
            changed = false;
            pcs.firePropertyChange(OptionsPanelController.PROP_CHANGED, true, false);
        }
        //pcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
    }
    
    void performValidation() {
        pcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
    }
}
