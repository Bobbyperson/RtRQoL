package rtrqol.patch;

import javassist.CtClass;
import javassist.CtMethod;
import rtrmodloader.api.ModPatch;

/**
 * Fixes AIRefineResource.enoughWorkersRefining():
 *
 * Workers ignore items already on the ground when deciding whether to produce more.
 * For RESOURCE_INFINITE_AMOUNT settings the method returns false immediately (never
 * enough), and for finite amounts the make-quota branch of the &&-check doesn't
 * account for ground stock at all. Either way, workers keep refining even when the
 * output type is piling up on the ground, and because AIRefineResource runs before
 * AIStoreResource in the AI chain the ground items are never picked up.
 *
 * Fix: at the top of enoughWorkersRefining(), if there are in-range ground items of
 * the output type, return true immediately. This makes the worker skip refining and
 * fall through to AIStoreResource, which picks up the ground items instead.
 */
public class RefineGroundCheckPatch implements ModPatch {

    @Override
    public void apply(CtClass cc, ClassLoader loader) throws Exception {
        // Vanilla: ../rtr/rtr/mobs/ai/AIRefineResource.java:286-313
        CtMethod m = cc.getDeclaredMethod("enoughWorkersRefining");
        m.insertBefore(
            "if (this.resource.getResourceGround($1, true) > 2) return true;"
        );
    }
}
