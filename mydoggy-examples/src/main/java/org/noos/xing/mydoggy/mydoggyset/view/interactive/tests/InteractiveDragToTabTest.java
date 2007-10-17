package org.noos.xing.mydoggy.mydoggyset.view.interactive.tests;

import org.noos.xing.mydoggy.ToolWindowAnchor;
import org.noos.xing.mydoggy.ToolWindowManager;

import java.awt.*;

/**
 * @author Angelo De Caro (angelo.decaro@gmail.com)
 */
public class InteractiveDragToTabTest extends MyDoggySetInteractiveTest {

    public InteractiveDragToTabTest(ToolWindowManager toolWindowManager, Container root) throws AWTException {
        super(toolWindowManager, root);
    }

    public String getName() {
        return "Drag a Tool as a Tab";
    }

    public String getDescription() {
        return "<html>" +
                "<h2>Test Description</h2>" +
                "<p>" +
                "This test shows how to drag a tool as a tab for another tool." +
                "</p>" +
                "</html>";
    }

    public void execute() {
        restoreWorkspace();

        componentLookuper.lookup().moveToCenter();

        showTip("<html>Let's go to <br>show Tool 3", 1000);
        clickOnRepresentativeButton("Tool 3");

        showTip("<html>Let's go to <br>show Tool 13", 1000);
        clickOnRepresentativeButton("Tool 13");
        delay(500);

        showTip("<html>Drag Tool 13 to <br>Tool 3 as a Tab", 1000);
        drag("toolWindow.Tool 13.tab.Title 13", "toolWindow.tabContainer.Tool 3");

        showTip("<html>Drag Tool 3 to <br> the Top bar", 1000);
        drag("toolWindow.Tool 3.tab.Title 13", "toolWindowManager.bar." + ToolWindowAnchor.TOP);

        showTip("<html>Drag Tool 13 to <br> Tool 3 as a Tab", 1000);
        drag("toolWindow.Tool 13.tab.Title 13", "toolWindow.tabContainer.Tool 3");

        showTip("<html>Extract Tool 13", 1000);
        drag("toolWindow.Tool 3.tab.Title 13", "toolWindow.container.Tool 3");
    }

}