package rtrqol.patch;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import rtrmodloader.api.ModPatch;

/**
 * Patches AIDeliverRoadConstructionResource so that when road delivery fails
 * for ORGANIZER jobs, the chain ends at AIHarvestWork rather than looping back
 * to AIDeliverCourierResource (which already ran earlier in the organizer chain).
 * Road construction is the final task organizers attempt.
 */
public class OrganizerRoadLastPatch implements ModPatch {

    @Override
    public void apply(CtClass cc, ClassLoader loader) throws Exception {
        CtMethod execute = cc.getDeclaredMethod("execute");
        execute.instrument(new ExprEditor() {
            @Override
            public void edit(MethodCall m) throws javassist.CannotCompileException {
                if ("getAIDeliverCourierResource".equals(m.getMethodName())) {
                    m.replace("{"
                        + "if ($0.getMobJobType() == rtr.mobs.jobs.MobJobBase$MobJobType.ORGANIZER) {"
                        +     "$_ = $0.getAIHarvestWork();"
                        + "} else {"
                        +     "$_ = $proceed($$);"
                        + "}"
                        + "}");
                }
            }
        });
    }
}
