package rtrqol.patch;

import javassist.CtClass;
import javassist.CtMethod;
import rtrmodloader.api.ModPatch;

/**
 * Patches AIDeliverConstructionResource.findWork() to honor building priority
 * when assigning delivery workers.
 *
 * Vanilla: builds a map of (type → buildings in priority order), then shuffles
 * the resource types and iterates types-outer / buildings-inner. Whichever type
 * wins the shuffle is served first, regardless of whether those buildings have
 * higher or lower priority than buildings needing other types. Over many workers
 * this produces roughly equal (random) distribution across types, ignoring the
 * user's priority ordering entirely.
 *
 * Fix: iterate buildings-outer (master priority list order) / types-inner.
 * The first deliverable (building, type) pair encountered is always the highest-
 * priority building that has any deliverable resource available, so every worker
 * serves the highest-priority under-supplied building first.
 */
public class DeliveryPriorityPatch implements ModPatch {

    @Override
    public void apply(CtClass cc, ClassLoader loader) throws Exception {
        CtMethod m = cc.getDeclaredMethod("findWork");
        m.setBody("{"
            // Early exits
            + "rtr.mobs.jobs.MobJobBase$MobJobType jobType = this.thisMob.getMobJobType();"
            // Organizers treat construction delivery as last resort; end their chain at AIHarvestWork
            // so they don't loop back to road/courier/redistribute (those already ran before us).
            + "rtr.mobs.ai.AIBase _failTarget;"
            + "if (jobType == rtr.mobs.jobs.MobJobBase$MobJobType.ORGANIZER) {"
            +     "_failTarget = this.thisMob.getAIDeliverRoadConstructionResource();"
            + "} else {"
            +     "_failTarget = failBranch;"
            + "}"
            + "if (jobType == rtr.mobs.jobs.MobJobBase$MobJobType.NONE) {"
            +     "this.switchNode(_failTarget);"
            +     "return;"
            + "}"
            + "rtr.mobs.jobs.MobJobBase job = jobType.getJob();"
            + "if (!job.canWorkerDeliverConstruction()) {"
            +     "this.switchNode(_failTarget);"
            +     "return;"
            + "}"
            // Setup
            + "rtr.utilities.Assignment assignment = this.thisMob.getAssignment();"
            + "assignment.reset();"
            + "int mobID = this.thisMob.getID();"
            + "java.util.ArrayList canDeliverResources = new java.util.ArrayList();"
            + "java.util.ArrayList objectList = this.object.getObjectMasterBuildingPriorityList();"
            // Build canDeliverResources: types where any building needs delivery
            + "java.util.ArrayList deliverableTypes = job.getCanWorkerDeliverConstruction();"
            + "for (int _di = 0; _di < deliverableTypes.size(); _di++) {"
            +     "rtr.resources.ResourceModule$ResourceType type = (rtr.resources.ResourceModule$ResourceType) deliverableTypes.get(_di);"
            +     "for (int _oi = 0; _oi < objectList.size(); _oi++) {"
            +         "rtr.objects.ObjectBase o = (rtr.objects.ObjectBase) objectList.get(_oi);"
            +         "if (!o.isPaused() && o.getObjectFlags().canBuildingBeBuilt(type)"
            +             "&& this.thisMob.canReach(o, true) && o.getAcceptBuildResourceCount(type, mobID) > 0) {"
            +             "canDeliverResources.add(type);"
            +             "break;"
            +         "}"
            +     "}"
            + "}"
            + "if (canDeliverResources.size() == 0) {"
            +     "this.switchNode(_failTarget);"
            +     "return;"
            + "}"
            // Build validConstruction: type -> buildings in priority order
            + "java.util.HashMap validConstruction = new java.util.HashMap();"
            + "boolean foundConstruction = false;"
            + "for (int _ti = 0; _ti < canDeliverResources.size(); _ti++) {"
            +     "rtr.resources.ResourceModule$ResourceType t = (rtr.resources.ResourceModule$ResourceType) canDeliverResources.get(_ti);"
            +     "validConstruction.put(t, new java.util.ArrayList());"
            +     "for (int _oi2 = 0; _oi2 < objectList.size(); _oi2++) {"
            +         "rtr.objects.ObjectBase o = (rtr.objects.ObjectBase) objectList.get(_oi2);"
            +         "java.util.ArrayList _vcList = (java.util.ArrayList) validConstruction.get(t);"
            +         "if (this.thisMob.canReach(o, true) && !o.isPaused()"
            +             "&& o.getObjectFlags().canBuildingBeBuilt(t)"
            +             "&& o.getAcceptBuildResourceCount(t, mobID) > 0 && !_vcList.contains(o)) {"
            +             "_vcList.add(o);"
            +             "foundConstruction = true;"
            +         "}"
            +     "}"
            + "}"
            + "if (!foundConstruction) {"
            +     "this.switchNode(_failTarget);"
            +     "return;"
            + "}"
            // Lookup maps (unchanged from vanilla)
            + "java.util.HashMap validStoresByTypeLowestPriority = rtr.mobs.ai.AIUtilities.createLowestPriorityStorageListHashMap(this.thisMob, canDeliverResources, objectList);"
            + "java.util.HashMap validResourcesOnGroundByType = rtr.mobs.ai.AIUtilities.createAvailabeResourceList(this.thisMob, canDeliverResources, true, this.thisMobFlags.getWorkNearbyResourcesOnGroundDistance());"
            // Fixed loop: buildings outer (priority order), types inner
            + "for (int _bi = 0; _bi < objectList.size(); _bi++) {"
            +     "rtr.objects.ObjectBase deliveryBuilding = (rtr.objects.ObjectBase) objectList.get(_bi);"
            +     "for (int _ti2 = 0; _ti2 < canDeliverResources.size(); _ti2++) {"
            +         "rtr.resources.ResourceModule$ResourceType deliveryType = (rtr.resources.ResourceModule$ResourceType) canDeliverResources.get(_ti2);"
            +         "java.util.ArrayList validConstructionArray = (java.util.ArrayList) validConstruction.get(deliveryType);"
            +         "if (!validConstructionArray.contains(deliveryBuilding)) continue;"
            +         "java.util.ArrayList validStoresByTypeLowestPriorityArray = (java.util.ArrayList) validStoresByTypeLowestPriority.get(deliveryType);"
            +         "java.util.ArrayList resourcesOnGroundByTypeArray = (java.util.ArrayList) validResourcesOnGroundByType.get(deliveryType);"
            +         "if (validStoresByTypeLowestPriorityArray.size() == 0 && resourcesOnGroundByTypeArray.size() == 0) continue;"
            +         "java.util.ArrayList coordinatesList = rtr.mobs.ai.AIUtilities.createAvailabeResourceCoordinatesList(this.thisMob, validStoresByTypeLowestPriorityArray, resourcesOnGroundByTypeArray);"
            +         "rtr.utilities.OrderedPair closestResourceAnywhere = this.pathFinder.searchFor(this.thisMob.getTileX(), this.thisMob.getTileY(), coordinatesList, false, true, this.thisMobFlags.getBlockMapGroup());"
            +         "rtr.resources.ResourceBase pickupResource = this.resource.getResourceOnGround(closestResourceAnywhere);"
            +         "if (pickupResource != null && pickupResource.getType() == deliveryType && resourcesOnGroundByTypeArray.contains(pickupResource)) {"
            +             "assignment.setDeliveryObject(deliveryBuilding);"
            +             "assignment.setPrimaryCoordinates(closestResourceAnywhere);"
            +             "assignment.addPickupResources(pickupResource);"
            +             "assignment.setAssignmentType(rtr.utilities.Assignment$AssignmentType.PICK_UP_RESOURCE_FROM_GROUND);"
            +             "assignment.finalizeAssignment();"
            +             "this.requestPath(closestResourceAnywhere);"
            +             "this.aiStep = 1;"
            +             "return;"
            +         "}"
            +         "rtr.objects.ObjectBase pickupBuilding = this.object.getBuildingAtLocation(closestResourceAnywhere);"
            +         "if (pickupBuilding == null || !validStoresByTypeLowestPriorityArray.contains(pickupBuilding)) continue;"
            +         "rtr.utilities.OrderedPair pickupOutCoordinates = pickupBuilding.getBestRandomInteractCoordinates(this.thisMob, true, true, true);"
            +         "rtr.utilities.OrderedPair deliverOutCoordinates = deliveryBuilding.getBestRandomInteractCoordinates(this.thisMob, true, true, true);"
            +         "int carryMax = this.thisMob.getCarryMax();"
            +         "int availableResources = pickupBuilding.getAvailableResourceCount(deliveryType, false);"
            +         "int deliveryMax = deliveryBuilding.getAcceptBuildResourceCount(deliveryType, mobID);"
            +         "int pickupAmount = availableResources > carryMax ? carryMax : availableResources;"
            +         "if (pickupAmount > deliveryMax) {"
            +             "pickupAmount = deliveryMax;"
            +         "}"
            +         "assignment.setDeliveryObject(deliveryBuilding);"
            +         "assignment.setSecondaryObject(pickupBuilding);"
            +         "assignment.setPrimaryCoordinates(deliverOutCoordinates);"
            +         "assignment.setSecondaryCoordinates(pickupOutCoordinates);"
            +         "int _i = 0;"
            +         "while (_i < pickupAmount) {"
            +             "assignment.addPickupResources(pickupBuilding.getAvailableResource(deliveryType));"
            +             "_i++;"
            +         "}"
            +         "assignment.setAssignmentType(rtr.utilities.Assignment$AssignmentType.DELIVERY);"
            +         "assignment.finalizeAssignment();"
            +         "this.requestPath(pickupOutCoordinates);"
            +         "this.aiStep = 2;"
            +         "return;"
            +     "}"
            + "}"
            + "this.switchNode(_failTarget);"
            + "}");
    }
}
