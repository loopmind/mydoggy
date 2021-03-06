package org.noos.xing.mydoggy;

/**
 * This enumeration specifies the "push away" modes for tools with specific anchor.
 *
 * @author Angelo De Caro (angelo.decaro@gmail.com)
 * @since 1.2.0
 */
public enum PushAwayMode {

    /**
     * Using this mode, left/right tools push away top/bottom tools.
     * @since 1.2.0
     */
    HORIZONTAL,

    /**
     * Using this mode, top/bottom tools push away left/right tools.
     * @since 1.2.0
     */
    VERTICAL,

    /**
     * Using this mode, left tool pushs away bottom tool, bottom tool pushs away right tool,
     * right tool pushs away top tool.
     * @since 1.2.0
     */
    ANTICLOCKWISE,

    /**
     * Whoever is pressed last toolwindow push away the previous toolwindows. This way the order
     * of opening the toolwindows determine who push who (and you can achieve any "priority" mode
     * by selecting the order of clicks)      
     *
     * @since 1.3.0
     */
    MOST_RECENT
}
