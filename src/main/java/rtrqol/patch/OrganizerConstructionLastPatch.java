package rtrqol.patch;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import rtrmodloader.api.ModPatch;

/**
 * Patches AIRefineWaterBottle to skip construction delivery for ORGANIZER jobs,
 * sending them directly to road construction delivery instead. This makes organizers
 * try construction delivery last (after store/redistribute/courier) rather than first.
 *
 * The tail of the chain is completed by OrganizerConstructionLastTailPatch (AIMakeTrashyCube)
 * and by the organizer-aware failBranch in DeliveryPriorityPatch.
 */
public class OrganizerConstructionLastPatch implements ModPatch {

    @Override
    public void apply(CtClass cc, ClassLoader loader) throws Exception {
        CtMethod execute = cc.getDeclaredMethod("execute");
        execute.instrument(new ExprEditor() {
            @Override
            public void edit(MethodCall m) throws javassist.CannotCompileException {
                if ("getAIDeliverConstructionResource".equals(m.getMethodName())) {
                    m.replace("{"
                        + "if ($0.getMobJobType() == rtr.mobs.jobs.MobJobBase$MobJobType.ORGANIZER) {"
                        +     "$_ = $0.getAIDeliverCourierResource();"
                        + "} else {"
                        +     "$_ = $proceed($$);"
                        + "}"
                        + "}");
                }
            }
        });
    }
}
