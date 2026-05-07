package rtrqol.patch;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import rtrmodloader.api.ModPatch;

/**
 * Patches PlayState.controlsMousePressedLeftPanel to add shift-click support
 * on the trade amount up/down buttons.
 *
 * Shift + up button → set trade amount to village stock shown in trade panel (total minus equipped, capped at 2000).
 * Shift + down button → set trade amount to 0.
 *
 * Mirrors the existing shift-click behaviour on the resource management and
 * courier panels (PlayState.java lines 2267–2365), where shift+up/down goes
 * to RESOURCE_INFINITE_AMOUNT/0.
 */
public class MarketSellMaxPatch implements ModPatch {

    @Override
    public void apply(CtClass cc, ClassLoader loader) throws Exception {
        CtMethod m = cc.getDeclaredMethod("controlsMousePressedLeftPanel");

        // Vanilla lines 2082/2086 (in the tradeResourceBuyDown/SellDown and
        // tradeResourceBuyUp/SellUp button handler loops):
        //   trade.decreaseTradeAmount((ResourceModule.ResourceType)t, rightClick ? 10 : 1);
        //   trade.increaseTradeAmount((ResourceModule.ResourceType)t, rightClick ? 10 : 1);
        m.instrument(new ExprEditor() {
            @Override
            public void edit(MethodCall mc) throws CannotCompileException {
                String name = mc.getMethodName();
                if ("increaseTradeAmount".equals(name)) {
                    // Shift+up: jump to village stock shown in UI (total minus equipped).
                    mc.replace("{"
                        + "if (rtr.states.StateBase.input.isKeyDown(42)) {"
                        + "    rtr.resources.ResourceModule res = (rtr.resources.ResourceModule)"
                        + "        rtr.states.StateBase.getModule(rtr.ModuleBase.ModuleType.RESOURCE);"
                        + "    int stock = res.getResourceTotal((rtr.resources.ResourceModule.ResourceType)$1, true)"
                        + "              - res.getResourceEquipped((rtr.resources.ResourceModule.ResourceType)$1);"
                        + "    $0.resetTradeAmount((rtr.resources.ResourceModule.ResourceType)$1);"
                        + "    $0.increaseTradeAmount((rtr.resources.ResourceModule.ResourceType)$1, stock);"
                        + "} else {"
                        + "    $proceed($$);"
                        + "}"
                        + "}");
                } else if ("decreaseTradeAmount".equals(name)) {
                    // Shift+down: jump to 0.
                    mc.replace("{"
                        + "if (rtr.states.StateBase.input.isKeyDown(42)) {"
                        + "    $0.resetTradeAmount((rtr.resources.ResourceModule.ResourceType)$1);"
                        + "} else {"
                        + "    $proceed($$);"
                        + "}"
                        + "}");
                }
            }
        });
    }
}
