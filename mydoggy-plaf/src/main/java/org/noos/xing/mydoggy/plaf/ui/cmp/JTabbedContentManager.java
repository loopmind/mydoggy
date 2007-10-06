package org.noos.xing.mydoggy.plaf.ui.cmp;

import org.noos.xing.mydoggy.*;
import org.noos.xing.mydoggy.plaf.MyDoggyToolWindowManager;
import org.noos.xing.mydoggy.plaf.ui.ResourceManager;
import org.noos.xing.mydoggy.plaf.ui.cmp.event.TabEvent;
import org.noos.xing.mydoggy.plaf.ui.cmp.event.TabListener;
import org.noos.xing.mydoggy.plaf.ui.content.action.NextContentAction;
import org.noos.xing.mydoggy.plaf.ui.content.action.PreviousContentAction;
import org.noos.xing.mydoggy.plaf.ui.util.SwingUtil;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.plaf.TabbedPaneUI;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Hashtable;
import java.util.Map;

public class JTabbedContentManager extends JTabbedPane {
    static final int BUTTONSIZE = 15;
    static final int WIDTHDELTA = 5;

    private ToolWindowManager toolWindowManager;
    private ResourceManager resourceManager;

    private Map<Accessible, ContentPage> contentPages;

    private JPopupMenu popupMenu;
    private boolean closeable;
    private boolean detachable;

    private ByteArrayOutputStream tmpWorkspace = null;


    public JTabbedContentManager() {
        super.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        this.contentPages = new Hashtable<Accessible, ContentPage>();
        this.closeable = this.detachable = true;
        setFocusable(false);

        MouseInputAdapter mouseInputAdapter = new MouseOverTabListener();
        TabMoveHandler tabMoveHandler = new TabMoveHandler();
        addMouseListener(mouseInputAdapter);
        addMouseListener(tabMoveHandler);
        addMouseMotionListener(mouseInputAdapter);
        addMouseMotionListener(tabMoveHandler);

    }


    public void insertTab(String title, Icon icon, Component component, String tip, int index) {
        this.insertTab(title, icon, component, tip, index, null);
    }

    public String getToolTipText(MouseEvent event) {
        int index = indexAtLocation(event.getX(), event.getY());
        if (index != -1)
            return getContentPage(index).getToolTipTextAt(event, index, super.getToolTipTextAt(index));
        return super.getToolTipText(event);
    }

    public void setTitleAt(int index, String title) {
        getContentPage(index).setTitle(title);
    }

    public String getTitleAt(int index) {
        return "";  // TODO: others look&feel may request for this ..........
    }

    public void setIconAt(int index, Icon icon) {
        ContentPage contentPage = getContentPage(index);
        contentPage.setIcon(icon);
        super.setIconAt(index, contentPage.getContentIcon());
    }

    public Icon getIconAt(int index) {
        return getContentPage(index).getContentIcon();
    }

    public void setMnemonicAt(int tabIndex, int mnemonic) {
        super.setMnemonicAt(tabIndex, mnemonic);
        getContentPage(tabIndex).setMnemonic(mnemonic);
    }

    public Icon getDisabledIconAt(int index) {
        return getContentPage(index).getContentIcon();
    }

    public void addTab(String title, Icon icon, Component component, String tip, TabbedContentUI tabbedContentUI) {
        insertTab(title, icon, component, tip, getTabCount(), tabbedContentUI);
    }

    public void setPopupMenuAt(int index, JPopupMenu popupMenu) {
        checkIndex(index);
        getContentPage(index).setPopupMenu(popupMenu);
    }

    public JPopupMenu getPopupMenuAt(int index) {
        checkIndex(index);
        return getContentPage(index).getPopupMenu();
    }

    public void setPopupMenu(JPopupMenu popupMenu) {
        this.popupMenu = popupMenu;
    }

    public JPopupMenu getPopupMenu() {
        return popupMenu;
    }

    public void setCloseable(boolean closeable) {
        this.closeable = closeable;
        for (ContentPage contentPage : contentPages.values()) {
            contentPage.setCloseable(closeable);
        }
    }

    public boolean isCloseable() {
        return closeable;
    }

    public boolean isDetachable() {
        return detachable;
    }

    public void setDetachable(boolean detachable) {
        this.detachable = detachable;
        for (ContentPage contentPage : contentPages.values()) {
            contentPage.setDetachable(detachable);
        }
    }

    public ContentPage getContentPage(int index) {
        return getContentPage(index, null);
    }

    public ContentPage getContentPage(Content content) {
        for (ContentPage contentPage : contentPages.values()) {
            if (contentPage.getContent() == content)
                return contentPage;
        }
        return null;
    }

    public void setToolWindowManager(MyDoggyToolWindowManager toolWindowManager) {
        this.toolWindowManager = toolWindowManager;
        this.resourceManager = toolWindowManager.getResourceManager();
        setupActions();
    }

    public void addTabListener(TabListener l) {
        listenerList.add(TabListener.class, l);
    }

    public void removeTabListener(TabListener l) {
        listenerList.remove(TabListener.class, l);
    }


    protected void insertTab(String title, Icon icon, Component component, String tip, int index, TabbedContentUI tabbedContentUI) {
        if (tip == null)
            tip = "";
        super.insertTab(title, icon, component, tip, index);

        ContentPage contentPage = getContentPage(index, tabbedContentUI);
        contentPage.setTitle(title);
        contentPage.setIcon(icon);

        super.setTitleAt(index, "");
        super.setIconAt(index, getContentPage(index).getContentIcon());
    }

    protected ContentPage getContentPage(int index, TabbedContentUI tabbedContentUI) {
        Accessible accessible = getAccessibleContext().getAccessibleChild(index);
        ContentPage contentPage = contentPages.get(accessible);
        if (contentPage == null) {
            if (tabbedContentUI == null)
                contentPage = new ContentPage(this, (AccessibleContext) accessible, resourceManager);
            else {
                contentPage = (ContentPage) tabbedContentUI;
                contentPage.setAccessible((AccessibleContext) accessible);
            }
            contentPages.put(accessible, contentPage);
        }
        return contentPage;
    }

    protected void checkIndex(int index) {
        if (index < 0 || index >= getTabCount())
            throw new IndexOutOfBoundsException("Index: " + index + ", Content count: " + getTabCount());
    }

    protected void fireCloseTabEvent(MouseEvent e, int overTabIndex) {
        TabEvent event = new TabEvent(this, TabEvent.ActionId.ON_CLOSE, getContent(overTabIndex), e, null, overTabIndex);
        for (TabListener tabListener : getListeners(TabListener.class))
            tabListener.tabEventFired(event);
    }

    protected void fireDetachTabEvent(MouseEvent e, int overTabIndex) {
        TabEvent event = new TabEvent(this, TabEvent.ActionId.ON_DETACH, getContent(overTabIndex), e, null, overTabIndex);
        for (TabListener tabListener : getListeners(TabListener.class))
            tabListener.tabEventFired(event);
    }

    protected void setMaximized(boolean maximize) {
        if (maximize) {
            toolWindowManager.getPersistenceDelegate().save(tmpWorkspace = new ByteArrayOutputStream());
            toolWindowManager.getToolWindowGroup().setVisible(false);
        } else {
            toolWindowManager.getPersistenceDelegate().merge(new ByteArrayInputStream(tmpWorkspace.toByteArray()),
                                                             PersistenceDelegate.MergePolicy.UNION);
            tmpWorkspace = null;
        }
    }

    protected boolean isMaximized() {
        return tmpWorkspace != null;
    }

    protected void setupActions() {
        SwingUtil.addKeyActionMapping(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, this,
                                      KeyStroke.getKeyStroke(39, InputEvent.ALT_MASK),
                                      "nextContent", new NextContentAction(toolWindowManager));
        SwingUtil.addKeyActionMapping(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, this,
                                      KeyStroke.getKeyStroke(37, InputEvent.ALT_MASK),
                                      "previousContent", new PreviousContentAction(toolWindowManager));
    }

    protected Content getContent(int tabIndex) {
        if (getTabCount() == 0) {
            if (tabIndex == 0 && !((TabbedContentManagerUI) toolWindowManager.getContentManager().getContentManagerUI()).isShowAlwaysTab())
                return toolWindowManager.getContentManager().getContent(0);
            else
                throw new IllegalStateException("Invalid State [code: AZ001]");
        } else
            return getContentPage(tabIndex).getContent();
    }


    class MouseOverTabListener extends MouseInputAdapter {
        private int mouseOverTab = -1;

        public void mouseClicked(MouseEvent e) {
            if (mouseOverTab >= 0 && mouseOverTab < getTabCount()) {
                ContentPage contentPage = getContentPage(mouseOverTab);

                if (contentPage.isDetachFired(e.getPoint())) {
                    fireDetachTabEvent(e, mouseOverTab);
                    return;
                }

                if (contentPage.isCloseFired(e.getPoint())) {
                    fireCloseTabEvent(e, mouseOverTab);
                    return;
                }

                if (e.getClickCount() == 2) {
                    // Maximization
                    setMaximized(!isMaximized());
                } else {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        getContentPage(mouseOverTab).showPopupMenu(
                                JTabbedContentManager.this, e, mouseOverTab, popupMenu
                        );
                    }
                }
            } else if (SwingUtilities.isRightMouseButton(e)) {
                if (popupMenu != null)
                    popupMenu.show(JTabbedContentManager.this, e.getX(), e.getY());
            }
        }

        public void mouseExited(MouseEvent e) {
            if (mouseOverTab != -1) {
                int tmp = mouseOverTab;
                mouseOverTab = -1;
                if (tmp < getTabCount())
                    repaint(getBoundsAt(tmp));
            }
        }

        public void mouseMoved(MouseEvent e) {
            if (!JTabbedContentManager.this.isEnabled())
                return;

            if (mouseOverTab != -1) {
                int tmp = mouseOverTab;
                mouseOverTab = -1;
                if (tmp < getTabCount())
                    repaint(getBoundsAt(tmp));
            }

            int tabIndex = indexAtLocation(e.getX(), e.getY());
            if (tabIndex >= 0 && isEnabledAt(tabIndex)) {
                mouseOverTab = tabIndex;
                if (tabIndex < getTabCount())
                    repaint(getBoundsAt(tabIndex));
            }
        }
    }

    /**
     * TODO: far funzionare meglio.......
     */
    class TabMoveHandler extends MouseAdapter implements MouseMotionListener {
        int startIndex = -1;
        private int currentIndex = -1;

        /* (non-Javadoc)
        * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
        */
        public void mousePressed(MouseEvent e) {
            if (!e.isPopupTrigger()) {
                JTabbedPane tabbedPane = (JTabbedPane) e.getSource();
                startIndex = tabbedPane.indexAtLocation(e.getX(), e.getY());
            }
            currentIndex = -1;
        }

        /* (non-Javadoc)
        * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
        */
        public void mouseReleased(MouseEvent e) {
            JTabbedPane tabbedPane = (JTabbedPane) e.getSource();
            if (!e.isPopupTrigger()) {
                int endIndex = tabbedPane.indexAtLocation(e.getX(), e.getY());

                if (startIndex != -1 && endIndex != -1 && startIndex != endIndex) {
                    moveTab(tabbedPane, startIndex, endIndex);
                    tabbedPane.setSelectedIndex(endIndex);

               }
            }
            startIndex = -1;
            clearRectangle(tabbedPane);
            currentIndex = -1;
        }

        /**
         */
        private void moveTab(JTabbedPane pane, int src, int dst) {
            // Get all the properties
            Component comp = pane.getComponentAt(src);
            String label = pane.getTitleAt(src);
            Icon icon = pane.getIconAt(src);
            Icon iconDis = pane.getDisabledIconAt(src);
            String tooltip = pane.getToolTipTextAt(src);
            boolean enabled = pane.isEnabledAt(src);
            int keycode = pane.getMnemonicAt(src);
            int mnemonicLoc = pane.getDisplayedMnemonicIndexAt(src);
            Color fg = pane.getForegroundAt(src);
            Color bg = pane.getBackgroundAt(src);

            // Remove the tab
            pane.remove(src);

            // Add a new tab
            pane.insertTab(label, icon, comp, tooltip, dst);

            // Restore all properties
            pane.setDisabledIconAt(dst, iconDis);
            pane.setEnabledAt(dst, enabled);
            pane.setMnemonicAt(dst, keycode);
            pane.setDisplayedMnemonicIndexAt(dst, mnemonicLoc);
            pane.setForegroundAt(dst, fg);
            pane.setBackgroundAt(dst, bg);
        }


        /* (non-Javadoc)
        * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
        */
        public void mouseDragged(MouseEvent e) {
            if (startIndex != -1) {
                JTabbedPane tabbedPane = (JTabbedPane) e.getSource();
                int index = tabbedPane.indexAtLocation(e.getX(), e.getY());

                if (index != -1 && index != currentIndex) { // moved over another tab
                    clearRectangle(tabbedPane);
                    currentIndex = index;
                }

                if (currentIndex != -1 && currentIndex != startIndex) {
                    drawRectangle(tabbedPane);
                }
            }
        }

        private void clearRectangle(JTabbedPane tabbedPane) {
            if (currentIndex == -1) {
                return;
            }
            TabbedPaneUI ui = tabbedPane.getUI();
            Rectangle rect = ui.getTabBounds(tabbedPane, currentIndex);
            tabbedPane.repaint(rect);
        }

        private void drawRectangle(JTabbedPane tabbedPane) {
            TabbedPaneUI ui = tabbedPane.getUI();
            Rectangle rect = ui.getTabBounds(tabbedPane, currentIndex);
            Graphics graphics = tabbedPane.getGraphics();
            graphics.setColor(Color.WHITE);
            graphics.drawRect(rect.x, rect.y, rect.width - 1, rect.height - 1);
        }

        /* (non-Javadoc)
        * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
        */
        public void mouseMoved(MouseEvent e) {
        }

        /* (non-Javadoc)
        * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
        */
        public void mouseExited(MouseEvent e) {
            clearRectangle((JTabbedPane) e.getSource());
            currentIndex = -1;
        }
    }
}

