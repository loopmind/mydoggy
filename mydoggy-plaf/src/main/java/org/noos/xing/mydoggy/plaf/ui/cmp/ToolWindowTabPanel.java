package org.noos.xing.mydoggy.plaf.ui.cmp;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;
import org.noos.xing.mydoggy.ToolWindow;
import org.noos.xing.mydoggy.ToolWindowListener;
import org.noos.xing.mydoggy.ToolWindowTab;
import org.noos.xing.mydoggy.event.ToolWindowTabEvent;
import org.noos.xing.mydoggy.plaf.ui.DockedContainer;
import org.noos.xing.mydoggy.plaf.ui.MyDoggyKeySpace;
import org.noos.xing.mydoggy.plaf.ui.ResourceManager;
import org.noos.xing.mydoggy.plaf.ui.ToolWindowDescriptor;
import org.noos.xing.mydoggy.plaf.ui.util.GraphicsUtil;
import org.noos.xing.mydoggy.plaf.ui.util.MouseEventDispatcher;
import org.noos.xing.mydoggy.plaf.ui.util.SwingUtil;

import javax.swing.*;
import javax.swing.plaf.basic.BasicLabelUI;
import javax.swing.plaf.basic.BasicPanelUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EventListener;

/**
 * @author Angelo De Caro (angelo.decaro@gmail.com)
 */
public class ToolWindowTabPanel extends JComponent implements PropertyChangeListener {
    protected DockedContainer dockedContainer;
    protected ToolWindowDescriptor descriptor;
    protected ToolWindow toolWindow;
    protected ResourceManager resourceManager;

    protected JViewport viewport;
    protected JPanel tabContainer;
    protected TableLayout containerLayout;

    protected ToolWindowTab selectedTab;
    protected Component selecTabButton;
    protected PopupButton popupButton;

    protected MouseEventDispatcher mouseEventDispatcher;


    public ToolWindowTabPanel(DockedContainer dockedContainer, ToolWindowDescriptor descriptor) {
        this.descriptor = descriptor;
        this.toolWindow = descriptor.getToolWindow();
        this.resourceManager = descriptor.getResourceManager();
        this.dockedContainer = dockedContainer;
        this.mouseEventDispatcher = new MouseEventDispatcher();

        initComponents();
        initListeners();
    }


    public void propertyChange(PropertyChangeEvent evt) {
        String property = evt.getPropertyName();
        if ("selected".equals(property)) {
            ToolWindowTab tab = (ToolWindowTab) evt.getSource();
            if (evt.getNewValue() == Boolean.TRUE) {
                if (selectedTab != null)
                    selectedTab.setSelected(false);
                selectedTab = tab;
            }
        }
    }


    public JViewport getViewport() {
        return viewport;
    }

    public JPanel getTabContainer() {
        return tabContainer;
    }

    public void addEventDispatcherlListener(EventListener eventListener) {
        mouseEventDispatcher.addListener(eventListener);
    }

    public void removeEventDispatcherlListener(EventListener eventListener) {
        mouseEventDispatcher.removeListener(eventListener);
    }


    protected void initComponents() {
        setLayout(new ExtendedTableLayout(new double[][]{{TableLayout.FILL, 1, 14}, {0, TableLayout.FILL, 0}}, false));
        setFocusable(false);
        setBorder(null);
        
        tabContainer = new JPanel(containerLayout = new TableLayout(new double[][]{{0},
                                                                                   {resourceManager.getFloat("toolwindow.title.font.size", 12)+4}})); // TODO: 16 -> -2
        tabContainer.setName("toolWindow.tabContainer." + descriptor.getToolWindow().getId());
        tabContainer.setOpaque(false);
        tabContainer.setBorder(null);
        tabContainer.setFocusable(false);

        viewport = new JViewport();
        viewport.setBorder(null);
        viewport.setOpaque(false);
        viewport.setFocusable(false);
        viewport.setView(tabContainer);

        add(viewport, "0,1,FULL,FULL");
        add(popupButton = new PopupButton(), "2,1,FULL,FULL");

        viewport.addMouseWheelListener(new WheelScroller());

        initTabs();
    }

    protected void initListeners() {
        dockedContainer.setPopupUpdater(new DockedContainer.PopupUpdater() {
            final JMenuItem nextTabItem = new JMenuItem(new SelectNextTabAction());
            final JMenuItem previousTabItem = new JMenuItem(new SelectPreviousTabAction());
            final JMenuItem closeAllItem = new JMenuItem(new CloseAllTabAction());

            public void update(Component source, JPopupMenu popupMenu) {
                if (source.getParent() instanceof TabButton) {
                    TabButton tabButton = (TabButton) source.getParent();

                    int index = 0;
                    if (tabButton.tab.isCloseable()) {
                        final JMenuItem closeItem = new JMenuItem(new CloseTabAction(tabButton.tab));
                        popupMenu.add(closeItem, index++);
                        popupMenu.add(closeAllItem, index++);
                        popupMenu.add(new JSeparator(), index++);
                    }
                    popupMenu.add(nextTabItem, index++);
                    popupMenu.add(previousTabItem, index++);
                    popupMenu.add(new JSeparator(), index);
                }
            }

            class CloseTabAction extends AbstractAction {
                ToolWindowTab tab;

                public CloseTabAction(ToolWindowTab tab) {
                    super(resourceManager.getString("@@tool.tab.close"));
                    this.tab = tab;
                }

                public void actionPerformed(ActionEvent e) {
                    if (tab.isCloseable()) {
                        ToolWindowTabEvent event = new ToolWindowTabEvent(this, ToolWindowTabEvent.ActionId.TAB_REMOVING,
                                                                          toolWindow, tab);
                        for (ToolWindowListener listener : toolWindow.getToolWindowListeners()) {
                            boolean result = listener.toolWindowTabRemoving(event);
                            if (!result)
                                break;
                        }

                        toolWindow.removeToolWindowTab(tab);
                    }
                }
            }

            class CloseAllTabAction extends AbstractAction {
                public CloseAllTabAction() {
                    super(resourceManager.getString("@@tool.tab.closeAll"));
                }

                public void actionPerformed(ActionEvent e) {
                    ToolWindowTab selectedTab = null;

                    for (ToolWindowTab tab : toolWindow.getToolWindowTabs()) {
                        if (tab.isSelected()) {
                            selectedTab = tab;
                            continue;
                        }
                        tryToClose(tab);
                    }

                    tryToClose(selectedTab);
                }

                protected void tryToClose(ToolWindowTab tab) {
                    if (tab != null && tab.isCloseable()) {
                        ToolWindowTabEvent event = new ToolWindowTabEvent(this, ToolWindowTabEvent.ActionId.TAB_REMOVING,
                                                                          toolWindow, tab);

                        for (ToolWindowListener listener : toolWindow.getToolWindowListeners()) {
                            boolean result = listener.toolWindowTabRemoving(event);
                            if (!result)
                                break;
                        }

                        toolWindow.removeToolWindowTab(tab);
                    }
                }
            }

        });

        toolWindow.addToolWindowListener(new ToolWindowListener() {
            public void toolWindowTabAdded(ToolWindowTabEvent event) {
                if (tabContainer.getComponentCount() == 0)
                    initTabs();
                else
                    addTab(event.getToolWindowTab());

                checkPopupButton();
            }

            public boolean toolWindowTabRemoving(ToolWindowTabEvent event) {
                return true;
            }

            public void toolWindowTabRemoved(ToolWindowTabEvent event) {
                ToolWindowTab nextTab = removeTab(event.getToolWindowTab());

                if (event.getToolWindowTab().isSelected()) {
                    ToolWindowTab[] tabs = toolWindow.getToolWindowTabs();
                    if (tabs.length > 0) {
                        if (nextTab != null)
                            nextTab.setSelected(true);
                        else
                            tabs[0].setSelected(true);
                    }
                }

                checkPopupButton();
            }
        });

        viewport.addMouseListener(dockedContainer.getTitleBarMouseAdapter());
        viewport.addMouseListener(mouseEventDispatcher);
        viewport.addMouseMotionListener(mouseEventDispatcher);
    }

    protected void initTabs() {
        for (ToolWindowTab tab : toolWindow.getToolWindowTabs()) {
            addTab(tab);
        }

        checkPopupButton();
    }

    protected void addTab(ToolWindowTab tab) {
        int column = containerLayout.getNumColumn();
        containerLayout.insertColumn(column, 0);
        containerLayout.insertColumn(column + 1, -2);
        containerLayout.insertColumn(column + 2, 0);

        tabContainer.add(new TabButton(tab), (column + 1) + ",0" + ",FULL,FULL");

        tab.removePropertyChangeListener(this);
        tab.addPropertyChangeListener(this);

        SwingUtil.repaint(tabContainer);
    }

    protected ToolWindowTab removeTab(ToolWindowTab toolWindowTab) {
        int nextTabCol = -1;
        for (Component component : tabContainer.getComponents()) {
            if (component instanceof TabButton) {
                TabButton tabButton = (TabButton) component;
                if (tabButton.tab == toolWindowTab) {
                    TableLayoutConstraints constraints = containerLayout.getConstraints(tabButton);
                    tabContainer.remove(tabButton);
                    tabButton.removePropertyChangeListener(this);

                    nextTabCol = constraints.col1;
                    int col = constraints.col1 - 1;
                    containerLayout.deleteColumn(col);
                    containerLayout.deleteColumn(col);
                    containerLayout.deleteColumn(col);
                    break;
                }
            }
        }

        SwingUtil.repaint(tabContainer);

        if (nextTabCol != -1)
            for (Component component : tabContainer.getComponents()) {
                if (component instanceof TabButton) {
                    TabButton tabButton = (TabButton) component;
                    TableLayoutConstraints constraints = containerLayout.getConstraints(tabButton);

                    if (constraints.col1 == nextTabCol)
                        return tabButton.tab;
                }
            }

        return null;
    }

    protected void checkPopupButton() {
        boolean visible = toolWindow.getToolWindowTabs().length > 1;
        popupButton.setVisible(visible);

        ((TableLayout) getLayout()).setColumn(2, visible ? 14 : 0);
    }


    public class TabButton extends JPanel implements PropertyChangeListener, MouseListener, ActionListener {
        protected ToolWindowTab tab;

        protected TableLayout layout;
        protected JLabel titleLabel;
        protected JButton closeButton;

        protected boolean pressed;
        protected boolean inside;
        protected boolean selected;

        protected Timer flashingTimer;
        protected int flasingDuration;
        protected boolean flashingState;


        public TabButton(final ToolWindowTab tab) {
            setLayout(layout = new TableLayout(new double[][]{{-1, 0, 0}, {-1}}));

            setOpaque(false);
            setFocusable(false);
            setUI(new BasicPanelUI() {
                public void update(Graphics g, JComponent c) {
                    Rectangle bounds = c.getBounds();
                    bounds.x = bounds.y = 0;
                    
                    if (tab.isFlashing() && toolWindow.isVisible()) {
                        if (flashingState) {
                            GraphicsUtil.fillRect(g, bounds,
                                                  resourceManager.getColor(MyDoggyKeySpace.TWTB_BACKGROUND_ACTIVE_END),
                                                  resourceManager.getColor(MyDoggyKeySpace.TWTB_BACKGROUND_ACTIVE_START),
                                                  new RoundRectangle2D.Double(
                                                          bounds.x, bounds.y, bounds.width - 1, bounds.height - 1, 10, 10
                                                  ),
                                                  GraphicsUtil.UP_TO_BOTTOM_GRADIENT);
                        } else {
                            GraphicsUtil.fillRect(g, bounds,
                                                  resourceManager.getColor(MyDoggyKeySpace.TWTB_BACKGROUND_ACTIVE_START),
                                                  resourceManager.getColor(MyDoggyKeySpace.TWTB_BACKGROUND_ACTIVE_END),
                                                  new RoundRectangle2D.Double(
                                                          bounds.x, bounds.y, bounds.width - 1, bounds.height - 1, 10, 10
                                                  ),
                                                  GraphicsUtil.UP_TO_BOTTOM_GRADIENT);
                        }

                        if (flashingTimer == null) {
                            flashingTimer = new Timer(600, new ActionListener() {
                                long start = 0;

                                public void actionPerformed(ActionEvent e) {
                                    Rectangle bounds = TabButton.this.getBounds();
                                    bounds.x = bounds.y = 0;

                                    if (start == 0)
                                        start = System.currentTimeMillis();

                                    flashingState = !flashingState;

                                    SwingUtil.repaint(TabButton.this);

                                    if (flasingDuration != -1 && System.currentTimeMillis() - start > flasingDuration)
                                        tab.setFlashing(false);
                                }
                            });
                            flashingState = true;
                        }
                        if (!flashingTimer.isRunning()) {
                            flashingTimer.start();
                        }
                    } else if (tabContainer.getComponentCount() > 1) {
                        if (selected) {
                            if (toolWindow.isActive()) {
                                GraphicsUtil.fillRect(g, bounds,
                                                      resourceManager.getColor(MyDoggyKeySpace.TWTB_BACKGROUND_ACTIVE_END),
                                                      resourceManager.getColor(MyDoggyKeySpace.TWTB_BACKGROUND_ACTIVE_START),
                                                      new RoundRectangle2D.Double(
                                                              bounds.x, bounds.y, bounds.width - 1, bounds.height - 1, 10, 10
                                                      ),
                                                      GraphicsUtil.UP_TO_BOTTOM_GRADIENT);
                            } else
                                GraphicsUtil.fillRect(g, bounds,
                                                      resourceManager.getColor(MyDoggyKeySpace.TWTB_BACKGROUND_INACTIVE_END),
                                                      resourceManager.getColor(MyDoggyKeySpace.TWTB_BACKGROUND_INACTIVE_START),
                                                      new RoundRectangle2D.Double(
                                                              bounds.x, bounds.y, bounds.width - 1, bounds.height - 1, 10, 10
                                                      ),
                                                      GraphicsUtil.UP_TO_BOTTOM_GRADIENT);
                        }
                    }

                    super.update(g, c);
                }
            });
            addMouseListener(mouseEventDispatcher);
            addMouseMotionListener(mouseEventDispatcher);

            String name = "toolWindow." + tab.getOwner().getId() + ".tab." + tab.getTitle();
            setName(name);

            this.tab = tab;
            this.tab.addPropertyChangeListener(this);
            this.selected = this.pressed = this.inside = false;

            titleLabel = new JLabel(tab.getTitle());
            titleLabel.setName(name + ".title");
            titleLabel.setForeground(resourceManager.getColor(MyDoggyKeySpace.TWTB_TAB_FOREGROUND_UNSELECTED));
            titleLabel.setOpaque(false);
            titleLabel.setFocusable(false);
            titleLabel.setIcon(tab.getIcon());
            titleLabel.setFont(titleLabel.getFont().deriveFont(resourceManager.getFloat("toolwindow.title.font.size", 12)));
            titleLabel.setUI(new BasicLabelUI() {
                protected void paintEnabledText(JLabel l, Graphics g, String s, int textX, int textY) {
                    if (pressed && inside)
                        super.paintEnabledText(l, g, s, textX + 1, textY + 1);
                    else
                        super.paintEnabledText(l, g, s, textX, textY);
                }


            });
            titleLabel.addMouseListener(dockedContainer.getTitleBarMouseAdapter());
            titleLabel.addMouseListener(mouseEventDispatcher);
            titleLabel.addMouseMotionListener(mouseEventDispatcher);
            titleLabel.addMouseListener(this);
            add(titleLabel, "0,0,FULL,FULL");

            closeButton = (JButton) resourceManager.createComponent(
                    MyDoggyKeySpace.TOOL_WINDOW_TITLE_BUTTON,
                    descriptor.getManager()
            );
            closeButton.setName(name + ".closeButton");
            closeButton.addActionListener(this);
            closeButton.setToolTipText(resourceManager.getString("@@tool.tab.close"));
            closeButton.setIcon(resourceManager.getIcon(MyDoggyKeySpace.TAB_CLOSE));

            add(closeButton, "2,0,FULL,c");
        }


        public void actionPerformed(ActionEvent e) {
            toolWindow.removeToolWindowTab(tab);
        }

        public void mousePressed(MouseEvent e) {
            toolWindow.setActive(true);

            if (SwingUtilities.isLeftMouseButton(e) && !selected) {
                pressed = true;
                repaint();
            } else {
                pressed = false;
                repaint();
            }
        }

        public void mouseReleased(MouseEvent e) {
            pressed = false;
            repaint();
        }

        public void mouseEntered(MouseEvent e) {
            inside = true;
            repaint();
        }

        public void mouseExited(MouseEvent e) {
            inside = false;
            repaint();
        }

        public void mouseClicked(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        TabButton.this.tab.setSelected(true);
                    }
                });
            }
        }

        public void propertyChange(PropertyChangeEvent evt) {
            String property = evt.getPropertyName();
            if ("selected".equals(property)) {
                if (evt.getNewValue() == Boolean.FALSE) {
                    selecTabButton = null;

                    titleLabel.setForeground(resourceManager.getColor(MyDoggyKeySpace.TWTB_TAB_FOREGROUND_UNSELECTED));
                    closeButton.setIcon(resourceManager.getIcon(MyDoggyKeySpace.TAB_CLOSE_INACTIVE));
                    setButtonsEnabled(false);

                    selected = false;
                } else {
                    tab.setFlashing(false);
                    selecTabButton = this;

                    // Ensure position
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            Rectangle cellBounds = getBounds();
                            cellBounds.x -= viewport.getViewPosition().x;
                            viewport.scrollRectToVisible(cellBounds);
                        }
                    });

                    titleLabel.setForeground(resourceManager.getColor(MyDoggyKeySpace.TWTB_TAB_FOREGROUND_SELECTED));
                    closeButton.setIcon(resourceManager.getIcon(MyDoggyKeySpace.TAB_CLOSE));
                    setButtonsEnabled(true);
                    selected = true;
                }
                SwingUtil.repaint(this);
            } else if ("title".equals(property)) {
                titleLabel.setText((String) evt.getNewValue());
                setName("toolWindow." + toolWindow.getId() + ".tabs." + tab.getTitle());
            } else if ("icon".equals(property)) {
                titleLabel.setIcon((Icon) evt.getNewValue());
            } else if ("flash".equals(property)) {
                if (evt.getNewValue() == Boolean.TRUE) {
                    if (!tab.isSelected()) {
                        flasingDuration = -1;
                        SwingUtil.repaint(this);
                    }
                } else {
                    if (flashingTimer != null) {
                        flashingTimer.stop();
                        flashingTimer = null;
                        SwingUtil.repaint(this);
                    }
                }
            } else if ("flash.duration".equals(property)) {
                if (evt.getNewValue() == Boolean.TRUE) {
                    if (!tab.isSelected()) {
                        flasingDuration = (Integer) evt.getNewValue();
                        SwingUtil.repaint(this);
                    }
                } else {
                    if (flashingTimer != null) {
                        flashingTimer.stop();
                        flashingTimer = null;
                        SwingUtil.repaint(this);
                    }
                }
            }
        }

        public Insets getInsets() {
            return new Insets(0, 5, 0, 5);
        }

        public ToolWindowTab getTab() {
            return tab;
        }


        protected void setButtonsEnabled(boolean enabled) {
            if (enabled && tabContainer.getComponentCount() > 1 && tab.isCloseable()) {
                layout.setColumn(1, 3);
                layout.setColumn(2, 14);
            } else {
                layout.setColumn(1, 0);
                layout.setColumn(2, 0);
            }
            revalidate();
            repaint();
        }

    }

    protected class PopupButton extends ToolWindowActiveButton implements ActionListener {
        protected JPopupMenu popupMenu;

        public PopupButton() {
            setIcon(resourceManager.getIcon(MyDoggyKeySpace.TOO_WINDOW_TAB_POPUP));
            addActionListener(this);
            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    toolWindow.setActive(true);
                }
            });
            setVisible(false);
        }

        public void actionPerformed(ActionEvent e) {
            initPopup();
            popupMenu.show(this, 10, 10);
        }

        protected void initPopup() {
            if (popupMenu == null) {
                popupMenu = new JPopupMenu("TabPopup");
                popupMenu.add(new SelectNextTabAction());
                popupMenu.add(new SelectPreviousTabAction());
                popupMenu.addSeparator();
            }

            for (int i = 3, size = popupMenu.getComponentCount(); i < size; i++)
                popupMenu.remove(3);

            for (ToolWindowTab tab : toolWindow.getToolWindowTabs())
                popupMenu.add(new SelectTabAction(tab));
        }

    }

    protected class WheelScroller implements MouseWheelListener {
        public void mouseWheelMoved(MouseWheelEvent e) {
            switch (e.getWheelRotation()) {
                case 1:
                    Rectangle visRect = viewport.getViewRect();
                    Rectangle bounds = tabContainer.getBounds();

                    visRect.x += e.getUnitsToScroll() * 2;
                    if (visRect.x + visRect.width >= bounds.width)
                        visRect.x = bounds.width - visRect.width;

                    viewport.setViewPosition(new Point(visRect.x, visRect.y));
                    break;
                case -1:
                    visRect = viewport.getViewRect();

                    visRect.x += e.getUnitsToScroll() * 2;
                    if (visRect.x < 0)
                        visRect.x = 0;
                    viewport.setViewPosition(new Point(visRect.x, visRect.y));
                    break;
            }
        }
    }

    public static class SelectTabAction extends AbstractAction {
        private ToolWindowTab tab;

        public SelectTabAction(ToolWindowTab tab) {
            super(tab.getTitle());
            this.tab = tab;
        }

        public void actionPerformed(ActionEvent e) {
            tab.setSelected(true);
        }
    }

    protected class SelectNextTabAction extends AbstractAction {

        public SelectNextTabAction() {
            super(resourceManager.getString("@@tool.tab.selectNext"));
        }

        public void actionPerformed(ActionEvent e) {
            ToolWindowTab[] tabs = toolWindow.getToolWindowTabs();
            if (selectedTab != null && selecTabButton != null) {
                int nextTabCol = containerLayout.getConstraints(selecTabButton).col1 + 3;

                for (Component component : tabContainer.getComponents()) {
                    if (component instanceof TabButton) {
                        TabButton tabButton = (TabButton) component;
                        TableLayoutConstraints constraints = containerLayout.getConstraints(tabButton);

                        if (constraints.col1 == nextTabCol) {
                            tabButton.tab.setSelected(true);
                            return;
                        }
                    }
                }
                if (tabs.length > 0)
                    tabs[0].setSelected(true);
            } else if (tabs.length > 0)
                tabs[0].setSelected(true);
        }
    }

    protected class SelectPreviousTabAction extends AbstractAction {

        public SelectPreviousTabAction() {
            super(resourceManager.getString("@@tool.tab.selectPreviuos"));
        }

        public void actionPerformed(ActionEvent e) {
            ToolWindowTab[] tabs = toolWindow.getToolWindowTabs();
            if (selectedTab != null && selecTabButton != null) {
                int nextTabCol = containerLayout.getConstraints(selecTabButton).col1 - 3;

                for (Component component : tabContainer.getComponents()) {
                    if (component instanceof TabButton) {
                        TabButton tabButton = (TabButton) component;
                        TableLayoutConstraints constraints = containerLayout.getConstraints(tabButton);

                        if (constraints.col1 == nextTabCol) {
                            tabButton.tab.setSelected(true);
                            return;
                        }
                    }
                }
                if (tabs.length > 0)
                    tabs[tabs.length - 1].setSelected(true);
            } else if (tabs.length > 0)
                tabs[tabs.length - 1].setSelected(true);
        }
    }

}
