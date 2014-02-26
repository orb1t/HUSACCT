package husacct.validate.domain.validation.ruletype.relationruletypes;

import husacct.common.dto.DependencyDTO;
import husacct.common.dto.RuleDTO;
import husacct.validate.domain.check.util.CheckConformanceUtilClass;
import husacct.validate.domain.configuration.ConfigurationServiceImpl;
import husacct.validate.domain.validation.Severity;
import husacct.validate.domain.validation.Violation;
import husacct.validate.domain.validation.ViolationType;
import husacct.validate.domain.validation.internaltransferobjects.Mapping;
import husacct.validate.domain.validation.ruletype.RuleType;
import husacct.validate.domain.validation.ruletype.RuleTypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

public class IsNotAllowedToUseRule extends RuleType {

	public IsNotAllowedToUseRule(String key, String category, List<ViolationType> violationTypes, Severity severity) {
		super(key, category, violationTypes, EnumSet.of(RuleTypes.IS_ALLOWED_TO_USE), severity);
	}

	@Override
	public List<Violation> check(ConfigurationServiceImpl configuration, RuleDTO rootRule, RuleDTO currentRule) {
		mappings = CheckConformanceUtilClass.filterClassesFrom(currentRule);
		physicalClasspathsFrom = mappings.getMappingFrom();
		List<Mapping> physicalClasspathsTo = mappings.getMappingTo();

		for (Mapping classPathFrom : physicalClasspathsFrom) {
			for (Mapping classPathTo : physicalClasspathsTo) {
				ArrayList<DependencyDTO> violatingDependencies = configuration.getDependenciesFromTo(classPathFrom.getPhysicalPath(), classPathTo.getPhysicalPath());
				int size = violatingDependencies.size();
				if(size >= 1){
					for(DependencyDTO dependency : violatingDependencies){
						Violation violation = createViolation(rootRule, classPathFrom, classPathTo, dependency, configuration);
                        violations.add(violation);
					}
				}
			}
		}
		return violations;
	}
}