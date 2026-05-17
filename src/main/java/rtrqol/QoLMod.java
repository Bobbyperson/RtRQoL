package rtrqol;

import rtrmodloader.api.ModPatch;
import rtrmodloader.api.RtRMod;
import rtrqol.patch.BuildDescriptionColorPatch;
import rtrqol.patch.BuildingSelectPriorityPatch;
import rtrqol.patch.BuildWorkPatch;
import rtrqol.patch.DeliveryPriorityPatch;
import rtrqol.patch.ConsoleScrollEntryPatch;
import rtrqol.patch.ConsoleScrollInputPatch;
import rtrqol.patch.ConsoleScrollRenderPatch;
import rtrqol.patch.FindPathPatch;
import rtrqol.patch.MarketSellMaxPatch;
import rtrqol.patch.OrganizerConstructionLastPatch;
import rtrqol.patch.OrganizerConstructionLastTailPatch;
import rtrqol.patch.OrganizerRoadLastPatch;
import rtrqol.patch.WaterPathCostPatch;
import rtrqol.patch.RefineGroundCheckPatch;
import rtrqol.patch.UnstickPathPatch;
import rtrqol.patch.UpgradeRequirementsColorPatch;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QoLMod implements RtRMod {

    @Override
    public String getId() {
        return "rtrqol";
    }

    @Override
    public Map<String, List<ModPatch>> getPatches() {
        Map<String, List<ModPatch>> patches = new HashMap<String, List<ModPatch>>();
        patches.put("rtr/mobs/pathfinder/PathFinder", Arrays.<ModPatch>asList(
            new FindPathPatch(),
            new UnstickPathPatch(),
            new WaterPathCostPatch()
        ));
        patches.put("rtr/mobs/ai/AIBuildWork", Arrays.<ModPatch>asList(
            new BuildWorkPatch()
        ));
        patches.put("rtr/mobs/ai/AIDeliverConstructionResource", Arrays.<ModPatch>asList(
            new DeliveryPriorityPatch()
        ));
        patches.put("rtr/mobs/ai/AIRefineWaterBottle", Arrays.<ModPatch>asList(
            new OrganizerConstructionLastPatch()
        ));
        patches.put("rtr/mobs/ai/AIMakeTrashyCube", Arrays.<ModPatch>asList(
            new OrganizerConstructionLastTailPatch()
        ));
        patches.put("rtr/mobs/ai/AIDeliverRoadConstructionResource", Arrays.<ModPatch>asList(
            new OrganizerRoadLastPatch()
        ));
        patches.put("rtr/mobs/ai/AIRefineResource", Arrays.<ModPatch>asList(
            new RefineGroundCheckPatch()
        ));
        patches.put("rtr/console/ConsoleEntry", Arrays.<ModPatch>asList(
            new ConsoleScrollEntryPatch()
        ));
        patches.put("rtr/console/Console", Arrays.<ModPatch>asList(
            new ConsoleScrollRenderPatch()
        ));
        patches.put("rtr/states/PlayState", Arrays.<ModPatch>asList(
            new ConsoleScrollInputPatch(),
            new MarketSellMaxPatch(),
            new BuildingSelectPriorityPatch()
        ));
        patches.put("rtr/gui/states/playstate/SelectedObjectUpgradePanel", Arrays.<ModPatch>asList(
            new UpgradeRequirementsColorPatch()
        ));
        patches.put("rtr/gui/states/shared/RightObjectDescriptionPanel", Arrays.<ModPatch>asList(
            new BuildDescriptionColorPatch()
        ));
        return patches;
    }
}
