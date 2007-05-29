package org.noos.xing.mydoggy.tutorial;

import info.clearthought.layout.TableLayout;
import org.noos.xing.mydoggy.*;
import org.noos.xing.mydoggy.event.ContentManagerUIEvent;
import org.noos.xing.mydoggy.plaf.MyDoggyToolWindowManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SampleApp {
    private JFrame frame;
    private ToolWindowManager toolWindowManager;

    protected void setUp() {
        initComponents();
        initToolWindowManager();
    }

    protected void start() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // Set debug tool active
                ToolWindow debugTool = toolWindowManager.getToolWindow("Debug");
                debugTool.setActive(true);

                frame.setVisible(true);
            }
        });
    }

    protected void initComponents() {
        // This is need to a correct visualization of all JPopupMenu.
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);

        this.frame = new JFrame("Sample App...");
        this.frame.setSize(640, 480);
        this.frame.setLocation(100, 100);
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // I love TableLayout. It's great.
        this.frame.getContentPane().setLayout(new TableLayout(new double[][]{{0, -1, 0}, {0, -1, 0}}));
    }

    protected void initToolWindowManager() {
        // Create a new instance of MyDoggyToolWindowManager passing the frame.
        MyDoggyToolWindowManager myDoggyToolWindowManager = new MyDoggyToolWindowManager(frame);
        this.toolWindowManager = myDoggyToolWindowManager;

        // Register a Tool.
        toolWindowManager.registerToolWindow("Debug",                   // Id
                                             "Debugging" ,              // Title
                                             null,                      // Icon
                                             new JButton("Debugging"),  // Component
                                             ToolWindowAnchor.LEFT);    // Anchor
        setupDebugTool();

        // Made all tools available
        for (ToolWindow window : toolWindowManager.getToolWindows())
            window.setAvailable(true);

        initContentManager();

        // Add myDoggyToolWindowManager to the frame. MyDoggyToolWindowManager is an extension of a JPanel
        this.frame.getContentPane().add(myDoggyToolWindowManager, "1,1,");
    }

    protected void setupDebugTool() {
        final ToolWindow debugTool = toolWindowManager.getToolWindow("Debug");

        // Setup descriptors
        DockedTypeDescriptor dockedTypeDescriptor = (DockedTypeDescriptor) debugTool.getTypeDescriptor(ToolWindowType.DOCKED);
        dockedTypeDescriptor.setDockLength(350);
        dockedTypeDescriptor.setPopupMenuEnabled(true);
        JMenu toolsMenu = dockedTypeDescriptor.getToolsMenu();
        toolsMenu.add(new AbstractAction("Hello World!!!") {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(frame, "Hello World!!!");
            }
        });
        dockedTypeDescriptor.setToolWindowActionHandler(new ToolWindowActionHandler() {
            public void onHideButtonClick(ToolWindow toolWindow) {
                JOptionPane.showMessageDialog(frame, "Hiding...");
                toolWindow.setVisible(false);
            }
        });

        SlidingTypeDescriptor slidingTypeDescriptor = (SlidingTypeDescriptor) debugTool.getTypeDescriptor(ToolWindowType.SLIDING);
        slidingTypeDescriptor.setEnabled(false);
        slidingTypeDescriptor.setTransparentMode(true);
        slidingTypeDescriptor.setTransparentRatio(0.8f);
        slidingTypeDescriptor.setTransparentDelay(0);

        FloatingTypeDescriptor floatingTypeDescriptor = (FloatingTypeDescriptor) debugTool.getTypeDescriptor(ToolWindowType.FLOATING);
        floatingTypeDescriptor.setEnabled(true);
        floatingTypeDescriptor.setLocation(150, 200);
        floatingTypeDescriptor.setSize(320, 200);
        floatingTypeDescriptor.setModal(false);
        floatingTypeDescriptor.setTransparentMode(true);
        floatingTypeDescriptor.setTransparentRatio(0.2f);
        floatingTypeDescriptor.setTransparentDelay(1000);

        // Setup Tabs
        JButton button = new JButton("Profiling");
        RemoveTabAction removeTabAction = new RemoveTabAction();
        button.addActionListener(removeTabAction);
        removeTabAction.tab = debugTool.addToolWindowTab("Profiling", button);
    }

    public Container getFrame() {
        return frame;
    }

    public void dispose() {
        frame.setVisible(false);
        frame.dispose();
    }

    class RemoveTabAction implements ActionListener {
        ToolWindowTab tab;

        public void actionPerformed(ActionEvent e) {
            ToolWindow debugTool = toolWindowManager.getToolWindow("Debug");
            debugTool.removeToolWindowTab(tab);
        }
    }

    protected void initContentManager() {
        JButton treeContent = new JButton("Add Tab");
        treeContent.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final ToolWindow debugTool = toolWindowManager.getToolWindow("Debug");

                JButton button = new JButton("Remove");
                RemoveTabAction removeTabAction = new RemoveTabAction();
                button.addActionListener(removeTabAction);
                JPanel p = new JPanel();
                p.add(button);
                p.add(new JButton("OK"));

                removeTabAction.tab = debugTool.addToolWindowTab("Tab_" + System.identityHashCode(button), p);
            }
        });

        JPanel panel = new JPanel();
        panel.add(treeContent);
        JButton ok  = new JButton("OK");
        ok.setName("ok");
        panel.add(ok);

        ContentManager contentManager = toolWindowManager.getContentManager();
        Content content = contentManager.addContent("Tree Key",
                                                    "Tree Title",
                                                    null,      // An icon
                                                    panel);
        content.setToolTipText("Tree tip");

        setupContentManagerUI();
    }

    protected void setupContentManagerUI() {
        TabbedContentManagerUI contentManagerUI = (TabbedContentManagerUI) toolWindowManager.getContentManager().getContentManagerUI();
        contentManagerUI.setShowAlwaysTab(true);
        contentManagerUI.setTabPlacement(TabbedContentManagerUI.TabPlacement.BOTTOM);
        contentManagerUI.addContentManagerUIListener(new ContentManagerUIListener() {
            public boolean contentUIRemoving(ContentManagerUIEvent event) {
                return JOptionPane.showConfirmDialog(frame, "Are you sure?") == JOptionPane.OK_OPTION;
            }

            public void contentUIDetached(ContentManagerUIEvent event) {
                JOptionPane.showMessageDialog(frame, "Hello World!!!");
            }
        });

        TabbedContentUI contentUI = contentManagerUI.getContentUI(toolWindowManager.getContentManager().getContent(0));
        contentUI.setCloseable(true);
        contentUI.setDetachable(true);
        contentUI.setTransparentMode(true);
        contentUI.setTransparentRatio(0.7f);
        contentUI.setTransparentDelay(1000);
    }


    public static void main(String[] args) {
        SampleApp test = new SampleApp();
        try {
            test.setUp();
            test.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}