package husacct.define.domain;

import husacct.ServiceProvider;
import husacct.define.domain.appliedrule.AppliedRuleStrategy;
import husacct.define.domain.module.ModuleFactory;
import husacct.define.domain.module.ModuleStrategy;
import husacct.define.domain.module.modules.Component;
import husacct.define.domain.module.modules.Layer;
import husacct.define.domain.services.ModuleDomainService;
import husacct.define.domain.services.WarningMessageService;
import husacct.define.domain.services.stateservice.StateService;
import husacct.define.domain.softwareunit.SoftwareUnitDefinition;
import husacct.define.task.JtreeController;

import java.util.ArrayList;
import java.util.Collections;
import org.apache.log4j.Logger;

public class SoftwareArchitecture {

	private static SoftwareArchitecture instance = null;
	private ArrayList<AppliedRuleStrategy> appliedRules;
	private ArrayList<ModuleStrategy> modules = new ArrayList<ModuleStrategy>();
	private ModuleStrategy rootModule;
	
	private Logger logger = Logger.getLogger(SoftwareArchitecture.class);

	public SoftwareArchitecture() {
		this("SoftwareArchitecture", "This is the root of the architecture",
				new ArrayList<ModuleStrategy>(),
				new ArrayList<AppliedRuleStrategy>());
	}

	public SoftwareArchitecture(String name, String description) {
		this(name, description, new ArrayList<ModuleStrategy>(),
				new ArrayList<AppliedRuleStrategy>());
	}

	public SoftwareArchitecture(String name, String description, ArrayList<ModuleStrategy> modules, ArrayList<AppliedRuleStrategy> rules) {
		rootModule = new ModuleDomainService().createNewModule("Root");
		rootModule.set(name, description);
		rootModule.setSubModules(modules);
		setAppliedRules(rules);
		registerModule(rootModule);
		this.modules.add(rootModule);

	}

	public static SoftwareArchitecture getInstance() {
		return instance == null ? (instance = new SoftwareArchitecture()): instance;
	}

	public static void setInstance(SoftwareArchitecture sA) {
		instance = sA;
	}


	private void registerModule(ModuleStrategy module) {
		modules.add(module);
		//if (module instanceof Component) {
		//	modules.add(module.getSubModules().get(0));
		//}
		
	}

	public void addAppliedRule(AppliedRuleStrategy rule) {
		if ((!hasAppliedRule(rule.getId())) && (!appliedRules.contains(rule))) {
			appliedRules.add(rule);
		} else {
			logger.warn(String.format(" Rule already added: " + rule.getRuleTypeKey() + ", " + rule.getModuleFrom().getName() + ", " + rule.getModuleTo().getName()));
			throw new RuntimeException(ServiceProvider.getInstance().getLocaleService().getTranslatedString("RuleAlreadyAdded"));
		}
	}

	// Only to be used to add rootmodules to the top module (root)
	public String addModuleToRoot(ModuleStrategy module) {
		String message = "";
		try {
			if (!hasModule(module.getName())) {
				rootModule.addSubModule(module);
                registerModule(module);
				updateWarnings();
			} else {
				message = ServiceProvider.getInstance().getLocaleService().getTranslatedString("SameNameModule");
			}
		} catch (Exception rt) {
			logger.error(" Exception: " + rt.getMessage());
			//rt.printStackTrace();
		}
		return message;
	}

	//Only to be used to add child modules to a parent module 
	public String addModuleToParent(long parentModuleId, ModuleStrategy module) {
		String message = "";
		ModuleStrategy parentModule = getModuleById(parentModuleId);
		if (parentModule != null) {
			registerModule(module);
			message = parentModule.addSubModule(module);
		} else {
			message = ServiceProvider.getInstance().getLocaleService().getTranslatedString("NoModule");
		}
 		return message;
	}

	public AppliedRuleStrategy getAppliedRuleById(long appliedRuleId) {
		if (hasAppliedRule(appliedRuleId)) {
			for (AppliedRuleStrategy rule : appliedRules) {
				if (rule.getId() == appliedRuleId) {
					return rule;
				}
			}
		} else {
			throw new RuntimeException(ServiceProvider.getInstance().getLocaleService().getTranslatedString("NoRule"));
		}
		return null;
	}

	public ArrayList<AppliedRuleStrategy> getAppliedRules() {
		return appliedRules;
	}

	public ArrayList<Long> getAppliedRulesIdsByModuleFromId(long moduleId) {
		ArrayList<Long> appliedRuleIds = new ArrayList<Long>();
		for (AppliedRuleStrategy rule : appliedRules) {
			if ((rule.getModuleFrom().getId() == moduleId) && (!rule.isException())) {
				appliedRuleIds.add(rule.getId());
			}
		}
		return appliedRuleIds;
	}

	public ArrayList<Long> getAppliedRulesIdsByModuleToId(long moduleId) {
		ArrayList<Long> appliedRuleIds = new ArrayList<Long>();
		for (AppliedRuleStrategy rule : appliedRules) {
			if ((rule.getModuleTo().getId() == moduleId) && (!rule.isException())) {
				appliedRuleIds.add(rule.getId());
			}
		}
		return appliedRuleIds;
	}

	public String getDescription() {
		return rootModule.getDescription();
	}

	/** 
	 * Finds the ModuleStrategy with the given moduleId.
	 * @param moduleId
	 * @return ModuleStrategy of the found module, or null if no module is found.
	 */
	public ModuleStrategy getModuleById(long moduleId) {
		ModuleStrategy currentModule = null;
		if (rootModule.getId() == moduleId || rootModule.hasSubModule(moduleId)) {
			currentModule = rootModule;
			while (currentModule.getId() != moduleId) {
				for (ModuleStrategy subModule : currentModule.getSubModules()) {
					if (subModule.getId() == moduleId || subModule.hasSubModule(moduleId)) {
						currentModule = subModule;
					}
				}
			}
		}
		return currentModule;
	}

	// Returns null, if no SoftwareUnit with softwareUnitName is mapped to a ModuleStrategy	
	public ModuleStrategy getModuleBySoftwareUnit(String softwareUnitName) {
		ModuleStrategy moduleMappedToSU = null;

		for (ModuleStrategy module : modules) {
			moduleMappedToSU = getModuleMappedToSoftwareUnitName(module, softwareUnitName);
			if (moduleMappedToSU != null) {
				break;
			}
		}
		return moduleMappedToSU;
	}

	private ModuleStrategy getModuleMappedToSoftwareUnitName(ModuleStrategy module, String softwareUnitName){
		ModuleStrategy moduleMappedToSU = null;

		for (SoftwareUnitDefinition softwareUnitResult : module.getUnits()) {
			if (softwareUnitResult.getName().equals(softwareUnitName)) {
				moduleMappedToSU = module;
				return moduleMappedToSU;
			}
		}
		for (ModuleStrategy mod : module.getSubModules()){
			moduleMappedToSU = getModuleMappedToSoftwareUnitName(mod, softwareUnitName);
		}
		return moduleMappedToSU;
	}
	

	public ArrayList<ModuleStrategy> getModules() {
		return rootModule.getSubModules();
	}

	private ArrayList<ModuleStrategy> getModulesForLayerSorting(long parentModuleId) {
		ArrayList<ModuleStrategy> modulesToCheck = rootModule.getSubModules();
		if (parentModuleId != -1L) {
			ModuleStrategy parentModule = getModuleById(parentModuleId);
			if (parentModule != null) {
				modulesToCheck = parentModule.getSubModules();
			}
		}
		return modulesToCheck;
	}

	public String getModulesLogicalPath(long moduleId) {
		String logicalPath = "";
		if (rootModule.getId() == moduleId) {
			logicalPath = "**";
			return logicalPath;
		} else {
			ModuleStrategy wantedModule = getModuleById(moduleId);
			if (wantedModule != null) {
				ArrayList<String> list = new ArrayList<String>();
				list.add(wantedModule.getName());
				wantedModule = wantedModule.getparent();
				while(!wantedModule.getType().equals("Root")){
					list.add(wantedModule.getName());
					wantedModule = wantedModule.getparent();
				}
				int lenght = list.size();
				String[] names = new String[lenght];
				int i = 0;
				for(String s : list){
					names[i] = s;
					i++;
				}	
		        
				for (int j = lenght-1; j >= 1; j--) {
		        	logicalPath = logicalPath + names[j] + ".";
		        }
				logicalPath = logicalPath + names[0];
			}
		}
		return logicalPath;
	}
		
	public String getName() {
		return rootModule.getName();
	}

	public long getParentModuleIdByChildId(long childModuleId) {
		long parentModuleId = -1L;

		if (rootModule.getId() == childModuleId) {
			parentModuleId = -1;
		} else {
			ModuleStrategy childModule = getModuleById(childModuleId);
			if (childModule != null) {
				parentModuleId = childModule.getparent().getId();
			}
		}
		return parentModuleId;
	}

	public ModuleStrategy getRootModule() {
		return rootModule;
	}

	public SoftwareUnitDefinition getSoftwareUnitByName(String softwareUnitName) {
		SoftwareUnitDefinition softwareUnit = null;
		if (rootModule.hasSoftwareUnit(softwareUnitName)) {
			softwareUnit = rootModule.getSoftwareUnitByName(softwareUnitName);
		}
		if (softwareUnit == null) {
			throw new RuntimeException(ServiceProvider.getInstance()
					.getLocaleService().getTranslatedString("NoSoftwareUnit"));
		}
		return softwareUnit;
	}

	private Layer getTheFirstLayerAbove(int currentHierarchicalLevel, long parentModuleId) {
		Layer layer = null;
		for (ModuleStrategy mod : getModulesForLayerSorting(parentModuleId)) {
			if (mod instanceof Layer) {
				Layer l = (Layer) mod;
				if ((l.getHierarchicalLevel() < currentHierarchicalLevel)
						&& (layer == null || l.getHierarchicalLevel() > layer
								.getHierarchicalLevel())) {
					layer = l;
				}
			}
		}
		return layer;
	}

	public Layer getTheFirstLayerBelow(int currentHierarchicalLevel, long parentModuleId) {
		Layer layer = null;
		for (ModuleStrategy mod : getModulesForLayerSorting(parentModuleId)) {
			if (mod instanceof Layer) {
				Layer l = (Layer) mod;
				if (l.getHierarchicalLevel() > currentHierarchicalLevel
						&& (layer == null || l.getHierarchicalLevel() < layer
								.getHierarchicalLevel())) {
					layer = l;
				}
			}
		}
		return layer;
	}

	public Layer getTheFirstLayerBelow(Layer layer) {
		return getTheFirstLayerBelow(layer.getHierarchicalLevel(), getParentModuleIdByChildId(layer.getId()));
	}

	private boolean hasAppliedRule(long ruleID) {
		boolean ruleFound = false;
		for (AppliedRuleStrategy rule : appliedRules) {
			if (rule.getId() == ruleID) {
				ruleFound = true;
			}
		}

		return ruleFound;
	}

	// Returns true if the received name equals the name of the top module (root) or one of its children  
	private boolean hasModule(String name) {
		if (rootModule.getName().equals(name)) {
			return true;
		} else {
			for (ModuleStrategy module : rootModule.getSubModules()) {
				if (module.getName().equals(name)) {
					return true;
				}
			}
		}
		return false;
	}

	public void moveLayerDown(long layerId) {
		Layer layer = (Layer) getModuleById(layerId);
		if (layer != null) {
			Layer layerBelowLayer = getTheFirstLayerBelow(
					layer.getHierarchicalLevel(),
					getParentModuleIdByChildId(layerId));
			if (layerBelowLayer != null) {
				switchHierarchicalLayerLevels(layer, layerBelowLayer);
			}
		}
	}

	public void moveLayerUp(long layerId) {
		Layer layer = (Layer) getModuleById(layerId);
		if (layer != null) {
			Layer layerAboveLayer = getTheFirstLayerAbove(
					layer.getHierarchicalLevel(),
					getParentModuleIdByChildId(layerId));
			if (layerAboveLayer != null) {
				switchHierarchicalLayerLevels(layer, layerAboveLayer);
			}
		}
	}

	public void removeAllModules() {
		rootModule.setSubModules(new ArrayList<ModuleStrategy>());
		modules = new ArrayList<ModuleStrategy>();
	}

	public void removeAppliedRule(long appliedRuleId) {
		if (hasAppliedRule(appliedRuleId)) {
			AppliedRuleStrategy mainRule = getAppliedRuleById(appliedRuleId);
			 if((mainRule.getExceptions() != null) && (mainRule.getExceptions().size() >= 0)){
				 for (AppliedRuleStrategy exceptionRule : mainRule.getExceptions()){
					 appliedRules.remove(exceptionRule);
				 }
			 }
			appliedRules.remove(mainRule);
		} else {
			throw new RuntimeException(ServiceProvider.getInstance()
					.getLocaleService().getTranslatedString("NoRule"));
		}
	}

	public void removeAppliedRules() {
		appliedRules = new ArrayList<AppliedRuleStrategy>();
	}

	public void removeModule(ModuleStrategy moduleToRemove) {
		if (moduleToRemove.equals(rootModule)) {
			return;
		}
		ArrayList<ModuleStrategy> toBeRemoved = new ArrayList<ModuleStrategy>();
		ArrayList<Object[]> toBeSaved = new ArrayList<Object[]>();
		removeRecursively(moduleToRemove, toBeRemoved);
		Collections.reverse(toBeRemoved);
		JtreeController.instance().restoreTreeItems(moduleToRemove);

		for (ModuleStrategy module : toBeRemoved) {
			ModuleStrategy parent = module.getparent();
			ArrayList<AppliedRuleStrategy> moduleRules = removeRelatedRules(module);
			int index = parent.getSubModules().indexOf(module);
			removeFromRegistry(module);
            JtreeController.instance().restoreTreeItems(module);
			parent.getSubModules().remove(index);
			toBeSaved.add(new Object[] { module, moduleRules });
			WarningMessageService.getInstance().removeImplementationWarning(module);
		}

		boolean moduleFound = true;
		if (!moduleFound) {
			throw new RuntimeException(ServiceProvider.getInstance()
					.getLocaleService().getTranslatedString("NoModule"));
		}
	}

	private void removeRecursively(ModuleStrategy module, ArrayList<ModuleStrategy> childrens) {
		if (module.getSubModules().size() == 0) {
			childrens.add(module);
		} else if (module.getSubModules().size() > 0) {
			childrens.add(module);
			for (ModuleStrategy m : module.getSubModules()) {
				removeRecursively(m, childrens);
			}
		}
	}

	private void removeFromRegistry(ModuleStrategy module) {
		try {
			int index = modules.indexOf(module);
			if (index >= 0) {
				modules.remove(index);
				logger.info(String.format(" Module removed from the registry: " + module.getId() + " " + module.getName()));
			}
			updateWarnings();
		} catch (Exception r) {
			logger.warn(String.format(" The following module cannot be removed from the registry: " + module.getName()));
		}

	}

	public void updateWarnings() {
		WarningMessageService.getInstance().clearImplementationLevelWarnings();
		for (ModuleStrategy module : modules) {

			WarningMessageService.getInstance().processModule(module);
		}

	}

	private ArrayList<AppliedRuleStrategy> removeRelatedRules(
			ModuleStrategy module) {
		// Copy all currentValues into another list to prevent
		// ConcurrentModificationExceptions
		ArrayList<AppliedRuleStrategy> returnList = new ArrayList<AppliedRuleStrategy>();
		@SuppressWarnings("unchecked")
		ArrayList<AppliedRuleStrategy> tmpList = (ArrayList<AppliedRuleStrategy>) appliedRules
				.clone();
		for (AppliedRuleStrategy rule : appliedRules) {
			if (rule.getModuleFrom().equals(module)
					|| rule.getModuleTo().equals(module)) {
				returnList.add(rule);
				tmpList.remove(rule);
			}

			@SuppressWarnings("unchecked")
			ArrayList<AppliedRuleStrategy> tmpExceptionList = (ArrayList<AppliedRuleStrategy>) rule
					.getExceptions().clone();
			for (AppliedRuleStrategy exceptionRule : rule.getExceptions()) {
				if (exceptionRule.getModuleFrom().equals(module)
						|| exceptionRule.getModuleTo().equals(module)) {
					tmpExceptionList.remove(exceptionRule);
				}
			}
			rule.setExceptions(tmpExceptionList);
		}
		appliedRules = tmpList;
		return returnList;
	}

	public void setAppliedRules(ArrayList<AppliedRuleStrategy> appliedRules) {
		this.appliedRules = appliedRules;
	}

	public void setDescription(String description) {
		rootModule.setDescription(description);
	}

	public void setName(String name) {
		rootModule.setName(name);
	}

	private void switchHierarchicalLayerLevels(Layer layerOne, Layer layerTwo) {
		int hierarchicalLevelLayerOne = layerOne.getHierarchicalLevel();
		layerOne.setHierarchicalLevel(layerTwo.getHierarchicalLevel());
		layerTwo.setHierarchicalLevel(hierarchicalLevelLayerOne);
	}

	public ModuleStrategy updateModuleType(ModuleStrategy oldModule, String newType) {
		int index = oldModule.getparent().getSubModules().indexOf(oldModule);
		ModuleStrategy updatedModule = new ModuleFactory().updateModuleType(oldModule, newType);
		updateModule(index,updatedModule);
		return updatedModule;
	}

	private void updateModule(int subModuleIndex, ModuleStrategy updatedModule) {
		// Update parent
		ModuleStrategy parent = updatedModule.getparent();
		parent.getSubModules().remove(subModuleIndex);
		parent.addSubModule(subModuleIndex, updatedModule);
		int moduleIndex=0;
		for (int i = 0; i < modules.size(); i++) {
			if (modules.get(i).getId()==updatedModule.getId()) {
				moduleIndex=i;
			}
		}
		modules.remove(moduleIndex);
		modules.add(moduleIndex, updatedModule);
		logger.info(String.format(" Module updated in the registry: " + updatedModule.getId() + " " + updatedModule.getName()));
		if (updatedModule instanceof Component) {
			SoftwareArchitecture.getInstance().addModuleToParent(updatedModule.getId(),updatedModule.getSubModules().get(0));
		}
	}

	public void changeSoftwareUnit(long from, long to, ArrayList<String> names) {
		ModuleStrategy fromModule= getModuleById(from);
		ModuleStrategy toModule = getModuleById(to);
		if ((fromModule != null) && (toModule != null)) {
			ArrayList<SoftwareUnitDefinition> units = fromModule.getAndRemoveSoftwareUnits(names);
			toModule.addSUDefinition(units);
		}
	}

	public void registerImportedValues() {
		for (ModuleStrategy module : modules) {
			for (SoftwareUnitDefinition unit : module.getUnits()) {
				StateService.instance().registerImportedUnit(unit);
			}
			
			
		}
	}

}
