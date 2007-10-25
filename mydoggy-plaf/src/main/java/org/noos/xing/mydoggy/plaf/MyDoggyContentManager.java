package org.noos.xing.mydoggy.plaf;

import org.noos.xing.mydoggy.*;
import org.noos.xing.mydoggy.event.ContentManagerEvent;
import org.noos.xing.mydoggy.plaf.ui.content.PlafContentManagerUI;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * @author Angelo De Caro (angelo.decaro@gmail.com)
 */
public class MyDoggyContentManager implements ContentManager {
    protected MyDoggyToolWindowManager toolWindowManager;

    protected List<Content> contents;
    protected Map<Object, Content> contentMap;
    protected PlafContentManagerUI plafContentManagerUI;

    protected EventListenerList listeners;


    MyDoggyContentManager(MyDoggyToolWindowManager windowManager) {
        this.toolWindowManager = windowManager;
        this.contents = new ArrayList<Content>();
        this.contentMap = new Hashtable<Object, Content>();
        this.listeners = new EventListenerList();
    }


    public void setContentManagerUI(ContentManagerUI contentManagerUI) {
        if (!(contentManagerUI instanceof PlafContentManagerUI))
            throw new IllegalArgumentException("ContentManagerUI type not supported. See Plaf prescription.");

        if (this.plafContentManagerUI == contentManagerUI)
            return;

        if (this.plafContentManagerUI != null)
            this.plafContentManagerUI.unistall();

        PlafContentManagerUI newContentManagerUI = (PlafContentManagerUI) contentManagerUI;
        this.plafContentManagerUI = newContentManagerUI.install(getContentManagerUI(), toolWindowManager);
    }

    public ContentManagerUI getContentManagerUI() {
        return (ContentManagerUI) plafContentManagerUI;
    }

    public int getContentCount() {
        return contents.size();
    }

    public Content addContent(Object key, String title, Icon icon, Component component, String tip) {
        return addContentInternal(key, title, icon, component, tip, null);
    }

    public Content addContent(Object key, String title, Icon icon, Component component) {
        return addContent(key, title, icon, component, null);
    }

    public Content addContent(ToolWindow toolWindow) {
        toolWindowManager.verifyDockable(toolWindow);


        System.out.println("toolWindow = " + toolWindow.getId());
        ((MyDoggyToolWindow)toolWindow).setTypeInternal(ToolWindowType.EXTERN);
        Content content = addContentInternal(toolWindow.getId(),
                                             toolWindow.getTitle(),
                                             toolWindow.getIcon(),
                                             toolWindow.getComponent(),
                                             null,
                                             toolWindow);
        return content;
    }

    public boolean removeContent(Content content) {
        if (content == null)
            throw new IllegalArgumentException("Content cannot be null");

        plafContentManagerUI.removeContent((MyDoggyContent) content);
        boolean result = contents.remove(content);

        if (result) {
            contentMap.remove(content.getKey());
            fireContentRemoved(content);
        }

        if (content.getDockableDelegator() != null) {
            Dockable delegator = content.getDockableDelegator();
            if  (delegator instanceof ToolWindow) {
                ToolWindow toolWindow = (ToolWindow) delegator;
                toolWindow.setType(ToolWindowType.DOCKED);
            }
        }

        return result;
    }

    public boolean removeContent(int index) {
        Content content = contents.get(index);
        return removeContent(content);
    }

    public void removeAllContents() {
        for (int i = 0, size = getContentCount(); i < size; i++)
            removeContent(i);
    }

    public Content getContent(int index) {
        return contents.get(index);
    }

    public Content getContent(Object key) {
        return contentMap.get(key);
    }

    public Content getContentByComponent(Component component) {
        for (Content content : contents) {
            if (content.getComponent() == component)
                return content;
        }
        throw new IllegalArgumentException("Cannot found content for component. [cmp : " + component + ']');
    }

    public Content getSelectedContent() {
        for (Content content : contents) {
            if (content.isSelected())
                return content;
        }
        return null;
    }

    public Content getNextContent() {
        if (contents.size() == 0)
            return null;

        if (getSelectedContent() == null)
            return contents.get(0);

        int index = contents.indexOf(getSelectedContent()) + 1;
        int startIndex = index;
        do {
            if (index >= contents.size())
                index = 0;
            Content content = getContent(index);
            if (content.isEnabled())
                return content;
            index++;
        } while (index != startIndex);

        return null;
    }

    public Content getPreviousContent() {
        if (contents.size() == 0)
            return null;

        if (getSelectedContent() == null)
            return contents.get(0);

        int index = contents.indexOf(getSelectedContent()) - 1;
        int startIndex = index;
        do {
            if (index < 0)
                index = contents.size() - 1;
            Content content = getContent(index);
            if (content.isEnabled())
                return content;
            index--;
        } while (index != startIndex);

        return null;
    }

    public Content[] getContents() {
        return contents.toArray(new Content[contents.size()]);
    }

    public void setPopupMenu(JPopupMenu popupMenu) {
        plafContentManagerUI.setPopupMenu(popupMenu);
    }

    public JPopupMenu getPopupMenu() {
        if (plafContentManagerUI != null)
            return plafContentManagerUI.getPopupMenu();
        return null;
    }

    public void addContentManagerListener(ContentManagerListener listener) {
        listeners.add(ContentManagerListener.class, listener);
    }

    public void removeContentManagerListener(ContentManagerListener listener) {
        listeners.remove(ContentManagerListener.class, listener);
    }

    public ContentManagerListener[] getContentManagerListeners() {
        return listeners.getListeners(ContentManagerListener.class);
    }


    public void updateUI() {
        for (Content content : contents) {
            SwingUtilities.updateComponentTreeUI(content.getComponent());
            if (content.getPopupMenu() != null)
                SwingUtilities.updateComponentTreeUI(content.getPopupMenu());
        }

        if (getPopupMenu() != null)
            SwingUtilities.updateComponentTreeUI(getPopupMenu());

        plafContentManagerUI.updateUI();
    }

    public PlafContentManagerUI getPlafContentManagerUI() {
        return plafContentManagerUI;
    }


    protected Content addContentInternal(Object key, String title, Icon icon, Component component, String tip,
                                         ToolWindow toolWindow) {
        if (key == null)
            throw new IllegalArgumentException("Key cannot be null.");
        if (component == null)
            throw new IllegalArgumentException("Component cannot be null.");

        if (contentMap.containsKey(key))
            throw new IllegalArgumentException("Cannot register content with passed key. An already registered content exists. [key : " + key + "]");

        MyDoggyContent content = new MyDoggyContent(this, key, title, icon, component, tip, toolWindow);
        content.addUIPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                assert evt.getSource() instanceof Content;

                if ("selected".equals(evt.getPropertyName())) {
                    if (Boolean.TRUE.equals(evt.getNewValue()))
                        fireContentSelected((Content) evt.getSource());
                }
            }
        });
        contents.add(content);
        contentMap.put(key, content);
        plafContentManagerUI.addContent(content);

        fireContentAdded(content);

        return content;
    }

    protected void firePropertyChange(String property, Object oldValue, Object newValue) {
        PropertyChangeEvent event = new PropertyChangeEvent(this, property, oldValue, newValue);

        for (PropertyChangeListener listener : listeners.getListeners(PropertyChangeListener.class)) {
            listener.propertyChange(event);
        }
    }

    protected void fireContentAdded(Content content) {
        ContentManagerEvent event = new ContentManagerEvent(this, ContentManagerEvent.ActionId.CONTENT_ADDED, content);
        for (ContentManagerListener listener : listeners.getListeners(ContentManagerListener.class)) {
            listener.contentAdded(event);
        }
    }

    protected void fireContentRemoved(Content content) {
        ContentManagerEvent event = new ContentManagerEvent(this, ContentManagerEvent.ActionId.CONTENT_REMOVED, content);
        for (ContentManagerListener listener : listeners.getListeners(ContentManagerListener.class)) {
            listener.contentRemoved(event);
        }
    }

    protected void fireContentSelected(Content content) {
        ContentManagerEvent event = new ContentManagerEvent(this, ContentManagerEvent.ActionId.CONTENT_SELECTED, content);
        for (ContentManagerListener listener : listeners.getListeners(ContentManagerListener.class)) {
            listener.contentSelected(event);
        }
    }

}
