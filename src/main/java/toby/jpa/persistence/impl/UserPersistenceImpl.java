package toby.jpa.persistence.impl;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import toby.jpa.dto.MusicDto;
import toby.jpa.dto.UserDto;
import toby.jpa.persistence.IUserPersistence;
import toby.jpa.service.IMusicFileService;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Repository
@Transactional
public class UserPersistenceImpl implements IUserPersistence {

    private final IMusicFileService musicFileService;

    UserPersistenceImpl(IMusicFileService musicFileService) {
        this.musicFileService = musicFileService;
    }

    @PersistenceContext
    protected EntityManager em;

    public EntityManager getEntityManager() {
        return em;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.em = entityManager;
    }


    @Override
    @SuppressWarnings("unchecked")
    public List<UserDto> listGuildUsers(Long guildId) {
        Query q = em.createNamedQuery("UserDto.getGuildAll", UserDto.class);
        q.setParameter("guildId", guildId);
        return q.getResultList();
    }

    @Override
    public UserDto createNewUser(UserDto userDto) {
        UserDto databaseConfig = em.find(UserDto.class, userDto);
        UserDto result = (databaseConfig == null) ? persistConfigDto(userDto) : databaseConfig;
        createMusicFileEntry(userDto);

        return result;
    }

    private void createMusicFileEntry(UserDto userDto) {
        MusicDto musicDto = new MusicDto(userDto.getDiscordId(), userDto.getGuildId(), null, null);
        em.persist(musicDto);
        em.flush();
    }

    @Override
    public UserDto getUserById(Long discordId, Long guildId) {
        Query userQuery = em.createNamedQuery("UserDto.getById", UserDto.class);
        userQuery.setParameter("discordId", discordId);
        userQuery.setParameter("guildId", guildId);

        UserDto dbUser = (UserDto) userQuery.getSingleResult();

        Query musicQuery = em.createNamedQuery("MusicDto.getById", MusicDto.class);
        musicQuery.setParameter("id", dbUser.getMusicId());
        MusicDto dbMusicFile = (MusicDto) musicQuery.getSingleResult();
        dbUser.setMusicDto(dbMusicFile);
        return dbUser;
    }

    @Override
    public UserDto updateUser(UserDto userDto) {
        MusicDto musicFileDto = userDto.getMusicDto();
        MusicDto databaseMusicFile = musicFileService.getMusicFileById(userDto.getMusicId());

        if (musicFileDto != null && musicFileDto.getMusicBlob() != null && (!musicFileDto.getMusicBlob().equals(databaseMusicFile.getMusicBlob()))) {
            em.merge(musicFileDto);
        }
        em.merge(userDto);
        em.flush();
        return userDto;
    }

    @Override
    public void deleteUser(UserDto userDto) {
        em.remove(userDto.getMusicDto());
        em.remove(userDto);
        em.flush();
    }

    @Override
    public void deleteUserById(Long discordId, Long guildId) {

        Query userQuery = em.createNamedQuery("UserDto.deleteById");
        userQuery.setParameter("discordId", discordId);
        userQuery.setParameter("guildId", guildId);
        userQuery.executeUpdate();

    }

    private UserDto persistConfigDto(UserDto userDto) {
        em.persist(userDto);
        em.flush();
        return userDto;
    }
}
