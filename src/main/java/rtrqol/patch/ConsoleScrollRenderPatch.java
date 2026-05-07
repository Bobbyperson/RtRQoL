package rtrqol.patch;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import rtrmodloader.api.ModPatch;

/**
 * Patches Console to support a scrollable view of message history.
 *
 * Two changes:
 * 1. Increases consoleMaxLength from 11 → 100 so there is actually history
 *    to scroll through (done by intercepting the field read in update()).
 * 2. Rewrites renderConsole to shift the displayed window by a scroll offset
 *    stored in System.getProperties() under "rtrqol.consolescroll", so
 *    ConsoleScrollInputPatch (in PlayState) can control which slice is shown.
 */
public class ConsoleScrollRenderPatch implements ModPatch {

    @Override
    public void apply(CtClass cc, ClassLoader loader) throws Exception {
        // Intercept the consoleMaxLength read in update() and return 100 instead,
        // allowing the console to retain up to 101 messages of history.
        // Vanilla: ../rtr/rtr/console/Console.java:16 — private static int consoleMaxLength = 11;
        //          ../rtr/rtr/console/Console.java:26 — if (consoleDisplay.size() > consoleMaxLength + 1)
        CtMethod update = cc.getDeclaredMethod("update");
        update.instrument(new ExprEditor() {
            @Override
            public void edit(FieldAccess fa) throws CannotCompileException {
                if (fa.isReader() && "consoleMaxLength".equals(fa.getFieldName())) {
                    fa.replace("$_ = 100;");
                }
            }
        });

        // Rewrite renderConsole(Graphics g, int x, int y, int count) to shift the
        // displayed window by a scroll offset stored in System.getProperties().
        // Vanilla: ../rtr/rtr/console/Console.java:53-61
        //   public static void renderConsole(Graphics g, int x, int y, int length) {
        //       int i = length;
        //       while (i > 0) {
        //           if (consoleDisplay.size() > i) {
        //               consoleDisplay.get(consoleDisplay.size() - i).render(x + 6, y - i * 12 - 3);
        //           }
        //           --i;
        //       }
        //   }
        CtMethod m = cc.getDeclaredMethod("renderConsole");
        m.setBody("{"
            + "int _off = 0;"
            + "Object _p = System.getProperties().get(\"rtrqol.consolescroll\");"
            + "if (_p != null) _off = ((Integer)_p).intValue();"
            + "for (int _i = $4; _i > 0; _i--) {"
            + "    int _idx = _i + _off;"
            + "    if (consoleDisplay.size() > _idx) {"
            + "        ((rtr.console.ConsoleEntry) consoleDisplay.get(consoleDisplay.size() - _idx))"
            + "            .render($2 + 6, $3 - _i * 12 - 3);"
            + "    }"
            + "}"
            + "}");
    }
}
