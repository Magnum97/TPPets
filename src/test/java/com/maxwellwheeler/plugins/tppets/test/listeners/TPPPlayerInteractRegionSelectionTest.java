package com.maxwellwheeler.plugins.tppets.test.listeners;

import com.maxwellwheeler.plugins.tppets.TPPets;
import com.maxwellwheeler.plugins.tppets.helpers.LogWrapper;
import com.maxwellwheeler.plugins.tppets.helpers.ToolsManager;
import com.maxwellwheeler.plugins.tppets.listeners.PlayerInteractRegionSelection;
import com.maxwellwheeler.plugins.tppets.regions.RegionSelectionManager;
import com.maxwellwheeler.plugins.tppets.storage.DBWrapper;
import com.maxwellwheeler.plugins.tppets.test.MockFactory;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TPPPlayerInteractRegionSelectionTest {
    private PlayerInteractRegionSelection playerInteractRegionSelection;
    private PlayerInteractEvent playerInteractEvent;
    private Player player;
    private ToolsManager toolsManager;
    private RegionSelectionManager regionSelectionManager;
    private Location blockLocation;

    @BeforeEach
    public void beforeEach() {
        DBWrapper dbWrapper = mock(DBWrapper.class);
        LogWrapper logWrapper = mock(LogWrapper.class);
        TPPets tpPets = MockFactory.getMockPlugin(dbWrapper, logWrapper, false, false, false);
        this.toolsManager = mock(ToolsManager.class);
        this.playerInteractEvent = mock(PlayerInteractEvent.class);
        Block blockClicked = mock(Block.class);
        World world = mock(World.class);
        this.blockLocation = MockFactory.getMockLocation(world, 10, 20, 30);
        this.regionSelectionManager = new RegionSelectionManager();
        this.player = MockFactory.getMockPlayer("MockPlayerId", "MockPlayerName", null, null, new String[]{});

        when(this.toolsManager.isMaterialValidTool("select_region", Material.BLAZE_ROD)).thenReturn(true);
        when(this.playerInteractEvent.getMaterial()).thenReturn(Material.BLAZE_ROD);
        when(blockClicked.getLocation()).thenReturn(this.blockLocation);
        when(this.playerInteractEvent.getClickedBlock()).thenReturn(blockClicked);
        when(this.playerInteractEvent.getPlayer()).thenReturn(this.player);
        when(tpPets.getToolsManager()).thenReturn(this.toolsManager);
        when(tpPets.getRegionSelectionManager()).thenReturn(this.regionSelectionManager);

        this.playerInteractRegionSelection = new PlayerInteractRegionSelection(tpPets);
    }

    @Test
    @DisplayName("Sets first position through left clicks")
    void setsFirstPosition() {
        when(this.playerInteractEvent.getAction()).thenReturn(Action.LEFT_CLICK_BLOCK);

        this.playerInteractRegionSelection.onPlayerInteract(this.playerInteractEvent);

        verify(this.player, times(1)).sendMessage(ChatColor.BLUE + "First position set!");

        assertNotNull(this.regionSelectionManager.getSelectionSession(this.player));
        assertFalse(this.regionSelectionManager.getSelectionSession(this.player).isCompleteSelection());

        this.regionSelectionManager.setEndLocation(this.player, this.blockLocation);
        assertTrue(this.regionSelectionManager.getSelectionSession(this.player).isCompleteSelection());
    }

    @Test
    @DisplayName("Sets first and final position through left clicks")
    void setsFirstFinalPosition() {
        when(this.playerInteractEvent.getAction()).thenReturn(Action.LEFT_CLICK_BLOCK);
        this.regionSelectionManager.setEndLocation(this.player, this.blockLocation);

        this.playerInteractRegionSelection.onPlayerInteract(this.playerInteractEvent);

        verify(this.player, times(1)).sendMessage(ChatColor.BLUE + "First position set! Selection is complete.");

        assertNotNull(this.regionSelectionManager.getSelectionSession(this.player));
        assertTrue(this.regionSelectionManager.getSelectionSession(this.player).isCompleteSelection());
    }

    @Test
    @DisplayName("Sets second position through right clicks")
    void setsSecondPosition() {
        when(this.playerInteractEvent.getAction()).thenReturn(Action.RIGHT_CLICK_BLOCK);

        this.playerInteractRegionSelection.onPlayerInteract(this.playerInteractEvent);

        verify(this.player, times(1)).sendMessage(ChatColor.BLUE + "Second position set!");

        assertNotNull(this.regionSelectionManager.getSelectionSession(this.player));
        assertFalse(this.regionSelectionManager.getSelectionSession(this.player).isCompleteSelection());

        this.regionSelectionManager.setStartLocation(this.player, this.blockLocation);
        assertTrue(this.regionSelectionManager.getSelectionSession(this.player).isCompleteSelection());
    }

    @Test
    @DisplayName("Sets second and final position through left clicks")
    void setsSecondFinalPosition() {
        when(this.playerInteractEvent.getAction()).thenReturn(Action.RIGHT_CLICK_BLOCK);
        this.regionSelectionManager.setStartLocation(this.player, this.blockLocation);

        this.playerInteractRegionSelection.onPlayerInteract(this.playerInteractEvent);

        verify(this.player, times(1)).sendMessage(ChatColor.BLUE + "Second position set! Selection is complete.");

        assertNotNull(this.regionSelectionManager.getSelectionSession(this.player));
        assertTrue(this.regionSelectionManager.getSelectionSession(this.player).isCompleteSelection());
    }

    @Test
    @DisplayName("Doesn't set position when action is neither right click or left click block")
    void cannotSetPositionInvalidAction() {
        when(this.playerInteractEvent.getAction()).thenReturn(Action.LEFT_CLICK_AIR);

        this.playerInteractRegionSelection.onPlayerInteract(this.playerInteractEvent);

        verify(this.player, never()).sendMessage(anyString());
        assertNull(this.regionSelectionManager.getSelectionSession(this.player));
    }

    @Test
    @DisplayName("Doesn't set position when clicking with invalid tool")
    void cannotSetPositionInvalidTool() {
        when(this.playerInteractEvent.getMaterial()).thenReturn(Material.BEETROOT);

        this.playerInteractRegionSelection.onPlayerInteract(this.playerInteractEvent);

        verify(this.player, never()).sendMessage(anyString());
        assertNull(this.regionSelectionManager.getSelectionSession(this.player));
    }

    @Test
    @DisplayName("Doesn't set position when clicking a null block")
    void cannotSetPositionInvalidBlock() {
        when(this.playerInteractEvent.getClickedBlock()).thenReturn(null);

        this.playerInteractRegionSelection.onPlayerInteract(this.playerInteractEvent);

        verify(this.player, never()).sendMessage(anyString());
        assertNull(this.regionSelectionManager.getSelectionSession(this.player));
    }

    @Test
    @DisplayName("Clears player session on quit")
    void clearsPlayerSessionOnQuit() {
        PlayerQuitEvent playerQuitEvent = mock(PlayerQuitEvent.class);
        when(playerQuitEvent.getPlayer()).thenReturn(this.player);

        this.regionSelectionManager.setEndLocation(this.player, this.blockLocation);
        assertNotNull(this.regionSelectionManager.getSelectionSession(this.player));

        this.playerInteractRegionSelection.onPlayerQuit(playerQuitEvent);

        verify(this.player, never()).sendMessage(anyString());
        assertNull(this.regionSelectionManager.getSelectionSession(this.player));
    }
}
