package com.maxwellwheeler.plugins.tppets.test.command;

import com.maxwellwheeler.plugins.tppets.TPPets;
import com.maxwellwheeler.plugins.tppets.commands.CommandTPP;
import com.maxwellwheeler.plugins.tppets.helpers.LogWrapper;
import com.maxwellwheeler.plugins.tppets.regions.ProtectedRegion;
import com.maxwellwheeler.plugins.tppets.storage.DBWrapper;
import com.maxwellwheeler.plugins.tppets.test.MockFactory;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

public class TPPCommandProtectedRemoveTest {
    private Player admin;
    private ArgumentCaptor<String> stringCaptor;
    private DBWrapper dbWrapper;
    private LogWrapper logWrapper;
    private TPPets tpPets;
    private Command command;
    private CommandTPP commandTPP;

    @BeforeEach
    public void beforeEach() throws SQLException {
        this.admin = MockFactory.getMockPlayer("MockAdminId", "MockAdminName", null, null, new String[]{"tppets.protected"});
        this.stringCaptor = ArgumentCaptor.forClass(String.class);
        this.dbWrapper = mock(DBWrapper.class);
        this.logWrapper = mock(LogWrapper.class);
        this.tpPets = MockFactory.getMockPlugin(this.dbWrapper, this.logWrapper, true, false, true);
        Hashtable<String, List<String>> aliases = new Hashtable<>();
        List<String> altAlias = new ArrayList<>();
        altAlias.add("protected");
        aliases.put("protected", altAlias);
        this.command = mock(Command.class);
        this.commandTPP = new CommandTPP(aliases, tpPets);

        World world = mock(World.class);

        when(this.dbWrapper.getProtectedRegion("ProtectedRegionName")).thenReturn(new ProtectedRegion("ProtectedRegionName", "Enter Message", "MockWorldName", world, new Location(world, 100, 200, 300), new Location(world, 400, 500, 600), "LostAndFoundRegion"));
        when(this.dbWrapper.removeProtectedRegion("ProtectedRegionName")).thenReturn(true);
    }

    @Test
    @DisplayName("Removes a protected region")
    void removesProtectedRegion() throws SQLException {
        String[] args = {"protected", "remove", "ProtectedRegionName"};
        this.commandTPP.onCommand(this.admin, this.command, "", args);

        verify(this.dbWrapper, times(1)).removeProtectedRegion(this.stringCaptor.capture());
        verify(this.dbWrapper, times(1)).getProtectedRegion(this.stringCaptor.capture());
        verify(this.tpPets, times(1)).removeProtectedRegion(this.stringCaptor.capture());

        List<String> capturedRegionNames = this.stringCaptor.getAllValues();
        assertEquals("ProtectedRegionName", capturedRegionNames.get(0));
        assertEquals("ProtectedRegionName", capturedRegionNames.get(1));
        assertEquals("ProtectedRegionName", capturedRegionNames.get(2));

        verify(this.logWrapper, times(1)).logSuccessfulAction(this.stringCaptor.capture());
        String capturedLogOutput = this.stringCaptor.getValue();
        assertEquals("Player MockAdminName removed protected region ProtectedRegionName", capturedLogOutput);

        verify(this.admin, times(1)).sendMessage(this.stringCaptor.capture());
        String capturedMessage = this.stringCaptor.getValue();
        assertEquals("You have removed protected region " + ChatColor.WHITE + "ProtectedRegionName", capturedMessage);
    }

    @Test
    @DisplayName("Can't remove a protected region without its name")
    void cannotRemoveProtectedRegionNoName() throws SQLException {
        String[] args = {"protected", "remove"};
        this.commandTPP.onCommand(this.admin, this.command, "", args);

        verify(this.dbWrapper, never()).removeProtectedRegion(anyString());
        verify(this.dbWrapper, never()).getProtectedRegion(anyString());
        verify(this.tpPets, never()).removeProtectedRegion(anyString());
        verify(this.logWrapper, never()).logSuccessfulAction(anyString());

        verify(this.admin, times(1)).sendMessage(this.stringCaptor.capture());
        String capturedMessage = this.stringCaptor.getValue();
        assertEquals(ChatColor.RED + "Syntax Error! Usage: /tpp pr remove [region name]", capturedMessage);
    }

    @Test
    @DisplayName("Can't remove a protected region without a valid name")
    void cannotRemoveProtectedRegionInvalidName() throws SQLException {
        String[] args = {"protected", "remove", "ProtectedRegionName;"};
        this.commandTPP.onCommand(this.admin, this.command, "", args);

        verify(this.dbWrapper, never()).removeProtectedRegion(anyString());
        verify(this.dbWrapper, never()).getProtectedRegion(anyString());
        verify(this.tpPets, never()).removeProtectedRegion(anyString());
        verify(this.logWrapper, never()).logSuccessfulAction(anyString());

        verify(this.admin, times(1)).sendMessage(this.stringCaptor.capture());
        String capturedMessage = this.stringCaptor.getValue();
        assertEquals(ChatColor.RED + "Invalid region name: " + ChatColor.WHITE + "ProtectedRegionName;", capturedMessage);
    }

    @Test
    @DisplayName("Can't remove a protected region when database fails searching for existing region")
    void cannotRemoveProtectedRegionDbNoGet() throws SQLException {
        when(this.dbWrapper.getProtectedRegion("ProtectedRegionName")).thenThrow(new SQLException());

        String[] args = {"protected", "remove", "ProtectedRegionName"};
        this.commandTPP.onCommand(this.admin, this.command, "", args);

        verify(this.dbWrapper, never()).removeProtectedRegion(anyString());
        verify(this.dbWrapper, times(1)).getProtectedRegion(anyString());
        verify(this.tpPets, never()).removeProtectedRegion(anyString());
        verify(this.logWrapper, never()).logSuccessfulAction(anyString());

        verify(this.admin, times(1)).sendMessage(this.stringCaptor.capture());
        String capturedMessage = this.stringCaptor.getValue();
        assertEquals(ChatColor.RED + "Could not remove protected region", capturedMessage);
    }

    @Test
    @DisplayName("Can't remove a protected region when database already can't find protected region with name")
    void cannotRemoveProtectedRegionAlreadyDone() throws SQLException {
        when(this.dbWrapper.getProtectedRegion("ProtectedRegionName")).thenReturn(null);

        String[] args = {"protected", "remove", "ProtectedRegionName"};
        this.commandTPP.onCommand(this.admin, this.command, "", args);

        verify(this.dbWrapper, never()).removeProtectedRegion(anyString());
        verify(this.dbWrapper, times(1)).getProtectedRegion(anyString());
        verify(this.tpPets, never()).removeProtectedRegion(anyString());
        verify(this.logWrapper, never()).logSuccessfulAction(anyString());

        verify(this.admin, times(1)).sendMessage(this.stringCaptor.capture());
        String capturedMessage = this.stringCaptor.getValue();
        assertEquals(ChatColor.RED + "Protected region " + ChatColor.WHITE + "ProtectedRegionName" + ChatColor.RED + " already does not exist", capturedMessage);
    }

    @Test
    @DisplayName("Can't remove a protected region when database fails to remove")
    void cannotRemoveProtectedRegionDbFailRemove() throws SQLException {
        when(this.dbWrapper.removeProtectedRegion("ProtectedRegionName")).thenReturn(false);

        String[] args = {"protected", "remove", "ProtectedRegionName"};
        this.commandTPP.onCommand(this.admin, this.command, "", args);

        verify(this.dbWrapper, times(1)).removeProtectedRegion(anyString());
        verify(this.dbWrapper, times(1)).getProtectedRegion(anyString());
        verify(this.tpPets, never()).removeProtectedRegion(anyString());
        verify(this.logWrapper, never()).logSuccessfulAction(anyString());

        verify(this.admin, times(1)).sendMessage(this.stringCaptor.capture());
        String capturedMessage = this.stringCaptor.getValue();
        assertEquals(ChatColor.RED + "Could not remove protected region", capturedMessage);
    }
}
