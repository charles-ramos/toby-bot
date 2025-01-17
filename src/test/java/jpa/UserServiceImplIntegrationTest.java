package jpa;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import toby.Application;
import toby.jpa.dto.MusicDto;
import toby.jpa.dto.UserDto;
import toby.jpa.persistence.IMusicFilePersistence;
import toby.jpa.persistence.IUserPersistence;
import toby.jpa.service.IMusicFileService;
import toby.jpa.service.IUserService;
import toby.jpa.service.impl.MusicFileServiceImpl;
import toby.jpa.service.impl.UserServiceImpl;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = Application.class)
public class UserServiceImplIntegrationTest {

    @Bean
    public IUserService userService() {
        return new UserServiceImpl();
    }

    @Autowired
    private IUserService userService;

    @Autowired
    private IUserPersistence userPersistence;

    @Bean
    public IMusicFileService musicFileService() {
        return new MusicFileServiceImpl();
    }

    @Autowired
    private IMusicFileService musicFileService;

    @Autowired
    private IMusicFilePersistence musicFilePersistence;


    @BeforeEach
    public void setUp() {
        userService.deleteUserById(1L, 1L);
        musicFileService.deleteMusicFileById("1_1");
    }

    @AfterEach
    public void tearDown(){
        userService.deleteUserById(1L, 1L);
        musicFileService.deleteMusicFileById("1_1");
    }

    @Test
    public void whenValidDiscordIdAndGuild_thenUserShouldBeFound() {
        UserDto userDto = new UserDto();
        userDto.setDiscordId(1L);
        userDto.setGuildId(1L);
        MusicDto musicDto = new MusicDto(userDto.getDiscordId(), userDto.getGuildId(), null, 0, null);
        userDto.setMusicDto(musicDto);
        userService.createNewUser(userDto);
        UserDto dbUser = userService.getUserById(userDto.getDiscordId(), userDto.getGuildId());

        assertEquals(dbUser.getDiscordId(),userDto.getDiscordId());
        assertEquals(dbUser.getGuildId(),userDto.getGuildId());
        assertTrue(dbUser.hasMusicPermission());
        assertTrue(dbUser.hasMemePermission());
        assertTrue(dbUser.hasDigPermission());
        assertFalse(dbUser.isSuperUser());
        assertNotNull(dbUser.getMusicDto());
    }

    @Test
    @Transactional
    public void testUpdate_thenNewUserValuesShouldBeReturned() {
        UserDto userDto1 = new UserDto();
        userDto1.setDiscordId(1L);
        userDto1.setGuildId(1L);
        MusicDto musicDto1 = new MusicDto(userDto1.getDiscordId(), userDto1.getGuildId(), null, 0, null);
        userDto1.setMusicDto(musicDto1);
        userDto1 = userService.createNewUser(userDto1);
        UserDto dbUser1 = userService.getUserById(userDto1.getDiscordId(), userDto1.getGuildId());

        assertEquals(dbUser1.getDiscordId(),userDto1.getDiscordId());
        assertEquals(dbUser1.getGuildId(),userDto1.getGuildId());
        assertTrue(dbUser1.hasMusicPermission());
        assertTrue(dbUser1.hasMemePermission());
        assertTrue(dbUser1.hasDigPermission());
        assertFalse(dbUser1.isSuperUser());
        assertNotNull(dbUser1.getMusicDto());

        UserDto userDto2 = new UserDto();
        userDto2.setDiscordId(1L);
        userDto2.setGuildId(1L);
        MusicDto musicDto2 = new MusicDto(userDto2.getDiscordId(), userDto2.getGuildId(), null, 0, null);
        userDto2.setMusicDto(musicDto2);
        userDto2.setDigPermission(false);
        userDto2 = userService.updateUser(userDto2);
        UserDto dbUser2 = userService.getUserById(userDto2.getDiscordId(), userDto2.getGuildId());

        int guildMemberSize = userService.listGuildUsers(userDto2.getGuildId()).size();

        assertEquals(dbUser2.getDiscordId(),userDto2.getDiscordId());
        assertEquals(dbUser2.getGuildId(),userDto2.getGuildId());
        assertTrue(dbUser2.hasMusicPermission());
        assertTrue(dbUser2.hasMemePermission());
        assertFalse(dbUser2.hasDigPermission());
        assertFalse(dbUser2.isSuperUser());
        assertNotNull(dbUser2.getMusicDto());
        assertEquals(1, guildMemberSize);
    }


    @Test
    public void whenMusicFileExistsWithSameDiscordIdAndGuild_thenUserShouldBeFoundWithMusicFile() {
        UserDto userDto = new UserDto();
        userDto.setDiscordId(1L);
        userDto.setGuildId(1L);
        MusicDto musicDto = new MusicDto(userDto.getDiscordId(), userDto.getGuildId(), "test", 0, null);
        userDto.setMusicDto(musicDto);
        userService.createNewUser(userDto);
        UserDto dbUser = userService.getUserById(userDto.getDiscordId(), userDto.getGuildId());

        assertEquals(dbUser.getDiscordId(),userDto.getDiscordId());
        assertEquals(dbUser.getGuildId(),userDto.getGuildId());
        assertTrue(dbUser.hasMusicPermission());
        assertTrue(dbUser.hasMemePermission());
        assertTrue(dbUser.hasDigPermission());
        assertFalse(dbUser.isSuperUser());
        MusicDto dbMusicFileDto = userDto.getMusicDto();
        assertNotNull(dbMusicFileDto);
        assertEquals(dbMusicFileDto.getId(), musicDto.getId());
        assertEquals(dbMusicFileDto.getFileName(), musicDto.getFileName());
    }

    @Test
    public void whenMusicFileExistsWithSameDiscordIdAndGuildAndUpdatedOnce_thenUserShouldBeFoundWithMusicFile() {
        UserDto userDto = new UserDto();
        userDto.setDiscordId(1L);
        userDto.setGuildId(1L);
        MusicDto musicDto = new MusicDto(userDto.getDiscordId(), userDto.getGuildId(), null, 0, null);
        userDto.setMusicDto(musicDto);
        userService.createNewUser(userDto);
        UserDto dbUser = userService.getUserById(userDto.getDiscordId(), userDto.getGuildId());

        assertEquals(dbUser.getDiscordId(),userDto.getDiscordId());
        assertEquals(dbUser.getGuildId(),userDto.getGuildId());
        assertTrue(dbUser.hasMusicPermission());
        assertTrue(dbUser.hasMemePermission());
        assertTrue(dbUser.hasDigPermission());
        assertFalse(dbUser.isSuperUser());
        MusicDto dbMusicFileDto = userDto.getMusicDto();
        assertNotNull(dbMusicFileDto);
        assertEquals(dbMusicFileDto.getId(), musicDto.getId());
        assertEquals(dbMusicFileDto.getFileName(), musicDto.getFileName());


        dbMusicFileDto.setFileName("file name");
        dbMusicFileDto.setMusicBlob("test data".getBytes());
        userDto.setMusicDto(dbMusicFileDto);
        UserDto dbUser2 = userService.updateUser(userDto);


        assertEquals(dbUser2.getDiscordId(),userDto.getDiscordId());
        assertEquals(dbUser2.getGuildId(),userDto.getGuildId());
        assertTrue(dbUser2.hasMusicPermission());
        assertTrue(dbUser2.hasMemePermission());
        assertTrue(dbUser2.hasDigPermission());
        assertFalse(dbUser2.isSuperUser());
        dbMusicFileDto = dbUser2.getMusicDto();
        assertNotNull(dbMusicFileDto);
        assertEquals(dbMusicFileDto.getId(), musicDto.getId());
        assertEquals(dbMusicFileDto.getFileName(), musicDto.getFileName());

    }
}
