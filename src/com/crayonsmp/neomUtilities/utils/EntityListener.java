package com.crayonsmp.neomUtilities.utils;

import com.crayonsmp.neomUtilities.NeomUtilities;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.animation.handler.AnimationHandler;
import com.ticxo.modelengine.api.animation.property.IAnimationProperty;
import com.ticxo.modelengine.api.events.AnimationPlayEvent;
import com.ticxo.modelengine.api.generator.parser.blockbench.BlockbenchModel;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import net.momirealms.craftengine.core.plugin.config.blockbench.Animation;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.inventory.meta.components.consumable.ConsumableComponent;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;

public class EntityListener implements Listener {

    private final Map<EntityType, Object> entityModelMapping = new HashMap<>();

    public EntityListener() {
        loadModelsFromConfig();
    }

    private void loadModelsFromConfig() {
        ConfigurationSection section = NeomUtilities.getInstance().getConfig().getConfigurationSection("entity-model-mapping");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            try {
                EntityType type = EntityType.valueOf(key.toUpperCase());
                entityModelMapping.put(type, section.get(key));
            } catch (IllegalArgumentException e) {
                NeomUtilities.getInstance().getLogger().warning("Invalid Entity Type in Config: " + key);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntitySpawn(EntitySpawnEvent event) {
        Entity entity = event.getEntity();
        Object mapping = entityModelMapping.get(entity.getType());

        if (mapping == null) return;

        String modelId = null;

        if (mapping instanceof String simpleId) {
            modelId = simpleId;
        } else if (mapping instanceof ConfigurationSection subSection) {
            modelId = determineStateModel(entity, subSection);
        }

        if (modelId != null) {
            applyModel(entity, modelId);
        }
    }

    private String determineStateModel(Entity entity, ConfigurationSection section) {
        String state = entity.getPersistentDataContainer().get(
                new NamespacedKey("neom", "oxidation_state"),
                PersistentDataType.STRING
        );

        if (state != null && section.contains(state)) {
            return section.getString(state);
        }

        return section.getString("default");
    }

    public void applyModel(Entity bukkitEntity, String modelId) {
        ModeledEntity modeledEntity = ModelEngineAPI.getOrCreateModeledEntity(bukkitEntity);
        ActiveModel activeModel = ModelEngineAPI.createActiveModel(modelId);
        
        if (activeModel != null) {
            modeledEntity.addModel(activeModel, true);
            if (bukkitEntity instanceof LivingEntity living) {
                living.setInvisible(true);
            }
        } else {
            NeomUtilities.getInstance().getLogger().warning("ModelEngine ID not found: " + modelId);
        }
    }
}