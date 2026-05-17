package rtrqol.patch;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import rtrmodloader.api.ModPatch;

/**
 * Patches AIMakeTrashyCube to insert construction delivery for ORGANIZER jobs
 * after the trash-disposal chain, making it the last task organizers attempt.
 * Without this patch organizers would skip to AIHarvestWork without ever trying
 * construction delivery (since OrganizerConstructionLastPatch bypasses it at the start).
 */
public class OrganizerConstructionLastTailPatch implements ModPatch {

    @Override
    public void apply(CtClass cc, ClassLoader loader) throws Exception {
        CtMethod execute = cc.getDeclaredMethod("execute");
        execute.instrument(new ExprEditor() {
            @Override
            public void edit(MethodCall m) throws javassist.CannotCompileException {
                if ("getAIHarvestWork".equals(m.getMethodName())) {
                    m.replace("{"
                        + "if ($0.getMobJobType() == rtr.mobs.jobs.MobJobBase$MobJobType.ORGANIZER) {"
                        +     "$_ = $0.getAIDeliverConstructionResource();"
                        + "} else {"
                        +     "$_ = $proceed($$);"
                        + "}"
                        + "}");
                }
            }
        });
    }
}
