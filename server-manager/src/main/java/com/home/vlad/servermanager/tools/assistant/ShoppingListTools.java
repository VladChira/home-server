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

    @Tool(name = "shopping_list_add_item", description = "Add an item to the Home Assistant shopping list. Capitalize first word.")

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

    @Tool(name = "shopping_list_complete_item", description = "Mark an existing shopping list item as completed. " +
            "Arg: id (string, required). You MUST call shopping_list_items first to get IDs.")
    public String completeItem(String itemId) {
        if (itemId == null || itemId.isBlank()) {
            return "Item id is required.";
        }
        haClient.callService(
                "shopping_list",
                "complete_item",
                Map.of("id", itemId));
        return "Item completed: " + itemId;
    }
}
