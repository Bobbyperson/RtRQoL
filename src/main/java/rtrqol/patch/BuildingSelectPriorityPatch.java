package rtrqol.patch;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import rtrmodloader.api.ModPatch;

/**
 * Patches PlayState.controlsMousePressedInterfaceMode to give buildings
 * priority over dead bodies when both occupy the same click location.
 *
 * Vanilla: getMobAt (includeDead=true) is called before getObjectAt, so a
 * corpse anywhere in the click hitbox pokes and returns, preventing the
 * building selection branch from ever running.
 *
 * Fix: if getMobAt returns a dead mob and there is a selectable building (or
 * loot box) at the mouse location, poke the dead body AND null out the mob
 * result so the building selection logic runs. Both effects happen on every
 * click — the building gets selected and the corpse still decays.
 */
public class BuildingSelectPriorityPatch implements ModPatch {

    @Override
    public void apply(CtClass cc, ClassLoader loader) throws Exception {
        CtMethod m = cc.getDeclaredMethod("controlsMousePressedInterfaceMode");
        m.instrument(new ExprEditor() {
            @Override
            public void edit(MethodCall mc) throws CannotCompileException {
                if ("getMobAt".equals(mc.getMethodName())) {
                    mc.replace("{"
                        + "$_ = $proceed($$);"
                        + "if ($_ != null && $_.isDead()) {"
                        + "    rtr.objects.ObjectBase _obj = object.getObjectAt((org.newdawn.slick.geom.Shape)this.mouse);"
                        + "    if (_obj != null && (!_obj.getObjectFlags().canNotSelect() || _obj.getObjectFlags().isLootBox())) {"
                        + "        $_.poke();"
                        + "        $_ = null;"
                        + "    }"
                        + "}"
                        + "}");
                }
            }
        });
    }
}
