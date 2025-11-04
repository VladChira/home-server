package com.home.vlad.servermanager.tools.assistant;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.home.vlad.servermanager.service.assistant.HomeAssistantClient;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ShoppingListTools extends BaseHomeAssistantTools {
    public ShoppingListTools(HomeAssistantClient haClient) {
        super(haClient);
    }

    @Tool(name = "shopping_list_items", description = "List current shopping list items.")
    public String listItems() {
        JsonNode root = haClient.getShoppingList();
        List<Map<String, Object>> items = new ArrayList<>();
        if (root != null && root.isArray()) {
            for (JsonNode n : root) {
                boolean completed = n.path("completed").asBoolean(false);
                if (completed) {
                    continue;
                }
                items.add(Map.of(
                        "id", n.path("id").asText(),
                        "name", n.path("name").asText()));
            }
        }
        return items.toString();
    }

    @Tool(name = "shopping_list_add_item", description = "Add an item to the Home Assistant shopping list.")

    public String addItem(String itemName) {
        if (itemName == null || itemName.isBlank()) {
            return "Item name is required.";
        }
        haClient.callService(
                "shopping_list",
                "add_item",
                Map.of("name", itemName));
        return "Item added: " + itemName;
    }

    @Tool(name = "shopping_list_remove_item", description = "Remove an existing item from the shopping list by name. You MUST CALL shopping_list_items FIRST to get the correct names.")
    public String removeItem(String name) {
        if (name == null || name.isBlank()) {
            return "Item name is required.";
        }
        haClient.callService(
                "shopping_list",
                "remove_item",
                Map.of("name", name));
        return "Item removed: " + name;
    }
}
