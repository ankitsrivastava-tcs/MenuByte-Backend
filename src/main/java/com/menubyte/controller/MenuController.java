/**
 * Controller for managing Menus.
 * Handles retrieval of menus for specific businesses owned by users.
 *
 * @author Ankit
 */
package com.menubyte.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.menubyte.dto.MenuDTO;
import com.menubyte.entity.BusinessMaster;
import com.menubyte.entity.Menu;
import com.menubyte.entity.User;
import com.menubyte.enums.SubscriptionStatus;
import com.menubyte.service.BusinessMasterService;
import com.menubyte.service.MenuService;
import com.menubyte.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/menus")
public class MenuController {

    private final MenuService menuService;
    private final UserService userService;
    private final BusinessMasterService businessMasterService;

    public MenuController(MenuService menuService, UserService userService, BusinessMasterService businessMasterService) {
        this.menuService = menuService;
        this.userService = userService;
        this.businessMasterService = businessMasterService;
    }

    /**
     * Retrieves the menu for a specific business if the logged-in user owns it.
     *
     * @param businessId The ID of the business.
     * @param userId     The ID of the logged-in user.
     * @return The corresponding MenuDTO object.
     */
    @GetMapping("/{businessId}")
    public ResponseEntity<MenuDTO> getMenuForUserBusiness(@PathVariable Long businessId, @RequestParam Long userId) {
        User user = userService.getUserById(userId);
        Menu menu = menuService.getMenuForUserBusiness(businessId, user);
        MenuDTO menuDTO = new MenuDTO(menu);
        BusinessMaster businessMaster = businessMasterService.getBusinessesByBusinessID(businessId);
        if (null != businessMaster && businessMaster.getSubscriptionStatus().name().equalsIgnoreCase(String.valueOf(SubscriptionStatus.INACTIVE))) {
            menuDTO.setSubscriptionStatus(SubscriptionStatus.INACTIVE);
        } else {
            menuDTO.setSubscriptionStatus(SubscriptionStatus.ACTIVE);

            try {
                ObjectMapper objectMapper = new ObjectMapper();
                String jsonResponse = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(menuDTO);
                log.info("MenuDTO Response:\n{}", jsonResponse); // Pretty JSON logging
            } catch (Exception e) {
                log.error("Error converting MenuDTO to JSON", e);
            }

        }
        return ResponseEntity.ok(menuDTO);

    }

    /**
     * Updates the items of a menu for a specific business.
     * @param businessId The ID of the business.
     * @param userId The ID of the logged-in user.
     * @param updatedMenu The updated menu object with new items.
     * @return The updated MenuDTO object.
     */
    @PutMapping("/{businessId}")
    public ResponseEntity<MenuDTO> updateMenuItems(
            @PathVariable Long businessId,
            @RequestParam Long userId,
            @RequestBody MenuDTO updatedMenu) {
        System.out.println("Received Update Request: " + updatedMenu); // Debugging
        User user = userService.getUserById(userId);
        Menu updatedMenuEntity = menuService.updateMenuItems(businessId, user, updatedMenu);
        return ResponseEntity.ok(new MenuDTO(updatedMenuEntity));
    }


}
