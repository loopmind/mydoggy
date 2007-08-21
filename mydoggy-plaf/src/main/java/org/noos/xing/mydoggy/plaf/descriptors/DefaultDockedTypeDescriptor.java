package org.noos.xing.mydoggy.plaf.descriptors;

import org.noos.xing.mydoggy.DockedTypeDescriptor;
import org.noos.xing.mydoggy.ToolWindowActionHandler;
import org.noos.xing.mydoggy.ToolWindowTypeDescriptor;
import org.noos.xing.mydoggy.plaf.ui.ResourceManager;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * @author Angelo De Caro (angelo.decaro@gmail.com)
 */
public class DefaultDockedTypeDescriptor implements DockedTypeDescriptor, PropertyChangeListener, InternalTypeDescriptor {
    private ResourceManager resourceManager;

    private ToolWindowActionHandler toolWindowActionHandler;
    private boolean popupMenuEnabled;
    private JMenu toolsMenu;
    private int dockLength;
    private boolean animating;

    private boolean previewEnabled;
    private int previewDelay;
    private float previewTransparentRatio;
    private boolean hideRepresentativeButtonOnVisible;
    private boolean idVisibleOnTitleBar;

    private EventListenerList listenerList;

    public DefaultDockedTypeDescriptor(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
        this.toolsMenu = new JMenu(resourceManager.getString("@@tool.toolsMenu"));
        this.popupMenuEnabled = true;
        this.dockLength = 200;
        this.toolWindowActionHandler = null;
        this.animating = true;
        this.previewEnabled = true;
        this.previewDelay = 1000;
        this.previewTransparentRatio = 0.65f;
        this.hideRepresentativeButtonOnVisible = false;
        this.idVisibleOnTitleBar = true;
    }

    public DefaultDockedTypeDescriptor(DefaultDockedTypeDescriptor parent, ResourceManager resourceManager,
                                       int dockLength, boolean popupMenuEnabled,
                                       ToolWindowActionHandler toolWindowActionHandler, boolean animating,
                                       boolean previewEnabled, int previewDelay, float previewTransparentRatio,
                                       boolean hideRepresentativeButtonOnVisible, boolean idVisibleOnTitleBar) {
        this.resourceManager = resourceManager;
        this.toolsMenu = new JMenu(resourceManager.getString("@@tool.toolsMenu"));
        this.popupMenuEnabled = popupMenuEnabled;
        this.dockLength = dockLength;
        this.toolWindowActionHandler = toolWindowActionHandler;
        this.animating = animating;
        this.previewEnabled = previewEnabled;
        this.previewDelay = previewDelay;
        this.previewTransparentRatio = previewTransparentRatio;
        this.hideRepresentativeButtonOnVisible = hideRepresentativeButtonOnVisible;
        this.idVisibleOnTitleBar = idVisibleOnTitleBar;

        this.listenerList = new EventListenerList();

        parent.addPropertyChangeListener(this);
    }

    public void setPopupMenuEnabled(boolean enabled) {
        boolean old = this.popupMenuEnabled;
        this.popupMenuEnabled = enabled;

        firePropertyChange("popupMenuEnabled", old, enabled);
    }

    public boolean isPopupMenuEnabled() {
        return popupMenuEnabled;
    }

    public JMenu getToolsMenu() {
        return toolsMenu;
    }

    public int getDockLength() {
        return dockLength;
    }

    public void setDockLength(int dockLength) {
        int old = this.dockLength;
        this.dockLength = dockLength;

        firePropertyChange("dockLength", old, dockLength);
    }

    public ToolWindowActionHandler getToolWindowActionHandler() {
        return toolWindowActionHandler;
    }

    public void setToolWindowActionHandler(ToolWindowActionHandler toolWindowActionHandler) {
        ToolWindowActionHandler old = this.toolWindowActionHandler;
        this.toolWindowActionHandler = toolWindowActionHandler;

        firePropertyChange("toolWindowActionHandler", old, toolWindowActionHandler);
    }

    public boolean isPreviewEnabled() {
        return previewEnabled;
    }

    public void setPreviewEnabled(boolean previewEnabled) {
        if (this.previewEnabled == previewEnabled)
            return;

        boolean old = this.previewEnabled;
        this.previewEnabled = previewEnabled;
        firePropertyChange("previewEnabled", old, previewEnabled);
    }

    public int getPreviewDelay() {
        return previewDelay;
    }

    public void setPreviewDelay(int previewDelay) {
        if (this.previewDelay == previewDelay)
            return;

        int old = this.previewDelay;
        this.previewDelay = previewDelay;
        firePropertyChange("previewDelay", old, previewDelay);
    }

    public float getPreviewTransparentRatio() {
        return previewTransparentRatio;
    }

    public void setHideRepresentativeButtonOnVisible(boolean hideRepresentativeButtonOnVisible) {
        if (this.hideRepresentativeButtonOnVisible == hideRepresentativeButtonOnVisible)
            return;

        boolean old = this.hideRepresentativeButtonOnVisible;
        this.hideRepresentativeButtonOnVisible = hideRepresentativeButtonOnVisible;
        firePropertyChange("hideRepresentativeButtonOnVisible", old, hideRepresentativeButtonOnVisible);
    }

    public boolean isHideRepresentativeButtonOnVisible() {
        return hideRepresentativeButtonOnVisible;
    }

    public void setIdVisibleOnTitleBar(boolean idVisibleOnTitleBar) {
        if (this.idVisibleOnTitleBar == idVisibleOnTitleBar)
            return;

        boolean old = this.idVisibleOnTitleBar;
        this.idVisibleOnTitleBar = idVisibleOnTitleBar;
        firePropertyChange("idVisibleOnTitleBar", old, idVisibleOnTitleBar);
    }

    public boolean isIdVisibleOnTitleBar() {
        return idVisibleOnTitleBar;
    }

    public void setPreviewTransparentRatio(float previewTransparentRatio) {
        if (this.previewTransparentRatio == previewTransparentRatio)
            return;

        float old = this.previewTransparentRatio;
        this.previewTransparentRatio = previewTransparentRatio;
        firePropertyChange("previewTransparentRatio", old, previewTransparentRatio);
    }

    public boolean isAnimating() {
        return animating;
    }

    public void setAnimating(boolean animating) {
        if (this.animating == animating)
            return;
        
        boolean old = this.animating;
        this.animating = animating;
        firePropertyChange("animating", old, animating);
    }

    public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        if (listenerList == null)
            listenerList = new EventListenerList();
        listenerList.add(PropertyChangeListener.class, propertyChangeListener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        if (listenerList == null)
            return;
        listenerList.remove(PropertyChangeListener.class, listener);
    }

    public PropertyChangeListener[] getPropertyChangeListeners() {
        if (listenerList == null)
            return new PropertyChangeListener[0];
        return listenerList.getListeners(PropertyChangeListener.class);
    }

    public ToolWindowTypeDescriptor cloneMe() {
        return new DefaultDockedTypeDescriptor(this, resourceManager, dockLength, popupMenuEnabled,
                                               toolWindowActionHandler, animating,
                                               previewEnabled, previewDelay, previewTransparentRatio,
                                               hideRepresentativeButtonOnVisible, idVisibleOnTitleBar);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if ("popupMenuEnabled".equals(evt.getPropertyName())) {
            this.popupMenuEnabled = (Boolean) evt.getNewValue();
        } else if ("dockLength".equals(evt.getPropertyName())) {
            this.dockLength = (Integer) evt.getNewValue();
        } else if ("animating".equals(evt.getPropertyName())) {
            this.animating = (Boolean) evt.getNewValue();
        } else if ("toolWindowActionHandler".equals(evt.getPropertyName())) {
            this.toolWindowActionHandler = (ToolWindowActionHandler) evt.getNewValue();
        } else if ("previewEnabled".equals(evt.getPropertyName())) {
            this.previewEnabled = (Boolean) evt.getNewValue();
        } else if ("previewDelay".equals(evt.getPropertyName())) {
            this.previewDelay = (Integer) evt.getNewValue();
        } else if ("previewTransparentRatio".equals(evt.getPropertyName())) {
            this.previewTransparentRatio = (Float) evt.getNewValue();
        } else if ("hideRepresentativeButtonOnVisible".equals(evt.getPropertyName())) {
            this.hideRepresentativeButtonOnVisible = (Boolean) evt.getNewValue();
        } else if ("idVisibleOnTitleBar".equals(evt.getPropertyName())) {
            this.idVisibleOnTitleBar = (Boolean) evt.getNewValue();
        } 
    }


    private void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        if (listenerList != null) {
            PropertyChangeListener[] listeners = listenerList.getListeners(PropertyChangeListener.class);
            PropertyChangeEvent event = new PropertyChangeEvent(this, propertyName, oldValue, newValue);
            for (PropertyChangeListener listener : listeners) {
                listener.propertyChange(event);
            }
        }
    }

}
