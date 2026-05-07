package rtrqol.patch;

import javassist.CtClass;
import javassist.CtMethod;
import rtrmodloader.api.ModPatch;

/**
 * Makes ConsoleEntry.decreaseAlpha a no-op so message alpha never decays.
 * Without this, each new console message calls decreaseAlpha(20) on every
 * existing entry, making scrolled history invisible after ~13 new messages.
 * The fade-in animation on new messages is unaffected (alphaCurrent still
 * ramps up from 0 via update()).
 */
public class ConsoleScrollEntryPatch implements ModPatch {

    @Override
    public void apply(CtClass cc, ClassLoader loader) throws Exception {
        // Vanilla: ../rtr/rtr/console/ConsoleEntry.java:35-38
        //   public void decreaseAlpha(float a) {
        //       this.alphaMax -= a;
        //       this.alphaCurrent -= a;
        //   }
        CtMethod m = cc.getDeclaredMethod("decreaseAlpha");
        m.setBody("{}");
    }
}
