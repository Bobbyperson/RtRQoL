package rtrqol.patch;

import javassist.CtClass;
import javassist.CtMethod;
import rtrmodloader.api.ModPatch;

/**
 * Patches PlayState.mouseWheelMoved to scroll the console panel.
 *
 * Wheel-up over the console shows older messages (increases scroll offset);
 * wheel-down returns toward the latest messages (decreases scroll offset).
 * The offset is stored in System.getProperties() under "rtrqol.consolescroll"
 * so that ConsoleScrollRenderPatch can read it without a cross-class field
 * dependency (which would break if Console loads after PlayState).
 */
public class ConsoleScrollInputPatch implements ModPatch {

    @Override
    public void apply(CtClass cc, ClassLoader loader) throws Exception {
        // Vanilla: ../rtr/rtr/states/PlayState.java:4230
        //   public void mouseWheelMoved(int change) { ... }
        // Code is inserted before the existing method body.
        CtMethod m = cc.getDeclaredMethod("mouseWheelMoved");
        m.insertBefore("{"
            + "if (!transition.getControlLockout() && !loadingRequired && !gamePaused && !map.isMapLost()) {"
            + "    rtr.gui.states.playstate.ConsolePanel _cp = gui.getConsolePanel();"
            + "    if (_cp.intersectsGUIMask()) {"
            + "        int _off = 0;"
            + "        Object _p = System.getProperties().get(\"rtrqol.consolescroll\");"
            + "        if (_p != null) _off = ((Integer)_p).intValue();"
            + "        if ($1 > 50) {"
            + "            _off++;"
            + "        } else if ($1 < -50) {"
            + "            if (_off > 0) _off--;"
            + "        }"
            + "        System.getProperties().put(\"rtrqol.consolescroll\", Integer.valueOf(_off));"
            + "    }"
            + "}"
            + "}");
    }
}
