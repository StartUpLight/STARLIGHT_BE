package starlight.adapter.ai.util;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import starlight.domain.businessplan.enumerate.SubSectionType;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@ConfigurationProperties(prefix = "prompt.checklist")
@Getter
@Setter
public class ChecklistCatalog {

        private Map<String, CatalogSection> catalog;

        @Getter
        @Setter
        public static class CatalogSection {
                private List<ChecklistItem> items;
        }

        @Getter
        @Setter
        public static class ChecklistItem {
                private String criteria;
                private String detailed;
        }

        // 서브섹션 타입에 해당하는 criteria 리스트를 반환합니다
        public List<String> getCriteriaBySubSectionType(SubSectionType subSectionType) {
                String tag = subSectionType.getTag();
                if (catalog == null || !catalog.containsKey(tag)) {
                        return List.of();
                }
                CatalogSection section = catalog.get(tag);
                if (section == null || section.getItems() == null) {
                        return List.of();
                }
                return section.getItems().stream()
                                .map(ChecklistItem::getCriteria)
                                .filter(c -> c != null && !c.isEmpty())
                                .collect(Collectors.toList());
        }

        // 서브섹션 타입에 해당하는 detailed-criteria 리스트를 반환합니다.
        public List<String> getDetailedCriteriaBySubSectionType(SubSectionType subSectionType) {
                String tag = subSectionType.getTag();
                if (catalog == null || !catalog.containsKey(tag)) {
                        return List.of();
                }
                CatalogSection section = catalog.get(tag);
                if (section == null || section.getItems() == null) {
                        return List.of();
                }
                return section.getItems().stream()
                                .map(ChecklistItem::getDetailed)
                                .filter(d -> d != null && !d.isEmpty())
                                .collect(Collectors.toList());
        }
}
