package rtrqol.patch;

import javassist.CtClass;
import javassist.CtMethod;
import rtrmodloader.api.ModPatch;

/**
 * Fixes AIBuildWork.workerCountTooHigh():
 *
 * The original compares resource limits against workerCountOnSite (workers physically
 * at the building), not workerCount (all workers assigned to the building, including
 * those still pathfinding). This lets an unlimited number of builders commit to a
 * site in parallel, all racing to build the same small set of available resources.
 *
 * Fix: use workerCount (all assigned workers) for the resource-based limit checks.
 * The MAXIMUM_WORKERS hard cap is left unchanged.  The > comparator is intentionally
 * kept (not changed to >=) because continueBuildWork() calls this method while the
 * mob is already counted in workerCount, so using >= would cause mobs to abort
 * in-progress builds when they are the only worker at a single-resource site.
 */
public class BuildWorkPatch implements ModPatch {

    @Override
    public void apply(CtClass cc, ClassLoader loader) throws Exception {
        // Vanilla: ../rtr/rtr/mobs/ai/AIBuildWork.java:440-478
        // Full method replaced. Key bug at line 477:
        //   return workerCount > 6
        //       || workerCountOnSite > totalResourcesNeededLeft   // ← should be workerCount
        //       || workerCountOnSite > totalResourcesAvailable;   // ← should be workerCount
        CtMethod m = cc.getDeclaredMethod("workerCountTooHigh");
        m.setBody("{"
            + "java.util.ArrayList mobArray = this.mob.getMobsByAssignment("
            +     "rtr.utilities.Assignment$AssignmentType.BUILDING, false);"
            + "int workerCount = 0;"
            + "int totalResourcesAvailable = 0;"
            + "for (int i = 0; i < mobArray.size(); i++) {"
            +     "rtr.mobs.MobBase mob = (rtr.mobs.MobBase) mobArray.get(i);"
            +     "if (mob.getAssignment() != null && mob.getAssignment().getPrimaryObject() == $1) {"
            +         "workerCount++;"
            +     "}"
            + "}"
            + "rtr.mobs.jobs.MobJobBase job = this.thisMob.getMobJobType().getJob();"
            + "rtr.resources.ResourceModule$ResourceType[] resourceTypeArray ="
            +     "rtr.resources.ResourceModule$ResourceType.values();"
            + "for (int n = 0; n < resourceTypeArray.length; n++) {"
            +     "rtr.resources.ResourceModule$ResourceType type = resourceTypeArray[n];"
            +     "if (job.canWorkerBuild(type) && !$1.isPaused()"
            +         "&& $1.getObjectFlags().canBuildingBeBuilt(type)"
            +         "&& $1.isResourceAvailable(type)) {"
            +         "totalResourcesAvailable += $1.getResourceCount(type);"
            +     "}"
            + "}"
            + "int totalResourcesNeededLeft ="
            +     "$1.getObjectFlags().getTotalValueBase() - $1.getTotalValue();"
            + "return workerCount > 6"
            +     "|| workerCount > totalResourcesNeededLeft"
            +     "|| workerCount > totalResourcesAvailable;"
            + "}");
    }
}
